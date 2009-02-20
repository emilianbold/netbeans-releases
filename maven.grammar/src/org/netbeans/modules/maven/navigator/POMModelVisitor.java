/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.navigator;

import java.awt.Image;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.maven.model.pom.Activation;
import org.netbeans.modules.maven.model.pom.ActivationCustom;
import org.netbeans.modules.maven.model.pom.ActivationFile;
import org.netbeans.modules.maven.model.pom.ActivationOS;
import org.netbeans.modules.maven.model.pom.ActivationProperty;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.CiManagement;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.Contributor;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyContainer;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.DeploymentRepository;
import org.netbeans.modules.maven.model.pom.Developer;
import org.netbeans.modules.maven.model.pom.DistributionManagement;
import org.netbeans.modules.maven.model.pom.Exclusion;
import org.netbeans.modules.maven.model.pom.Extension;
import org.netbeans.modules.maven.model.pom.IssueManagement;
import org.netbeans.modules.maven.model.pom.License;
import org.netbeans.modules.maven.model.pom.MailingList;
import org.netbeans.modules.maven.model.pom.ModelList;
import org.netbeans.modules.maven.model.pom.Notifier;
import org.netbeans.modules.maven.model.pom.Organization;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.POMQNames;
import org.netbeans.modules.maven.model.pom.Parent;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginExecution;
import org.netbeans.modules.maven.model.pom.PluginManagement;
import org.netbeans.modules.maven.model.pom.Prerequisites;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.model.pom.ReportPlugin;
import org.netbeans.modules.maven.model.pom.ReportSet;
import org.netbeans.modules.maven.model.pom.Reporting;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.modules.maven.model.pom.RepositoryPolicy;
import org.netbeans.modules.maven.model.pom.Resource;
import org.netbeans.modules.maven.model.pom.Scm;
import org.netbeans.modules.maven.model.pom.Site;
import org.netbeans.modules.maven.model.pom.StringList;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkleint
 */
public class POMModelVisitor implements org.netbeans.modules.maven.model.pom.POMComponentVisitor {

    private Map<String, Node> childs = new LinkedHashMap<String, Node>();
    private int count = 0;
    private POMQNames names;
    private boolean filterUndefined;

    public POMModelVisitor(POMQNames names, boolean filterUndefined) {
        this.names = names;
        this.filterUndefined = filterUndefined;
    }

    public void reset() {
         childs = new LinkedHashMap<String, Node>();
         count = 0;
    }

    Node[] getChildNodes() {
        List<Node> toRet = new ArrayList<Node>();
        for (Node nd : childs.values()) {
            POMCutHolder cut = nd.getLookup().lookup(POMCutHolder.class);
            if (!filterUndefined || POMModelPanel.definesValue(cut.getCutValues())) {
                toRet.add(nd);
            }
        }
        return toRet.toArray(new Node[0]);
    }

    public void visit(Project target) {
        Project t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        //ordered by appearance in pom schema..
        checkChildString(names.MODELVERSION, NbBundle.getMessage(POMModelVisitor.class, "MODEL_VERSION"), t != null ? t.getModelVersion() : null);
        checkChildString(names.GROUPID, NbBundle.getMessage(POMModelVisitor.class, "GROUPID"), t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, NbBundle.getMessage(POMModelVisitor.class, "ARTIFACTID"), t != null ? t.getArtifactId() : null);
        checkChildString(names.PACKAGING, NbBundle.getMessage(POMModelVisitor.class, "PACKAGING"), t != null ? t.getPackaging() : null);
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.VERSION, NbBundle.getMessage(POMModelVisitor.class, "VERSION"), t != null ? t.getVersion() : null);
        checkChildString(names.DESCRIPTION, NbBundle.getMessage(POMModelVisitor.class, "DESCRIPTION"), t != null ? t.getDescription() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getURL() : null);
        checkChildObject(names.PREREQUISITES, Prerequisites.class, NbBundle.getMessage(POMModelVisitor.class, "PREREQUISITES"), t != null ? t.getPrerequisites() : null);
        checkChildObject(names.ISSUEMANAGEMENT, IssueManagement.class, NbBundle.getMessage(POMModelVisitor.class, "ISSUEMANAGEMENT"), t != null ? t.getIssueManagement() : null);
        checkChildObject(names.CIMANAGEMENT, CiManagement.class, NbBundle.getMessage(POMModelVisitor.class, "CIMANAGEMENT"), t != null ? t.getCiManagement() : null);
        checkChildString(names.INCEPTIONYEAR, NbBundle.getMessage(POMModelVisitor.class, "INCEPTION_YEAR"), t != null ? t.getInceptionYear() : null);
        this.<MailingList>checkListObject(names.MAILINGLISTS, names.MAILINGLIST,
                MailingList.class, NbBundle.getMessage(POMModelVisitor.class, "MAILING_LISTS"),
                t != null ? t.getMailingLists() : null,
                new IdentityKeyGenerator<MailingList>() {
                    public String createName(MailingList c) {
                        return c.getName() != null ? c.getName() : NbBundle.getMessage(POMModelVisitor.class, "MAILING_LIST");
                    }
                });
        this.<Developer>checkListObject(names.DEVELOPERS, names.DEVELOPER,
                Developer.class, NbBundle.getMessage(POMModelVisitor.class, "DEVELOPERS"),
                t != null ? t.getDevelopers() : null,
                new IdentityKeyGenerator<Developer>() {
                    public String createName(Developer c) {
                        return c.getId() != null ? c.getId() : NbBundle.getMessage(POMModelVisitor.class, "DEVELOPER");
                    }
                });
        this.<Contributor>checkListObject(names.CONTRIBUTORS, names.CONTRIBUTOR,
                Contributor.class, NbBundle.getMessage(POMModelVisitor.class, "CONTRIBUTORS"),
                t != null ? t.getContributors() : null,
                new IdentityKeyGenerator<Contributor>() {
                    public String createName(Contributor c) {
                        return c.getName() != null ? c.getName() : NbBundle.getMessage(POMModelVisitor.class, "CONTRIBUTOR");
                    }
                });
        this.<License>checkListObject(names.LICENSES, names.LICENSE,
                License.class, NbBundle.getMessage(POMModelVisitor.class, "LICENSES"),
                t != null ? t.getLicenses() : null,
                new IdentityKeyGenerator<License>() {
                    public String createName(License c) {
                        return c.getName() != null ? c.getName() : NbBundle.getMessage(POMModelVisitor.class, "LICENSE");
                    }
                });
        checkChildObject(names.SCM, Scm.class, NbBundle.getMessage(POMModelVisitor.class, "SCM"), t != null ? t.getScm() : null);
        checkChildObject(names.ORGANIZATION, Organization.class, NbBundle.getMessage(POMModelVisitor.class, "ORGANIZATION"), t != null ? t.getOrganization() : null);
        checkChildObject(names.BUILD, Build.class, NbBundle.getMessage(POMModelVisitor.class, "BUILD"), t != null ? t.getBuild() : null);
        this.<Profile>checkListObject(names.PROFILES, names.PROFILE,
                Profile.class, NbBundle.getMessage(POMModelVisitor.class, "PROFILES"),
                t != null ? t.getProfiles() : null,
                new KeyGenerator<Profile>() {
                    public Object generate(Profile c) {
                        return c.getId();
                    }
                    public String createName(Profile c) {
                        return c.getId() != null ? c.getId() : NbBundle.getMessage(POMModelVisitor.class, "PROFILE");
                    }
                });
        this.<Repository>checkListObject(names.REPOSITORIES, names.REPOSITORY,
                Repository.class, NbBundle.getMessage(POMModelVisitor.class, "REPOSITORIES"),
                t != null ? t.getRepositories() : null,
                new KeyGenerator<Repository>() {
                    public Object generate(Repository c) {
                        return c.getId();
                    }
                    public String createName(Repository c) {
                        return c.getId() != null ? c.getId() : NbBundle.getMessage(POMModelVisitor.class, "REPOSITORY");
                    }
                });
        this.<Repository>checkListObject(names.PLUGINREPOSITORIES, names.PLUGINREPOSITORY,
                Repository.class, NbBundle.getMessage(POMModelVisitor.class, "PLUGIN_REPOSITORIES"),
                t != null ? t.getPluginRepositories() : null,
                new KeyGenerator<Repository>() {
                    public Object generate(Repository c) {
                        return c.getId();
                    }
                    public String createName(Repository c) {
                        return c.getId() != null ? c.getId() : NbBundle.getMessage(POMModelVisitor.class, "REPOSITORY");
                    }
                });
        checkDependencies(t);
        checkChildObject(names.REPORTING, Reporting.class, NbBundle.getMessage(POMModelVisitor.class, "REPORTING"), t != null ? t.getReporting() : null);
        checkChildObject(names.DEPENDENCYMANAGEMENT, DependencyManagement.class, NbBundle.getMessage(POMModelVisitor.class, "DEPENDENCY_MANAGEMENT"), t != null ? t.getDependencyManagement() : null);
        checkChildObject(names.DISTRIBUTIONMANAGEMENT, DistributionManagement.class, NbBundle.getMessage(POMModelVisitor.class, "DISTRIBUTION_MANAGEMENT"), t != null ? t.getDistributionManagement() : null);
        checkChildObject(names.PROPERTIES, Properties.class, NbBundle.getMessage(POMModelVisitor.class, "PROPERTIES"), t != null ? t.getProperties() : null);

        count++;
    }

    public void visit(Parent target) {
    }

    public void visit(Organization target) {
        Organization t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(DistributionManagement target) {
        DistributionManagement t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildObject(names.DIST_REPOSITORY, DeploymentRepository.class, NbBundle.getMessage(POMModelVisitor.class, "REPOSITORY"), t != null ? t.getRepository() : null);
        checkChildObject(names.DIST_SNAPSHOTREPOSITORY, DeploymentRepository.class, NbBundle.getMessage(POMModelVisitor.class, "SNAPSHOT_REPOSITORY"), t != null ? t.getSnapshotRepository() : null);
        checkChildObject(names.SITE, Site.class, NbBundle.getMessage(POMModelVisitor.class, "SITE"), t != null ? t.getSite() : null);
        checkChildString(names.DOWNLOADURL, NbBundle.getMessage(POMModelVisitor.class, "DOWNLOAD_URL"), t != null ? t.getDownloadUrl() : null);

        count++;
    }

    public void visit(Site target) {
        Site t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, NbBundle.getMessage(POMModelVisitor.class, "ID"), t != null ? t.getId() : null);
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(DeploymentRepository target) {
        DeploymentRepository t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, NbBundle.getMessage(POMModelVisitor.class, "ID"), t != null ? t.getId() : null);
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);
        checkChildString(names.LAYOUT, NbBundle.getMessage(POMModelVisitor.class, "LAYOUT"), t != null ? t.getLayout() : null);

        count++;
    }

    public void visit(Prerequisites target) {
        Prerequisites t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.MAVEN, NbBundle.getMessage(POMModelVisitor.class, "MAVEN"), t != null ? t.getMaven() : null);

        count++;
    }

    public void visit(Contributor target) {
        Contributor t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.EMAIL, NbBundle.getMessage(POMModelVisitor.class, "EMAIL"), t != null ? t.getEmail() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);
        checkChildString(names.ORGANIZATION, NbBundle.getMessage(POMModelVisitor.class, "ORGANIZATION"), t != null ? t.getOrganization() : null);
        checkChildString(names.ORGANIZATIONURL, NbBundle.getMessage(POMModelVisitor.class, "ORGANIZATION_URL"), t != null ? t.getOrganizationUrl() : null);
        checkChildString(names.TIMEZONE, NbBundle.getMessage(POMModelVisitor.class, "TIMEZONE"), t != null ? t.getTimezone() : null);

        count++;    }

    public void visit(Scm target) {
        Scm t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.CONNECTION, NbBundle.getMessage(POMModelVisitor.class, "CONNECTION"), t != null ? t.getConnection() : null);
        checkChildString(names.DEVELOPERCONNECTION, NbBundle.getMessage(POMModelVisitor.class, "DEVELOPER_CONNECTION"), t != null ? t.getDeveloperConnection() : null);
        checkChildString(names.TAG, NbBundle.getMessage(POMModelVisitor.class, "TAG"), t != null ? t.getTag() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(IssueManagement target) {
        IssueManagement t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.SYSTEM, NbBundle.getMessage(POMModelVisitor.class, "SYSTEM"), t != null ? t.getSystem() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(CiManagement target) {
        CiManagement t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.SYSTEM, NbBundle.getMessage(POMModelVisitor.class, "SYSTEM"), t != null ? t.getSystem() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(Notifier target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Repository target) {
        Repository t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, NbBundle.getMessage(POMModelVisitor.class, "ID"), t != null ? t.getId() : null);
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);
        checkChildString(names.LAYOUT, NbBundle.getMessage(POMModelVisitor.class, "LAYOUT"), t != null ? t.getLayout() : null);
        checkChildObject(names.RELEASES, RepositoryPolicy.class, NbBundle.getMessage(POMModelVisitor.class, "RELEASES"), t != null ? t.getReleases() : null);
        checkChildObject(names.SNAPSHOTS, RepositoryPolicy.class, NbBundle.getMessage(POMModelVisitor.class, "SNAPSHOTS"), t != null ? t.getSnapshots() : null);

        count++;
    }

    public void visit(RepositoryPolicy target) {
        RepositoryPolicy t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ENABLED, NbBundle.getMessage(POMModelVisitor.class, "ENABLED"), t != null ? (t.isEnabled() != null ? t.isEnabled().toString() : null) : null);
        checkChildString(names.UPDATEPOLICY, NbBundle.getMessage(POMModelVisitor.class, "UPDATE_POLICY"), t != null ? t.getUpdatePolicy() : null);
        checkChildString(names.CHECKSUMPOLICY, NbBundle.getMessage(POMModelVisitor.class, "CHECKSUM_POLICY"), t != null ? t.getChecksumPolicy() : null);

        count++;
    }

    public void visit(Profile target) {
        Profile t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, NbBundle.getMessage(POMModelVisitor.class, "ID"), t != null ? t.getId() : null);
        checkChildObject(names.ACTIVATION, Activation.class, NbBundle.getMessage(POMModelVisitor.class, "ACTIVATION"), t != null ? t.getActivation() : null);
        checkChildObject(names.BUILD, BuildBase.class, NbBundle.getMessage(POMModelVisitor.class, "BUILD"), t != null ? t.getBuildBase() : null);
        this.<Repository>checkListObject(names.REPOSITORIES, names.REPOSITORY,
                Repository.class, NbBundle.getMessage(POMModelVisitor.class, "REPOSITORIES"),
                t != null ? t.getRepositories() : null,
                new KeyGenerator<Repository>() {
                    public Object generate(Repository c) {
                        return c.getId();
                    }
                    public String createName(Repository c) {
                        return c.getId() != null ? c.getId() : NbBundle.getMessage(POMModelVisitor.class, "REPOSITORY");
                    }
                });
        this.<Repository>checkListObject(names.PLUGINREPOSITORIES, names.PLUGINREPOSITORY,
                Repository.class, NbBundle.getMessage(POMModelVisitor.class, "PLUGIN_REPOSITORIES"),
                t != null ? t.getPluginRepositories() : null,
                new KeyGenerator<Repository>() {
                    public Object generate(Repository c) {
                        return c.getId();
                    }
                    public String createName(Repository c) {
                        return c.getId() != null ? c.getId() : NbBundle.getMessage(POMModelVisitor.class, "REPOSITORY");
                    }
                });
        checkDependencies(t);
        checkChildObject(names.REPORTING, Reporting.class, NbBundle.getMessage(POMModelVisitor.class, "REPORTING"), t != null ? t.getReporting() : null);
        checkChildObject(names.DEPENDENCYMANAGEMENT, DependencyManagement.class, NbBundle.getMessage(POMModelVisitor.class, "DEPENDENCY_MANAGEMENT"), t != null ? t.getDependencyManagement() : null);
        checkChildObject(names.DISTRIBUTIONMANAGEMENT, DistributionManagement.class, NbBundle.getMessage(POMModelVisitor.class, "DISTRIBUTION_MANAGEMENT"), t != null ? t.getDistributionManagement() : null);
        checkChildObject(names.PROPERTIES, Properties.class, NbBundle.getMessage(POMModelVisitor.class, "PROPERTIES"), t != null ? t.getProperties() : null);

        count++;
    }

    public void visit(BuildBase target) {
        BuildBase t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.DEFAULTGOAL, NbBundle.getMessage(POMModelVisitor.class, "DEFAULT_GOAL"), t != null ? t.getDefaultGoal() : null);
        this.<Resource>checkListObject(names.RESOURCES, names.RESOURCE,
                Resource.class, NbBundle.getMessage(POMModelVisitor.class, "RESOURCES"),
                t != null ? t.getResources() : null,
                new IdentityKeyGenerator<Resource>() {
                    public String createName(Resource c) {
                        return c.getDirectory() != null ? c.getDirectory() : NbBundle.getMessage(POMModelVisitor.class, "RESOURCE");
                    }
                });
        this.<Resource>checkListObject(names.TESTRESOURCES, names.TESTRESOURCE,
                Resource.class, NbBundle.getMessage(POMModelVisitor.class, "TEST_RESOURCES"),
                t != null ? t.getTestResources() : null,
                new IdentityKeyGenerator<Resource>() {
                    public String createName(Resource c) {
                        return c.getDirectory() != null ? c.getDirectory() : NbBundle.getMessage(POMModelVisitor.class, "TEST_RESOURCE");
                    }
                });
        checkChildString(names.DIRECTORY, NbBundle.getMessage(POMModelVisitor.class, "DIRECTORY"), t != null ? t.getDirectory() : null);
        checkChildString(names.FINALNAME, NbBundle.getMessage(POMModelVisitor.class, "FINAL_NAME"), t != null ? t.getFinalName() : null);
        //TODO filters
        checkChildObject(names.PLUGINMANAGEMENT, PluginManagement.class, NbBundle.getMessage(POMModelVisitor.class, "PLUGIN_MANAGEMENT"), t != null ? t.getPluginManagement() : null);
        this.<Plugin>checkListObject(names.PLUGINS, names.PLUGIN,
                Plugin.class, NbBundle.getMessage(POMModelVisitor.class, "PLUGINS"),
                t != null ? t.getPlugins() : null,
                new KeyGenerator<Plugin>() {
                    public Object generate(Plugin c) {
                        String gr = c.getGroupId();
                        if (gr == null) {
                            gr = "org.apache.maven.plugins"; //NOI18N
                        }
                        return gr + ":" + c.getArtifactId(); //NOI18N
                    }
                    public String createName(Plugin c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : NbBundle.getMessage(POMModelVisitor.class, "PLUGIN");
                    }
                });

        count++;
    }

    public void visit(Plugin target) {
        Plugin t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.GROUPID, NbBundle.getMessage(POMModelVisitor.class, "GROUPID"), t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, NbBundle.getMessage(POMModelVisitor.class, "ARTIFACTID"), t != null ? t.getArtifactId() : null);
        checkChildString(names.VERSION, NbBundle.getMessage(POMModelVisitor.class, "VERSION"), t != null ? t.getVersion() : null);
        checkChildString(names.EXTENSIONS, NbBundle.getMessage(POMModelVisitor.class, "EXTENSIONS"), t != null ? (t.isExtensions() != null ? t.isExtensions().toString() : null) : null);
        this.<PluginExecution>checkListObject(names.EXECUTIONS, names.EXECUTION,
                PluginExecution.class, NbBundle.getMessage(POMModelVisitor.class, "EXECUTIONS"),
                t != null ? t.getExecutions() : null,
                new KeyGenerator<PluginExecution>() {
                    public Object generate(PluginExecution c) {
                        return c.getId(); //NOI18N
                    }
                    public String createName(PluginExecution c) {
                        return c.getId() != null ? c.getId() : NbBundle.getMessage(POMModelVisitor.class, "EXECUTION");
                    }
                });
        checkDependencies(t);
        //TODO goals.
        checkChildString(names.INHERITED, NbBundle.getMessage(POMModelVisitor.class, "INHERITED"), t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, NbBundle.getMessage(POMModelVisitor.class, "CONFIGURATION"), t != null ? t.getConfiguration() : null);

        count++;
    }

    public void visit(Dependency target) {
        Dependency t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.GROUPID, NbBundle.getMessage(POMModelVisitor.class, "GROUPID"), t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, NbBundle.getMessage(POMModelVisitor.class, "ARTIFACTID"), t != null ? t.getArtifactId() : null);
        checkChildString(names.VERSION, NbBundle.getMessage(POMModelVisitor.class, "VERSION"), t != null ? t.getVersion() : null);
        checkChildString(names.TYPE, NbBundle.getMessage(POMModelVisitor.class, "TYPE"), t != null ? t.getType() : null);
        checkChildString(names.CLASSIFIER, NbBundle.getMessage(POMModelVisitor.class, "CLASSIFIER"), t != null ? t.getClassifier() : null);
        checkChildString(names.SCOPE, NbBundle.getMessage(POMModelVisitor.class, "SCOPE"), t != null ? t.getScope() : null);

        this.<Exclusion>checkListObject(names.EXCLUSIONS, names.EXCLUSION,
                Exclusion.class, NbBundle.getMessage(POMModelVisitor.class, "EXCLUSIONS"),
                t != null ? t.getExclusions() : null,
                new IdentityKeyGenerator<Exclusion>() {
                    public String createName(Exclusion c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : NbBundle.getMessage(POMModelVisitor.class, "EXCLUSION");
                    }
                });

        count++;
    }

    public void visit(Exclusion target) {
        Exclusion t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.GROUPID, NbBundle.getMessage(POMModelVisitor.class, "GROUPID"), t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, NbBundle.getMessage(POMModelVisitor.class, "ARTIFACTID"), t != null ? t.getArtifactId() : null);

        count++;
    }

    public void visit(PluginExecution target) {
        PluginExecution t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, NbBundle.getMessage(POMModelVisitor.class, "ID"), t != null ? t.getId() : null);
        checkChildString(names.PHASE, NbBundle.getMessage(POMModelVisitor.class, "PHASE"), t != null ? t.getPhase() : null);
        //TODO goals.
        checkChildString(names.INHERITED, NbBundle.getMessage(POMModelVisitor.class, "INHERITED"), t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, NbBundle.getMessage(POMModelVisitor.class, "CONFIGURATION"), t != null ? t.getConfiguration() : null);

        count++;
    }

    public void visit(Resource target) {
        Resource t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.TARGETPATH, NbBundle.getMessage(POMModelVisitor.class, "TARGET_PATH"), t != null ? t.getTargetPath() : null);
        //TODO filtering
        checkChildString(names.DIRECTORY, NbBundle.getMessage(POMModelVisitor.class, "DIRECTORY"), t != null ? t.getDirectory() : null);
        //TODO includes, excludes

        count++;
    }

    public void visit(PluginManagement target) {
        PluginManagement t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        this.<Plugin>checkListObject(names.PLUGINS, names.PLUGIN,
                Plugin.class, NbBundle.getMessage(POMModelVisitor.class, "PLUGINS"),
                t != null ? t.getPlugins() : null,
                new KeyGenerator<Plugin>() {
                    public Object generate(Plugin c) {
                        String gr = c.getGroupId();
                        if (gr == null) {
                            gr = "org.apache.maven.plugins"; //NOI18N
                        }
                        return gr + ":" + c.getArtifactId(); //NOI18N
                    }
                    public String createName(Plugin c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : NbBundle.getMessage(POMModelVisitor.class, "PLUGIN");
                    }
                });

        count++;
    }

    public void visit(Reporting target) {
        Reporting t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.EXCLUDEDEFAULTS, NbBundle.getMessage(POMModelVisitor.class, "EXCLUDE_DEFAULTS"), t != null ? (t.isExcludeDefaults() != null ? t.isExcludeDefaults().toString() : null) : null);
        checkChildString(names.OUTPUTDIRECTORY, NbBundle.getMessage(POMModelVisitor.class, "OUTPUT_DIRECTORY"), t != null ? t.getOutputDirectory() : null);
        this.<ReportPlugin>checkListObject(names.REPORTPLUGINS, names.REPORTPLUGIN,
                ReportPlugin.class, NbBundle.getMessage(POMModelVisitor.class, "REPORT_PLUGINS"),
                t != null ? t.getReportPlugins() : null,
                new KeyGenerator<ReportPlugin>() {
                    public Object generate(ReportPlugin c) {
                        return c.getGroupId() + ":" + c.getArtifactId(); //NOI18N
                    }
                    public String createName(ReportPlugin c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : NbBundle.getMessage(POMModelVisitor.class, "REPORT_PLUGIN");
                    }
                });

        count++;
    }

    public void visit(ReportPlugin target) {
        ReportPlugin t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.GROUPID, NbBundle.getMessage(POMModelVisitor.class, "GROUPID"), t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, NbBundle.getMessage(POMModelVisitor.class, "ARTIFACTID"), t != null ? t.getArtifactId() : null);
        checkChildString(names.VERSION, NbBundle.getMessage(POMModelVisitor.class, "VERSION"), t != null ? t.getVersion() : null);
        checkChildString(names.INHERITED, NbBundle.getMessage(POMModelVisitor.class, "INHERITED"), t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, NbBundle.getMessage(POMModelVisitor.class, "CONFIGURATION"), t != null ? t.getConfiguration() : null);
        this.<ReportSet>checkListObject(names.REPORTSETS, names.REPORTSET,
                ReportSet.class, NbBundle.getMessage(POMModelVisitor.class, "REPORTSETS"),
                t != null ? t.getReportSets() : null,
                new KeyGenerator<ReportSet>() {
                    public Object generate(ReportSet c) {
                        return c.getId(); //NOI18N
                    }
                    public String createName(ReportSet c) {
                        return c.getId() != null ? c.getId() : NbBundle.getMessage(POMModelVisitor.class, "REPORTSET");
                    }
                });

        count++;
    }

    public void visit(ReportSet target) {
        ReportSet t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, NbBundle.getMessage(POMModelVisitor.class, "ID"), t != null ? t.getId() : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, NbBundle.getMessage(POMModelVisitor.class, "CONFIGURATION"), t != null ? t.getConfiguration() : null);
        checkChildString(names.INHERITED, NbBundle.getMessage(POMModelVisitor.class, "INHERITED"), t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        //TODO reports.

        count++;
    }

    public void visit(Activation target) {
        Activation t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildObject(names.ACTIVATIONOS, ActivationOS.class, NbBundle.getMessage(POMModelVisitor.class, "OPERATING_SYSTEM"), t != null ? t.getActivationOS() : null);
        checkChildObject(names.ACTIVATIONPROPERTY, ActivationProperty.class, NbBundle.getMessage(POMModelVisitor.class, "PROPERTY"), t != null ? t.getActivationProperty() : null);
        checkChildObject(names.ACTIVATIONFILE, ActivationFile.class, NbBundle.getMessage(POMModelVisitor.class, "FILE"), t != null ? t.getActivationFile() : null);
        checkChildObject(names.ACTIVATIONCUSTOM, ActivationCustom.class, NbBundle.getMessage(POMModelVisitor.class, "CUSTOM"), t != null ? t.getActivationCustom() : null);

        count++;
    }

    public void visit(ActivationProperty target) {
    }

    public void visit(ActivationOS target) {
    }

    public void visit(ActivationFile target) {
    }

    public void visit(ActivationCustom target) {
    }

    public void visit(DependencyManagement target) {
        DependencyManagement t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkDependencies(t);

        count++;
    }

    public void visit(Build target) {
        Build t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.SOURCEDIRECTORY, NbBundle.getMessage(POMModelVisitor.class, "SOURCE_DIRECTORY"), t != null ? t.getSourceDirectory() : null);
        //just ignore script directory
        checkChildString(names.TESTSOURCEDIRECTORY, NbBundle.getMessage(POMModelVisitor.class, "TEST_SOURCE_DIRECTORY"), t != null ? t.getTestSourceDirectory() : null);
        checkChildString(names.OUTPUTDIRECTORY, NbBundle.getMessage(POMModelVisitor.class, "OUTPUT_DIRECTORY"), t != null ? t.getOutputDirectory() : null);
        checkChildString(names.TESTOUTPUTDIRECTORY, NbBundle.getMessage(POMModelVisitor.class, "TEST_OUTPUT_DIRECTORY"), t != null ? t.getTestOutputDirectory() : null);
        this.<Extension>checkListObject(names.EXTENSIONS, names.EXTENSION,
                Extension.class, NbBundle.getMessage(POMModelVisitor.class, "EXTENSIONS"),
                t != null ? t.getExtensions() : null,
                new KeyGenerator<Extension>() {
                    public Object generate(Extension c) {
                        String gr = c.getGroupId();
                        return gr + ":" + c.getArtifactId(); //NOI18N
                    }
                    public String createName(Extension c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : "Extension";
                    }
                });
        checkChildString(names.DEFAULTGOAL, NbBundle.getMessage(POMModelVisitor.class, "DEFAULT_GOAL"), t != null ? t.getDefaultGoal() : null);
        this.<Resource>checkListObject(names.RESOURCES, names.RESOURCE,
                Resource.class, NbBundle.getMessage(POMModelVisitor.class, "RESOURCES"),
                t != null ? t.getResources() : null,
                new IdentityKeyGenerator<Resource>() {
                    public String createName(Resource c) {
                        return c.getDirectory() != null ? c.getDirectory() : NbBundle.getMessage(POMModelVisitor.class, "RESOURCE");
                    }
                });
        this.<Resource>checkListObject(names.TESTRESOURCES, names.TESTRESOURCE,
                Resource.class, NbBundle.getMessage(POMModelVisitor.class, "TEST_RESOURCES"),
                t != null ? t.getTestResources() : null,
                new IdentityKeyGenerator<Resource>() {
                    public String createName(Resource c) {
                        return c.getDirectory() != null ? c.getDirectory() : NbBundle.getMessage(POMModelVisitor.class, "TEST_RESOURCE");
                    }
                });
        checkChildString(names.DIRECTORY, NbBundle.getMessage(POMModelVisitor.class, "DIRECTORY"), t != null ? t.getDirectory() : null);
        checkChildString(names.FINALNAME, NbBundle.getMessage(POMModelVisitor.class, "FINAL_NAME"), t != null ? t.getFinalName() : null);
        //TODO filters
        checkChildObject(names.PLUGINMANAGEMENT, PluginManagement.class, NbBundle.getMessage(POMModelVisitor.class, "PLUGIN_MANAGEMENT"), t != null ? t.getPluginManagement() : null);
        this.<Plugin>checkListObject(names.PLUGINS, names.PLUGIN,
                Plugin.class, NbBundle.getMessage(POMModelVisitor.class, "PLUGINS"),
                t != null ? t.getPlugins() : null,
                new KeyGenerator<Plugin>() {
                    public Object generate(Plugin c) {
                        String gr = c.getGroupId();
                        if (gr == null) {
                            gr = "org.apache.maven.plugins"; //NOI18N
                        }
                        return gr + ":" + c.getArtifactId(); //NOI18N
                    }
                    public String createName(Plugin c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : NbBundle.getMessage(POMModelVisitor.class, "PLUGIN");
                    }
                });

        count++;
    }

    public void visit(Extension target) {
        Extension t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.GROUPID, NbBundle.getMessage(POMModelVisitor.class, "GROUPID"), t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, NbBundle.getMessage(POMModelVisitor.class, "ARTIFACTID"), t != null ? t.getArtifactId() : null);
        checkChildString(names.VERSION, NbBundle.getMessage(POMModelVisitor.class, "VERSION"), t != null ? t.getVersion() : null);
    }

    public void visit(License target) {
        License t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);
        count++;
    }

    public void visit(MailingList target) {
        MailingList t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.SUBSCRIBE, NbBundle.getMessage(POMModelVisitor.class, "SUBSCRIBE"), t != null ? t.getSubscribe() : null);
        checkChildString(names.UNSUBSCRIBE, NbBundle.getMessage(POMModelVisitor.class, "UNSUBSCRIBE"), t != null ? t.getUnsubscribe() : null);
        checkChildString(names.POST, NbBundle.getMessage(POMModelVisitor.class, "POST"), t != null ? t.getPost() : null);
        checkChildString(names.ARCHIVE, NbBundle.getMessage(POMModelVisitor.class, "ARCHIVE"), t != null ? t.getArchive() : null);
        count++;
    }

    public void visit(Developer target) {
        Developer t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, NbBundle.getMessage(POMModelVisitor.class, "ID"), t != null ? t.getId() : null);
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.EMAIL, NbBundle.getMessage(POMModelVisitor.class, "EMAIL"), t != null ? t.getEmail() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);
        checkChildString(names.ORGANIZATION, NbBundle.getMessage(POMModelVisitor.class, "ORGANIZATION"), t != null ? t.getOrganization() : null);
        checkChildString(names.ORGANIZATIONURL, NbBundle.getMessage(POMModelVisitor.class, "ORGANIZATION_URL"), t != null ? t.getOrganizationUrl() : null);
        checkChildString(names.TIMEZONE, NbBundle.getMessage(POMModelVisitor.class, "TIMEZONE"), t != null ? t.getTimezone() : null);

        count++;
    }

    public void visit(POMExtensibilityElement target) {
        POMExtensibilityElement t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        if (t != null) {
            doVisit(t.getAnyElements());
        }
        for (Node prop : childs.values()) {
            growToSize(count, prop.getLookup().lookup(POMCutHolder.class));
        }
        count++;
    }

    public void visit(ModelList target) {
    }

    public void visit(Configuration target) {
        Configuration t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        if (t != null) {
            doVisit(t.getConfigurationElements());
        }
        for (Node prop : childs.values()) {
            growToSize(count, prop.getLookup().lookup(POMCutHolder.class));
        }

        count++;
    }

    private void doVisit(List<POMExtensibilityElement> elems) {
        for (POMExtensibilityElement el : elems) {
            List<POMExtensibilityElement> any = el.getAnyElements();
            if (any != null && !any.isEmpty()) {
                Node nd = childs.get(el.getQName().getLocalPart());
                if (nd == null) {
                    POMCutHolder cutter = new POMCutHolder();
                    nd = new ObjectNode(Lookups.fixed(cutter, el.getQName()), new PomChildren(cutter, names, POMExtensibilityElement.class, filterUndefined), el.getQName().getLocalPart());
                    childs.put(el.getQName().getLocalPart(), nd);
                }
                fillValues(count, nd.getLookup().lookup(POMCutHolder.class), el);
            } else {
                Node nd = childs.get(el.getQName().getLocalPart());
                if (nd == null) {
                    nd = new SingleFieldNode(Lookups.fixed(new POMCutHolder()), el.getQName().getLocalPart());
                    childs.put(el.getQName().getLocalPart(), nd);
                }
                fillValues(count, nd.getLookup().lookup(POMCutHolder.class), el.getElementText());
            }
        }

    }

    public void visit(Properties target) {
        Properties t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        if (t != null) {
            Map<String, String> props = t.getProperties();
            for (Map.Entry<String, String> ent : props.entrySet()) {
                Node nd = childs.get(ent.getKey());
                if (nd == null) {
                    nd = new SingleFieldNode(Lookups.fixed(new POMCutHolder()), ent.getKey());
                    childs.put(ent.getKey(), nd);
                }
                fillValues(count, nd.getLookup().lookup(POMCutHolder.class), ent.getValue());
            }
        }

        for (Node prop : childs.values()) {
            growToSize(count, prop.getLookup().lookup(POMCutHolder.class));
        }

        count++;

    }

    public void visit(StringList target) {
    }


    @SuppressWarnings("unchecked")
    private void checkChildString(POMQName qname, String displayName, String value) {
        Node nd = childs.get(qname.getName());
        if (nd == null) {
            nd = new SingleFieldNode(Lookups.fixed(new POMCutHolder(), qname), displayName);
            childs.put(qname.getName(), nd);
        }
        fillValues(count, nd.getLookup().lookup(POMCutHolder.class), value);
    }

    private void checkChildObject(POMQName qname, Class type, String displayName, POMComponent value) {
        Node nd = childs.get(qname.getName());
        if (nd == null) {
            POMCutHolder cutter = new POMCutHolder();
            nd = new ObjectNode(Lookups.fixed(cutter, qname), new PomChildren(cutter, names, type, filterUndefined), displayName);
            childs.put(qname.getName(), nd);
        }
        fillValues(count, nd.getLookup().lookup(POMCutHolder.class), value);
    }


    private <T extends POMComponent> void checkListObject(POMQName qname, POMQName childName, Class type, String displayName, List<T> values, KeyGenerator<T> keygen) {
        Node nd = childs.get(qname.getName());
        if (nd == null) {
            POMCutHolder cutter = new POMCutHolder();
            nd = new ListNode(Lookups.fixed(cutter, qname), new PomListChildren<T>(cutter, names, type, keygen, true, childName), displayName);
            childs.put(qname.getName(), nd);
        }
        fillValues(count, nd.getLookup().lookup(POMCutHolder.class), values);
    }


    private void fillValues(int current, POMCutHolder cutHolder, Object value) {
        growToSize(current, cutHolder);
        cutHolder.addCut(value);
    }

    private void growToSize(int count, POMCutHolder cutHolder) {
        while (cutHolder.getCutsSize() < count) {
            cutHolder.addCut(null);
        }
    }

    private void checkDependencies(DependencyContainer container) {
        this.<Dependency>checkListObject(names.DEPENDENCIES, names.DEPENDENCY,
                Dependency.class, NbBundle.getMessage(POMModelVisitor.class, "DEPENDENCIES"),
                container != null ? container.getDependencies() : null,
                new KeyGenerator<Dependency>() {
                    public Object generate(Dependency c) {
                        return c.getGroupId() + ":" + c.getArtifactId(); //NOI18N
                    }
                    public String createName(Dependency c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : NbBundle.getMessage(POMModelVisitor.class, "DEPENDENCY");
                    }
                });

    }

//    private static Children createOverrideListChildren(ChildrenCreator subs, List<List> values) {
//        Children toRet = new Children.Array();
//        int count = 0;
//        for (List lst : values) {
//            if (lst != null && lst.size() > 0) {
//                for (Object o : lst) {
//                    List objectList = new ArrayList(Collections.nCopies(count, null));
//                    objectList.add(o);
//                    toRet.add(new Node[] {
//                        new ObjectNode(Lookup.EMPTY, subs.createChildren(objectList),  subs.createName(o), objectList)
//                    });
//                }
//                break;
//            }
//            count = count + 1;
//        }
//
//        return toRet;
//    }
//
//    private static Children createMergeListChildren(ChildrenCreator2 subs, List<POMModel> key, List<List> values) {
//        Children toRet = new Children.Array();
//        HashMap<Object, List> content = new HashMap<Object, List>();
//        List order = new ArrayList();
//
//        int count = 0;
//        for (List lst : values) {
//            if (lst != null && lst.size() > 0) {
//                for (Object o : lst) {
//                    processObjectList(o, content, count, subs);
//                            new ArrayList(Collections.nCopies(count, null));
//                }
//            }
//            count = count + 1;
//        }
//        for (Map.Entry<Object, List> entry : content.entrySet()) {
//            toRet.add(new Node[] {
//                new ObjectNode(Lookup.EMPTY, subs.createChildren(entry.getValue(), key), key, subs.createName(entry.getKey()), entry.getValue())
//            });
//        }
//
//        return toRet;
//    }


    private interface KeyGenerator<T extends POMComponent> {
        Object generate(T c);

        String createName(T c);
    }

    private abstract class IdentityKeyGenerator<T extends POMComponent> implements  KeyGenerator<T> {
        public Object generate(T c) {
            return c;
        }

    }

    static class POMCutHolder {
        private List cuts = new ArrayList();
        Object[] getCutValues() {
            return cuts.toArray();
        }

        String[] getCutValuesAsString() {
            String[] toRet = new String[cuts.size()];
            int i = 0;
            for (Object cut : cuts) {
                toRet[i] = (cut != null ? cut.toString() : null);
                i++;
            }
            return toRet;
        }

        @SuppressWarnings("unchecked")
        void addCut(Object obj) {
            cuts.add(obj);
        }

        int getCutsSize() {
            return cuts.size();
        }
    }

    private static Image[] ICONS = new Image[] {
        ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/value.png"), // NOI18N
        ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/value2.png"), // NOI18N
        ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/value3.png"), // NOI18N
        ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/value4.png"), // NOI18N
    };

    private static Image getIconForCutHolder(POMCutHolder holder) {
        int level = POMModelPanel.currentValueDepth(holder.getCutValues());
        if (level >= 0 && level < ICONS.length) {
            return ICONS[level];
        }
        return ICONS[ICONS.length - 1];
    }


    private static class SingleFieldNode extends AbstractNode {

        private String key;
        private SingleFieldNode(Lookup lkp, String key) {
            super(Children.LEAF, lkp);
            setName(key);
            this.key = key;
        }

        @Override
        public String getHtmlDisplayName() {
            String[] values = getLookup().lookup(POMCutHolder.class).getCutValuesAsString();

            String dispVal = POMModelPanel.getValidValue(values);
            if (dispVal == null) {
                dispVal = NbBundle.getMessage(POMModelVisitor.class, "UNDEFINED");
            }
            boolean override = POMModelPanel.overridesParentValue(values);
            String overrideStart = override ? "<b>" : "";
            String overrideEnd = override ? "</b>" : "";
            boolean inherited = !POMModelPanel.isValueDefinedInCurrent(values);
            String inheritedStart = inherited ? "<i>" : "";
            String inheritedEnd = inherited ? "</i>" : "";

            String message = "<html>" +
                    inheritedStart + overrideStart +
                    key + " : " + dispVal +
                    overrideEnd + inheritedEnd;
            return message;
        }

        @Override
        public Image getIcon(int type) {
             return getIconForCutHolder(getLookup().lookup(POMCutHolder.class));
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

    }

    private static class ObjectNode extends AbstractNode {

        private String key;
        private ObjectNode(Lookup lkp, Children children, String key) {
            super( children, lkp);
            setName(key);
            this.key = key;
        }

        @Override
        public String getHtmlDisplayName() {
            Object[] values = getLookup().lookup(POMCutHolder.class).getCutValues();
            String dispVal = POMModelPanel.definesValue(values) ? "" : NbBundle.getMessage(POMModelVisitor.class, "UNDEFINED");
            boolean override = POMModelPanel.overridesParentValue(values);
            String overrideStart = override ? "<b>" : "";
            String overrideEnd = override ? "</b>" : "";
            boolean inherited = !POMModelPanel.isValueDefinedInCurrent(values);
            String inheritedStart = inherited ? "<i>" : "";
            String inheritedEnd = inherited ? "</i>" : "";

            String message = "<html>" +
                    inheritedStart + overrideStart +
                    key + " " + dispVal +
                    overrideEnd + inheritedEnd;

            return message;
        }

        @Override
        public Image getIcon(int type) {
             return getIconForCutHolder(getLookup().lookup(POMCutHolder.class));
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

    }

    private static class ListNode extends AbstractNode {

        private String key;

        private ListNode(Lookup lkp, Children childs, String name) {
            super(childs , lkp);
            setName(name);
            this.key = name;
        }

        @Override
        public String getHtmlDisplayName() {
            //TODO - this needs different markings..
            Object[] values = getLookup().lookup(POMCutHolder.class).getCutValues();

            String dispVal = POMModelPanel.definesValue(values) ? "" : NbBundle.getMessage(POMModelVisitor.class, "UNDEFINED");
            boolean override = POMModelPanel.overridesParentValue(values);
            String overrideStart = override ? "<b>" : "";
            String overrideEnd = override ? "</b>" : "";
            boolean inherited = !POMModelPanel.isValueDefinedInCurrent(values) && POMModelPanel.definesValue(values);
            String inheritedStart = inherited ? "<i>" : "";
            String inheritedEnd = inherited ? "</i>" : "";
            String message = "<html>" +
                    inheritedStart + overrideStart +
                    key + " " + dispVal +
                    overrideEnd + inheritedEnd;
            return message;
        }

        @Override
        public Image getIcon(int type) {
             return getIconForCutHolder(getLookup().lookup(POMCutHolder.class));
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

    }

    static class PomChildren extends Children.Keys<Object> {
        private Object[] one = new Object[] {new Object()};
        private POMCutHolder holder;
        private POMQNames names;
        private POMModelVisitor visitor;
        private Class type;
        private boolean filterUndefined;
        public PomChildren(POMCutHolder holder, POMQNames names, Class type, boolean filterUndefined) {
            setKeys(one);
            this.holder = holder;
            this.names = names;
            this.type = type;
            this.filterUndefined = filterUndefined;
        }

        public void reshow(boolean filterUndefined) {
            this.filterUndefined = filterUndefined;
            this.refreshKey(one[0]);
        }

        @Override
        protected Node[] createNodes(Object key) {
            boolean hasNonNullValue = false;
            visitor = new POMModelVisitor(names, filterUndefined);
            try {
                Method m = POMModelVisitor.class.getMethod("visit", type); //NOI18N
                for (Object comp : holder.getCutValues()) {
                    if (comp != null) {
                        hasNonNullValue = true;
                    }
                    try {
                        m.invoke(visitor, comp);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            return hasNonNullValue ? visitor.getChildNodes() : new Node[0];
        }


    }

    class PomListChildren<T extends POMComponent> extends Children.Keys<Object> {
        private Object[] one = new Object[] {new Object()};
        private POMCutHolder holder;
        private POMQNames names;
        private Class type;
        private KeyGenerator<T> keyGenerator;
        private boolean override;
        private POMQName childName;
        public PomListChildren(POMCutHolder holder, POMQNames names, Class type, KeyGenerator<T> generator, boolean override, POMQName childName) {
            setKeys(one);
            this.holder = holder;
            this.names = names;
            this.type = type;
            this.keyGenerator = generator;
            this.override = override;
            this.childName = childName;
        }

        public void reshow() {
            this.refreshKey(one);
        }

        @Override
        protected Node[] createNodes(Object key) {
            List<Node> toRet = new ArrayList<Node>();
            LinkedHashMap<Object, List<T>> cut = new LinkedHashMap<Object, List<T>>();

            int level = 0;
            for (Object comp : holder.getCutValues()) {
                if (comp == null) {
                    level++;
                    continue;
                }
                @SuppressWarnings("unchecked")
                List<T> lst = (List<T>) comp;
                for (T c : lst) {
                    Object keyGen = keyGenerator.generate(c);
                    List<T> currentCut = cut.get(keyGen);
                    if (currentCut == null) {
                        currentCut = new ArrayList<T>();
                        cut.put(keyGen, currentCut);
                    }
                    fillValues(level, currentCut, c);
                }
                level++;
            }
            for (List<T> lst : cut.values()) {
                POMCutHolder cutHolder = new POMCutHolder();
                T topMost = null;
                for (T c : lst) {
                    cutHolder.addCut(c);
                    if (topMost == null) {
                        topMost = c;
                    }
                }
                growToSize(holder.getCutsSize(), cutHolder);

                String itemName = keyGenerator.createName(topMost);
                toRet.add(new ObjectNode(Lookups.fixed(cutHolder, childName), new PomChildren(cutHolder, names, type, filterUndefined), itemName));
            }

            return toRet.toArray(new Node[0]);
        }

        private void fillValues(int current, List<T> list, T value) {
            while (list.size() < current) {
                list.add(null);
            }
            list.add(value);
        }
    }


}
