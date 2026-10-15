package com.sreecharan.app_01_subscription_treemap.ui.format

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val UsdFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
private val FullDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
private val ShortDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)

/*
 * Money and date formatting for the whole UI layer. USD everywhere in this
 * app; dates render in US short form. Called on the main thread only, so the
 * shared NumberFormat instance is safe.
 */

/** "$17.99" */
fun formatUsd(amount: Double): String = UsdFormat.format(amount)

/** "Oct 12, 2026" from a [LocalDate] epoch day. Timezone-independent. */
fun formatEpochDay(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(FullDateFormat)

/** "Oct 12" from epoch millis. */
fun formatEpochMillisShort(epochMillis: Long): String =
    Instant
        .ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(ShortDateFormat)

/** "16.1%" — one decimal, US locale. */
fun formatPercentOneDecimal(value: Double): String = "%.1f%%".format(Locale.US, value)
