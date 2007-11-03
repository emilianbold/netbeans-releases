#!/bin/zsh
# Script which updates the preindexed files
# See INDICES.txt for more info. 
# Some interactive participation is necessary. The script will start the IDE twice.
# Each time, create a new Rails Project, wait until indexing is done, then exit.
# It's important to create a Rails project and not a plain Ruby project since plain
# Ruby projects may filter out some unnecessary gems.
# Configure the following parameters:

NBCVSHOME=~/netbeans/work
#NATIVERUBYHOME=/Applications/Locomotive2/Bundles/standardRailsMar2007.locobundle/i386
NATIVERUBYHOME=/Users/tor/dev/ruby/install/ruby-1.8.5/

# You probably don't want to change these:
NB=$NBCVSHOME/nbbuild/netbeans/bin/netbeans
# Location of a Ruby interpreter which contains lots of gems
NATIVERUBY=$NATIVERUBYHOME/bin/ruby
SCRATCHFILE=/tmp/native.zip
USERDIR=/tmp/preindexing

#############################################################################################
# No user-configurable parts beyond this point...

RUBY1=$NBCVSHOME/nbbuild/netbeans/ruby1
unset GEM_HOME

find $RUBY1 . -name "netbeans-index*.zip" -exec rm {} \;
find $NATIVERUBYHOME . -name "netbeans-index*.zip" -exec rm {} \;
rm -rf $RUBY1/preindexed/lib

rm -rf $USERDIR
$NB -J-Dgsf.preindexing=true -J-Druby.computeindex --userdir $USERDIR
rm -rf $USERDIR
$NB -J-Dgsf.preindexing=true -J-Druby.computeindex --userdir $USERDIR -J-Druby.interpreter=$NATIVERUBY

# Pack preindexed.zip
cd $RUBY1
rm -f preindexed-jruby.zip
zip -r preindexed-jruby.zip `find . -name "netbeans-index*" | egrep -v "action|active|rails"`
mv preindexed-jruby.zip $NBCVSHOME/ruby/platform/release/preindexed.zip
rm -f preindexed-jruby.zip

# Go to the native installation:
cd $NATIVERUBYHOME
rm -f $SCRATCHFILE
zip -r $SCRATCHFILE `find . -name "netbeans*.zip"` 

cd $RUBY1
rm -rf preindexed
mkdir preindexed
cd preindexed
unzip $SCRATCHFILE
cd ..
rm $NBCVSHOME/ruby/platform/release/preindexed-native.zip
zip -r $NBCVSHOME/ruby/platform/release/preindexed-native.zip preindexed/

