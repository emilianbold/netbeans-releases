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
package org.netbeans.test.xml.schema;

import java.awt.Point;
import junit.framework.TestSuite;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import org.netbeans.test.xml.schema.lib.util.Helpers;

/**
 *
 * @author Tony Beckham
 */
public class RefactoringTestCase extends JellyTestCase {

    static final String[] m_aTestMethods = {
        "createNewPackage",
        "createLongNameSchema"
//        "createSchemaComponents",
//        "refactorRenamePreviewElement",
//        "undoRenameElement",
//        "refactorRenameElement",
//        "refactorRenamePreviewSchema",
//        "refactorRenameSchema",
//        "undoRenameSchema",
//        "refactorSafeDeletePreview",
//        "refactorSafeDelete",
//        "undoSafeDelete"
//        "addElement"
    };
    static final String LONG_SCHEMA_NAME = randomLongName();
    static final String SCHEMA_EXTENSION = ".xsd";
    static final String NEW_SCHEMA_NAME = "Renamed";

    public RefactoringTestCase(String arg0) {
        super(arg0);
        Helpers.closeMimeWarningIfOpened();
    }

    public static TestSuite suite() {
        TestSuite testSuite = new TestSuite(RefactoringTestCase.class.getName());

        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new RefactoringTestCase(strMethodName));
        }

        return testSuite;
    }

    /**
     * Creates a new schema with a random very long name of lower case letters as a stress test
     * Should be first method to be run in this test bag
     */
    public void createLongNameSchema() {
        startTest();

        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        opNewFileWizard.selectCategory("XML");
        opNewFileWizard.selectFileType("XML Schema");
        opNewFileWizard.next();
        NewJavaFileNameLocationStepOperator opNewFileNameLocationStep = new NewJavaFileNameLocationStepOperator();
        opNewFileNameLocationStep.setObjectName(LONG_SCHEMA_NAME);
//        opNewFileNameLocationStep.setFolder("NotDefaultPackage");
        
    }

    /**
     * refactorRenameSchema() will rename a schema using refactor context menu item
     * The name is changed to the value of NEW_SCHEMA_NAME
     * Should be called after refactorRenamePreviewSchema
     */
    public void refactorRenameSchema() {
        startTest();

        ProjectsTabOperator pto = new ProjectsTabOperator();

        // work with nodes
        ProjectRootNode prn = pto.getProjectRootNode("XSDTestProject");
        Node node = new Node(prn, "Source Packages|<default package>|PreviewRefactor" + NEW_SCHEMA_NAME + SCHEMA_EXTENSION);

        node.callPopup().pushMenuNoBlock("Refactor|Rename...");
        Helpers.waitNoEvent();

        WizardOperator opWizard = new WizardOperator("File Rename");
        new JTextFieldOperator(opWizard, 0).setText(NEW_SCHEMA_NAME);
        new JButtonOperator(opWizard, "Refactor").pushNoBlock();
        opWizard.waitClosed();
        Helpers.waitNoEvent();

        endTest();
    }

    /**
     * Will rename schema using refactor.  First the preview of changes then do the refactoring.  
     * Name of schema is changed to PreviewRefactor<NEW_SCHEMA_NAME>
     * 
     * Must be called after createLongNameSchema()
     */
    public void refactorRenamePreviewSchema() {
        startTest();

        ProjectsTabOperator pto = new ProjectsTabOperator();

        // work with nodes
        ProjectRootNode prn = pto.getProjectRootNode("XSDTestProject");
        Node node = new Node(prn, "Source Packages|<default package>|" + LONG_SCHEMA_NAME + SCHEMA_EXTENSION);

        node.callPopup().pushMenuNoBlock("Refactor|Rename...");
        Helpers.waitNoEvent();

        WizardOperator opWizard = new WizardOperator("File Rename");
        new JTextFieldOperator(opWizard, 0).setText("PreviewRefactor" + NEW_SCHEMA_NAME);
        new JButtonOperator(opWizard, "Preview").pushNoBlock();

        Action action = new Action("Window|Output|Refactoring Preview", null);
        action.perform();

        new JButtonOperator(pto.getWindowContainerOperator(), "Do Refactoring").pushNoBlock();
        Helpers.waitNoEvent();

        endTest();
    }

    /**
     * To be called after refactorRenameSchema()
     * refactorSafeDelete() will remove Element "E" using Refactor|Safe Delete
     */
    public void refactorSafeDelete() {
        startTest();

        SchemaMultiView opMultiView = new SchemaMultiView("PreviewRefactor" + NEW_SCHEMA_NAME);

        //  Get Element
        JListOperator opList0 = opMultiView.getColumnListOperator(0);
        opList0.selectItem("Elements");

        JListOperator opList1 = opMultiView.getColumnListOperator(1);
        callPopupOnListItem(opList1, "MyElementRenamed", "Refactor|Safe Delete...");
        Helpers.waitNoEvent();

        WizardOperator opWizard = new WizardOperator("Safe Delete");
        new JButtonOperator(opWizard, "Refactor").pushNoBlock();
        opWizard.waitClosed();
        Helpers.waitNoEvent();

        endTest();
    }

    /**
     * To be called after refactorRenameSchema()
     * refactorSafeDeletePreview() will remove Complex Type "CT" using Refactor|Safe Delete
     * Preview of changes is requested then changes done. 
     */
    public void refactorSafeDeletePreview() {
        startTest();

        SchemaMultiView opMultiView = new SchemaMultiView("PreviewRefactor" + NEW_SCHEMA_NAME);

        //  Get Element
        JListOperator opList0 = opMultiView.getColumnListOperator(0);
        opList0.selectItem("Complex Types");

        JListOperator opList1 = opMultiView.getColumnListOperator(1);
        callPopupOnListItem(opList1, "MyCT", "Refactor|Safe Delete...");
        Helpers.waitNoEvent();

        WizardOperator opWizard = new WizardOperator("Safe Delete");
        new JButtonOperator(opWizard, "Preview").pushNoBlock();

        WindowOperator wo = new WindowOperator();
        new JButtonOperator(wo, "Do Refactoring").pushNoBlock();
        Helpers.waitNoEvent();

        endTest();
    }

    public void createSchemaComponents() {
        startTest();

        String[][] aComponentsMenu = {
            {"Complex Type...", "CT"},
            {"Simple Type...", "ST"},
            {"Element...", "E"},
            {"Attribute...", "A"},
            {"Element...", "MyElement"},
            {"Complex Type...", "MyCT"}
        };

        SchemaMultiView opMultiView = new SchemaMultiView(LONG_SCHEMA_NAME);
        JListOperator opList = opMultiView.getColumnListOperator(0);

        for (int i = 0; i < aComponentsMenu.length; i++) {
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

    private void callPopupOnListItem(JListOperator opList, String strItem, String strMenuPath) {
        opList.selectItem(strItem);

        int index = opList.getSelectedIndex();
        Point p = opList.getClickPoint(index);
        opList.clickForPopup(p.x, p.y);
        new JPopupMenuOperator().pushMenuNoBlock(strMenuPath);
    }

    @Override
    protected void startTest() {
        super.startTest();
        Helpers.closeUMLWarningIfOpened();
//        Helpers.closeMimeWarningIfOpened();
    }

    /**
     * randomLongName() will create a random string of 236 lower case letters a-z
     * @return
     */
    public static String randomLongName() {
        char[] str = new char[235];
        for (int i = 0; i < str.length; ++i) {
            str[i] = (char) ('a' + (int) Math.floor(Math.random() * 26D));
        }
        return new String(str);
    }

    /**
     * Simply add a new element "MyElement" to schema <NEW_SCHEMA_NAME>
     */
    public void addElement() {
        startTest();

        SchemaMultiView opMultiView = new SchemaMultiView("PreviewRefactor" + NEW_SCHEMA_NAME);
        JListOperator opList = opMultiView.getColumnListOperator(0);

        Point p = opList.getClickPoint(0);
        opList.clickForPopup(p.x, p.y);
        new JPopupMenuOperator().pushMenuNoBlock("Add|Element...");
        Helpers.waitNoEvent();

        JDialogOperator opCustomizer = new JDialogOperator();
        new JTextFieldOperator(opCustomizer, 0).setText("newElement");
        new JButtonOperator(opCustomizer, "OK").pushNoBlock();
        Helpers.waitNoEvent();

        endTest();
    }

    /**
     * undoSafeDelete() will undo delete of element after safe delete
     */
    public void undoSafeDelete() {
        startTest();

        Action action = new Action("Refactor|Undo [Delete MyElementRenamed]", null);
        action.perform();

        endTest();
    }

    public void refactorRenameElement() {
        startTest();

        SchemaMultiView opMultiView = new SchemaMultiView(LONG_SCHEMA_NAME);

        //  Get Element
        JListOperator opList0 = opMultiView.getColumnListOperator(0);
        opList0.selectItem("Elements");

        JListOperator opList1 = opMultiView.getColumnListOperator(1);
        callPopupOnListItem(opList1, "MyElement", "Refactor|Rename...");


        WizardOperator opWizard = new WizardOperator("Rename");
        new JTextFieldOperator(opWizard, 0).setText("MyElementRenamed");
        new JButtonOperator(opWizard, "Refactor").pushNoBlock();
        opWizard.waitClosed();

        Helpers.waitNoEvent();

        endTest();
    }

    public void refactorRenamePreviewElement() {
        startTest();

        SchemaMultiView opMultiView = new SchemaMultiView(LONG_SCHEMA_NAME);

        //  Get Element
        JListOperator opList0 = opMultiView.getColumnListOperator(0);
        opList0.selectItem("Elements");

        JListOperator opList1 = opMultiView.getColumnListOperator(1);
        callPopupOnListItem(opList1, "MyElement", "Refactor|Rename...");

        WizardOperator opWizard = new WizardOperator("Rename");
        new JTextFieldOperator(opWizard, 0).setText("MyElementRenamed");
        new JButtonOperator(opWizard, "Preview").pushNoBlock();

        Action action = new Action("Window|Output|Refactoring Preview", null);
        action.perform();

        WindowOperator wo = new WindowOperator();
        new JButtonOperator(wo, "Do Refactoring").pushNoBlock();
        Helpers.waitNoEvent();

        endTest();
    }

    public void undoRenameElement() {
        startTest();

        Action action = new Action("Refactor|Undo [Rename]", null);
        action.perform();

        endTest();
    }

    public void undoRenameSchema() {
        startTest();

        Action action = new Action("Refactor|Undo [File Rename]", null);
        action.perform();

        endTest();
    }

    public void createNewPackage() {
        startTest();

        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        opNewFileWizard.selectCategory("Java");
        opNewFileWizard.selectFileType("Java Package");
        opNewFileWizard.next();

        NewJavaFileNameLocationStepOperator opNewFileNameLocationStep = new NewJavaFileNameLocationStepOperator();
        opNewFileNameLocationStep.setObjectName("NotDefaultPackage");
        opNewFileWizard.finish();

        endTest();
    }
}



