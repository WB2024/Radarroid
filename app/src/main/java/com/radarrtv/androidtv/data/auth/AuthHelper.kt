package com.radarrtv.androidtv.data.auth

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AuthHelper(private val serverUrl: String) {
    private val base = serverUrl.trimEnd('/')

    // Try without credentials — works when auth is disabled or DisabledForLocalAddresses.
    suspend fun tryNoAuth(): String? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .followRedirects(false)
                .build()
            val resp = client.newCall(
                Request.Builder().url("$base/api/v3/config/host").get().build()
            ).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: return@withContext null
                JSONObject(body).optString("apiKey").takeIf { it.isNotBlank() }
            } else null
        } catch (_: Exception) { null }
    }

    // Authenticate with username/password. Tries three strategies in order:
    //   1. No-auth (handles DisabledForLocalAddresses)
    //   2. HTTP Basic auth (handles AuthenticationMethod=Basic)
    //   3. Form-based cookie login (handles AuthenticationMethod=Forms)
    suspend fun loginAndGetApiKey(username: String, password: String): String = withContext(Dispatchers.IO) {
        // Strategy 1: no credentials (local network bypass)
        tryNoAuth()?.let { return@withContext it }

        // Strategy 2: HTTP Basic auth
        tryBasicAuth(username, password)?.let { return@withContext it }

        // Strategy 3: form-based cookie login
        tryFormLogin(username, password)
    }

    private fun makeClient(
        cookieJar: CookieJar? = null,
        followRedirects: Boolean = true
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(followRedirects)
        .apply { cookieJar?.let { cookieJar(it) } }
        .build()

    private fun basicHeader(username: String, password: String): String {
        val creds = Base64.encodeToString("$username:$password".toByteArray(), Base64.NO_WRAP)
        return "Basic $creds"
    }

    private fun extractApiKey(body: String): String? = try {
        JSONObject(body).optString("apiKey").takeIf { it.isNotBlank() }
    } catch (_: Exception) { null }

    private fun isHtml(body: String) = body.trimStart().startsWith("<")

    private fun checkNotHtml(body: String) {
        if (isHtml(body))
            throw Exception("Server returned a web page instead of data — check the URL includes the correct port (e.g. http://192.168.1.250:7878)")
    }

    // When Radarr is configured for Basic authentication, /initialize.json accepts
    // an Authorization: Basic header and returns the apiKey. API endpoints (/api/v3/...)
    // only accept X-Api-Key, so we use the web UI endpoint here instead.
    private fun tryBasicAuth(username: String, password: String): String? {
        return try {
            val resp = makeClient(followRedirects = false).newCall(
                Request.Builder()
                    .url("$base/initialize.json")
                    .header("Authorization", basicHeader(username, password))
                    .get()
                    .build()
            ).execute()
            if (resp.isSuccessful) extractApiKey(resp.body?.string() ?: "") else null
        } catch (_: Exception) { null }
    }

    private fun tryFormLogin(username: String, password: String): String {
        val cookieStore = mutableMapOf<String, List<Cookie>>()
        val cookieJar = object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = (cookieStore[url.host] ?: emptyList()) + cookies
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> =
                cookieStore[url.host] ?: emptyList()
        }

        // Don't follow redirects on login POST so we can inspect the Location header.
        // A successful Radarr/Sonarr login always responds 302; a 200 with HTML body
        // means we're hitting the wrong server or port.
        val loginClient = makeClient(cookieJar, followRedirects = false)
        val formBody = FormBody.Builder()
            .add("Username", username)
            .add("Password", password)
            .build()
        val loginResp = loginClient.newCall(
            Request.Builder().url("$base/login").post(formBody).build()
        ).execute()

        if (loginResp.code == 200) {
            val body = loginResp.body?.string() ?: ""
            if (isHtml(body))
                throw Exception("Server returned a web page — check the URL includes the correct port (e.g. http://192.168.1.250:7878)")
        }

        val location = loginResp.header("Location") ?: ""
        when {
            loginResp.code == 401 || loginResp.code == 403 ->
                throw Exception("Login failed — check username and password")
            location.contains("loginFailed=true", ignoreCase = true) ->
                throw Exception("Login failed — check username and password")
            loginResp.code == 500 ->
                throw Exception("Login failed (500) — if Radarr uses Basic auth, enter your API key directly instead of username/password")
            loginResp.code !in 200..399 ->
                throw Exception("Login failed (${loginResp.code})")
        }

        // API endpoints require an API key, not a cookie. /initialize.json uses the UI
        // policy which accepts cookie auth and returns the API key.
        val initResp = makeClient(cookieJar).newCall(
            Request.Builder().url("$base/initialize.json").get().build()
        ).execute()

        if (!initResp.isSuccessful)
            throw Exception("Logged in but couldn't retrieve API key (${initResp.code})")

        val initBody = initResp.body?.string() ?: ""
        checkNotHtml(initBody)
        return try { JSONObject(initBody).optString("apiKey").takeIf { it.isNotBlank() } }
            catch (_: Exception) { null }
            ?: throw Exception("API key not found — Radarr may need a restart")
    }

    suspend fun ping(): Boolean = withContext(Dispatchers.IO) {
        try {
            val resp = makeClient(followRedirects = false).newCall(
                Request.Builder().url("$base/api/v3/system/status").get().build()
            ).execute()
            resp.code in 200..403
        } catch (_: Exception) { false }
    }
}
