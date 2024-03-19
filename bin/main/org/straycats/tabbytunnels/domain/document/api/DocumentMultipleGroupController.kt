package org.straycats.tabbytunnels.domain.document.api

import io.swagger.v3.oas.annotations.Operation
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.springdoc.api.OpenApiResourceNotFoundException
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springdoc.core.customizers.GlobalOperationCustomizer
import org.springdoc.core.customizers.SpringDocCustomizers
import org.springdoc.core.filters.GlobalOpenApiMethodFilter
import org.springdoc.core.models.GroupedOpenApi
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.providers.SpringDocProviders
import org.springdoc.core.service.AbstractRequestService
import org.springdoc.core.service.GenericResponseService
import org.springdoc.core.service.OpenAPIService
import org.springdoc.core.service.OperationService
import org.springdoc.core.utils.Constants
import org.springdoc.webflux.api.MultipleOpenApiWebFluxResource
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.straycats.tabbytunnels.common.Constant
import org.straycats.tabbytunnels.config.AppEnvironment
import org.straycats.tabbytunnels.domain.authentication.token.TokenFinder
import org.straycats.tabbytunnels.utils.fromJson
import org.straycats.tabbytunnels.utils.toJson
import reactor.core.publisher.Mono
import java.nio.charset.Charset
import java.util.Locale
import java.util.Optional
import java.util.function.Consumer
import java.util.stream.Collectors

@RestController("multipleOpenApiResource")
class DocumentMultipleGroupController(
    private val appEnvironment: AppEnvironment,
    private val groupedOpenApis: List<GroupedOpenApi>,
    private val defaultOpenAPIBuilder: ObjectFactory<OpenAPIService>,
    private val requestBuilder: AbstractRequestService,
    private val responseBuilder: GenericResponseService,
    private val operationParser: OperationService,
    private val springDocConfigProperties: SpringDocConfigProperties,
    private val springDocProviders: SpringDocProviders,
    private val springDocCustomizers: SpringDocCustomizers,
    private val routeLocator: RouteLocator,
    private val tokenFinder: TokenFinder
) : MultipleOpenApiWebFluxResource(
    groupedOpenApis,
    defaultOpenAPIBuilder, requestBuilder,
    responseBuilder, operationParser,
    springDocConfigProperties,
    springDocProviders, springDocCustomizers
) {
    /**
     * The Grouped open api resources.
     */
    private var groupedOpenApiResources: Map<String, DocumentGroupResources>? = null

    private lateinit var documentRequester: CloseableHttpClient

    init {
        val manager = PoolingHttpClientConnectionManagerBuilder.create()
            .build()

        documentRequester = HttpClients.custom()
            .setConnectionManager(manager)
            .setDefaultRequestConfig(
                RequestConfig.custom().build()
            )
            .build()
    }

    @Suppress("UNCHECKED_CAST")
    @Operation(hidden = true)
    @GetMapping(value = [Constants.API_DOCS_URL + "/{group}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    override fun openapiJson(
        serverHttpRequest: ServerHttpRequest,
        @Value(Constants.API_DOCS_URL) apiDocsUrl: String,
        @PathVariable group: String,
        locale: Locale?
    ): Mono<ByteArray> {

        val authorizedServices = serverHttpRequest.headers[Constant.Api.AuthorizationHeader]?.getOrNull(0)
            ?.run { tokenFinder.findByKey(this).block() }
            ?.run { this.getAuthorizedService() }
            ?: emptyList()

        return routeLocator.routes.collectList().map { routes ->
            routes.find { it.id == group } ?: throw IllegalStateException()
        }.doOnNext { route ->
            // Check permissions for requested groups
            val permissionKey = route.metadata[Constant.Api.MetadataPermissionKey]
            if (permissionKey != null && authorizedServices.contains(permissionKey).not()) {
                throw IllegalStateException()
            }
        }.map { route ->
            val result = route?.run {
                val url = this.metadata[Constant.Api.MetadataDocumentationUri]
                val request = HttpGet(url.toString())
                val response = documentRequester.execute(request).entity
                val apiSpecs = EntityUtils.toString(response, Charset.defaultCharset())
                Mono.just(apiSpecs.toByteArray())
            } ?: getOpenApiResourceOrThrow(group).openapiJson(
                serverHttpRequest,
                apiDocsUrl + AntPathMatcher.DEFAULT_PATH_SEPARATOR + group,
                locale
            )

            val apiSpecs = String(result.block()!!).fromJson<MutableMap<String, Any>>()
            val servers: MutableList<Any> = apiSpecs["servers"] as MutableList<Any>? ?: mutableListOf()
            if (servers.isEmpty().not()) {
                (servers[0] as MutableMap<String, String>)["url"] =
                    serverHttpRequest.uri.toString()
                        .replace(serverHttpRequest.uri.rawPath, "")
                        .run {
                            if (appEnvironment.useSecureProtocol) {
                                this.replace("http", "https")
                            } else {
                                this
                            }
                        }
            }
            // Force add security for token authentication
            if (apiSpecs["security"] == null) {
                apiSpecs["security"] = arrayListOf(mapOf("x-partner-key" to emptyList<Any>()))
            }
            if (apiSpecs["components"] != null) {
                val components = apiSpecs["components"] as MutableMap<String, Any>
                if (components["securitySchemes"] == null) {
                    components["securitySchemes"] =
                        mapOf(
                            "x-partner-key" to mapOf(
                                "type" to "apiKey",
                                "name" to "x-partner-key",
                                "in" to "header"
                            )
                        )
                }
            }
            apiSpecs["servers"] = servers
            apiSpecs.toJson().toByteArray()
        }
    }

    @Operation(hidden = true)
    @GetMapping(
        value = [Constants.DEFAULT_API_DOCS_URL_YAML + "/{group}"],
        produces = [Constants.APPLICATION_OPENAPI_YAML]
    )
    override fun openapiYaml(
        serverHttpRequest: ServerHttpRequest?,
        @Value(Constants.DEFAULT_API_DOCS_URL_YAML) apiDocsUrl: String,
        @PathVariable group: String,
        locale: Locale?
    ): Mono<ByteArray> {
        throw IllegalArgumentException("YAML does not supported")
    }

    override fun afterPropertiesSet() {
        this.groupedOpenApis.forEach(
            Consumer { groupedOpenApi: GroupedOpenApi ->
                springDocCustomizers.globalOpenApiCustomizers
                    .ifPresent { openApiCustomizerCollection: List<GlobalOpenApiCustomizer?>? ->
                        groupedOpenApi.addAllOpenApiCustomizer(
                            openApiCustomizerCollection
                        )
                    }
                springDocCustomizers.globalOperationCustomizers
                    .ifPresent { operationCustomizerCollection: List<GlobalOperationCustomizer?>? ->
                        groupedOpenApi.addAllOperationCustomizer(
                            operationCustomizerCollection
                        )
                    }
                springDocCustomizers.globalOpenApiMethodFilters
                    .ifPresent { openApiMethodFilterCollection: List<GlobalOpenApiMethodFilter?>? ->
                        groupedOpenApi.addAllOpenApiMethodFilter(
                            openApiMethodFilterCollection
                        )
                    }
            }
        )
        groupedOpenApiResources = groupedOpenApis.stream()
            .collect(
                Collectors.toMap(
                    { obj: GroupedOpenApi -> obj.group },
                    { item: GroupedOpenApi ->
                        val groupConfig = SpringDocConfigProperties.GroupConfig(
                            item.group,
                            item.pathsToMatch,
                            item.packagesToScan,
                            item.packagesToExclude,
                            item.pathsToExclude,
                            item.producesToMatch,
                            item.consumesToMatch,
                            item.headersToMatch,
                            item.displayName
                        )
                        springDocConfigProperties.addGroupConfig(groupConfig)
                        buildWebFluxOpenApiResource(item)
                    }
                )
            )
    }

    private fun buildWebFluxOpenApiResource(item: GroupedOpenApi): DocumentGroupResources {
        return DocumentGroupResources(
            item.group,
            defaultOpenAPIBuilder,
            requestBuilder,
            responseBuilder,
            operationParser,
            springDocConfigProperties,
            springDocProviders,
            SpringDocCustomizers(
                Optional.of(item.openApiCustomizers), Optional.of(item.operationCustomizers),
                Optional.of(item.routerOperationCustomizers), Optional.of(item.openApiMethodFilters)
            )
        )
    }

    override fun getOpenApiResourceOrThrow(group: String): DocumentGroupResources {
        return groupedOpenApiResources!![group]
            ?: throw OpenApiResourceNotFoundException("No OpenAPI resource found for group: $group")
    }
}
