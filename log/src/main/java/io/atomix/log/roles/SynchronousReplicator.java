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
package io.atomix.log.roles;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import io.atomix.log.impl.DistributedLogServerContext;
import io.atomix.log.protocol.BackupOperation;
import io.atomix.log.protocol.BackupRequest;
import io.atomix.log.protocol.ResponseStatus;
import org.slf4j.Logger;

/**
 * Synchronous replicator.
 */
class SynchronousReplicator implements Replicator {
  private final DistributedLogServerContext context;
  private final Logger log;
  private final Map<String, BackupQueue> queues = new HashMap<>();
  private final Map<Long, CompletableFuture<Void>> futures = new LinkedHashMap<>();

  SynchronousReplicator(DistributedLogServerContext context, Logger log) {
    this.context = context;
    this.log = log;
  }

  @Override
  public CompletableFuture<Void> replicate(BackupOperation operation) {
    if (context.followers().isEmpty()) {
      context.setCommitIndex(operation.getIndex());
      return CompletableFuture.completedFuture(null);
    }

    CompletableFuture<Void> future = new CompletableFuture<>();
    futures.put(operation.getIndex(), future);
    Collection<String> followers = context.followers();
    for (String backup : followers) {
      queues.computeIfAbsent(backup, BackupQueue::new).add(operation);
    }
    return future;
  }

  /**
   * Completes futures.
   */
  private void completeFutures() {
    long commitIndex = context.followers().stream()
        .map(follower -> queues.get(follower))
        .filter(queue -> queue != null)
        .map(queue -> queue.ackedIndex)
        .reduce(Math::min)
        .orElse(0L);
    for (long i = context.getCommitIndex() + 1; i <= commitIndex; i++) {
      CompletableFuture<Void> future = futures.remove(i);
      if (future != null) {
        future.complete(null);
      }
    }
    context.setCommitIndex(commitIndex);
  }

  @Override
  public void close() {
    futures.values().forEach(f -> f.completeExceptionally(new IllegalStateException("Not the primary")));
  }

  /**
   * Synchronous backup queue.
   */
  private final class BackupQueue {
    private final Queue<BackupOperation> operations = new LinkedList<>();
    private final String memberId;
    private boolean inProgress;
    private long ackedIndex;

    BackupQueue(String memberId) {
      this.memberId = memberId;
    }

    /**
     * Adds an operation to the queue.
     *
     * @param operation the operation to add
     */
    void add(BackupOperation operation) {
      operations.add(operation);
      maybeBackup();
    }

    /**
     * Sends the next batch if operations are queued and no backup is already in progress.
     */
    private void maybeBackup() {
      if (!inProgress && !operations.isEmpty()) {
        inProgress = true;
        backup();
      }
    }

    /**
     * Sends the next batch of operations to the backup.
     */
    private void backup() {
      List<BackupOperation> operations = new LinkedList<>();
      long index = 0;
      while (operations.size() < 100 && !this.operations.isEmpty()) {
        BackupOperation operation = this.operations.remove();
        operations.add(operation);
        index = operation.getIndex();
      }

      long lastIndex = index;
      BackupRequest request = BackupRequest.newBuilder()
          .setLeader(context.serverId())
          .setTerm(context.currentTerm())
          .setIndex(context.getCommitIndex())
          .addAllOperations(operations)
          .build();

      log.trace("Sending {} to {}", request, memberId);
      context.protocol().backup(memberId, request).whenCompleteAsync((response, error) -> {
        if (error == null) {
          log.trace("Received {} from {}", response, memberId);
          if (response.getStatus() == ResponseStatus.OK) {
            ackedIndex = lastIndex;
            completeFutures();
          } else {
            log.trace("Replication to {} failed!", memberId);
          }
        } else {
          log.trace("Replication to {} failed! {}", memberId, error);
        }
        inProgress = false;
        maybeBackup();
      }, context.threadContext());
      operations.clear();
    }
  }
}
