package org.straycats.tabbytunnels.config.filter

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.http.server.PathContainer
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono
import java.nio.file.Paths

/**
 * request 요청시, relative path를 통해 노출되지 않은 API를 호출할 수 있으므로 predicate pattern 필터를 추가함
 */
@Component
class PathNormalizeFilter : OrderedFilter(), GlobalFilter {
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        // 요청 uri path normalize
        // ex) /v1/open-api/river-otter/../../../v3/api-docs => /v3/api-docs
        val normalizedPath = Paths.get(exchange.request.path.toString()).normalize().toString()
        val attributes = exchange.attributes
        // 매칭된 route
        val route = attributes[ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR] as Route
        // 매칭된 uri 패턴
        val matchedPattern = attributes[ServerWebExchangeUtils.GATEWAY_PREDICATE_MATCHED_PATH_ATTR] as String
        // normalized path와 pattern 다시 비교
        return if (PathPatternParser().parse(matchedPattern).matches(PathContainer.parsePath(normalizedPath)).not()) {
            Mono.error(IllegalAccessError())
        } else {
            chain.filter(exchange)
        }
    }
}
