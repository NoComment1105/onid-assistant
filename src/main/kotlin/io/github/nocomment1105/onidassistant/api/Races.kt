package io.github.nocomment1105.onidassistant.api

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Races(
	val id: Int,
	val raceName: String,
	val track: String,
	val startsAt: Instant
)
