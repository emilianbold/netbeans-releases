/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import org.netbeans.junit.*;
import junit.textui.TestRunner;

/** Tests behaviour of asynchronous actions and exit dialog.
 */
public class ModuleActionsTest extends NbTestCase {
    
    public ModuleActionsTest (String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(ModuleActionsTest.class));
    }
    
    protected boolean runInEQ () {
        return true;
    }    
    
    public void testActionIsListedAsRunning () throws Exception {
        Act act = Act.get(Act.class);
        
        synchronized (act) {
            act.actionPerformed (new java.awt.event.ActionEvent (this, 0, ""));
            act.wait ();
        }
        
        java.util.Collection col = ModuleActions.getDefaultInstance ().getRunningActions ();
        java.util.Iterator it = col.iterator ();
        while (it.hasNext ()) {
            javax.swing.Action a = (javax.swing.Action)it.next ();
            if (a.getValue (javax.swing.Action.NAME) == act.getName ()) {
                return;
            }
        }
        fail ("Act should be running: " + col);
    }

    public static class Act extends org.openide.util.actions.CallableSystemAction {
        
        public org.openide.util.HelpCtx getHelpCtx () {
            return org.openide.util.HelpCtx.DEFAULT_HELP;
        }
        
        public String getName () {
            return getClass().getName ();
        }
        
        public synchronized void performAction () {
            notifyAll ();
            try {
                wait ();
            } catch (InterruptedException ex) {
                fail ("Shall not be interupted");
            }
        }
        
        protected boolean asynchronous () {
            return true;
        }
        
    }
    
}
