package org.straycats.tabbytunnels.domain.authentication.token.api

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.straycats.tabbytunnels.common.Reply
import org.straycats.tabbytunnels.domain.authentication.token.Token
import org.straycats.tabbytunnels.domain.authentication.token.TokenFinder
import org.straycats.tabbytunnels.domain.authentication.token.TokenInteraction
import org.straycats.tabbytunnels.utils.toReply
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("v1/maintenance/tokens")
class TokenMaintenanceController(
    private val tokenFinder: TokenFinder,
    private val tokenInteraction: TokenInteraction
) {
    @Operation(description = "fetch all tokens")
    @GetMapping
    fun findAll(): Flux<Token> {
        return tokenFinder.findAll()
    }

    @Operation(description = "issue a token")
    @PostMapping
    fun register(
        @RequestBody request: TokenMaintenanceResources.Request.Register
    ): Mono<Reply<TokenMaintenanceResources.Reply.Me>> {
        return tokenInteraction.register(request)
            .map { TokenMaintenanceResources.Reply.Me.from(it).toReply() }
    }
}
