#!/bin/sh

INSTALATION_DIR=`dirname $0`;

CLASSPATH=$INSTALATION_DIR:$CLASSPATH;

for i in $INSTALATION_DIR/*.jar;
do
  CLASSPATH=$CLASSPATH:$i
done

java -Dstandalone.distribution=$INSTALATION_DIR -classpath $CLASSPATH org.netbeans.editor.example.Editor $@

