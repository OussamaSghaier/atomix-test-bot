/*
 * Copyright 2019-present Open Networking Foundation
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
package io.atomix.primitive.event;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Interface for primitives that support events.
 */
public interface AsyncListenable<E> {

  /**
   * Adds a listener.
   *
   * @param listener the listener to add
   * @return a future to be completed once the listener has been added
   */
  CompletableFuture<Void> addListener(Consumer<E> listener);

  /**
   * Removes a listener.
   *
   * @param listener the listener to remove
   * @return a future to be completed once the listener has been removed
   */
  CompletableFuture<Void> removeListener(Consumer<E> listener);

}
