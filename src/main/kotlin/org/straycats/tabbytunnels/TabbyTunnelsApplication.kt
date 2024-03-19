package org.straycats.tabbytunnels

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.straycats.tabbytunnels.config.AppEnvironment

@EnableConfigurationProperties(value = [AppEnvironment::class])
@SpringBootApplication
class TabbyTunnelsApplication

fun main(args: Array<String>) {
    runApplication<TabbyTunnelsApplication>(*args)
}
