package com.radarrtv.androidtv.data.api.model

data class Provider(
    val id: Int = 0,
    val name: String = "",
    val implementationName: String = "",
    val implementation: String = "",
    val configContract: String = "",
    val infoLink: String? = null,
    val message: ProviderMessage? = null,
    val tags: List<Int> = emptyList(),
    val fields: List<ProviderField> = emptyList(),
    val presets: List<Provider>? = null
)

data class ProviderMessage(
    val message: String = "",
    val type: String = ""
)

data class ProviderField(
    val order: Int = 0,
    val name: String = "",
    val label: String = "",
    val unit: String? = null,
    val helpText: String? = null,
    val helpTextWarning: String? = null,
    val helpLink: String? = null,
    val value: Any? = null,
    val type: String = "string",
    val advanced: Boolean = false,
    val selectOptions: List<SelectOption>? = null,
    val selectOptionsProviderAction: String? = null,
    val section: String? = null,
    val hidden: String? = null,
    val privacy: String = "normal",
    val placeholder: String? = null,
    val isFloat: Boolean = false
)

data class SelectOption(
    val value: Int = 0,
    val name: String = "",
    val order: Int = 0,
    val hint: String? = null
)

data class Indexer(
    val id: Int = 0,
    val name: String = "",
    val implementationName: String = "",
    val implementation: String = "",
    val configContract: String = "",
    val infoLink: String? = null,
    val message: ProviderMessage? = null,
    val tags: List<Int> = emptyList(),
    val fields: List<ProviderField> = emptyList(),
    val priority: Int = 25,
    val downloadClientId: Int = 0,
    val enableRss: Boolean = true,
    val enableAutomaticSearch: Boolean = true,
    val enableInteractiveSearch: Boolean = true,
    val supportsRss: Boolean = true,
    val supportsSearch: Boolean = true,
    val protocol: String = "usenet",
    val capabilities: IndexerCapabilities? = null
)

data class IndexerCapabilities(
    val supportsRss: Boolean = true,
    val supportsSearch: Boolean = true,
    val categories: List<IndexerCategory> = emptyList()
)

data class IndexerCategory(
    val id: Int = 0,
    val name: String = "",
    val subCategories: List<IndexerCategory> = emptyList()
)

data class DownloadClient(
    val id: Int = 0,
    val enable: Boolean = true,
    val protocol: String = "usenet",
    val priority: Int = 1,
    val removeCompletedDownloads: Boolean = true,
    val removeFailedDownloads: Boolean = true,
    val name: String = "",
    val fields: List<ProviderField> = emptyList(),
    val implementationName: String = "",
    val implementation: String = "",
    val configContract: String = "",
    val infoLink: String? = null,
    val message: ProviderMessage? = null,
    val tags: List<Int> = emptyList()
)

data class Notification(
    val id: Int = 0,
    val onGrab: Boolean = false,
    val onDownload: Boolean = false,
    val onUpgrade: Boolean = false,
    val onRename: Boolean = false,
    val onMovieAdded: Boolean = false,
    val onMovieDelete: Boolean = false,
    val onMovieFileDelete: Boolean = false,
    val onMovieFileDeleteForUpgrade: Boolean = false,
    val onHealthIssue: Boolean = false,
    val includeHealthWarnings: Boolean = false,
    val onApplicationUpdate: Boolean = false,
    val supportsOnGrab: Boolean = true,
    val supportsOnDownload: Boolean = true,
    val supportsOnUpgrade: Boolean = true,
    val supportsOnRename: Boolean = true,
    val supportsOnMovieAdded: Boolean = false,
    val supportsOnMovieDelete: Boolean = false,
    val supportsOnMovieFileDelete: Boolean = false,
    val supportsOnMovieFileDeleteForUpgrade: Boolean = false,
    val supportsOnHealthIssue: Boolean = true,
    val supportsOnApplicationUpdate: Boolean = true,
    val name: String = "",
    val fields: List<ProviderField> = emptyList(),
    val implementationName: String = "",
    val implementation: String = "",
    val configContract: String = "",
    val infoLink: String? = null,
    val message: ProviderMessage? = null,
    val tags: List<Int> = emptyList()
)

data class ImportList(
    val id: Int = 0,
    val enabled: Boolean = true,
    val enableAuto: Boolean = false,
    val shouldMonitor: Boolean = true,
    val rootFolderPath: String = "",
    val qualityProfileId: Int = 1,
    val searchOnAdd: Boolean = false,
    val listType: String = "other",
    val listOrder: Int = 0,
    val name: String = "",
    val fields: List<ProviderField> = emptyList(),
    val implementationName: String = "",
    val implementation: String = "",
    val configContract: String = "",
    val infoLink: String? = null,
    val message: ProviderMessage? = null,
    val tags: List<Int> = emptyList(),
    val minimumAvailability: String = "released"
)

data class CustomFormat(
    val id: Int = 0,
    val name: String = "",
    val includeCustomFormatWhenRenaming: Boolean = false,
    val specifications: List<CustomFormatSpec> = emptyList()
)

data class CustomFormatSpec(
    val id: Int = 0,
    val name: String = "",
    val implementation: String = "",
    val implementationName: String = "",
    val infoLink: String? = null,
    val negate: Boolean = false,
    val required: Boolean = false,
    val fields: List<ProviderField> = emptyList()
)

data class MetadataProvider(
    val id: Int = 0,
    val enable: Boolean = false,
    val name: String = "",
    val fields: List<ProviderField> = emptyList(),
    val implementationName: String = "",
    val implementation: String = "",
    val configContract: String = "",
    val infoLink: String? = null,
    val message: ProviderMessage? = null,
    val tags: List<Int> = emptyList()
)
