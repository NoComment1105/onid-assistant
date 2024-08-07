package io.github.nocomment1105.onidassistant.api

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Races(
	val id: Int,
	val raceName: String,
	val track: String,
	val startsAt: Instant
)
