/*
 * Copyright 2015-present Open Networking Foundation
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

package io.atomix.primitive.service;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.atomix.primitive.Consistency;
import io.atomix.primitive.operation.OperationId;
import io.atomix.primitive.operation.OperationType;
import io.atomix.primitive.operation.PrimitiveOperation;
import io.atomix.primitive.operation.StreamType;
import io.atomix.primitive.util.ByteArrayDecoder;
import io.atomix.primitive.util.ByteArrayEncoder;
import io.atomix.utils.stream.StreamHandler;

/**
 * Facilitates registration and execution of state machine commands and provides deterministic scheduling.
 * <p>
 * The state machine executor is responsible for managing input to and output from a {@link PrimitiveService}.
 * As operations are committed to the Raft log, the executor is responsible for applying them to the state machine.
 * {@link OperationType#COMMAND commands} are guaranteed to be applied to the state machine in the order in which
 * they appear in the Raft log and always in the same thread, so state machines don't have to be thread safe.
 * {@link OperationType#QUERY queries} are not generally written to the Raft log and will instead be applied according
 * to their {@link Consistency}.
 * <p>
 * State machines can use the executor to provide deterministic scheduling during the execution of command callbacks.
 * <pre>
 *   {@code
 *   private Object putWithTtl(Commit<PutWithTtl> commit) {
 *     map.put(commit.operation().key(), commit);
 *     executor.schedule(Duration.ofMillis(commit.operation().ttl()), () -> {
 *       map.remove(commit.operation().key()).close();
 *     });
 *   }
 *   }
 * </pre>
 * As with all state machine callbacks, the executor will ensure scheduled callbacks are executed sequentially and
 * deterministically. As long as state machines schedule callbacks deterministically, callbacks will be executed
 * deterministically. Internally, the state machine executor triggers callbacks based on various timestamps in the
 * Raft log. This means the scheduler is dependent on internal or user-defined operations being written to the log.
 * Prior to the execution of a command, any expired scheduled callbacks will be executed based on the command's
 * logged timestamp.
 * <p>
 * It's important to note that callbacks can only be scheduled during {@link PrimitiveOperation} operations or by recursive
 * scheduling. If a state machine attempts to schedule a callback via the executor during the execution of a
 * query, a {@link IllegalStateException} will be thrown. This is because queries are usually only applied
 * on a single state machine within the cluster, and so scheduling callbacks in reaction to query execution would
 * not be deterministic.
 *
 * @see PrimitiveService
 * @see ServiceContext
 */
public interface ServiceOperationRegistry {

  /**
   * Registers a operation callback.
   *
   * @param operationId the operation identifier
   * @param callback    the operation callback
   * @throws NullPointerException if the {@code operationId} or {@code callback} is null
   */
  void register(OperationId<Void, Void> operationId, Runnable callback);

  /**
   * Registers a no argument operation callback.
   *
   * @param operationId the operation identifier
   * @param callback    the operation callback
   * @param encoder     the response encoder
   * @throws NullPointerException if the {@code operationId} or {@code callback} is null
   */
  <R> void register(OperationId<Void, R> operationId, Supplier<R> callback, ByteArrayEncoder<R> encoder);

  /**
   * Registers a operation callback.
   *
   * @param operationId the operation identifier
   * @param callback    the operation callback
   * @param decoder     the operation decoder
   * @throws NullPointerException if the {@code operationId} or {@code callback} is null
   */
  <T> void register(OperationId<T, Void> operationId, Consumer<T> callback, ByteArrayDecoder<T> decoder);

  /**
   * Registers an operation callback.
   *
   * @param operationId the operation identifier
   * @param callback    the operation callback
   * @param decoder     the operation decoder
   * @param encoder     the response encoder
   * @throws NullPointerException if the {@code operationId} or {@code callback} is null
   */
  <T, R> void register(OperationId<T, R> operationId, Function<T, R> callback, ByteArrayDecoder<T> decoder, ByteArrayEncoder<R> encoder);

  /**
   * Registers an asynchronous operation callback.
   *
   * @param operationId the operation identifier
   * @param callback    the operation callback
   * @param decoder     the operation decoder
   * @param encoder     the response encoder
   * @throws NullPointerException if the {@code operationId} or {@code callback} is null
   */
  <T, R> void register(OperationId<T, R> operationId, StreamType<R> streamType, Function<T, CompletableFuture<R>> callback, ByteArrayDecoder<T> decoder, ByteArrayEncoder<R> encoder);

  /**
   * Registers an operation callback.
   *
   * @param operationId the operation identifier
   * @param streamType  the stream type
   * @param callback    the operation callback
   * @param encoder     the response encoder
   * @throws NullPointerException if the {@code operationId} or {@code callback} is null
   */
  <R> void register(OperationId<Void, R> operationId, StreamType<R> streamType, Consumer<StreamHandler<R>> callback, ByteArrayEncoder<R> encoder);

  /**
   * Registers an operation callback.
   *
   * @param operationId the operation identifier
   * @param streamType  the stream type
   * @param callback    the operation callback
   * @param decoder     the operation decoder
   * @param encoder     the response encoder
   * @throws NullPointerException if the {@code operationId} or {@code callback} is null
   */
  <T, R> void register(OperationId<T, R> operationId, StreamType<R> streamType, BiConsumer<T, StreamHandler<R>> callback, ByteArrayDecoder<T> decoder, ByteArrayEncoder<R> encoder);

}