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
  public static final String FUNCTION_SETUPAUTH = "auth_mongodb";

  public static final String PARAM_PROVIDER = "-c";
  public static final String PARAM_PORT = "-p";
  public static final String PARAM_TARBALL = "-t";
  public static final String PARAM_REPLSETNAME = "-r";
  public static final String PARAM_PASSWORD = "-w";
  public static final String PARAM_USER = "-u";
  public static final String PARAM_BINDIP = "-b";

  public static final String PARAM_NOJOURNAL = "--nojournal";
  public static final String PARAM_NOPREALLOC = "--noprealloc";
  public static final String PARAM_SMALLFILES = "--smallfiles";
  public static final String PARAM_NOTABLESCAN = "--notablescan";
  public static final String PARAM_REST = "--rest";
  public static final String PARAM_NOHTTP = "--nohttpinterface";
  public static final String PARAM_NOUNIX = "--nounixsocket";
  public static final String PARAM_OBJCHECK = "--objcheck";
  public static final String PARAM_OPLOGSIZE = "--oplogSize";
  public static final String PARAM_SLOWMS = "--slowMs";
  public static final String PARAM_NSSIZE = "--nssize";
  public static final String PARAM_JOURNALINTERVAL = "--journalCommitInterval";

  public static final String CFG_KEY_NOJOURNAL   = "mongodb.nojournal";
  public static final String CFG_KEY_AUTH_PW     = "mongodb.auth.password";
  public static final String CFG_KEY_AUTH_USER   = "mongodb.auth.username";
  public static final String CFG_KEY_REPLSETNAME = "mongodb.replset.name";
  public static final String CFG_KEY_BINDIP      = "mongodb.bindip";

  public static final String CFG_KEY_NOPREALLOC      = "mongodb.noprealloc";
  public static final String CFG_KEY_SMALLFILES      = "mongodb.smallfiles";
  public static final String CFG_KEY_NOTABLESCAN     = "mongodb.notablescan";
  public static final String CFG_KEY_NSSIZE          = "mongodb.nssize"; //
  public static final String CFG_KEY_SLOWMS          = "mongodb.slowms"; //
  public static final String CFG_KEY_REST            = "mongodb.rest";
  public static final String CFG_KEY_NOHTTP          = "mongodb.nohttpinterface";
  public static final String CFG_KEY_JOURNALINTERVAL = "mongodb.journalCommitInterval"; //
  public static final String CFG_KEY_NOUNIX          = "mongodb.nounixsocket";
  public static final String CFG_KEY_OBJCHECK        = "mongodb.objcheck";
  public static final String CFG_KEY_OPLOGSIZE       = "mongodb.oplogSize";

  public static final String FILE_MONGODB_DEFAULT_PROPERTIES = "whirr-mongodb-default.properties";

  private MongoDBConstants() {
  }

}

