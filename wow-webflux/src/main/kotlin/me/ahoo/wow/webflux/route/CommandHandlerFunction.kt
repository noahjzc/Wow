/*
 * Copyright [2021-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.wow.webflux.route

import me.ahoo.wow.api.command.CommandMessage
import me.ahoo.wow.command.CommandGateway
import me.ahoo.wow.command.CommandResult
import me.ahoo.wow.command.wait.CommandStage
import me.ahoo.wow.command.wait.WaitingFor
import me.ahoo.wow.modeling.matedata.AggregateMetadata
import me.ahoo.wow.route.CommandRouteMetadata
import me.ahoo.wow.webflux.exception.ExceptionHandler
import me.ahoo.wow.webflux.exception.asServerResponse
import me.ahoo.wow.webflux.route.CommandParser.parse
import me.ahoo.wow.webflux.route.appender.CommandHeaders
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

val DEFAULT_TIME_OUT: Duration = Duration.ofSeconds(30)

/**
 * [org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping]
 *
 * [org.springframework.web.reactive.function.server.support.RouterFunctionMapping]
 */
class CommandHandlerFunction(
    private val aggregateMetadata: AggregateMetadata<*, *>,
    private val commandRouteMetadata: CommandRouteMetadata<out Any>,
    private val commandGateway: CommandGateway,
    private val timeout: Duration = DEFAULT_TIME_OUT,
    private val exceptionHandler: ExceptionHandler
) : HandlerFunction<ServerResponse> {
    private val bodyExtractor = CommandBodyExtractor(commandRouteMetadata)
    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        val commandWaitTimeout = request.headers().firstHeader(CommandHeaders.WAIT_TIME_OUT)?.let {
            Duration.ofMillis(it.toLong())
        } ?: timeout
        return if (commandRouteMetadata.pathVariableMetadata.isEmpty()) {
            request.bodyToMono(commandRouteMetadata.commandMetadata.commandType)
        } else {
            request.body(
                bodyExtractor,
                mapOf(RouterFunctions.URI_TEMPLATE_VARIABLES_ATTRIBUTE to request.pathVariables()),
            )
        }.flatMap {
            request.parse(
                aggregateMetadata = aggregateMetadata,
                commandBody = it,
            )
        }
            .flatMap {
                request.sendCommand(commandGateway, it, commandWaitTimeout)
            }
            .asServerResponse()
            .onErrorResume {
                exceptionHandler.handle(it)
            }
    }

    companion object {
        fun ServerRequest.sendCommand(
            commandGateway: CommandGateway,
            command: CommandMessage<Any>,
            timeout: Duration
        ): Mono<CommandResult> {
            val stage: CommandStage = headers().firstHeader(CommandHeaders.WAIT_STAGE)?.let { stage ->
                CommandStage.valueOf(stage.uppercase(Locale.getDefault()))
            } ?: CommandStage.PROCESSED
            return if (CommandStage.SENT == stage) {
                commandGateway.sendAndWaitForSent(command)
            } else {
                commandGateway.sendAndWait(command, waitStrategy = WaitingFor.stage(stage))
            }.timeout(timeout)
        }
    }

    override fun toString(): String {
        return "CommandHandlerFunction(commandType=$commandRouteMetadata)"
    }
}
