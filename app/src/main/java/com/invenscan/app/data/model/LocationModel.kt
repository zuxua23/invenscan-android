package com.invenscan.app.data.model

import com.google.gson.annotations.SerializedName

data class LocationModel(
    @SerializedName("id") val id: Long,
    @SerializedName("locationCode") val locationCode: String,
    @SerializedName("locationName") val locationName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("createdBy") val createdBy: String?,
    @SerializedName("createdAt") val createdAt: String?
)
