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

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.Cluster;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.RolePredicates;
import org.apache.whirr.service.ClusterActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;

import java.util.ArrayList;
import java.util.Set;

import static org.apache.whirr.RolePredicates.role;
import static org.apache.whirr.service.FirewallManager.Rule;
import static org.jclouds.scriptbuilder.domain.Statements.call;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class BaseMongoDBClusterActionHandler extends MongoDBClusterActionHandler {

  private final String role;
  private final int defaultPort;
  protected int port;
  private final String configKeyPort;

  private static final Logger LOG =
    LoggerFactory.getLogger(BaseMongoDBClusterActionHandler.class);

  protected String tarUrl = null;

  //Configurable options to go into mongodb.conf file
  protected Boolean noJournal      = null;
  protected String replicaSetName  = null;
  protected String authPassword    = null;
  protected String authUsername    = null;
  protected String bindIp          = null;

  public BaseMongoDBClusterActionHandler(String role, int portNum, String configKeyPort) {
      this.role = role;
      this.defaultPort = portNum;
      this.configKeyPort = configKeyPort;
	  this.port = this.defaultPort;
  }

  @Override 
  public String getRole() {
    return role;
  }

  @Override
  protected void beforeBootstrap(ClusterActionEvent event) 
            throws IOException {
    ClusterSpec clusterSpec = event.getClusterSpec();
    Configuration config = getConfiguration(clusterSpec);

    addStatement(event, call("configure_hostnames",
      MongoDBConstants.PARAM_PROVIDER, clusterSpec.getProvider()));
	this.noJournal  = config.getBoolean(MongoDBConstants.CFG_KEY_NOJOURNAL, null);
	this.replicaSetName  = config.getString(MongoDBConstants.CFG_KEY_REPLSETNAME, null);
	this.authPassword  = config.getString(MongoDBConstants.CFG_KEY_AUTH_PW, null);
	this.authUsername  = config.getString(MongoDBConstants.CFG_KEY_AUTH_USER, null);
	this.bindIp  = config.getString(MongoDBConstants.CFG_KEY_BINDIP, null);
  }

  @Override
  protected void afterBootstrap(ClusterActionEvent event)
            throws IOException, InterruptedException { 
    ClusterSpec clusterSpec = event.getClusterSpec();
    Cluster cluster = event.getCluster();
    Configuration config = getConfiguration(clusterSpec);
	this.tarUrl = config.getString("whirr.mongodb.tarball.url");
	if(this.tarUrl != null && this.tarUrl.length() == 0){
		this.tarUrl = null;
	}
  }

  @Override
  protected void beforeConfigure(ClusterActionEvent event) 
            throws IOException, InterruptedException {
	LOG.info("Handling beforeConfigure event");
    ClusterSpec clusterSpec = event.getClusterSpec();
    Cluster cluster = event.getCluster();
    Configuration config = getConfiguration(clusterSpec);

    if (configKeyPort != null) {
      this.port = config.getInt(configKeyPort, defaultPort);
    }

    LOG.info("Opening firewall port for MongoDB on '" + port + "'");

    // TODO - For sharding, only open mongod ports internal to cluster?
    event.getFirewallManager().addRule(
      Rule.create()
        .destination(cluster.getInstances())
        .ports(port)
    );

    addStatement(event, call("install_service"));
    addStatement(event, call("install_tarball"));

	if(this.tarUrl != null){
		addStatement(event, call(
			getInstallFunction(config), role,
			MongoDBConstants.PARAM_PROVIDER, clusterSpec.getProvider(),
			MongoDBConstants.PARAM_TARBALL, prepareRemoteFileUrl(event, this.tarUrl) )
		);

	}else{
		addStatement(event, call(
			getInstallFunction(config), role,
			MongoDBConstants.PARAM_PROVIDER, clusterSpec.getProvider()));

		addStatement(event, call(getStopFunction(config)));
	}

    String configFunction = getConfigureFunction(config);

	ArrayList<String> configArgs = new ArrayList<String>();
	configArgs.add(role);
	configArgs.add(MongoDBConstants.PARAM_PROVIDER);
	configArgs.add(clusterSpec.getProvider());
	configArgs.add(MongoDBConstants.PARAM_PORT);
	configArgs.add(String.valueOf(port));
	if(this.noJournal != null){
		configArgs.add(MongoDBConstants.PARAM_NOJOURNAL);
		configArgs.add(this.noJournal.toString());
	}

	if(this.replicaSetName != null){
		configArgs.add(MongoDBConstants.PARAM_REPLSETNAME);
		configArgs.add(this.replicaSetName.toString());
	}

    if(this.authPassword != null && this.authUsername != null){
		configArgs.add(MongoDBConstants.PARAM_PASSWORD);
		configArgs.add(this.authPassword);
		configArgs.add(MongoDBConstants.PARAM_USER);
		configArgs.add(this.authUsername);
	}

	if(this.bindIp != null){
		configArgs.add(MongoDBConstants.PARAM_BINDIP);
		configArgs.add(this.bindIp);
	}

    // TODO - Config of RS, etc for base classes
    addStatement(event, call( configFunction, configArgs.toArray(new String[]{})));

    LOG.info("Calling start function: "+ getStartFunction(config));

    // TODO - Do we need to start mongo inside the config for RS etc?
    addStatement(event, call(getStartFunction(config)));

    if(this.authPassword != null && this.authUsername != null){
		addStatement(event, call(MongoDBConstants.FUNCTION_SETUPAUTH, this.authUsername, this.authPassword));
	}

  }

  @Override 
  protected void afterConfigure(ClusterActionEvent event) {
    ClusterSpec clusterSpec = event.getClusterSpec();
    Cluster cluster = event.getCluster();

    LOG.info("Completed configuration of {}", clusterSpec.getClusterName());
  }

//   private void setupAuthUsers(Set<Cluster.Instance> instances, String user, String password){
// 	  for(Cluster.Instance instance : instances){
// 		try{
// 			LOG.info("Connecting to " + instance.getPublicAddress().getHostAddress() + " to configure auth.");
// 			Mongo mongo = new Mongo(instance.getPublicAddress().getHostAddress(), this.port); 
// 			DB db = mongo.getDB("admin");
// 			db.addUser("mike", "mikey".toCharArray());
// 			mongo.close();
// 		}catch(Exception e){
// 			LOG.error("Unable to create auth user on instance");
// 			e.printStackTrace();
// 			continue;
// 		}
// 	  }
//   }

}
