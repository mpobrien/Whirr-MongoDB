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
import static org.apache.whirr.service.FirewallManager.Rule;
import static org.jclouds.scriptbuilder.domain.Statements.call;

public class MongoDBReplSetMemberClusterActionHandler extends BaseMongoDBClusterActionHandler {

  public static final String ROLE = "mongodb-replsetmember";
  public static final int PORT = 27017;
  private static final String CFG_KEY_PORT = "mongodb.replset.port";
  private static final Logger LOG =
    LoggerFactory.getLogger(MongoDBReplSetMemberClusterActionHandler.class);

  public MongoDBReplSetMemberClusterActionHandler() {
    super(ROLE, PORT, CFG_KEY_PORT);
  }

  @Override 
  protected void afterConfigure(ClusterActionEvent event) {
    ClusterSpec clusterSpec = event.getClusterSpec();
    Cluster cluster = event.getCluster();

	LOG.info("Configuring replica set members.");

	//Get all the instances that are marked as replica set members
	Set<Cluster.Instance> replSetInstances = cluster.getInstancesMatching(role(ROLE));
	//Just grab the first of these instances, use it to send the rs.initiate()
	Cluster.Instance setLeader = replSetInstances.iterator().next();

	//TODO this is obvs temporary, use the port from config file,
	//need to refactor how its configured/exposed
	
	Mongo mongo;
	DB db;

	try{
		// throws IOExc, UnknownHostExc:
		LOG.info("Connecting to " + setLeader.getPublicAddress().getHostAddress() + " to initiate replica set.");
		mongo = new Mongo(setLeader.getPublicAddress().getHostAddress(), PORT); 
		db = mongo.getDB("admin");
	}catch(Exception e){
		LOG.error("Unable to get public host address of replica set leader, " + e.getMessage());
		return;
	}

	try{
		BasicDBObject configObject = generateReplicaSetConfig(replSetInstances); // throws IOexc
		BasicDBObject commandInfo = new BasicDBObject("replSetInitiate", configObject);
		LOG.info("Sending rs.initiate() command");
		CommandResult initiateResult = db.command(commandInfo);
		LOG.info("Command Result: " + initiateResult.toString());
	}catch(IOException e){
		LOG.error("Unable to get private host addresses of replica set members, " + e.getMessage());
	}finally{
		//TODO any cleanup?
	}
	
  }

  /** 
   * Returns a BasicDBObject containing a replica set config object, usable as param to rs.initiate().
   * 
   * @param memberInstances   A set containing instances to be included in config object.
   * @throws IOException      Thrown if the private IP of any instance is unavailable
   *
   */
  private static BasicDBObject generateReplicaSetConfig(Set<Cluster.Instance> memberInstances)
	  throws IOException 
  {
	  BasicDBObject returnVal = new BasicDBObject();

	  //TODO make the replica set name configurable.
	  returnVal.put("_id", "whirr");
	  int counter = 0;

	  ArrayList replicaSetMembers = new ArrayList();
	  for(Cluster.Instance member : memberInstances){
		  String host = member.getPrivateAddress().getHostAddress() + ":" + PORT; // throws an IOExc

		  replicaSetMembers.add(
            BasicDBObjectBuilder.start()
			  .add("_id", counter)
			  .add("host", host)
			  .get()
		  );
		  counter++;
	  }

	  returnVal.put("members", replicaSetMembers);
	  return returnVal;
  }

	  
}
