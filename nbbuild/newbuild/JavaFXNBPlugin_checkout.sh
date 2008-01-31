#!/bin/sh
# This script is purposed
#to getting all needed sources
# to start Java FX Netbeans Plugin builds


#Check if there is the main repo 
cd $WORKSPACE
if [ ! -d main ] ;
then 
hg clone  http://hg.netbeans.org/main
else
	cd $WORKSPACE/main
	if [ -d .hg ] 
       	then 
		hg pull http://hg.netbeans.org/main
		hg update -C
	else
		cd $WORKSPACE
		hg clone  http://hg.netbeans.org/main
	fi
fi

#Check if there is the repo main/contrib
cd $WORKSPACE/main

if [ ! -d contrib ] ;
then 
hg clone  http://hg.netbeans.org/main/contrib
else
	cd $WORKSPACE/main/contrib
	if [ -d .hg ] 
       	then 
		hg pull http://hg.netbeans.org/main/contrib
		hg update -C
	else
		cd $WORKSPACE
		hg clone  http://hg.netbeans.org/maincontrib
	fi
fi
