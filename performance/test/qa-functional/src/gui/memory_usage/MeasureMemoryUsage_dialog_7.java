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
public class MeasureMemoryUsage_dialog_7 {
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new EmptyTestCase("measureMemoryUsage", "Empty test case"));
        
//TODO        suite.addTest(new DocumentsDialog("measureTime", "Documents dialog open"));
        
        suite.addTest(new AddServerInstanceDialog("measureTime", "Add Server Instance dialog open"));
        
        suite.addTest(new CreateTestsDialog("measureTime", "Create Tests dialog open"));
        
        suite.addTest(new RefactorFindUsagesDialog("measureTime", "Refactor find usages dialog open"));
        suite.addTest(new RefactorRenameDialog("measureTime", "Refactor rename dialog open"));
//TODO hard to indentify end of the action        suite.addTest(new RefactorMoveClassDialog("measureTime", "Refactor move class dialog open"));
        
        return suite;
    }
    
}
