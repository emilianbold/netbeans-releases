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
public class MeasureMemoryUsage_dialog_1 {
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new EmptyTestCase("measureMemoryUsage", "Empty test case"));
        
        // dialogs and windows which don't require any preparation
        suite.addTest(new About("doMeasurement", "About dialog open"));
        suite.addTest(new About_2("doMeasurement", "About details open"));
        
        suite.addTest(new KeyboardShortcuts("doMeasurement", "Keyboard Shortcut dialog open"));
        suite.addTest(new KeyboardShortcuts_2("doMeasurement", "Keyboard Shortcut shortcuts open"));
 
        suite.addTest(new Options("doMeasurement", "Options dialog open"));
 
        suite.addTest(new UpdateCenter("doMeasurement", "Update Center wizard open"));
        suite.addTest(new ProxyConfiguration("doMeasurement", "Proxy Configuration open"));
        suite.addTest(new VersioningManager("doMeasurement", "Versioning Manager open"));
                
        return suite;
    }
    
}
