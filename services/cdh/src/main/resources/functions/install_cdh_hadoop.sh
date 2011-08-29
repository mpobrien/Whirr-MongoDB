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
set -x
function register_cloudera_repo() {
  if which dpkg &> /dev/null; then
    cat > /etc/apt/sources.list.d/cloudera.list <<EOF
deb http://archive.cloudera.com/debian lucid-$REPO contrib
deb-src http://archive.cloudera.com/debian lucid-$REPO contrib
EOF
    curl -s http://archive.cloudera.com/debian/archive.key | sudo apt-key add -
    sudo apt-get update
  elif which rpm &> /dev/null; then
    rm -f /etc/yum.repos.d/cloudera.repo
    REPO_NUMBER=`echo $REPO | sed -e 's/cdh\([0-9][0-9]*\)/\1/'`
    cat > /etc/yum.repos.d/cloudera-$REPO.repo <<EOF
[cloudera-$REPO]
name=Cloudera's Distribution for Hadoop, Version $REPO_NUMBER
mirrorlist=http://archive.cloudera.com/redhat/cdh/$REPO_NUMBER/mirrors
gpgkey = http://archive.cloudera.com/redhat/cdh/RPM-GPG-KEY-cloudera
gpgcheck = 0
EOF
    yum update -y yum
  fi
}

function install_cdh_hadoop() {
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
  
  REPO=${REPO:-cdh3}
  HADOOP=hadoop-${HADOOP_VERSION:-0.20}
  HADOOP_CONF_DIR=/etc/$HADOOP/conf.dist

  register_cloudera_repo
  
  if which dpkg &> /dev/null; then
    apt-get update
    apt-get -y install $HADOOP
    cp -r /etc/$HADOOP/conf.empty $HADOOP_CONF_DIR
    update-alternatives --install /etc/$HADOOP/conf $HADOOP-conf $HADOOP_CONF_DIR 90
  elif which rpm &> /dev/null; then
    yum install -y $HADOOP
    cp -r /etc/$HADOOP/conf.empty $HADOOP_CONF_DIR
    alternatives --install /etc/$HADOOP/conf $HADOOP-conf $HADOOP_CONF_DIR 90
  fi
}
