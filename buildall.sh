#!/usr/bin/env bash

pushd .

cd ~/bpg2/ohs/nop/
mvn install -DskipTests

cd ~/bpg2/ohs/jops/
mvn install -DskipTests

cd ~/bpg2/ohs/futil/
mvn install -DskipTests

cd ~/bpg2/ohs/eadd/
mvn install -DskipTests

cd ~/bpg2/ohs/cli/
mvn install -DskipTests

popd
mvn install -DskipTests

depgraph
