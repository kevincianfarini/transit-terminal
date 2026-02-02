package io.github.kevincianfarini.grtc.state

import com.jakewharton.mosaic.text.AnnotatedString

public data class GrtcServiceAlertsState(val alerts: LoadingState<List<GrtcServiceAlert>, AnnotatedString>)