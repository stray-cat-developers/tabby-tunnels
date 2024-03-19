package org.straycats.tabbytunnels.domain.authentication.token

import org.springframework.data.jpa.repository.JpaRepository

interface TokenRepository : JpaRepository<Token, Long> {
    fun findByKey(key: String): Token?
}
