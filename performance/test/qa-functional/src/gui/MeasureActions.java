/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui;


import org.netbeans.junit.NbTestSuite;
import gui.action.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureActions  {
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        suite.addTest(new OpenFiles("testOpening20kBJavaFile", "Open Java file (20kB)"));
        suite.addTest(new OpenFilesWithOpenedEditor("testOpening20kBJavaFile", "Open Java file (20kB) if Editor opened"));

        suite.addTest(new OpenFiles("testOpening20kBTxtFile", "Open Txt file (20kB)"));
        suite.addTest(new OpenFilesWithOpenedEditor("testOpening20kBTxtFile", "Open Txt file (20kB) if Editor opened"));
        
        suite.addTest(new OpenFiles("testOpening20kBXmlFile", "Open Xml file (20kB)"));
        suite.addTest(new OpenFilesWithOpenedEditor("testOpening20kBXmlFile", "Open Xml file (20kB) if Editor opened"));
        
//TODO it doesn't work now       suite.addTest(new OpenFiles("testOpening20kBJSPFile", "Open JSP file"));
//TODO it doesn't work now       suite.addTest(new OpenFilesWithOpenedEditor("testOpening20kBJSPFile", "Open JSP file if Editor opened"));
        
        suite.addTest(new OpenFilesNoCloneableEditor("testOpening20kBPropertiesFile", "Open Properties file (20kB)"));
        suite.addTest(new OpenFilesNoCloneableEditorWithOpenedEditor("testOpening20kBPropertiesFile", "Open Properties file (20kB) if Editor opened"));
        suite.addTest(new OpenFilesNoCloneableEditor("testOpening20kBPictureFile", "Open Picture file (20kB)"));
        suite.addTest(new OpenFilesNoCloneableEditorWithOpenedEditor("testOpening20kBPictureFile", "Open Picture file (20kB) if Editor opened"));
        
//TODO Form designer isn't TopComponent        suite.addTest(new OpenFormFile("testOpening20kBFormFile", "Open Form file (20kB)"));
//TODO Form designer isn't TopComponent        suite.addTest(new OpenFormFileWithOpenedEditor("testOpening20kBFormFile", "Open Form file (20kB) if Editor opened"));        

        suite.addTest(new PasteInEditor("measureTime", "Paste in the editor"));
        suite.addTest(new PageUpPageDownInEditor("measureTime", "Press Page Up in the editor", true));
        suite.addTest(new PageUpPageDownInEditor("measureTime", "Press Page Down in the editor", false));
        
        suite.addTest(new JavaCompletionInEditor("measureTime", "Invoke Code Completion dialog in Editor"));
        suite.addTest(new TypingInEditor("measureTime", "Type a character in Editor"));
        
        
//TODO        suite.addTest(new ExpandNodesInExplorer("testExpandFolderWith50JavaFiles", "Expand folder with 50 java files"));
//TODO        suite.addTest(new ExpandNodesInExplorer("testExpandFolderWith100JavaFiles", "Expand folder with 100 java files"));
//TODO        suite.addTest(new ExpandNodesInExplorer("testExpandFolderWith100TxtFiles", "Expand folder with 100 txt files"));
//TODO        suite.addTest(new ExpandNodesInExplorer("testExpandFolderWith100XmlFiles", "Expand folder with 100 xml files"));
        
//TODO        suite.addTest(new RefreshFolder("testRefreshFolderWith50JavaFiles", "Refresh folder with 50 java files"));
//TODO        suite.addTest(new RefreshFolder("testRefreshFolderWith100JavaFiles", "Refresh folder with 100 java files"));
//TODO        suite.addTest(new RefreshFolder("testRefreshFolderWith100XmlFiles", "Refresh folder with 100 xml files"));
//TODO        suite.addTest(new RefreshFolder("testRefreshFolderWith100TxtFiles", "Refresh folder with 100 txt files"));
        
//TODO        suite.addTest(new ExpandNodesInNewFromTemplate("testExpandJavaClasses","Expand node Java Classes in New from Template"));
//TODO        suite.addTest(new ExpandNodesInNewFromTemplate("testExpandGUIForms","Expand node GUI Forms in New from Template"));
//TODO        suite.addTest(new ExpandNodesInNewFromTemplate("testExpandXML","Expand node XML in New from Template"));
//TODO        suite.addTest(new ExpandNodesInNewFromTemplate("testExpandOther","Expand node Other in New from Template"));

//TODO        suite.addTest(new ExpandNodesInOptions("testExpandEditorSettings", "Expand node Editor Settings in Tools | Options"));
        
//TODO        suite.addTest(new OpenFiles("testOpening20kBWebXMLFile", "Open web.xml file"));
//TODO        suite.addTest(new OpenFilesWithOpenedEditor("testOpening20kBWebXMLFile", "Open web.xml file if Editor opened"));
        
//TODO        suite.addTest(new ExpandNodesInComponentInspector("testExpandContainerJFrame","Expand Container node in Component Inspector"));
        
        
//TODO        suite.addTest(new FinishNewFromTemplateWizard("testCreateJavaMainClass","Finish new wizard | create Java Main Class"));
//TODO        suite.addTest(new FinishNewFromTemplateWizardWithOpenedEditor("testCreateJavaMainClass","Finish new wizard | create Java Main Class if Editor opened"));
        
//TODO        suite.addTest(new FinishMountWizardLocalFilesystem("measureTime","Finish mount wizard | Local Filesystem"));
//TODO        suite.addTest(new FinishMountWizardJARFilesystem("measureTime","Finish mount wizard | JAR Filesystem"));
        //TODO solve problem with test suite.addTest(new FinishMountWizardCVSFilesystem("measureTime","Finish mount wizard | CVS Filesystem"));
        
//TODO        suite.addTest(new UnmountLocalFilesystem("measureTime","Unmount Local Filesystem"));
//TODO        suite.addTest(new UnmountJARFilesystem("measureTime","Unmount JAR Filesystem"));
        
//TODO        suite.addTest(new ExpandJARFilesystemNode("measureTime","Expand JAR Filesystem Node"));
//TODO        suite.addTest(new ExpandVCSFilesystemNode("measureTime","Expand VCS Filesystem Node"));
        
//TODO        suite.addTest(new DeleteFolder("testDeleteFolderWith100JavaFiles", "Delete folder with 100 java files"));
//TODO        suite.addTest(new DeleteFolder("testDeleteFolderWith500JavaFiles", "Delete folder with 500 java files"));
        
//TODO        suite.addTest(new CloseEditor("testClosing20kBJavaFile", "Close Java file (20kB)"));
//TODO        suite.addTest(new CloseEditor("testClosing20kBFormFile", "Close Form file (20kB)"));
//TODO        suite.addTest(new CloseAllEditors("testClosing20JavaFiles", "Close All Documents if 20 Java files opened"));
        
//TODO        suite.addTest(new CloseEditorTab("testClosingTab", "Close on tab from Editor window"));
        
//TODO        suite.addTest(new CloseEditorModified("testClosingModifiedJavaFile", "Close modified Java file"));
        
//TODO        suite.addTest(new SaveModifiedFile("testSaveModifiedJavaFile", "Save modified Java file"));
        
        
        return suite;
    }
    
}
