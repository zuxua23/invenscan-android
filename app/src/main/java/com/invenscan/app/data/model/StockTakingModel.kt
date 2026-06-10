package com.invenscan.app.data.model

import com.google.gson.annotations.SerializedName

data class StockTakingModel(
    @SerializedName("id") val id: Long,
    @SerializedName("sessionCode") val sessionCode: String,
    @SerializedName("remark") val remark: String?,
    @SerializedName("status") val status: String,
    @SerializedName("createdBy") val createdBy: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("closedAt") val closedAt: String?
)

data class StockTakingTagModel(
    @SerializedName("id") val id: Long,
    @SerializedName("sttId") val sttId: Long,
    @SerializedName("tagId") val tagId: String,
    @SerializedName("epcTag") val epcTag: String?,
    @SerializedName("itemId") val itemId: Long,
    @SerializedName("itemCode") val itemCode: String,
    @SerializedName("itemName") val itemName: String,
    @SerializedName("action") val action: String,
    @SerializedName("scannedAt") val scannedAt: String?,
    @SerializedName("createdBy") val createdBy: String?
)

data class StockTakingSubmitRequest(
    @SerializedName("sttId") val sttId: Long,
    @SerializedName("scannedTags") val scannedTags: List<StockTakingScanItem>
)

data class StockTakingScanItem(
    @SerializedName("tagId") val tagId: String,
    @SerializedName("itemId") val itemId: Long,
    @SerializedName("action") val action: String,
    @SerializedName("scannedAt") val scannedAt: Long
)

object StockTakingStatus {
    const val OPEN = "OPEN"
    const val CLOSED = "CLOSED"
}

object StockTakingAction {
    const val SYSTEM = "SYSTEM"
    const val SCAN = "SCAN"
    const val MISSING = "MISSING"
}
