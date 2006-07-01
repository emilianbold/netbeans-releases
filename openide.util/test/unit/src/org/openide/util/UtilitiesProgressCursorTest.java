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
