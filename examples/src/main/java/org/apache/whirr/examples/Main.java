/*
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

package org.apache.whirr.examples;

import java.io.PrintStream;
import java.util.Arrays;

public class Main {

  private static PrintStream out = System.out;

  private static Example[] examples = {
    new HadoopClusterExample()
  };

  public static void main(String[] args) throws Exception {
    if (args.length > 0) {
      Example example = getByName(args[0]);
      if (example != null) {
        System.exit(example.main(Arrays.copyOfRange(args, 1, args.length)));
      }
    }
    printUsage();
  }

  private static Example getByName(String name) {
    for(Example example : examples)
      if (example.getName().equals(name)) return example;
    return null;
  }

  private static void printUsage() {
    out.println("Usage: ./bin/examples <example-name>");

    out.println("Available examples:");
    for(Example example : examples) {
      out.println("\t" + example.getName());
    }
  }
}
