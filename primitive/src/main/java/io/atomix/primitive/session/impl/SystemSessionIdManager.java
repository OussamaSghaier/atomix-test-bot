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
package io.atomix.primitive.session.impl;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

import io.atomix.primitive.session.SessionId;
import io.atomix.primitive.session.SessionIdService;
import io.atomix.utils.component.Component;

/**
 * Default session ID service.
 */
@Component
public class SystemSessionIdManager implements SessionIdService {
  private final Random random = new Random();

  @Override
  public CompletableFuture<SessionId> nextSessionId() {
    return CompletableFuture.completedFuture(SessionId.from(random.nextLong()));
  }
}
