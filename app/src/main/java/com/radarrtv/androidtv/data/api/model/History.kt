package com.radarrtv.androidtv.data.api.model

data class HistoryResponse(
    val page: Int = 1,
    val pageSize: Int = 20,
    val sortKey: String? = null,
    val sortDirection: String? = null,
    val totalRecords: Int = 0,
    val records: List<HistoryItem> = emptyList()
)

data class HistoryItem(
    val id: Int = 0,
    val movieId: Int = 0,
    val movie: Movie? = null,
    val sourceTitle: String = "",
    val quality: QualityModel? = null,
    val qualityCutoffNotMet: Boolean = false,
    val date: String = "",
    val downloadId: String? = null,
    val eventType: String = "",
    val data: Map<String, String?> = emptyMap(),
    val languages: List<Language> = emptyList(),
    val customFormats: List<CustomFormatRef> = emptyList(),
    val customFormatScore: Int = 0
) {
    val eventTypeDisplay: String
        get() = when (eventType) {
            "grabbed" -> "Grabbed"
            "seriesFolderImported" -> "Imported"
            "downloadFolderImported" -> "Imported"
            "downloadFailed" -> "Failed"
            "movieFileDeleted" -> "Deleted"
            "movieFileRenamed" -> "Renamed"
            "ignored" -> "Ignored"
            else -> eventType.replaceFirstChar { it.uppercase() }
        }
}
