package io.github.nocomment1105.onidassistant.utils

import dev.kord.common.entity.Snowflake
import dev.kordex.core.utils.env
import dev.kordex.core.utils.envOrNull

val TOKEN = env("TOKEN")

val SG_API_KEY = envOrNull("SG_API_KEY") ?: ""

val GUILD_ID = Snowflake(env("GUILD_ID"))
