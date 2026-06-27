package com.kevinnesbitt.simple_ist.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import com.kevinnesbitt.simple_ist.HomeViewModel

class TextVisualTransformation(private val transformationRanges: List<HomeViewModel.TransformationRanges>) : VisualTransformation {
    override fun filter(text: AnnotatedString) : TransformedText {
        val builder = AnnotatedString.Builder(text.text)

        transformationRanges.forEach { range ->
            if (range.start <= text.length && range.end <= text.length) {
                builder.addStyle(
                    style =  SpanStyle(
                        fontWeight = if (range.type == "bold") FontWeight.Bold else null,
                        fontStyle = if (range.type == "italic") FontStyle.Italic else null,
                        textDecoration = if (range.type == "underline") TextDecoration.Underline else null
                        ),
                    start = range.start,
                    end = range.end
                )
            }
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}