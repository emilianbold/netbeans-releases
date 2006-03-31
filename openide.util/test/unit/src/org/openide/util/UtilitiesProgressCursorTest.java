/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util;

import java.awt.Cursor;
import javax.swing.JComponent;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Dafe Simonek
 */
public class UtilitiesProgressCursorTest extends NbTestCase {
    
    /** Creates a new instance of UtilProgressCursorTest */
    public UtilitiesProgressCursorTest(String testName) {
        super(testName);
    }

    public void testProgressCursor () {
        JComponent testTc = new ProgressCursorComp();
        Cursor progressCursor = Utilities.createProgressCursor(testTc);
        testTc.setCursor(progressCursor);
        //testTc.open();
        Cursor compCursor = testTc.getCursor();
        if (!progressCursor.equals(compCursor)) {
            fail("Setting of progress cursor don't work: \n" +
                 "Comp cursor: " + compCursor + "\n" +
                 "Progress cursor: " + progressCursor);
        }
    }
    
    /** testing component for setting cursor
     */
    private static class ProgressCursorComp extends JComponent {
        
        public String getName () {
            return "TestProgressCursorComp";
        }
        
    }

}
