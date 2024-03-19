package org.straycats.tabbytunnels.domain.authentication.token

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.straycats.tabbytunnels.utils.fromJson
import org.straycats.tabbytunnels.utils.toJson
import java.time.LocalDateTime
import java.util.UUID

@Entity
class Token(
    @Column(name = "`key`", unique = true)
    val key: String = UUID.randomUUID().toString().replace("-", "")
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    private var authorizedServices: String? = null

    var expired: Boolean = false
        protected set

    var expiredAt: LocalDateTime? = null
        protected set

    fun setAuthorizedService(services: List<String>) {
        this.authorizedServices = services.toJson()
    }

    fun getAuthorizedService() = this.authorizedServices?.fromJson<List<String>>() ?: emptyList()

    fun expire() {
        this.expired = true
        this.expiredAt = LocalDateTime.now()
    }
}
