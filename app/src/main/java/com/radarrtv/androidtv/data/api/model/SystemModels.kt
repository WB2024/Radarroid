package com.radarrtv.androidtv.data.api.model

data class SystemStatus(
    val appName: String = "",
    val instanceName: String = "",
    val version: String = "",
    val buildTime: String = "",
    val isDebug: Boolean = false,
    val isProduction: Boolean = true,
    val isAdmin: Boolean = false,
    val isUserInteractive: Boolean = false,
    val startupPath: String = "",
    val appData: String = "",
    val osName: String = "",
    val osVersion: String = "",
    val isNetCore: Boolean = true,
    val isLinux: Boolean = false,
    val isOsx: Boolean = false,
    val isWindows: Boolean = false,
    val isDocker: Boolean = false,
    val mode: String = "",
    val branch: String = "",
    val authentication: String = "",
    val sqliteVersion: String = "",
    val migrationVersion: Int = 0,
    val urlBase: String = "",
    val runtimeVersion: String = "",
    val runtimeName: String = "",
    val startTime: String = "",
    val packageVersion: String? = null,
    val packageAuthor: String? = null,
    val packageUpdateMechanism: String = ""
)

data class HealthCheck(
    val source: String = "",
    val type: String = "",
    val message: String = "",
    val wikiUrl: String? = null
)

data class DiskSpace(
    val path: String = "",
    val label: String = "",
    val freeSpace: Long = 0,
    val totalSpace: Long = 0
) {
    val usedSpace: Long get() = totalSpace - freeSpace
    val usedPct: Double get() = if (totalSpace > 0) (usedSpace.toDouble() / totalSpace) * 100.0 else 0.0
}

data class ScheduledTask(
    val id: Int = 0,
    val name: String = "",
    val taskName: String = "",
    val interval: Int = 0,
    val lastExecution: String = "",
    val lastStartTime: String = "",
    val nextExecution: String = "",
    val lastDuration: String = "",
    val isManual: Boolean = false
)

data class LogEntry(
    val id: Int = 0,
    val time: String = "",
    val exception: String? = null,
    val exceptionType: String? = null,
    val level: String = "",
    val logger: String = "",
    val message: String = "",
    val method: String? = null
)

data class LogResponse(
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalRecords: Int = 0,
    val records: List<LogEntry> = emptyList()
)

data class Command(
    val id: Int = 0,
    val name: String = "",
    val commandName: String = "",
    val message: String? = null,
    val priority: String = "",
    val status: String = "",
    val queued: String = "",
    val started: String? = null,
    val ended: String? = null,
    val duration: String? = null,
    val trigger: String = "",
    val clientUserAgent: String? = null,
    val stateChangeTime: String? = null,
    val sendUpdatesToClient: Boolean = false,
    val updateScheduledTask: Boolean = false,
    val lastExecutionTime: String? = null
)

data class CommandRequest(
    val name: String,
    val movieIds: List<Int>? = null,
    val movieId: Int? = null
)

data class WantedResponse(
    val page: Int = 1,
    val pageSize: Int = 20,
    val sortKey: String? = null,
    val sortDirection: String? = null,
    val totalRecords: Int = 0,
    val records: List<Movie> = emptyList()
)

data class BlocklistResponse(
    val page: Int = 1,
    val pageSize: Int = 20,
    val sortKey: String? = null,
    val sortDirection: String? = null,
    val totalRecords: Int = 0,
    val records: List<BlocklistItem> = emptyList()
)

data class BlocklistItem(
    val id: Int = 0,
    val movieId: Int = 0,
    val movie: Movie? = null,
    val sourceTitle: String = "",
    val quality: QualityModel? = null,
    val languages: List<Language> = emptyList(),
    val date: String = "",
    val protocol: String = "",
    val indexer: String? = null,
    val message: String? = null
)
