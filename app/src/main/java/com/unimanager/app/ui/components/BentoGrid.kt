package com.unimanager.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Bento Grid Layout - 2026 Design Trend
 * Creates asymmetric, modular layout with different sized tiles
 */
@Composable
fun BentoGrid(
    modifier: Modifier = Modifier,
    content: @Composable BentoGridScope.() -> Unit
) {
    val scope = BentoGridScopeImpl()
    scope.content()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        scope.rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.items.forEach { item ->
                    Box(modifier = item.modifier) {
                        item.content()
                    }
                }
            }
        }
    }
}

interface BentoGridScope {
    fun row(content: @Composable BentoRowScope.() -> Unit)
}

interface BentoRowScope {
    fun item(
        weight: Float = 1f,
        content: @Composable () -> Unit
    )
}

private class BentoGridScopeImpl : BentoGridScope {
    val rows = mutableListOf<BentoRowData>()

    override fun row(content: @Composable BentoRowScope.() -> Unit) {
        val rowScope = BentoRowScopeImpl()
        rowScope.content()
        rows.add(BentoRowData(rowScope.items))
    }
}

private class BentoRowScopeImpl : BentoRowScope {
    val items = mutableListOf<BentoItemData>()

    override fun item(weight: Float, content: @Composable () -> Unit) {
        items.add(BentoItemData(Modifier.weight(weight), content))
    }
}

private data class BentoRowData(val items: List<BentoItemData>)
private data class BentoItemData(val modifier: Modifier, val content: @Composable () -> Unit)

/**
 * Simple 2-column bento grid for mobile
 */
@Composable
fun SimpleBentoGrid(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        content()
    }
}

@Composable
fun BentoRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        content()
    }
}
