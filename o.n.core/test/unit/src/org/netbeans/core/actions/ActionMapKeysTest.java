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

package org.netbeans.core.actions;

import junit.framework.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;

/** Checks that the keys defined in API are really working.
 *
 * @author Jaroslav Tulach
 */
public class ActionMapKeysTest extends TestCase {
    
    public ActionMapKeysTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
    }

    protected void tearDown () throws Exception {
    }

    public void testJumpNextAction () {
        JumpNextAction a = (JumpNextAction)JumpNextAction.get (JumpNextAction.class);
        assertEquals ("jumpNext", a.getActionMapKey ());
    }

    public void testJumpPrevAction () {
        JumpPrevAction a = (JumpPrevAction)JumpPrevAction.get (JumpPrevAction.class);
        assertEquals ("jumpPrev", a.getActionMapKey ());
    }
    
}
