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
