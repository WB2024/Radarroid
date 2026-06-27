package com.radarrtv.androidtv.data.api.model

data class MovieFile(
    val id: Int = 0,
    val movieId: Int = 0,
    val relativePath: String = "",
    val path: String = "",
    val size: Long = 0,
    val dateAdded: String = "",
    val sceneName: String? = null,
    val releaseGroup: String? = null,
    val quality: QualityModel? = null,
    val languages: List<Language> = emptyList(),
    val edition: String? = null,
    val mediaInfo: MediaInfo? = null,
    val originalFilePath: String? = null
)

data class QualityModel(
    val quality: Quality? = null,
    val revision: Revision? = null
)

data class Quality(
    val id: Int = 0,
    val name: String = "",
    val source: String = "",
    val resolution: Int = 0,
    val modifier: String = ""
)

data class Revision(
    val version: Int = 1,
    val real: Int = 0,
    val isRepack: Boolean = false
)

data class Language(
    val id: Int = 0,
    val name: String = ""
)

data class MediaInfo(
    val audioBitrate: Long? = null,
    val audioChannels: Double? = null,
    val audioCodec: String? = null,
    val audioLanguages: String? = null,
    val audioStreamCount: Int? = null,
    val videoBitDepth: Int? = null,
    val videoBitrate: Long? = null,
    val videoCodec: String? = null,
    val videoFps: Double? = null,
    val videoDynamicRange: String? = null,
    val videoDynamicRangeType: String? = null,
    val resolution: String? = null,
    val runTime: String? = null,
    val scanType: String? = null,
    val subtitles: String? = null
)
