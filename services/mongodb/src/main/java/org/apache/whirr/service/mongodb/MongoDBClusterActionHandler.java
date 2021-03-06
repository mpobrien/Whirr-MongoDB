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
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.service.ClusterActionHandlerSupport;
import com.google.common.collect.Sets;
import java.util.Set;

import java.io.IOException;

/**
 * Base class for MongoDB service handlers.
 */
public abstract class MongoDBClusterActionHandler
       extends ClusterActionHandlerSupport {

  protected static final Set<String> ALL_ROLES = Sets.newHashSet(MongoDBStandaloneClusterActionHandler.ROLE, 
		                                                         MongoDBReplSetMemberClusterActionHandler.ROLE);

  /**
   * Returns a composite configuration that is made up from the global
   * configuration coming from the Whirr core with a hbase defaults
   * properties.
   */
  protected synchronized Configuration getConfiguration(
      ClusterSpec clusterSpec) throws IOException {
    return getConfiguration(clusterSpec,
      MongoDBConstants.FILE_MONGODB_DEFAULT_PROPERTIES);
  }

  /** TODO - Role based config */
  protected String getInstallFunction(Configuration config) {
    return getInstallFunction(config, "mongodb", MongoDBConstants.FUNCTION_INSTALL);
  }

  protected String getConfigureFunction(Configuration config) {
    return getConfigureFunction(config, "mongodb", MongoDBConstants.FUNCTION_CONFIGURE);
  }   

  protected String getStartFunction(Configuration config) {
    return getStartFunction(config, "mongodb", MongoDBConstants.FUNCTION_START);
  }

  protected String getStopFunction(Configuration config) {
    return getStopFunction(config, "mongodb", MongoDBConstants.FUNCTION_STOP);
  }
}
