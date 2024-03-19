package org.straycats.tabbytunnels.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
class AppEnvironment {
    var useSecureProtocol: Boolean = false
}
