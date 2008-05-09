#!/bin/sh -x

for f in `find $1 -name "*.jar"`
do
  bn=`basename $f`
  if  [ "$bn" != "jhall.jar" ] && [ "$bn" != "derby.jar" ] && [ "$bn" != "derbyclient.jar" ]
  then
    echo Packing $f
    pack200 -J-Xmx256m -g $f.pack $f
    rm $f
  fi
done

