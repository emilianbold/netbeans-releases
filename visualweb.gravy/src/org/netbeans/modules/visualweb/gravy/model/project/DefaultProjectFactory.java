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

package org.netbeans.modules.visualweb.gravy.model.project;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.modules.visualweb.gravy.ProjectNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.Bundle;

import java.io.File;

/**
 * Default factory for creation of projects.
 */

public class DefaultProjectFactory extends ProjectFactory {

    private final static String bundle = "org.netbeans.modules.visualweb.gravy.model.project.Bundle";
    
    /**
     * Type of project.
     */
    private final static String projectType = Bundle.getStringTrimmed(
                                              Bundle.getStringTrimmed(bundle, "ProjectWizardBundle"),
                                              Bundle.getStringTrimmed(bundle, "ProjectType"));
    
    /**
     * Category of project.
     */
    private final static String projectCategory = Bundle.getStringTrimmed(
                                                  Bundle.getStringTrimmed(bundle, "ProjectWizardBundle"),
                                                  Bundle.getStringTrimmed(bundle, "ProjectCategory"));
    
    private final static String blueprintsStructure = Bundle.getStringTrimmed(
                                                      Bundle.getStringTrimmed(bundle, "J2EEProjectsBundle"),
                                                      Bundle.getStringTrimmed(bundle, "JavaBluePrints"));
    
    private final static String jakartaStructure = Bundle.getStringTrimmed(
                                                      Bundle.getStringTrimmed(bundle, "J2EEProjectsBundle"),
                                                      Bundle.getStringTrimmed(bundle, "Jakarta"));
    
    /**
     * Default project factory.
     */
    private static DefaultProjectFactory defaultPFactory;

    /**
     * Create new DefaultProjectFactory.
     */
    public DefaultProjectFactory() {
    }

    /**
     * Create instance of DefaultProjectFactory or return it, if it is already exist.
     * Singleton.
     * @return DefaultProjectFactory.
     */
    public static DefaultProjectFactory getFactory() {
      if (defaultPFactory != null) return defaultPFactory;
      else return (defaultPFactory = new DefaultProjectFactory());
    }

    /**
     * Create new project.
     * @param projectDescriptor Descriptor of project.
     * @return created project.
     */
    protected Project createProject(ProjectDescriptor projectDescriptor) {
        String _projectPath = projectDescriptor.getProperty(projectDescriptor.LOCATION_KEY);
        String _projectName = projectDescriptor.getProperty(projectDescriptor.NAME_KEY);
        String _J2EEVersion = projectDescriptor.getProperty(projectDescriptor.J2EEVERSION_KEY);
        String _targetServer = projectDescriptor.getProperty(projectDescriptor.SERVER_KEY);
        if (_projectPath.lastIndexOf(File.separator) != _projectPath.length() - 1) _projectPath = _projectPath + File.separator;
        try {
            TestUtils.createNewProject(_projectPath, _projectName, true, projectType, projectCategory, blueprintsStructure, _J2EEVersion, _targetServer);
        }
        catch(Exception e) {
            throw new JemmyException("Project can't be created!", e);
        }
        return new JSFWebProject(projectDescriptor);
    }
    
    /**
     * Open existing project.
     * @param pathToProject Path to project's folder.
     * @return opened project.
     */
    protected Project openProject(String pathToProject) {       
        try {
            TestUtils.openProject(pathToProject);
        }
        catch(Exception e) {
            throw new JemmyException("Project can't be opened!", e);
        }
        String projectName = pathToProject.substring(pathToProject.lastIndexOf(File.separator) + 1, pathToProject.length());
        String projectLocation = pathToProject.substring(0, pathToProject.lastIndexOf(File.separator) + 1);
        ProjectNavigatorOperator.pressPopupItemOnNode(projectName, "Properties");
        JDialogOperator propertiesDialog = new JDialogOperator("Project Properties - "+projectName);
        TestUtils.wait(1000);
        JTreeOperator tree = new JTreeOperator(propertiesDialog);
        tree.selectPath(tree.findPath("Run"));
        TestUtils.wait(1000);
        String targetServer = new JComboBoxOperator(propertiesDialog).getSelectedItem().toString();
        String J2EEVersion = new JTextFieldOperator(propertiesDialog, 0).getText();
        new JButtonOperator(propertiesDialog, "OK").pushNoBlock();
        TestUtils.wait(1000);
        System.out.println("projectName="+projectName+"; projectLocation="+projectLocation+"; J2EEVersion="+J2EEVersion+"; targetServer="+targetServer);
        ProjectDescriptor projectDescriptor = new ProjectDescriptor(projectName, projectLocation, J2EEVersion, targetServer);
        return new JSFWebProject(projectDescriptor);
    }
}
