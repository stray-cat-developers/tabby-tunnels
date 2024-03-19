package org.straycats.tabbytunnels.config.filter

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.straycats.tabbytunnels.common.Constant
import org.straycats.tabbytunnels.domain.authentication.token.Token
import reactor.core.publisher.Mono

@Component
class TokenAccessibleRouteFilter : OrderedFilter(), GlobalFilter {
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val token = (exchange.attributes[Constant.Attribute.Token] as? Token) ?: return Mono.error(IllegalAccessError())
        val route = exchange.getAttribute<Route?>(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR)
        if (route != null) {
            val requiredPermission = route.metadata[Constant.Api.MetadataPermissionKey]
            if (token.getAuthorizedService().contains(requiredPermission))
                return chain.filter(exchange)
        }
        return Mono.error(IllegalAccessError())
    }
}
