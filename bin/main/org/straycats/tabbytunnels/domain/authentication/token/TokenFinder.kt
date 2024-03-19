package org.straycats.tabbytunnels.domain.authentication.token

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
@Transactional(readOnly = true)
class TokenFinder(
    private val tokenRepository: TokenRepository
) {

    fun findByKey(key: String): Mono<Token?> {
        return Mono.justOrEmpty(tokenRepository.findByKey(key))
    }

    fun findAll(): Flux<Token> {
        return Flux.fromIterable(tokenRepository.findAll())
    }
}
