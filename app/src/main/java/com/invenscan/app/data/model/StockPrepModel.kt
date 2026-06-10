package com.invenscan.app.data.model

import com.google.gson.annotations.SerializedName

data class StockPrepModel(
    @SerializedName("id") val id: Long,
    @SerializedName("docNumber") val docNumber: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("status") val status: String,
    @SerializedName("createdBy") val createdBy: String?,
    @SerializedName("createdAt") val createdAt: String?
)

data class StockPrepDetailModel(
    @SerializedName("id") val id: Long,
    @SerializedName("docNumber") val docNumber: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("status") val status: String,
    @SerializedName("createdBy") val createdBy: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("details") val details: List<StockPrepDetailItem>
)

data class StockPrepDetailItem(
    @SerializedName("id") val id: Long,
    @SerializedName("stockPrepId") val stockPrepId: Long,
    @SerializedName("itemId") val itemId: Long,
    @SerializedName("itemCode") val itemCode: String,
    @SerializedName("itemName") val itemName: String,
    @SerializedName("locationId") val locationId: Long,
    @SerializedName("locationName") val locationName: String?,
    @SerializedName("requestedQty") val requestedQty: Int,
    @SerializedName("pickedQty") val pickedQty: Int,
    @SerializedName("status") val status: String,
    @SerializedName("scannedCode") val scannedCode: String?
)

data class StockPrepSubmitRequest(
    @SerializedName("stockPrepId") val stockPrepId: Long,
    @SerializedName("items") val items: List<StockPrepPickedItem>
)

data class StockPrepPickedItem(
    @SerializedName("detailId") val detailId: Long,
    @SerializedName("scannedCode") val scannedCode: String,
    @SerializedName("scanType") val scanType: String,
    @SerializedName("pickedQty") val pickedQty: Int
)

data class SearchItemModel(
    @SerializedName("id") val id: Long,
    @SerializedName("itemCode") val itemCode: String,
    @SerializedName("itemName") val itemName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("unit") val unit: String?,
    @SerializedName("minStock") val minStock: Int,
    @SerializedName("locationName") val locationName: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("epcTag") val epcTag: String?
)

object StockPrepStatus {
    const val OPEN = "OPEN"
    const val IN_PROGRESS = "IN_PROGRESS"
    const val DONE = "DONE"
}

object StockPrepDetailStatus {
    const val PENDING = "PENDING"
    const val PICKED = "PICKED"
}
