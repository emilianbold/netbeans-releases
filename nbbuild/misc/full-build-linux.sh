#!/bin/sh
# Build and test NetBeans from scratch, assuming Linux.
# Mostly just calls Ant, but sets up some other things to make it more convenient.
# See: http://www.netbeans.org/community/guidelines/commit.html
# Author: jglick@netbeans.org  Bug reports: http://www.netbeans.org/issues/enter_bug.cgi?component=nbbuild

# Adjust the following parameters according to your needs. You can just override things by passing
# them on the command line, or in a wrapper script, e.g.
# ant=/some/other/dir/bin/ant .../full-build-linux.sh
# or by creating a file in this directory called build-site-local.sh with one name=value per line
# More things can be configured in ../user.build.properties; see ../build.properties for details.
#
# sources=/space/src/nb_all
# a full NB source checkout (cvs co standard ide xtest jemmy jellytools)
#
# nbjdk=/opt/java/j2se/1.3
# JDK 1.3 installation directory. (Full JDK, not just JRE.)
# YOU MUST TEST WITH 1.3 TO ENSURE YOU ARE NOT USING 1.4-SPECIFIC CALLS
#
# ant=/opt/ant-1.4.1/bin/ant
# Ant 1.4.1 installation directory. 1.5.x is not yet officially supported.
# YOU MUST TEST WITH 1.4.1 IF EDITING BUILD SCRIPTS
#
# doclean=no
# if set to "no", do not clean sources before starting (do an incremental build)
# default is "yes", so clean everything first
# YOU MUST DO A CLEAN BUILD BEFORE COMMITTING TO THE TRUNK
#
# testedmodule=full
# which tests to run after the build:
# "validate" - just validation tests (fastest, default)
# "full" - validation tests plus all stable developer tests (slower, but safest)
# a module, e.g. "openide" - validation tests plus all stable developer tests in that module
# "none" - do not run tests
# YOU MUST RUN AT LEAST THE VALIDATION TESTS BEFORE COMMITTING TO THE TRUNK
#
# spawndisplay=yes
# use a different X display if set to "yes" -
# often helpful, esp. for GUI tests which pop up a lot of windows
#
# nbclasspath=
# NB bundles all its own libs, so normally this can be left blank.
# If you want to build Javadoc (e.g. ../user.build.properties contains moduleconfig=stable-with-apisupport), use:
# $sources/libs/external/xalan-2.3.1.jar:$sources/core/external/xml-apis-1.0b2.jar:$sources/core/external/xerces-2.0.2.jar:.../ant14/lib/optional.jar
# and use a version of Ant 1.4.1 that does *not* include Crimson in its lib/ dir (it has a bug that interacts with Ant 1.4).
#
# mozbrowser=mozilla
# display test results automatically in Netscape or Mozilla
#
# unscramble=MAGICVALUEHERE
# If you are a Sun employee or otherwise got explicit permission, you may auto unscramble files;
# just insert the magic value (see http://nbbuild.netbeans.org/scrambler.html)


# --- Beginning of script. ---

sitelocal=$(dirname $0)/build-site-local.sh
if [ -f $sitelocal ]
then
    . $sitelocal
fi

if [ -z "$sources" ]
then
    sources=$(cd $(dirname $0)/../..; pwd)
fi

if [ -z "$nbjdk" ]
then
    nbjdk=$(cd $(dirname $(which java))/..; pwd)
fi

if [ -z "$ant" ]
then
    ant=ant
fi

if [ "$override" != yes ]
then
    if $nbjdk/bin/java -version 2>&1 | fgrep -q -v 1.3
    then
        echo "You need to set the variable 'nbjdk' to a JDK 1.3 installation" 1>&2
        exit 2
    fi
    if $ant -version 2>&1 | fgrep -q -v 1.4
    then
        echo "You need to set the variable 'ant' to an Ant 1.4.1 binary" 1>&2
        exit 2
    fi
fi

if [ -z "$spawndisplay" ]
then
    spawndisplay=no
fi

if [ -z "$testedmodule" ]
then
    testedmodule=validate
fi

if [ -z "$doclean" ]
then
    doclean=yes
fi

export JAVA_HOME=$nbjdk
export PATH=$nbjdk/bin:$PATH
export CLASSPATH=$nbclasspath

if [ -n "$unscramble" ]
then
    scramblerflag=-Dscrambler=$unscramble
fi

origdisplay=$DISPLAY
if [ $spawndisplay = yes ]
then
    # Use a separate display.
    display=:69
    xauthority=/tmp/.Xauthority-$display
    export XAUTHORITY=$xauthority
    Xnest -kb -name 'NetBeans test display' $display &
    xpid=$!
    xauth generate $display .
    export DISPLAY=$display
    sleep 3 # give X time to start
    twmrc=/tmp/.twmrc-$display
    echo 'RandomPlacement' > $twmrc
    twm -f $twmrc &
    twmpid=$!
    trap "rm $xauthority; kill $xpid; rm $twmrc; kill $twmpid" EXIT
    sleep 2 # give WM time to work
    xmessage -timeout 3 'Testing X server...minimize this nested display window if you want.'
    status=$?
    if [ $status != 0 ]
    then
        echo "Sample X client failed with status $status!" 1>&2
        exit 2
    fi
fi

if [ $doclean = yes ]
then
    cleantarget=real-clean
fi
echo "Building and trying NetBeans..." 1>&2
nice $ant -emacs -f $sources/nbbuild/build.xml $scramblerflag $cleantarget nozip-check
status=$?
if [ $status != 0 ]
then
    echo "NetBeans build failed with status $status!" 1>&2
    exit 1
fi

function browse() {
    if [ -n "$mozbrowser" ]
    then
        DISPLAY=$origdisplay $mozbrowser -remote "openURL(file://$1,new-window)"
    fi
}

if [ $testedmodule != none ]
then
    if [ $doclean = yes ]
    then
        testcleantarget=cleantests
    fi
    echo "Running validation tests" 1>&2
    nice $ant -emacs -f $sources/nbbuild/build.xml $scramblerflag commitValidation
    browse $sources/xtest/instance/results/index.html
    if [ $testedmodule = full ]
    then
        # Run full test suite.
        nice $ant -emacs -f $sources/xtest/instance/build.xml $scramblerflag $testcleantarget runtests
        browse $sources/xtest/instance/results/index.html
    elif [ $testedmodule != validate ]
    then
        # Run full suite for one module.
        nice $ant -emacs -f $sources/$testedmodule/test/build.xml $scramblerflag $testcleantarget runtests
        browse $sources/$testedmodule/test/results/index.html
    fi
fi
