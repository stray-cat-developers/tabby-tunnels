package org.straycats.tabbytunnels.config.filter

import org.springframework.core.Ordered

open class OrderedFilter : Ordered {
    override fun getOrder(): Int {
        return FilterOrder.getOrder(this::class)
    }
}
