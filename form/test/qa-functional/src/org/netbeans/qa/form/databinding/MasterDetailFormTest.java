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
package org.netbeans.qa.form.databinding;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.*;
import org.netbeans.qa.form.ExtJellyTestCase;
import java.util.*;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;


/**
 * Basic test of Master/Detail form wizzard
 *
 * @author Jiri Vagner
 */
public class MasterDetailFormTest extends ExtJellyTestCase {
    //private String _newFormName = "NewMasterDetailForm_1182247916989";
    private String _newFormName = "NewMasterDetailForm_" + getTimeStamp();

    /**
     * Constructor required by JUnit
     */
    public MasterDetailFormTest(String testName) {
        super(testName);
    }
    
    /**
     * Method allowing to execute test directly from IDE.
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /**
     * Creates suite from particular test cases.
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new MasterDetailFormTest("testMasterDetailWizard")); // NOI18N
        suite.addTest(new MasterDetailFormTest("testGeneratedJpaStuff")); // NOI18N
        suite.addTest(new MasterDetailFormTest("testGeneratedCode")); // NOI18N
                
        return suite;
    }
    
    /** Uses Master/Detail Sample wizard */
    public void testMasterDetailWizard() {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(getTestProjectName());
        nfwo.selectCategory("Swing GUI Forms"); // NOI18N
        nfwo.selectFileType("Master/Detail Sample Form"); // NOI18N
        nfwo.next();
        
        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText(_newFormName);
        nfnlso.setPackage(getTestPackageName());
        nfnlso.next();
        
        NbDialogOperator masterOp = new NbDialogOperator("New Master/Detail Form"); // NOI18N
        new JComboBoxOperator(masterOp,1).selectItem(SetUpDerbyDatabaseTest.JDBC_URL);
        new JButtonOperator(masterOp,"Next").clickMouse(); // NOI18N
        
        masterOp = new NbDialogOperator("New Master/Detail Form"); // NOI18N
        new JButtonOperator(masterOp,"Finish").clickMouse(); // NOI18N
        waitNoEvent(8000);
    }
    
    /** Tests avail. of JPA components */
    public void testGeneratedJpaStuff() {
        // persistance config file exists
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(getTestProjectName());
        prn.select();
        new Node(prn, "Source Packages|META-INF|persistence.xml"); // NOI18N
        
        // open form file ...
        openFile(_newFormName);
        FormDesignerOperator designer = new FormDesignerOperator(_newFormName);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();

        // ... and check the components inside Other Components
        new Node(inspector.treeComponents(), "Other Components|entityManager [EntityManager]"); // NOI18N
        new Node(inspector.treeComponents(), "Other Components|query [Query]"); // NOI18N
        new Node(inspector.treeComponents(), "Other Components|list [List]"); // NOI18N
    }

    /** Tests generated code */
    public void testGeneratedCode() {
        FormDesignerOperator designer = new FormDesignerOperator(_newFormName);

        ArrayList<String> lines = new ArrayList<String>();
        lines.add("JButton refreshButton;"); // NOI18N
        lines.add("JButton saveButton;"); // NOI18N
        lines.add("JLabel authorLabel;"); // NOI18N
        lines.add("JTextField authorField;"); // NOI18N
        lines.add("JLabel authorLabel;"); // NOI18N
        lines.add("JTextField authorField;"); // NOI18N
        lines.add("void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)"); // NOI18N
        lines.add("bindingContext = new javax.beans.binding.BindingContext();"); // NOI18N
        lines.add("entityManager = javax.persistence.Persistence.createEntityManagerFactory(\"testdatabasePU\").createEntityManager();"); // NOI18N
        lines.add("query = entityManager.createQuery(\"SELECT b FROM Book b\");"); // NOI18N
        lines.add("list = com.sun.java.util.BindingCollections.observableList(query.getResultList());"); // NOI18N
        lines.add("bindingContext.addBinding(masterTable, \"${selectedElement.author}\", authorField, \"text\");"); // NOI18N        

        findInCode(lines, designer);
    }
}
