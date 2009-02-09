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

package org.netbeans.test.xslt;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import javax.swing.JTable;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.input.MouseEventDriver;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.test.xslt.lib.Helpers;
import org.netbeans.test.xslt.lib.PaletteOperator;
import org.netbeans.test.xslt.lib.XSLTEditorOperator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author ca@netbeans.org
 */

public class AcceptanceTestCase  extends JellyTestCase {
    
    static final String [] m_aTestMethods = {
        "createNewXSLTModule",
                "createSchemas",
                "createWSDL",
                "createXSLT"//,
              //  "editXSLT",
              //  "checkSource"
    };
    
    static final String SCHEMA_NAME = "schema";
    static final String SCHEMA_EXTENSION = ".xsd";
    static final String strWSDLFileName = "testWSDL";
    
    static final String FILE_NAME = "testXSLFile.xsl";
    
    static final String TEST_PROJECT_NAME = "AcceptanceTestProject";
    
    static final String CONCAT_TITLE = "Concat";
    
    public AcceptanceTestCase(String arg0) {
        super(arg0);
    }
    
    public static junit.framework.Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(AcceptanceTestCase.class)
                .addTest(m_aTestMethods)
                .clusters(".*")
                .enableModules(".*")
                .gui(true)
                );
    }
    
    public void createNewXSLTModule() {
        startTest();
        
        String strDirectory = System.getProperty("netbeans.user");

        MainWindowOperator.getDefault().maximize();
        
        NewProjectWizardOperator opWizard = NewProjectWizardOperator.invoke();
        opWizard.selectCategory("SOA");
        opWizard.selectProject("XSLT Module");
        opWizard.next();
        
        NewProjectNameLocationStepOperator opLocationStep = new NewProjectNameLocationStepOperator();
        opLocationStep.txtProjectName().setText(TEST_PROJECT_NAME);
        opLocationStep.txtProjectLocation().setText(strDirectory);
        opWizard.finish();
        
        endTest();
    }
    
    public void createSchemas() {
        startTest();
        
        for (int i = 0; i < 2; i++) {
            createSchema(SCHEMA_NAME + i, i);
        }
        
        endTest();
    }
    
    private void createSchema(String strName, int index) {
        final String strSampleSchemas = "XML";
        final String strFileType = "Purchase Order Sample Schema";
        final String strSrc = "src";
        
        final int FOLDER_FIELD_INDEX = 2;
        
        final String strNameSpace = "http://xml.netbeans.org/schema/test";
        
        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        opNewFileWizard.selectCategory(strSampleSchemas);
        opNewFileWizard.selectFileType(strFileType);
        opNewFileWizard.next();
        
        NewFileNameLocationStepOperator opNewFileNameLocationStep = new NewFileNameLocationStepOperator();
        opNewFileNameLocationStep.setObjectName(strName);
        new JTextFieldOperator(opNewFileNameLocationStep, FOLDER_FIELD_INDEX).setText(strSrc);
        opNewFileWizard.finish();
    }
    
    public void createWSDL() {
        startTest();
    
	pause(5000);
    
        final String JTABLE_CLASS = "javax.swing.JTable";
        
        final String strWSDLDocument = "XML";
        final String strFileType = "WSDL Document";
        
        final int FOLDER_FIELD_INDEX = 2;
        
        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        opNewFileWizard.selectCategory(strWSDLDocument);
        opNewFileWizard.selectFileType(strFileType);
        opNewFileWizard.next();
        
        final String strSrc = "src";
        
        NewFileNameLocationStepOperator opNewFileNameLocationStep = new NewFileNameLocationStepOperator();
        opNewFileNameLocationStep.setObjectName(strWSDLFileName);
        new JTextFieldOperator(opNewFileNameLocationStep, FOLDER_FIELD_INDEX).setText(strSrc);
        opNewFileWizard.next();
        
        JDialogOperator opAbstractConfigStep = new JDialogOperator("New WSDL Document");
        
        JComponentOperator opComponent = Helpers.getComponentOperator(opAbstractConfigStep, JTABLE_CLASS, 2);
        JTable table = (JTable) opComponent.getSource();
        JTableOperator opInputTable = new JTableOperator(table);
        
        opComponent = Helpers.getComponentOperator(opAbstractConfigStep, JTABLE_CLASS, 1);
        table = (JTable) opComponent.getSource();
        JTableOperator opOutputTable = new JTableOperator(table);
        
	pause(5000);

        Rectangle r = opInputTable.getCellRect(0, 1, false);
        new MouseEventDriver().clickMouse(opInputTable, r.x + r.width - 5, r.y + r.height - 5, 1, InputEvent.BUTTON1_MASK, 0, new Timeout("timeout", 500));
        
        JDialogOperator opSelectElementOrType = new JDialogOperator("Select Element Or Type");
        
        JTreeOperator opTree = new JTreeOperator(opSelectElementOrType);
        opTree.selectRow(0);
        TreePath treePath = opTree.findPath("By File|" + TEST_PROJECT_NAME + "|src/schema0.xsd|Elements|purchaseOrder");
        opTree.selectPath(treePath);
        new JButtonOperator(opSelectElementOrType, "OK").pushNoBlock();
        opSelectElementOrType.waitClosed();
        
	pause(5000);

        r = opOutputTable.getCellRect(0, 1, false);
        new MouseEventDriver().clickMouse(opOutputTable, r.x + r.width - 5, r.y + r.height - 5, 1, InputEvent.BUTTON1_MASK, 0, new Timeout("timeout", 500));
        
        opSelectElementOrType = new JDialogOperator("Select Element Or Type");
        opTree = new JTreeOperator(opSelectElementOrType);
        opTree.selectRow(0);
        treePath = opTree.findPath("By File|" + TEST_PROJECT_NAME + "|src/schema1.xsd|Elements|purchaseOrder");
        opTree.selectPath(treePath);
        new JButtonOperator(opSelectElementOrType, "OK").pushNoBlock();
        opSelectElementOrType.waitClosed();
        
        opNewFileWizard.next();
        
        JDialogOperator opConcreteConfigStep = new JDialogOperator("New WSDL Document");
        
        JComboBoxOperator opCombo = new JComboBoxOperator(opConcreteConfigStep);
        opCombo.selectItem("SOAP");
        
        JRadioButtonOperator opRadio = new JRadioButtonOperator(opConcreteConfigStep, "Document Literal");
        opRadio.pushNoBlock();
        
        opNewFileWizard.finish();
        
        endTest();
    }
    
    public void createXSLT() {
        startTest();
        
//        final String strSOADocument = "Service Oriented Architecture";
        final String strSOADocument = "SOA";
        final String strFileType = "XSLT Service";
        final String strXSLFile = "XSL File:";
        
        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        opNewFileWizard.selectCategory(strSOADocument);
        opNewFileWizard.selectFileType(strFileType);
        opNewFileWizard.next();
        
        new JRadioButtonOperator(opNewFileWizard, "Request-Reply Service").pushNoBlock();
        
        opNewFileWizard.next();
        
        opNewFileWizard.next();
        
        JLabelOperator opLabel = new JLabelOperator(opNewFileWizard, strXSLFile);
        Helpers.getTextFieldOpByLabel(opLabel).setText(FILE_NAME);
        
        opNewFileWizard.finish();
        
        endTest();
    }
    
    public void editXSLT() {
        startTest();
        
        XSLTEditorOperator opEditor = new XSLTEditorOperator(FILE_NAME);
        
        opEditor.dropPaletteItemOnCanvas(PaletteOperator.Groups.STRING, "concat", new Point(50, 50));
        
        opEditor.bindSourceToMethoid("billTo|street", CONCAT_TITLE, 0, 0);
        opEditor.bindSourceToMethoid("billTo|city", CONCAT_TITLE, 0, 1);
        opEditor.bindMethoidToTarget(CONCAT_TITLE, 0, "purchaseOrder|billTo|name");
        
        opEditor.bindSourceToTarget("shipTo|name", "purchaseOrder|shipTo|name");
        opEditor.bindSourceToTarget("shipTo|city", "purchaseOrder|shipTo|city");
        opEditor.bindSourceToTarget("shipTo|street", "purchaseOrder|shipTo|street");
        
        endTest();
    }
    
    public void checkSource() {
        final long goldenCheckSum = 181301011L; // 708657604L
        
        startTest();
        
        XSLTEditorOperator opEditor = new XSLTEditorOperator(FILE_NAME);
        
        opEditor.switchToSource();
        
        EditorOperator opNBEditor = new EditorOperator(FILE_NAME);
        String strText = opNBEditor.getText();
        Helpers.writeJemmyLog("{" + strText + "}");
        
        opEditor.switchToDesign();
        
        if (!Helpers.isCRC32Equal(strText, goldenCheckSum)) {
            fail("The source check sum doesn't match the golden value");
        }
        
        endTest();
    }
    

    public static void pause(int milliseconds) {
        //System.out.println("Paused for " + milliseconds);
        try {
            Thread.currentThread().sleep(milliseconds);
        } catch (Exception e) {
            
        }
    }
 
    
    public void tearDown() {
        new SaveAllAction().performAPI();
    }
}
