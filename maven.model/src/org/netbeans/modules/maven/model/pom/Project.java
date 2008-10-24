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

import java.util.*;

/**
 *
 * @author mkleint
 */
public interface Project extends POMComponent {

    // attribute properties
    // child element properties
    public static final String PARENT_PROPERTY = "parent"; // NOI18N
    public static final String PREREQUISITES_PROPERTY = "prerequisites"; // NOI18N
    public static final String ISSUEMANAGEMENT_PROPERTY = "issueManagement"; // NOI18N
    public static final String CIMANAGEMENT_PROPERTY = "ciManagement"; // NOI18N
    public static final String MAILINGLIST_PROPERTY = "mailingList"; // NOI18N
    public static final String DEVELOPER_PROPERTY = "developer"; // NOI18N
    public static final String CONTRIBUTOR_PROPERTY = "contributor"; // NOI18N
    public static final String LICENSE_PROPERTY = "license"; // NOI18N
    public static final String SCM_PROPERTY = "scm"; // NOI18N
    public static final String ORGANIZATION_PROPERTY = "organization"; // NOI18N
    public static final String BUILD_PROPERTY = "build"; // NOI18N
    public static final String PROFILE_PROPERTY = "profile"; // NOI18N
    public static final String MODULE_PROPERTY = "module"; // NOI18N
    public static final String REPOSITORY_PROPERTY = "repository"; // NOI18N
    public static final String PLUGINREPOSITORY_PROPERTY = "pluginRepository"; // NOI18N
    public static final String DEPENDENCY_PROPERTY = "dependency"; // NOI18N
    public static final String REPORTING_PROPERTY = "reporting"; // NOI18N
    public static final String DEPENDENCYMANAGEMENT_PROPERTY = "dependencyManagement"; // NOI18N
    public static final String DISTRIBUTIONMANAGEMENT_PROPERTY = "distributionManagement"; // NOI18N

    public Parent getParent();
    public void setParent(Parent parent);

    public Prerequisites getPrerequisites();
    public void setPrerequisites(Prerequisites prerequisites);

    public IssueManagement getIssueManagement();
    public void setIssueManagement(IssueManagement issueManagement);

    public CiManagement getCiManagement();
    public void setCiManagement(CiManagement ciManagement);

    public List<MailingList> getMailingLists();
    public void addMailingList(MailingList mailingList);
    public void removeMailingList(MailingList mailingList);

    public List<Developer> getDevelopers();
    public void addDeveloper(Developer developer);
    public void removeDeveloper(Developer developer);

    public List<Contributor> getContributors();
    public void addContributor(Contributor contributor);
    public void removeContributor(Contributor contributor);

    public List<License> getLicenses();
    public void addLicense(License license);
    public void removeLicense(License license);

    public Scm getScm();
    public void setScm(Scm scm);

    public Organization getOrganization();
    public void setOrganization(Organization organization);

    public Build getBuild();
    public void setBuild(Build build);

    public List<Profile> getProfiles();
    public void addProfile(Profile profile);
    public void removeProfile(Profile profile);

    public List<Module> getModules();
    public void addModule(Module module);
    public void removeModule(Module module);

    public List<Repository> getRepositorys();
    public void addRepository(Repository repository);
    public void removeRepository(Repository repository);

    public List<PluginRepository> getPluginRepositorys();
    public void addPluginRepository(PluginRepository pluginRepository);
    public void removePluginRepository(PluginRepository pluginRepository);

    public List<Dependency> getDependencys();
    public void addDependency(Dependency dependency);
    public void removeDependency(Dependency dependency);

    public Reporting getReporting();
    public void setReporting(Reporting reporting);

    public DependencyManagement getDependencyManagement();
    public void setDependencyManagement(DependencyManagement dependencyManagement);

    public DistributionManagement getDistributionManagement();
    public void setDistributionManagement(DistributionManagement distributionManagement);

}