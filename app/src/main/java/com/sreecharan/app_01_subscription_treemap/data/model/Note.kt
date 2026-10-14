package com.sreecharan.app_01_subscription_treemap.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Note(
    val id: Long,
    val title: String,
    val body: String,
    val createdAt: Long,
)
