#!/bin/sh

ant jar zipall 

if [ $? = 0 ]
then
    version=`java -jar jemmy.jar | sed -e 's/.*://' | sed -e 's/.* //g'`
    rm -f builds/jemmy-${version}\.*
    cp jemmy.jar builds/jemmy-${version}.jar
    cp jemmy.zip builds/jemmy-${version}.zip
    rm -rf classes/*
fi
