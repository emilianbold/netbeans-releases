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
 * Software is Sun Microsystems, Inc. Portions Copyright 2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * TestGenerateJavadoc.java
 *
 * Created on February 3, 2003, 3:56 PM
 */

package org.netbeans.test.gui.javadoc;

import java.io.File;

import org.netbeans.jellytools.*;

import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.PropertiesAction;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;

import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/** JUnit test suite with Jemmy support
 *
 * @author mk97936
 * @version 1.0
 */
public class TestGenerateJavadoc extends JavadocTestCase {
    
    public static final String sep = File.separator;
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public TestGenerateJavadoc(String testName) {
        super(testName);
    }
    
    protected void setUp() {
        new PropertiesAction().perform();
    }
    
    // -------------------------------------------------------------------------
    
   /* public void testGenerate() {
        
        String userHome = System.getProperty("netbeans.user"); // NOI18N
        RepositoryTabOperator repoTabOper = RepositoryTabOperator.invoke();
        Node topNode = new Node(repoTabOper.getRootNode(), 0);
        
        String sampledirPath = topNode.getPath() + sep + "org" + sep + "netbeans" + sep + "test" + sep + // NOI18N
                               "gui" + sep + "javadoc" + sep + "data" + sep + "sampledir"; // NOI18N
        
        repoTabOper.mountLocalDirectoryAPI(sampledirPath); // NOI18N
        
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 120000); // NOI18N
        Action generateJDoc = new Action(toolsMainMenuItem + "|" + generateMenuItem, // NOI18N
                                         toolsPopupMenuItem + "|" + generateMenuItem); // NOI18N
        
        // javadoc is generated to default location ${netbeans.user}/javadoc
        
        // generate for MemoryView.java
        Node memoryViewNode = new Node(new Node(repoTabOper.getRootNode(), sampledirPath), 
                                       "examples|imageviewer|ImageViewer"); // NOI18N
        generateJDoc.perform(memoryViewNode);
        
        // question about showing javadoc in browser
        NbDialogOperator questionDialogOper = new NbDialogOperator(questionWinTitle);
        questionDialogOper.no();
        
        verifyCommonJdocFiles(userHome, "javadoc");
        assertTrue("ImageViewer doesn't exist!", new File(userHome + sep + "javadoc" + sep + "examples" + sep + // NOI18N
                   "imageviewer" + sep + "ImageViewer.html").exists()); // NOI18N
        new EventTool().waitNoEvent(1000);
        
        // generate for colorpicker package
        Node colorPickerNode = new Node(new Node(repoTabOper.getRootNode(), sampledirPath), "examples|colorpicker"); // NOI18N        
        generateJDoc.perform(colorPickerNode);
        
        // question about showing javadoc in browser
        NbDialogOperator questionDialogOper_2 = new NbDialogOperator(questionWinTitle);
        questionDialogOper_2.no();
        
        verifyCommonJdocFiles(userHome, "javadoc");
        assertTrue("ColorPicker doesn't exist!", new File(userHome + sep + "javadoc" + sep + "examples" + sep + // NOI18N
                   "colorpicker" + sep + "ColorPicker.html").exists()); // NOI18N
        new EventTool().waitNoEvent(1000);
        
        // generate for examples package
        Node examplesNode = new Node(new Node(repoTabOper.getRootNode(), sampledirPath), "examples"); // NOI18N        
        generateJDoc.perform(examplesNode);
        
        // question about showing javadoc in browser
        NbDialogOperator questionDialogOper_3 = new NbDialogOperator(questionWinTitle);
        questionDialogOper_3.no();
        
        verifyCommonJdocFiles(userHome, "javadoc");
//        verifyCommonJdocFilesInFolder(userHome, "javadoc" + sep + "examples" + sep + "clock");
        verifyCommonJdocFilesInFolder(userHome, "javadoc" + sep + "examples" + sep + "imageviewer");
//        verifyCommonJdocFilesInFolder(userHome, "javadoc" + sep + "examples" + sep + "advanced");
        verifyCommonJdocFilesInFolder(userHome, "javadoc" + sep + "examples" + sep + "texteditor");
        verifyCommonJdocFilesInFolder(userHome, "javadoc" + sep + "examples" + sep + "colorpicker");
        
//        assertTrue("ClockFrame doesn't exist!", new File(userHome + sep + "javadoc" + sep + "examples" + sep + // NOI18N
//                   "clock" + sep + "ClockFrame.html").exists()); // NOI18N
        assertTrue("ImageFrame doesn't exist!", new File(userHome + sep + "javadoc" + sep + "examples" + sep + // NOI18N
                   "imageviewer" + sep + "ImageFrame.html").exists()); // NOI18N
        assertTrue("ImageViewer doesn't exist!", new File(userHome + sep + "javadoc" + sep + "examples" + sep + // NOI18N
                   "imageviewer" + sep + "ImageViewer.html").exists()); // NOI18N
//        assertTrue("MemoryView doesn't exist!", new File(userHome + sep + "javadoc" + sep + "examples" + sep + // NOI18N
//                   "advanced" + sep + "MemoryView.html").exists()); // NOI18N
        assertTrue("Ted doesn't exist!", new File(userHome + sep + "javadoc" + sep + "examples" + sep + // NOI18N
                   "texteditor" + sep + "Ted.html").exists()); // NOI18N
        assertTrue("About doesn't exist!", new File(userHome + sep + "javadoc" + sep + "examples" + sep + // NOI18N
                   "texteditor" + sep + "About.html").exists()); // NOI18N
        assertTrue("Finder doesn't exist!", new File(userHome + sep + "javadoc" + sep + "examples" + sep + // NOI18N
                   "texteditor" + sep + "Finder.html").exists()); // NOI18N
        assertTrue("ColorPicker doesn't exist!", new File(userHome + sep + "javadoc" + sep + "examples" + sep + // NOI18N
                   "colorpicker" + sep + "ColorPicker.html").exists()); // NOI18N
        new EventTool().waitNoEvent(1000);
        
    }*/
    
    private void verifyCommonJdocFiles(String base, String folder) {
        assertTrue("index.html doesn't exist!", new File(base + sep + folder + sep + "index.html").exists()); // NOI18N
        assertTrue("index-all.html doesn't exist!", new File(base + sep + folder + sep + "index-all.html").exists()); // NOI18N
        assertTrue("allclasses-frame.html doesn't exist!", new File(base + sep + folder + sep + "allclasses-frame.html").exists()); // NOI18N
        assertTrue("allclasses-noframe.html doesn't exist!", new File(base + sep + folder + sep + "allclasses-noframe.html").exists()); // NOI18N
        assertTrue("packages.html doesn't exist!", new File(base + sep + folder + sep + "packages.html").exists()); // NOI18N
        assertTrue("stylesheet.css doesn't exist!", new File(base + sep + folder + sep + "stylesheet.css").exists()); // NOI18N
        assertTrue("package-list doesn't exist!", new File(base + sep + folder + sep + "package-list").exists()); // NOI18N
        assertTrue("help-doc.html doesn't exist!", new File(base + sep + folder + sep + "help-doc.html").exists()); // NOI18N
        assertTrue("overview-tree.html doesn't exist!", new File(base + sep + folder + sep + "overview-tree.html").exists()); // NOI18N
    }
    
    private void verifyCommonJdocFilesInFolder(String base, String folder) {
        assertTrue("package-frame.html doesn't exist!", new File(base + sep + folder + sep + "package-frame.html").exists()); // NOI18N
        assertTrue("package-summary.html doesn't exist!", new File(base + sep + folder + sep + "package-summary.html").exists()); // NOI18N
        assertTrue("package-tree.html doesn't exist!", new File(base + sep + folder + sep + "package-tree.html").exists()); // NOI18N
    }
    
  /*  public void testGenerateToFolder() {
        
        String userHome = System.getProperty("netbeans.user"); // NOI18N
        RepositoryTabOperator repoTabOper = RepositoryTabOperator.invoke();
        Node topNode = new Node(repoTabOper.getRootNode(), 0);
        
        String sampledirPath = topNode.getPath() + sep + "org" + sep + "netbeans" + sep + "test" + sep + // NOI18N
                               "gui" + sep + "javadoc" + sep + "data" + sep + "sampledir"; // NOI18N
        
        repoTabOper.mountLocalDirectoryAPI(sampledirPath); // NOI18N
        Action generateJDoc = new Action(toolsMainMenuItem + "|" + generateMenuItem, // NOI18N
                                         toolsPopupMenuItem + "|" + generateMenuItem); // NOI18N
        
        // set property "Ask for Destination Directory" to true
        OptionsOperator optionsOper = OptionsOperator.invoke();
        optionsOper.selectOption("Code Documentation|Documentation");
        PropertySheetOperator propertiesTab = new PropertySheetOperator(optionsOper);
        Property askForDestProp = new Property(propertiesTab, "Ask for Destination Directory");
        askForDestProp.setValue("true");
        optionsOper.close();
        // -----
        
        Node imageviewerNode = new Node(new Node(repoTabOper.getRootNode(), sampledirPath), "examples|imageviewer"); // NOI18N        
        generateJDoc.perform(imageviewerNode);
        
        NbDialogOperator dialogOper = new NbDialogOperator("Javadoc Destination Directory");
        JTextFieldOperator textOper = new JTextFieldOperator(dialogOper);
        textOper.clearText();
        textOper.typeText(userHome + sep + "javadoc_1");
        dialogOper.ok();
        
        // question about creating non existing folder
        NbDialogOperator questionDialogOper = new NbDialogOperator(questionWinTitle);
        questionDialogOper.ok();
        
        // question about showing javadoc in browser
        NbDialogOperator questionDialogOper_2 = new NbDialogOperator(questionWinTitle);
        questionDialogOper_2.no();
        
        verifyCommonJdocFiles(userHome, "javadoc_1");
        assertTrue("ImageViewer doesn't exist!", new File(userHome + sep + "javadoc_1" + sep + "examples" + sep + // NOI18N
                   "imageviewer" + sep + "ImageViewer.html").exists()); // NOI18N
        new EventTool().waitNoEvent(1000);
        
    }*/
    
    public void testCreateDoclet() {
        
        String [] docletSettings = new String [] {"Author", "Bottom", "Charset", "Doc Title", "Footer", "Group",
            "Header", "Help File", "Link", "Linkoffline", "No Deprecated", "No Deprecated List", "No Help", "No Index", 
            "No Navbar", "No Tree", "Split Index", "Style Sheet File", "Use", "Version", "Window Title"};
        String [] docletValues = new String [] {"false", "", "", "", "", "", "", "", "", "", "false", "false", "false",
            "false", "false", "false", "false", "", "false", "false", ""};
        
        // create new doclet with name TestDoclet
        OptionsOperator optionsOper = OptionsOperator.invoke();
        JTreeOperator optionsTreeOper = optionsOper.treeTable().tree();
        Node node = new Node(optionsTreeOper, "Code Documentation|Doclets");
        ActionNoBlock newAction = new ActionNoBlock(null, "New|Standard Doclet");
        newAction.perform(node);
        WizardOperator newWizard = new WizardOperator("New Wizard - Standard Doclet");
        JTextFieldOperator nameOper = new JTextFieldOperator(newWizard);
        nameOper.typeText("TestDoclet");
        newWizard.finish();
        
        // test default doclet values
        optionsOper.selectOption("Code Documentation|Doclets|TestDoclet");
        PropertySheetOperator propertiesTab = new PropertySheetOperator(optionsOper);
        for (int i = 0; i < docletSettings.length; i++) {
            Property docletProp = new Property(propertiesTab, docletSettings[i]);
            assertTrue("Default value of " + docletSettings[i] + " is not " + docletValues[i], 
                       docletProp.getValue().indexOf(docletValues[i]) != -1);
        }
        
        optionsOper.close();
        
    }
//    
    public void testCreateExecutor() {
        
        // Doclets setting excluded
        String [] execSettings = new String [] {"1.1 style", "Enable JDK 1.4 source", "Encoding", 
            "Extdirs", "External Process", "Locale", "Members", "Overview", "Recursive", "Verbose"};
        String [] execValues = new String [] {"false", "false", "", "", "External Javadoc Executor",
            "", "protected", "", "true", "false"};
        
        OptionsOperator optionsOper = OptionsOperator.invoke();
        JTreeOperator optionsTreeOper = optionsOper.treeTable().tree();
        Node node = new Node(optionsTreeOper, "Code Documentation|Javadoc Executors");
        ActionNoBlock newAction = new ActionNoBlock(null, "New|External Javadoc");
        newAction.perform(node);
        WizardOperator newWizard = new WizardOperator("New Wizard - External Javadoc");
        JTextFieldOperator nameOper = new JTextFieldOperator(newWizard);
        nameOper.typeText("TestExecutor");
        newWizard.finish();
        
        // test default executor values
        optionsOper.selectOption("Code Documentation|Javadoc Executors|TestExecutor");
        PropertySheetOperator propertiesTab = new PropertySheetOperator(optionsOper);
        for (int i = 0; i < execSettings.length; i++) {
            Property execProp = new Property(propertiesTab, execSettings[i]);
            assertTrue("Default value of " + execSettings[i] + " is not " + execValues[i], 
                       execProp.getValue().indexOf(execValues[i]) != -1);
        }
        
        optionsOper.close();
    }
    
    private static final String toolsPopupMenuItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "CTL_Tools");
    private static final String toolsMainMenuItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Tools");
    private static final String generateMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.javadoc.Bundle", "CTL_ActionGenerate");
    private static final String questionWinTitle = Bundle.getStringTrimmed("org.openide.Bundle", "NTF_QuestionTitle");
    
}
