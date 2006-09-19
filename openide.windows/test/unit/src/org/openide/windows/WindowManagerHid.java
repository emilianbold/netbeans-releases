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

package org.openide.windows;

import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;

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
