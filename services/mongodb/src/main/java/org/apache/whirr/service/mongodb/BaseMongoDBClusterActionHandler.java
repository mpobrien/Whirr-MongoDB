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
import org.apache.whirr.service.ClusterActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;

import static org.apache.whirr.RolePredicates.role;
import static org.apache.whirr.service.FirewallManager.Rule;
import static org.jclouds.scriptbuilder.domain.Statements.call;

public class BaseMongoDBClusterActionHandler extends MongoDBClusterActionHandler {

  private final String role;
  private final int defaultPort;
  private final String configKeyPort;

  private static final Logger LOG =
    LoggerFactory.getLogger(BaseMongoDBClusterActionHandler.class);

  public BaseMongoDBClusterActionHandler(String role, int port, String configKeyPort) {
      this.role = role;
      this.defaultPort = port;
      this.configKeyPort = configKeyPort;
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
  }

  @Override
  protected void afterBootstrap(ClusterActionEvent event)
            throws IOException, InterruptedException { 
    ClusterSpec clusterSpec = event.getClusterSpec();
    Cluster cluster = event.getCluster();
    Configuration config = getConfiguration(clusterSpec);
  }

  @Override
  protected void beforeConfigure(ClusterActionEvent event) 
            throws IOException, InterruptedException {
    ClusterSpec clusterSpec = event.getClusterSpec();
    Cluster cluster = event.getCluster();
    Configuration config = getConfiguration(clusterSpec);

    int port = defaultPort;

	
    if (configKeyPort != null) {
      port = config.getInt(configKeyPort, defaultPort);
    }

    Cluster.Instance instance = cluster.getInstanceMatching(role(role));
    InetAddress publicAddress = instance.getPublicAddress();

    LOG.info("Opening firewall port for MongoDB on '" + port + "'");
    // TODO - For sharding, only open mongod ports internal to cluster?
    event.getFirewallManager().addRule(
      Rule.create().destination(instance).port(port)
    );

    addStatement(event, call("install_service"));
    addStatement(event, call("install_tarball"));

	String tarUrl = config.getString("whirr.mongodb.tarball.url");
	if(tarUrl != null && tarUrl.length() > 0 ){
		addStatement(event, call(
			getInstallFunction(config),
			MongoDBConstants.PARAM_PROVIDER, clusterSpec.getProvider(),
			MongoDBConstants.PARAM_TARBALL, prepareRemoteFileUrl(event, tarUrl) )
		);
	}else{
		addStatement(event, call(
			getInstallFunction(config),
			MongoDBConstants.PARAM_PROVIDER, clusterSpec.getProvider()));
	}

    /*addStatement(event, call(
        getInstallFunction(config),
        MongoDBConstants.PARAM_PROVIDER, clusterSpec.getProvider())
    );*/

    String configFunction = getConfigureFunction(config);

    // TODO - Config of RS, etc for base classes
    addStatement(event, call(
      configFunction, role,
      MongoDBConstants.PARAM_PROVIDER, clusterSpec.getProvider(),
      MongoDBConstants.PARAM_PORT, String.valueOf(port))
    );

    LOG.info("Called config function....");


    LOG.info("Calling start function "+ getStartFunction(config));
    // TODO - Do we need to start mongo inside the config for RS etc?
    addStatement(event, call(getStartFunction(config)));

  }

  @Override 
  protected void afterConfigure(ClusterActionEvent event) {
    ClusterSpec clusterSpec = event.getClusterSpec();
    Cluster cluster = event.getCluster();

    LOG.info("Completed configuration of {}", clusterSpec.getClusterName());
    /*String hosts = Joiner.on(',').join(getHosts(cluster.getInstancesMatching(
      role(ZOOKEEPER_ROLE))));
    LOG.info("Hosts: {}", hosts);*/
  }
}
