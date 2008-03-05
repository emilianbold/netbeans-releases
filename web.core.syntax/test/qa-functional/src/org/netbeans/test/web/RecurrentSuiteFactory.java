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
package org.netbeans.test.web;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author ms113234
 */
public class RecurrentSuiteFactory {

    private static boolean debug = true;

    public static Test createSuite(Class clazz, File projectsDir, FileObjectFilter filter) {
        String clazzName = clazz.getName();
        NbTestSuite suite = new NbTestSuite(clazzName);
        try {
            //get list of projects to be used for testing
            File[] projects = projectsDir.listFiles(new FilenameFilter() {
                // filter out non-project folders
                public boolean accept(File dir, String fileName) {
                    return !fileName.equals("CVS");
                }
            });
            debug("RecurrentSuiteFactory");
            debug("Projects dir: " + projectsDir);
            if (projects != null) {
                for (int i = 0; i < projects.length; i++) {
                    debug("Prj Folder: " + projects[i].getName());
                    Project project = (Project) ProjectSupport.openProject(projects[i]);
                    // not a project
                    if (project == null) {
                        debug("WW: Not a project!!!");
                        continue;
                    }
                    resolveServer(projects[i].getName());
                    ProjectInformation projectInfo = ProjectUtils.getInformation(project);
                    // find recursively all test.*[.jsp|.jspx|.jspf|.html] files in
                    // the web/ folder

                    // TODO check if the project is of current version and if necessery update it.
                    // enables transparent update, see: org.netbeans.modules.web.project.UpdateHelper
                    // System.setProperty("webproject.transparentUpdate",  "true");

                    FileObject prjDir = project.getProjectDirectory();
                    Enumeration fileObjs = prjDir.getChildren(true);

                    while (fileObjs.hasMoreElements()) {
                        FileObject fo = (FileObject) fileObjs.nextElement();
                        if (filter.accept(fo)) {
                            String testName = projectInfo.getName() + "_" + FileUtil.getRelativePath(prjDir, fo).replaceAll("[/.]", "_");
                            Constructor cnstr = clazz.getDeclaredConstructor(new Class[]{String.class, FileObject.class});
                            NbTestCase test = (NbTestCase) cnstr.newInstance(new Object[]{testName, fo});
                            suite.addTest(test);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return suite;
    }

    private static void debug(Object msg) {
        if (!debug) {
            return;
        }
        System.err.println("[debug] " + msg);
    }

    private static void resolveServer(String projectName) {
        ProjectSupport.waitScanFinished();
        Logger log = Logger.getLogger(RecurrentSuiteFactory.class.getName());
        String openProjectTitle = Bundle.getString("org.netbeans.modules.j2ee.common.ui.Bundle", "MSG_Broken_Server_Title");
        if (JDialogOperator.findJDialog(openProjectTitle, true, true) != null) {
            new NbDialogOperator(openProjectTitle).close();
            log.info("Resolving server");
            // open project properties
            ProjectsTabOperator.invoke().getProjectRootNode(projectName).properties();
            // "Project Properties"
            String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
            NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
            // select "Run" category
            new Node(new JTreeOperator(propertiesDialogOper), "Run").select();
            // set default server
            new JComboBoxOperator(propertiesDialogOper).setSelectedIndex(0);
            propertiesDialogOper.ok();
            // if setting default server, it scans server jars; otherwise it continues immediatelly
            ProjectSupport.waitScanFinished();
        }
        String editPropertiesTitle = "Edit Project Properties";
        while (JDialogOperator.findJDialog(editPropertiesTitle, true, true) != null) {
            new NbDialogOperator(editPropertiesTitle).cancel();
            log.info("Closing buildscript regeneration");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException exc) {
                log.log(Level.INFO, "interrupt exception", exc);
            }
        }
    }
}
