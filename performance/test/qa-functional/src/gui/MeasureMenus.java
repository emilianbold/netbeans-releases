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
        suite.addTest(new MainSubMenus("testWinDebuggingMenu", "Window | Debug main menu"));
        suite.addTest(new MainSubMenus("testWinVersioningMenu", "Window | Versioning main menu"));
        suite.addTest(new MainSubMenus("testWinSelectDocumentNodeInMenu", "Window | Select Document Node in main menu"));
        
        suite.addTest(new ProjectsViewPopupMenu("testProjectNodePopupMenu", "Project node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testSourcePackagesPopupMenu", "Source Packages node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testTestPackagesPopupMenu", "Test Packages node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testPackagePopupMenu", "Package node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testJavaFilePopupMenu", "Java file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testTxtFilePopupMenu", "Txt file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testPropertiesFilePopupMenu", "Properties file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testXmlFilePopupMenu", "Xml file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testJspFilePopupMenu", "Jsp file node popup in Projects View"));
        
        suite.addTest(new FilesViewPopupMenu("testProjectNodePopupMenu", "Project node popup in Files View"));
        suite.addTest(new FilesViewPopupMenu("testPackagePopupMenu", "Package node popup in Files View"));
        suite.addTest(new FilesViewPopupMenu("testbuildXmlFilePopupMenu", "build.xml file node popup in Files View"));
        
        suite.addTest(new ToolsMenu("testJavaToolsMenu", "Tools main menu for Java node"));
        suite.addTest(new ToolsMenu("testXmlToolsMenu", "Tools main menu for XML node"));
        suite.addTest(new ToolsMenu("testTxtToolsMenu", "Tools main menu for text node"));

        suite.addTest(new EditorDownButtonPopupMenu("measureTime", "Editor Down Button popup"));
        
        suite.addTest(new SourceEditorPopupMenu("testPopupInTxt", "Plain text Editor popup"));
        suite.addTest(new SourceEditorPopupMenu("testPopupInXml", "XML Editor popup"));
        suite.addTest(new SourceEditorPopupMenu("testPopupOnMethod", "Java Editor Method popup"));
        suite.addTest(new SourceEditorPopupMenu("testPopupOnClassName", "Java Editor Class Name popup"));
        
        suite.addTest(new RuntimeViewPopupMenu("testServerRegistryPopupMenu", "Server Registry node popup in Projects View"));
        suite.addTest(new RuntimeViewPopupMenu("testTomcatPopupMenu", "Tomcat node popup in Runtime View"));
        suite.addTest(new RuntimeViewPopupMenu("testHttpTomcatPopupMenu", "http localhost node popup in Runtime View"));
        
//TODO        suite.addTest(new FormInspectorNodePopupMenu("measureTime", "Form Inspector node popup"));
        
        suite.addTest(new ToolsOptionsNodePopupMenu("measureTime", "Tools-Options node popup"));

        
//TODO        suite.addTest(new WebNodePopupMenu("measureTime", "Web Filesystem node popup"));  
//TODO        suite.addTest(new WebInfNodePopupMenu("measureTime", "WEB-INF node popup"));  
//TODO        suite.addTest(new WebXMLNodePopupMenu("measureTime", "web.xml node popup"));
        
//TODO        suite.addTest(new VCSRootNodePopupMenu("measureTime", "CVS Filesystem node popup"));
//TODO        suite.addTest(new VCSJavaNodePopupMenu("measureTime", "CVS Java node popup"));
        // it is no more in the production build suite.addTest(new ExplorerPopupSubMenus("testFilesystemsNodeSubMenuVCS", "Mount | Version Control submenu on Filesystems node"));
//TODO        suite.addTest(new ExplorerPopupSubMenus("testCVSFilesystemsNodeCVSSubMenu", "CVS submenu on CVS Filesystems node"));
        
        
        
        return suite;
    }
    
}
