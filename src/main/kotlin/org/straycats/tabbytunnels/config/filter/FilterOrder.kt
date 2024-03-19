package org.straycats.tabbytunnels.config.filter

import org.springframework.core.Ordered
import kotlin.reflect.KClass

object FilterOrder {
    private val order = mapOf<KClass<*>, Int>(
        Pair(PathNormalizeFilter::class, Ordered.LOWEST_PRECEDENCE),
        Pair(TokenAuthenticationFilter::class, Ordered.HIGHEST_PRECEDENCE + 1),
        Pair(TokenAccessibleRouteFilter::class, Ordered.HIGHEST_PRECEDENCE + 3),
    )

    fun getOrder(clazz: KClass<out Any>): Int {
        return order.getOrElse(clazz) { Ordered.LOWEST_PRECEDENCE }
    }
}
