/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.instantiation;

import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.service.ServiceLookup;

/**
 * A scheme, or strategy, for creating objects.
 *
 * <p>Implementations are provided by a {@link InstantiatorFactory}.</p>
 */
public interface InstantiationScheme {
    /**
     * Creates a new {@link InstanceFactory} for the given type, which creates instances based on the configuration of this scheme.
     */
    <T> InstanceFactory<T> forType(Class<T> type);

    /**
     * Creates a new {@link Instantiator} which creates instances using the given services, based on the configuration of this scheme.
     */
    Instantiator withServices(ServiceLookup services);

    /**
     * Returns the instantiator which creates instances using a default set of services (usually empty), based on the configuration of this scheme.
     */
    Instantiator instantiator();
}
