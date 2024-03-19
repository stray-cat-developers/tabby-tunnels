package org.straycats.tabbytunnels.config.filter

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.http.server.PathContainer
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono
import java.nio.file.Paths

/**
 * Add a predicate pattern filter because an unexposed API can be called through relative paths
 */
@Component
class PathNormalizeFilter : OrderedFilter(), GlobalFilter {
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        // normalize request path
        // ex) /v1/open-api/river-otter/../../../v3/api-docs => /v3/api-docs
        val normalizedPath = Paths.get(exchange.request.path.toString()).normalize().toString()
        val attributes = exchange.attributes
        val matchedPattern = attributes[ServerWebExchangeUtils.GATEWAY_PREDICATE_MATCHED_PATH_ATTR] as String
        // compare normalized path and matched pattern
        return if (PathPatternParser().parse(matchedPattern).matches(PathContainer.parsePath(normalizedPath)).not()) {
            Mono.error(IllegalAccessError())
        } else {
            chain.filter(exchange)
        }
    }
}
