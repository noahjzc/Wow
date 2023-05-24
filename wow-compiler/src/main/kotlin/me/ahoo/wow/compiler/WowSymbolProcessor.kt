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

package me.ahoo.wow.compiler

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.validate
import me.ahoo.wow.api.annotation.AggregateRoot
import me.ahoo.wow.api.annotation.BoundedContext
import me.ahoo.wow.compiler.AggregateRootResolver.resolveAggregateRoot
import me.ahoo.wow.compiler.BoundedContextResolver.resolveBoundedContext
import me.ahoo.wow.configuration.WOW_METADATA_RESOURCE_NAME
import me.ahoo.wow.serialization.asPrettyJson

/**
 * @see me.ahoo.wow.configuration.WowMetadata
 */
class WowSymbolProcessor(environment: SymbolProcessorEnvironment) :
    SymbolProcessor {
    companion object {
        val BOUNDED_CONTEXT_NAME = BoundedContext::class.qualifiedName!!
        val AGGREGATE_ROOT_NAME = AggregateRoot::class.qualifiedName!!
        const val WOW_METADATA_RESOURCE_PATH = WOW_METADATA_RESOURCE_NAME
    }

    private var wowMetadataMerger: WowMetadataMerger = WowMetadataMerger()

    private val logger = environment.logger
    private val codeGenerator = environment.codeGenerator
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("WowSymbolProcessor - process[$this]")
        val dependencyFiles = mutableSetOf<KSFile>()
        resolver.getSymbolsWithAnnotation(BOUNDED_CONTEXT_NAME)
            .filterIsInstance<KSClassDeclaration>()
            .filter {
                it.validate()
            }
            .forEach {
                it.containingFile?.let { file ->
                    dependencyFiles.add(file)
                }
                val boundedContextMetadata = it.resolveBoundedContext()
                wowMetadataMerger.merge(boundedContextMetadata)
            }

        resolver.getSymbolsWithAnnotation(AGGREGATE_ROOT_NAME)
            .filterIsInstance<KSClassDeclaration>()
            .filter {
                it.validate()
            }
            .forEach {
                it.containingFile?.let { file ->
                    dependencyFiles.add(file)
                }
                val aggregateName = it.asName()
                val aggregate = it.resolveAggregateRoot(resolver)
                wowMetadataMerger.merge(aggregateName, aggregate)
            }
        if (dependencyFiles.isEmpty()) {
            return emptyList()
        }
        val dependencies = Dependencies(aggregating = true, sources = dependencyFiles.toTypedArray())
        val file = codeGenerator
            .createNewFile(
                dependencies = dependencies,
                packageName = "",
                fileName = WOW_METADATA_RESOURCE_PATH,
                extensionName = "",
            )
        file.write(wowMetadataMerger.metadata.asPrettyJson().toByteArray())
        file.close()
        return emptyList()
    }

    override fun finish() {
        logger.info("WowSymbolProcessor - finish[$this]")
    }

    override fun onError() {
        logger.info("WowSymbolProcessor - onError[$this]")
    }
}
