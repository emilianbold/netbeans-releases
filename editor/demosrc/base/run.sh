#!/bin/sh

CLASSPATH=.
for i in *.jar
do
  CLASSPATH=$CLASSPATH:$i
done

java -classpath $CLASSPATH org.netbeans.editor.example.Editor $@

