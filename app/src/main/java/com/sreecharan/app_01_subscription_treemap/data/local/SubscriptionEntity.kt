package com.sreecharan.app_01_subscription_treemap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val currency: String,
    /** [com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle] enum name. */
    val cycle: String,
    /** [com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory] enum name. */
    val category: String,
    /** [java.time.LocalDate.getEpochDay]; null when the next billing date is unknown. */
    val nextBillingEpochDay: Long?,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long,
)
