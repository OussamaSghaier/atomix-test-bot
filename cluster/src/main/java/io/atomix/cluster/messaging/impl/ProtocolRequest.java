/*
 * Copyright 2017-present Open Networking Foundation
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
package io.atomix.cluster.messaging.impl;

import com.google.common.base.MoreObjects;
import io.atomix.utils.misc.ArraySizeHashPrinter;
import io.atomix.utils.net.Address;

/**
 * Internal request message.
 */
public class ProtocolRequest extends ProtocolMessage {
  private final Address sender;
  private final String subject;
  private final byte[] payload;

  public ProtocolRequest(long id, Address sender, String subject, byte[] payload) {
    super(id);
    this.sender = sender;
    this.subject = subject;
    this.payload = payload;
  }

  @Override
  public Type type() {
    return Type.REQUEST;
  }

  public String subject() {
    return subject;
  }

  public Address sender() {
    return sender;
  }

  public byte[] payload() {
    return payload;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id())
        .add("subject", subject)
        .add("sender", sender)
        .add("payload", ArraySizeHashPrinter.of(payload()))
        .toString();
  }
}
