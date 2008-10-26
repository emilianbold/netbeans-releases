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

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author mkleint
 */
public enum POMQName {    
    
    PROJECT(createQName("project")), // NOI18N
    PARENT(createQName("parent")), // NOI18N
    ORGANIZATION(createQName("organization")), // NOI18N
    DISTRIBUTIONMANAGEMENT(createQName("distributionManagement")), // NOI18N
    SITE(createQName("site")), // NOI18N
    DIST_REPOSITORY(createQName("repository")), // NOI18N
    DIST_SNAPSHOTREPOSITORY(createQName("snapshotRepository")), // NOI18N
    PREREQUISITES(createQName("prerequisites")), // NOI18N
    CONTRIBUTOR(createQName("contributor")), // NOI18N
    SCM(createQName("scm")), // NOI18N
    ISSUEMANAGEMENT(createQName("issueManagement")), // NOI18N
    CIMANAGEMENT(createQName("ciManagement")), // NOI18N
    NOTIFIER(createQName("notifier")), // NOI18N
    REPOSITORY(createQName("repository")), // NOI18N
    PLUGINREPOSITORY(createQName("pluginRepository")), // NOI18N
    RELEASE(createQName("release")), // NOI18N
    SNAPSHOT(createQName("snapshot")), // NOI18N
    PROFILE(createQName("profile")), // NOI18N
    BUILDBASE(createQName("buildBase")), // NOI18N
    PLUGIN(createQName("plugin")), // NOI18N
    DEPENDENCY(createQName("dependency")), // NOI18N
    EXCLUSION(createQName("exclusion")), // NOI18N
    EXECUTION(createQName("execution")), // NOI18N
    RESOURCE(createQName("resource")), // NOI18N
    TESTRESOURCE(createQName("testResource")), // NOI18N
    PLUGINMANAGEMENT(createQName("pluginManagement")), // NOI18N
    REPORTING(createQName("reporting")), // NOI18N
    REPORTPLUGIN(createQName("reportPlugin")), // NOI18N
    REPORTSET(createQName("reportSet")), // NOI18N
    ACTIVATION(createQName("activation")), // NOI18N
    ACTIVATIONPROPERTY(createQName("activationProperty")), // NOI18N
    ACTIVATIONOS(createQName("activationOS")), // NOI18N
    ACTIVATIONFILE(createQName("activationFile")), // NOI18N
    ACTIVATIONCUSTOM(createQName("activationCustom")), // NOI18N
    DEPENDENCYMANAGEMENT(createQName("dependencyManagement")), // NOI18N
    BUILD(createQName("build")), // NOI18N
    EXTENSION(createQName("extension")), // NOI18N
    LICENSE(createQName("license")), // NOI18N
    MAILINGLIST(createQName("mailingList")), // NOI18N
    DEVELOPER(createQName("developer")), // NOI18N

    MAILINGLISTS(createQName("mailingLists")), // NOI18N
    DEPENDENCIES(createQName("dependencies")), // NOI18N
    DEVELOPERS(createQName("developers")), // NOI18N
    CONTRIBUTORS(createQName("contributors")), // NOI18N
    LICENSES(createQName("licenses")), // NOI18N
    PROFILES(createQName("profiles")), // NOI18N
    REPOSITORIES(createQName("repositories")), // NOI18N
    PLUGINREPOSITORIES(createQName("pluginRepositories")), // NOI18N
    EXCLUSIONS(createQName("exclusions")), // NOI18N
    EXECUTIONS(createQName("executions")), // NOI18N
    PLUGINS(createQName("plugins")), // NOI18N
    EXTENSIONS(createQName("extensions")), // NOI18N
    RESOURCES(createQName("resources")), // NOI18N
    TESTRESOURCES(createQName("testResources")), // NOI18N


    ID(createQName("id")), //NOI18N
    GROUPID(createQName("groupId")), //NOI18N
    ARTIFACTID(createQName("artifactId")), //NOI18N
    VERSION(createQName("version")), //NOI18N
    CONFIGURATION(createQName("configuration")), //NOI18N

    RELATIVEPATH(createQName("relativePath")), //NOI18N

    MODELVERSION(createQName("modelVersion")), //NOI18N
    PACKAGING(createQName("packaging")), //NOI18N
    URL(createQName("url")), //NOI18N
    NAME(createQName("name")), //NOI18N
    DESCRIPTION(createQName("description")), //NOI18N
    INCEPTIONYEAR(createQName("inceptionYear")), //NOI18N

    TYPE(createQName("type")), //NOI18N
    CLASSIFIER(createQName("classifier")), //NOI18N
    SCOPE(createQName("scope")), //NOI18N
    SYSTEMPATH(createQName("systemPath")), //NOI18N
    OPTIONAL(createQName("optional")), //NOI18N

    INHERITED(createQName("inherited")), //NOI18N
    PHASE(createQName("phase")), //NOI18N
    ;
    
    public static final String NS_URI = "http://maven.apache.org/POM/4.0.0";  // NOI18N
    public static final String NS_PREFIX = "pom";   // NOI18N        
    
    public static QName createQName(String localName){
        return new QName(NS_URI, localName, NS_PREFIX);
    }
    
    private POMQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }

    public String getName() {
        return qName.getLocalPart();
    }
    
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (POMQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }    
    
    public String getQualifiedName() {
        return qName.getPrefix() + ":" + qName.getLocalPart();      // NOI18N
    }
    
    private final QName qName;
}
