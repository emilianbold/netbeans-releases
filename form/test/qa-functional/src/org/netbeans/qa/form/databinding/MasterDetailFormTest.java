/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.qa.form.databinding;

import org.netbeans.qa.form.*;
import org.netbeans.qa.form.visualDevelopment.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.actions.*;
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
        nfwo.selectProject(ExtJellyTestCase.TEST_PROJECT_NAME);
        nfwo.selectCategory("Swing GUI Forms");
        nfwo.selectFileType("Master/Detail Sample Form");
        nfwo.next();
        
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText(_newFormName);
        nfnlso.setPackage(ExtJellyTestCase.TEST_PACKAGE_NAME);
        nfnlso.next();
        
        NbDialogOperator masterOp = new NbDialogOperator("New Master/Detail Form");
        new JComboBoxOperator(masterOp,1).selectItem(SetUpDerbyDatabaseTest.JDBC_URL);
        new JButtonOperator(masterOp,"Next").clickMouse();
        
        masterOp = new NbDialogOperator("New Master/Detail Form");
        new JButtonOperator(masterOp,"Finish").clickMouse();
        waitNoEvent(8000);
    }
    
    /** Tests avail. of JPA components */
    public void testGeneratedJpaStuff() {
        // persistance config file exists
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(TEST_PROJECT_NAME);
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
