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

package org.netbeans.test.mobility;


//<editor-fold desc="imports">
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.junit.ide.ProjectSupport;
//</editor-fold>

/**
 *
 * @author tester
 */
public class JavaMEWebServiceClientTest extends JellyTestCase {
    
    //<editor-fold desc="Constants">
    public static final String WIZARD_BUNDLE = "org.netbeans.modules.mobility.project.ui.wizard.Bundle";
    public static final String CATEGORY_MIDP = Bundle.getStringTrimmed(WIZARD_BUNDLE, "Templates/MIDP");
    public static final String PROJECT_MIDP = Bundle.getStringTrimmed(WIZARD_BUNDLE, "Templates/Project/J2ME/MobileApplication");
    public static final String PACKAGE = "testing";   
    public static final String ITEM_J2MEWSClient = "Java ME Web Service Client";
    public static final String PROJECT_NAME_MIDP = "MobileApplication";
    //</editor-fold>
    
    //<editor-fold desc="Test Suite - base">
    /** Constructor required by JUnit */
    public JavaMEWebServiceClientTest(String tname, boolean init) {
        super(tname);
        if (init) init();
    }
    
    /** Creates suite from particular test cases. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new JavaMEWebServiceClientTest("testJavaMEWebServClient", true));
        return suite;
    }
    //</editor-fold>
    
    //<editor-fold desc="Sleep">    
    public void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    //</editor-fold>
    
    public void init() {
        new Action(null, "Open").perform(new Node(ProjectsTabOperator.invoke().tree(), PROJECT_NAME_MIDP + "|Source Packages|hello|HelloMIDlet.java"));
    }
    
    //<editor-fold desc="MIDP Files">
    public void testJavaMEWebServClient() {
        NewFileWizardOperator newFile = NewFileWizardOperator.invoke(); 
        newFile.selectCategory(CATEGORY_MIDP);
        newFile.selectFileType(ITEM_J2MEWSClient);
        newFile.next();
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        new JTextFieldOperator(op, 0).setText("http://wiki.netbeans.org/wiki/attach/TS_60_MobilityEndToEnd/EchoNoArraysDOCUMENT.wsdl");
        //op.setObjectName("http://wiki.netbeans.org/wiki/attach/TS_60_MobilityEndToEnd/EchoNoArraysDOCUMENT.wsdl"); //TODO !!! doesn't work with some file types. It doesn;t change the name
        sleep(1000);
        new JButtonOperator(op, "Retrieve WSDL").push();
        new NbDialogOperator("Validation Results").btOK().push();
        op.finish();
        ProjectSupport.waitScanFinished();
        Action ca = new Action(null, "Build");
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        ca.perform(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME_MIDP));
        stt.waitText("Finished building");
    }
    //</editor-fold>
    
}