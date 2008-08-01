/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.visualweb.gravy.model;

import org.netbeans.modules.visualweb.gravy.model.components.*;
import org.netbeans.modules.visualweb.gravy.model.deployment.*;
import org.netbeans.modules.visualweb.gravy.model.project.*;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.gravy.TestUtils;

import org.netbeans.jemmy.JemmyException;

import java.util.List;

/**
 * Representing IDE itself.
 */

public class IDE {
    
    /**
     * Variable representing IDE.
     */
    private static IDE ide;
    
    /**
     * Add default depoyment targets.
     */
    private IDE() {
        DeploymentTargetDescriptor dtd = new DeploymentTargetDescriptor();
        dtd.loadDefault();
        if (dtd.getProperties()!=null) {
            DefaultDeploymentTargetFactory.getFactory().create(dtd);
        }
    }
    
    /**
     * Create instance of IDE or return it, if it is already exist.
     * Singleton.
     * @return IDE.
     */
    public static IDE getIDE() {
        if (ide != null) return ide;
        else return (ide = new IDE());
    }
    
    /**
     * Get deployment targets.
     * @return List of deployment targets.
     */
    public List getDeploymentTargets() {
        return DeploymentTargetList.getList();
    }
    
    /**
     * Add deployment target to IDE.
     * @param DTDescriptor Descriptor of deployment target.
     * @return added deployment target.
     */
    public DeploymentTarget addDeploymentTarget(DeploymentTargetDescriptor DTDescriptor) {
        return DefaultDeploymentTargetFactory.getFactory().create(DTDescriptor);
    }
    
    /**
     * Get projects.
     * @return List of projects.
     */
    public List getProjects() {
        return ProjectList.getList();
    }
    
    /**
     * Create new project.
     * @param projectDescriptor Descriptor of project.
     * @return created project.
     */
    public Project createProject(ProjectDescriptor projectDescriptor) {
        return DefaultProjectFactory.getFactory().create(projectDescriptor);
    }
    
    /**
     * Open existing project.
     * @param pathToProject Path to project's folder.
     * @return existing project.
     */
    public Project openProject(String pathToProject) {
        return DefaultProjectFactory.getFactory().open(pathToProject);
    }
    
    /**
     * Deploy main project.
     */
    public void deploy(){
        try {
            Util.getMainWindow().btDeploy().push();
        } catch(Exception e) {
            throw new JemmyException("IDE can't deploy Application!", e);
        }
        TestUtils.wait(20000);
    }
    
    /**
     * Return default component set.
     */
    public ComponentSet getDefaultComponentSet() {
        return BasicComponentSet.getBasicComponentSet();
    }
}
