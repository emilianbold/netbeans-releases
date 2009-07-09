#!/bin/sh

#
# jython-parser.jar is updated from the jython sources. In theory, you could
# just use the standard Jython trunk or particular build - run the developer-build
# target and then copy out the resulting jython-dev
# jar. However, Jython currently only supports 2.5 - and chokes on 2.6 constructs.
# To avoid NetBeans not being able to even index a 2.6 installation, Frank has
# provided a port which contains a 2.6 grammar. We're using that one for IDE
# parsing - and a more standard 2.5 build for execution.
#
# Run this script to update the jython.jar when applicable.
#


# Location to fetch Jython sources from:
#repository=https://jython.svn.sourceforge.net/svnroot/jython/tags/Release_2_5beta0/jython
#repository=https://jython.svn.sourceforge.net/svnroot/jython/trunk/jython
#repository=https://jython.svn.sourceforge.net/svnroot/jython/trunk/sandbox/wierzbicki/grammar26
#repository=https://jython.svn.sourceforge.net/svnroot/jython/tags/Release_2_5rc2/jython
repository=https://jython.svn.sourceforge.net/svnroot/jython/branches/jy26

# Name of jar file we're creating from the jython.jar
target=jython-parser.jar

# Temp build location
location=tmp

rm -rf "$location"
echo "Svn checkout from $repository to $location"
svn export "$repository" "$location"
cd "$location"
echo "Applying Frank's patch to update $location/src/org/python/core/util/FileUtil.java"
patch -p0 < isatty.diff
echo "Building bits"
ant jar-complete
#cp dist/jython-dev.jar ../$target
cp dist/jython.jar ../$target
cd ..
echo "Updating binaries-list"
echo `openssl dgst -sha1 $target | awk '{ print toupper($2) }'` $target > binaries-list
echo "Cleaning up"
#rm -rf "$location"
echo "Done."

