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
import gui.menu.*;
import gui.window.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureJava  {
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

//        suite.addTest(new ExpandNodesProjectsView("testExpandProjectNode", "Expand Project node"));
//        suite.addTest(new ExpandNodesProjectsView("testExpandSourcePackagesNode", "Expand Source Packages node"));
//        suite.addTest(new ExpandNodesProjectsView("testExpandFolderWith50JavaFiles", "Expand folder with 50 java files"));
//        suite.addTest(new ExpandNodesProjectsView("testExpandFolderWith100JavaFiles", "Expand folder with 100 java files"));
//
//        suite.addTest(new OpenFiles("testOpening20kBJavaFile", "Open Java file (20kB)"));
//        suite.addTest(new OpenFilesWithOpenedEditor("testOpening20kBJavaFile", "Open Java file (20kB) if Editor opened"));
//
//        suite.addTest(new OpenJspFile("testOpening20kBJSPFile", "Open JSP file"));
//        suite.addTest(new OpenJspFileWithOpenedEditor("testOpening20kBJSPFile", "Open JSP file if Editor opened"));
//        
//        suite.addTest(new PasteInEditor("measureTime", "Paste in the editor"));
//        suite.addTest(new PageUpPageDownInEditor("measureTime", "Press Page Up in the editor", true));
//        suite.addTest(new PageUpPageDownInEditor("measureTime", "Press Page Down in the editor", false));
//        
//        suite.addTest(new JavaCompletionInEditor("measureTime", "Invoke Code Completion dialog in Editor"));
//        suite.addTest(new TypingInEditor("measureTime", "Type a character in Editor"));
//
//
//        suite.addTest(new CloseEditor("testClosing20kBJavaFile", "Close Java file (20kB)"));
//        
//        suite.addTest(new CloseEditorModified("testClosingModifiedJavaFile", "Close modified Java file"));
//        
//        suite.addTest(new SaveModifiedFile("testSaveModifiedJavaFile", "Save modified Java file"));
//
//        suite.addTest(new RefreshFolder("testRefreshFolderWith50JavaFiles", "Refresh folder with 50 java files"));
//        suite.addTest(new RefreshFolder("testRefreshFolderWith100JavaFiles", "Refresh folder with 100 java files"));
//        
//        suite.addTest(new DeleteFolder("testDeleteFolderWith50JavaFiles", "Delete folder with 50 java files"));
//        suite.addTest(new DeleteFolder("testDeleteFolderWith100JavaFiles", "Delete folder with 100 java files"));
//        
//        suite.addTest(new CreateProject("testCreateJavaApplicationProject", "Create Java Application project"));
//        suite.addTest(new CreateProject("testCreateJavaLibraryProject", "Create Java Library project"));
//        suite.addTest(new CreateProject("testCreateWebApplicationProject", "Create Web Application project"));
//        
//        suite.addTest(new ProjectsViewPopupMenu("testPackagePopupMenuProjects", "Package node popup in Projects View"));
//        suite.addTest(new ProjectsViewPopupMenu("testJavaFilePopupMenuProjects", "Java file node popup in Projects View"));
//        suite.addTest(new ProjectsViewPopupMenu("testJspFilePopupMenuProjects", "Jsp file node popup in Projects View"));
//        
//        suite.addTest(new FilesViewPopupMenu("testProjectNodePopupMenuFiles", "Project node popup in Files View"));
//        suite.addTest(new FilesViewPopupMenu("testPackagePopupMenuFiles", "Package node popup in Files View"));
//        
//        suite.addTest(new ToolsMenu("testJavaToolsMenu", "Tools main menu for Java node"));
//
//        suite.addTest(new SourceEditorPopupMenu("testPopupOnMethod", "Java Editor Method popup"));
//        suite.addTest(new SourceEditorPopupMenu("testPopupOnClassName", "Java Editor Class Name popup"));
//
//        suite.addTest(new ToDoWindow("measureTime", "To Do window open"));
//
//        suite.addTest(new OverrideMethods("measureTime", "Override and Implement Methods dialog open"));
//        
//        suite.addTest(new AutoCommentWindow("measureTime", "Auto Comment Tool open"));
//
//        suite.addTest(new RefactorFindUsagesDialog("measureTime", "Refactor find usages dialog open"));
//        suite.addTest(new RefactorRenameDialog("measureTime", "Refactor rename dialog open"));
//        suite.addTest(new RefactorMoveClassDialog("measureTime", "Refactor move class dialog open"));
        
        suite.addTest(new RefactorFindUsages("measureTime", "Refactor find usages"));
        
        return suite;
    }
    
}
