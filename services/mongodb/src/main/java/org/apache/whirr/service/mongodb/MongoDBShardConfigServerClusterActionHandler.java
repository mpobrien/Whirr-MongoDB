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

package org.apache.whirr.service.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.CommandResult;
import java.io.IOException;
import java.util.Set;
import java.util.ArrayList;
import java.net.InetAddress;
import org.apache.commons.configuration.Configuration;
import org.apache.whirr.Cluster;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.service.ClusterActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.whirr.RolePredicates.role;
import static org.apache.whirr.RolePredicates.anyRoleIn;
import static org.apache.whirr.service.FirewallManager.Rule;
import static org.jclouds.scriptbuilder.domain.Statements.call;
import com.google.common.collect.Sets;
			  

public class MongoDBShardConfigServerClusterActionHandler extends BaseMongoDBClusterActionHandler {

  public static final String ROLE = "mongodb-shard-configserver";
  public static final int PORT = 27019;
  private int arbiterPort = MongoDBShardConfigServerClusterActionHandler.PORT;
  private static final String CFG_KEY_PORT = "mongodb.configserver.port";
  private static final Logger LOG =
    LoggerFactory.getLogger(MongoDBShardConfigServerClusterActionHandler.class);

  public MongoDBShardConfigServerClusterActionHandler() {
    super(ROLE, PORT, CFG_KEY_PORT);
  }

  @Override 
  protected void afterConfigure(ClusterActionEvent event) {
    //ClusterSpec clusterSpec = event.getClusterSpec();
    //Cluster cluster = event.getCluster();
  }

}
