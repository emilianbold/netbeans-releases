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
public class MeasureMemoryUsage_dialog_2 {
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new EmptyTestCase("measureMemoryUsage", "Empty test case"));
        
        suite.addTest(new FilesWindow("doMeasurement", "Files window open"));
        suite.addTest(new ProjectsWindow("doMeasurement", "Projects window open"));
        suite.addTest(new RuntimeWindow("doMeasurement", "Runtime window open"));
        suite.addTest(new VersioningWindow("doMeasurement", "Versioning window open"));
        suite.addTest(new FavoritesWindow("doMeasurement", "Favorites window open"));
        
//TODO it still fails in Promo D       suite.addTest(new OutputWindow("doMeasurement", "Output window open"));
        suite.addTest(new ToDoWindow("doMeasurement", "To Do window open"));
        suite.addTest(new HttpMonitorWindow("doMeasurement", "Http Monitor window open"));

        suite.addTest(new DeleteFileDialog("doMeasurement", "Delete Object dialog open"));
        
        return suite;
    }
    
}
