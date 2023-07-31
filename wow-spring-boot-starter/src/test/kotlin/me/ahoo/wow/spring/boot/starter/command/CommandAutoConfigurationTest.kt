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

package me.ahoo.wow.spring.boot.starter.command

import io.mockk.mockk
import me.ahoo.wow.command.DistributedCommandBus
import me.ahoo.wow.command.InMemoryCommandBus
import me.ahoo.wow.command.LocalCommandBus
import me.ahoo.wow.command.LocalFirstCommandBus
import me.ahoo.wow.spring.boot.starter.BusType
import me.ahoo.wow.spring.boot.starter.enableWow
import org.assertj.core.api.AssertionsForInterfaceTypes
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner

internal class CommandAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()

    @Test
    fun contextLoads() {
        contextRunner
            .enableWow()
            .withPropertyValues("${CommandProperties.BUS_TYPE}=${BusType.IN_MEMORY_NAME}")
            .withUserConfiguration(
                CommandAutoConfiguration::class.java,
            )
            .run { context: AssertableApplicationContext ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasSingleBean(InMemoryCommandBus::class.java)
            }
    }

    @Test
    fun contextLoadsIfLocalFirst() {
        contextRunner
            .enableWow()
            .withBean(DistributedCommandBus::class.java, { mockk() })
            .withUserConfiguration(
                CommandAutoConfiguration::class.java,
            )
            .run { context: AssertableApplicationContext ->
                AssertionsForInterfaceTypes.assertThat(context)
                    .hasSingleBean(LocalCommandBus::class.java)
                    .hasSingleBean(LocalFirstCommandBus::class.java)
            }
    }
}
