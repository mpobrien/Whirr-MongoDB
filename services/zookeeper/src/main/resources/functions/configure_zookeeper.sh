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
function configure_zookeeper() {
  local OPTIND
  local OPTARG
  
  CLOUD_PROVIDER=
  while getopts "c:" OPTION; do
    case $OPTION in
    c)
      CLOUD_PROVIDER="$OPTARG"
      shift $((OPTIND-1)); OPTIND=1
      ;;
    esac
  done
  
  case $CLOUD_PROVIDER in
    # Use private IP for SELF_HOST
    # ZooKeeper listens on all addresses, not just the one specified in server.<id>
    ec2 | aws-ec2 )
      SELF_HOST=`wget -q -O - http://169.254.169.254/latest/meta-data/local-ipv4`
      ;;
    cloudservers-uk | cloudservers-us)
      SELF_HOST=`/sbin/ifconfig eth1 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'`
      ;;
    *)
      SELF_HOST=`/sbin/ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'`
      ;;
  esac
  
  myid_file=/var/log/zookeeper/txlog/myid
  config_file=/etc/zookeeper/conf/zoo.cfg
  
  cat > $config_file <<EOF
# The number of milliseconds of each tick
tickTime=2000
# The number of ticks that the initial
# synchronization phase can take
initLimit=10
# The number of ticks that can pass between
# sending a request and getting an acknowledgement
syncLimit=5
# The directory where the snapshot is stored.
dataDir=/var/log/zookeeper/txlog
# The port at which the clients will connect
clientPort=2181
# The servers in the ensemble
EOF
  
  if [[ $# -gt 1 ]]; then
    id=1
    for server in "$@"; do
      if [ $server == $SELF_HOST ]; then
        myid=$id
      fi
      echo "server.$id=$server:2888:3888" >> $config_file
      id=$((id+1))
    done
  
    if [ -z $myid ]; then
      echo "Could not determine id for my host $SELF_HOST against servers $@."
      exit 1
    fi
    echo $myid > $myid_file
  fi
}
