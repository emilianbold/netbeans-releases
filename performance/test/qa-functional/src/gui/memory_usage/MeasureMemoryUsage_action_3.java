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
public class MeasureMemoryUsage_action_3 {
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new EmptyTestCase("measureMemoryUsage", "Empty test case"));
        
        suite.addTest(new PasteInEditor("doMeasurement", "Paste in the editor"));
        suite.addTest(new PageUpPageDownInEditor("doMeasurement", "Press Page Up in the editor", true));
        suite.addTest(new PageUpPageDownInEditor("doMeasurement", "Press Page Down in the editor", false));
        
        suite.addTest(new JavaCompletionInEditor("doMeasurement", "Invoke Code Completion dialog in Editor"));
        suite.addTest(new TypingInEditor("doMeasurement", "Type a character in Editor"));


        return suite;
    }
    
}
