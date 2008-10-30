/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.model.pom;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author mkleint
 */
public final class POMQNames {
    
    public final POMQName PROJECT; // NOI18N
    public final POMQName PARENT; // NOI18N
    public final POMQName ORGANIZATION; // NOI18N
    public final POMQName DISTRIBUTIONMANAGEMENT; // NOI18N
    public final POMQName SITE; // NOI18N
    public final POMQName DIST_REPOSITORY; // NOI18N
    public final POMQName DIST_SNAPSHOTREPOSITORY; // NOI18N
    public final POMQName PREREQUISITES; // NOI18N
    public final POMQName CONTRIBUTOR; // NOI18N
    public final POMQName SCM; // NOI18N
    public final POMQName ISSUEMANAGEMENT; // NOI18N
    public final POMQName CIMANAGEMENT; // NOI18N
    public final POMQName NOTIFIER; // NOI18N
    public final POMQName REPOSITORY; // NOI18N
    public final POMQName PLUGINREPOSITORY; // NOI18N
    public final POMQName RELEASES; // NOI18N
    public final POMQName SNAPSHOTS; // NOI18N
    public final POMQName PROFILE; // NOI18N
    public final POMQName PLUGIN; // NOI18N
    public final POMQName DEPENDENCY; // NOI18N
    public final POMQName EXCLUSION; // NOI18N
    public final POMQName EXECUTION; // NOI18N
    public final POMQName RESOURCE; // NOI18N
    public final POMQName TESTRESOURCE; // NOI18N
    public final POMQName PLUGINMANAGEMENT; // NOI18N
    public final POMQName REPORTING; // NOI18N
    public final POMQName REPORTPLUGIN; // NOI18N
    public final POMQName REPORTSET; // NOI18N
    public final POMQName ACTIVATION; // NOI18N
    public final POMQName ACTIVATIONPROPERTY; // NOI18N
    public final POMQName ACTIVATIONOS; // NOI18N
    public final POMQName ACTIVATIONFILE; // NOI18N
    public final POMQName ACTIVATIONCUSTOM; // NOI18N
    public final POMQName DEPENDENCYMANAGEMENT; // NOI18N
    public final POMQName BUILD; // NOI18N
    public final POMQName EXTENSION; // NOI18N
    public final POMQName LICENSE; // NOI18N
    public final POMQName MAILINGLIST; // NOI18N
    public final POMQName DEVELOPER; // NOI18N

    public final POMQName MAILINGLISTS; // NOI18N
    public final POMQName DEPENDENCIES; // NOI18N
    public final POMQName DEVELOPERS; // NOI18N
    public final POMQName CONTRIBUTORS; // NOI18N
    public final POMQName LICENSES; // NOI18N
    public final POMQName PROFILES; // NOI18N
    public final POMQName REPOSITORIES; // NOI18N
    public final POMQName PLUGINREPOSITORIES; // NOI18N
    public final POMQName EXCLUSIONS; // NOI18N
    public final POMQName EXECUTIONS; // NOI18N
    public final POMQName PLUGINS; // NOI18N
    public final POMQName EXTENSIONS; // NOI18N
    public final POMQName RESOURCES; // NOI18N
    public final POMQName TESTRESOURCES; // NOI18N
    public final POMQName REPORTPLUGINS; // NOI18N
    public final POMQName REPORTSETS; // NOI18N


    public final POMQName ID; //NOI18N
    public final POMQName GROUPID; //NOI18N
    public final POMQName ARTIFACTID; //NOI18N
    public final POMQName VERSION; //NOI18N
    public final POMQName CONFIGURATION; //NOI18N
    public final POMQName PROPERTIES; //NOI18N

    public final POMQName RELATIVEPATH; //NOI18N

    public final POMQName MODELVERSION; //NOI18N
    public final POMQName PACKAGING; //NOI18N
    public final POMQName URL; //NOI18N
    public final POMQName NAME; //NOI18N
    public final POMQName DESCRIPTION; //NOI18N
    public final POMQName INCEPTIONYEAR; //NOI18N

    public final POMQName TYPE; //NOI18N
    public final POMQName CLASSIFIER; //NOI18N
    public final POMQName SCOPE; //NOI18N
    public final POMQName SYSTEMPATH; //NOI18N
    public final POMQName OPTIONAL; //NOI18N

    public final POMQName INHERITED; //NOI18N
    public final POMQName PHASE; //NOI18N

    public final POMQName CIMANAG_SYSTEM; //NOI18N

    public final POMQName DIRECTORY; //NOI18N
    public final POMQName DEFAULTGOAL; //NOI18N
    public final POMQName FINALNAME; //NOI18N

    public final POMQName SOURCEDIRECTORY; //NOI18N
    public final POMQName SCRIPTSOURCEDIRECTORY; //NOI18N
    public final POMQName TESTSOURCEDIRECTORY; //NOI18N
    public final POMQName OUTPUTDIRECTORY; //NOI18N
    public final POMQName TESTOUTPUTDIRECTORY; //NOI18N

    public final POMQName EXCLUDEDEFAULTS; //NOI18N

    public final POMQName VALUE; //NOI18N

    public final POMQName LAYOUT; //NOI18N

    public final POMQName GOALS; //NOI18N
    public final POMQName GOAL; //NOI18N

    public final POMQName MODULES; //NOI18N
    public final POMQName MODULE; //NOI18N
    
    public POMQNames(boolean ns) {
        PROJECT = new POMQName(POMQName.createQName("project"), ns); // NOI18N
        PARENT = new POMQName(POMQName.createQName("parent"), ns); // NOI18N
        ORGANIZATION = new POMQName(POMQName.createQName("organization"), ns); // NOI18N
        DISTRIBUTIONMANAGEMENT = new POMQName(POMQName.createQName("distributionManagement"), ns); // NOI18N
        SITE = new POMQName(POMQName.createQName("site"), ns); // NOI18N
        DIST_REPOSITORY = new POMQName(POMQName.createQName("repository"), ns); // NOI18N
        DIST_SNAPSHOTREPOSITORY = new POMQName(POMQName.createQName("snapshotRepository"), ns); // NOI18N
        PREREQUISITES = new POMQName(POMQName.createQName("prerequisites"), ns); // NOI18N
        CONTRIBUTOR = new POMQName(POMQName.createQName("contributor"), ns); // NOI18N
        SCM = new POMQName(POMQName.createQName("scm"), ns); // NOI18N
        ISSUEMANAGEMENT = new POMQName(POMQName.createQName("issueManagement"), ns); // NOI18N
        CIMANAGEMENT = new POMQName(POMQName.createQName("ciManagement"), ns); // NOI18N
        NOTIFIER = new POMQName(POMQName.createQName("notifier"), ns); // NOI18N
        REPOSITORY = new POMQName(POMQName.createQName("repository"), ns); // NOI18N
        PLUGINREPOSITORY = new POMQName(POMQName.createQName("pluginRepository"), ns); // NOI18N
        RELEASES = new POMQName(POMQName.createQName("releases"), ns); // NOI18N
        SNAPSHOTS = new POMQName(POMQName.createQName("snapshots"), ns); // NOI18N
        PROFILE = new POMQName(POMQName.createQName("profile"), ns); // NOI18N
        PLUGIN = new POMQName(POMQName.createQName("plugin"), ns); // NOI18N
        DEPENDENCY = new POMQName(POMQName.createQName("dependency"), ns); // NOI18N
        EXCLUSION = new POMQName(POMQName.createQName("exclusion"), ns); // NOI18N
        EXECUTION = new POMQName(POMQName.createQName("execution"), ns); // NOI18N
        RESOURCE = new POMQName(POMQName.createQName("resource"), ns); // NOI18N
        TESTRESOURCE = new POMQName(POMQName.createQName("testResource"), ns); // NOI18N
        PLUGINMANAGEMENT = new POMQName(POMQName.createQName("pluginManagement"), ns); // NOI18N
        REPORTING = new POMQName(POMQName.createQName("reporting"), ns); // NOI18N
        REPORTPLUGIN = new POMQName(POMQName.createQName("reportPlugin"), ns); // NOI18N
        REPORTSET = new POMQName(POMQName.createQName("reportSet"), ns); // NOI18N
        ACTIVATION = new POMQName(POMQName.createQName("activation"), ns); // NOI18N
        ACTIVATIONPROPERTY = new POMQName(POMQName.createQName("activationProperty"), ns); // NOI18N
        ACTIVATIONOS = new POMQName(POMQName.createQName("activationOS"), ns); // NOI18N
        ACTIVATIONFILE = new POMQName(POMQName.createQName("activationFile"), ns); // NOI18N
        ACTIVATIONCUSTOM = new POMQName(POMQName.createQName("activationCustom"), ns); // NOI18N
        DEPENDENCYMANAGEMENT = new POMQName(POMQName.createQName("dependencyManagement"), ns); // NOI18N
        BUILD = new POMQName(POMQName.createQName("build"), ns); // NOI18N
        EXTENSION = new POMQName(POMQName.createQName("extension"), ns); // NOI18N
        LICENSE = new POMQName(POMQName.createQName("license"), ns); // NOI18N
        MAILINGLIST = new POMQName(POMQName.createQName("mailingList"), ns); // NOI18N
        DEVELOPER = new POMQName(POMQName.createQName("developer"), ns); // NOI18N

        MAILINGLISTS = new POMQName(POMQName.createQName("mailingLists"), ns); // NOI18N
        DEPENDENCIES = new POMQName(POMQName.createQName("dependencies"), ns); // NOI18N
        DEVELOPERS = new POMQName(POMQName.createQName("developers"), ns); // NOI18N
        CONTRIBUTORS = new POMQName(POMQName.createQName("contributors"), ns); // NOI18N
        LICENSES = new POMQName(POMQName.createQName("licenses"), ns); // NOI18N
        PROFILES = new POMQName(POMQName.createQName("profiles"), ns); // NOI18N
        REPOSITORIES = new POMQName(POMQName.createQName("repositories"), ns); // NOI18N
        PLUGINREPOSITORIES = new POMQName(POMQName.createQName("pluginRepositories"), ns); // NOI18N
        EXCLUSIONS = new POMQName(POMQName.createQName("exclusions"), ns); // NOI18N
        EXECUTIONS = new POMQName(POMQName.createQName("executions"), ns); // NOI18N
        PLUGINS = new POMQName(POMQName.createQName("plugins"), ns); // NOI18N
        EXTENSIONS = new POMQName(POMQName.createQName("extensions"), ns); // NOI18N
        RESOURCES = new POMQName(POMQName.createQName("resources"), ns); // NOI18N
        TESTRESOURCES = new POMQName(POMQName.createQName("testResources"), ns); // NOI18N
        REPORTPLUGINS = new POMQName(POMQName.createQName("reportPlugins"), ns); // NOI18N
        REPORTSETS = new POMQName(POMQName.createQName("reportSets"), ns); // NOI18N


        ID = new POMQName(POMQName.createQName("id"), ns); //NOI18N
        GROUPID = new POMQName(POMQName.createQName("groupId"), ns); //NOI18N
        ARTIFACTID = new POMQName(POMQName.createQName("artifactId"), ns); //NOI18N
        VERSION = new POMQName(POMQName.createQName("version"), ns); //NOI18N
        CONFIGURATION = new POMQName(POMQName.createQName("configuration"), ns); //NOI18N
        PROPERTIES = new POMQName(POMQName.createQName("properties"), ns); //NOI18N

        RELATIVEPATH = new POMQName(POMQName.createQName("relativePath"), ns); //NOI18N

        MODELVERSION = new POMQName(POMQName.createQName("modelVersion"), ns); //NOI18N
        PACKAGING = new POMQName(POMQName.createQName("packaging"), ns); //NOI18N
        URL = new POMQName(POMQName.createQName("url"), ns); //NOI18N
        NAME = new POMQName(POMQName.createQName("name"), ns); //NOI18N
        DESCRIPTION = new POMQName(POMQName.createQName("description"), ns); //NOI18N
        INCEPTIONYEAR = new POMQName(POMQName.createQName("inceptionYear"), ns); //NOI18N

        TYPE = new POMQName(POMQName.createQName("type"), ns); //NOI18N
        CLASSIFIER = new POMQName(POMQName.createQName("classifier"), ns); //NOI18N
        SCOPE = new POMQName(POMQName.createQName("scope"), ns); //NOI18N
        SYSTEMPATH = new POMQName(POMQName.createQName("systemPath"), ns); //NOI18N
        OPTIONAL = new POMQName(POMQName.createQName("optional"), ns); //NOI18N

        INHERITED = new POMQName(POMQName.createQName("inherited"), ns); //NOI18N
        PHASE = new POMQName(POMQName.createQName("phase"), ns); //NOI18N

        CIMANAG_SYSTEM = new POMQName(POMQName.createQName("system"), ns); //NOI18N

        DIRECTORY = new POMQName(POMQName.createQName("directory"), ns); //NOI18N
        DEFAULTGOAL = new POMQName(POMQName.createQName("defaultGoal"), ns); //NOI18N
        FINALNAME = new POMQName(POMQName.createQName("finalName"), ns); //NOI18N

        SOURCEDIRECTORY = new POMQName(POMQName.createQName("sourceDirectory"), ns); //NOI18N
        SCRIPTSOURCEDIRECTORY = new POMQName(POMQName.createQName("scriptSourceDirectory"), ns); //NOI18N
        TESTSOURCEDIRECTORY = new POMQName(POMQName.createQName("testSourceDirectory"), ns); //NOI18N
        OUTPUTDIRECTORY = new POMQName(POMQName.createQName("outputDirectory"), ns); //NOI18N
        TESTOUTPUTDIRECTORY = new POMQName(POMQName.createQName("testOutputDirectory"), ns); //NOI18N

        EXCLUDEDEFAULTS = new POMQName(POMQName.createQName("excludeDefaults"), ns); //NOI18N

        VALUE = new POMQName(POMQName.createQName("value"), ns); //NOI18N

        LAYOUT = new POMQName(POMQName.createQName("layout"), ns); //NOI18N

        GOALS = new POMQName(POMQName.createQName("goals"), ns); //NOI18N
        GOAL = new POMQName(POMQName.createQName("goal"), ns); //NOI18N

        MODULES = new POMQName(POMQName.createQName("modules"), ns); //NOI18N
        MODULE = new POMQName(POMQName.createQName("module"), ns); //NOI18N

        //when adding items here, need to add them to the set below as well.

    }

    public Set<QName> getElementQNames() {
        QName[] names = new QName[] {
            PROJECT.getQName(),
            PARENT.getQName(),
            ORGANIZATION.getQName(),
            DISTRIBUTIONMANAGEMENT.getQName(),
            SITE.getQName(),
            DIST_REPOSITORY.getQName(),
            DIST_SNAPSHOTREPOSITORY.getQName(),
            PREREQUISITES.getQName(),
            CONTRIBUTOR.getQName(),
            SCM.getQName(),
            ISSUEMANAGEMENT.getQName(),
            CIMANAGEMENT.getQName(),
            NOTIFIER.getQName(),
            REPOSITORY.getQName(),
            PLUGINREPOSITORY.getQName(),
            RELEASES.getQName(),
            SNAPSHOTS.getQName(),
            PROFILE.getQName(),
            PLUGIN.getQName(),
            DEPENDENCY.getQName(),
            EXCLUSION.getQName(),
            EXECUTION.getQName(),
            RESOURCE.getQName(),
            TESTRESOURCE.getQName(),
            PLUGINMANAGEMENT.getQName(),
            REPORTING.getQName(),
            REPORTPLUGIN.getQName(),
            REPORTSET.getQName(),
            ACTIVATION.getQName(),
            ACTIVATIONPROPERTY.getQName(),
            ACTIVATIONOS.getQName(),
            ACTIVATIONFILE.getQName(),
            ACTIVATIONCUSTOM.getQName(),
            DEPENDENCYMANAGEMENT.getQName(),
            BUILD.getQName(),
            EXTENSION.getQName(),
            LICENSE.getQName(),
            MAILINGLIST.getQName(),
            DEVELOPER.getQName(),
            MAILINGLISTS.getQName(),
            DEPENDENCIES.getQName(),
            DEVELOPERS.getQName(),
            CONTRIBUTORS.getQName(),
            LICENSES.getQName(),
            PROFILES.getQName(),
            REPOSITORIES.getQName(),
            PLUGINREPOSITORIES.getQName(),
            EXCLUSIONS.getQName(),
            EXECUTIONS.getQName(),
            PLUGINS.getQName(),
            EXTENSIONS.getQName(),
            RESOURCES.getQName(),
            TESTRESOURCES.getQName(),
            REPORTPLUGINS.getQName(),
            REPORTSETS.getQName(),
            ID.getQName(),
            GROUPID.getQName(),
            ARTIFACTID.getQName(),
            VERSION.getQName(),
            CONFIGURATION.getQName(),
            PROPERTIES.getQName(),
            RELATIVEPATH.getQName(),
            MODELVERSION.getQName(),
            PACKAGING.getQName(),
            URL.getQName(),
            NAME.getQName(),
            DESCRIPTION.getQName(),
            INCEPTIONYEAR.getQName(),
            TYPE.getQName(),
            CLASSIFIER.getQName(),
            SCOPE.getQName(),
            SYSTEMPATH.getQName(),
            OPTIONAL.getQName(),
            INHERITED.getQName(),
            PHASE.getQName(),
            CIMANAG_SYSTEM.getQName(),
            DIRECTORY.getQName(),
            DEFAULTGOAL.getQName(),
            FINALNAME.getQName(),
            SOURCEDIRECTORY.getQName(),
            SCRIPTSOURCEDIRECTORY.getQName(),
            TESTSOURCEDIRECTORY.getQName(),
            OUTPUTDIRECTORY.getQName(),
            TESTOUTPUTDIRECTORY.getQName(),
            EXCLUDEDEFAULTS.getQName(),
            VALUE.getQName(),
            LAYOUT.getQName(),
            GOALS.getQName(),
            GOAL.getQName(),
            MODULES.getQName(),
            MODULE.getQName()
        };
        List<QName> list = Arrays.asList(names);
        return new HashSet<QName>(list);
    }
    
}
