/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda.testapps;

/**
 * Sample class breakpoints application. DO NOT MODIFY - line numbers must not change in this source file.
 *
 * @author Maros Sandor
 */
public class ThreadBreakpointApp {

    public static void main(String[] args) {
        ThreadBreakpointApp sa = new ThreadBreakpointApp();
        sa.threads();
    }

    private void threads() {
        ThreadGroup tgrp = new ThreadGroup("testgroup");
        new SampleThread(tgrp, "test-1").start();
        new SampleThread(tgrp, "test-2").start();
        new SampleThread(tgrp, "test-3").start();
        new SampleThread(tgrp, "test-4").start();
        new SampleThread(tgrp, "test-5").start();
    }


    private class SampleThread extends Thread {

        public SampleThread(ThreadGroup group, String name) {
            super(group, name);
        }

        public void run() {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
    }
}
