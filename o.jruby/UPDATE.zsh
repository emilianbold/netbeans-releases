#!/bin/zsh
# Script which updates the preindexed files
# See INDICES.txt for more info. 
#
# In advance, create a Rails project. Then configure the $INDEXING_PROJECT below
# to the full path to this project. It will then be used for indexing purposes.
# (It's important that this is currently a Rails project since Rails contains both
# Ruby and the JavaScript libraries in its load path; also, unlike Ruby projects, Rails
# projects don't exclude any of the gems.)
#
# Older:
# Some interactive participation is necessary. The script will start the IDE twice.
# Each time, create a new Rails Project, wait until indexing is done, then exit.
# It's important to create a Rails project and not a plain Ruby project since plain
# Ruby projects may filter out some unnecessary gems.
#
# You can invoke this script with the extra parameter "local" to only update the local zip files (preindexed)
# or "native" to only update the native zips, or "both" to update everything.
#
# To try debugging this, set up the properties in GsfModuleInstaller.java's restored method, for example like this:
# System.setProperty("gsf.preindexing", "true");
# System.setProperty("netbeans.full.hack=true", "true");
# System.setProperty("ruby.interpreter", "/Users/tor/dev/ruby/install/ruby-1.8.5/bin/ruby");
# System.setProperty("gsf.preindexing.projectpath", "/Users/tor/NetBeansProjects/RailsPreindexProject");
#


#
# Configure the following parameters:
#
NBHGHOME=~/netbeans/hg/main
#NATIVERUBYHOME=/Applications/Locomotive2/Bundles/standardRailsMar2007.locobundle/i386
NATIVERUBYHOME=/Users/tor/dev/ruby/install/ruby-1.8.5/
#NATIVERUBYHOME=/home/tor/dev/ruby-1.8.5
VMFLAGS=-J-Xmx1024m
# -ANY- Rails project will do, I chose a recent one to avoid getting Rails version mismatch warning dialogs
INDEXING_PROJECT=/Users/tor/NetBeansProjects/RailsPreindexProject

# You probably don't want to change these:
NB=$NBHGHOME/nbbuild/netbeans/bin/netbeans
# Location of a Ruby interpreter which contains lots of gems
NATIVERUBY=$NATIVERUBYHOME/bin/ruby
SCRATCHFILE=/tmp/native.zip
USERDIR=/tmp/preindexing
TMP_BINARIES=/tmp/binaries-list

#############################################################################################
# No user-configurable parts beyond this point...

ar="$1"
if test "$ar" = "" ; then
  ar="both"
fi

export PATH=$NATIVERUBYHOME/bin:$PATH
CLUSTERS=$NBHGHOME/nbbuild/netbeans
RUBY=$CLUSTERS/ruby2
GSF=$CLUSTERS/gsf1
unset GEM_HOME

if test ! -f $CLUSTERS/extra/modules/org-netbeans-modules-gsf-tools.jar ; then
  echo "You should build contrib/gsf.tools first, which will automate the indexing process within the IDE when this script is run."
  exit 0
fi

find $CLUSTERS -name "netbeans-index*.zip" -exec rm {} \;
rm -rf $RUBY/preindexed/lib
rm -rf $GSF/preindexed-javascript/lib

if test "$ar" = "local" -o  "$ar" = "both" ; then

rm -rf $USERDIR
$NB $VMFLAGS -J-Dgsf.preindexing=true -J-Druby.computeindex -J-Dgsf.preindexing.projectpath=$INDEXING_PROJECT -J-Dnetbeans.full.hack=true --userdir $USERDIR

# Pack preindexed.zip
#cd $CLUSTERS
cd $RUBY
rm -f preindexed-jruby.zip
find . -name "netbeans-index-*php*.zip" -exec rm {} \;
find . -name "netbeans-index-*groovy*.zip" -exec rm {} \;
#zip -r preindexed-jruby.zip `find . -name "netbeans-index-*ruby*" | egrep -v "action|active|rails"`
zip -r preindexed-jruby.zip `find . -name "netbeans-index-*" | egrep -v "action|active|rails"`
mv preindexed-jruby.zip $NBHGHOME/ruby.platform/external/preindexed.zip
rm -f preindexed-jruby.zip

cd $GSF
rm -f preindexed-javascript.zip
find . -name "netbeans-index-*php*.zip" -exec rm {} \;
find . -name "netbeans-index-*groovy*.zip" -exec rm {} \;
#zip -r preindexed-javascript.zip `find . -name "netbeans-index-*javascript*" | egrep -v "action|active|rails"`
zip -r preindexed-javascript.zip `find . -name "netbeans-index-*" | egrep -v "action|active|rails"`
mv preindexed-javascript.zip $NBHGHOME/javascript.editing/external/preindexed.zip
rm -f preindexed-javascript.zip

fi

# NATIVE
if test "$ar" = "native" -o  "$ar" = "both" ; then

find $NATIVERUBYHOME . -name "netbeans-index*.zip" -exec rm {} \;
rm -rf $USERDIR
$NB $VMFLAGS -J-Dgsf.preindexing=true -J-Druby.computeindex -J-Dgsf.preindexing.projectpath=$INDEXING_PROJECT -J-Dnetbeans.full.hack=true --userdir $USERDIR -J-Druby.interpreter=$NATIVERUBY

# Go to the native installation:
# Ruby
cd $NATIVERUBYHOME
rm -f $SCRATCHFILE
zip -r $SCRATCHFILE `find . -name "netbeans-index-*ruby*.zip"` 
cd $RUBY
rm -rf preindexed
mkdir preindexed
cd preindexed
unzip $SCRATCHFILE
cd ..
rm -f $NBHGHOME/ruby.platform/external/preindexed-native.zip
find . -name "netbeans-index-*php*.zip" -exec rm {} \;
find . -name "netbeans-index-*groovy*.zip" -exec rm {} \;
zip -r $NBHGHOME/ruby.platform/external/preindexed-native.zip preindexed/

# JavaScript
cd $NATIVERUBYHOME
rm -f $SCRATCHFILE
echo "**************"
echo "Indexing complete. There should be no output after this:"
find . -name "netbeans-index-*javascript*.zip"
#zip -r $SCRATCHFILE `find . -name "netbeans-index-*javascript*.zip"` 
#cd $GSF
#rm -rf preindexed-javascript
#mkdir preindexed-javascript
#cd preindexed-javascript
#unzip $SCRATCHFILE
#cd ..
#rm -f $NBHGHOME/javascript.editing/external/preindexed-native.zip
#find . -name "netbeans-index-*php*.zip" -exec rm {} \;
#find . -name "netbeans-index-*groovy*.zip" -exec rm {} \;
#zip -r $NBHGHOME/javascript.editing/external/preindexed-native.zip preindexed-javascript/
fi




# Update binaries-list -- manual upload is still necessary!
# Ruby
rm -f $TMP_BINARIES
cat $NBHGHOME/ruby.platform/external/binaries-list | sed '/preindexed.zip/d' | sed '/preindexed-native.zip/d' > $TMP_BINARIES
echo `openssl dgst -sha1 $NBHGHOME/ruby.platform/external/preindexed.zip | awk '{ print toupper($2) }'` preindexed.zip >> $TMP_BINARIES
echo `openssl dgst -sha1 $NBHGHOME/ruby.platform/external/preindexed-native.zip | awk '{ print toupper($2) }'` preindexed-native.zip >> $TMP_BINARIES
mv $TMP_BINARIES $NBHGHOME/ruby.platform/external/binaries-list

# JavaScript
rm -f $TMP_BINARIES
cat $NBHGHOME/javascript.editing/external/binaries-list | sed '/preindexed.zip/d' > $TMP_BINARIES
echo `openssl dgst -sha1 $NBHGHOME/javascript.editing/external/preindexed.zip | awk '{ print toupper($2) }'` preindexed.zip >> $TMP_BINARIES
mv $TMP_BINARIES $NBHGHOME/javascript.editing/external/binaries-list

