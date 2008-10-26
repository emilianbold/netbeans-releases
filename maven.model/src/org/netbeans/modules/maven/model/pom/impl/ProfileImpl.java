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

import java.util.Collections;
import org.w3c.dom.Element;
import org.netbeans.modules.maven.model.pom.*;	
import org.netbeans.modules.maven.model.pom.visitor.POMComponentVisitor;	

/**
 *
 * @author mkleint
 */
public class ProfileImpl extends POMComponentImpl implements Profile {

    public ProfileImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ProfileImpl(POMModel model) {
        this(model, createElementNS(model, POMQName.PROFILE));
    }

    // attributes

    // child elements
    public Activation getActivation() {
        return getChild(Activation.class);
    }

    public void setActivation(Activation activation) {
        java.util.List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(Activation.class, ACTIVATION_PROPERTY, activation, empty);
    }

    public BuildBase getBuildBase() {
        return getChild(BuildBase.class);
    }

    public void setBuildBase(BuildBase buildBase) {
        java.util.List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(BuildBase.class, BUILDBASE_PROPERTY, buildBase, empty);
    }

//    public List<Module> getModules() {
//        return getChildren(Module.class);
//    }
//
//    public void addModule(Module buildBase) {
//        appendChild(MODULE_PROPERTY, buildBase);
//    }
//
//    public void removeModule(Module buildBase) {
//        removeChild(MODULE_PROPERTY, buildBase);
//    }

    public java.util.List<Repository> getRepositories() {
        return getChildren(Repository.class);
    }

    public void addRepository(Repository buildBase) {
        appendChild(REPOSITORY_PROPERTY, buildBase);
    }

    public void removeRepository(Repository buildBase) {
        removeChild(REPOSITORY_PROPERTY, buildBase);
    }

    public java.util.List<Repository> getPluginRepositories() {
        return getChildren(Repository.class);
    }

    public void addPluginRepository(Repository buildBase) {
        appendChild(PLUGINREPOSITORY_PROPERTY, buildBase);
    }

    public void removePluginRepository(Repository buildBase) {
        removeChild(PLUGINREPOSITORY_PROPERTY, buildBase);
    }

    public java.util.List<Dependency> getDependencies() {
        return getChildren(Dependency.class);
    }

    public void addDependency(Dependency buildBase) {
        appendChild(DEPENDENCY_PROPERTY, buildBase);
    }

    public void removeDependency(Dependency buildBase) {
        removeChild(DEPENDENCY_PROPERTY, buildBase);
    }

    public Reporting getReporting() {
        return getChild(Reporting.class);
    }

    public void setReporting(Reporting reporting) {
        java.util.List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(Reporting.class, REPORTING_PROPERTY, reporting, empty);
    }

    public DependencyManagement getDependencyManagement() {
        return getChild(DependencyManagement.class);
    }

    public void setDependencyManagement(DependencyManagement dependencyManagement) {
        java.util.List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(DependencyManagement.class, DEPENDENCYMANAGEMENT_PROPERTY, dependencyManagement, empty);
    }

    public DistributionManagement getDistributionManagement() {
        return getChild(DistributionManagement.class);
    }

    public void setDistributionManagement(DistributionManagement distributionManagement) {
        java.util.List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(DistributionManagement.class, DISTRIBUTIONMANAGEMENT_PROPERTY, distributionManagement, empty);
    }

    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

    public static class List extends ListImpl<Profile> {
        public List(POMModel model, Element element) {
            super(model, element, POMQName.PROFILE, Profile.class);
        }

        public List(POMModel model) {
            this(model, createElementNS(model, POMQName.PROFILES));
        }
    }


}