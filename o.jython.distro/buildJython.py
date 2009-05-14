#! /usr/bin/python

""" 
	Module to checkout and zip jython ether in dev mode or production
"""

__author__="alley"
__date__ ="$Aug 9, 2008 9:25:01 AM$"

import os

#repoURL="https://jython.svn.sourceforge.net/svnroot/jython/trunk/jython"
repoURL="https://jython.svn.sourceforge.net/svnroot/jython/tags/Release_2_5rc2/jython"
zipName = "jython-2.5.zip"
location = "jython"
svnCommand="svn export "
zipCommand="zip -rm"
zipLocation="external"+ os.sep 


def checkout():
    """ export jython to working directory """
    print "Checking out jython"
    os.system(svnCommand + " " + repoURL + " " + location)

def buildZip():
    """ build a zip from the checked out files """
    print "removing old jython zip"
    try:
        os.remove(zipLocation + zipName)
    except OSError:
        print "Could not remove zip file"
    os.system(zipCommand + " " + zipLocation + zipName + " " + location)

def updateBinariesList():
    print "TODO: Update external/binaries-list with hex key from the output of:  openssl dgst -sha1 " + zipLocation + zipName + " | awk '{ print toupper($2) }'"

def main():
    checkout()
    buildZip()
    updateBinariesList()

if __name__ == "__main__":
    main()
