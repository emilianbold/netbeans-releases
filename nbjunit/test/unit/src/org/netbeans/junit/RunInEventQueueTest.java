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
        assertTrue("setUp should be run in AWT thread.", SwingUtilities.isEventDispatchThread());
    }

    protected void tearDown () throws Exception {
        assertTrue("tearDown should be run in AWT thread.", SwingUtilities.isEventDispatchThread());
    }
    
    protected boolean runInEQ () {
        return true;
    }

    public void testRunsInAWTThread () {
        assertTrue ("We are in Event Thread", SwingUtilities.isEventDispatchThread ());
    }
    

    
}
