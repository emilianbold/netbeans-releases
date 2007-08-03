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
import org.netbeans.modules.ruby.debugger.breakpoints.RubyBreakpoint;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.rubyforge.debugcommons.model.RubyThreadInfo;
import org.rubyforge.debugcommons.model.RubyVariable;

/**
 * @author Martin Krauskopf
 */
public final class RubySessionTest extends TestBase {
    
    public RubySessionTest(final String name) {
        super(name, true);
    }
    
    public void testLocalVariables() throws Exception {
        if (tryToSwitchToRDebugIDE()) {
            String[] testContent = {
                "a = 5",
                "sleep 0.01",
            };
            File testF = createScript(testContent);
            FileObject testFO = FileUtil.toFileObject(testF);
            addBreakpoint(testFO, 2);
            Process p = startDebugging(testF);
            RubySession session = Util.getCurrentSession();
            assertEquals("a variable", 1, session.getVariables().length);
            doContinue();
            p.waitFor();
        }
    }
    
    public void testSuspendedThread() throws Exception {
        if (tryToSwitchToRDebugIDE()) {
            String[] testContent = {
                "loop do",
                "  a = 2",
                "  sleep 0.1",
                "  puts a",
                "end"
            };
            File testF = createScript(testContent);
            FileObject testFO = FileUtil.toFileObject(testF);
            RubyBreakpoint bp3 = addBreakpoint(testFO, 3);
            Process p = startDebugging(testF);
            RubySession session = Util.getCurrentSession();
            assertTrue("session suspended", session.isSessionSuspended());
            RubyVariable[] vars = session.getVariables();
            assertEquals("a variable", 1, vars.length);
            bp3.disable();
            doAction(ActionsManager.ACTION_CONTINUE);
            Thread.sleep(1000);
            assertEquals("a variable", 0, session.getVariables().length);
            doAction(ActionsManager.ACTION_KILL);
            p.waitFor();
        }
    }
    
    public void testSwitchToNonSuspendedThread() throws Exception {
        if (tryToSwitchToRDebugIDE()) {
            String[] testContent = {
                "loop do",
                "  sleep 1",
                "end"
            };
            File testF = createScript(testContent);
            Process p = startDebugging(testF, false);
            RubySession session = Util.getCurrentSession();
            RubyThreadInfo ti = session.getThreadInfos()[0];
            session.switchThread(ti.getId(), null);
            doAction(ActionsManager.ACTION_KILL);
            p.waitFor();
        }
    }

    public void testResolveAbsolutePath() throws Exception {
        String[] testContent = {
            "sleep 0.01",
        };
        File testF = createScript(testContent);
        FileObject testFO = FileUtil.toFileObject(testF);
        addBreakpoint(testFO, 1);
        Process p = startDebugging(testF);
        RubySession session = Util.getCurrentSession();
        assertNotNull("test.rb relative resolved", session.resolveAbsolutePath(testF.getName()));
        assertNotNull("test.rb absolute resolved", session.resolveAbsolutePath(testF.getAbsolutePath()));
        doContinue();
        p.waitFor();
    }

    public void testSynchronization() throws Exception { // #111088
        String[] testContent = {
            "require 'thread'",
            "m = Mutex.new",
            "i = 0",
            "(1..2).each do",
            "  Thread.new do",
            "    (1..5).each do",
            "      Thread.new do",
            "        (1..10).each do",
            "          Thread.new do",
            "            sleep 0.01",
            "            m.synchronize do",
            "              i += 1",
            "            end",
            "          end",
            "        end",
            "      end",
            "    end",
            "  end",
            "end",
            "while i != 100",
            "  sleep 0.4",
            "end",
            "puts 'main thread'"
        };
        File testF = createScript(testContent);
        FileObject testFO = FileUtil.toFileObject(testF);
        RubyBreakpoint bp = addBreakpoint(testFO, 10);
        Process p = startDebugging(testF);
        doContinue();
        RubyBreakpoint.removeBreakpoint(bp);
        doContinue();
        p.waitFor();
    }

}
