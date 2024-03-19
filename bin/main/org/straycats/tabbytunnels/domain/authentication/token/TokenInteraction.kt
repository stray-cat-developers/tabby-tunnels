package org.straycats.tabbytunnels.domain.authentication.token

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.straycats.tabbytunnels.domain.authentication.token.api.TokenMaintenanceResources
import reactor.core.publisher.Mono

@Service
@Transactional
class TokenInteraction(
    private val tokenRepository: TokenRepository
) {

    fun register(request: TokenMaintenanceResources.Request.Register): Mono<Token> {
        val token = Token()
            .apply {
                setAuthorizedService(request.services)
            }

        return Mono.just(tokenRepository.save(token))
    }
}
