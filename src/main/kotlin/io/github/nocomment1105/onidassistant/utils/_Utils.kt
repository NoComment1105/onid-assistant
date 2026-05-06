package io.github.nocomment1105.onidassistant.utils

import io.github.oshai.kotlinlogging.KLogger
import io.ktor.client.plugins.*
import io.ktor.http.*

/**
 * A utility function to handle responding to errors. Currently only supports Client request exceptions
 *
 * @param logger The [KLogger] to send the logging information too
 * @param e The exception that was thrown
 */
fun errorResponse(logger: KLogger, e: Exception) {
	if (e is ClientRequestException && e.response.status.value in 400 until 600) {
		if (e.response.status.value == HttpStatusCode.NotFound.value) {
			logger.debug { "Code ${e.response.status}" }
		} else {
			logger.error(e) { "Code ${e.response.status}" }
		}
	}
}
