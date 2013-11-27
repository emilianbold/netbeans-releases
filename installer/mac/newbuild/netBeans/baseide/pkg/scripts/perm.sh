#!/bin/sh -x
changedir=$1
echo Changing permissions in $changedir
find $changedir -perm -100 -exec chmod ugo+x {} \;
find $changedir -perm -200 -exec chmod ug+w {} \;
find $changedir -perm -400 -exec chmod ugo+r {} \;
