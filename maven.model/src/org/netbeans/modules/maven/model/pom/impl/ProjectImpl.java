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
package org.netbeans.modules.maven.model.pom.impl;

import java.util.*;
import org.w3c.dom.Element;
import org.netbeans.modules.maven.model.pom.*;	
import org.netbeans.modules.maven.model.pom.visitor.POMComponentVisitor;	

/**
 *
 * @author mkleint
 */
public class ProjectImpl extends POMComponentImpl implements Project {

    public ProjectImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ProjectImpl(POMModel model) {
        this(model, createElementNS(model, POMQName.PROJECT));
    }

    // attributes

    // child elements
    public Parent getParent() {
        return getChild(Parent.class);
    }

    public void setParent(Parent parent) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(Parent.class, PARENT_PROPERTY, parent, empty);
    }

    public Prerequisites getPrerequisites() {
        return getChild(Prerequisites.class);
    }

    public void setPrerequisites(Prerequisites prerequisites) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(Prerequisites.class, PREREQUISITES_PROPERTY, prerequisites, empty);
    }

    public IssueManagement getIssueManagement() {
        return getChild(IssueManagement.class);
    }

    public void setIssueManagement(IssueManagement issueManagement) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(IssueManagement.class, ISSUEMANAGEMENT_PROPERTY, issueManagement, empty);
    }

    public CiManagement getCiManagement() {
        return getChild(CiManagement.class);
    }

    public void setCiManagement(CiManagement ciManagement) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(CiManagement.class, CIMANAGEMENT_PROPERTY, ciManagement, empty);
    }

    public List<MailingList> getMailingLists() {
        return getChildren(MailingList.class);
    }

    public void addMailingList(MailingList ciManagement) {
        appendChild(MAILINGLIST_PROPERTY, ciManagement);
    }

    public void removeMailingList(MailingList ciManagement) {
        removeChild(MAILINGLIST_PROPERTY, ciManagement);
    }

    public List<Developer> getDevelopers() {
        return getChildren(Developer.class);
    }

    public void addDeveloper(Developer ciManagement) {
        appendChild(DEVELOPER_PROPERTY, ciManagement);
    }

    public void removeDeveloper(Developer ciManagement) {
        removeChild(DEVELOPER_PROPERTY, ciManagement);
    }

    public List<Contributor> getContributors() {
        return getChildren(Contributor.class);
    }

    public void addContributor(Contributor ciManagement) {
        appendChild(CONTRIBUTOR_PROPERTY, ciManagement);
    }

    public void removeContributor(Contributor ciManagement) {
        removeChild(CONTRIBUTOR_PROPERTY, ciManagement);
    }

    public List<License> getLicenses() {
        return getChildren(License.class);
    }

    public void addLicense(License ciManagement) {
        appendChild(LICENSE_PROPERTY, ciManagement);
    }

    public void removeLicense(License ciManagement) {
        removeChild(LICENSE_PROPERTY, ciManagement);
    }

    public Scm getScm() {
        return getChild(Scm.class);
    }

    public void setScm(Scm scm) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(Scm.class, SCM_PROPERTY, scm, empty);
    }

    public Organization getOrganization() {
        return getChild(Organization.class);
    }

    public void setOrganization(Organization organization) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(Organization.class, ORGANIZATION_PROPERTY, organization, empty);
    }

    public Build getBuild() {
        return getChild(Build.class);
    }

    public void setBuild(Build build) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(Build.class, BUILD_PROPERTY, build, empty);
    }

    public List<Profile> getProfiles() {
        return getChildren(Profile.class);
    }

    public void addProfile(Profile build) {
        appendChild(PROFILE_PROPERTY, build);
    }

    public void removeProfile(Profile build) {
        removeChild(PROFILE_PROPERTY, build);
    }

    public List<Module> getModules() {
        return getChildren(Module.class);
    }

    public void addModule(Module build) {
        appendChild(MODULE_PROPERTY, build);
    }

    public void removeModule(Module build) {
        removeChild(MODULE_PROPERTY, build);
    }

    public List<Repository> getRepositorys() {
        return getChildren(Repository.class);
    }

    public void addRepository(Repository build) {
        appendChild(REPOSITORY_PROPERTY, build);
    }

    public void removeRepository(Repository build) {
        removeChild(REPOSITORY_PROPERTY, build);
    }

    public List<PluginRepository> getPluginRepositorys() {
        return getChildren(PluginRepository.class);
    }

    public void addPluginRepository(PluginRepository build) {
        appendChild(PLUGINREPOSITORY_PROPERTY, build);
    }

    public void removePluginRepository(PluginRepository build) {
        removeChild(PLUGINREPOSITORY_PROPERTY, build);
    }

    public List<Dependency> getDependencys() {
        return getChildren(Dependency.class);
    }

    public void addDependency(Dependency build) {
        appendChild(DEPENDENCY_PROPERTY, build);
    }

    public void removeDependency(Dependency build) {
        removeChild(DEPENDENCY_PROPERTY, build);
    }

    public Reporting getReporting() {
        return getChild(Reporting.class);
    }

    public void setReporting(Reporting reporting) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(Reporting.class, REPORTING_PROPERTY, reporting, empty);
    }

    public DependencyManagement getDependencyManagement() {
        return getChild(DependencyManagement.class);
    }

    public void setDependencyManagement(DependencyManagement dependencyManagement) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(DependencyManagement.class, DEPENDENCYMANAGEMENT_PROPERTY, dependencyManagement, empty);
    }

    public DistributionManagement getDistributionManagement() {
        return getChild(DistributionManagement.class);
    }

    public void setDistributionManagement(DistributionManagement distributionManagement) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(DistributionManagement.class, DISTRIBUTIONMANAGEMENT_PROPERTY, distributionManagement, empty);
    }

    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

}