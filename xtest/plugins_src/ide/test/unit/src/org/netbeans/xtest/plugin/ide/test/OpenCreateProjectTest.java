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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 package org.netbeans.xtest.plugin.ide.testst.plugin.ide.test;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.project.ui.OpenProjectList;

/** Test xtest properties which handle creating and opening of projects:
 * xtest.ide.create.project - if true java project XTestProject should be created;
 * xtest.ide.open.project - if set to e.g. ${xtest.data}/SampleProject, the project should be opened;
 * if projects.zip is present in ${xtest.data} folder, all projects in zip should be opened;
 */
public class OpenCreateProjectTest extends NbTestCase {
    
    /** Creates a new test.
     * @param name test name
     */
    public OpenCreateProjectTest(String name) {
        super(name);
    }
    
    /** Create suite. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new OpenCreateProjectTest("testCreateProject"));
        suite.addTest(new OpenCreateProjectTest("testOpenProject"));
        suite.addTest(new OpenCreateProjectTest("testOpenProjectsFromZip"));
        return suite;
    }
    
    /** Set up. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** Test xtest.ide.create.project - if true java project XTestProject should be created. */
    public void testCreateProject() {
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            Project project = projects[i];
            if(ProjectUtils.getInformation(project).getName().equals("XTestProject")) {
                return;
            }
        }
        fail("Project XTestProject should be created because property xtest.ide.create.project is set");
    }
 
    /** Test xtest.ide.open.project - if set to e.g. ${xtest.data}/SampleProject, the project should be opened. */
     public void testOpenProject() {
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            Project project = projects[i];
            if(ProjectUtils.getInformation(project).getName().equals("SampleProject")) {
                return;
            }
        }
        fail("Project SampleProject should be opened because property xtest.ide.open.project is set to ${xtest.data}/SampleProject");
    }
    
    /** If projects.zip is present in ${xtest.data} folder, all projects in zip should be opened. */
    public void testOpenProjectsFromZip() {
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        boolean project1 = false;
        boolean project2 = false;
        for (int i = 0; i < projects.length; i++) {
            Project project = projects[i];
            if(ProjectUtils.getInformation(project).getName().equals("SampleProjectFromZip1")) {
                project1 = true;
            }
            if(ProjectUtils.getInformation(project).getName().equals("SampleProjectFromZip2")) {
                project2 = true;
            }
        }
        assertTrue("Projects SampleProjectFromZip1 and SampleProjectFromZip2 should be opened because projects.zip is present in xtest.data folder.", project1 && project2);
    }
}