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

package org.netbeans.test.xml.schema;

import org.netbeans.test.xml.schema.abe.*;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.zip.CRC32;
import javax.swing.tree.TreePath;
import junit.framework.TestSuite;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
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
 * @author Mikhail Matveev
 */

public class DVTestCase extends JellyTestCase {
    
    static final String [] m_aTestMethods = {
        "createLASchema",
        "DesignPane",
        //"createNewElement",
                //"createSchemaComponents",
                //"customizeSchema",
                //"checkSourceCRC",
                "applyDesignPattern"
    };
    
    static final String TEST_SCHEMA_NAME = "testSchema";
    static final String SCHEMA_EXTENSION = ".xsd";
    
    static SchemaMultiView multiView;
    static DesignViewOperator dvOperator;
    
    public DVTestCase(String arg0) {
        super(arg0);
    }
    
    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(DVTestCase.class.getName());
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new DVTestCase(strMethodName));
        }
        
        return testSuite;
    }
    
    
    public void createLASchema() {
        startTest();
        
        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        opNewFileWizard.selectCategory("XML");
        opNewFileWizard.selectFileType("Loan Application Sample Schema");
        opNewFileWizard.next();
        
        NewJavaFileNameLocationStepOperator opNewFileNameLocationStep = new NewJavaFileNameLocationStepOperator();
        opNewFileNameLocationStep.setObjectName(TEST_SCHEMA_NAME);
        opNewFileWizard.finish();
        
        endTest();
    }
    
    public void DesignPane(){
        startTest();        
        
        multiView=new SchemaMultiView(TEST_SCHEMA_NAME);
        multiView.switchToDesign();
        dvOperator=new DesignViewOperator(multiView.getTopComponentOperator());        
        
        // JemmyProperties.getCurrentOutput().printLine("Complex type root: " + dvOperator.getComplexTypesRoot().getCenterX() + " " + dvOperator.getComplexTypesRoot().getCenterY());                    
        // JemmyProperties.getCurrentOutput().printLine("Elements root: " + dvOperator.getElementsRoot().getCenterX() + " " + dvOperator.getElementsRoot().getCenterY());                           
        
        // dvOperator.getComplexTypesRoot().clickForPopup();
        
        DVNodeOperator autoLoan=new DVNodeOperator(dvOperator,"autoLoanApplication");
        
        autoLoan.addAttribute("TheAttr");
        autoLoan.pushKey(KeyEvent.VK_ENTER);
        autoLoan.addAttribute("TheAttr1");
        autoLoan.pushKey(KeyEvent.VK_ENTER);
        
//
//        autoLoan.setExpanded(false);
//        Helpers.waitNoEvent();
//        
//        autoLoan.clickForPopupRobot();
//        new JPopupMenuOperator().pushMenu("Properties");        
//        Helpers.waitNoEvent();
//        new JDialogOperator().close();
//        Helpers.waitNoEvent();
//
//        autoLoan.setExpanded(true);
//        Helpers.waitNoEvent();
//        
        Helpers.recurseComponent(1,dvOperator.getSource());
//        
//        DVNodeOperator term=new DVNodeOperator(autoLoan,"loan",true,true);
//        term.renameInplace("renameTerm");
//        
//        dvOperator.getNameSpace().renameInplace("new namespace");
//        Helpers.waitNoEvent();
//        
//        autoLoan.clickForPopupRobot();
//        new JPopupMenuOperator().pushMenu("Properties");        
//        Helpers.waitNoEvent();
//        new JDialogOperator().close();
//        Helpers.waitNoEvent();
        
        DVNodeOperator newEl=dvOperator.addElement("myElement");
        Helpers.waitNoEvent();
        newEl.renameInplace("inplaceNewName");
        Helpers.waitNoEvent();
        newEl.renameRefactor("refactorNewName");
        Helpers.waitNoEvent();
        newEl.delete();
        Helpers.waitNoEvent();
        
        dvOperator.getComplexTypesRoot();
        
        endTest();
    }
    
    public void createNewElement(){                
        
        startTest();        
        
        dvOperator.getElementsRoot().clickForPopupRobot();
        new JPopupMenuOperator().pushMenu("Add|Element");
        Helpers.waitNoEvent();
        dvOperator.getComplexTypesRoot().clickForPopupRobot();
        new JPopupMenuOperator().pushMenu("Add|Complex Type");
        Helpers.waitNoEvent();
        
        Helpers.recurseComponent(1,multiView.getTopComponentOperator().getSource());
        
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

    public void checkSourceCRC() {
        startTest();
        
        final long goldenCRC32 = 2295334600L;
        
        multiView.switchToSource();
        
        EditorOperator opEditor = new EditorOperator(TEST_SCHEMA_NAME);
        String strText = opEditor.getText();
        
        multiView.switchToDesign();
        
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

    protected void startTest(){
        super.startTest();
        Helpers.closeUMLWarningIfOpened();
    }

}
