package org.straycats.tabbytunnels.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue

internal object Jackson {
    private val mapper = jacksonMapperBuilder()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(MapperFeature.DEFAULT_VIEW_INCLUSION)
        .addModules(JavaTimeModule())
        .build()

    fun getMapper(): ObjectMapper = mapper
}

internal fun <T> T.toJson(): String = Jackson.getMapper().writeValueAsString(this)
internal inline fun <reified T> String.fromJson(): T = Jackson.getMapper().readValue(this)
