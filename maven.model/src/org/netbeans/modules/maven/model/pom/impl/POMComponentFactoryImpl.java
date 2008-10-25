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

import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.pom.*;
import org.netbeans.modules.maven.model.pom.spi.ElementFactory;
import org.netbeans.modules.maven.model.pom.spi.POMExtensibilityElementBase;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class POMComponentFactoryImpl implements POMComponentFactory {
    
    private POMModel model;
    
    /**
     * Creates a new instance of POMComponentFactoryImpl
     */
    public POMComponentFactoryImpl(POMModel model) {
        this.model = model;
    }    
 
    private static QName getQName(Element element, POMComponentImpl context) {
        String namespace = element.getNamespaceURI();
        String prefix = element.getPrefix();
        if (namespace == null && context != null) {
            namespace = context.lookupNamespaceURI(prefix);
        }
        String localName = element.getLocalName();
        assert(localName != null);
        if (namespace == null && prefix == null) {
            return new QName(localName);
        } else if (namespace != null && prefix == null) {
            return new QName(namespace, localName);
        } else {
            return new QName(namespace, localName, prefix);
        }
    }

    public POMComponent create(Element element, POMComponent context) {
        // return new SCAComponentCreateVisitor().create(element, context);
        QName qName = getQName(element, (POMComponentImpl)context);
        ElementFactory elementFactory = ElementFactoryRegistry.getDefault().get(qName);
        return create(elementFactory, element, context);
    }
    
    private POMComponent create(ElementFactory elementFactory, Element element, POMComponent context) {
        if (elementFactory != null ){
            return elementFactory.create(context, element);
        } else {
            return new POMExtensibilityElementBase(model, element);
        }
    }
    
    public POMComponent create(POMComponent context, QName qName) {
       String prefix = qName.getPrefix();
       if (prefix == null || prefix.length() == 0) {
           prefix = qName.getLocalPart();
       } else {
           prefix = prefix + ":" + qName.getLocalPart();
       }

       ElementFactory factory = ElementFactoryRegistry.getDefault().get(qName);
       Element element = model.getDocument().createElementNS(qName.getNamespaceURI(), prefix);
       return create(factory, element, context);
    }
    
    public Project createProject() {
        return new ProjectImpl(model);
    }

    public Parent createParent() {
        return new ParentImpl(model);
    }

    public Organization createOrganization() {
        return new OrganizationImpl(model);
    }

    public DistributionManagement createDistributionManagement() {
        return new DistributionManagementImpl(model);
    }

    public Site createSite() {
        return new SiteImpl(model);
    }

    public DeploymentRepository createDistRepository() {
        return new DeploymentRepositoryImpl(model, POMQName.DIST_REPOSITORY);
    }

    public DeploymentRepository createDistSnapshotRepository() {
        return new DeploymentRepositoryImpl(model, POMQName.DIST_SNAPSHOTREPOSITORY);
    }

    public Prerequisites createPrerequisites() {
        return new PrerequisitesImpl(model);
    }

    public Contributor createContributor() {
        return new ContributorImpl(model);
    }

    public Scm createScm() {
        return new ScmImpl(model);
    }

    public IssueManagement createIssueManagement() {
        return new IssueManagementImpl(model);
    }

    public CiManagement createCiManagement() {
        return new CiManagementImpl(model);
    }

    public Notifier createNotifier() {
        return new NotifierImpl(model);
    }

    public Repository createRepository() {
        return new RepositoryImpl(model);
    }

    public RepositoryPolicy createReleaseRepositoryPolicy() {
        return new RepositoryPolicyImpl(model, POMQName.RELEASE);
    }

    public RepositoryPolicy createSnapshotRepositoryPolicy() {
        return new RepositoryPolicyImpl(model, POMQName.SNAPSHOT);
    }


    public Profile createProfile() {
        return new ProfileImpl(model);
    }

    public BuildBase createBuildBase() {
        return new BuildBaseImpl(model);
    }

    public Plugin createPlugin() {
        return new PluginImpl(model);
    }

    public Dependency createDependency() {
        return new DependencyImpl(model);
    }
    
    public DependencyImpl.List createDependencyList() {
        return new DependencyImpl.List(model);
    }

    public Exclusion createExclusion() {
        return new ExclusionImpl(model);
    }

    public PluginExecution createExecution() {
        return new PluginExecutionImpl(model);
    }

    public Resource createResource() {
        return new ResourceImpl(model);
    }

    public PluginManagement createPluginManagement() {
        return new PluginManagementImpl(model);
    }

    public Reporting createReporting() {
        return new ReportingImpl(model);
    }

    public ReportPlugin createReportPlugin() {
        return new ReportPluginImpl(model);
    }

    public ReportSet createReportSet() {
        return new ReportSetImpl(model);
    }

    public Activation createActivation() {
        return new ActivationImpl(model);
    }

    public ActivationProperty createActivationProperty() {
        return new ActivationPropertyImpl(model);
    }

    public ActivationOS createActivationOS() {
        return new ActivationOSImpl(model);
    }

    public ActivationFile createActivationFile() {
        return new ActivationFileImpl(model);
    }

    public ActivationCustom createActivationCustom() {
        return new ActivationCustomImpl(model);
    }

    public DependencyManagement createDependencyManagement() {
        return new DependencyManagementImpl(model);
    }

    public Build createBuild() {
        return new BuildImpl(model);
    }

    public Extension createExtension() {
        return new ExtensionImpl(model);
    }

    public License createLicense() {
        return new LicenseImpl(model);
    }

    public MailingList createMailingList() {
        return new MailingListImpl(model);
    }

    public MailingListImpl.List createMailingListList() {
        return new MailingListImpl.List(model);
    }

    public Developer createDeveloper() {
        return new DeveloperImpl(model);
    }

}
