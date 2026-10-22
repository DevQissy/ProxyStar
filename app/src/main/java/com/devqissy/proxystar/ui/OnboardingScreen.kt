package com.devqissy.proxystar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devqissy.proxystar.R
import com.devqissy.proxystar.data.Prefs
import com.devqissy.proxystar.data.GeoGate
import com.devqissy.proxystar.ui.theme.*
import kotlinx.coroutines.launch


@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var acceptedTerms by remember { mutableStateOf(false) }
    var isStarting by remember { mutableStateOf(false) }

    var isRestrictedRegion by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isRestrictedRegion = GeoGate.isRestrictedRegion(context)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(
                listOf(Color(0xFF030510), Color(0xFF0A1024), Color(0xFF030510))
            ))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 24.dp)
        ) {
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = Brush.linearGradient(listOf(Color.White, Cyan, Purple))
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.img_law),
                    contentDescription = null,
                    modifier = Modifier.size(180.dp)
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OnboardCard {
                    Text(
                        stringResource(R.string.terms_title),
                        style = MaterialTheme.typography.titleMedium, color = Color.White
                    )
                    Spacer(Modifier.height(10.dp))
                    listOf(
                        R.string.terms_item_client,
                        R.string.terms_item_service,
                        R.string.terms_item_region,
                        R.string.terms_item_responsibility
                    ).forEach { res ->
                        Row {
                            Text("•", color = Cyan, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(res),
                                style = MaterialTheme.typography.bodySmall, color = TextDim)
                        }
                    }
                }

                OnboardCheck(
                    checked = acceptedTerms,
                    onChecked = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        acceptedTerms = it
                    },
                    border = if (isRestrictedRegion) Amber.copy(alpha = 0.4f)
                    else Color.White.copy(alpha = 0.08f),
                    text = if (isRestrictedRegion)
                        stringResource(R.string.geo_confirm_restricted)
                    else
                        stringResource(R.string.geo_confirm_general)
                )

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isStarting = true
                        scope.launch {
                            Prefs.setOnboarded(context, true)
                            onFinished()
                        }
                    },
                    enabled = acceptedTerms && !isStarting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan, contentColor = Color(0xFF04283A)
                    ),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        stringResource(R.string.onboard_next),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(R.string.privacy_note),
                style = MaterialTheme.typography.labelSmall,
                color = TextFaint, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
private fun OnboardCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Card0.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), content = content)
    }
}

@Composable
private fun OnboardCheck(
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
    border: Color,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Card0.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onChecked,
                colors = CheckboxDefaults.colors(
                    checkedColor = Cyan, checkmarkColor = Color(0xFF04283A)
                )
            )
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = Color.White)
        }
    }
}