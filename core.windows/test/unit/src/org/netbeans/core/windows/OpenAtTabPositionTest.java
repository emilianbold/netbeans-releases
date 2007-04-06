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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows;

import junit.framework.*;
import org.netbeans.junit.*;

import org.openide.windows.*;


/** 
 * Tests correct behaviour of TopComponent.openAtTabPosition and TopComponent.getTabPosition.
 * 
 * @author Dafe Simonek
 */
public class OpenAtTabPositionTest extends NbTestCase {

    public OpenAtTabPositionTest (String name) {
        super (name);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(OpenAtTabPositionTest.class);
        
        return suite;
    }

    protected boolean runInEQ () {
        return true;
    }
     
    public void testIsOpenedAtRightPosition () throws Exception {
        Mode mode = WindowManagerImpl.getInstance().createMode("testIsOpenedAtRightPositionMode",
                Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        
        TopComponent firstTC = new TopComponent();
        mode.dockInto(firstTC);
        TopComponent tc1 = new TopComponent();
        mode.dockInto(tc1);
        TopComponent tc2 = new TopComponent();
        mode.dockInto(tc2);
        TopComponent tc3 = new TopComponent();
        mode.dockInto(tc3);
        
        System.out.println("Checking getTabPosition on closed TopComponent...");
        assertTrue("Expected TC position -1, but got " + tc1.getTabPosition(), tc1.getTabPosition() == -1);
                
        System.out.println("Checking open both on impossible and possible positions...");
        
        firstTC.open();
        
        tc1.openAtTabPosition(2);
        assertTrue(tc1.isOpened());
        assertTrue("Expected TC position 1, but got " + tc1.getTabPosition(), tc1.getTabPosition() == 1);
        
        tc2.openAtTabPosition(-2);
        assertTrue(tc2.isOpened());
        assertTrue("Expected TC position 0, but got " + tc2.getTabPosition(), tc2.getTabPosition() == 0);
        
        tc3.openAtTabPosition(1);
        assertTrue(tc3.isOpened());
        assertTrue("Expected TC position 1, but got " + tc3.getTabPosition(), tc3.getTabPosition() == 1);
        assertTrue("Expected TC position 3, but got " + tc1.getTabPosition(), tc1.getTabPosition() == 3);
        assertTrue("Expected TC position 0, but got " + tc2.getTabPosition(), tc2.getTabPosition() == 0);
        
        tc3.close();
        assertTrue("Expected TC position -1, but got " + tc3.getTabPosition(), tc3.getTabPosition() == -1);
        
    }
    
}
