/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.Source;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.SourceFactory;
import com.oracle.truffle.api.source.SourceLineLocation;
import com.oracle.truffle.debug.LineBreakpoint;
import com.oracle.truffle.js.engine.TruffleJSEngine;
import javax.script.ScriptEngine;

/**
 * Truffle accessor for JPDA debugger.
 * 
 * @author Martin
 */
public class JPDATruffleAccessor extends Object {
    
    private static final String ACCESS_THREAD_NAME = "JPDA Truffle Access Loop";   // NOI18N
    private static volatile boolean accessLoopRunning = false;
    //private static volatile AccessLoop accessLoopRunnable;
    private static volatile Thread accessLoopThread;
    private static JPDATruffleDebugManager debugManager;
    /** Explicitly set this field to true to step into script calls. */
    static boolean isSteppingInto = false; // Step into was issued in JPDA debugger
    /** A step command:
     * 0 no step (continue)
     * 1 step into
     * 2 step over
     * 3 step out
     */
    private static int stepCmd = 0;

    public JPDATruffleAccessor() {}
    
    static boolean startAccessLoop() {
        if (!accessLoopRunning) {
            Thread loop;
            AccessLoop accessLoop;
            try {
                accessLoop = new AccessLoop();
                loop = new Thread(accessLoop, ACCESS_THREAD_NAME);
                loop.setDaemon(true);
                loop.setPriority(Thread.MIN_PRIORITY);
            } catch (SecurityException se) {
                return false;
            }
            accessLoopThread = loop;
            //accessLoopRunnable = accessLoop;
            accessLoopRunning = true;
            loop.start();
        }
        return true;
    }
    
    static void stopAccessLoop() {
        accessLoopRunning = false;
        //accessLoopRunnable = null;
        if (accessLoopThread != null) {
            accessLoopThread.interrupt();
        }
    }
    
    static JPDATruffleDebugManager setUpDebugManager() {
        if (debugManager == null) {
            debugManager = JPDATruffleDebugManager.setUp();
        }
        return debugManager;
    }
    
    //static JPDATruffleDebugManager setUpDebugManagerFor(ScriptEngine engine) {
    static JPDATruffleDebugManager setUpDebugManagerFor(Object engine) {
        debugManager = JPDATruffleDebugManager.setUp((ScriptEngine) engine);
        return debugManager;
    }
    
    static void executionHalted(Node astNode, MaterializedFrame frame,
                                long srcId, String srcName, String srcPath, int line, String code) {
        // Called when the execution is halted.
        setCommand();
    }
    
    static void executionStepInto(Node astNode, String name,
                                  long srcId, String srcName, String srcPath, int line, String code) {
        // Called when the execution steps into a call.
        setCommand();
    }
    
    private static void setCommand() {
        switch (stepCmd) {
            case 0: debugManager.prepareContinue();
                    break;
            case 1: debugManager.prepareStep(1);
                    break;
            case 2: debugManager.prepareNext(1);
                    break;
            case 3: boolean success = debugManager.prepareStepOut();
                    System.err.println("Successful step out = "+success);
                    break;
        }
        stepCmd = 0;
    }
    
    static void debuggerAccess() {
        // A breakpoint is submitted on this method.
        // When accessLoopThread is interrupted, this breakpoint is hit
        // and methods can be executed via JPDA debugger.
    }
    
    /*
    static boolean inStepInto(Node astNode, String name) {
        if (isSteppingInto) {
            executionStepInto(astNode, name);
            return true;
        } else {
            return false;
        }
    }
    */
    
    static LineBreakpoint setLineBreakpoint(String path, int line) {
        Source source = SourceFactory.fromFile(path, true);
        LineBreakpoint lb = debugManager.setLineBreakpoint(new SourceLineLocation(source, line));
        System.err.println("setLineBreakpoint("+path+", "+line+"): source = "+source+", lb = "+lb);
        return lb;
    }
    
    private static class AccessLoop implements Runnable {

        @Override
        public void run() {
            while (accessLoopRunning) {
                // Wait until we're interrupted
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException iex) {}
                debuggerAccess();
            }
        }
        
    }
    
}
