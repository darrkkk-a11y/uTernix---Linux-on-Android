// uTernix onboarding — four concise, responsive screens with the real setup flow.
// SPDX-License-Identifier: GPL-3.0-or-later
package com.daryan.tessera.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.daryan.tessera.R
import com.daryan.tessera.ui.deb.DebHeroIcon
import com.daryan.tessera.ui.deb.DebIcon
import com.daryan.tessera.ui.deb.UColors
import com.daryan.tessera.ui.deb.UtBackdrop
import kotlinx.coroutines.launch

private data class IntroPage(val icon: DebIcon, val title: Int, val description: Int)
private val pages = listOf(
    IntroPage(DebIcon.BrandLauncher, R.string.ut2_intro_one_title, R.string.ut2_intro_one_body),
    IntroPage(DebIcon.NavTerminal, R.string.ut2_intro_two_title, R.string.ut2_intro_two_body),
    IntroPage(DebIcon.PkgShop, R.string.ut2_intro_three_title, R.string.ut2_intro_three_body),
    IntroPage(DebIcon.NavFiles, R.string.ut2_intro_four_title, R.string.ut2_intro_four_body),
)

@Composable
fun IntroScreen(onDone: () -> Unit) {
    val pager = rememberPagerState(pageCount = {pages.size})
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == pages.lastIndex
    UtBackdrop(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().widthIn(max = 800.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("uTernix", style = MaterialTheme.typography.titleLarge, color = UColors.Text,
                    modifier = Modifier.weight(1f))
                if (!last) TextButton(onClick = onDone) { Text(stringResource(R.string.ut2_intro_skip)) }
            }
            HorizontalPager(pager, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(Modifier.widthIn(max = 620.dp).fillMaxWidth()
                        .verticalScroll(rememberScrollState()).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        DebHeroIcon(pages[page].icon, size = 88.dp)
                        Text(stringResource(pages[page].title), color = UColors.Text,
                            style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                        Text(stringResource(pages[page].description), color = UColors.Muted,
                            style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    }
                }
            }
            Row(Modifier.widthIn(max = 620.dp).fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${pager.currentPage + 1} / ${pages.size}",
                    style = MaterialTheme.typography.labelLarge, color = UColors.Muted,
                    modifier = Modifier.weight(1f))
                Button(onClick = {
                    if(last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(if(last) R.string.ut2_intro_start else R.string.ut2_intro_next))
                }
            }
        }
    }
}
