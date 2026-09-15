package org.ukrida.labvora.data.model

data class UploadPhotoResponse(
    val success: Boolean = false,
    val filename: String? = null,
    val url: String? = null,
    val message: String? = null
)
