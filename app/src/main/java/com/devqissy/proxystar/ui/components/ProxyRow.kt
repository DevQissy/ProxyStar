package com.devqissy.proxystar.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devqissy.proxystar.R
import com.devqissy.proxystar.data.Proxy
import com.devqissy.proxystar.ui.theme.TextDim
import com.devqissy.proxystar.ui.theme.TextFaint

@Composable
internal fun ProxyRow(
    proxy: Proxy,
    modifier: Modifier = Modifier,
    onShare: () -> Unit,
    onConnect: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClick(onConnect),
        shape = RoundedCornerShape(RowRadius),
        color = GlassCard,
        border = BorderStroke(1.dp, GlassBorderSoft)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SignalBars(proxy)
            Spacer(Modifier.width(12.dp))

            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    "${proxy.server}:${proxy.port}",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LatencyPill(proxy)
                    Text(
                        sourceLabel(proxy.source),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextFaint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(2.dp))
            BareIconAction(
                R.drawable.ic_share,
                stringResource(R.string.action_share),
                TextDim,
                onShare
            )
        }
    }
}
