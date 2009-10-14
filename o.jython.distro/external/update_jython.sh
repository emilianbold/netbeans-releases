#!/bin/sh

repository=https://jython.svn.sourceforge.net/svnroot/jython/tags/Release_2_5_1/jython
target=jython-2.5.1.zip
dist=jython-2.5.1
location=jython

svn co $repository
cd $location
# Note - need both ant calls
ant
ant jar-complete
# Nuke SVN stuff
#find . -type d -name .svn -exec rm -rf {} \; 
rm -f ../$target
mv dist $dist
zip -r ../$target $dist/Lib $dist/bin/ $dist/jython.jar $dist/registry 
cd ..
echo "Updating binaries-list"
echo `openssl dgst -sha1 $target | awk '{ print toupper($2) }'` $target > binaries-list
echo "Cleaning up"
#rm -rf "$location"
echo "Done."

