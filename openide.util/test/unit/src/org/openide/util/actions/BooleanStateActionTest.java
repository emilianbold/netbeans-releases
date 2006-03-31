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

package org.openide.util.actions;

import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;

/** Test that boolean actions are in fact toggled.
 * @author Jesse Glick
 */
public class BooleanStateActionTest extends NbTestCase {
    
    public BooleanStateActionTest(String name) {
        super(name);
    }
    
    /** Self-explanatory, hopefully. */
    public void testToggle() throws Exception {
        BooleanStateAction a1 = (BooleanStateAction)SystemAction.get(SimpleBooleanAction1.class);
        assertTrue(a1.isEnabled());
        BooleanStateAction a2 = (BooleanStateAction)SystemAction.get(SimpleBooleanAction2.class);
        assertTrue(a1.getBooleanState());
        assertFalse(a2.getBooleanState());
        ActionsInfraHid.WaitPCL l = new ActionsInfraHid.WaitPCL(BooleanStateAction.PROP_BOOLEAN_STATE);
        a1.addPropertyChangeListener(l);
        a1.actionPerformed(null);
        assertTrue(l.changed());
        assertFalse(a1.getBooleanState());
        a1.removePropertyChangeListener(l);
        l.gotit = 0;
        a2.addPropertyChangeListener(l);
        a2.actionPerformed(null);
        assertTrue(l.changed());
        assertTrue(a2.getBooleanState());
        a2.removePropertyChangeListener(l);
    }
    
    public static final class SimpleBooleanAction1 extends BooleanStateAction {
        public String getName() {
            return "SimpleBooleanAction1";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }
    }
    
    public static final class SimpleBooleanAction2 extends BooleanStateAction {
        protected void initialize() {
            super.initialize();
            setBooleanState(false);
        }
        public String getName() {
            return "SimpleBooleanAction2";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }
    }
    
}
