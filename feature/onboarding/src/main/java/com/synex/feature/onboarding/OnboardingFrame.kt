package com.synex.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.synex.core.ui.SynexGold
import com.synex.core.ui.SynexInk
import com.synex.core.ui.SynexPaper
import com.synex.core.ui.SynexTeal
import com.synex.core.ui.SynexWordmark

@Composable
internal fun OnboardingFrame(
    step: Int,
    buttonLabel: String,
    onButtonClick: () -> Unit,
    onBack: (() -> Unit)? = null,
    buttonEnabled: Boolean = true,
    isSaving: Boolean = false,
    content: LazyListScope.() -> Unit,
) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFF8F8F5), SynexPaper)),
        ).windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Go back")
                    }
                }
                SynexWordmark(Modifier.padding(start = if (onBack == null) 8.dp else 2.dp))
                ProgressDots(step, Modifier.padding(start = 14.dp).weight(1f))
                Text("$step of 3", style = MaterialTheme.typography.labelMedium)
            }
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
            Button(
                onClick = onButtonClick,
                enabled = buttonEnabled && !isSaving,
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(SynexInk, Color.White),
                contentPadding = PaddingValues(vertical = 17.dp),
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(22.dp), Color.White, strokeWidth = 2.dp)
                else Text(buttonLabel)
            }
        }
    }
}

@Composable
private fun ProgressDots(step: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(3) { index ->
            Box(
                Modifier.weight(1f).padding(vertical = 4.dp).background(
                    if (index < step) Brush.horizontalGradient(listOf(SynexTeal, SynexGold))
                    else Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.09f), Color.Black.copy(alpha = 0.09f))),
                    CircleShape,
                ).padding(vertical = 2.dp),
            )
        }
    }
}
