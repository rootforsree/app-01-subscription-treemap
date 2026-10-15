package com.sreecharan.app_01_subscription_treemap.ui.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.sreecharan.app_01_subscription_treemap.BuildConfig
import com.sreecharan.app_01_subscription_treemap.R
import com.sreecharan.app_01_subscription_treemap.ui.components.GroupCard
import com.sreecharan.app_01_subscription_treemap.ui.components.InsetGroupedList
import com.sreecharan.app_01_subscription_treemap.ui.components.RowDivider
import com.sreecharan.app_01_subscription_treemap.ui.components.SettingsRow
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppDimens
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTypography
import com.sreecharan.app_01_subscription_treemap.ui.theme.LocalAppColors
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppColors

@Composable
fun AboutRoute() {
    val context = LocalContext.current
    AboutScreenContent(
        appVersion = BuildConfig.VERSION_NAME,
        onOpenGitHub = { openUrl(context, context.getString(R.string.about_github_url)) },
        onShareApp = { shareApp(context) },
        onRateApp = { openUrl(context, context.getString(R.string.about_rate_url)) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreenContent(
    appVersion: String,
    onOpenGitHub: () -> Unit,
    onShareApp: () -> Unit,
    onRateApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { AboutTopBar(scrollBehavior = scrollBehavior, colors = colors) },
        containerColor = colors.groupedBackground,
    ) { padding ->
        InsetGroupedList(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + AppDimens.tabBarClearance,
            ),
        ) {
            aboutIdentitySection(appVersion = appVersion, colors = colors)
            aboutLinksSection(
                onOpenGitHub = onOpenGitHub,
                onShareApp = onShareApp,
                onRateApp = onRateApp,
                colors = colors,
            )
        }
    }
}

private fun LazyListScope.aboutIdentitySection(
    appVersion: String,
    colors: AppColors,
) {
    item(key = "identity", contentType = "identity") {
        GroupCard {
            Column(
                modifier = Modifier.padding(AppDimens.spacing16),
                verticalArrangement = Arrangement.spacedBy(AppDimens.spacing4),
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = AppTypography.headline,
                    color = colors.label,
                )
                Text(
                    text = stringResource(
                        R.string.about_version,
                        appVersion,
                    ),
                    style = AppTypography.caption,
                    color = colors.secondaryLabel,
                )
            }
        }
    }
}

private fun LazyListScope.aboutLinksSection(
    onOpenGitHub: () -> Unit,
    onShareApp: () -> Unit,
    onRateApp: () -> Unit,
    colors: AppColors,
) {
    item(key = "links", contentType = "links") {
        GroupCard {
            linkRow(
                titleRes = R.string.about_github_title,
                subtitleRes = R.string.about_github_sub,
                icon = Icons.Filled.Code,
                onClick = onOpenGitHub,
            )
            RowDivider()
            linkRow(
                titleRes = R.string.about_share_title,
                subtitleRes = R.string.about_share_sub,
                icon = Icons.Filled.Share,
                onClick = onShareApp,
            )
            RowDivider()
            linkRow(
                titleRes = R.string.about_rate_title,
                subtitleRes = R.string.about_rate_sub,
                icon = Icons.Filled.Star,
                onClick = onRateApp,
            )
        }
    }
}

@Composable
private fun linkRow(
    titleRes: Int,
    subtitleRes: Int,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    SettingsRow(
        title = stringResource(titleRes),
        subtitle = stringResource(subtitleRes),
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.secondaryLabel,
                modifier = Modifier.size(AppDimens.iconSize),
            )
        },
        trailing = {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = colors.secondaryLabel,
                modifier = Modifier.size(AppDimens.iconSize),
            )
        },
        onClick = onClick,
    )
}

private fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun shareApp(context: Context) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text))
    }
    val chooser = Intent.createChooser(send, context.getString(R.string.share_chooser_title))
    context.startActivity(chooser)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    colors: AppColors,
) {
    LargeTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.about_title),
                style = AppTypography.largeTitle,
            )
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = colors.groupedBackground,
            scrolledContainerColor = colors.groupedBackground,
        ),
    )
}
