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

package me.ahoo.wow.openapi.converter

import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import me.ahoo.wow.api.Version
import me.ahoo.wow.eventsourcing.snapshot.Snapshot
import me.ahoo.wow.serialization.MessageRecords

/**
 * Snapshot Converter
 * @see me.ahoo.wow.serialization.state.SnapshotSerializer
 */
class SnapshotConverter : TargetTypeModifyConverter() {
    override val targetType: Class<*> = Snapshot::class.java

    override fun modify(resolvedSchema: Schema<*>): Schema<*> {
        resolvedSchema.properties.remove(MessageRecords.AGGREGATE_ID)
        resolvedSchema.properties.remove(Snapshot<*>::expectedNextVersion.name)
        resolvedSchema.properties.remove(Version::initialized.name)
        resolvedSchema.properties.remove("initialVersion")
        resolvedSchema.properties[MessageRecords.CONTEXT_NAME] = StringSchema()
        resolvedSchema.properties[MessageRecords.AGGREGATE_NAME] = StringSchema()
        resolvedSchema.properties[MessageRecords.AGGREGATE_ID] = StringSchema()
        resolvedSchema.properties[MessageRecords.TENANT_ID] = StringSchema()
        return resolvedSchema
    }
}
