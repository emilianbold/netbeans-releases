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
