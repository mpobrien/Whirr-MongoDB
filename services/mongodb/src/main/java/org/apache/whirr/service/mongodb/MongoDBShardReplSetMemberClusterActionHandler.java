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
import java.util.Iterator;
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
			  

public class MongoDBShardReplSetMemberClusterActionHandler extends BaseMongoDBClusterActionHandler {

  public static final String ROLE = "mongodb-shard-replsetmember";
  public static final int PORT = 27018;
  private int arbiterPort = MongoDBShardArbiterClusterActionHandler.PORT;
  private int shardReplSetSize = 0;
  private static final String CFG_KEY_PORT = "mongodb.shardreplset.port";
  private static final Logger LOG =
    LoggerFactory.getLogger(MongoDBShardReplSetMemberClusterActionHandler.class);

  public MongoDBShardReplSetMemberClusterActionHandler() {
    super(ROLE, PORT, CFG_KEY_PORT);
  }

  @Override 
  protected void afterConfigure(ClusterActionEvent event) {
    ClusterSpec clusterSpec = event.getClusterSpec();
    Cluster cluster = event.getCluster();

	LOG.info("Configuring replica set members.");

	//Get all the instances that are marked as replica set members
	Set<String> replSetRoles = Sets.newHashSet(ROLE);//, MongoDBShardArbiterClusterActionHandler.ROLE);
	Set<Cluster.Instance> replSetInstances = cluster.getInstancesMatching(anyRoleIn(replSetRoles));
	//Just grab the first of these instances, use it to send the rs.initiate()

    try{
        Configuration config = getConfiguration(clusterSpec);
        shardReplSetSize = config.getInt(MongoDBConstants.CFG_KEY_SHARDREPLSETSIZE);
    }catch(IOException e){
        LOG.error("Replica set size for each shard is not specified.");
    }

    //TODO sanity check the # of allocated instances with the replset size per shard
    //it should be 
    //  - an odd number >= 3
    //  - less than or equal to the # of instances allocated
    //  - # of instances allocated must be divisble by replset size per shard
    
	LOG.info("shardreplsetsize " + shardReplSetSize);
	LOG.info("replSetInstancessize " + replSetInstances.size());
	int numShards = replSetInstances.size() / shardReplSetSize;

	Mongo mongo;
	DB db;

	try{

		//TODO use a java.util.concurrent.ExecutorService here maybe?
		ArrayList<Thread> threadTracker = new ArrayList<Thread>();
		Iterator<Cluster.Instance> instanceIterator = replSetInstances.iterator();
		for(int i=0;i<numShards;i++){
			LOG.info("setting up shard " + i);
			Set<Cluster.Instance> shardReplSet = Sets.newHashSet();
			//Partition all the replica set members allocated
			//into sub-sets so we can assign them to different shards.
			for(int j=0;j<shardReplSetSize;j++){
				Cluster.Instance setMember = instanceIterator.next();
				shardReplSet.add(setMember);
			}

			LOG.info("shard " + i + " is " + shardReplSet);

			BasicDBObject configObject = MongoDBReplSetMemberClusterActionHandler.generateReplicaSetConfig(shardReplSet,
											this.replicaSetName,
											this.arbiterPort, this.port);

			LOG.info("username: " + this.authUsername);
			LOG.info("password: " + this.authPassword);
			ReplicaSetInitializer replicaSetInitializer = new ReplicaSetInitializer(configObject,
																					shardReplSet.iterator().next(),
																					this.authUsername,
																					this.authPassword);
			replicaSetInitializer.setPort(this.port);
			replicaSetInitializer.setArbiterPort(this.arbiterPort);
			Thread worker = new Thread(replicaSetInitializer);
			threadTracker.add(worker);
			worker.start();
		}

		//Wait for all the shards to come up before continuing
		for(Thread t : threadTracker){
			t.join();
		}
	}catch(Exception e){
		LOG.error("Unable to get public host address of replica set leader, " + e.getMessage());
		return;
	}
  }

		  
}
