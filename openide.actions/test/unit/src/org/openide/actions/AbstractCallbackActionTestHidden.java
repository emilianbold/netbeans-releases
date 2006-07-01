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

package org.openide.actions;


import java.util.Arrays;
import javax.swing.Action;
import javax.swing.ActionMap;

import junit.textui.TestRunner;

import org.netbeans.junit.*;
import org.openide.actions.*;
import org.openide.util.Lookup;
import org.openide.util.actions.CallbackSystemAction;


/** Test behaviour of regular callback actions.
 */
public abstract class AbstractCallbackActionTestHidden extends NbTestCase {
    public AbstractCallbackActionTestHidden(String name) {
        super(name);
    }




    /** global action */
    protected org.openide.util.actions.CallbackSystemAction global;
    
    /** our action that is being added into the map */
    protected OurAction action = new OurAction ();
    
    /** map that we lookup action in */
    protected ActionMap map;
    /** the clonned action */
    protected Action clone;
    
    /** listener that is attached to the clone action and allows counting of prop events.*/
    protected CntListener listener;
    
    /** that is the action being clonned to */
    private Lookup lookup;
    
    /** Which action to test should be subclass of CallbackSystemAction.
     */
    protected abstract Class actionClass ();
    
    /** The key that is used in the action map
     */
    protected abstract String actionKey ();

    
    
    protected boolean runInEQ () {
        return true;
    }
    
    protected void setUp() throws Exception {
        global = (CallbackSystemAction)CallbackSystemAction.get (actionClass ());
        map = new ActionMap ();
        map.put (actionKey (), action);
        lookup = org.openide.util.lookup.Lookups.singleton(map);
        // Retrieve context sensitive action instance if possible.
        clone = global.createContextAwareInstance(lookup);
        
        listener = new CntListener ();
        clone.addPropertyChangeListener(listener);
    }
    
    public void testThatDefaultEditorKitPasteActionIsTheCorrectKeyOfPasteAction () {
        clone.actionPerformed (new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        action.assertCnt ("Clone correctly delegates to OurAction", 1);
    }
    
    public void testChangesAreCorrectlyPropagatedToTheDelegate () {
        action.setEnabled (true);
        
        assertTrue ("Clone is correctly enabled", clone.isEnabled ());
        
        action.setEnabled (false);
        assertTrue ("Clone is disabled", !clone.isEnabled());
        listener.assertCnt ("Change notified", 1);
        
        action.setEnabled (true);
        listener.assertCnt ("Change notified again", 1);
        assertTrue ("Clone is correctly enabled", clone.isEnabled ());
    }
    
    protected static final class OurAction extends javax.swing.AbstractAction {
        private int cnt;
        private java.util.HashSet listeners = new java.util.HashSet ();
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
            cnt++;
        }
        
        public void assertCnt (String msg, int count) {
            assertEquals (msg, count, this.cnt);
            this.cnt = 0;
        }
        
        public void assertListeners (String msg, int count) throws Exception {
            if (count == 0) {
                synchronized (this) {
                    int c = 5;
                    while (this.listeners.size () != 0 && c-- > 0) {
                        System.gc ();
                        wait (500);
                    }
                }
            }
            
            if (count != this.listeners.size ()) {
                fail (msg + " listeners expected: " + count + " but are " + this.listeners);
            }
        }
        
        public void addPropertyChangeListener (java.beans.PropertyChangeListener listener) {
            super.addPropertyChangeListener (listener);
            listeners.add (listener);
        }        
        
        public synchronized void removePropertyChangeListener (java.beans.PropertyChangeListener listener) {
            super.removePropertyChangeListener (listener);
            listeners.remove (listener);
            notifyAll ();
        }
    } // end of OurAction
    
    protected static final class CntListener extends Object
    implements java.beans.PropertyChangeListener {
        private int cnt;
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            cnt++;
        }
        
        public void assertCnt (String msg, int count) {
            assertEquals (msg, count, this.cnt);
            this.cnt = 0;
        }
    } // end of CntListener
}
