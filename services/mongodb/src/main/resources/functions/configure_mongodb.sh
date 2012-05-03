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

in_array() {
    local hay needle=$1
    shift
    for hay; do
        [[ $hay == $needle ]] && return 0
    done
    return 1
}

function configure_mongodb() {
  echo "configuring mongodb"
  local OPTIND
  local OPTARG
  
  ROLES=$1
  shift

  echo "configuration args: $@"
  echo "my roles are: $ROLES"

  MONGO_HOME=/var
  MONGO_CONF_DIR=/etc
  MONGO_LOG_DIR=/var/log/mongo
  MONGO_DATA_DIR=/var/lib/mongo
  mkdir $MONGO_LOG_DIR

  PORT=27017
  CLOUD_PROVIDER=
  NOJOURNAL=
  REPLICASET_NAME=whirr
  AUTH_PW=
  AUTH_USER=
  BIND_IP=

  # get parameters
echo "Creating a config file"
cat > /etc/mongodb.conf <<EOF
dbpath = $MONGO_DATA_DIR
logpath = $MONGO_LOG_DIR/mongod.log
logappend = true
fork = true
EOF

while true
do
	case $# in 0) break ;; esac
	case $1 in 
		--nojournal)
			echo "nojournal=true" >> /etc/mongodb.conf
			shift ;;
		--noprealloc)
			echo "noprealloc=true" >> /etc/mongodb.conf
			shift ;;
		--smallfiles)
			echo "smallfiles = true" >> /etc/mongodb.conf
			shift ;;
		--notablescan)
			echo "notablescan = true" >> /etc/mongodb.conf
			shift ;;
		--rest)
			echo "rest = true" >> /etc/mongodb.conf
			shift ;;
		--nohttpinterface)
			echo "nohttpinterface = true" >> /etc/mongodb.conf
			shift ;;
		--nounixsocket)
			echo "nounixsocket = true" >> /etc/mongodb.conf
			shift ;;
		--objcheck)
			echo "objcheck = true" >> /etc/mongodb.conf
			shift ;;
        --oplogSize)
            shift;
            echo "oplogSize = $1" >> /etc/mongodb.conf
            shift ;;
        --journalCommitInterval)
            shift;
            echo "journalCommitInterval = $1" >> /etc/mongodb.conf
            shift ;;
        --nssize)
            shift;
            echo "nssize = $1" >> /etc/mongodb.conf
            shift ;;
        --slowMs)
            shift;
            echo "slowMs = $1" >> /etc/mongodb.conf
            shift ;;
		-p)
			shift;
			PORT=$1; shift ;;
		-c)
			shift;
			CLOUD_PROVIDER="$1"; shift ;;
		-r)
			shift;
			REPLICASET_NAME="$1"; shift ;;
		-w)
			shift;
			AUTH_PW="$1"; shift ;;
		-u)
			shift;
			AUTH_USER="$1"; shift ;;
		-b)
			shift;
			BIND_IP="$1"; shift ;;
		-*)
			break ;;
	esac
done;




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


if [ "$AUTH_PW" -a "$AUTH_USER" ];
then
	echo "auth = true" >> /etc/mongodb.conf
fi

if [ "$BIND_IP" ];
then
	echo "bind_ip = $BIND_IP" >> /etc/mongodb.conf
fi


  for role in $(echo "$ROLES" | tr "," "\n"); do
    case $role in
    mongodb-standalone)
      echo "Configuring mongodb standalone"
      ;;
    mongodb-replsetmember)
      echo "Configuring mongodb replsetmember"
      echo "replSet = $REPLICASET_NAME/localhost:$PORT" >> /etc/mongodb.conf
      ;;
    mongodb-arbiter)
      echo "Configuring mongodb arbiter"
      echo "replSet = $REPLICASET_NAME/localhost:$PORT" >> /etc/mongodb.conf
      ;;
    esac
  done


}
