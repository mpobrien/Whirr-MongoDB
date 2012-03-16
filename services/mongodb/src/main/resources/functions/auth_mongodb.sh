
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
function auth_mongodb() {
  if [[ "$1" != "" ]]; then
	  #Create the user in the auth database (must be done from localhost if --auth is already set)
	  USERNAME=$1
	  shift
	  PASSWORD=$1
	  mongo admin --eval "db.addUser('$USERNAME', '$PASSWORD')"
  fi
}
