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

package org.netbeans.junit;

import javax.swing.SwingUtilities;

/** Checks that NbTestCase really sends tests to AWT thread.
 *
 * @author Jaroslav Tulach
 */
public class RunInEventQueueTest extends NbTestCase {
    
    public RunInEventQueueTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
    }

    protected void tearDown () throws Exception {
    }
    
    protected boolean runInEQ () {
        return true;
    }

    public void testRunsInAWTThread () {
        assertTrue ("We are in Event Thread", SwingUtilities.isEventDispatchThread ());
    }
    

    
}
