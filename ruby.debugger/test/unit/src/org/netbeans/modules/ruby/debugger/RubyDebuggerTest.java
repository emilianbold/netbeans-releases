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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.debugger;

import java.io.File;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.ruby.debugger.breakpoints.RubyBreakpoint;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 * @author Martin Krauskopf
 */
public final class RubyDebuggerTest extends TestBase {
    
    private static final boolean VERBOSE = false;
    
    public RubyDebuggerTest(final String name) {
        super(name, VERBOSE);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        watchStepping = false;
        DebuggerPreferences.getInstance().setVerboseDebugger(VERBOSE);
    }
    
    public void testBasics() throws Exception {
        String[] testContent = {
            "puts 'aaa'",
            "puts 'bbb'",
            "puts 'ccc'",
            "puts 'ddd'",
            "puts 'eee'",
        };
        File testF = createScript(testContent);
        FileObject testFO = FileUtil.toFileObject(testF);
        addBreakpoint(testFO, 2);
        addBreakpoint(testFO, 4);
        Process p = startDebugging(testF);
        doContinue(); // 2 -> 4
        doAction(ActionsManager.ACTION_STEP_OVER); // 4 -> 5
        doContinue(); // finish
        p.waitFor();
    }
    
    public void testStepInto() throws Exception {
        String[] testContent = {
            "def a",
            "  puts 'aaa'",
            "end",
            "a",
            "puts 'end'"
        };
        File testF = createScript(testContent);
        FileObject testFO = FileUtil.toFileObject(testF);
        addBreakpoint(testFO, 4);
        Process p = startDebugging(testF);
        doAction(ActionsManager.ACTION_STEP_INTO); // 4 -> 2
        doAction(ActionsManager.ACTION_STEP_OVER); // 2 -> 5
        doAction(ActionsManager.ACTION_STEP_OVER); // 5 -> finish
        p.waitFor();
    }
    
    public void testStepOut() throws Exception {
        String[] testContent = {
            "def a",
            "  puts 'a'",
            "  puts 'aa'",
            "  puts 'aaa'",
            "  puts 'aaaa'",
            "end",
            "a",
            "puts 'end'"
        };
        File testF = createScript(testContent);
        FileObject testFO = FileUtil.toFileObject(testF);
        addBreakpoint(testFO, 2);
        Process p = startDebugging(testF);
        doAction(ActionsManager.ACTION_STEP_OVER); // 2 -> 3
        doAction(ActionsManager.ACTION_STEP_OUT); // 3 -> 8
        doAction(ActionsManager.ACTION_STEP_OVER); // 8 -> finish
        p.waitFor();
    }
    
    public void testSimpleLoop() throws Exception {
        String[] testContent = {
            "1.upto(3) {",
            "  puts 'aaa'",
            "  puts 'bbb'",
            "  puts 'ccc'",
            "}",
        };
        File testF = createScript(testContent);
        FileObject testFO = FileUtil.toFileObject(testF);
        addBreakpoint(testFO, 2);
        RubyBreakpoint bp4 = addBreakpoint(testFO, 4);
        Process p = startDebugging(testF);
        doContinue(); // 2 -> 4
        doContinue(); // 4 -> 2
        RubyBreakpoint.removeBreakpoint(bp4);
        doContinue(); // 2 -> 2
        doContinue(); // 2 -> finish
        p.waitFor();
    }
    
    public void testSpaceAndSemicolonsInPath() throws Exception {
        String[] testContent = {
            "1.upto(3) {",
            "  puts 'aaa'",
            "  puts 'bbb'",
            "  puts 'ccc'",
            "}",
        };
        File testF = createScript(testContent, "path spaces semi:colon.rb");
        FileObject testFO = FileUtil.toFileObject(testF);
        addBreakpoint(testFO, 2);
        RubyBreakpoint bp4 = addBreakpoint(testFO, 4);
        Process p = startDebugging(testF);
        doContinue(); // 2 -> 4
        doContinue(); // 4 -> 2
        RubyBreakpoint.removeBreakpoint(bp4);
        doContinue(); // 2 -> 2
        doContinue(); // 2 -> finish
        p.waitFor();
    }
    
    //    public void testScriptArgumentsNoticed() throws Exception {
    //        String[] scriptArgs = { "--used-languages", "Ruby and Java" };
    //        String[] testContent = {
    //            "exit 1 if ARGV.size != 2",
    //            "puts 'OK'"
    //        };
    //        File testF = createScript(testContent);
    //        FileObject testFO = FileUtil.toFileObject(testF);
    //        addBreakpoint(testFO, 2);
    //        Process p = startDebugging(testF);
    //        Thread.sleep(3000); // TODO: do not depend on timing (use e.g. RubyDebugEventListener)
    //        doContinue(); // 2 -> finish
    //        p.waitFor();
    //    }
    
    public void testBreakpointsRemovingFirst() throws Exception {
        String[] testContent = {
            "3.times do", // 1
            "  b=10",     // 2
            "  b=11",     // 3
            "end"         // 4
        };
        File testF = createScript(testContent);
        FileObject testFO = FileUtil.toFileObject(testF);
        RubyBreakpoint bp2 = addBreakpoint(testFO, 2);
        addBreakpoint(testFO, 3);
        Process p = startDebugging(testF);
        doContinue(); // 2 -> 3
        doContinue(); // 3 -> 2
        RubyBreakpoint.removeBreakpoint(bp2);
        doContinue(); // 2 -> 3
        doContinue(); // 3 -> 3
        doContinue(); // 3 -> finish
        p.waitFor();
    }
    
    public void testBreakpointsUpdating() throws Exception {
        String[] testContent = {
            "4.times do", // 1
            "  b=10",     // 2
            "  b=11",     // 3
            "end"         // 4
        };
        File testF = createScript(testContent);
        FileObject testFO = FileUtil.toFileObject(testF);
        RubyBreakpoint bp2 = addBreakpoint(testFO, 2);
        addBreakpoint(testFO, 3);
        Process p = startDebugging(testF);
        doContinue(); // 2 -> 3
        doContinue(); // 3 -> 2
        bp2.disable();
        doContinue(); // 2 -> 3
        doContinue(); // 3 -> 3
        bp2.enable();
        doContinue(); // 3 -> 2
        doContinue(); // 2 -> 3
        doContinue(); // 3 -> finish
        p.waitFor();
    }
    
    public void testFinish() throws Exception {
        String[] testContent = {
            "sleep 0.1", // 1
            "sleep 0.1", // 2
        };
        File testF = createScript(testContent);
        FileObject testFO = FileUtil.toFileObject(testF);
        addBreakpoint(testFO, 2);
        Process p = startDebugging(testF);
        Thread.sleep(3000); // TODO: rather wait for appropriate event
        doAction(ActionsManager.ACTION_KILL);
        p.waitFor();
    }
    
    public void testFinishWhenSpawnedThreadIsSuspended() throws Exception {
        if (switchToRDebugIDE()) {
            String[] testContent = {
                "Thread.start do",
                "    puts '1'",
                "end"
            };
            File testF = createScript(testContent);
            FileObject testFO = FileUtil.toFileObject(testF);
            addBreakpoint(testFO, 2);
            Process p = startDebugging(testF);
            Thread.sleep(3000); // TODO: rather wait for appropriate event
            doAction(ActionsManager.ACTION_KILL);
            p.waitFor();
        }
    }
    
    public void testActionsFlood() throws Exception {
        // classic debugger only
        String[] testContent = {
            "20.times do",
            "    sleep 0.001",
            "end"
        };
        File testF = createScript(testContent);
        FileObject testFO = FileUtil.toFileObject(testF);
        addBreakpoint(testFO, 2);
        Process p = startDebugging(testF);
        DebuggerEngine engine;
        while ((engine = getEngineManager()) != null) {
            Thread.sleep(10);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    DebuggerEngine engine = getEngineManager();
                    if (engine != null) {
                        ActionsManager actionManager = engine.getActionsManager();
                        actionManager.doAction(ActionsManager.ACTION_STEP_OVER);
                    }
                }
            });
        }
        p.waitFor();
    }
    
    private DebuggerEngine getEngineManager() {
        return DebuggerManager.getDebuggerManager().getCurrentEngine();
    }
    
}
