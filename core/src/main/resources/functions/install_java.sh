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
function install_java_deb() {
  # Enable multiverse
  # TODO: check that it is not already enabled
  sed -i -e 's/universe$/universe multiverse/' /etc/apt/sources.list
  
  DISTRO=`lsb_release -s -c`
  cat > /etc/apt/sources.list.d/canonical.com.list <<EOF
deb http://archive.canonical.com/ubuntu $DISTRO partner
deb-src http://archive.canonical.com/ubuntu $DISTRO partner
EOF
  
  apt-get update
  
  echo 'sun-java6-bin   shared/accepted-sun-dlj-v1-1    boolean true
sun-java6-jdk   shared/accepted-sun-dlj-v1-1    boolean true
sun-java6-jre   shared/accepted-sun-dlj-v1-1    boolean true
sun-java6-jre   sun-java6-jre/stopthread        boolean true
sun-java6-jre   sun-java6-jre/jcepolicy note
sun-java6-bin   shared/present-sun-dlj-v1-1     note
sun-java6-jdk   shared/present-sun-dlj-v1-1     note
sun-java6-jre   shared/present-sun-dlj-v1-1     note
' | debconf-set-selections
  
  apt-get -y install sun-java6-jdk
  
  echo "export JAVA_HOME=/usr/lib/jvm/java-6-sun" >> /etc/profile
  export JAVA_HOME=/usr/lib/jvm/java-6-sun
  java -version
  
}

function install_java_rpm() {
  MACHINE_TYPE=`uname -m`
  if [ ${MACHINE_TYPE} == 'x86_64' ]; then
    JDK_PACKAGE=jdk-6u21-linux-x64-rpm.bin
  else
    JDK_PACKAGE=jdk-6u21-linux-i586-rpm.bin
  fi
  JDK_INSTALL_PATH=/usr/java
  mkdir -p $JDK_INSTALL_PATH
  cd $JDK_INSTALL_PATH
  wget http://whirr-third-party.s3.amazonaws.com/$JDK_PACKAGE
  chmod +x $JDK_PACKAGE
  mv /bin/more /bin/more.no
  yes | ./$JDK_PACKAGE -noregister
  mv /bin/more.no /bin/more
  rm -f *.rpm $JDK_PACKAGE
  
  export JAVA_HOME=$(ls -d $JDK_INSTALL_PATH/jdk*)
  echo "export JAVA_HOME=$JAVA_HOME" >> /etc/profile
  alternatives --install /usr/bin/java java $JAVA_HOME/bin/java 17000
  alternatives --set java $JAVA_HOME/bin/java
  java -version
}

function install_java() {
  if which dpkg &> /dev/null; then
    install_java_deb
  elif which rpm &> /dev/null; then
    install_java_rpm
  fi
}
