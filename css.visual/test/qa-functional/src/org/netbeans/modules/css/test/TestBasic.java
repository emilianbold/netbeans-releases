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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.css.test;

import java.io.File;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.modules.web.NavigatorOperator;
import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.modules.css.test.operator.StyleRuleEditorOperator;

/**
 *
 * @author Jindrich Sedek
 */
public class TestBasic extends CSSTest{
    private static final String fileType = Bundle.getString("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet/CascadeStyleSheet.css");
    private static final String wizardTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_NewFileWizard_Title");
    private static final String createRuleAction = Bundle.getString("org.netbeans.modules.css.actions.Bundle", "Create_Rule");
    
    public TestBasic(String name) {
        super(name);
    }
    
    public void testNewCSS() throws Exception {
        File dataDir = this.getDataDir();
        ProjectSupport.openProject(dataDir + File.separator + projectName);
        new NbDialogOperator("Open Project").close();// close server reference problem
        System.out.println("running testNewCSS + " + dataDir + projectName);
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke(wizardTitle);
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Web");
        nfwo.selectFileType(fileType);
        nfwo.next();
        WizardOperator newCSSFile = new WizardOperator("New CSS File");
        new JTextFieldOperator(newCSSFile, 0).setText(newFileName);//FileName
        new JTextFieldOperator(newCSSFile, 2).setText("css");//Folder
        newCSSFile.finish();
        String text = new EditorOperator(newFileName).getText();
        assertTrue(text.contains("root"));
        assertTrue(text.contains("display:"));
        assertTrue(text.contains("block"));
    }
    
    public void testAddRule() throws Exception{
        EditorOperator eop = new EditorOperator(newFileName);
        eop.setCaretPositionToLine(rootRuleLineNumber);
        AbstractButtonOperator abo = eop.getToolbarButton(createRuleAction);
        abo.push();
        StyleRuleEditorOperator styleOperator = new StyleRuleEditorOperator();
        styleOperator.selectHtmlElement("button");
        styleOperator.addRule();
        styleOperator.selectClass("caption", "first");
        styleOperator.addRule();
        styleOperator.selectElementID("33");
        styleOperator.addRule();
        assertEquals("button caption.first #33", styleOperator.getPreview());
        styleOperator.up("33");
        assertEquals("button #33 caption.first", styleOperator.getPreview());
        styleOperator.down("button");
        assertEquals("#33 button caption.first", styleOperator.getPreview());
        styleOperator.ok();
        assertTrue(eop.getText().contains("#33 button caption.first"));
    }
    
    public void testNavigator() throws Exception{
        String navigatorTestFile = "navigatorTest.css";
        openFile(newFileName);
        NavigatorOperator navigatorOperator = NavigatorOperator.invokeNavigator();
        assertNotNull(navigatorOperator);
        JTreeOperator treeOperator = navigatorOperator.getTree();
        Object root = treeOperator.getRoot();
        assertNotNull(root);
        assertEquals("NUMBER OF ROOT CHILD", 2, treeOperator.getChildCount(root));
        openFile(navigatorTestFile).setVerification(true);
        treeOperator = navigatorOperator.getTree();
        root = treeOperator.getRoot();
        assertNotNull(root);
        assertEquals("NUMBER OF ROOT CHILD", 2, treeOperator.getChildCount(root));
        Object firstChild = treeOperator.getChild(root, 0);
        assertEquals("NUMBER OF @MEDIA SCREEN CHILD", 2, treeOperator.getChildCount(firstChild));
        Object aChild = treeOperator.getChild(firstChild, 1);
        assertNotNull("A rule", aChild);
        TreePath path = new TreePath(new Object[]{root, firstChild, aChild});
        treeOperator.clickOnPath(path, 2);
        //        new EditorOperator(navigatorTestFile).
        JEditorPaneOperator editorPane = new EditorOperator(navigatorTestFile).txtEditorPane();
        assertEquals("CARET POSSITION ", 374, editorPane.getCaretPosition());
    }
    
}
