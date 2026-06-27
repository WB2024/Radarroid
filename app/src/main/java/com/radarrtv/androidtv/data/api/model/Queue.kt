package com.radarrtv.androidtv.data.api.model

data class QueueResponse(
    val page: Int = 1,
    val pageSize: Int = 20,
    val sortKey: String? = null,
    val sortDirection: String? = null,
    val totalRecords: Int = 0,
    val records: List<QueueItem> = emptyList()
)

data class QueueItem(
    val id: Int = 0,
    val movieId: Int? = null,
    val movie: Movie? = null,
    val title: String = "",
    val size: Double = 0.0,
    val sizeleft: Double = 0.0,
    val timeleft: String? = null,
    val estimatedCompletionTime: String? = null,
    val status: String = "",
    val trackedDownloadStatus: String? = null,
    val trackedDownloadState: String? = null,
    val statusMessages: List<StatusMessage> = emptyList(),
    val downloadId: String? = null,
    val protocol: String = "",
    val downloadClient: String? = null,
    val indexer: String? = null,
    val outputPath: String? = null,
    val quality: QualityModel? = null,
    val languages: List<Language> = emptyList(),
    val customFormats: List<CustomFormatRef> = emptyList(),
    val customFormatScore: Int = 0
) {
    val progressPct: Double
        get() = if (size > 0.0) ((size - sizeleft) / size) * 100.0 else 0.0
}

data class StatusMessage(
    val title: String = "",
    val messages: List<String> = emptyList()
)

data class CustomFormatRef(
    val id: Int = 0,
    val name: String = ""
)
