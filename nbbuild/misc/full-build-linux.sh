#!/bin/sh
#                 Sun Public License Notice
# 
# The contents of this file are subject to the Sun Public License
# Version 1.0 (the "License"). You may not use this file except in
# compliance with the License. A copy of the License is available at
# http://www.sun.com/
# 
# The Original Code is NetBeans. The Initial Developer of the Original
# Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
# Microsystems, Inc. All Rights Reserved.

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
# nbjdk=/opt/java/j2se/1.4
# JDK 1.4 installation directory. (Full JDK, not just JRE.)
#
# nbtestjdk=/opt/java/j2se/1.4
# JDK installation directory for use when running (but not building!) test suites.
# By default, same as nbjdk. However you may wish to run tests with a
# different VM.
#
# ant=/opt/ant-1.4.1/bin/ant
# Ant 1.4.1 installation directory. 1.5.x is not yet officially supported.
# If you want to build Javadoc (e.g. ../user.build.properties contains moduleconfig=stable-with-apisupport),
# use a version of Ant 1.4.1 that does *not* include Crimson in its lib/ dir (it has a bug that interacts with Ant 1.4).
# Anyway Crimson is bundled in JDK 1.4 so you must in that case set the boot classpath
# to include Xerces and use it. E.g.
# ant=/opt/ant-1.4.1/bin/ant-with-xerces
# ---%<--- /opt/ant-1.4.1/bin/ant-with-xerces
# #!/bin/sh
# export ANT_OPTS="-Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl $ANT_OPTS"
# export CLASSPATH=/space/src/nb_all/core/external/xml-apis-1.0b2.jar:/space/src/nb_all/core/external/xerces-2.0.2.jar
# exec /space/ant14/bin/ant "$@"
# ---%<---
# where /opt/ant-1.4.1/lib/ contains just ant.jar and optional.jar.
# YOU MUST TEST WITH 1.4.1 IF EDITING BUILD SCRIPTS
#
# testant=/opt/ant-1.4.1/bin/ant
# Ant 1.4.1 installation directory for use with XTest. 1.5.x is not yet officially supported.
# By default, same as ant.
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

if [ -z "$nbtestjdk" ]
then
    nbtestjdk=$nbjdk
fi

if [ -z "$ant" ]
then
    ant=ant
fi

if [ -z "$testant" ]
then
    testant="$ant"
fi

if [ "$override" != yes ]
then
    if $nbjdk/bin/java -version 2>&1 | fgrep -q -v 1.4
    then
        echo "You need to set the variable 'nbjdk' to a JDK 1.4 installation" 1>&2
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

antcmd="nice $ant -emacs $scramblerflag"

if [ $doclean = yes ]
then
    cleantarget=real-clean
fi
echo "----------BUILDING NETBEANS----------" 1>&2
# Intentionally skipping check-commit-validation.
# Running sanity-start just so you have a good chance to see deprecation messages etc.
$antcmd -f $sources/nbbuild/build.xml $cleantarget nozip-check
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
    else
        echo "---- SEE RESULTS: $1 ----"
    fi
}

if [ $testedmodule != none ]
then
    testantcmd="nice $testant -emacs $scramblerflag -Djdkhome=$nbtestjdk"
    if [ $doclean = yes ]
    then
        echo "----------CLEANING AND BUILDING TESTS----------" 1>&2
        $testantcmd -f $sources/xtest/instance/build.xml -Dxtest.config=commit-validation-nb realclean
        if [ $testedmodule = full ]
        then
            $testantcmd -f $sources/xtest/instance/build.xml realclean
        elif [ $testedmodule != validate ]
        then
            $testantcmd -f $sources/$testedmodule/test/build.xml realclean
        fi
        $testantcmd -f $sources/xtest/instance/build.xml -Dxtest.config=commit-validation-nb buildtests
        if [ $testedmodule = full ]
        then
            $testantcmd -f $sources/xtest/instance/build.xml buildtests
        elif [ $testedmodule != validate ]
        then
            $testantcmd -f $sources/$testedmodule/test/build.xml buildtests
        fi
    fi
    echo "----------RUNNING TESTS----------" 1>&2
    # Always run validation suite.
    $testantcmd -f $sources/xtest/instance/build.xml -Dxtest.config=commit-validation-nb runtests
    if [ $testedmodule = validate ]
    then
        browse $sources/xtest/instance/results/index.html
    else
        # Don't let these be clobbered by subsequent tests!
        dir=/tmp/xtest-validation-suite-results-$USER
        rm -rf $dir
        cp -r $sources/xtest/instance/results $dir
        browse $dir/index.html
    fi
    if [ $testedmodule = full ]
    then
        # Run full developer test suite.
        $testantcmd -f $sources/xtest/instance/build.xml runtests
        browse $sources/xtest/instance/results/index.html
    elif [ $testedmodule != validate ]
    then
        # Run full suite for one module.
        $testantcmd -f $sources/$testedmodule/test/build.xml runtests
        browse $sources/$testedmodule/test/results/index.html
    fi
fi
