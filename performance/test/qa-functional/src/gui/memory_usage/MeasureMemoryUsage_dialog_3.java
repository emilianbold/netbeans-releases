/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package gui.memory_usage;

import org.netbeans.junit.NbTestSuite;
import gui.menu.*;
import gui.window.*;
import gui.action.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureMemoryUsage_dialog_3 {
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new EmptyTestCase("measureMemoryUsage", "Empty test case"));
        
//TODO       suite.addTest(new OutputWindow("measureTime", "Output window open"));
        suite.addTest(new ToDoWindow("measureTime", "To Do window open"));
        suite.addTest(new HttpMonitorWindow("measureTime", "Http Monitor window open"));

        suite.addTest(new HelpContentsWindow("measureTime", "Help Contents window open"));
        
        suite.addTest(new PropertyEditorString("measureTime", "String Property Editor open"));
//TODO fails often        suite.addTest(new PropertyEditorColor("measureTime", "Color Property Editor open"));
        
        suite.addTest(new AddServerInstanceDialog("measureTime", "Add JDBC Driver dialog open"));
        suite.addTest(new NewDatabaseConnectionDialog("measureTime", "New Database Connection dialog open"));
        suite.addTest(new AddXMLandDTDSchemaCatalog("measureTime", "Add Catalog dialog open"));
                
        return suite;
    }
    
}
