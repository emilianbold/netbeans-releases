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
