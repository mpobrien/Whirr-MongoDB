#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
function start_mongodb() {
  echo "Starting mongodb."
  sudo /etc/init.d/mongodb start
  echo -n 'Waiting for MongoDB to start (If this is the first startup, files must allocate.)'
  while [ `sudo grep "waiting for connections" /var/log/mongo/mongod.log  | wc -l` == 0 ]; do
    echo 'Waiting for MongoDB to start....'
    sleep 5
  done
}
