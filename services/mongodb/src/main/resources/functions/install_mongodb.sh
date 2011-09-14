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
function register_mongodb_repo() {
  if which dpkg &> /dev/null; then 
    # TODO - Give an option for disabling upstart in favor of sysvinit
    # (10gen offers repos for both styles)
    cat > 10gen-mongodb.list <<EOF
deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen
EOF
    sudo mv 10gen-mongodb.list /etc/apt/sources.list.d/
    sudo apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10
    sudo apt-get update 
  elif which rpm &> /dev/null; then
    # x86_64 or i686
    ARCH=`uname -m`
    # Cleanup
    sudo rm -rf /etc/yum.repos.d/10gen-mongodb.repo
    sudo rm -rf /etc/yum.repos.d/10gen-mongodb_$ARCH.repo
    cat > 10gen-mongodb_$ARCH.repo <<EOF
[10gen-mongodb_$ARCH]
name=10gen MongoDB Repository for $ARCH
baseurl=http://downloads-distro.mongodb.org/repo/redhat/os/$ARCH
gpgcheck=0
EOF
    sudo mv 10gen-mongodb_$ARCH.repo /etc/yum.repos.d/
    sudo yum update -y yum
  fi
}

function update_repo() {
  if which dpkg &> /dev/null; then
    sudo apt-get update
  elif which rpm &> /dev/null; then
    sudo yum update -y yum
  fi
}

function install_mongodb_package() {
  if which dpkg &> /dev/null; then
    sudo apt-get update
    sudo apt-get -y install mongodb-10gen
  elif which rpm &> /dev/null; then
    sudo yum install -y mongo-10gen-server
  fi
}
function install_mongodb() {
  local OPTIND
  local OPTARG

  CLOUD_PROVIDER=
  while getopts "c:" OPTION; do
    case $OPTION in 
    c) 
      CLOUD_PROVIDER="$OPTARG"
      ;;
    esac
  done

  case $CLOUD_PROVIDER in 
    ec2 | aws-ec2 )
      # Alias /mnt as /data
      if [ ! -e /data ]; then sudo ln -s /mnt /data; fi
      ;;
    *)
      ;;
  esac

  MONGO_USER=
  MONGO_HOME=/var
  MONGO_CONF_DIR=/etc
  MONGO_LOG_DIR=/var/log/mongo
  MONGO_DATA_DIR=/var/lib/mongo

  if which dpkg &> /dev/null; then
    MONGO_USER=mongodb
  elif which rpm &> /dev/null; then
    MONGO_USER=mongod
  fi

  register_mongodb_repo
  install_mongodb_package

  # Logging
  sudo rm -rf $MONGO_LOG_DIR
  sudo mkdir -p /data/mongodb/logs
  sudo ln -s /data/mongodb/logs $MONGO_LOG_DIR
  
  # Data
  sudo rm -rf $MONGO_DATA_DIR
  sudo mkdir -p /data/mongodb/data
  sudo ln -s /data/mongodb/data $MONGO_DATA_DIR
  
  sudo chown -R $MONGO_USER:$MONGO_USER /data/mongodb

  # Change the default TCP Keepalive to 300 seconds per 10gen Recommendation
  # See: http://www.mongodb.org/display/DOCS/Troubleshooting#Troubleshooting-Socketerrorsinshardedclustersandreplicasets
  sudo sh -c 'echo 300 > /proc/sys/net/ipv4/tcp_keepalive_time'

   # up ulimits
  sudo sh -c 'echo "root soft nofile 65535" >> /etc/security/limits.conf'
  sudo sh -c 'echo "root hard nofile 65535" >> /etc/security/limits.conf'
  sudo sh -c 'ulimit -n 65535'

  # if there is no hosts file then provide a minimal one
  [ ! -f /etc/hosts ] && sudo sh -c 'echo "127.0.0.1 localhost" > /etc/hosts'

}
