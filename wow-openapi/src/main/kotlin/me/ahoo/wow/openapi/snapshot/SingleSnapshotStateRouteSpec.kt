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

package me.ahoo.wow.openapi.snapshot

import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponses
import me.ahoo.wow.api.naming.NamedBoundedContext
import me.ahoo.wow.api.query.Condition
import me.ahoo.wow.modeling.matedata.AggregateMetadata
import me.ahoo.wow.openapi.AbstractAggregateRouteSpecFactory
import me.ahoo.wow.openapi.AggregateRouteSpec
import me.ahoo.wow.openapi.Https
import me.ahoo.wow.openapi.RequestBodyRef.Companion.toRequestBody
import me.ahoo.wow.openapi.ResponseRef.Companion.toResponse
import me.ahoo.wow.openapi.ResponseRef.Companion.withNotFound
import me.ahoo.wow.openapi.RouteIdSpec
import me.ahoo.wow.openapi.RouteSpec
import me.ahoo.wow.openapi.SchemaRef.Companion.toSchemaRef

class SingleSnapshotStateRouteSpec(
    override val currentContext: NamedBoundedContext,
    override val aggregateMetadata: AggregateMetadata<*, *>,
    override val appendTenantPath: Boolean
) : AggregateRouteSpec {
    override val id: String
        get() = RouteIdSpec()
            .aggregate(aggregateMetadata)
            .appendTenant(appendTenantPath)
            .resourceName("snapshot_state")
            .operation("single")
            .build()

    override val method: String
        get() = Https.Method.POST

    override val appendPathSuffix: String
        get() = "snapshot/single/state"

    override val summary: String
        get() = "Single snapshot state"
    override val requestBody: RequestBody = Condition::class.java.toRequestBody()

    private val responseSchema = aggregateMetadata.state.aggregateType.toSchemaRef()

    override val responses: ApiResponses
        get() = responseSchema.ref.toResponse().let {
            ApiResponses().addApiResponse(Https.Code.OK, it)
        }.withNotFound()
}

class SingleSnapshotStateRouteSpecFactory : AbstractAggregateRouteSpecFactory() {
    init {
        Condition::class.java.toSchemaRef().schemas.mergeSchemas()
    }

    override fun create(
        currentContext: NamedBoundedContext,
        aggregateMetadata: AggregateMetadata<*, *>
    ): List<RouteSpec> {
        val defaultRouteSpec = SingleSnapshotStateRouteSpec(currentContext, aggregateMetadata, false)
        val appendTenantPath = aggregateMetadata.staticTenantId.isNullOrBlank()
        if (appendTenantPath) {
            val tenantRouteSpec = SingleSnapshotStateRouteSpec(currentContext, aggregateMetadata, true)
            return listOf(defaultRouteSpec, tenantRouteSpec)
        }
        return listOf(defaultRouteSpec)
    }
}
