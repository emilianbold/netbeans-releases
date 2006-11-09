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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui;

import gui.window.*;

import org.netbeans.junit.NbTestSuite;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureDialogs  {

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        // dialogs and windows which don't require any preparation
        //remove from test run for NB4.1        suite.addTest(new About("measureTime", "About dialog open"));
        //remove from test run for NB4.1        suite.addTest(new About_2("measureTime", "About details open"));
        
        suite.addTest(new ModuleManager("measureTime", "Module Manager dialog open"));
        suite.addTest(new ServerManager("measureTime", "Server Manager dialog open"));
        suite.addTest(new TemplateManager("measureTime", "Template Manager dialog open"));
        
        suite.addTest(new Options("measureTime", "Options dialog open"));
 
        suite.addTest(new NewProjectDialog("measureTime", "New Project dialog open"));
        suite.addTest(new NewFileDialog("measureTime", "New File dialog open"));
        suite.addTest(new OpenProjectDialog("measureTime", "Open Project dialog open"));
        suite.addTest(new OpenFileDialog("measureTime", "Open File dialog open"));
 
        suite.addTest(new UpdateCenter("measureTime", "Update Center wizard open"));
        suite.addTest(new ProxyConfiguration("measureTime", "Proxy Configuration open"));
        
        suite.addTest(new FavoritesWindow("measureTime", "Favorites window open"));
        //remove from test run for NB4.1        suite.addTest(new FilesWindow("measureTime", "Files window open"));
        //remove from test run for NB4.1        suite.addTest(new ProjectsWindow("measureTime", "Projects window open"));
        //remove from test run for NB4.1        suite.addTest(new RuntimeWindow("measureTime", "Runtime window open"));
        suite.addTest(new VersioningWindow("measureTime", "Versioning window open"));
        
//TODO       suite.addTest(new OutputWindow("measureTime", "Output window open"));
        suite.addTest(new ToDoWindow("measureTime", "To Do window open"));
        suite.addTest(new HttpMonitorWindow("measureTime", "Http Monitor window open"));

        suite.addTest(new HelpContentsWindow("measureTime", "Help Contents window open"));
        
        suite.addTest(new PropertyEditorString("measureTime", "String Property Editor open"));
//TODO fails often        suite.addTest(new PropertyEditorColor("measureTime", "Color Property Editor open"));
        
        suite.addTest(new AddServerInstanceDialog("measureTime", "Add JDBC Driver dialog open"));
        suite.addTest(new NewDatabaseConnectionDialog("measureTime", "New Database Connection dialog open"));
        suite.addTest(new AddXMLandDTDSchemaCatalog("measureTime", "Add Catalog dialog open"));
        
        suite.addTest(new FindInProjects("measureTime", "Find in Projects dialog open"));
        suite.addTest(new ProjectPropertiesWindow("testJSEProject", "JSE Project Properties window open"));
        suite.addTest(new ProjectPropertiesWindow("testNBProject", "NB Project Properties window open"));
        suite.addTest(new ProjectPropertiesWindow("testWebProject", "Web Project Properties window open"));
 
        suite.addTest(new DeleteFileDialog("measureTime", "Delete Object dialog open"));
        
        suite.addTest(new AttachDialog("measureTime", "Attach dialog open"));
        suite.addTest(new NewBreakpointDialog("measureTime", "New Breakpoint dialog open"));
        suite.addTest(new NewWatchDialog("measureTime", "New Watch dialog open"));

//TODO failing after retouche integration        suite.addTest(new JavadocIndexSearch("measureTime", "Javadoc Index Search open"));
        
        suite.addTest(new JavaPlatformManager("measureTime", "Java Platform Manager open"));
        suite.addTest(new LibrariesManager("measureTime", "Libraries Manager open"));
        suite.addTest(new NetBeansPlatformManager("measureTime", "NetBeans Platform Manager open"));
        
        // dialogs and windows which first open a file in the editor
//TODO failing after retouche integration        suite.addTest(new OverrideMethods("measureTime", "Override and Implement Methods dialog open"));
//TODO failing after retouche integration        suite.addTest(new GotoClassDialog("measureTime", "Go To Class dialog open"));
        suite.addTest(new GotoLineDialog("measureTime", "Go to Line dialog open"));
//TODO failing after retouche integration        suite.addTest(new AutoCommentWindow("measureTime", "Auto Comment Tool open"));
        suite.addTest(new FindInSourceEditor("measureTime", "Find in Source Editor dialog open"));
        suite.addTest(new InternationalizeDialog("measureTime", "Internationalize dialog open"));
        
//TODO        suite.addTest(new DocumentsDialog("measureTime", "Documents dialog open"));
        
        suite.addTest(new AddServerInstanceDialog("measureTime", "Add Server Instance dialog open"));
        
//TODO failing after retouche integration        suite.addTest(new CreateTestsDialog("measureTime", "Create Tests dialog open"));
        
//TODO failing after retouche integration        suite.addTest(new RefactorFindUsagesDialog("measureTime", "Refactor find usages dialog open"));
//TODO failing after retouche integration        suite.addTest(new RefactorRenameDialog("measureTime", "Refactor rename dialog open"));
//TODO hard to indentify end of the action        suite.addTest(new RefactorMoveClassDialog("measureTime", "Refactor move class dialog open"));
        
        return suite;
    }
    
}
