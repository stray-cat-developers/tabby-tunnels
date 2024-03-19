package org.straycats.tabbytunnels.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration
class WebConfiguration {

    @Bean
    fun staticRoutes(): RouterFunction<ServerResponse> {
        return RouterFunctions
            .resources("/webjars/swagger-ui/**", ClassPathResource("static/"))
    }
}
