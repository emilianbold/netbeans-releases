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
import gui.menu.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureMenus  {
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new MainMenu("testFileMenu", "File main menu"));
        suite.addTest(new MainMenu("testEditMenu", "Edit main menu"));
        suite.addTest(new MainMenu("testViewMenu", "View main menu"));
        suite.addTest(new MainMenu("testBuildMenu", "Build main menu"));
        suite.addTest(new MainMenu("testRunMenu", "Debug main menu"));
        suite.addTest(new MainMenu("testVersioningMenu", "Versioning main menu"));
        suite.addTest(new MainMenu("testWindowMenu", "Window main menu"));
        suite.addTest(new MainMenu("testHelpMenu", "Help main menu"));

        suite.addTest(new MainSubMenus("testFileOpenRecentProjectMenu", "File | Open Recent Project main menu"));
        suite.addTest(new MainSubMenus("testFileSetMainProjectMenu", "File | Set Main Project main menu"));
        suite.addTest(new MainSubMenus("testViewDocumentationIndicesMenu", "View | Documentation Indices main menu"));
        suite.addTest(new MainSubMenus("testViewToolbarsMenu", "View | Toolbars main menu"));
        suite.addTest(new MainSubMenus("testViewCodeFoldsMenu", "View | Code Folds main menu"));
        suite.addTest(new MainSubMenus("testRunRunOtherMenu", "Run | Run Other main menu"));
        suite.addTest(new MainSubMenus("testRunStackMenu", "Run | Stack main menu"));
        suite.addTest(new MainSubMenus("testVcsCommnadsMenu", "Versioning | Global Commands main menu"));
        suite.addTest(new MainSubMenus("testToolsI18nMenu", "Tools | Internationalization main menu"));
        suite.addTest(new MainSubMenus("testWinGuiMenu", "Window | GUI Editing main menu"));
        suite.addTest(new MainSubMenus("testWinDebuggingMenu", "Window | Debugging main menu"));
        suite.addTest(new MainSubMenus("testWinVersioningMenu", "Window | Versioning main menu"));
        suite.addTest(new MainSubMenus("testWinSelectDocumentNodeInMenu", "Window | Select Document Node in main menu"));
        
//TODO        suite.addTest(new ProjectNodePopupMenu("testProjectsView", "Project node popup in Projects View"));
//TODO        suite.addTest(new ProjectNodePopupMenu("testFilesView", "Project node popup in Files View"));
        
//TODO        suite.addTest(new LocalFilesystemNodePopupMenu("measureTime", "Local Filesystem node popup"));
//TODO        suite.addTest(new JarFSNodePopupMenu("measureTime", "Jar Filesystem node popup"));
        
//TODO        suite.addTest(new FolderNodePopupMenu("measureTime","Folder node popup"));
        
//TODO        suite.addTest(new JavaNodePopupMenu("measureTime", "Java File node popup"));
//TODO        suite.addTest(new TxtNodePopupMenu("measureTime", "Text File node popup"));
//TODO        suite.addTest(new XmlNodePopupMenu("measureTime", "Xml File node popup"));
//TODO        suite.addTest(new JSPNodePopupMenu("measureTime", "JSP File node popup"));

//TODO        suite.addTest(new JavaNodeUnselectedPopupMenu("measureTime", "Unselected Java File node popup")); //TODO
        
//TODO        suite.addTest(new ExplorerPopupSubMenus("testFilesystemsNodeSubMenu", "Mount submenu on Filesystems node"));
//TODO        suite.addTest(new ExplorerPopupSubMenus("testFolderNodeNewSubMenu", "New submenu on Folder node"));
        
//TODO        suite.addTest(new ToolsMenu("testJavaToolsMenu", "Tools main menu for Java node"));
//TODO        suite.addTest(new ToolsMenu("testXmlToolsMenu", "Tools main menu for XML node"));
//TODO        suite.addTest(new ToolsMenu("testTxtToolsMenu", "Tools main menu for text node"));

//TODO        suite.addTest(new SourceEditorPopupMenu("testPopupInTxt", "Plain text Editor popup"));
//TODO        suite.addTest(new SourceEditorPopupMenu("testPopupInXml", "XML Editor popup"));
//TODO        suite.addTest(new SourceEditorPopupMenu("testPopupOnMethod", "Java Editor Method popup"));
//TODO        suite.addTest(new SourceEditorPopupMenu("testPopupOnClassName", "Java Editor Class Name popup"));
        
        
//TODO        suite.addTest(new WebNodePopupMenu("measureTime", "Web Filesystem node popup"));  
//TODO        suite.addTest(new WebInfNodePopupMenu("measureTime", "WEB-INF node popup"));  
//TODO        suite.addTest(new WebXMLNodePopupMenu("measureTime", "web.xml node popup"));
        
//TODO        suite.addTest(new VCSRootNodePopupMenu("measureTime", "CVS Filesystem node popup"));
//TODO        suite.addTest(new VCSJavaNodePopupMenu("measureTime", "CVS Java node popup"));
        // it is no more in the production build suite.addTest(new ExplorerPopupSubMenus("testFilesystemsNodeSubMenuVCS", "Mount | Version Control submenu on Filesystems node"));
//TODO        suite.addTest(new ExplorerPopupSubMenus("testCVSFilesystemsNodeCVSSubMenu", "CVS submenu on CVS Filesystems node"));
        
//TODO        suite.addTest(new RuntimeServerRegistryPopupMenu("measureTime", "Runtime Server Registry popup"));
//TODO        suite.addTest(new RuntimeTomcatPopupMenu("measureTime", "Runtime Tomcat popup"));
        
//TODO        suite.addTest(new FormInspectorNodePopupMenu("measureTime", "Form Inspector node popup"));
//TODO        suite.addTest(new EditorDownButtonPopupMenu("measureTime", "Editor Down Button popup"));
        
        suite.addTest(new ToolsOptionsNodePopupMenu("measureTime", "Tools-Options node popup"));
      
        return suite;
    }
    
}
