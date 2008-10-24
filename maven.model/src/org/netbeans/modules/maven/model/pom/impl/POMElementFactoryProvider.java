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

        if (isElementQName(POMQName.MAILINGLIST)) {
            created = new MailingListImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.DEVELOPER)) {
            created = new DeveloperImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.CONTRIBUTOR)) {
            created = new ContributorImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.LICENSE)) {
            created = new LicenseImpl(context.getModel(), element);
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

        if (isElementQName(POMQName.PROFILE)) {
            created = new ProfileImpl(context.getModel(), element);
            return;
        }

//        if (isElementQName(POMQName.MODULE)) {
//            created = new ModuleImpl(context.getModel(), element);
//            return;
//        }

        //TODO distinguish repository and pluginrepository and repository in
        //distributionManagement
        if (isElementQName(POMQName.REPOSITORY)) {
            created = new RepositoryImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PLUGINREPOSITORY)) {
            created = new RepositoryImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.DEPENDENCY)) {
            created = new DependencyImpl(context.getModel(), element);
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
        if (isElementQName(POMQName.RELEASE)) {
            created = new RepositoryPolicyImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.SNAPSHOT)) {
            created = new RepositoryPolicyImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    public void visit(RepositoryPolicy context) {
        //createExtensibilityElement(context);
    }


    @Override
    public void visit(Profile context) {
        if (isElementQName(POMQName.ACTIVATION)) {
            created = new ActivationImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.BUILDBASE)) {
            created = new BuildBaseImpl(context.getModel(), element);
            return;
        }

//        if (isElementQName(POMQName.MODULE)) {
//            created = new ModuleImpl(context.getModel(), element);
//            return;
//        }

        if (isElementQName(POMQName.REPOSITORY)) {
            created = new RepositoryImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PLUGINREPOSITORY)) {
            created = new RepositoryImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.DEPENDENCY)) {
            created = new DependencyImpl(context.getModel(), element);
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

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(BuildBase context) {
        if (isElementQName(POMQName.RESOURCE)) {
            created = new ResourceImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.TESTRESOURCE)) {
            created = new ResourceImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PLUGINMANAGEMENT)) {
            created = new PluginManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PLUGIN)) {
            created = new PluginImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Plugin context) {
        if (isElementQName(POMQName.EXECUTION)) {
            created = new PluginExecutionImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.DEPENDENCY)) {
            created = new DependencyImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Dependency context) {
        if (isElementQName(POMQName.EXCLUSION)) {
            created = new ExclusionImpl(context.getModel(), element);
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
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Resource context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(PluginManagement context) {
        if (isElementQName(POMQName.PLUGIN)) {
            created = new PluginImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Reporting context) {
        if (isElementQName(POMQName.REPORTPLUGIN)) {
            created = new ReportPluginImpl(context.getModel(), element);
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
        if (isElementQName(POMQName.DEPENDENCY)) {
            created = new DependencyImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(Build context) {
        visit((BuildBase) context);

        if (isElementQName(POMQName.EXTENSION)) {
            created = new ExtensionImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.RESOURCE)) {
            created = new ResourceImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.TESTRESOURCE)) {
            created = new ResourceImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PLUGINMANAGEMENT)) {
            created = new PluginManagementImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(POMQName.PLUGIN)) {
            created = new PluginImpl(context.getModel(), element);
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
    public void visit(POMExtensibilityElement context) {
        //if (isForeignElement()) {
        //    created = new POMExtensibilityElementImpl(context.getModel(), element);
        //}
    }
}
    