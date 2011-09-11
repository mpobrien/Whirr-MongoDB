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
function configure_mongodb() {
  local OPTIND
  local OPTARG
  
  ROLES=$1
  shift

  MONGO_HOME=/var
  MONGO_CONF_DIR=/etc
  MONGO_LOG_DIR=/var/log/mongo
  MONGO_DATA_DIR=/var/lib/mongo

  PORT=27017
  CLOUD_PROVIDER=

  # get parameters

  while getopts "p:c" OPTION; do
    case $OPTION in 
      p)
        PORT="$OPTARG"
        ;;
      c)
        CLOUD_PROVIDER="$OPTARG"
        ;;
    esac
  done

  # determine machine name
  case $CLOUD_PROVIDER in
    ec2 | aws-ec2 )
      # Use public hostname for EC2
      SELF_HOST=`wget -q -O - http://169.254.169.254/latest/meta-data/public-hostname`
      ;;    
    cloudservers-uk | cloudservers-us)
      SELF_HOST=`/sbin/ifconfig eth1 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'`
      ;;
    *)
      SELF_HOST=`/sbin/ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'`
      ;;
  esac

  # Mount Points
  case $CLOUD_PROVIDER in
  *)
    MOUNT=/data
    ;;
  esac

  sudo sed -i -e "s|port.*|port = $PORT|" $MONGO_CONF_DIR/mongod.conf
}
