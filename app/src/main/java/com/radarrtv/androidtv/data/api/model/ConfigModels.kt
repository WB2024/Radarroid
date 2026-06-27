package com.radarrtv.androidtv.data.api.model

data class HostConfig(
    val id: Int = 0,
    val bindAddress: String = "*",
    val port: Int = 7878,
    val sslPort: Int = 9898,
    val enableSsl: Boolean = false,
    val launchBrowser: Boolean = true,
    val authenticationMethod: String = "none",
    val authenticationRequired: String = "enabled",
    val analyticsEnabled: Boolean = true,
    val username: String? = null,
    val password: String? = null,
    val passwordConfirmation: String? = null,
    val logLevel: String = "info",
    val consoleLogLevel: String? = null,
    val branch: String = "master",
    val apiKey: String = "",
    val sslCertPath: String? = null,
    val sslCertPassword: String? = null,
    val urlBase: String = "",
    val instanceName: String = "Radarr",
    val applicationUrl: String = "",
    val updateAutomatically: Boolean = false,
    val updateMechanism: String = "builtIn",
    val updateScriptPath: String? = null,
    val proxyEnabled: Boolean = false,
    val proxyType: String = "http",
    val proxyHostname: String? = null,
    val proxyPort: Int = 8080,
    val proxyUsername: String? = null,
    val proxyPassword: String? = null,
    val proxyBypassFilter: String? = null,
    val proxyBypassLocalAddresses: Boolean = true,
    val certificateValidation: String = "enabled",
    val backupFolder: String = "Backups",
    val backupInterval: Int = 7,
    val backupRetention: Int = 28
)

data class UiConfig(
    val id: Int = 0,
    val movieInfoLanguage: Int = 1,
    val uiLanguage: Int = 1,
    val calendarWeekColumnHeader: String = "ddd M/D",
    val shortDateFormat: String = "MMM D YYYY",
    val longDateFormat: String = "dddd, MMMM D YYYY",
    val timeFormat: String = "h(:mm)a",
    val showRelativeDates: Boolean = true,
    val enableColorImpairedMode: Boolean = false,
    val firstDayOfWeek: Int = 0,
    val movieRuntimeFormat: String = "hoursMinutes",
    val theme: String = "auto"
)

data class NamingConfig(
    val id: Int = 0,
    val renameMovies: Boolean = false,
    val replaceIllegalCharacters: Boolean = true,
    val colonReplacementFormat: String = "delete",
    val standardMovieFormat: String = "{Movie Title} ({Release Year}) {Quality Full}",
    val movieFolderFormat: String = "{Movie Title} ({Release Year})"
)

data class MediaManagementConfig(
    val id: Int = 0,
    val autoUnmonitorPreviouslyDownloadedMovies: Boolean = false,
    val recycleBin: String = "",
    val recycleBinCleanupDays: Int = 7,
    val downloadPropersAndRepacks: String = "preferAndUpgrade",
    val createEmptyMovieFolders: Boolean = false,
    val deleteEmptyFolders: Boolean = false,
    val fileDate: String = "none",
    val rescanAfterRefresh: String = "always",
    val autoRenameFolders: Boolean = false,
    val pathsDefaultStatic: Boolean = false,
    val setPermissionsLinux: Boolean = false,
    val chmodFolder: String = "755",
    val chownGroup: String = "",
    val skipFreeSpaceCheckWhenImporting: Boolean = false,
    val minimumFreeSpaceWhenImporting: Int = 100,
    val copyUsingHardlinks: Boolean = true,
    val useScriptImport: Boolean = false,
    val scriptImportPath: String = "",
    val importExtraFiles: Boolean = false,
    val extraFileExtensions: String = "srt",
    val enableMediaInfo: Boolean = true
)

data class NamingExample(
    val movieExample: String = "",
    val movieFolderExample: String = ""
)
