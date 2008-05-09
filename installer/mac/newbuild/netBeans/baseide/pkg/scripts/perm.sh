#!/bin/sh -x
echo Changing permissions in `pwd`
find . -perm -100 -exec chmod ugo+x {} \;
find . -perm -200 -exec chmod ugo+w {} \;
find . -perm -400 -exec chmod ugo+r {} \;

