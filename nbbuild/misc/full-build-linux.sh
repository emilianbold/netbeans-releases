#!/bin/bash
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html
# or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License file at
# nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
# particular file as subject to the "Classpath" exception as provided
# by Sun in the GPL Version 2 section of the License file that
# accompanied this code. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
# Microsystems, Inc. All Rights Reserved.
#
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 2, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 2] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 2 code and therefore, elected the GPL
# Version 2 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.

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
# a full NB source checkout
#
# nbjdk=/opt/java/j2se/1.5.0_15
# JDK 1.5.0_15 installation directory. (Full JDK, not just JRE.)
#
# nbtestjdk=/opt/java/j2se/1.5.0_15
# JDK installation directory for use when running (but not building!) test suites.
# By default, same as nbjdk. However you may wish to run tests with a
# different VM.
#
# ant=/opt/ant-1.7.0/bin/ant
# Ant 1.7.0 binary.
# You can also set useful build options, e.g.
# ant='.../ant -Dcluster.config=basic'
#
# testant=/opt/ant-1.7.0/bin/ant
# By default, same as ant.
#
# doclean=no
# if set to "no", do not clean sources before starting (do an incremental build)
# default is "yes", so clean everything first
# YOU MUST DO A CLEAN BUILD BEFORE COMMITTING TO THE TRUNK
#
# dobuild=no
# if set to "no", do not do a build, just run tests
# default is "yes", do a build (incl. commit verification)
# YOU MUST DO A CLEAN BUILD BEFORE COMMITTING TO THE TRUNK
#
# testedmodule=full
# which tests to run after the build:
# "validate" - just validation tests (fastest, default)
# "full" - validation tests plus all stable developer tests (slower, but safest)
# some module name, e.g. "core/palette" - validation tests plus all stable developer tests in that module
# "none" - do not run tests
# YOU MUST RUN AT LEAST THE VALIDATION TESTS BEFORE COMMITTING TO THE TRUNK
#
# spawndisplay=yes
# use a different X display if set to "yes" -
# often helpful, esp. for GUI tests which pop up a lot of windows
#
# spawndisplaytype=vnc
# may be "X", "Xnest", "vnc" (raw Xvnc), or "vncserver"; default "X"
# vnc seems stablest; sometimes other servers have bugs
#
# vncdisplayargs="-SecurityTypes=none"
# extra arguments to pass to VNC, depending on which version you run
#
# vncviewer="vncviewer -ViewOnly"
# command and arguments to run the VNC viewer.  For XOrg, use
# "xf4vncviewer -viewonly"
#
# spawnwm="mwm"
# the X window manager to use, such as mwm, metacity, etc.
#
# mozbrowser=mozilla
# display test results automatically in Netscape or Mozilla


# --- Beginning of script. ---

sitelocal=$(dirname $0)/build-site-local.sh
if [ -f $sitelocal ]
then
    . $sitelocal
else 
    echo "No build-site-local.sh file found, running with default settings." 1>&2
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
    if $nbjdk/bin/java -version 2>&1 | fgrep -q -v 1.5.0_15
    then
        echo "You need to set the variable 'nbjdk' to a JDK 1.5.0_15 installation" 1>&2
        exit 2
    fi
    if $ant -version 2>&1 | fgrep -q -v 1.7.0
    then
        echo "You need to set the variable 'ant' to an Ant 1.7.0 binary" 1>&2
        exit 2
    fi
fi

if [ -z "$spawndisplay" ]
then
    spawndisplay=no
fi

if [ -z "$spawndisplaytype" ]
then
    spawndisplaytype=X
fi

if [ -z "$vncdisplayargs" ]
then
    vncdisplayargs="-SecurityTypes=none"
fi

if [ -z "$vncviewer" ]
then
    vncviewer="vncviewer -ViewOnly"
fi

if [ -z "spawnwm" ]
then
    spawnwm="mwm"
fi

if [ -z "$testedmodule" ]
then
    testedmodule=validate
fi

if [ -z "$doclean" ]
then
    doclean=yes
fi

if [ -z "$dobuild" ]
then
    dobuild=yes
fi

export JAVA_HOME=$nbjdk
export PATH=$nbjdk/bin:$PATH
export CLASSPATH=

origdisplay=$DISPLAY
origxauthority=$XAUTHORITY
if [ $spawndisplay = yes -a $spawndisplaytype \!= vncserver ]
then
    # Use a separate display.
    display=:69
    xauthority=/tmp/.Xauthority-$display
    export XAUTHORITY=$xauthority
    if [ $spawndisplaytype = Xnest ]
    then
        Xnest -kb -name 'NetBeans test display' $display &
        xpid=$!
    elif [ $spawndisplaytype = X ]
    then
        X $display &
        xpid=$!
    elif [ $spawndisplaytype = vnc ]
    then
        Xvnc -localhost $vncdisplayargs -desktop 'NetBeans test display' -geometry 1024x768 -depth 16 $display -log '*:stderr:15' &
        xpid=$!
    else
        echo "strange \$spawndisplaytype: $spawndisplaytype" >&2
        exit 2
    fi
    xauth generate $display .
    export DISPLAY=$display
    sleep 2 # give X time to start
    $spawnwm &
    wmpid=$!
    trapcmd="kill $wmpid > /dev/null 2>&1; kill $xpid > /dev/null 2>&1; rm -f $xauthority"
    sleep 2 # give WM time to work
    if [ $spawndisplaytype = Xnest ]
    then
        message='Testing X server...minimize this nested display window if you want.'
    elif [ $spawndisplaytype = X ]
    then
        message='Testing X server...use Ctrl-Alt-F7/8 to toggle screens.'
    else
        message="Close/minimize this window if you want. [vncviewer $display]"
        XAUTHORITY=$origxauthority $vncviewer -display $origdisplay $display &
        vncviewerpid=$!
        trapcmd_vnc="kill $vncviewerpid > /dev/null 2>&1;"
    fi
    trap "$trapcmd_vnc $trapcmd" EXIT
    xmessage -timeout 3 "$message"
    status=$?
    if [ $status != 0 ]
    then
        echo "Sample X client failed with status $status! Try 'xhost +local:' if you disallow remote logins" 1>&2
        exit 2
    fi
elif [ $spawndisplay = yes -a $spawndisplaytype = vncserver ]
then
    display=:69
    vncserver $display -desktop 'NetBeans test display' -geometry 1024x768 -depth 16
    trap "vncserver -kill $display" EXIT
    vncviewer -passwd ~/.vnc/passwd $display &
    export DISPLAY=$display
    $spawnwm &
    xmessage -timeout 3 "Close/minimize this window if you want. [vncviewer $display]"
    status=$?
    if [ $status != 0 ]
    then
        echo "Sample X client failed with status $status!" 1>&2
        exit 2
    fi
fi

antcmd="nice $ant -emacs"

if [ $doclean = yes ]
then
    echo "----------CLEANING SOURCES----------" 1>&2
    $antcmd -f $sources/nbbuild/build.xml clean
fi

if [ $dobuild = yes ]
then
    echo "----------BUILDING NETBEANS----------" 1>&2
    # Intentionally skipping check-commit-validation.
    # Make sure to explicitly set JDK (ignore any definition in user.build.properties).
    $antcmd -f $sources/build.xml -Dnbjdk.home=$nbjdk
    status=$?
    if [ $status '!=' 0 ]
    then
        echo "NetBeans build failed with status $status!" 1>&2
        exit 1
    fi
fi

function browse() {
    if [ -n "$mozbrowser" ]
    then
        XAUTHORITY=$origxauthority DISPLAY=$origdisplay $mozbrowser -remote "openURL(file://$1,new-window)"
    else
        echo "---- SEE RESULTS: $1 ----"
    fi
}

if [ $testedmodule != none ]
then
    testantcmd="nice $testant -emacs -Djdkhome=$nbtestjdk"
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
    $testantcmd -f $sources/xtest/instance/build.xml -Dxtest.config=commit-validation-nb -Dxtest.fail.on.failure=true runtests
    validation_status=$?
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
    other_status=0
    if [ $testedmodule = full ]
    then
        # Run full developer test suite.
        $testantcmd -f $sources/xtest/instance/build.xml -Dxtest.fail.on.failure=true runtests
        other_status=$?
        browse $sources/xtest/instance/results/index.html
    elif [ $testedmodule != validate ]
    then
        # Run full suite for one module.
        $testantcmd -f $sources/$testedmodule/test/build.xml -Dxtest.fail.on.failure=true runtests
        # XXX failures do not seem to result in non-zero exit code here
        other_status=$?
        browse $sources/$testedmodule/test/results/index.html
    fi
    if [ $validation_status != 0 -o $other_status != 0 ]
    then
        echo "Some NetBeans tests failed!" 1>&2
        exit 1
    fi
fi
