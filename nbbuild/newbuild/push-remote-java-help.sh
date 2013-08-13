#!/bin/sh

hg pull -b $push_branch
hg up $push_branch -C
rm -rf usersguide/javahelp

# mkdir -p usersguide/javahelp/org/netbeans/modules/usersguide
# cp -r -v $pull_path/org/netbeans/modules/usersguide/* usersguide/javahelp/org/netbeans/modules/usersguide/
mkdir -p usersguide/javahelp

cp -r -v $pull_path/* usersguide/javahelp/

hg add usersguide/javahelp
hg st usersguide/javahelp

DELETED_FILES_COUNT=`hg st -d usersguide/javahelp | wc -l`
echo DELETED_FILES_COUNT: $DELETED_FILES_COUNT
if [ "$DELETED_FILES_COUNT" -gt 0 ]; then
    hg st -d usersguide/javahelp | cut -c 3- | xargs hg rm -A
fi

CHANGED_FILES_COUNT=`hg st -mar usersguide/javahelp | wc -l`
echo CHANGED_FILES_COUNT: $CHANGED_FILES_COUNT
if [ "$CHANGED_FILES_COUNT" -gt 0 ]; then
    echo $CHANGED_FILES_COUNT changes found, let\'s start build and push.
    ant clean build-nozip || exit 2
    echo Build succeed.
    hg pull -u
    hg ci -m "new help files" -u "$commit_username"
    echo Pushing...
    hg push -b $push_branch -f $push_url
    HG_RESULT=$?
    if [ $HG_RESULT == 0 ]; then
        echo Push succeed.
    else
        echo "Hg push failed: $HG_RESULT"
        exit $HG_RESULT;
    fi
else
    echo No changes found, no reason to push.
    hg up -C
    hg clean
fi
