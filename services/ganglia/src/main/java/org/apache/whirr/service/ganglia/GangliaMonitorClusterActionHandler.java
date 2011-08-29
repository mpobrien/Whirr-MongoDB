package org.apache.whirr.service.ganglia;
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

import static org.apache.whirr.RolePredicates.role;
import static org.jclouds.scriptbuilder.domain.Statements.call;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.Cluster;
import org.apache.whirr.Cluster.Instance;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.RolePredicates;
import org.apache.whirr.service.ClusterActionEvent;
import org.apache.whirr.service.ClusterActionHandlerSupport;
import org.apache.whirr.service.FirewallManager.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class GangliaMonitorClusterActionHandler extends ClusterActionHandlerSupport {
  
  private static final Logger LOG =
    LoggerFactory.getLogger(GangliaMonitorClusterActionHandler.class);
    
  public static final String GANGLIA_MONITOR_ROLE = "ganglia-monitor";
  public static final int GANGLIA_MONITOR_PORT = 8649;

  @Override
  public String getRole() {
    return GANGLIA_MONITOR_ROLE;
  }

  protected Configuration getConfiguration(ClusterSpec spec)
    throws IOException {
    return getConfiguration(spec, "whirr-ganglia-default.properties");
  }

  protected String getInstallFunction(Configuration config) {
    return getInstallFunction(config, getRole(), GangliaCluster.INSTALL_FUNCTION);
  }

  protected String getConfigureFunction(Configuration config) {
    return getConfigureFunction(config, getRole(), GangliaCluster.CONFIGURE_FUNCTION);
  }

  @Override
  protected void beforeBootstrap(ClusterActionEvent event) throws IOException {
    ClusterSpec clusterSpec = event.getClusterSpec();
    Configuration config = getConfiguration(clusterSpec);

    addStatement(event, call(getInstallFunction(config),
      "-c", clusterSpec.getProvider(),
      "-r", GANGLIA_MONITOR_ROLE)
    );
  }

  @Override
  protected void beforeConfigure(ClusterActionEvent event) throws IOException, InterruptedException {
    ClusterSpec clusterSpec = event.getClusterSpec();
    Cluster cluster = event.getCluster();

    event.getFirewallManager().addRule(
        Rule.create().destination(role(GANGLIA_MONITOR_ROLE)).port(GANGLIA_MONITOR_PORT)
    );

    Configuration config = getConfiguration(clusterSpec);
    String configureFunction = getConfigureFunction(config);

    // Call the configure function.
    addStatement(event, call(configureFunction,
            "-c", clusterSpec.getProvider(),
            "-m", cluster.getInstanceMatching(RolePredicates.role(GangliaMetadClusterActionHandler.GANGLIA_METAD_ROLE)).getPrivateIp()));

  }
  
  @Override
  protected void afterConfigure(ClusterActionEvent event) {
    ClusterSpec clusterSpec = event.getClusterSpec();
    Cluster cluster = event.getCluster();

    LOG.info("Completed configuration of {}", clusterSpec.getClusterName());
    String hosts = Joiner.on(',').join(getHosts(cluster.getInstancesMatching(
      role(GANGLIA_MONITOR_ROLE))));
    LOG.info("Monitors: {}", hosts);
  }

  static List<String> getHosts(Set<Instance> instances) {
    return Lists.transform(Lists.newArrayList(instances),
        new Function<Instance, String>() {
      @Override
      public String apply(Instance instance) {
        try {
          return instance.getPublicHostName();
        } catch (IOException e) {
          throw new IllegalArgumentException(e);
        }
      }
    });
  }

}
