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
