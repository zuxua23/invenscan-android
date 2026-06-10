package com.invenscan.app.data.model

import com.google.gson.annotations.SerializedName

data class ItemModel(
    @SerializedName("id") val id: Long,
    @SerializedName("itemCode") val itemCode: String,
    @SerializedName("itemName") val itemName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("unit") val unit: String?,
    @SerializedName("minStock") val minStock: Int,
    @SerializedName("createdBy") val createdBy: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)
