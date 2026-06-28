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
    override fun filter(text: AnnotatedString) : TransformedText {
        val builder = AnnotatedString.Builder(text.text)

        transformationRanges.forEach { range ->
            // android.util.Log.d("Ranges", "id=${range.id} type=${range.type} start=${range.start} end=${range.end} textLength=${text.length}")
            if (range.start < text.length && range.end <= text.length && range.start < range.end) {
                builder.addStyle(
                    style =  SpanStyle(
                        fontWeight = if (range.type.contains("bold")) FontWeight.Bold else null,
                        fontStyle = if (range.type.contains("italic")) FontStyle.Italic else null,
                        textDecoration = if (range.type.contains("underline")) TextDecoration.Underline else null,
                        fontSize = when {
                            range.type.contains("bigHeader") -> { 25.sp }
                            range.type.contains("biggerHeader") -> { 32.sp }
                            else -> { 20.sp }
                        }
                        ),
                    start = range.start,
                    end = range.end
                )
            }
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}