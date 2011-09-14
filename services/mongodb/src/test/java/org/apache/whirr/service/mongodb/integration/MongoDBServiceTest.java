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
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MongoDBServiceTest {

  protected static MongoDBServiceController controller;

  @AfterClass
  public static void tearDown() throws Exception {
    controller.shutdown();
  }

  @BeforeClass
  public static void setUp() throws Exception {
    controller = MongoDBServiceController.getInstance("whirr-mongodb-test.properties");
    controller.ensureClusterRunning();
  }

  @Test
  public void test() throws Exception {
    Mongo mongo = controller.getMongo();

    DB db = mongo.getDB("test");
    DBCollection coll = db.getCollection("test_whirr");

    for (int i = 0; i < 200; i++) {
      coll.save(new BasicDBObject("i", i), WriteConcern.SAFE);
    }

    assertTrue(coll.count() == 200);
  }

}

