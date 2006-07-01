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


/** Test to guarantee that the compatibility for docking operations is
 * preserved for components written against release 3.5 and later and
 * that such components can be docked.
 *
 * @author Jaroslav Tulach
 */
public class DockingCompatibilityTest extends NbTestCase {

    /** Creates a new instance of SFSTest */
    public DockingCompatibilityTest (String name) {
        super (name);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(DockingCompatibilityTest.class);
        
        return suite;
    }

    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testSimplyOpenedComponentCanBeDockedWhereeverItWants () throws Exception {
        TopComponent tc = new TopComponent ();
        tc.open ();
        
        assertCanBeDocked (tc, Boolean.TRUE);
    }
    
    public void testComponentPutIntoOwnModeCanBeDockedAsWell () {
        Mode mode = WindowManager.getDefault ().getCurrentWorkspace ().createMode ("OwnMode", "displayName", null);
        TopComponent tc = new TopComponent ();
        mode.dockInto (tc);
        tc.open ();
        
        assertCanBeDocked (tc, Boolean.TRUE);
    }

    public void testComponentPlacedDirectlyIntoEditorModeHasToStayThere () {
        Mode mode = WindowManager.getDefault ().findMode ("editor");
        assertNotNull ("Shall not be null", mode);
        TopComponent tc = new TopComponent ();
        mode.dockInto (tc);
        assertCanBeDocked (tc, null);
    }
    
    
    private static void assertCanBeDocked (TopComponent tc, Boolean expectedValue) {
        assertEquals (
            expectedValue,  
            tc.getClientProperty (Constants.TOPCOMPONENT_ALLOW_DOCK_ANYWHERE)
        );
    }
}

