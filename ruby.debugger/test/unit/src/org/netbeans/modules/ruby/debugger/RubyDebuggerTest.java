/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.ruby.debugger;

import java.io.File;
import java.io.IOException;
import junit.framework.AssertionFailedError;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.ruby.debugger.breakpoints.RubyBreakpoint;
import org.netbeans.modules.ruby.debugger.breakpoints.RubyBreakpointManager;
import org.netbeans.modules.ruby.platform.DebuggerPreferences;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.platform.gems.GemManager;
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
        clearWorkDir();
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
        while (switchToNextEngine()) {
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
        RubyBreakpointManager.removeBreakpoint(bp4);
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
        RubyBreakpointManager.removeBreakpoint(bp4);
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
        RubyBreakpointManager.removeBreakpoint(bp2);
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

    public void testFinish2() throws Exception {
        // issue #109659
        if (tryToSwitchToRDebugIDE()) {
            String[] testContent = {
                "Thread.start() { puts 'hello from new thread' }",
                "puts 'main thread'"
            };
            File testF = createScript(testContent);
            FileObject testFO = FileUtil.toFileObject(testF);
            addBreakpoint(testFO, 1);
            Process p = startDebugging(testF);
            doAction(ActionsManager.ACTION_STEP_OVER);
            doAction(ActionsManager.ACTION_KILL);
            p.waitFor();
        }
    }

    public void testFinishWhenSpawnedThreadIsSuspended() throws Exception {
        if (tryToSwitchToRDebugIDE()) {
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
        while ((getEngineManager()) != null) {
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

    public void testDoNotStepIntoTheEval() throws Exception { // issue #106115
        while (switchToNextEngine()) {
            String[] testContent = {
                "module A",
                "  module_eval(\"def A.a; sleep 0.01\\n sleep 0.01; end\")",
                "end",
                "A.a",
                "sleep 0.01",
                "sleep 0.01"
            };
            File testF = createScript(testContent);
            FileObject testFO = FileUtil.toFileObject(testF);
            addBreakpoint(testFO, 4);
            Process p = startDebugging(testF);
            doAction(ActionsManager.ACTION_STEP_INTO);
            doAction(ActionsManager.ACTION_STEP_INTO);
            doAction(ActionsManager.ACTION_STEP_INTO);
            p.waitFor();
        }
    }
    
//    public void testDoNotStepIntoNonResolvedPath() throws Exception { // issue #106115
//        MockServices.setServices(DialogDisplayerImpl.class, IFL.class);
//        switchToJRuby();
//        String[] testContent = {
//            "require 'java'",
//            "import 'java.util.TreeSet'",
//            "t = TreeSet.new",
//            "t.add 1",
//            "t.add 2"
//        };
//        File testF = createScript(testContent);
//        FileObject testFO = FileUtil.toFileObject(testF);
//        addBreakpoint(testFO, 3);
//        Process p = startDebugging(testF);
//        doAction(ActionsManager.ACTION_STEP_INTO);
//        doAction(ActionsManager.ACTION_STEP_INTO);
//        doAction(ActionsManager.ACTION_STEP_INTO);
//        p.waitFor();
//    }
    
    public void testCheckAndTuneSettings() throws IOException {
        MockServices.setServices(DialogDisplayerImpl.class, IFL.class);
        RubyPlatform jruby = RubyPlatformManager.getDefaultPlatform();
        ExecutionDescriptor descriptor = new ExecutionDescriptor(jruby);
        // DialogDisplayerImpl.createDialog() assertion would fail if dialog is shown
        assertTrue("default setting OK with JRuby", RubyDebugger.checkAndTuneSettings(descriptor));
        FileObject gemRepo = FileUtil.toFileObject(getWorkDir()).createFolder("gem-repo");
        GemManager.initializeRepository(gemRepo);
        jruby.setGemHome(FileUtil.toFile(gemRepo));
        assertFalse("does not have fast debugger", jruby.hasFastDebuggerInstalled());
        
        DebuggerPreferences prefs = DebuggerPreferences.getInstance();
        prefs.setUseClassicDebugger(jruby, false);
        try {
            assertTrue("fail when no fast debugger available", RubyDebugger.checkAndTuneSettings(descriptor));
        } catch (AssertionFailedError afe) {
            // OK, expected
        }
        
        installFakeFastRubyDebugger(jruby);
        assertTrue("succeed when fast debugger available", RubyDebugger.checkAndTuneSettings(descriptor));
    }

    private DebuggerEngine getEngineManager() {
        return DebuggerManager.getDebuggerManager().getCurrentEngine();
    }
    
}

