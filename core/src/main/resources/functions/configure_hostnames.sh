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
function configure_hostnames() {
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
    cloudservers | cloudservers-uk | cloudservers-us )
      if which dpkg &> /dev/null; then
        PRIVATE_IP=`/sbin/ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'`
        HOSTNAME=`echo $PRIVATE_IP | tr . -`.static.cloud-ips.com
        echo $HOSTNAME > /etc/hostname
        sed -i -e "s/$PRIVATE_IP.*/$PRIVATE_IP $HOSTNAME/" /etc/hosts
        set +e
        /etc/init.d/hostname restart
        set -e
        sleep 2
        hostname
      fi
      ;;
  esac
}
