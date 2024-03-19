package org.straycats.tabbytunnels.config.filter

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.straycats.tabbytunnels.common.Constant
import org.straycats.tabbytunnels.domain.authentication.token.Token
import org.straycats.tabbytunnels.domain.authentication.token.TokenFinder
import reactor.core.publisher.Mono

@Component
class TokenAuthenticationFilter(
    private val tokenFinder: TokenFinder
) : OrderedFilter(), GlobalFilter {
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val response = exchange.response
        val token = request.headers[Constant.Api.AuthorizationHeader]?.firstOrNull()

        return getAuthenticationOrThrow(token)
            .doOnNext { exchange.attributes[Constant.Attribute.Token] = it }
            .then(chain.filter(exchange))
            .onErrorResume { handleUnAuthorized(response) }
    }

    private fun handleUnAuthorized(response: ServerHttpResponse): Mono<Void> {
        response.statusCode = HttpStatus.UNAUTHORIZED
        return response.setComplete()
    }

    private fun getAuthenticationOrThrow(token: String?): Mono<Token> {
        return Mono.defer { token?.let { tokenFinder.findByKey(it) } ?: throw IllegalArgumentException() }
    }
}
