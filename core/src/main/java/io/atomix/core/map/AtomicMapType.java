/*
 * Copyright 2017-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.core.map;

import io.atomix.core.map.impl.DefaultAtomicMapBuilder;
import io.atomix.primitive.PrimitiveManagementService;
import io.atomix.primitive.PrimitiveType;
import io.atomix.utils.component.Component;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Consistent map primitive type.
 */
@Component
public class AtomicMapType<K, V> implements PrimitiveType<AtomicMapBuilder<K, V>, AtomicMapConfig, AtomicMap<K, V>> {
  private static final String NAME = "atomic-map";

  private static final AtomicMapType INSTANCE = new AtomicMapType();

  /**
   * Returns a new consistent map type.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @return a new consistent map type
   */
  @SuppressWarnings("unchecked")
  public static <K, V> AtomicMapType<K, V> instance() {
    return INSTANCE;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public AtomicMapConfig newConfig() {
    return new AtomicMapConfig();
  }

  @Override
  public AtomicMapBuilder<K, V> newBuilder(String name, AtomicMapConfig config, PrimitiveManagementService managementService) {
    return new DefaultAtomicMapBuilder<>(name, config, managementService);
  }

  @Override
  public String toString() {
    return toStringHelper(this)
        .add("name", name())
        .toString();
  }
}
