 # DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 #
 # Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 #
 # Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 # Other names may be trademarks of their respective owners.
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
 # nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 # particular file as subject to the "Classpath" exception as provided
 # by Oracle in the GPL Version 2 section of the License file that
 # accompanied this code. If applicable, add the following below the
 # License Header, with the fields enclosed by brackets [] replaced by
 # your own identifying information:
 # "Portions Copyrighted [year] [name of copyright owner]"
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
 #
 # Contributor(s):
 #
 # Portions Copyrighted 2012 Sun Microsystems, Inc.

set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

pack_component() 
{
    dist=$1
    base_name=$2
    component=$3
    filter=$4
    zip -q -r $dist/$base_name-$component.zip $filter
#    gtar cvzf $dist/targz/$base_name-$component.tar.gz $filter
#    gtar cvjf $dist/tarbz2/$base_name-$component.tar.bz2 $filter
}

###################################################################
#
# Pack all the components
#
###################################################################

pack_all_components()
{
    DIST_DIR=${1}
    NAME=${2}

    mkdir $DIST_DIR/zip/moduleclusters

    cd $NB_ALL/nbbuild

    #Pack the distributions
    if [ -z $JDEV_BUILD ]; then
	ant zip-cluster-config -Dcluster.config=full -Dzip.name=$DIST_DIR/zip/$NAME.zip || exit 1
        #ant zip-cluster-config -Dcluster.config=platform -Dzip.name=$DIST_DIR/zip/$NAME-platform.zip || exit 1
	ant zip-cluster-config -Dcluster.config=basic -Dzip.name=$DIST_DIR/zip/$NAME-javase.zip || exit 1
	ant zip-cluster-config -Dcluster.config=standard -Dzip.name=$DIST_DIR/zip/$NAME-javaee.zip || exit 1
	ant zip-cluster-config -Dcluster.config=php -Dzip.name=$DIST_DIR/zip/$NAME-php.zip || exit 1
	ant zip-cluster-config -Dcluster.config=cnd -Dzip.name=$DIST_DIR/zip/$NAME-cpp.zip || exit 1
    else
	ant zip-cluster-config -Dcluster.config=java -Dzip.name=$DIST_DIR/zip/$NAME.zip || exit 1
    fi

    cd $NB_ALL/nbbuild/netbeans

    #codename="org-netbeans-core-browser"
    #native="extra/modules/lib/native"
    #locale="extra/modules/locale"
    #config="extra/config/Modules"
    #update_tracking="extra/update_tracking"
    #modules="extra/modules"

    #pack_component $DIST_DIR/zip/moduleclusters $NAME extra-core-browser-linux       "$config/$codename-linux.xml      $update_tracking/$codename-linux.xml   $modules/$codename-linux.jar   $locale/$codename-linux_*.jar   $native/linux/xulrunner"
    #pack_component $DIST_DIR/zip/moduleclusters $NAME extra-core-browser-windows     "$config/$codename-win.xml        $update_tracking/$codename-win.xml     $modules/$codename-win.jar     $locale/$codename-win_*.jar     $native/win32/xulrunner"
    #pack_component $DIST_DIR/zip/moduleclusters $NAME extra-core-browser-solaris-x86 "$config/$codename-solaris.xml    $update_tracking/$codename-solaris.xml $modules/$codename-solaris.jar $locale/$codename-solaris_*.jar $native/solaris-x86/xulrunner"
    #pack_component $DIST_DIR/zip/moduleclusters $NAME extra-core-browser-macosx      "$config/$codename-macosx.xml     $update_tracking/$codename-macosx.xml  $modules/$codename-macosx.jar  $locale/$codename-macosx_*.jar  $native/macosx/xulrunner $native/macosx/libcocoautils.jnilib"

    rm -rf extra

    pack_component $DIST_DIR/zip/moduleclusters $NAME xml "xml*"
    rm -rf xml*

    pack_component $DIST_DIR/zip/moduleclusters $NAME javacard "javacard*"
    rm -rf javacard*

    cd $NB_ALL/nbbuild

    #Pack all the NetBeans
    pack_component $DIST_DIR/zip/moduleclusters $NAME all-in-one netbeans

    cd $NB_ALL/nbbuild/netbeans

    #Continue with individual component
    pack_component $DIST_DIR/zip/moduleclusters $NAME dlight "dlight*"
    rm -rf dlight*

    pack_component $DIST_DIR/zip/moduleclusters $NAME webcommon "webcommon*"
    rm -rf webcommon*

    pack_component $DIST_DIR/zip/moduleclusters $NAME groovy "groovy*"
    rm -rf groovy*

    pack_component $DIST_DIR/zip/moduleclusters $NAME php "php*"
    rm -rf php*

    pack_component $DIST_DIR/zip/moduleclusters $NAME profiler "profiler*"
    rm -rf profiler*

    pack_component $DIST_DIR/zip/moduleclusters $NAME platform "platform*"
    rm -rf platform*

    pack_component $DIST_DIR/zip/moduleclusters $NAME mobility "mobility*"
    rm -rf mobility*

    pack_component $DIST_DIR/zip/moduleclusters $NAME identity "identity*"
    rm -rf identity*

    pack_component $DIST_DIR/zip/moduleclusters $NAME ide "ide*"
    rm -rf ide*

    pack_component $DIST_DIR/zip/moduleclusters $NAME harness "harness*"
    rm -rf harness*

    pack_component $DIST_DIR/zip/moduleclusters $NAME enterprise "enterprise*"
    rm -rf enterprise*

    pack_component $DIST_DIR/zip/moduleclusters $NAME ergonomics "ergonomics*"
    rm -rf ergonomics*

    pack_component $DIST_DIR/zip/moduleclusters $NAME apisupport "apisupport*"
    rm -rf apisupport*

    pack_component $DIST_DIR/zip/moduleclusters $NAME java "java*"
    rm -rf java*

    pack_component $DIST_DIR/zip/moduleclusters $NAME cnd "cnd*"
    rm -rf cnd*

    pack_component $DIST_DIR/zip/moduleclusters $NAME python "python*"
    rm -rf python*
    rm -rf ruby*

    pack_component $DIST_DIR/zip/moduleclusters $NAME nb-etc "*"
}

pack_all_components $DIST $BASENAME
