/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.log.protocol;

import java.util.Collection;
import java.util.Map;

/**
 * Base class for Raft protocol.
 */
public abstract class TestLogProtocol {
  private final Map<String, TestLogServerProtocol> servers;
  private final Map<String, TestLogClientProtocol> clients;

  public TestLogProtocol(Map<String, TestLogServerProtocol> servers, Map<String, TestLogClientProtocol> clients) {
    this.servers = servers;
    this.clients = clients;
  }

  TestLogServerProtocol server(String memberId) {
    return servers.get(memberId);
  }

  Collection<TestLogServerProtocol> servers() {
    return servers.values();
  }

  TestLogClientProtocol client(String memberId) {
    return clients.get(memberId);
  }
}
