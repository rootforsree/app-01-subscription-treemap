package com.sreecharan.app_01_subscription_treemap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "price_hikes")
data class PriceHikeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subscriptionId: Long,
    val subscriptionName: String,
    val oldAmount: Double,
    val newAmount: Double,
    /** [com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle] enum name. */
    val cycle: String,
    val detectedAt: Long,
    val acknowledged: Boolean,
)
