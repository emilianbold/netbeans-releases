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
package org.netbeans.jellytools;

import java.io.IOException;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

/**
 * Test of org.netbeans.jellytools.NewFileWizardOperator.
 * @author tb115823
 */
public class NewFileWizardOperatorTest extends JellyTestCase {

    public static NewFileWizardOperator op;
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    public static final String[] tests = new String[] {
        "testInvokeTitle", "testInvoke", "testSelectProjectAndCategoryAndFileType", 
        "testGetDescription", "testCreate"};    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        /*
        TestSuite suite = new NbTestSuite();
        suite.addTest(new NewFileWizardOperatorTest("testInvokeTitle"));
        suite.addTest(new NewFileWizardOperatorTest("testInvoke"));
        suite.addTest(new NewFileWizardOperatorTest("testSelectProjectAndCategoryAndFileType"));
        suite.addTest(new NewFileWizardOperatorTest("testGetDescription"));
        suite.addTest(new NewFileWizardOperatorTest("testCreate"));
        return suite;
         */
        return createModuleTest(NewFileWizardOperatorTest.class, 
                tests);
    }
    
    protected void setUp() throws IOException {
        System.out.println("### "+getName()+" ###");
        openDataProjects("SampleProject");           
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewFileWizardOperatorTest(String testName) {
        super(testName);
    }
    
    /** Test of invoke method with title parameter. Opens New File wizard, waits for the dialog and closes it. */
    public void testInvokeTitle() {
        // "New File"
        String title = Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_NewFileWizard_Title");
        op = NewFileWizardOperator.invoke(title);
        op.close();
    }
    
    /** Test of invoke method. Opens New File wizard and waits for the dialog. */
    public void testInvoke() {
        op = NewFileWizardOperator.invoke();
    }
    
    /** Test components on New File Wizard panel 
     *  sets category and filetype
     */
    public void testSelectProjectAndCategoryAndFileType() {
        org.netbeans.jemmy.operators.JComboBoxOperator cbo = op.cboProject();
        cbo.selectItem(0);
        // Java Classes
        op.selectCategory(Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes"));
        // Java Class
        op.selectFileType(Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes/Class.java"));
    }

    /** Test description component on New File Wizard panel
     *  gets description of selected filetype
     */
    public void testGetDescription() {
        assertTrue("Description should contain Java class sub string.", op.getDescription().indexOf("Java class") > 0);
        op.cancel();
    }

    public void testCreate() {
        // Java Classes
        String javaClassesLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes");
        // Java Class
        String javaClassLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes/Class.java");
        NewJavaFileWizardOperator.create("SampleProject", javaClassesLabel, javaClassLabel, "sample1", "TempClass");  // NOI18N
        Node classNode = new Node(new SourcePackagesNode("SampleProject"), "sample1|TempClass");  // NOI18N
        DeleteAction deleteAction = new DeleteAction();
        deleteAction.perform(classNode);
        // "Safe Delete"
        //TODO: is this the correct bundle/key to use here?
        String safeDeleteTitle = Bundle.getString("org.netbeans.modules.project.ui.actions.Bundle", "LBL_DeleteProjectAction_Name"); // I18N
        new NbDialogOperator(safeDeleteTitle).ok();
    }
}
