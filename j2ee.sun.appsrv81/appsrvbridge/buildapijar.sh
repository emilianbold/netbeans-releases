#!/bin/sh
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
#
# this script collects the content for the appservapis-w.x.y.z.jar that is used to
# support compilation of the GlassFish server integration plugin.
#
# this has only been tested on solaris 10.
#
# find out what classes we need to go look for
#
oldcwd=`pwd`
cd $1
cd serverplugins/sun
find appsrv/src appsrv81/src appsrv81/appsrvbridge/src sunddui/src sunddapi/src -name '*.java' -exec egrep '^import .*\;$' {} \; | awk '{ print $1 " " $2 }' | sort | uniq | egrep -v 'openide' | egrep -v 'w3c' | egrep -v ' org.\netbeans' | egrep -v ' org\.xml' | egrep -v ' javax' | egrep -v ' java\.' | awk '{ print $2 }' > /tmp/classeslist

# Now convert the package names into paths
#
sed 's|\.|/|g' /tmp/classeslist  | sed 's/\;$/\.class/g' > /tmp/classeslist2

#
# now add some classes to the classlist, since they weren't imported....
#
cat >> /tmp/classeslist2 << EOF1
com/sun/jdo/api/persistence/mapping/ejb/beans/FetchedWith.class
com/sun/appserv/management/util/jmx/MBeanServerConnection_Hook\$Hook.class
com/sun/appserv/management/util/jmx/MBeanServerConnection_Hook\$HookImpl.class
com/sun/jdo/api/persistence/model/jdo/PersistenceElement.class
com/sun/appserv/management/config/ResourceRefConfigCR.class
com/sun/appserv/management/config/TemplateResolver.class
com/sun/appserv/management/config/NamedConfigElement.class
com/sun/appserv/management/config/SystemPropertiesAccess.class
com/sun/appserv/management/config/PropertiesAccess.class
com/sun/appserv/management/base/Utility.class
com/sun/appserv/management/j2ee/ConfigProvider.class
com/sun/appserv/management/base/Container.class
com/sun/jdo/api/persistence/model/mapping/MappingElement.class
com/sun/jdo/api/persistence/model/mapping/MappingMemberElement.class
com/sun/jdo/api/persistence/model/jdo/PersistenceMemberElement.class
com/sun/enterprise/config/ConfigBean.class
com/sun/appserv/management/base/DottedNames.class
com/sun/jdo/spi/persistence/generator/database/DatabaseGenerator\$Results.class
com/sun/jdo/spi/persistence/generator/database/DatabaseGenerator\$NameTuple.class
com/sun/jdo/api/persistence/model/jdo/PersistenceMemberElement\$Impl.class
com/sun/jdo/api/persistence/model/mapping/impl/MappingMemberElementImpl.class
com/sun/enterprise/deployapi/SunTarget.class
com/sun/enterprise/config/ConfigBeanBase.class
com/sun/enterprise/config/serverbeans/Configs.class
com/sun/enterprise/config/serverbeans/HttpService.class
com/sun/enterprise/deployment/deploy/spi/DeploymentManager.class
com/sun/appserv/management/client/ConnectionSource.class
com/sun/appserv/management/base/AMXMBeanLogging.class
com/sun/appserv/management/config/AMXConfig.class
com/sun/appserv/management/base/Singleton.class
com/sun/appserv/management/config/ConfigElement.class
com/sun/appserv/management/config/Description.class
com/sun/appserv/management/config/ConfigRemover.class
com/sun/appserv/management/config/DeployedItemRefConfigCR.class
com/sun/appserv/management/j2ee/J2EELogicalServer.class
com/sun/appserv/management/config/ObjectType.class
com/sun/jdo/api/persistence/model/mapping/impl/MappingElementImpl.class
com/sun/appserv/management/j2ee/StateManageable.class
com/sun/appserv/management/config/ResourceRefConfigReferent.class
com/sun/appserv/management/config/DeployedItemRefConfigReferent.class
com/sun/appserv/management/config/Libraries.class
com/sun/appserv/management/config/JavaWebStart.class
com/sun/appserv/management/config/WebServiceEndpointConfigCR.class
com/sun/appserv/management/config/RefConfig.class
com/sun/jdo/api/persistence/mapping/ejb/beans/SecondaryTable.class
com/sun/jdo/api/persistence/mapping/ejb/beans/Consistency.class
com/sun/jdo/api/persistence/mapping/ejb/beans/CheckVersionOfAccessedInstances.class
com/sun/jdo/api/persistence/model/jdo/impl/PersistenceMemberElementImpl.class
com/sun/jdo/api/persistence/model/jdo/PersistenceFieldElement\$Impl.class
com/sun/jdo/api/persistence/model/jdo/PersistenceElement\$Impl.class
com/sun/appserv/management/base/AllDottedNames.class
com/sun/appserv/management/config/RefConfigReferent.class
com/sun/jdo/api/persistence/model/jdo/impl/PersistenceElementImpl.class
com/sun/jdo/api/persistence/model/jdo/RelationshipElement\$Impl.class
EOF1
cd $oldcwd
asinst=$2

#
# make the jar list file
#
cat > /tmp/jarlist1 << EOF
/lib/appserv-admin.jar
/lib/appserv-ext.jar
/lib/appserv-rt.jar
/lib/appserv-cmp.jar
/lib/commons-logging.jar
/lib/admin-cli.jar
/lib/common-laucher.jar
/lib/j2ee.jar
/lib/install/applications/jmsra/imqjmsra.jar
/lib/dom.jar
/lib/xalan.jar
/lib/jaxrpc-api.jar /lib/jaxrpc-impl.jar
/lib/appserv-deployment-client.jar
EOF

# this is terribly bad, but deserves no more engineering time.
#
for classname 
in `cat /tmp/classeslist2` 
do
    for jarfile 
    in `cat /tmp/jarlist1`
    do
        completejar=$asinst/$jarfile
        if [ -f $completejar ]
        then jar xvf $completejar $classname
        fi
    done
done


