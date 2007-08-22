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

package gui;

import gui.menu.*;

import org.netbeans.junit.NbTestSuite;

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
        suite.addTest(new MainMenu("testNavigateMenu", "Navigate main menu"));
        suite.addTest(new MainMenu("testSourceMenu", "Source main menu"));
        suite.addTest(new MainMenu("testBuildMenu", "Build main menu"));
        suite.addTest(new MainMenu("testRunMenu", "Run main menu"));
        suite.addTest(new MainMenu("testRefactoringMenu", "Refactor main menu"));
        suite.addTest(new MainMenu("testVersioningMenu", "Versioning main menu"));
        suite.addTest(new MainMenu("testProfileMenu", "Profile main menu"));
        suite.addTest(new MainMenu("testWindowMenu", "Window main menu"));
        suite.addTest(new MainMenu("testHelpMenu", "Help main menu"));
        
        // recent prj menu is empty and disabled
        // suite.addTest(new MainSubMenus("testFileOpenRecentProjectMenu", "File | Open Recent Project main menu"));
        suite.addTest(new MainSubMenus("testFileSetMainProjectMenu", "File | Set Main Project main menu"));
        // suite.addTest(new MainSubMenus("testViewDocumentationIndicesMenu", "View | Documentation Indices main menu"));
        suite.addTest(new MainSubMenus("testViewCodeFoldsMenu", "View | Code Folds main menu"));
        suite.addTest(new MainSubMenus("testViewEditorsMenu", "View | Editors main menu"));
        suite.addTest(new MainSubMenus("testViewToolbarsMenu", "View | Toolbars main menu"));
        suite.addTest(new MainSubMenus("testRunStackMenu", "Run | Stack main menu"));
        suite.addTest(new MainSubMenus("testRunRunFileMenu", "Run | Run File main menu"));
        suite.addTest(new MainSubMenus("testVersLocalHistoryMenu", "Versioning | Local History main menu"));
        suite.addTest(new MainSubMenus("testVersSubversionMenu", "Versioning | Subversion main menu"));
        suite.addTest(new MainSubMenus("testToolsI18nMenu", "Tools | Internationalization main menu"));
        suite.addTest(new MainSubMenus("testToolsPaletteMenu", "Tools | Palette main menu"));
        suite.addTest(new MainSubMenus("testWinDebuggingMenu", "Window | Debugging main menu"));
        suite.addTest(new MainSubMenus("testWinVersioningMenu", "Window | Versioning main menu"));
        suite.addTest(new MainSubMenus("testWinProfilingMenu", "Window | Profiling main menu"));
//        suite.addTest(new MainSubMenus("testHelpTutorials", "Help | Tutorials main menu"));
        suite.addTest(new MainSubMenus("testHelpJavadoc", "Help | Javadoc References main menu"));
        
        suite.addTest(new ProjectsViewPopupMenu("testProjectNodePopupMenuProjects", "JSE Project node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testSourcePackagesPopupMenuProjects", "Source Packages node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testTestPackagesPopupMenuProjects", "Test Packages node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testPackagePopupMenuProjects", "Package node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testJavaFilePopupMenuProjects", "Java file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testTxtFilePopupMenuProjects", "Txt file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testPropertiesFilePopupMenuProjects", "Properties file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testXmlFilePopupMenuProjects", "Xml file node popup in Projects View"));
//TODO no tomcat - see issue 101104         suite.addTest(new ProjectsViewPopupMenu("testJspFilePopupMenuProjects", "Jsp file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testNBProjectNodePopupMenuProjects", "NB Project node popup in Projects View"));

        suite.addTest(new ProjectsViewSubMenus("testProjectNodeCVSsubmenu", "CVS Submenu over projects node in Projects View"));
        suite.addTest(new ProjectsViewSubMenus("testProjectNodeNewSubmenu", "New Submenu over projects node in Projects View"));
        
        suite.addTest(new FilesViewPopupMenu("testProjectNodePopupMenuFiles", "Project node popup in Files View"));
        suite.addTest(new FilesViewPopupMenu("testPackagePopupMenuFiles", "Package node popup in Files View"));
        suite.addTest(new FilesViewPopupMenu("testbuildXmlFilePopupMenuFiles", "build.xml file node popup in Files View"));
        
        suite.addTest(new ToolsMenu("testJavaToolsMenu", "Tools main menu for Java node"));
        suite.addTest(new ToolsMenu("testXmlToolsMenu", "Tools main menu for XML node"));
        suite.addTest(new ToolsMenu("testTxtToolsMenu", "Tools main menu for Txt node"));

        suite.addTest(new SourceEditorPopupMenu("testPopupInTxt", "Plain text Editor popup"));
        suite.addTest(new SourceEditorPopupMenu("testPopupInXml", "XML Editor popup"));
        suite.addTest(new SourceEditorPopupMenu("testPopupOnMethod", "Java Editor Method popup"));
        suite.addTest(new SourceEditorPopupMenu("testPopupOnClassName", "Java Editor Class Name popup"));
        
        suite.addTest(new RuntimeViewPopupMenu("testServerRegistryPopupMenuRuntime", "Servers node popup in Services View"));
//TODO no tomcat - see issue 101104        suite.addTest(new RuntimeViewPopupMenu("testTomcatPopupMenuRuntime", "Bundled Tomcat node popup in Runtime View"));
        
        suite.addTest(new FormInspectorNodePopupMenu("testFormNodePopupMenuInspector", "Form Inspector node popup"));
        
/*  TEMPORARY commented -> try to solve never finished tests
        suite.addTest(new EditorDownButtonPopupMenu("testEditorDownButtonPopupMenu", "Editor Down Button popup")); 
 */
        return suite;
    }
    
}
