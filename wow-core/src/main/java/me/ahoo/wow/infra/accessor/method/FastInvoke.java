/*
 * Copyright [2021-present] [ahoo wang <ahoowang@qq.com>].
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

package me.ahoo.wow.infra.accessor.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <a href="https://detekt.dev/docs/rules/performance/#spreadoperator">
 * SpreadOperator
 * </a>
 */
public final class FastInvoke {
    private FastInvoke() {
    }

    @SuppressWarnings("AvoidObjectArrays")
    public static Object invoke(Method method, Object target, Object[] args)
            throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }

    @SuppressWarnings("AvoidObjectArrays")
    public static <T> T newInstance(Constructor<T> constructor, Object[] args)
            throws InvocationTargetException, InstantiationException,
            IllegalAccessException {
        return constructor.newInstance(args);
    }
}
