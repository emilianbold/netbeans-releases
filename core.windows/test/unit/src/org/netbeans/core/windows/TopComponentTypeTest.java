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
 * Ensure that TopComponent type - "editor" / "view" - is interpreted correctly.
 * 
 * @author S. Aubrecht
 */
public class TopComponentTypeTest extends NbTestCase {

    public TopComponentTypeTest (String name) {
        super (name);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TopComponentTypeTest.class);
        
        return suite;
    }

    protected boolean runInEQ () {
        return true;
    }
     
    public void testIsEditorTopComponent () throws Exception {
        TopComponent tc = new TopComponent ();
        Mode mode = WindowManagerImpl.getInstance().createMode( "editorMode", Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        mode.dockInto( tc );
        
        assertTrue( WindowManagerImpl.getInstance().isEditorTopComponent( tc ) );
        assertTrue( WindowManagerImpl.getInstance().isEditorMode( mode ) );
    }
    
    public void testIsViewTopComponent () throws Exception {
        TopComponent tc = new TopComponent ();
        Mode mode = WindowManagerImpl.getInstance().createMode( "viewMode", Constants.MODE_KIND_VIEW, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        mode.dockInto( tc );
        
        assertFalse( WindowManagerImpl.getInstance().isEditorTopComponent( tc ) );
        assertFalse( WindowManagerImpl.getInstance().isEditorMode( mode ) );
    }
    
    public void testUnknownTopComponent () throws Exception {
        TopComponent tc = new TopComponent ();
        //no mode defined for the topcomponent
        
        assertFalse( WindowManagerImpl.getInstance().isEditorTopComponent( tc ) );
    }
}

