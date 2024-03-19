package org.straycats.tabbytunnels.config

import io.swagger.v3.oas.models.media.Schema
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.context.annotation.Configuration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Configuration
class SwaggerConfiguration {
    init {
        SpringDocUtils.getConfig().replaceWithSchema(
            LocalDateTime::class.java,
            Schema<LocalDateTime>().apply {
                example(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                type("string")
            },
        )
        SpringDocUtils.getConfig().replaceWithSchema(
            LocalTime::class.java,
            Schema<LocalTime>().apply {
                example(LocalTime.now().format(DateTimeFormatter.ISO_TIME))
                type("string")
            },
        )
        SpringDocUtils.getConfig().replaceWithSchema(
            LocalDate::class.java,
            Schema<LocalDate>().apply {
                example(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                type("string")
            },
        )
    }
}
