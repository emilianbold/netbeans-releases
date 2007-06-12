/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.xml.schema;

import java.awt.Point;
import java.util.zip.CRC32;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import org.netbeans.test.xml.schema.lib.util.Helpers;

/**
 *
 * @author ca@netbeans.org
 */

public class AcceptanceTestCase extends JellyTestCase {
    
    static final String [] m_aTestMethods = {
        "createNewSchema",
                "createSchemaComponents",
                "customizeSchema",
                "checkSourceCRC",
                "refactorComplexType",
                "applyDesignPattern"
    };
    
    static final String TEST_SCHEMA_NAME = "testSchema";
    static final String SCHEMA_EXTENSION = ".xsd";
    
    public AcceptanceTestCase(String arg0) {
        super(arg0);
    }
    
    public static junit.framework.TestSuite suite() {
        junit.framework.TestSuite testSuite = new junit.framework.TestSuite("Acceptance suite");
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new AcceptanceTestCase(strMethodName));
        }
        
        return testSuite;
    }
    
    public void createNewSchema() {
        startTest();
        
        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        opNewFileWizard.selectCategory("XML");
        opNewFileWizard.selectFileType("XML Schema");
        opNewFileWizard.next();
        
        NewFileNameLocationStepOperator opNewFileNameLocationStep = new NewFileNameLocationStepOperator();
        opNewFileNameLocationStep.setObjectName(TEST_SCHEMA_NAME);
        opNewFileWizard.finish();
        
        TopComponentOperator opTopComponent = new TopComponentOperator(TEST_SCHEMA_NAME + SCHEMA_EXTENSION);
        
        endTest();
    }
    
    public void createSchemaComponents() {
        startTest();
        
        String[][] aComponentsMenu = {
            {"Complex Type...", "CT"},
            {"Simple Type...",  "ST"},
            {"Element...",      "E"},
            {"Attribute...",    "A"}
        };
        
        SchemaMultiView opMultiView = new SchemaMultiView(TEST_SCHEMA_NAME);
        JListOperator opList = opMultiView.getColumnListOperator(0);
        
        for (int i = 0; i <aComponentsMenu.length; i++) {
            Point p = opList.getClickPoint(0);
            opList.clickForPopup(p.x, p.y);
            new JPopupMenuOperator().pushMenuNoBlock("Add|" + aComponentsMenu[i][0]);
            Helpers.waitNoEvent();
            
            JDialogOperator opCustomizer = new JDialogOperator();
            new JTextFieldOperator(opCustomizer, 0).setText(aComponentsMenu[i][1]);
            new JButtonOperator(opCustomizer, "OK").pushNoBlock();
            Helpers.waitNoEvent();
        }
        
        endTest();
    }
    
    public void customizeSchema() {
        startTest();

        SchemaMultiView opMultiView = new SchemaMultiView(TEST_SCHEMA_NAME);
        
        //  Customize Element
        JListOperator opList0 = opMultiView.getColumnListOperator(0);
        opList0.selectItem("Elements");
        
        JListOperator opList1 = opMultiView.getColumnListOperator(1);
        callPopupOnListItem(opList1, "E", "Customize");
        
        JDialogOperator opCustomizer = new JDialogOperator();
        new JRadioButtonOperator(opCustomizer, "Use Existing Type").pushNoBlock();
        Helpers.waitNoEvent();
        
        JTreeOperator opTree = new JTreeOperator(opCustomizer);
        TreePath treePath = opTree.findPath("Complex Types|CT");
        opTree.selectPath(treePath);
        
        new JButtonOperator(opCustomizer, "OK").pushNoBlock();
        Helpers.waitNoEvent();
        
        // Customize Attribute
        opList0.selectItem("Attributes");
        
        opList1 = opMultiView.getColumnListOperator(1);
        callPopupOnListItem(opList1, "A", "Customize");
        
        opCustomizer = new JDialogOperator();
        new JRadioButtonOperator(opCustomizer, "Use Existing Type").pushNoBlock();
        Helpers.waitNoEvent();
        
        opTree = new JTreeOperator(opCustomizer);
        treePath = opTree.findPath("Simple Types|ST");
        opTree.selectPath(treePath);
        
        new JButtonOperator(opCustomizer, "OK").pushNoBlock();
        Helpers.waitNoEvent();
        
        // Customize Complex Type
        opList0.selectItem("Complex Types");
        
        opList1 = opMultiView.getColumnListOperator(1);
        callPopupOnListItem(opList1, "CT", "Add|Attribute Reference");
        
        opCustomizer = new JDialogOperator();
        
        opTree = new JTreeOperator(opCustomizer);
        treePath = opTree.findPath("Attributes|A");
        opTree.selectPath(treePath);
        
        new JButtonOperator(opCustomizer, "OK").pushNoBlock();
        Helpers.waitNoEvent();
        
        endTest();
    }
    
    public void checkSourceCRC() {
        startTest();
        
        final long goldenCRC32 = 2295334600L;
        
        SchemaMultiView opMultiView = new SchemaMultiView(TEST_SCHEMA_NAME);
        opMultiView.switchToSource();
        
        EditorOperator opEditor = new EditorOperator(TEST_SCHEMA_NAME);
        String strText = opEditor.getText();
        
        opMultiView.switchToSchema();
        
        strText = strText.replaceAll("[  [\t\f\r]]", "");
        Helpers.writeJemmyLog("{" + strText + "}");
        
        CRC32 crc32 = new CRC32();
        crc32.update(strText.getBytes());
        long checkSum = crc32.getValue();
        Helpers.writeJemmyLog("CRC32=" + checkSum);
        if ( checkSum != goldenCRC32) {
            fail("Schema source check sum doesn't match golden value");
        }
        
        endTest();
    }
    
    public void refactorComplexType() {
        startTest();
        
        SchemaMultiView opMultiView = new SchemaMultiView(TEST_SCHEMA_NAME);
        
        JListOperator opList0 = opMultiView.getColumnListOperator(0);
        opList0.selectItem("Complex Types");
        
        JListOperator opList1 = opMultiView.getColumnListOperator(1);
        callPopupOnListItem(opList1, "CT", "Refactor|Rename...");
        
        JDialogOperator opDialog = new JDialogOperator();
        new JTextFieldOperator(opDialog).setText("CT1");
        new JButtonOperator(opDialog, "Refactor").pushNoBlock();
        opDialog.waitClosed();
        
        opList0 = opMultiView.getColumnListOperator(0);
        opList0.selectItem("Elements");
        Helpers.waitNoEvent();
        
        opList1 = opMultiView.getColumnListOperator(1);
        opList1.selectItem("E");
        Helpers.waitNoEvent();
        
        JListOperator opList2 = opMultiView.getColumnListOperator(2);
        opList2.selectItem("CT1");
        
        opMultiView.switchToSource();
        boolean bValid = isSchemaValid(TEST_SCHEMA_NAME);
        opMultiView.switchToSchema();
        
        if (!bValid) {
            failInvalidSchema();
        }
        
        endTest();
    }
    
    public void applyDesignPattern() {
        startTest();
        
        ProjectsTabOperator pto = new ProjectsTabOperator();
        
        JTreeOperator opTree = pto.tree();
        
        // work with nodes
        ProjectRootNode prn = pto.getProjectRootNode("XSDTestProject");
        Node node = new Node(prn, "Source Packages|<default package>|" + TEST_SCHEMA_NAME + SCHEMA_EXTENSION);
        
        node.callPopup().pushMenuNoBlock("Apply Design Pattern...");
        Helpers.waitNoEvent();
        
        WizardOperator opWizard = new WizardOperator("Apply Design Pattern");
        new JRadioButtonOperator(opWizard, "Create a Single Global Element").pushNoBlock();
        Helpers.waitNoEvent();
        
        new JRadioButtonOperator(opWizard, "Do not Create Type(s)").pushNoBlock();
        Helpers.waitNoEvent();
        
        opWizard.finish();
        Helpers.waitNoEvent();
        
        SchemaMultiView opMultiView = new SchemaMultiView(TEST_SCHEMA_NAME);
        opMultiView.switchToSource();
        boolean bValid = isSchemaValid(TEST_SCHEMA_NAME);
        opMultiView.switchToSchema();
        
        if (!bValid) {
            failInvalidSchema();
        }
        
        endTest();
    }
    
    private boolean isSchemaValid(String strSchemaName) {
        boolean bValid = true;
        
        EditorOperator opEditor = new EditorOperator(strSchemaName);
        
        opEditor.clickForPopup();
        new JPopupMenuOperator().pushMenu("Validate XML");
        Helpers.waitNoEvent();
        
        OutputOperator opOutput = new OutputOperator();
        String strOutput = opOutput.getText();
        
        if (!strOutput.matches("\\D*0 Error\\(s\\),  0 Warning\\(s\\)\\.\\D*")) {
            Helpers.writeJemmyLog("Validate XML output:\n" + strOutput);
            bValid = false;
        }
        
        return bValid;
    }
    
    private void failInvalidSchema() {
        fail("Schema validation failed.");
    }
    
    private void callPopupOnListItem(JListOperator opList, String strItem, String strMenuPath) {
        opList.selectItem(strItem);
        
        int index = opList.getSelectedIndex();
        Point p = opList.getClickPoint(index);
        opList.clickForPopup(p.x, p.y);
        new JPopupMenuOperator().pushMenuNoBlock(strMenuPath);
    }
    
    public void tearDown() {
        new SaveAllAction().performAPI();
    }
}
