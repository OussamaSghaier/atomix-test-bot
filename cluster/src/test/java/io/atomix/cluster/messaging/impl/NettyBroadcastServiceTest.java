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
package io.atomix.cluster.messaging.impl;

import java.io.IOException;
import java.net.ServerSocket;

import io.atomix.cluster.ClusterConfig;
import io.atomix.cluster.MemberConfig;
import io.atomix.cluster.MulticastConfig;
import io.atomix.utils.net.Address;
import net.jodah.concurrentunit.ConcurrentTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Netty broadcast service test.
 */
public class NettyBroadcastServiceTest extends ConcurrentTestCase {

  private static final Logger LOGGER = getLogger(NettyBroadcastServiceTest.class);

  NettyBroadcastService netty1;
  NettyBroadcastService netty2;

  Address localAddress1;
  Address localAddress2;
  Address groupAddress;

  @Test
  public void testBroadcast() throws Exception {
    netty1.addListener("test", bytes -> {
      threadAssertEquals(0, bytes.length);
      resume();
    });

    netty2.broadcast("test", new byte[0]);
    await(5000);
  }

  @Before
  public void setUp() throws Exception {
    localAddress1 = Address.from("127.0.0.1", findAvailablePort(5001));
    localAddress2 = Address.from("127.0.0.1", findAvailablePort(5002));
    groupAddress = Address.from("230.0.0.1", findAvailablePort(1234));

    netty1 = new NettyBroadcastService();
    netty1.start(new ClusterConfig()
        .setNodeConfig(new MemberConfig()
            .setAddress(localAddress1))
        .setMulticastConfig(new MulticastConfig()
            .setGroup(groupAddress.address()))).join();

    netty2 = new NettyBroadcastService();
    netty2.start(new ClusterConfig()
        .setNodeConfig(new MemberConfig()
            .setAddress(localAddress2))
        .setMulticastConfig(new MulticastConfig()
            .setGroup(groupAddress.address()))).join();
  }

  @After
  public void tearDown() throws Exception {
    if (netty1 != null) {
      try {
        netty1.stop().join();
      } catch (Exception e) {
        LOGGER.warn("Failed stopping netty1", e);
      }
    }

    if (netty2 != null) {
      try {
        netty2.stop().join();
      } catch (Exception e) {
        LOGGER.warn("Failed stopping netty2", e);
      }
    }
  }

  private static int findAvailablePort(int defaultPort) {
    try {
      ServerSocket socket = new ServerSocket(0);
      socket.setReuseAddress(true);
      int port = socket.getLocalPort();
      socket.close();
      return port;
    } catch (IOException ex) {
      return defaultPort;
    }
  }
}
