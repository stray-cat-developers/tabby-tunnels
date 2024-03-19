package org.straycats.tabbytunnels.domain.document.api

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils
import org.springdoc.core.customizers.SpringDocCustomizers
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.providers.SpringDocProviders
import org.springdoc.core.service.AbstractRequestService
import org.springdoc.core.service.GenericResponseService
import org.springdoc.core.service.OpenAPIService
import org.springdoc.core.service.OperationService
import org.springdoc.core.utils.Constants
import org.springdoc.webflux.api.OpenApiWebfluxResource
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono
import java.util.Locale

class DocumentGroupResources
constructor(
    groupName: String,
    openAPIBuilderObjectFactory: ObjectFactory<OpenAPIService>,
    requestBuilder: AbstractRequestService,
    responseBuilder: GenericResponseService,
    operationParser: OperationService,
    springDocConfigProperties: SpringDocConfigProperties,
    springDocProviders: SpringDocProviders,
    springDocCustomizers: SpringDocCustomizers
) : OpenApiWebfluxResource(
    groupName,
    openAPIBuilderObjectFactory,
    requestBuilder,
    responseBuilder,
    operationParser,
    springDocConfigProperties,
    springDocProviders,
    springDocCustomizers
) {
    @Operation(hidden = true)
    @GetMapping(value = [Constants.API_DOCS_URL], produces = [MediaType.APPLICATION_JSON_VALUE])
    override fun openapiJson(
        serverHttpRequest: ServerHttpRequest?,
        apiDocsUrl: String?,
        locale: Locale?
    ): Mono<ByteArray> {
        return super.openapiJson(serverHttpRequest, apiDocsUrl, locale)
    }

    @Operation(hidden = true)
    @GetMapping(value = [Constants.DEFAULT_API_DOCS_URL_YAML], produces = [Constants.APPLICATION_OPENAPI_YAML])
    override fun openapiYaml(
        serverHttpRequest: ServerHttpRequest?,
        @Value(Constants.DEFAULT_API_DOCS_URL_YAML) apiDocsUrl: String?,
        locale: Locale?
    ): Mono<ByteArray> {
        throw IllegalArgumentException("YAML does not supported")
    }

    override fun getServerUrl(serverHttpRequest: ServerHttpRequest, apiDocsUrl: String): String? {
        val requestUrl = decode(serverHttpRequest.uri.toString())
        val springWebProviderOptional = springDocProviders.springWebProvider
        var prefix = StringUtils.EMPTY
        if (springWebProviderOptional.isPresent) prefix =
            springWebProviderOptional.get().findPathPrefix(springDocConfigProperties)
        return requestUrl.substring(0, requestUrl.length - apiDocsUrl.length - prefix.length)
    }
}
