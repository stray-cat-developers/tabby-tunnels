package org.straycats.tabbytunnels.domain.document.api

import io.swagger.v3.oas.annotations.Operation
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties
import org.springdoc.core.properties.SwaggerUiConfigParameters
import org.springdoc.core.utils.Constants
import org.springdoc.webflux.ui.SwaggerConfigResource
import org.springdoc.webflux.ui.SwaggerWelcomeCommon
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.straycats.tabbytunnels.common.Constant
import org.straycats.tabbytunnels.domain.authentication.token.TokenFinder
import java.util.Optional

@ConditionalOnProperty("springdoc.swagger-ui.enabled", havingValue = "true")
@RestController
class DocumentController(
    private val swaggerWelcomeCommon: SwaggerWelcomeCommon,
    private val routeLocator: RouteLocator,
    private val tokenFinder: TokenFinder,
) : SwaggerConfigResource(swaggerWelcomeCommon) {

    @Value(Constants.API_DOCS_URL)
    private lateinit var apiDocUrlPrefix: String

    @Operation(hidden = true)
    @GetMapping(Constants.SWAGGER_CONFIG_URL)
    override fun getSwaggerUiConfig(request: ServerHttpRequest?): MutableMap<String, Any> {
        val result = super.getSwaggerUiConfig(request)
        val allowedServices = (request?.headers?.get(Constant.Api.AuthorizationHeader) ?: emptyList())
            .run { this.getOrNull(0)?.let { tokenFinder.findByKey(it) } }
            ?.map { it?.getAuthorizedService() ?: emptyList() }
            ?.block()

        // Creating data from a proxy server
        val dynamicUrls = (
            routeLocator.routes.filter {
                it.metadata[Constant.Api.MetadataIgnoreDocument] != true &&
                    allowedServices?.contains(it.metadata[Constant.Api.MetadataPermissionKey] ?: "") == true
            }.map {
                val swaggerUrl =
                    AbstractSwaggerUiConfigProperties.SwaggerUrl(it.id, apiDocUrlPrefix + "/" + it.id, it.id)
                Optional.ofNullable(swaggerUrl)
            }.filter { it.isPresent }
                .collectList().block() ?: emptyList()
            ).toMutableSet()

        // provide accessible urls
        result[SwaggerUiConfigParameters.URLS_PROPERTY] = dynamicUrls

        return result
    }
}
