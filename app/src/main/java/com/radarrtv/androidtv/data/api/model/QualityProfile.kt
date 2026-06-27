package com.radarrtv.androidtv.data.api.model

data class QualityProfile(
    val id: Int = 0,
    val name: String = "",
    val upgradeAllowed: Boolean = true,
    val cutoff: Int = 0,
    val items: List<QualityProfileItem> = emptyList(),
    val minFormatScore: Int = 0,
    val cutoffFormatScore: Int = 0,
    val formatItems: List<FormatItem> = emptyList(),
    val language: Language? = null
)

data class QualityProfileItem(
    val id: Int = 0,
    val name: String? = null,
    val quality: Quality? = null,
    val items: List<QualityProfileItem> = emptyList(),
    val allowed: Boolean = true
)

data class FormatItem(
    val id: Int = 0,
    val format: Int = 0,
    val name: String = "",
    val score: Int = 0
)

data class RootFolder(
    val id: Int = 0,
    val path: String = "",
    val accessible: Boolean = true,
    val freeSpace: Long? = null,
    val totalSpace: Long? = null,
    val unmappedFolders: List<UnmappedFolder> = emptyList()
)

data class UnmappedFolder(
    val name: String = "",
    val path: String = "",
    val relativePath: String = ""
)

data class Tag(
    val id: Int = 0,
    val label: String = ""
)
