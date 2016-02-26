#!/usr/bin/env bash

cp target/diagramgen.jar  .
jarfile=diagramgen.jar

cp ${jarfile} /Volumes/C/Users/ohs/eatools/
cp node_to_url.bat /Volumes/C/Users/ohs/eatools/

cp ${jarfile} /Volumes/C/Users/ohs/eaConnectPackages/
cp ${jarfile} /Volumes/C/cygwin/home/ohs/

cp ${jarfile} /Volumes/share/

#java -Dlogback.configurationFile=/path/to/config.xml -jar <jar>

if [ ! -z $1 ]
then
    ../../code/confluence_py/attachFile.sh ove.scheel OppenHeimer2015 2000833 ${jarfile}
fi

echo " "
echo "done"
