package io.github.kevincianfarini.grtc.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jakewharton.mosaic.LocalTerminalState
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.text.AnnotatedString
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Layout
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle
import com.jakewharton.mosaic.ui.UnderlineStyle
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
public fun MarqueeText(
    value: CharSequence,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    background: Color = Color.Unspecified,
    textStyle: TextStyle = TextStyle.Unspecified,
    underlineStyle: UnderlineStyle = UnderlineStyle.Unspecified,
    underlineColor: Color = Color.Unspecified,
    scrollComplete: () -> Unit = {},
) {
    var maxTextWidth by remember(LocalTerminalState.current.size.width) {
        mutableIntStateOf(0)
    }
    Layout(
        modifier = modifier,
        content = {
            var startIndex by remember(value, maxTextWidth) {
                mutableIntStateOf(0)
            }
            var endIndex by remember(value, maxTextWidth) {
                mutableIntStateOf(minOf(value.length, maxTextWidth))
            }
            LaunchedEffect(value, maxTextWidth, scrollComplete) {
                if (value.length > maxTextWidth) {
                    while (true) {
                        delay(3.seconds) // Initially delay and then start scrolling.
                        while (endIndex <= value.lastIndex) {
                            delay(100.milliseconds)
                            startIndex = (startIndex + 1).coerceAtMost(value.length)
                            endIndex = (endIndex + 1).coerceAtMost(value.length)
                        }
                        delay(3.seconds)
                        startIndex = 0
                        endIndex = minOf(value.length, maxTextWidth)
                        scrollComplete()
                    }
                }
            }
            when (value) {
                is AnnotatedString -> Text(
                    value = value.subSequence(startIndex, endIndex),
                    color = color,
                    background = background,
                    textStyle = textStyle,
                    underlineStyle = underlineStyle,
                    underlineColor = underlineColor,
                )
                is String -> Text(
                    value = value.substring(startIndex, endIndex),
                    color = color,
                    background = background,
                    textStyle = textStyle,
                    underlineStyle = underlineStyle,
                    underlineColor = underlineColor,
                )
                else -> throw IllegalArgumentException("Not implemented for ${value::class.simpleName}.")
            }
        },
        measurePolicy = { measurables, constraints ->
            maxTextWidth = constraints.maxWidth
            val placeable = measurables.single().measure(constraints)
            layout(minOf(value.length, constraints.maxWidth), 1) {
                placeable.place(0, 0)
            }
        }
    )
}