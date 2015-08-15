/*
 * Copyright 2015 the original author or authors.
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
package net.kuujo.copycat.raft.session;

import net.kuujo.copycat.util.Listener;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Provides event-based methods for monitoring Raft sessions and communicating between Raft clients and servers.
 * <p>
 * Each client or server connected to any server in a Raft cluster must open a {@link Session}.
 * Sessions can be used by both clients and servers to monitor the connection status of another client or server. When
 * a client first connects to a server, it must register a new session. Once the session has been registered, listeners
 * registered via {@link #onOpen(Consumer)} will be called on <em>both the client and
 * server side</em>. Thereafter, the session can be used to {@link #publish(Object)} and
 * {@link #onReceive(Consumer) receive} messages between client and server.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Session {

  /**
   * Returns the session ID.
   *
   * @return The session ID.
   */
  long id();

  /**
   * Returns a boolean value indicating whether the session is open.
   *
   * @return Indicates whether the session is open.
   */
  boolean isOpen();

  /**
   * Sets an open listener on the session.
   *
   * @param listener The session open listener.
   * @return The listener context.
   */
  Listener<Session> onOpen(Consumer<Session> listener);

  /**
   * Publishes a message to the session.
   *
   * @param message The message to publish.
   * @return A completable future to be called once the message has been published.
   */
  CompletableFuture<Void> publish(Object message);

  /**
   * Sets a session receive listener.
   *
   * @param listener The session receive listener.
   * @return The listener context.
   */
  <T> Listener<T> onReceive(Consumer<T> listener);

  /**
   * Sets a session close listener.
   *
   * @param listener The session close listener.
   * @return The session.
   */
  Listener<Session> onClose(Consumer<Session> listener);

  /**
   * Returns a boolean value indicating whether the session is closed.
   *
   * @return Indicates whether the session is closed.
   */
  boolean isClosed();

  /**
   * Returns a boolean value indicating whether the session is expired.
   *
   * @return Indicates whether the session is expired.
   */
  boolean isExpired();

}
