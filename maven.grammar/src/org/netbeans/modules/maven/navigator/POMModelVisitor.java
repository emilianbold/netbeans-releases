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
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.xml.namespace.QName;
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
import org.netbeans.modules.maven.model.pom.POMModel;
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
import org.netbeans.modules.xml.xam.Model;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkleint
 */
public class POMModelVisitor implements org.netbeans.modules.maven.model.pom.POMComponentVisitor {

    private Map<String, POMCutHolder> childs = new LinkedHashMap<String, POMCutHolder>();
    private int count = 0;
    private POMModelPanel.Configuration configuration;
    private POMCutHolder parent;

    POMModelVisitor(POMCutHolder parent, POMModelPanel.Configuration configuration) {
        this.parent = parent;
        this.configuration = configuration;
    }

    public void reset() {
         childs = new LinkedHashMap<String, POMCutHolder>();
         count = 0;
    }

    POMCutHolder[] getChildValues() {
        List<POMCutHolder> toRet = new ArrayList<POMCutHolder>();
        toRet.addAll(childs.values());
        return toRet.toArray(new POMCutHolder[0]);
    }

    public void visit(Project target) {
        Project t = target;
        if (t != null && (!t.isInDocumentModel() || !t.getModel().getState().equals(Model.State.VALID))) {
            POMModel mdl = t.getModel();
            if (!mdl.getState().equals(Model.State.VALID)) {
                try {
                    mdl.sync();
                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
                }
            }
            t = t.getModel().getProject();
        }
        //ordered by appearance in pom schema..
        POMQNames names = parent.getPOMQNames();
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
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(DistributionManagement target) {
        DistributionManagement t = target;
        POMQNames names = parent.getPOMQNames();
        checkChildObject(names.DIST_REPOSITORY, DeploymentRepository.class, NbBundle.getMessage(POMModelVisitor.class, "REPOSITORY"), t != null ? t.getRepository() : null);
        checkChildObject(names.DIST_SNAPSHOTREPOSITORY, DeploymentRepository.class, NbBundle.getMessage(POMModelVisitor.class, "SNAPSHOT_REPOSITORY"), t != null ? t.getSnapshotRepository() : null);
        checkChildObject(names.SITE, Site.class, NbBundle.getMessage(POMModelVisitor.class, "SITE"), t != null ? t.getSite() : null);
        checkChildString(names.DOWNLOADURL, NbBundle.getMessage(POMModelVisitor.class, "DOWNLOAD_URL"), t != null ? t.getDownloadUrl() : null);

        count++;
    }

    public void visit(Site target) {
        Site t = target;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.ID, NbBundle.getMessage(POMModelVisitor.class, "ID"), t != null ? t.getId() : null);
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(DeploymentRepository target) {
        DeploymentRepository t = target;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.ID, NbBundle.getMessage(POMModelVisitor.class, "ID"), t != null ? t.getId() : null);
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);
        checkChildString(names.LAYOUT, NbBundle.getMessage(POMModelVisitor.class, "LAYOUT"), t != null ? t.getLayout() : null);

        count++;
    }

    public void visit(Prerequisites target) {
        Prerequisites t = target;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.MAVEN, NbBundle.getMessage(POMModelVisitor.class, "MAVEN"), t != null ? t.getMaven() : null);

        count++;
    }

    public void visit(Contributor target) {
        Contributor t = target;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.EMAIL, NbBundle.getMessage(POMModelVisitor.class, "EMAIL"), t != null ? t.getEmail() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);
        checkChildString(names.ORGANIZATION, NbBundle.getMessage(POMModelVisitor.class, "ORGANIZATION"), t != null ? t.getOrganization() : null);
        checkChildString(names.ORGANIZATIONURL, NbBundle.getMessage(POMModelVisitor.class, "ORGANIZATION_URL"), t != null ? t.getOrganizationUrl() : null);
        checkChildString(names.TIMEZONE, NbBundle.getMessage(POMModelVisitor.class, "TIMEZONE"), t != null ? t.getTimezone() : null);

        count++;
    }

    public void visit(Scm target) {
        Scm t = target;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.CONNECTION, NbBundle.getMessage(POMModelVisitor.class, "CONNECTION"), t != null ? t.getConnection() : null);
        checkChildString(names.DEVELOPERCONNECTION, NbBundle.getMessage(POMModelVisitor.class, "DEVELOPER_CONNECTION"), t != null ? t.getDeveloperConnection() : null);
        checkChildString(names.TAG, NbBundle.getMessage(POMModelVisitor.class, "TAG"), t != null ? t.getTag() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(IssueManagement target) {
        IssueManagement t = target;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.SYSTEM, NbBundle.getMessage(POMModelVisitor.class, "SYSTEM"), t != null ? t.getSystem() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(CiManagement target) {
        CiManagement t = target;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.SYSTEM, NbBundle.getMessage(POMModelVisitor.class, "SYSTEM"), t != null ? t.getSystem() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);

        count++;
    }

    public void visit(Notifier target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void visit(Repository target) {
        Repository t = target;
        POMQNames names = parent.getPOMQNames();
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
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.ENABLED, NbBundle.getMessage(POMModelVisitor.class, "ENABLED"), t != null ? (t.isEnabled() != null ? t.isEnabled().toString() : null) : null);
        checkChildString(names.UPDATEPOLICY, NbBundle.getMessage(POMModelVisitor.class, "UPDATE_POLICY"), t != null ? t.getUpdatePolicy() : null);
        checkChildString(names.CHECKSUMPOLICY, NbBundle.getMessage(POMModelVisitor.class, "CHECKSUM_POLICY"), t != null ? t.getChecksumPolicy() : null);

        count++;
    }

    public void visit(Profile target) {
        Profile t = target;
        POMQNames names = parent.getPOMQNames();
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
        POMQNames names = parent.getPOMQNames();
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
        POMQNames names = parent.getPOMQNames();
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
        checkStringListObject(names.GOALS, names.GOAL, org.openide.util.NbBundle.getMessage(POMModelVisitor.class, "GOALS"), t != null ? t.getGoals() : null);
        checkChildString(names.INHERITED, NbBundle.getMessage(POMModelVisitor.class, "INHERITED"), t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, NbBundle.getMessage(POMModelVisitor.class, "CONFIGURATION"), t != null ? t.getConfiguration() : null);

        count++;
    }

    public void visit(Dependency target) {
        Dependency t = target;
        POMQNames names = parent.getPOMQNames();
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
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.GROUPID, NbBundle.getMessage(POMModelVisitor.class, "GROUPID"), t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, NbBundle.getMessage(POMModelVisitor.class, "ARTIFACTID"), t != null ? t.getArtifactId() : null);

        count++;
    }

    public void visit(PluginExecution target) {
        PluginExecution t = target;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.ID, NbBundle.getMessage(POMModelVisitor.class, "ID"), t != null ? t.getId() : null);
        checkChildString(names.PHASE, NbBundle.getMessage(POMModelVisitor.class, "PHASE"), t != null ? t.getPhase() : null);
        checkChildString(names.INHERITED, NbBundle.getMessage(POMModelVisitor.class, "INHERITED"), t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, NbBundle.getMessage(POMModelVisitor.class, "CONFIGURATION"), t != null ? t.getConfiguration() : null);

        count++;
    }

    public void visit(Resource target) {
        Resource t = target;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.TARGETPATH, NbBundle.getMessage(POMModelVisitor.class, "TARGET_PATH"), t != null ? t.getTargetPath() : null);
        //TODO filtering
        checkChildString(names.DIRECTORY, NbBundle.getMessage(POMModelVisitor.class, "DIRECTORY"), t != null ? t.getDirectory() : null);
        checkStringListObject(names.INCLUDES, names.INCLUDE, org.openide.util.NbBundle.getMessage(POMModelVisitor.class, "INCLUDES"), t != null ? t.getIncludes() : null);
        checkStringListObject(names.EXCLUDES, names.EXCLUDE, org.openide.util.NbBundle.getMessage(POMModelVisitor.class, "EXCLUDES"), t != null ? t.getExcludes() : null);

        count++;
    }

    public void visit(PluginManagement target) {
        PluginManagement t = target;
        POMQNames names = parent.getPOMQNames();
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
        POMQNames names = parent.getPOMQNames();
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
        POMQNames names = parent.getPOMQNames();
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
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.ID, NbBundle.getMessage(POMModelVisitor.class, "ID"), t != null ? t.getId() : null);
        checkChildObject(names.CONFIGURATION, Configuration.class, NbBundle.getMessage(POMModelVisitor.class, "CONFIGURATION"), t != null ? t.getConfiguration() : null);
        checkChildString(names.INHERITED, NbBundle.getMessage(POMModelVisitor.class, "INHERITED"), t != null ? (t.isInherited() != null ? t.isInherited().toString() : null) : null);
        checkStringListObject(names.REPORTS, names.REPORT, org.openide.util.NbBundle.getMessage(POMModelVisitor.class, "REPORTS"), t != null ? t.getReports() : null);

        count++;
    }

    public void visit(Activation target) {
        Activation t = target;
        POMQNames names = parent.getPOMQNames();
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
        checkDependencies(t);

        count++;
    }

    public void visit(Build target) {
        Build t = target;
        POMQNames names = parent.getPOMQNames();
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
                        return c.getArtifactId() != null ? c.getArtifactId() : org.openide.util.NbBundle.getMessage(POMModelVisitor.class, "EXTENSION");
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
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.GROUPID, NbBundle.getMessage(POMModelVisitor.class, "GROUPID"), t != null ? t.getGroupId() : null);
        checkChildString(names.ARTIFACTID, NbBundle.getMessage(POMModelVisitor.class, "ARTIFACTID"), t != null ? t.getArtifactId() : null);
        checkChildString(names.VERSION, NbBundle.getMessage(POMModelVisitor.class, "VERSION"), t != null ? t.getVersion() : null);
    }

    public void visit(License target) {
        License t = target;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.URL, NbBundle.getMessage(POMModelVisitor.class, "URL"), t != null ? t.getUrl() : null);
        count++;
    }

    public void visit(MailingList target) {
        MailingList t = target;
        POMQNames names = parent.getPOMQNames();
        checkChildString(names.NAME, NbBundle.getMessage(POMModelVisitor.class, "NAME"), t != null ? t.getName() : null);
        checkChildString(names.SUBSCRIBE, NbBundle.getMessage(POMModelVisitor.class, "SUBSCRIBE"), t != null ? t.getSubscribe() : null);
        checkChildString(names.UNSUBSCRIBE, NbBundle.getMessage(POMModelVisitor.class, "UNSUBSCRIBE"), t != null ? t.getUnsubscribe() : null);
        checkChildString(names.POST, NbBundle.getMessage(POMModelVisitor.class, "POST"), t != null ? t.getPost() : null);
        checkChildString(names.ARCHIVE, NbBundle.getMessage(POMModelVisitor.class, "ARCHIVE"), t != null ? t.getArchive() : null);
        count++;
    }

    public void visit(Developer target) {
        Developer t = target;
        POMQNames names = parent.getPOMQNames();
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
        if (t != null) {
            doVisit(t.getAnyElements());
        }

        count++;
        
        for (POMCutHolder prop : childs.values()) {
            growToSize(count, prop);
        }
    }

    public void visit(ModelList target) {
    }

    public void visit(Configuration target) {
        Configuration t = target;
        if (t != null) {
            doVisit(t.getConfigurationElements());
        }

        count++;

        for (POMCutHolder prop : childs.values()) {
            growToSize(count, prop);
        }

    }

    private void doVisit(List<POMExtensibilityElement> elems) {
        for (POMExtensibilityElement el : elems) {
            List<POMExtensibilityElement> any = el.getAnyElements();
            if (any != null && !any.isEmpty()) {
                POMCutHolder nd = childs.get(el.getQName().getLocalPart());
                if (nd == null) {
                    nd = new SingleObjectCH(parent, el.getQName(), el.getQName().getLocalPart(), POMExtensibilityElement.class, configuration);
                    childs.put(el.getQName().getLocalPart(), nd);
                }
                fillValues(count, nd, el);
            } else {
                POMCutHolder nd = childs.get(el.getQName().getLocalPart());
                if (nd == null) {
                    nd = new SingleFieldCH(parent, el.getQName(), el.getQName().getLocalPart());
                    childs.put(el.getQName().getLocalPart(), nd);
                }
                fillValues(count, nd, el.getElementText());
            }
        }

    }

    public void visit(Properties target) {
        Properties t = target;
        if (t != null) {
            Map<String, String> props = t.getProperties();
            for (Map.Entry<String, String> ent : props.entrySet()) {
                POMCutHolder nd = childs.get(ent.getKey());
                if (nd == null) {
                    nd = new SingleFieldCH(parent, ent.getKey());
                    childs.put(ent.getKey(), nd);
                }
                fillValues(count, nd, ent.getValue());
            }
        }

        count++;
        
        for (POMCutHolder prop : childs.values()) {
            growToSize(count, prop);
        }


    }

    public void visit(StringList target) {
    }


    @SuppressWarnings("unchecked")
    private void checkChildString(POMQName qname, String displayName, String value) {
        POMCutHolder nd = childs.get(qname.getName());
        if (nd == null) {
            nd = new SingleFieldCH(parent, qname, displayName);
            childs.put(qname.getName(), nd);
        }
        fillValues(count, nd, value);
    }

    private void checkChildObject(POMQName qname, Class type, String displayName, POMComponent value) {
        POMCutHolder  nd = childs.get(qname.getName());
        if (nd == null) {
            nd = new SingleObjectCH(parent, qname, displayName, type, configuration);
            childs.put(qname.getName(), nd);
        }
        fillValues(count, nd, value);
    }


    private <T extends POMComponent> void checkListObject(POMQName qname, POMQName childName, Class type, String displayName, List<T> values, KeyGenerator<T> keygen) {
        POMCutHolder nd = childs.get(qname.getName());
        if (nd == null) {
            nd = new ListObjectCH<T>(parent, qname, childName, type, keygen, displayName, configuration);
            childs.put(qname.getName(), nd);
        }
        fillValues(count, nd, values);
    }

    private void checkStringListObject(POMQName qname, POMQName childName, String displayName, List<String> values) {
        POMCutHolder nd = childs.get(qname.getName());
        if (nd == null) {
            nd =  new ListStringCH(parent, qname, childName, displayName, configuration);
            childs.put(qname.getName(), nd);
        }
        fillValues(count, nd, values);
    }



    private static void fillValues(int current, POMCutHolder cutHolder, Object value) {
        growToSize(current, cutHolder);
        cutHolder.addCut(value);
    }

    private static void growToSize(int count, POMCutHolder cutHolder) {
        while (cutHolder.getCutsSize() < count) {
            cutHolder.addCut(null);
        }
    }

    private void checkDependencies(DependencyContainer container) {
        POMQNames names = parent.getPOMQNames();
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


    private interface KeyGenerator<T extends POMComponent> {
        Object generate(T c);

        String createName(T c);
    }

    private abstract class IdentityKeyGenerator<T extends POMComponent> implements  KeyGenerator<T> {
        public Object generate(T c) {
            return c;
        }

    }

    abstract static class POMCutHolder {
        private List cuts = new ArrayList();

        POMCutHolder parent;
        private POMModel[] models;
        private POMQNames names;

        protected POMCutHolder(POMModel[] source, POMQNames names) {
            models = source;
            this.names = names;
        }

        protected POMCutHolder(POMCutHolder parent) {
            this.parent = parent;
        }

        public POMModel[] getSource() {
            if (models != null) {
                return models;
            }
            if (parent != null) {
                return parent.getSource();
            }
            throw new IllegalStateException();
        }

        public POMQNames getPOMQNames() {
            if (names != null) {
                return names;
            }
            if (parent != null) {
                return parent.getPOMQNames();
            }
            throw new IllegalStateException();
        }



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

        abstract Node createNode();
    }

    private static class SingleFieldCH extends POMCutHolder {
        private Object qname;
        private String display;

        private SingleFieldCH(POMCutHolder parent, POMQName qname, String displayName) {
            super(parent);
            this.qname = qname;
            this.display = displayName;
        }

        private SingleFieldCH(POMCutHolder parent, QName qname, String displayName) {
            super(parent);
            this.qname = qname;
            this.display = displayName;
        }

        private SingleFieldCH(POMCutHolder parent, String displayName) {
            super(parent);
            this.qname = displayName;
            this.display = displayName;
        }


        @Override
        Node createNode() {
            return new SingleFieldNode(Lookups.fixed(this, qname), display);
        }
    }

    static class SingleObjectCH extends POMCutHolder {
        private Object qname;
        private String display;
        private Class type;
        private POMModelPanel.Configuration configuration;

        SingleObjectCH(POMModel[] models, POMQNames names, POMQName qname, Class type, POMModelPanel.Configuration config) {
            super(models, names);
            this.qname = qname;
            this.display = "root"; //NOI18N
            this.type = type;
            this.configuration = config;
        }

        private SingleObjectCH(POMCutHolder parent, POMQName qname, String displayName, Class type, POMModelPanel.Configuration config) {
            super(parent);
            this.qname = qname;
            this.display = displayName;
            this.type = type;
            this.configuration = config;
        }

        private SingleObjectCH(POMCutHolder parent, QName qname, String displayName, Class type, POMModelPanel.Configuration config) {
            super(parent);
            this.qname = qname;
            this.display = displayName;
            this.type = type;
            this.configuration = config;
        }

        private SingleObjectCH(POMCutHolder parent, POMQName qname, String displayName) {
            super(parent);
            this.qname = qname;
            this.display = displayName;
        }


        @Override
        Node createNode() {
            if (type == null) {
                return new ObjectNode(Lookups.fixed(this, qname), Children.LEAF, display);
            }
            return new ObjectNode(Lookups.fixed(this, qname), new PomChildren(this, getPOMQNames(), type, configuration), display);
        }
    }

    static class ListObjectCH<T extends POMComponent> extends POMCutHolder {
        private POMQName qname;
        private POMQName childName;
        private String displayName;
        private Class type;
        private KeyGenerator<T> keygen;
        private POMModelPanel.Configuration configuration;

        private ListObjectCH(POMCutHolder parent, POMQName qname, POMQName childName, Class type, KeyGenerator<T> keygen, String displayName, POMModelPanel.Configuration configuration) {
            super(parent);
            this.qname = qname;
            this.childName = childName;
            this.displayName = displayName;
            this.type = type;
            this.keygen = keygen;
            this.configuration = configuration;
        }

        Class getListClass() {
            return type;
        }

        @Override
        Node createNode() {
            return new ListNode(Lookups.fixed(this, qname), new PomListChildren<T>(this, getPOMQNames(), type, keygen, configuration, childName), displayName);
        }

    }

    private static class ListStringCH extends POMCutHolder {
        private POMQName qname;
        private String display;
        private POMQName childName;
        private POMModelPanel.Configuration configuration;

        private ListStringCH(POMCutHolder parent, POMQName qname, POMQName childName, String displayName, POMModelPanel.Configuration configuration) {
            super(parent);
            this.qname = qname;
            this.display = displayName;
            this.childName = childName;
            this.configuration = configuration;
        }

        @Override
        Node createNode() {
            return new ListNode(Lookups.fixed(this, qname), new PomStringListChildren(this, childName), display);
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
        public Action[] getActions(boolean context) {
            return new Action[] {
                new SelectAction(this)
            };
        }

        @Override
        public Action getPreferredAction() {
            return new SelectAction(this, 0);
        }


        @Override
        public String getShortDescription() {
            String[] values = getLookup().lookup(POMCutHolder.class).getCutValuesAsString();
            POMModel[] mdls = getLookup().lookup(POMCutHolder.class).getSource();
            assert values.length == mdls.length : "Values (len=" + values.length + ") don't match models (len=" + mdls.length + ")." +  Arrays.toString(values);
            StringBuffer buff = new StringBuffer();
            int index = 0;
            buff.append("<html>" + //NOI18N
                    NbBundle.getMessage(POMModelVisitor.class, "TOOLTIP_Defined_in") +
                    "<p><table><thead><tr><th>" + //NOI18N
                    NbBundle.getMessage(POMModelVisitor.class, "TOOLTIP_ArtifactId") +
                    "</th><th>" + //NOI18N
                    NbBundle.getMessage(POMModelVisitor.class, "TOOLTIP_Value") +
                    "</th></tr></thead><tbody>"); //NOI18N
            for (POMModel mdl : mdls) {
                String artifact = mdl.getProject().getArtifactId();
                buff.append("<tr><td>"); //NOI18N
                buff.append(artifact != null ? artifact : "project");
                buff.append("</td><td>"); //NOI18N
                buff.append(values[index] != null ? values[index] : org.openide.util.NbBundle.getMessage(POMModelVisitor.class, "UNDEFINED"));
                buff.append("</td></tr>");//NOI18N
                index++;
            }
            buff.append("</tbody></table>");//NOI18N

            return buff.toString();
        }


        @Override
        public String getHtmlDisplayName() {
            String[] values = getLookup().lookup(POMCutHolder.class).getCutValuesAsString();

            String dispVal = POMModelPanel.getValidValue(values);
            if (dispVal == null) {
                dispVal = NbBundle.getMessage(POMModelVisitor.class, "UNDEFINED");
            }
            boolean override = POMModelPanel.overridesParentValue(values);
            String overrideStart = override ? "<b>" : ""; //NOI18N
            String overrideEnd = override ? "</b>" : ""; //NOI18N
            boolean inherited = !POMModelPanel.isValueDefinedInCurrent(values);
            String inheritedStart = inherited ? "<i>" : ""; //NOI18N
            String inheritedEnd = inherited ? "</i>" : ""; //NOI18N

            String message = "<html>" + //NOI18N
                    inheritedStart + overrideStart +
                    key + " : " + dispVal + //NOI18N
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
        public Action[] getActions(boolean context) {
            return new Action[] {
                new SelectAction(this)
            };
        }

        @Override
        public Action getPreferredAction() {
            return new SelectAction(this, 0);
        }


        @Override
        public String getHtmlDisplayName() {
            Object[] values = getLookup().lookup(POMCutHolder.class).getCutValues();
            String dispVal = POMModelPanel.definesValue(values) ? "" : NbBundle.getMessage(POMModelVisitor.class, "UNDEFINED");
            boolean override = POMModelPanel.overridesParentValue(values);
            String overrideStart = override ? "<b>" : ""; //NOI18N
            String overrideEnd = override ? "</b>" : ""; //NOI18N
            boolean inherited = !POMModelPanel.isValueDefinedInCurrent(values);
            String inheritedStart = inherited ? "<i>" : ""; //NOI18N
            String inheritedEnd = inherited ? "</i>" : ""; //NOI18N

            String message = "<html>" + //NOI18N
                    inheritedStart + overrideStart +
                    key + " " + dispVal + //NOI18N
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

        @Override
        public String getShortDescription() {
            Object[] values = getLookup().lookup(POMCutHolder.class).getCutValues();
            POMModel[] mdls = getLookup().lookup(POMCutHolder.class).getSource();
            StringBuffer buff = new StringBuffer();
            int index = 0;
            buff.append("<html>" + //NOI18N
                    NbBundle.getMessage(POMModelVisitor.class, "TOOLTIP_Defined_in") +
                    "<p><table><thead><tr><th>" + //NOI18N
                    NbBundle.getMessage(POMModelVisitor.class, "TOOLTIP_ArtifactId") +
                    "</th><th>" + //NOI18N
                    NbBundle.getMessage(POMModelVisitor.class, "TOOLTIP_IS_DEFINED") +
                    "</th></tr></thead><tbody>"); //NOI18N
            for (POMModel mdl : mdls) {
                String artifact = mdl.getProject().getArtifactId();
                buff.append("<tr><td>"); //NOI18N
                buff.append(artifact != null ? artifact : "project");
                buff.append("</td><td>"); //NOI18N
                buff.append(values[index] != null ? org.openide.util.NbBundle.getMessage(POMModelVisitor.class, "TOOLTIP_YES") : org.openide.util.NbBundle.getMessage(POMModelVisitor.class, "TOOLTIP_NO"));
                buff.append("</td></tr>");//NOI18N
                index++;
            }
            buff.append("</tbody></table>");//NOI18N

            return buff.toString();
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
        public Action[] getActions(boolean context) {
            return new Action[] {
                new SelectAction(this)
            };
        }

        @Override
        public Action getPreferredAction() {
            return new SelectAction(this, 0);
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
        public String getShortDescription() {
            Object[] values = getLookup().lookup(POMCutHolder.class).getCutValues();
            POMModel[] mdls = getLookup().lookup(POMCutHolder.class).getSource();
            StringBuffer buff = new StringBuffer();
            int index = 0;
            buff.append("<html>" + //NOI18N
                    NbBundle.getMessage(POMModelVisitor.class, "TOOLTIP_Defined_in") +
                    "<p><table><thead><tr><th>" + //NOI18N
                    NbBundle.getMessage(POMModelVisitor.class, "TOOLTIP_ArtifactId") +
                    "</th><th>" + //NOI18N
                    NbBundle.getMessage(POMModelVisitor.class, "TOOLTIP_IS_DEFINED") +
                    "</th></tr></thead><tbody>"); //NOI18N
            for (POMModel mdl : mdls) {
                String artifact = mdl.getProject().getArtifactId();
                buff.append("<tr><td>"); //NOI18N
                buff.append(artifact != null ? artifact : "project");
                buff.append("</td><td>"); //NOI18N
                buff.append(values[index] != null ? NbBundle.getMessage(POMModelVisitor.class, "TOOLTIP_YES") : NbBundle.getMessage(POMModelVisitor.class, "TOOLTIP_NO"));
                buff.append("</td></tr>");//NOI18N
                index++;
            }
            buff.append("</tbody></table>");//NOI18N

            return buff.toString();
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

    static class PomChildren extends Children.Keys<POMCutHolder> implements PropertyChangeListener {
        private POMCutHolder parentHolder;
        private POMQNames names;
        private POMModelVisitor visitor;
        private Class type;
        private POMModelPanel.Configuration configuration;
        private List<POMCutHolder> children;

        public PomChildren(POMCutHolder parent, POMQNames names, Class type, POMModelPanel.Configuration config) {
            this.parentHolder = parent;
            this.names = names;
            this.type = type;
            this.configuration = config;
        }

        private void reshow() {
            List<POMCutHolder> childs = children;
            if (childs != null) {
                for (POMCutHolder h : childs) {
                    if (!POMModelPanel.definesValue(h.getCutValues())) {
                        refreshKey(h);
                    }
                }
            }
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(rescan(new POMModelVisitor(parentHolder, configuration)));
            configuration.addPropertyChangeListener(this);
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            children = null;
            configuration.removePropertyChangeListener(this);

        }

        private List<POMCutHolder> rescan(POMModelVisitor visitor) {
            try {
                Method m = POMModelVisitor.class.getMethod("visit", type); //NOI18N
                for (Object comp : parentHolder.getCutValues()) {
                    try {
                        m.invoke(visitor, comp);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            children = Arrays.asList(visitor.getChildValues());
            return children;
        }

        @Override
        protected Node[] createNodes(POMCutHolder childkey) {
            if (configuration.isFilterUndefined() && !POMModelPanel.definesValue(childkey.getCutValues())) {
                return new Node[0];
            }
            return new Node[] {childkey.createNode()};
        }

        public void propertyChange(PropertyChangeEvent evt) {
            reshow();
        }


    }

    static class PomListChildren<T extends POMComponent> extends Children.Keys<Object> {
        private Object[] one = new Object[] {new Object()};
        private POMCutHolder holder;
        private POMQNames names;
        private Class type;
        private KeyGenerator<T> keyGenerator;
        private POMQName childName;
        private POMModelPanel.Configuration configuration;
        public PomListChildren(POMCutHolder holder, POMQNames names, Class type, KeyGenerator<T> generator, POMModelPanel.Configuration configuration, POMQName childName) {
            setKeys(one);
            this.holder = holder;
            this.names = names;
            this.type = type;
            this.keyGenerator = generator;
            this.childName = childName;
            this.configuration = configuration;
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
                T topMost = null;
                for (T c : lst) {
                    if (topMost == null) {
                        topMost = c;
                    }
                }

                String itemName = keyGenerator.createName(topMost);
                POMCutHolder cutHolder = new SingleObjectCH(holder, childName, itemName, type, configuration);
                for (T c : lst) {
                    cutHolder.addCut(c);
                }
                growToSize(holder.getCutsSize(), cutHolder);

                toRet.add(new ObjectNode(Lookups.fixed(cutHolder, childName), new PomChildren(cutHolder, names, type, configuration), itemName));
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

    static class PomStringListChildren extends Children.Keys<Object> {
        private Object[] one = new Object[] {new Object()};
        private POMCutHolder holder;
        private POMQName childName;
        public PomStringListChildren(POMCutHolder holder, POMQName childName) {
            setKeys(one);
            this.holder = holder;
            this.childName = childName;
        }

        public void reshow() {
            this.refreshKey(one);
        }

        @Override
        protected Node[] createNodes(Object key) {
            List<Node> toRet = new ArrayList<Node>();
            LinkedHashMap<String, List<String>> cut = new LinkedHashMap<String, List<String>>();

            int level = 0;
            for (Object comp : holder.getCutValues()) {
                if (comp == null) {
                    level++;
                    continue;
                }
                @SuppressWarnings("unchecked")
                List<String> lst = (List<String>) comp;
                for (String c : lst) {
                    List<String> currentCut = cut.get(c);
                    if (currentCut == null) {
                        currentCut = new ArrayList<String>();
                        cut.put(c, currentCut);
                    }
                    fillValues(level, currentCut, c);
                }
                level++;
            }
            for (List<String> lst : cut.values()) {
                String topMost = null;
                for (String c : lst) {
                    if (topMost == null) {
                        topMost = c;
                    }
                }
                POMCutHolder cutHolder = new SingleObjectCH(holder, childName, topMost);
                for (String c : lst) {
                    cutHolder.addCut(c);
                }
                growToSize(holder.getCutsSize(), cutHolder);
            }

            return toRet.toArray(new Node[0]);
        }

        private void fillValues(int current, List<String> list, String value) {
            while (list.size() < current) {
                list.add(null);
            }
            list.add(value);
        }

    }


    static class SelectAction extends AbstractAction implements Presenter.Popup {
        private Node node;
        private int layer = -1;

        SelectAction(Node node) {
            this.node = node;
        }

        SelectAction(Node node, int layer) {
            this.node = node;
            this.layer = layer;
        }
        public void actionPerformed(ActionEvent e) {
            if (layer != -1) {
                POMModelPanel.selectByNode(node, null, layer);
            }
        }

        public JMenuItem getPopupPresenter() {
            JMenu menu = new JMenu();
            menu.setText(org.openide.util.NbBundle.getMessage(POMModelVisitor.class, "ACT_Show"));
            POMCutHolder pch = node.getLookup().lookup(POMCutHolder.class);
            POMModel[] mdls = pch.getSource();
            Object[] val = pch.getCutValues();
            int index = 0;
            for (POMModel mdl : mdls) {
                String artifact = mdl.getProject().getArtifactId();
                JMenuItem item = new JMenuItem();
                item.setAction(new SelectAction(node, index));
                if (index == 0) {
                    item.setText(NbBundle.getMessage(POMModelVisitor.class, "ACT_Current", artifact != null ? artifact : "project"));
                } else {
                    item.setText(NbBundle.getMessage(POMModelVisitor.class, "ACT_PARENT", artifact != null ? artifact : "project"));
                }
                item.setEnabled(val[index] != null);
                menu.add(item);
                index++;
            }
            return menu;
        }

    }

}
