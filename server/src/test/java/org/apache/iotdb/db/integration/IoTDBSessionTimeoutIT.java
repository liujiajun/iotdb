/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.db.integration;

import org.apache.iotdb.db.conf.IoTDBDescriptor;
import org.apache.iotdb.db.utils.EnvironmentUtils;
import org.apache.iotdb.jdbc.Config;
import org.apache.iotdb.jdbc.IoTDBSQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IoTDBSessionTimeoutIT {

  public static final int SESSION_TIMEOUT = 2000;

  @BeforeClass
  public static void setUp() throws ClassNotFoundException {
    EnvironmentUtils.closeStatMonitor();
    IoTDBDescriptor.getInstance().getConfig().setSessionTimeoutThreshold(SESSION_TIMEOUT);
    EnvironmentUtils.envSetUp();
    Class.forName(Config.JDBC_DRIVER_NAME);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    EnvironmentUtils.cleanEnv();
    IoTDBDescriptor.getInstance().getConfig().setSessionTimeoutThreshold(0);
  }

  @Test
  public void sessionTimeoutTest() {
    try (Connection connection = DriverManager.getConnection("jdbc:iotdb://127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      Thread.sleep(SESSION_TIMEOUT + 5000);
      statement.execute("show storage group");
      fail("session did not timeout as expected");
    } catch (IoTDBSQLException e) {
      assertEquals("601: null", e.getMessage());
    } catch (Exception e) {
      fail(e.getMessage());
    }

    try (Connection connection = DriverManager.getConnection("jdbc:iotdb://127.0.0.1:6667/", "root", "root");
         Statement statement = connection.createStatement()) {
      Thread.sleep(SESSION_TIMEOUT / 2);
      statement.execute("select * from root.sg.d1");
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }
}