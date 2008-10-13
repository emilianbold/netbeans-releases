#!/bin/bash
# Script which updates the PHP preindexed files
#
# In advance, create a PHP project. Then configure the $INDEXING_PROJECT below
# to the full path to this project. It will then be used for indexing purposes.
#
# Configure the following parameters:
# Location of your netbeans Mercurial clone:

# Configure the following parameters:
# Location of your netbeans Mercurial clone:
NBHGHOME=/space/mercurial/trunk/main

# Location of a PHP project
INDEXING_PROJECT=/home/petr/NetBeansProjects/PhpProject1

# Any flags to pass to the IDE
VMFLAGS=-J-Xmx1024m

# You probably don't want to change these:
NB=$NBHGHOME/nbbuild/netbeans/bin/netbeans
SCRATCHFILE=/tmp/native.zip
USERDIR=/tmp/preindexing

#############################################################################################
# No user-configurable parts beyond this point...
CLUSTERS=$NBHGHOME/nbbuild/netbeans
PHP=$CLUSTERS/php1

if test ! -f $CLUSTERS/extra/modules/org-netbeans-modules-gsf-tools.jar ; then
  echo "You should build contrib/gsf.tools first, which will automate the indexing process within the IDE when this script is run."
  exit 0
fi

find $CLUSTERS . -name "netbeans-index*.zip" -exec rm {} \;

rm -rf $USERDIR

echo "Running NetBeans .... ";
$NB $VMFLAGS -J-Dgsf.preindexing=true -J-Druby.computeindex -J-Dgsf.preindexing.projectpath=$INDEXING_PROJECT -J-Dnetbeans.full.hack=true --userdir $USERDIR

# Pack preindexed.zip
cd $PHP
rm -f preindexed-php.zip
zip -r preindexed-php.zip `find . -name "netbeans-index-*php*"`
find . -name "netbeans-index-*.zip" -exec rm {} \;
mv preindexed-php.zip $NBHGHOME/php.project/external/preindexed-php.zip
rm -f preindexed-php.zip
