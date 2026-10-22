package com.devqissy.proxystar.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devqissy.proxystar.R
import com.devqissy.proxystar.data.Prefs
import com.devqissy.proxystar.data.Proxy
import com.devqissy.proxystar.ui.components.AnimationClock
import com.devqissy.proxystar.ui.components.HeroCard
import com.devqissy.proxystar.ui.components.ProxyRow
import com.devqissy.proxystar.ui.components.DotsLabel
import com.devqissy.proxystar.ui.components.SquareAction
import com.devqissy.proxystar.ui.theme.Bg0
import com.devqissy.proxystar.ui.theme.Blue
import com.devqissy.proxystar.ui.theme.Cyan
import com.devqissy.proxystar.ui.theme.Purple
import com.devqissy.proxystar.ui.theme.TextDim
import com.devqissy.proxystar.ui.theme.TextFaint
import kotlinx.coroutines.launch

@Composable
fun ProxyScreen(viewModel: ProxyViewModel = viewModel()) {
    val context = LocalContext.current
    var onboarded by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        onboarded = Prefs.isOnboarded(context)
    }

    Crossfade(targetState = onboarded, animationSpec = tween(320), label = "root") { done ->
        when (done) {
            null -> Box(Modifier.fillMaxSize().background(Bg0))
            false -> OnboardingScreen(onFinished = { onboarded = true })
            true -> MainProxyScreen(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainProxyScreen(viewModel: ProxyViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current

    SideEffect {
        (view.context as? Activity)?.window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onForeground()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var shape by remember { mutableStateOf<ConstellationShape?>(null) }

    val onShape = remember { { s: ConstellationShape -> shape = s } }

    var skipTelegram by remember { mutableStateOf<Boolean?>(null) }
    var skipGithub by remember { mutableStateOf<Boolean?>(null) }
    var pendingProxy by remember { mutableStateOf<Proxy?>(null) }
    var askGithub by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        skipTelegram = Prefs.skipTelegramConfirm(context)
        skipGithub = Prefs.skipGithubConfirm(context)
    }

    val goTelegram: (Proxy) -> Unit = { p ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        openInTelegram(context, p)
    }

    val connect: (Proxy) -> Unit = { p ->
        if (skipTelegram == true) goTelegram(p) else pendingProxy = p
    }

    val openRepo: () -> Unit = {
        if (skipGithub == true) openRepository(context) else askGithub = true
    }

    val coldStart = state.isColdStart()

    AnimationClock {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Box(Modifier.fillMaxSize()) {
                SpaceBackdrop(Modifier.fillMaxSize(), onShape)

                Column(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    Header(onGitHub = openRepo)

                    ProgressLine(state)

                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 10.dp,
                                bottom = 28.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item(key = "hero") {
                                HeroCard(
                                    state = state,
                                    shape = shape,
                                    haptic = haptic,
                                    onSwitchNext = viewModel::switchNext,
                                    onRetry = viewModel::refresh,
                                    onConnect = connect
                                )
                            }

                            items(state.proxies, key = { it.id }) { proxy ->
                                ProxyRow(
                                    proxy = proxy,
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = tween(280),
                                        placementSpec = spring(
                                            dampingRatio = 0.9f,
                                            stiffness = Spring.StiffnessMediumLow
                                        ),
                                        fadeOutSpec = tween(180)
                                    ),
                                    onShare = { shareLink(context, proxy) },
                                    onConnect = { connect(proxy) }
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = coldStart,
                        enter = fadeIn(tween(280)) + expandVertically(tween(280)),
                        exit = fadeOut(tween(180)) + shrinkVertically(tween(220))
                    ) {
                        BottomHint(state)
                    }
                }
            }
        }
    }

    ConfirmSwitchDialog(
        visible = pendingProxy != null,
        title = stringResource(R.string.confirm_telegram_title),
        message = stringResource(R.string.confirm_telegram_msg),
        confirmLabel = stringResource(R.string.proceed),
        dismissLabel = stringResource(R.string.cancel),
        skipLabel = stringResource(R.string.dont_show_again),
        onConfirm = {
            val target = pendingProxy
            pendingProxy = null
            if (target != null) goTelegram(target)
        },
        onDismiss = { pendingProxy = null },
        onSkip = {
            skipTelegram = true
            scope.launch { Prefs.setSkipTelegramConfirm(context, true) }
        }
    )

    ConfirmSwitchDialog(
        visible = askGithub,
        title = stringResource(R.string.confirm_github_title),
        message = stringResource(R.string.confirm_github_msg),
        confirmLabel = stringResource(R.string.proceed),
        dismissLabel = stringResource(R.string.cancel),
        skipLabel = stringResource(R.string.dont_show_again),
        onConfirm = {
            askGithub = false
            openRepository(context)
        },
        onDismiss = { askGithub = false },
        onSkip = {
            skipGithub = true
            scope.launch { Prefs.setSkipGithubConfirm(context, true) }
        }
    )
}

private fun ProxyUiState.isColdStart(): Boolean =
    proxies.isEmpty() && (isLoading || isChecking)

@Composable
private fun BottomHint(state: ProxyUiState) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.totalCount > 0) {

            Text(
                stringResource(
                    R.string.discovery_checking,
                    state.checkedCount,
                    state.totalCount
                ),
                style = MaterialTheme.typography.labelSmall,
                color = TextDim
            )
        } else {
            DotsLabel(
                text = stringResource(R.string.hero_loading),
                color = TextDim,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
@Composable
private fun Header(onGitHub: () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge.copy(
                        brush = Brush.linearGradient(listOf(Color.White, Cyan, Purple))
                    ),
                    textAlign = TextAlign.Left
                )
                Text(
                    stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextFaint,
                    textAlign = TextAlign.Left
                )
            }

            SquareAction(
                R.drawable.ic_github,
                stringResource(R.string.github),
                Color.White.copy(alpha = 0.88f),
                onGitHub
            )
        }
    }
}

@Composable
private fun ProgressLine(state: ProxyUiState) {
    val busy = state.isLoading || state.isChecking || state.isRefreshing

    Box(
        Modifier
            .fillMaxWidth()
            .height(3.dp)
    ) {

        AnimatedVisibility(
            visible = busy,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(340))
        ) {

            Crossfade(
                targetState = state.aliveCount > 0,
                animationSpec = tween(260),
                label = "progressMode"
            ) { determinate ->
                if (determinate) {
                    val frac = if (state.targetAlive <= 0) 0f
                    else (state.aliveCount.toFloat() / state.targetAlive).coerceIn(0f, 1f)
                    val anim by animateFloatAsState(frac, tween(500), label = "topbar")
                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = anim.coerceIn(0.02f, 1f)
                                transformOrigin = TransformOrigin(0f, 0.5f)
                            }
                            .background(Brush.horizontalGradient(listOf(Cyan, Blue, Purple)))
                    )
                } else {
                    val tr = rememberInfiniteTransition(label = "sweep")
                    val x by tr.animateFloat(
                        0f, 1f,
                        infiniteRepeatable(
                            tween(1_900, easing = LinearEasing),
                            RepeatMode.Restart
                        ),
                        label = "sweepX"
                    )
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        val segment = maxWidth * 0.32f
                        Box(
                            Modifier
                                .width(segment)
                                .fillMaxHeight()
                                .graphicsLayer {
                                    val full = size.width / 0.32f
                                    translationX = (full + size.width) * x - size.width
                                }
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, Cyan, Blue, Color.Transparent)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
