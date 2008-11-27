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
package org.netbeans.modules.maven.model.pom.visitor;

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
import org.netbeans.modules.maven.model.pom.POMComponentVisitor;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
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


/**
 * Default shallow visitor.
 *
 * @author mkleint
 */
public class DefaultVisitor implements POMComponentVisitor {
        
    public void visit(Project target) {
        visitComponent(target);
    }

    public void visit(Parent target) {
        visitComponent(target);
    }

    public void visit(Organization target) {
        visitComponent(target);
    }

    public void visit(DistributionManagement target) {
        visitComponent(target);
    }

    public void visit(Site target) {
        visitComponent(target);
    }

    public void visit(DeploymentRepository target) {
        visitComponent(target);
    }

    public void visit(Prerequisites target) {
        visitComponent(target);
    }

    public void visit(Contributor target) {
        visitComponent(target);
    }

    public void visit(Scm target) {
        visitComponent(target);
    }

    public void visit(IssueManagement target) {
        visitComponent(target);
    }

    public void visit(CiManagement target) {
        visitComponent(target);
    }

    public void visit(Notifier target) {
        visitComponent(target);
    }

    public void visit(Repository target) {
        visitComponent(target);
    }

    public void visit(RepositoryPolicy target) {
        visitComponent(target);
    }

    public void visit(Profile target) {
        visitComponent(target);
    }

    public void visit(BuildBase target) {
        visitComponent(target);
    }

    public void visit(Plugin target) {
        visitComponent(target);
    }

    public void visit(Dependency target) {
        visitComponent(target);
    }

    public void visit(Exclusion target) {
        visitComponent(target);
    }

    public void visit(PluginExecution target) {
        visitComponent(target);
    }

    public void visit(Resource target) {
        visitComponent(target);
    }

    public void visit(PluginManagement target) {
        visitComponent(target);
    }

    public void visit(Reporting target) {
        visitComponent(target);
    }

    public void visit(ReportPlugin target) {
        visitComponent(target);
    }

    public void visit(ReportSet target) {
        visitComponent(target);
    }

    public void visit(Activation target) {
        visitComponent(target);
    }

    public void visit(ActivationProperty target) {
        visitComponent(target);
    }

    public void visit(ActivationOS target) {
        visitComponent(target);
    }

    public void visit(ActivationFile target) {
        visitComponent(target);
    }

    public void visit(ActivationCustom target) {
        visitComponent(target);
    }

    public void visit(DependencyManagement target) {
        visitComponent(target);
    }

    public void visit(Build target) {
        visitComponent(target);
    }

    public void visit(Extension target) {
        visitComponent(target);
    }

    public void visit(License target) {
        visitComponent(target);
    }

    public void visit(MailingList target) {
        visitComponent(target);
    }

    public void visit(Developer target) {
        visitComponent(target);
    }

    public void visit(POMExtensibilityElement target) {
        visitComponent(target);
    }

    public void visit(ModelList target) {
        visitComponent(target);
    }
    
    public void visit(Configuration target) {
        visitComponent(target);
    }

    public void visit(Properties target) {
        visitComponent(target);
    }

    protected void visitComponent(POMComponent target) {
    }

    public void visit(StringList target) {
        visitComponent(target);
    }

}