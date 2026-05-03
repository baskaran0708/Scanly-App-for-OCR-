package com.app.ocrscanner.util

/**
 * Builds a minimal RTF document from plain text and a title.
 * Used by both OcrViewModel and DocumentDetailViewModel to avoid duplication.
 */
fun buildRtf(text: String, title: String): String = buildString {
    append("{\\rtf1\\ansi\\deff0\n")
    append("{\\fonttbl{\\f0\\froman\\fcharset0 Times New Roman;}{\\f1\\fswiss\\fcharset0 Arial;}}\n")
    append("\\f1\\fs28\\b $title\\b0\\par\\par\n")
    append("\\f0\\fs22\n")
    text.lines().forEach { line ->
        val escaped = line
            .replace("\\", "\\\\")
            .replace("{", "\\{")
            .replace("}", "\\}")
        append(escaped)
        append("\\par\n")
    }
    append("}")
}
