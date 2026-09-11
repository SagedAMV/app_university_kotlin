package com.unimanager.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * صف بنمط Bento: يوزّع البطاقات بالتساوي أفقيًا مع مسافات ثابتة.
 * (كان الملف يضم كذلك BentoGrid/SimpleBentoGrid بلغة DSL بلا أي مستدعٍ — حُذفت.)
 */
@Composable
fun BentoRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        content()
    }
}
