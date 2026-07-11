package com.kevinnesbitt.simple_ist.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.kevinnesbitt.simple_ist.HomeViewModel

class TextVisualTransformation(private val transformationRanges: List<HomeViewModel.TransformationRanges>) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text.text)

        transformationRanges.forEach { range ->
            if (range.start < text.length && range.end <= text.length && range.start < range.end) {

                // Apply font size ONLY from header ranges
                if (range.type == "bigHeader" || range.type == "biggerHeader") {
                    builder.addStyle(
                        style = SpanStyle(
                            fontSize = if (range.type == "biggerHeader") 32.sp else 25.sp
                        ),
                        start = range.start,
                        end = range.end
                    )
                }

                // Apply decorations separately, never touching fontSize
                val fontWeight = if (range.type == "bold") FontWeight.Bold else null
                val fontStyle = if (range.type == "italic") FontStyle.Italic else null
                val textDecoration = if (range.type == "underline") TextDecoration.Underline else null

                if (fontWeight != null || fontStyle != null || textDecoration != null) {
                    builder.addStyle(
                        style = SpanStyle(
                            fontWeight = fontWeight,
                            fontStyle = fontStyle,
                            textDecoration = textDecoration
                        ),
                        start = range.start,
                        end = range.end
                    )
                }
            }
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}