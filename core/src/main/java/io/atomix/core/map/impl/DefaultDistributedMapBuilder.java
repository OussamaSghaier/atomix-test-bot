/*
 * Copyright 2018-present Open Networking Foundation
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
package io.atomix.core.map.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import io.atomix.core.map.AsyncAtomicMap;
import io.atomix.core.map.AsyncDistributedMap;
import io.atomix.core.map.DistributedMap;
import io.atomix.core.map.DistributedMapBuilder;
import io.atomix.core.map.DistributedMapConfig;
import io.atomix.primitive.ManagedAsyncPrimitive;
import io.atomix.primitive.PrimitiveManagementService;
import io.atomix.primitive.partition.PartitionId;
import io.atomix.primitive.partition.Partitioner;
import io.atomix.utils.serializer.Serializer;

/**
 * Default distributed map builder.
 */
public class DefaultDistributedMapBuilder<K, V> extends DistributedMapBuilder<K, V> {
  public DefaultDistributedMapBuilder(String name, DistributedMapConfig config, PrimitiveManagementService managementService) {
    super(name, config, managementService);
  }

  @Override
  @SuppressWarnings("unchecked")
  public CompletableFuture<DistributedMap<K, V>> buildAsync() {
    return managementService.getPrimitiveRegistry().createPrimitive(name, type)
        .thenApply(v -> newMultitonProxies(MapService.TYPE, MapProxy::new))
        .thenApply(proxies -> proxies.entrySet().stream()
            .map(entry -> Maps.<PartitionId, AsyncAtomicMap<String, byte[]>>immutableEntry(
                entry.getKey(),
                new RawAsyncAtomicMap(entry.getValue(), config.getSessionTimeout(), managementService)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
        .thenApply(partitions -> new PartitionedAsyncAtomicMap(name, type, partitions, Partitioner.MURMUR3))
        .thenCompose(ManagedAsyncPrimitive::connect)
        .thenApply(rawMap -> {
          Serializer serializer = serializer();
          return new TranscodingAsyncAtomicMap<K, V, String, byte[]>(
              rawMap,
              key -> BaseEncoding.base16().encode(serializer.encode(key)),
              string -> serializer.decode(BaseEncoding.base16().decode(string)),
              value -> serializer.encode(value),
              bytes -> serializer.decode(bytes));
        })
        .thenApply(map -> {
          if (config.getCacheConfig().isEnabled()) {
            return new CachingAsyncAtomicMap<>(map, config.getCacheConfig());
          }
          return map;
        })
        .<AsyncAtomicMap<K, V>>thenApply(map -> {
          if (config.isReadOnly()) {
            return new UnmodifiableAsyncAtomicMap<>(map);
          }
          return map;
        })
        .thenApply(DelegatingAsyncDistributedMap::new)
        .thenApply(AsyncDistributedMap::sync);
  }
}
