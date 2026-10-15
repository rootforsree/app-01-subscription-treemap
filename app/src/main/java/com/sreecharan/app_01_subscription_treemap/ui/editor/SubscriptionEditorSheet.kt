package com.sreecharan.app_01_subscription_treemap.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import com.sreecharan.app_01_subscription_treemap.R
import com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle
import com.sreecharan.app_01_subscription_treemap.data.model.Subscription
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionInput
import com.sreecharan.app_01_subscription_treemap.ui.format.formatEpochDay
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppDimens
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppShapes
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTypography
import com.sreecharan.app_01_subscription_treemap.ui.theme.CategoryColors
import com.sreecharan.app_01_subscription_treemap.ui.theme.LocalAppColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Add/edit sheet. Stateless: every field is hoisted as local UI state and the
 * result leaves through [onSave]/[onDelete]. Validation runs on Save; errors
 * render under the offending field and nothing is saved until they clear.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionEditorSheet(
    initial: Subscription?,
    categories: List<SubscriptionCategory>,
    onSave: (SubscriptionInput) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = AppShapes.sheet,
        containerColor = LocalAppColors.current.cardSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        SubscriptionEditorContent(
            initial = initial,
            categories = categories,
            onSave = onSave,
            onDelete = onDelete,
            onDismiss = onDismiss,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionEditorContent(
    initial: Subscription?,
    categories: List<SubscriptionCategory>,
    onSave: (SubscriptionInput) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val sheetKey = initial?.id
    var name by rememberSaveable(sheetKey) { mutableStateOf(initial?.name.orEmpty()) }
    var amountText by rememberSaveable(sheetKey) { mutableStateOf(initial?.amount?.toString().orEmpty()) }
    var notes by rememberSaveable(sheetKey) { mutableStateOf(initial?.notes.orEmpty()) }
    var cycle by remember(sheetKey) { mutableStateOf(initial?.cycle ?: BillingCycle.MONTHLY) }
    var category by remember(sheetKey) { mutableStateOf(initial?.category ?: SubscriptionCategory.STREAMING) }
    var nextBillingEpochDay by rememberSaveable(sheetKey) { mutableStateOf(initial?.nextBillingEpochDay) }
    var nameError by rememberSaveable(sheetKey) { mutableStateOf(false) }
    var amountError by rememberSaveable(sheetKey) { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable(sheetKey) { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppDimens.spacing16)
            .padding(bottom = AppDimens.spacing24),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing16),
    ) {
        Text(
            text = stringResource(
                if (initial == null) R.string.editor_title_add else R.string.editor_title_edit,
            ),
            style = AppTypography.title2,
            color = colors.label,
        )
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = false
            },
            label = { Text(stringResource(R.string.field_name)) },
            isError = nameError,
            supportingText = if (nameError) {
                { Text(stringResource(R.string.error_name_blank)) }
            } else {
                null
            },
            singleLine = true,
            shape = AppShapes.card,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("editor_name_field"),
        )
        OutlinedTextField(
            value = amountText,
            onValueChange = {
                amountText = it
                amountError = false
            },
            label = { Text(stringResource(R.string.field_amount)) },
            prefix = { Text(stringResource(R.string.currency_symbol)) },
            isError = amountError,
            supportingText = if (amountError) {
                { Text(stringResource(R.string.error_amount_invalid)) }
            } else {
                null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = AppShapes.card,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("editor_amount_field"),
        )
        FieldLabel(text = stringResource(R.string.field_cycle))
        CycleSelector(
            selected = cycle,
            onSelect = { cycle = it },
        )
        FieldLabel(text = stringResource(R.string.field_category))
        CategorySelector(
            categories = categories,
            selected = category,
            onSelect = { category = it },
        )
        NextBillingRow(
            epochDay = nextBillingEpochDay,
            onClick = { showDatePicker = true },
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.field_notes)) },
            shape = AppShapes.card,
            minLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("editor_notes_field"),
        )
        Button(
            onClick = {
                val parsed = amountText.toDoubleOrNull()
                nameError = name.isBlank()
                amountError = parsed == null || parsed <= 0.0
                if (!nameError && !amountError && parsed != null) {
                    onSave(
                        SubscriptionInput(
                            id = initial?.id,
                            name = name.trim(),
                            amount = parsed,
                            cycle = cycle,
                            category = category,
                            nextBillingEpochDay = nextBillingEpochDay,
                            notes = notes.trim(),
                        ),
                    )
                }
            },
            shape = AppShapes.capsule,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AppDimens.rowMinHeight),
        ) {
            Text(
                text = stringResource(R.string.action_save),
                style = AppTypography.headline,
            )
        }
        if (initial != null) {
            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppDimens.rowMinHeight)
                    .testTag("editor_delete_button"),
            ) {
                Text(
                    text = stringResource(R.string.action_delete),
                    style = AppTypography.headline,
                    color = colors.destructive,
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = nextBillingEpochDay?.let { day ->
                LocalDate.ofEpochDay(day).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            nextBillingEpochDay = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toEpochDay()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.action_done))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val deleting = initial
    if (showDeleteConfirm && deleting != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_title, deleting.name)) },
            text = { Text(stringResource(R.string.delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(deleting.id)
                    },
                    modifier = Modifier.testTag("editor_delete_confirm_button"),
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = colors.destructive,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun FieldLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = AppTypography.footnote,
        color = LocalAppColors.current.secondaryLabel,
        modifier = modifier,
    )
}

@Composable
private fun CycleSelector(
    selected: BillingCycle,
    onSelect: (BillingCycle) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
    ) {
        for (entry in BillingCycle.entries) {
            val isSelected = entry == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(AppDimens.chipHeight)
                    .clip(AppShapes.capsule)
                    .background(if (isSelected) colors.accent else colors.groupedBackground)
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(entry) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.displayName,
                    style = AppTypography.subheadline,
                    color = if (isSelected) Color.White else colors.label,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun CategorySelector(
    categories: List<SubscriptionCategory>,
    selected: SubscriptionCategory,
    onSelect: (SubscriptionCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing4),
    ) {
        for (category in categories) {
            val isSelected = category == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppDimens.rowMinHeight)
                    .clip(AppShapes.card)
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(category) },
                    )
                    .padding(
                        horizontal = AppDimens.spacing12,
                        vertical = AppDimens.spacing8,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
            ) {
                Box(
                    modifier = Modifier
                        .size(AppDimens.spacing12)
                        .clip(CircleShape)
                        .background(CategoryColors.colorFor(category)),
                )
                Text(
                    text = category.displayName,
                    style = AppTypography.body,
                    color = colors.label,
                    modifier = Modifier.weight(1f),
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(AppDimens.iconSize),
                    )
                }
            }
        }
    }
}

@Composable
private fun NextBillingRow(
    epochDay: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
    ) {
        FieldLabel(text = stringResource(R.string.field_next_billing))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AppDimens.rowMinHeight)
                .clip(AppShapes.card)
                .background(colors.groupedBackground)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(
                    horizontal = AppDimens.spacing16,
                    vertical = AppDimens.spacing8,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = colors.secondaryLabel,
                modifier = Modifier.size(AppDimens.iconSize),
            )
            Text(
                text = epochDay?.let { formatEpochDay(it) } ?: stringResource(R.string.pick_date),
                style = AppTypography.body,
                color = if (epochDay == null) colors.secondaryLabel else colors.label,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
