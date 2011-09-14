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

public final class MongoDBConstants {

  public static final String FUNCTION_INSTALL = "install_mongodb";
  public static final String FUNCTION_CONFIGURE = "configure_mongodb";
  public static final String FUNCTION_START = "start_mongodb";
  public static final String FUNCTION_STOP = "stop_mongodb";

  public static final String PARAM_PROVIDER = "-c";
  public static final String PARAM_PORT = "-p";

  public static final String FILE_MONGODB_DEFAULT_PROPERTIES = "whirr-mongodb-default.properties";

  private MongoDBConstants() {
  }

}

