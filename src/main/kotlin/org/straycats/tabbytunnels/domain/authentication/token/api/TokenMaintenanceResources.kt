package org.straycats.tabbytunnels.domain.authentication.token.api

import io.swagger.v3.oas.annotations.media.Schema
import org.straycats.tabbytunnels.domain.authentication.token.Token

class TokenMaintenanceResources {

    class Request {
        @Schema(name = "Token.Maintenance.Request.Register")
        data class Register(
            @Schema(description = "authorized services")
            val services: List<String>
        )
    }

    class Reply {

        @Schema(name = "Token.Maintenance.Reply.Me")
        data class Me(
            @Schema(description = "authentication key")
            val key: String,
            @Schema(description = "authorized services")
            val services: List<String>
        ) {
            companion object {
                fun from(token: Token): Me {
                    return Me(
                        token.key,
                        token.getAuthorizedService()
                    )
                }
            }
        }
    }
}
