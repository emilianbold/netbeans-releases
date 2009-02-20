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
        checkChildString(names.MODELVERSION, "Model Version", t != null ? t.getModelVersion() : null);
        checkChildString(names.GROUPID, "GroupId", t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, "ArtifactId", t != null ? t.getArtifactId() : null);
        checkChildString(names.PACKAGING, "Packaging", t != null ? t.getPackaging() : null);
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.VERSION, "Version", t != null ? t.getVersion() : null);
        checkChildString(names.DESCRIPTION, "Description", t != null ? t.getDescription() : null);
        checkChildString(names.URL, "Url", t != null ? t.getURL() : null);
        checkChildObject(names.PREREQUISITES, Prerequisites.class, "Prerequisites", t != null ? t.getPrerequisites() : null);
        checkChildObject(names.ISSUEMANAGEMENT, IssueManagement.class, "IssueManagement", t != null ? t.getIssueManagement() : null);
        checkChildObject(names.CIMANAGEMENT, CiManagement.class, "CiManagement", t != null ? t.getCiManagement() : null);
        checkChildString(names.INCEPTIONYEAR, "Inception Year", t != null ? t.getInceptionYear() : null);
        this.<MailingList>checkListObject(names.MAILINGLISTS, names.MAILINGLIST,
                MailingList.class, "Mailing Lists",
                t != null ? t.getMailingLists() : null,
                new IdentityKeyGenerator<MailingList>() {
                    public String createName(MailingList c) {
                        return c.getName() != null ? c.getName() : "Mailing List";
                    }
                });
        this.<Developer>checkListObject(names.DEVELOPERS, names.DEVELOPER,
                Developer.class, "Developers",
                t != null ? t.getDevelopers() : null,
                new IdentityKeyGenerator<Developer>() {
                    public String createName(Developer c) {
                        return c.getId() != null ? c.getId() : "Developer";
                    }
                });
        this.<Contributor>checkListObject(names.CONTRIBUTORS, names.CONTRIBUTOR,
                Contributor.class, "Contributors",
                t != null ? t.getContributors() : null,
                new IdentityKeyGenerator<Contributor>() {
                    public String createName(Contributor c) {
                        return c.getName() != null ? c.getName() : "Contributor";
                    }
                });
        this.<License>checkListObject(names.LICENSES, names.LICENSE,
                License.class, "Licenses",
                t != null ? t.getLicenses() : null,
                new IdentityKeyGenerator<License>() {
                    public String createName(License c) {
                        return c.getName() != null ? c.getName() : "License";
                    }
                });
        checkChildObject(names.SCM, Scm.class, "Scm", t != null ? t.getScm() : null);
        checkChildObject(names.ORGANIZATION, Organization.class, "Organization", t != null ? t.getOrganization() : null);
        checkChildObject(names.BUILD, Build.class, "Build", t != null ? t.getBuild() : null);
        this.<Profile>checkListObject(names.PROFILES, names.PROFILE,
                Profile.class, "Profiles",
                t != null ? t.getProfiles() : null,
                new KeyGenerator<Profile>() {
                    public Object generate(Profile c) {
                        return c.getId();
                    }
                    public String createName(Profile c) {
                        return c.getId() != null ? c.getId() : "Profile";
                    }
                });
        this.<Repository>checkListObject(names.REPOSITORIES, names.REPOSITORY,
                Repository.class, "Repositories",
                t != null ? t.getRepositories() : null,
                new KeyGenerator<Repository>() {
                    public Object generate(Repository c) {
                        return c.getId();
                    }
                    public String createName(Repository c) {
                        return c.getId() != null ? c.getId() : "Repository";
                    }
                });
        this.<Repository>checkListObject(names.PLUGINREPOSITORIES, names.PLUGINREPOSITORY,
                Repository.class, "Plugin Repositories",
                t != null ? t.getPluginRepositories() : null,
                new KeyGenerator<Repository>() {
                    public Object generate(Repository c) {
                        return c.getId();
                    }
                    public String createName(Repository c) {
                        return c.getId() != null ? c.getId() : "Repository";
                    }
                });
        checkDependencies(t);
        checkChildObject(names.REPORTING, Reporting.class, "Reporting", t != null ? t.getReporting() : null);
        checkChildObject(names.DEPENDENCYMANAGEMENT, DependencyManagement.class, "Dependency Management", t != null ? t.getDependencyManagement() : null);
        checkChildObject(names.DISTRIBUTIONMANAGEMENT, DistributionManagement.class, "Distribution Management", t != null ? t.getDistributionManagement() : null);
        checkChildObject(names.PROPERTIES, Properties.class, "Properties", t != null ? t.getProperties() : null);

        count++;
    }

    public void visit(Parent target) {
    }

    public void visit(Organization target) {
        Organization t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(DistributionManagement target) {
        DistributionManagement t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildObject(names.DIST_REPOSITORY, DeploymentRepository.class, "Repository", t != null ? t.getRepository() : null);
        checkChildObject(names.DIST_SNAPSHOTREPOSITORY, DeploymentRepository.class, "Snapshot Repository", t != null ? t.getSnapshotRepository() : null);
        checkChildObject(names.SITE, Site.class, "Site", t != null ? t.getSite() : null);
        checkChildString(names.DOWNLOADURL, "Download Url", t != null ? t.getDownloadUrl() : null);

        count++;
    }

    public void visit(Site target) {
        Site t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, "Id", t != null ? t.getId() : null);
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(DeploymentRepository target) {
        DeploymentRepository t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, "Id", t != null ? t.getId() : null);
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);
        checkChildString(names.LAYOUT, "Layout", t != null ? t.getLayout() : null);

        count++;
    }

    public void visit(Prerequisites target) {
        Prerequisites t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.MAVEN, "Maven", t != null ? t.getMaven() : null);

        count++;
    }

    public void visit(Contributor target) {
        Contributor t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.EMAIL, "Email", t != null ? t.getEmail() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);
        checkChildString(names.ORGANIZATION, "Organization", t != null ? t.getOrganization() : null);
        checkChildString(names.ORGANIZATIONURL, "Organization Url", t != null ? t.getOrganizationUrl() : null);
        checkChildString(names.TIMEZONE, "Timezone", t != null ? t.getTimezone() : null);

        count++;    }

    public void visit(Scm target) {
        Scm t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.CONNECTION, "Connection", t != null ? t.getConnection() : null);
        checkChildString(names.DEVELOPERCONNECTION, "Developer Connection", t != null ? t.getDeveloperConnection() : null);
        checkChildString(names.TAG, "Tag", t != null ? t.getTag() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(IssueManagement target) {
        IssueManagement t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.SYSTEM, "System", t != null ? t.getSystem() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(CiManagement target) {
        CiManagement t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.SYSTEM, "System", t != null ? t.getSystem() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);

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
        checkChildString(names.ID, "Id", t != null ? t.getId() : null);
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);
        checkChildString(names.LAYOUT, "Layout", t != null ? t.getLayout() : null);
        checkChildObject(names.RELEASES, RepositoryPolicy.class, "Releases", t != null ? t.getReleases() : null);
        checkChildObject(names.SNAPSHOTS, RepositoryPolicy.class, "Snapshots", t != null ? t.getSnapshots() : null);

        count++;
    }

    public void visit(RepositoryPolicy target) {
        RepositoryPolicy t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ENABLED, "Enabled", t != null ? (t.isEnabled() != null ? t.isEnabled().toString() : null) : null);
        checkChildString(names.UPDATEPOLICY, "Update Policy", t != null ? t.getUpdatePolicy() : null);
        checkChildString(names.CHECKSUMPOLICY, "Checksum Policy", t != null ? t.getChecksumPolicy() : null);

        count++;
    }

    public void visit(Profile target) {
        Profile t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, "Id", t != null ? t.getId() : null);
        checkChildObject(names.ACTIVATION, Activation.class, "Activation", t != null ? t.getActivation() : null);
        checkChildObject(names.BUILD, BuildBase.class, "Build", t != null ? t.getBuildBase() : null);
        this.<Repository>checkListObject(names.REPOSITORIES, names.REPOSITORY,
                Repository.class, "Repositories",
                t != null ? t.getRepositories() : null,
                new KeyGenerator<Repository>() {
                    public Object generate(Repository c) {
                        return c.getId();
                    }
                    public String createName(Repository c) {
                        return c.getId() != null ? c.getId() : "Repository";
                    }
                });
        this.<Repository>checkListObject(names.PLUGINREPOSITORIES, names.PLUGINREPOSITORY,
                Repository.class, "Plugin Repositories",
                t != null ? t.getPluginRepositories() : null,
                new KeyGenerator<Repository>() {
                    public Object generate(Repository c) {
                        return c.getId();
                    }
                    public String createName(Repository c) {
                        return c.getId() != null ? c.getId() : "Repository";
                    }
                });
        checkDependencies(t);
        checkChildObject(names.REPORTING, Reporting.class, "Reporting", t != null ? t.getReporting() : null);
        checkChildObject(names.DEPENDENCYMANAGEMENT, DependencyManagement.class, "Dependency Management", t != null ? t.getDependencyManagement() : null);
        checkChildObject(names.DISTRIBUTIONMANAGEMENT, DistributionManagement.class, "Distribution Management", t != null ? t.getDistributionManagement() : null);
        checkChildObject(names.PROPERTIES, Properties.class, "Properties", t != null ? t.getProperties() : null);

        count++;
    }

    public void visit(BuildBase target) {
        BuildBase t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.DEFAULTGOAL, "Default Goal", t != null ? t.getDefaultGoal() : null);
        this.<Resource>checkListObject(names.RESOURCES, names.RESOURCE,
                Resource.class, "Resources",
                t != null ? t.getResources() : null,
                new IdentityKeyGenerator<Resource>() {
                    public String createName(Resource c) {
                        return c.getDirectory() != null ? c.getDirectory() : "Resource";
                    }
                });
        this.<Resource>checkListObject(names.TESTRESOURCES, names.TESTRESOURCE,
                Resource.class, "Test Resources",
                t != null ? t.getTestResources() : null,
                new IdentityKeyGenerator<Resource>() {
                    public String createName(Resource c) {
                        return c.getDirectory() != null ? c.getDirectory() : "Test Resource";
                    }
                });
        checkChildString(names.DIRECTORY, "Directory", t != null ? t.getDirectory() : null);
        checkChildString(names.FINALNAME, "Final Name", t != null ? t.getFinalName() : null);
        //TODO filters
        checkChildObject(names.PLUGINMANAGEMENT, PluginManagement.class, "Plugin Management", t != null ? t.getPluginManagement() : null);
        this.<Plugin>checkListObject(names.PLUGINS, names.PLUGIN,
                Plugin.class, "Plugins",
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
                        return c.getArtifactId() != null ? c.getArtifactId() : "Plugin";
                    }
                });

        count++;
    }

    public void visit(Plugin target) {
        Plugin t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.GROUPID, "GroupId", t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, "ArtifactId", t != null ? t.getArtifactId() : null);
        checkChildString(names.VERSION, "Version", t != null ? t.getVersion() : null);
        checkChildString(names.EXTENSIONS, "Extensions", t != null ? (t.isExtensions() != null ? t.isExtensions().toString() : null) : null);
        this.<PluginExecution>checkListObject(names.EXECUTIONS, names.EXECUTION,
                PluginExecution.class, "Executions",
                t != null ? t.getExecutions() : null,
                new KeyGenerator<PluginExecution>() {
                    public Object generate(PluginExecution c) {
                        return c.getId(); //NOI18N
                    }
                    public String createName(PluginExecution c) {
                        return c.getId() != null ? c.getId() : "Execution";
                    }
                });
        checkDependencies(t);
        //TODO goals.
        checkChildString(names.INHERITED, "Inherited", t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, "Configuration", t != null ? t.getConfiguration() : null);

        count++;
    }

    public void visit(Dependency target) {
        Dependency t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.GROUPID, "GroupId", t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, "ArtifactId", t != null ? t.getArtifactId() : null);
        checkChildString(names.VERSION, "Version", t != null ? t.getVersion() : null);
        checkChildString(names.TYPE, "Type", t != null ? t.getType() : null);
        checkChildString(names.CLASSIFIER, "Classifier", t != null ? t.getClassifier() : null);
        checkChildString(names.SCOPE, "Scope", t != null ? t.getScope() : null);

        this.<Exclusion>checkListObject(names.EXCLUSIONS, names.EXCLUSION,
                Exclusion.class, "Exclusions",
                t != null ? t.getExclusions() : null,
                new IdentityKeyGenerator<Exclusion>() {
                    public String createName(Exclusion c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : "Exclusion";
                    }
                });

        count++;
    }

    public void visit(Exclusion target) {
        Exclusion t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.GROUPID, "GroupId", t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, "ArtifactId", t != null ? t.getArtifactId() : null);

        count++;
    }

    public void visit(PluginExecution target) {
        PluginExecution t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, "Id", t != null ? t.getId() : null);
        checkChildString(names.PHASE, "Phase", t != null ? t.getPhase() : null);
        //TODO goals.
        checkChildString(names.INHERITED, "Inherited", t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, "Configuration", t != null ? t.getConfiguration() : null);

        count++;
    }

    public void visit(Resource target) {
        Resource t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.TARGETPATH, "Target Path", t != null ? t.getTargetPath() : null);
        //TODO filtering
        checkChildString(names.DIRECTORY, "Directory", t != null ? t.getDirectory() : null);
        //TODO includes, excludes

        count++;
    }

    public void visit(PluginManagement target) {
        PluginManagement t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        this.<Plugin>checkListObject(names.PLUGINS, names.PLUGIN,
                Plugin.class, "Plugins",
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
                        return c.getArtifactId() != null ? c.getArtifactId() : "Plugin";
                    }
                });

        count++;
    }

    public void visit(Reporting target) {
        Reporting t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.EXCLUDEDEFAULTS, "Exclude Defaults", t != null ? (t.isExcludeDefaults() != null ? t.isExcludeDefaults().toString() : null) : null);
        checkChildString(names.OUTPUTDIRECTORY, "Output Directory", t != null ? t.getOutputDirectory() : null);
        this.<ReportPlugin>checkListObject(names.REPORTPLUGINS, names.REPORTPLUGIN,
                ReportPlugin.class, "Report Plugins",
                t != null ? t.getReportPlugins() : null,
                new KeyGenerator<ReportPlugin>() {
                    public Object generate(ReportPlugin c) {
                        return c.getGroupId() + ":" + c.getArtifactId();
                    }
                    public String createName(ReportPlugin c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : "Report Plugin";
                    }
                });

        count++;
    }

    public void visit(ReportPlugin target) {
        ReportPlugin t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.GROUPID, "GroupId", t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, "ArtifactId", t != null ? t.getArtifactId() : null);
        checkChildString(names.VERSION, "Version", t != null ? t.getVersion() : null);
        checkChildString(names.INHERITED, "Inherited", t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, "Configuration", t != null ? t.getConfiguration() : null);
        this.<ReportSet>checkListObject(names.REPORTSETS, names.REPORTSET,
                ReportSet.class, "ReportSets",
                t != null ? t.getReportSets() : null,
                new KeyGenerator<ReportSet>() {
                    public Object generate(ReportSet c) {
                        return c.getId(); //NOI18N
                    }
                    public String createName(ReportSet c) {
                        return c.getId() != null ? c.getId() : "ReportSet";
                    }
                });

        count++;
    }

    public void visit(ReportSet target) {
        ReportSet t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, "Id", t != null ? t.getId() : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, "Configuration", t != null ? t.getConfiguration() : null);
        checkChildString(names.INHERITED, "Inherited", t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        //TODO reports.

        count++;
    }

    public void visit(Activation target) {
        Activation t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildObject(names.ACTIVATIONOS, ActivationOS.class, "Operating System", t != null ? t.getActivationOS() : null);
        checkChildObject(names.ACTIVATIONPROPERTY, ActivationProperty.class, "Property", t != null ? t.getActivationProperty() : null);
        checkChildObject(names.ACTIVATIONFILE, ActivationFile.class, "File", t != null ? t.getActivationFile() : null);
        checkChildObject(names.ACTIVATIONCUSTOM, ActivationCustom.class, "Custom", t != null ? t.getActivationCustom() : null);

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
        checkChildString(names.SOURCEDIRECTORY, "Source Directory", t != null ? t.getSourceDirectory() : null);
        //just ignore script directory
        checkChildString(names.TESTSOURCEDIRECTORY, "Test Source Directory", t != null ? t.getTestSourceDirectory() : null);
        checkChildString(names.OUTPUTDIRECTORY, "Output Directory", t != null ? t.getOutputDirectory() : null);
        checkChildString(names.TESTOUTPUTDIRECTORY, "Test Output Directory", t != null ? t.getTestOutputDirectory() : null);
        this.<Extension>checkListObject(names.EXTENSIONS, names.EXTENSION,
                Extension.class, "Extensions",
                t != null ? t.getExtensions() : null,
                new KeyGenerator<Extension>() {
                    public Object generate(Extension c) {
                        String gr = c.getGroupId();
                        return gr + ":" + c.getArtifactId(); //NOI18N
                    }
                    public String createName(Extension c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : "Plugin";
                    }
                });
        checkChildString(names.DEFAULTGOAL, "Default Goal", t != null ? t.getDefaultGoal() : null);
        this.<Resource>checkListObject(names.RESOURCES, names.RESOURCE,
                Resource.class, "Resources",
                t != null ? t.getResources() : null,
                new IdentityKeyGenerator<Resource>() {
                    public String createName(Resource c) {
                        return c.getDirectory() != null ? c.getDirectory() : "Resource";
                    }
                });
        this.<Resource>checkListObject(names.TESTRESOURCES, names.TESTRESOURCE,
                Resource.class, "Test Resources",
                t != null ? t.getTestResources() : null,
                new IdentityKeyGenerator<Resource>() {
                    public String createName(Resource c) {
                        return c.getDirectory() != null ? c.getDirectory() : "Test Resource";
                    }
                });
        checkChildString(names.DIRECTORY, "Directory", t != null ? t.getDirectory() : null);
        checkChildString(names.FINALNAME, "Final Name", t != null ? t.getFinalName() : null);
        //TODO filters
        checkChildObject(names.PLUGINMANAGEMENT, PluginManagement.class, "Plugin Management", t != null ? t.getPluginManagement() : null);
        this.<Plugin>checkListObject(names.PLUGINS, names.PLUGIN,
                Plugin.class, "Plugins",
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
                        return c.getArtifactId() != null ? c.getArtifactId() : "Plugin";
                    }
                });

        count++;
    }

    public void visit(Extension target) {
        Extension t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.GROUPID, "GroupId", t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, "ArtifactId", t != null ? t.getArtifactId() : null);
        checkChildString(names.VERSION, "Version", t != null ? t.getVersion() : null);
    }

    public void visit(License target) {
        License t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);
        count++;
    }

    public void visit(MailingList target) {
        MailingList t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.SUBSCRIBE, "Subscribe", t != null ? t.getSubscribe() : null);
        checkChildString(names.UNSUBSCRIBE, "Unsubscribe", t != null ? t.getUnsubscribe() : null);
        checkChildString(names.POST, "Post", t != null ? t.getPost() : null);
        checkChildString(names.ARCHIVE, "Archive", t != null ? t.getArchive() : null);
        count++;
    }

    public void visit(Developer target) {
        Developer t = target;
        if (t != null && !t.isInDocumentModel()) {
            t = null;
        }
        checkChildString(names.ID, "Id", t != null ? t.getId() : null);
        checkChildString(names.NAME, "Name", t != null ? t.getName() : null);
        checkChildString(names.EMAIL, "Email", t != null ? t.getEmail() : null);
        checkChildString(names.URL, "Url", t != null ? t.getUrl() : null);
        checkChildString(names.ORGANIZATION, "Organization", t != null ? t.getOrganization() : null);
        checkChildString(names.ORGANIZATIONURL, "Organization Url", t != null ? t.getOrganizationUrl() : null);
        checkChildString(names.TIMEZONE, "Timezone", t != null ? t.getTimezone() : null);

        count++;
    }

    public void visit(POMExtensibilityElement target) {
    }

    public void visit(ModelList target) {
    }

    public void visit(Configuration target) {
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
                Dependency.class, "Dependencies",
                container != null ? container.getDependencies() : null,
                new KeyGenerator<Dependency>() {
                    public Object generate(Dependency c) {
                        return c.getGroupId() + ":" + c.getArtifactId();
                    }
                    public String createName(Dependency c) {
                        return c.getArtifactId() != null ? c.getArtifactId() : "Dependency";
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
                dispVal = "&lt;Undefined&gt;";
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
            String dispVal = POMModelPanel.definesValue(values) ? "" : "&lt;Undefined&gt;";
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

            String dispVal = POMModelPanel.definesValue(values) ? "" : "&lt;Undefined&gt;";
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
