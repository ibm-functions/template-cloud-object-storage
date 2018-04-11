#!/bin/bash

SCRIPTDIR=$(cd $(dirname "$0") && pwd)
HOMEDIR="$SCRIPTDIR/../../../"
DEPLOYDIR="$HOMEDIR/openwhisk/catalog/extra-packages/packageDeploy"

# clone utilties repo. in order to run scanCode.py
cd $HOMEDIR
git clone https://github.com/apache/incubator-openwhisk-utilities.git

# shallow clone OpenWhisk repo.
git clone --depth 1 https://github.com/apache/incubator-openwhisk.git openwhisk

# shallow clone deploy package repo.
git clone --depth 1 https://github.com/apache/incubator-openwhisk-package-deploy $DEPLOYDIR

cd openwhisk

./gradlew \
:common:scala:install \
:core:controller:install \
:core:invoker:install \
:tests:install

# use runtimes.json that defines python-jessie & IBM Node.js 8
rm -f ansible/files/runtimes.json
cp $HOMEDIR/template-cloud-object-storage/ansible/files/runtimes.json ansible/files/runtimes.json

./tools/travis/setup.sh
