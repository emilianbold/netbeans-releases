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
import gui.window.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureDialogs  {
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        // dialogs and windows which don't require any preparation
        suite.addTest(new About("measureTime", "About dialog open"));
        suite.addTest(new About_2("measureTime", "About details open"));
        suite.addTest(new SetupWizard("measureTime", "Setup Wizard open"));
        suite.addTest(new SetupWizard_2("measureTime", "Setup Wizard next open"));
        suite.addTest(new SetupWizard_3("measureTime", "Setup Wizard next next open"));
        suite.addTest(new KeyboardShortcuts("measureTime", "Keyboard Shortcut dialog open"));
        suite.addTest(new KeyboardShortcuts_2("measureTime", "Keyboard Shortcut shortcuts open"));
        suite.addTest(new Options("measureTime", "Options dialog open"));
        suite.addTest(new NewProjectDialog("measureTime", "New Project dialog open"));
        suite.addTest(new NewFileDialog("measureTime", "New File dialog open"));
        suite.addTest(new OpenProjectDialog("measureTime", "Open Project dialog open"));
        suite.addTest(new OpenFileDialog("measureTime", "Open File dialog open"));
        suite.addTest(new UpdateCenter("measureTime", "Update Center wizard open"));
        suite.addTest(new ProxyConfiguration("measureTime", "Proxy Configuration open"));
        
        suite.addTest(new RuntimeWindow("measureTime", "Runtime window open"));
        suite.addTest(new VersioningWindow("measureTime", "Versioning window open"));
//TODO        suite.addTest(new OutputWindow("measureTime", "Output window open"));
        suite.addTest(new ToDoWindow("measureTime", "To Do window open"));
        suite.addTest(new HttpMonitorWindow("measureTime", "Http Monitor window open"));
        suite.addTest(new HelpContentsWindow("measureTime", "Help Contents window open"));
        suite.addTest(new PropertyEditorColor("measureTime", "Color Property Editor open"));
        suite.addTest(new AddJDBCDriverDialog("measureTime", "Add JDBC Driver dialog open"));
 
        
        
        
//TODO        suite.addTest(new JavadocManagerDialog("measureTime", "Javadoc Manager dialog open"));
//TODO        suite.addTest(new AttachDialog("measureTime", "Attach dialog open"));
//TODO        suite.addTest(new ProjectManagerDialog("measureTime", "Project Manager dialog open"));
//TODO        suite.addTest(new ProjectSetMainClassDialog("measureTime", "Set Project Main Class dialog open"));
        //It doesn't work if printer isn't installed suite.addTest(new PageSetupDialog("measureTime", "Page Setup dialog open"));
        
        

        // dialogs and windows which first select a node somewhere
//TODO        suite.addTest(new PropertiesWindow("measureTime", "Properties window open"));
//TODO        suite.addTest(new NewDatabaseConnectionDialog("measureTime", "New Database Connection dialog open"));
//TODO        suite.addTest(new MountXMLCatalogDialog("measureTime", "Mount XML Catalog dialog open"));
//TODO        suite.addTest(new AddNewServerInstanceDialog("measureTime", "Add Server Instance dialog open"));
//TODO        suite.addTest(new SetDefaultServerDialog("measureTime", "Set Default Server dialog open"));
        
//TODO        suite.addTest(new FindDialogExplorer("measureTime", "Explorer Find open"));
//TODO        suite.addTest(new NewFromTemplate("measureTime", "New From Template open"));
//        suite.addTest(new PrintToHTMLDialog("measureTime", "Print To HTML dialog open"));

        // dialogs and windows which first open a file in the editor
//TODO        suite.addTest(new ImportManagementWizard("measureTime", "Import Management Tool open"));
//TODO        suite.addTest(new AutoCommentWindow("measureTime", "Auto Comment Tool open"));
//TODO        suite.addTest(new InternationalizeDialog("measureTime", "Internationalize dialog open"));
//TODO        suite.addTest(new DocumentsDialog("measureTime", "Documents dialog open"));
//TODO        suite.addTest(new FindDialogSourceEditor("measureTime", "Source Editor Find dialog open"));
//TODO        suite.addTest(new GoToLineDialog("measureTime", "Go To dialog open"));
//TODO        suite.addTest(new EditorProperties("measureTime", "Editor Properties open"));
//TODO        suite.addTest(new NewBreakpointDialog("measureTime", "New Breakpoint dialog open"));
//TODOs        suite.addTest(new NewWatchDialog("measureTime", "New Watch dialog open"));

        
        
//        suite.addTest(new SetArgumentsDialog("measureTime", "Set Arguments dialog open"));
        //It doesn't work if printer isn't installed suite.addTest(new PrintDialog("measureTime", "Print dialog open"));
        
        
        return suite;
    }
    
}
