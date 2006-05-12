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

package org.openide.actions;

import java.awt.event.ActionEvent;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.LifecycleManager;

/**
 *
 * @author Jaroslav Tulach
 */
public class SaveAllActionTest extends NbTestCase {
    
    public SaveAllActionTest (String testName) {
        super (testName);
    }
    
    protected boolean runInEQ () {
        return true;
    }
    
    
    protected void setUp () {
        MockServices.setServices(new Class[] {Life.class});
        Life.max = 0;
        Life.cnt = 0;
        Life.executed = 0;
    }

    public void testThatTheActionIsInvokeOutsideOfAWTThreadAndOnlyOnceAtATime () throws Exception {
        SaveAllAction a = (SaveAllAction)SaveAllAction.get (SaveAllAction.class);
        a.setEnabled (true);
        assertTrue ("Is enabled", a.isEnabled ());
        
        ActionEvent ev = new ActionEvent (this, 0, "");
        a.actionPerformed (ev);
        a.actionPerformed (ev);
        a.actionPerformed (ev);
        
        Object life = org.openide.util.Lookup.getDefault ().lookup (LifecycleManager.class);
        synchronized (life) {
            while (Life.executed != 3) {
                life.wait ();
            }
        }
        
        assertEquals ("Maximum is one invocation of saveAll at one time", 1, Life.max);
    }

    public static final class Life extends LifecycleManager {
        static int max;
        static int cnt;
        static int executed;
        
        public synchronized void saveAll () {
            cnt++;
            if (cnt > max) {
                max = cnt;
            }
            try {
                wait (500);
            } catch (Exception ex) {
                
            }
            executed++;
            notifyAll ();
            
            cnt--;
            assertFalse ("No AWT thread: ", javax.swing.SwingUtilities.isEventDispatchThread ());
        }

        public void exit () {
            fail ("Not supported");
        }
        
    }
}
