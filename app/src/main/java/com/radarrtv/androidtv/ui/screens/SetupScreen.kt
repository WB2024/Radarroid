package com.radarrtv.androidtv.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radarrtv.androidtv.data.auth.AuthHelper
import com.radarrtv.androidtv.data.preferences.UserPreferences
import com.radarrtv.androidtv.ui.components.TvFocusButton
import com.radarrtv.androidtv.ui.components.TvTextField
import com.radarrtv.androidtv.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(prefs: UserPreferences, onComplete: () -> Unit) {
    var serverUrl by remember { mutableStateOf("http://") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var connecting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(480.dp)
                .verticalScroll(rememberScrollState())
                .padding(32.dp)
        ) {
            Text(
                "RADARR",
                color = RadarrBlue,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
            Spacer(Modifier.height(4.dp))
            Text("Android TV", color = RadarrMuted, style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(40.dp))

            TvTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = "Server URL",
                placeholder = "http://192.168.1.x:7878"
            )

            Spacer(Modifier.height(16.dp))

            TvTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username  (blank if auth is disabled)",
                placeholder = "admin"
            )

            Spacer(Modifier.height(12.dp))

            TvTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "••••••••",
                isPassword = true
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "Leave username and password blank if your Radarr has no authentication configured.",
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrMuted
            )

            Spacer(Modifier.height(20.dp))

            TvTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = "API Key (optional — overrides username/password)",
                placeholder = "Paste from Radarr → Settings → General"
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "If login fails, find your API key in Radarr under Settings → General and paste it above.",
                style = MaterialTheme.typography.bodyMedium,
                color = RadarrMuted
            )

            Spacer(Modifier.height(28.dp))

            if (status.isNotEmpty()) {
                Text(
                    status,
                    color = if (isError) RadarrRed else RadarrGreen,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            TvFocusButton(
                onClick = {
                    if (serverUrl.isBlank() || serverUrl == "http://") {
                        status = "Please enter a server URL"
                        isError = true
                        return@TvFocusButton
                    }
                    val url = serverUrl.trimEnd('/')
                        .let { if (!it.startsWith("http")) "http://$it" else it }

                    scope.launch {
                        connecting = true
                        isError = false
                        status = "Connecting…"
                        try {
                            val auth = AuthHelper(url)
                            val resolvedApiKey = when {
                                apiKey.isNotBlank() -> {
                                    status = "Verifying API key…"
                                    apiKey.trim()
                                }
                                username.isBlank() -> {
                                    status = "Checking server…"
                                    auth.tryNoAuth() ?: run {
                                        status = "Server requires authentication — enter username/password or API key"
                                        isError = true
                                        connecting = false
                                        return@launch
                                    }
                                }
                                else -> {
                                    status = "Logging in…"
                                    auth.loginAndGetApiKey(username, password)
                                }
                            }
                            status = "Connected!"
                            prefs.serverUrl = url
                            prefs.apiKey = resolvedApiKey
                            onComplete()
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            status = e.message ?: "Connection failed"
                            isError = true
                        } finally {
                            connecting = false
                        }
                    }
                },
                isPrimary = true,
                enabled = !connecting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (connecting) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = RadarrWhite, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Text(if (connecting) "Connecting…" else "Connect")
            }
        }
    }
}
