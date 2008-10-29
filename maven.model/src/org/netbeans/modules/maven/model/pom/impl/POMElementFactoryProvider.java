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

import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.pom.*;
import org.netbeans.modules.maven.model.pom.spi.ElementFactory;
import org.netbeans.modules.maven.model.pom.spi.POMExtensibilityElementBase;
import org.netbeans.modules.maven.model.pom.visitor.DefaultVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class POMElementFactoryProvider implements ElementFactory {

    public Set<QName> getElementQNames() {
        return POMQName.getQNames();
    }

    public POMComponent create(POMComponent context, Element element) {
        return new POMComponentCreateVisitor().create(element, context);
    }
}

class POMComponentCreateVisitor extends DefaultVisitor {
    private Element element;
    private POMComponent created;
        
    public POMComponent create(Element element, POMComponent context) {
        this.element = element;
        context.accept(this);
        return created;
    }

    private boolean isElementQName(POMQName q) {
        return areSameQName(q, element);
    }
      
    public static boolean areSameQName(POMQName q, Element e) {
        return q.getQName().equals(AbstractDocumentComponent.getQName(e));
    }
/* 
    private boolean isForeignElement() {
        return !POMQName.NS_URI.equals(AbstractDocumentComponent.getQName(element).getNamespaceURI());
    }

    private void createExtensibilityElement(SCAComponent context) {
        assert isForeignElement();
        created = new POMExtensibilityElementImpl(context.getModel(), element);
    }
*/
    @Override
    public void visit(Project context) {
        if (isElementQName(POMQName.PARENT)) {
            created = new ParentImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PREREQUISITES)) {
            created = new PrerequisitesImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.ISSUEMANAGEMENT)) {
            created = new IssueManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.CIMANAGEMENT)) {
            created = new CiManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.MAILINGLISTS)) {
            created = new MailingListImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.DEVELOPERS)) {
            created = new DeveloperImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.CONTRIBUTORS)) {
            created = new DeveloperImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.LICENSES)) {
            created = new LicenseImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.SCM)) {
            created = new ScmImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.ORGANIZATION)) {
            created = new OrganizationImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.BUILD)) {
            created = new BuildImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PROFILES)) {
            created = new ProfileImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.MODULES)) {
            created = new StringListImpl(context.getModel(), element, POMQName.MODULE);
            return;
        }

        if (isElementQName(POMQName.REPOSITORIES)) {
            created = new RepositoryImpl.RepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PLUGINREPOSITORIES)) {
            created = new RepositoryImpl.PluginRepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.DEPENDENCIES)) {
            created = new DependencyImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.REPORTING)) {
            created = new ReportingImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.DEPENDENCYMANAGEMENT)) {
            created = new DependencyManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.DISTRIBUTIONMANAGEMENT)) {
            created = new DistributionManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PROPERTIES)) {
            created = new PropertiesImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Parent context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Organization context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(DistributionManagement context) {
        if (isElementQName(POMQName.DIST_REPOSITORY)) {
            created = new DeploymentRepositoryImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.DIST_SNAPSHOTREPOSITORY)) {
            created = new DeploymentRepositoryImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.SITE)) {
            created = new SiteImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Site context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(DeploymentRepository context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Prerequisites context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Contributor context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Scm context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(IssueManagement context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(CiManagement context) {
        if (isElementQName(POMQName.NOTIFIER)) {
            created = new NotifierImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Notifier context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Repository context) {
        if (isElementQName(POMQName.RELEASES)) {
            created = new RepositoryPolicyImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.SNAPSHOTS)) {
            created = new RepositoryPolicyImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(RepositoryPolicy context) {
        //createExtensibilityElement(context);
    }


    @Override
    public void visit(Profile context) {
        if (isElementQName(POMQName.ACTIVATION)) {
            created = new ActivationImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.BUILD)) {
            created = new BuildBaseImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.MODULES)) {
            created = new StringListImpl(context.getModel(), element, POMQName.MODULE);
            return;
        }

        if (isElementQName(POMQName.REPOSITORIES)) {
            created = new RepositoryImpl.RepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PLUGINREPOSITORIES)) {
            created = new RepositoryImpl.PluginRepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.DEPENDENCIES)) {
            created = new DependencyImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.REPORTING)) {
            created = new ReportingImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.DEPENDENCYMANAGEMENT)) {
            created = new DependencyManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.DISTRIBUTIONMANAGEMENT)) {
            created = new DistributionManagementImpl(context.getModel(), element);
            return;
        }
        
        if (isElementQName(POMQName.PROPERTIES)) {
            created = new PropertiesImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(BuildBase context) {
        if (isElementQName(POMQName.RESOURCES)) {
            created = new ResourceImpl.ResList(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.TESTRESOURCES)) {
            created = new ResourceImpl.TestResList(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PLUGINMANAGEMENT)) {
            created = new PluginManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PLUGINS)) {
            created = new PluginImpl.List(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Plugin context) {
        if (isElementQName(POMQName.EXECUTIONS)) {
            created = new PluginExecutionImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.DEPENDENCIES)) {
            created = new DependencyImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.CONFIGURATION)) {
            created = new ConfigurationImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.GOALS)) {
            created = new StringListImpl(context.getModel(), element, POMQName.GOAL);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(StringList context) {
        created = new POMExtensibilityElementBase(context.getModel(), element);
    }

    @Override
    public void visit(Dependency context) {
        if (isElementQName(POMQName.EXCLUSIONS)) {
            created = new ExclusionImpl.List(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Exclusion context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(PluginExecution context) {
        if (isElementQName(POMQName.GOALS)) {
            created = new StringListImpl(context.getModel(), element, POMQName.GOAL);
            return;
        }
        if (isElementQName(POMQName.CONFIGURATION)) {
            created = new ConfigurationImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Resource context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(PluginManagement context) {
        if (isElementQName(POMQName.PLUGINS)) {
            created = new PluginImpl.List(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Reporting context) {
        if (isElementQName(POMQName.REPORTPLUGINS)) {
            created = new ReportPluginImpl.List(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(ReportPlugin context) {
        if (isElementQName(POMQName.REPORTSET)) {
            created = new ReportSetImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(POMQName.CONFIGURATION)) {
            created = new ConfigurationImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(ReportSet context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Activation context) {
        if (isElementQName(POMQName.ACTIVATIONOS)) {
            created = new ActivationOSImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.ACTIVATIONPROPERTY)) {
            created = new ActivationPropertyImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.ACTIVATIONFILE)) {
            created = new ActivationFileImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.ACTIVATIONCUSTOM)) {
            created = new ActivationCustomImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(ActivationProperty context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(ActivationOS context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(ActivationFile context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(ActivationCustom context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(DependencyManagement context) {
        if (isElementQName(POMQName.DEPENDENCIES)) {
            created = new DependencyImpl.List(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Build context) {
        visit((BuildBase) context);

        if (isElementQName(POMQName.EXTENSIONS)) {
            created = new ExtensionImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.RESOURCES)) {
            created = new ResourceImpl.ResList(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.TESTRESOURCE)) {
            created = new ResourceImpl.TestResList(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PLUGINMANAGEMENT)) {
            created = new PluginManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PLUGINS)) {
            created = new PluginImpl.List(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Extension context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(License context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(MailingList context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Developer context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(ModelList context) {
        if (isElementQName(POMQName.MAILINGLIST) && context.getListClass().equals(MailingList.class)) {
            created = new MailingListImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(POMQName.DEPENDENCY) && context.getListClass().equals(Dependency.class)) {
            created = new DependencyImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(POMQName.DEVELOPER) && context.getListClass().equals(Developer.class)) {
            created = new DeveloperImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(POMQName.CONTRIBUTOR) && context.getListClass().equals(Contributor.class)) {
            created = new ContributorImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(POMQName.LICENSE) && context.getListClass().equals(License.class)) {
            created = new LicenseImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(POMQName.PROFILE) && context.getListClass().equals(Profile.class)) {
            created = new ProfileImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(POMQName.REPOSITORY) && context.getListClass().equals(Repository.class)) {
            created = new RepositoryImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(POMQName.PLUGINREPOSITORY) && context.getListClass().equals(Repository.class)) {
            created = new RepositoryImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(POMQName.EXCLUSION) && context.getListClass().equals(Exclusion.class)) {
            created = new ExclusionImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(POMQName.PLUGIN) && context.getListClass().equals(Plugin.class)) {
            created = new PluginImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(POMQName.EXTENSION) && context.getListClass().equals(Extension.class)) {
            created = new ExtensionImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(POMQName.EXECUTION) && context.getListClass().equals(PluginExecution.class)) {
            created = new PluginExecutionImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(POMQName.RESOURCE) && context.getListClass().equals(Resource.class)) {
            created = new ResourceImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(POMQName.TESTRESOURCE) && context.getListClass().equals(Resource.class)) {
            created = new ResourceImpl(context.getModel(), element);
            return;
        }
    }

    @Override
    public void visit(Configuration context) {
        created = new POMExtensibilityElementBase(context.getModel(), element);
    }

    @Override
    public void visit(Properties context) {
        created = new POMExtensibilityElementBase(context.getModel(), element);
    }


    @Override
    public void visit(POMExtensibilityElement context) {
        created = new POMExtensibilityElementBase(context.getModel(), element);
    }
}
    