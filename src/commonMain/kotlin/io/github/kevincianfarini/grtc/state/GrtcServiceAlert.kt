package io.github.kevincianfarini.grtc.state

import com.jakewharton.mosaic.text.AnnotatedString

public data class GrtcServiceAlert(
    val postedAt: AnnotatedString,
    val message: AnnotatedString
)