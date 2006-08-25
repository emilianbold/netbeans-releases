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

package org.openide.windows;

import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import javax.swing.SwingUtilities;
import junit.framework.TestCase;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/** A piece of the test compatibility suite for the execution APIs.
 *
 * @author Jaroslav Tulach
 */
public class WindowManagerHid extends NbTestCase {
    
    public WindowManagerHid(String testName) {
        super(testName);
    }
    
    public void testGetDefault() {
        WindowManager result = WindowManager.getDefault();
        assertNotNull(result);
    }
    
    public void testInvokeWhenUIReady() throws Exception {
        class R implements Runnable {
            public boolean started;
            public boolean finished;
            public boolean block;
            
            public synchronized void run() {
                assertTrue("Runs only in AWT thread", SwingUtilities.isEventDispatchThread());
                try {
                    started = true;
                    notifyAll();
                    if (block) {
                        wait();
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                finished = true;
                notifyAll();
            }
        }
        
        R run = new R();
        R snd = new R();
        
        WindowManager wm = WindowManager.getDefault();
        synchronized (run) {
            wm.invokeWhenUIReady(run);
            run.block = true;
            run.wait();
        }
        assertTrue("started", run.started);
        assertFalse("but not finished", run.finished);
        
        wm.invokeWhenUIReady(snd);
        
        Thread.sleep(100);
        
        assertFalse("Not started", snd.started);
        synchronized (snd) {
            synchronized (run) {
                run.notifyAll();
                run.wait();
            }
            assertTrue("run is finished", run.finished);
            snd.wait();
            assertTrue("snd also finished", snd.finished);
        }
    }
}
