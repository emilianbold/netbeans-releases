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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.jellytools.modules.web;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.insane.impl.Utils;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.modules.project.ui.test.ProjectSupport;

/**
 * Test of org.netbeans.jellytools.NewJspFileNameStepOperator.
 * @author Martin.Schovanek@sun.com
 */
public class NewWebProjectTest extends J2eeTestCase {
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewWebProjectTest(String testName) {
        super(testName);
    }
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        /*
        TestSuite suite = new NbTestSuite();
        suite.addTest(new NewWebProjectTest("createSampleWebProject"));
        return suite;
         */
        return createModuleTest(NewWebProjectTest.class, "createSampleWebProject");
    }

    @Override
    public void setUp() throws Exception {        
        System.out.println("### "+getName()+" ###");
        Util.addSjsasInstance();
    }

    //TODO: fix this test
    // a web server registered in IDE required
    public void createSampleWebProject() throws Exception {
        //getGlassFishV2Node();
        String prjName = "SampleWebApplication";
        String web = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "OpenIDE-Module-Display-Category");
        String webApplication = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.project.ui.wizards.Bundle",
                "Templates/Project/Web/emptyWeb.xml");
        
        // if sample web project does not exist in the data dir, create it
        File prjDir = new File(getDataDir(), prjName);
        if (!prjDir.exists()) {
            ProjectSupport.closeProject(prjName);
            NewProjectWizardOperator nop = NewProjectWizardOperator.invoke();
            nop.selectCategory(web);
            nop.selectProject(webApplication);
            nop.next();
            NewWebProjectNameLocationStepOperator lop =
                    new NewWebProjectNameLocationStepOperator();
            lop.setProjectName(prjName);
            try {
                lop.setProjectLocation(getDataDir().getCanonicalPath());
            } catch (IOException ioe) {
                fail(ioe);
            }
            lop.next();
            lop.finish();
            // Opening Projects
            String openingProjectsTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_Opening_Projects_Progress");
            try {
                // wait at most 60 second until progress dialog dismiss
                JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 60000);
                new NbDialogOperator(openingProjectsTitle).waitClosed();
            } catch (TimeoutExpiredException e) {
                // ignore when progress dialog was closed before we started to wait for it
            }
            // wait for opening
            ProjectsTabOperator.invoke().getProjectRootNode(prjName);
            try {
                Class.forName("org.netbeans.api.java.source.SourceUtils", true, Thread.currentThread().getContextClassLoader()).
                        getMethod("waitScanFinished").invoke(null);
            } catch (ClassNotFoundException x) {
                System.err.println("Warning: org.netbeans.api.java.source.SourceUtils could not be found, will not wait for scan to finish");
            }
        } else {
            openDataProjects(prjName);
            new OpenAction().perform(
                    new Node(new ProjectsTabOperator().getProjectRootNode(prjName),
                                    "Web Pages|index.jsp")); // NOI18N
        }
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
}
