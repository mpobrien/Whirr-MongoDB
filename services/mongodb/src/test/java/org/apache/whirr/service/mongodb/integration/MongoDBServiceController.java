/**
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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.whirr.service.mongodb.integration;

import com.mongodb.BasicDBObject;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.whirr.Cluster;
import org.apache.whirr.ClusterController;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.RolePredicates;
import org.apache.whirr.service.mongodb.MongoDBStandaloneClusterActionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class MongoDBServiceController {

  private final String configResource;

  private static final Logger LOG =
      LoggerFactory.getLogger(MongoDBServiceController.class);

  private static final Map<String, MongoDBServiceController> INSTANCES = new HashMap<String, MongoDBServiceController>();

  public static MongoDBServiceController getInstance(String configResource) {
    MongoDBServiceController controller = INSTANCES.get(configResource);
    if (controller == null) {
      controller = new MongoDBServiceController(configResource);
      INSTANCES.put(configResource, controller);
    }
    return controller;
  }

  private boolean running;
  private ClusterSpec clusterSpec;
  private ClusterController controller;
  private Cluster cluster;
  private Mongo mongo;

  private MongoDBServiceController(String configResource) {
    this.configResource = configResource;
  }

  public synchronized boolean ensureClusterRunning() throws Exception {
    if (running) {
      LOG.info("Cluster already running.");
      return false;
    } else {
      startup();
      return true;
    }
  }

  public synchronized void startup() throws Exception {
    LOG.info("Starting up cluster...");
    CompositeConfiguration config = new CompositeConfiguration();
    if (System.getProperty("config") != null) {
      config.addConfiguration(new PropertiesConfiguration(System.getProperty("config")));
    }
    config.addConfiguration(new PropertiesConfiguration(this.configResource));
    clusterSpec = ClusterSpec.withTemporaryKeys(config);
    controller = new ClusterController();

    cluster = controller.launchCluster(clusterSpec);

    waitForMongod();
    running = true;
  }

  private void waitForMongod() {
    LOG.info("Waiting for Mongod...");
    InetAddress mongoAddr = null;
    try {
      mongoAddr = cluster.getInstanceMatching(RolePredicates.role(
          MongoDBStandaloneClusterActionHandler.ROLE)).getPublicAddress();
    } catch (IOException e) {
      throw new RuntimeException("Failed to acquire a Mongod instance");
    }

    while (true) {
      try {
        getMongo(mongoAddr);
        break;
      } catch (Exception e) {
        try {
          LOG.info("FAILED CONNECTING.");
          System.out.print(".");
          Thread.sleep(1000);
        } catch (InterruptedException ex) {
          break;
        }
      }
    }
    System.out.println();
    LOG.info("Master reported in. Continuing.");
  }

  private void getMongo(InetAddress mongoAddr) throws Exception {
    if (mongo == null || mongo.getAddress() != new ServerAddress(mongoAddr))
      mongo = new Mongo(new ServerAddress(mongoAddr));
    /**
     * Test the connection...
     */
    mongo.getDB("test").getCollection("whirr_conn_validation").save(
        new BasicDBObject("foo", "bar"), WriteConcern.SAFE
    );

    LOG.info("Connected to MongoDB Server. ");
  }



  public synchronized void shutdown() throws IOException, InterruptedException {
    LOG.info("Shutting down cluster...");
    if (controller != null) {
      controller.destroyCluster(clusterSpec);
    }
    running = false;
  }


  public Mongo getMongo() {
    return mongo;
  }
}
