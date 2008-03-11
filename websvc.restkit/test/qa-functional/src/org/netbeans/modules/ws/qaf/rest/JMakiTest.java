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
package org.netbeans.modules.ws.qaf.rest;

import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 * Test jMaki client stub.
 * 
 * Running of this test suite can be influenced by following system properties:
 * "plugins.jmaki.skip=true"
 *    - skipping this suite and related preparation steps (un/installing of the jMaki plugin)
 * "plugins.jmaki.nbm=/path/to/jmaki.nbm"
 *    - where to find jmaki.nbm (default is to download nbm from:
 *      https://ajax.dev.java.net/files/documents/3115/86078/org-netbeans-modules-sun-jmaki.nbm)
 * 
 * @author lukas
 */
public class JMakiTest extends CStubsTSuite {

    public JMakiTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        try {
            Class.forName("org.netbeans.modules.sun.jmaki.Installer");
        } catch (ClassNotFoundException ex) {
            fail("jMaki is not installed.");
        }
    }

    @Override
    protected String getProjectName() {
        return "JMakiClient"; //NOI18N
    }

    @Override
    protected boolean useJMaki() {
        return true;
    }

    /**
     * Test stubs creation from a foreign project
     */
    public void testCreateStubs() {
        addJMakiFrameWork();
        createStubs("FromEntities"); //NOI18N
    }
    
    public void testJMakiTestsSkipped() {
        //nothing to do
    }
    
    private void addJMakiFrameWork() {
        // open project properties
        getProjectRootNode().properties();
        // "Project Properties"
        String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
        // select "Frameworks" category
        String frmLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Config_Frameworks");
        new Node(new JTreeOperator(propertiesDialogOper), frmLabel).select();
        //Add ...
        String addBtn = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_AddFramework");
        new JButtonOperator(propertiesDialogOper, addBtn).pushNoBlock();
        //Add a Framework
        String addFrameworkTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_SelectWebExtension_DialogTitle");
        NbDialogOperator addFrameworkDialogOper = new NbDialogOperator(addFrameworkTitle);
        new JListOperator(addFrameworkDialogOper).selectItem("jMaki"); //NOI18N
        addFrameworkDialogOper.ok();
        // confirm properties dialog
        propertiesDialogOper.ok();
        // if setting default server, it scans server jars; otherwise it continues immediatelly
        ProjectSupport.waitScanFinished();
    }

    /**
     * Creates suite from particular test cases. You can define order of testcases here.
     */
    public static TestSuite suite() {
        TestSuite suite = new NbTestSuite();
        if (!Boolean.getBoolean("plugins.jmaki.skip")) { //NOI18N
            suite.addTest(new JMakiTest("testCreateStubs")); //NOI18N
            suite.addTest(new JMakiTest("testFromWADL")); //NOI18N
            suite.addTest(new JMakiTest("testCloseProject")); //NOI18N
        } else {
            suite.addTest(new JMakiTest("testJMakiTestsSkipped"));
        }
        return suite;
    }

    /**
     * Method allowing test execution directly from the IDE.
     */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
    }
}
