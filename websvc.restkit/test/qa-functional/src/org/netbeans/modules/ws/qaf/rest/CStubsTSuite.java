/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ws.qaf.rest;

import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.ListModel;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 * Tests for New REST service client stub wizard
 *
 * @author lukas
 */
public class CStubsTSuite extends RestTestBase {

    public CStubsTSuite(String name) {
        super(name);
    }

    @Override
    protected String getProjectName() {
        return "RESTClient"; //NOI18N
    }

    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static TestSuite suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new CStubsTSuite("testWizard")); //NOI18N
        suite.addTest(new CStubsTSuite("testCreateSimpleStubs")); //NOI18N
        suite.addTest(new CStubsTSuite("testFromWADL")); //NOI18N
        suite.addTest(new CStubsTSuite("testCloseProject")); //NOI18N
        return suite;
    }

    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
    }

    /**
     * Test the wizard - add 2 projects, remove 1 and then Cancel the wizard
     */
    public void testWizard() {
        //invoke the wizard
        //RESTful Web Service Client Stubs
        String cStubsLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestClientStubs");
        String path = FileUtil.toFile(getProject("FromEntities").getProjectDirectory()).getAbsolutePath(); //NOI18N
        String path2 = FileUtil.toFile(getProject("FromPatterns").getProjectDirectory()).getAbsolutePath(); //NOI18N
        createNewWSFile(getProject(), cStubsLabel);
        WizardOperator wo = new WizardOperator(cStubsLabel);
        //add project
        addProject(wo, path);
        JListOperator jlo = new JListOperator(wo, 1);
        ListModel lm = jlo.getModel();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
        assertEquals(1, lm.getSize());
        //add second project
        wo = new WizardOperator(cStubsLabel);
        addProject(wo, path2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
        assertEquals(2, lm.getSize());
        //select first project
        jlo.selectItem(0);
        //remove it
        wo = new WizardOperator(cStubsLabel);
        new JButtonOperator(wo, 4).pushNoBlock();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
        assertEquals(1, lm.getSize());
        //cancel/close the wizard
        wo.cancel();
    }

    /**
     * Test stubs creation from a foreign project
     */
    public void testCreateSimpleStubs() {
        createStubs("FromEntities"); //NOI18N
    }

    /**
     * Test stubs creation from a local WADL file
     */
    public void testFromWADL() throws IOException {
        //RESTful Web Service Client Stubs
        String cStubsLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestClientStubs");
        createNewWSFile(getProject(), cStubsLabel);
        WizardOperator wo = new WizardOperator(cStubsLabel);
        new JRadioButtonOperator(wo, 1).clickMouse();
        new JTextFieldOperator(wo, 2).typeText(new File(getRestDataDir(), "testApplication.wadl").getCanonicalFile().getAbsolutePath()); //NOI18N
        //http://www.netbeans.org/issues/show_bug.cgi?id=123573
        new JCheckBoxOperator(wo, 0).setSelected(useJMaki());
        wo.finish();
        //Generating Client Stubs From RESTful Web Services...
        String progressLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ClientStubsProgress");
        waitDialogClosed(progressLabel);
    }

    protected void createStubs(String sourceProject) {
        String sourcePath = FileUtil.toFile(getProject(sourceProject).getProjectDirectory()).getAbsolutePath();
        //RESTful Web Service Client Stubs
        String cStubsLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestClientStubs");
        createNewWSFile(getProject(), cStubsLabel);
        WizardOperator wo = new WizardOperator(cStubsLabel);
        addProject(wo, sourcePath);
        //http://www.netbeans.org/issues/show_bug.cgi?id=123573
        new JCheckBoxOperator(wo, 0).setSelected(useJMaki());
        wo.finish();
        //Generating Client Stubs From RESTful Web Services...
        String progressLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ClientStubsProgress");
        waitDialogClosed(progressLabel);
    }
    
    protected boolean useJMaki() {
        return false;
    }

    private void addProject(WizardOperator wo, String path) {
        //Add Project...
        new JButtonOperator(wo, 5).pushNoBlock();
        //Select Project
        String prjDlgTitle = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ProjectChooserTitle");
        NbDialogOperator ndo = new NbDialogOperator(prjDlgTitle);
        JTextFieldOperator jtfo = new JTextFieldOperator(ndo, 0);
        jtfo.clearText();
        jtfo.typeText(path);
        //Open
        JButton jb = JButtonOperator.findJButton(ndo.getContentPane(), "Open", false, false); //NOI18N
        if (jb != null) {
            JButtonOperator jbo = new JButtonOperator(jb);
            jbo.pushNoBlock();
        } else {
            fail("Open button not found...."); //NOI18N
        }
    }

    private Project getProject(String name) {
        ProjectRootNode n = ProjectsTabOperator.invoke().getProjectRootNode(name);
        return ((Node)n.getOpenideNode()).getLookup().lookup(Project.class);
    }
}
