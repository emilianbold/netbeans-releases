/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.ruby.debugger.breakpoints.RubyLineBreakpoint;
import org.netbeans.modules.ruby.debugger.breakpoints.RubyBreakpointManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.rubyforge.debugcommons.model.RubyThreadInfo;
import org.rubyforge.debugcommons.model.RubyVariable;

public final class RubySessionTest extends TestBase {
    
    public RubySessionTest(final String name) {
        super(name, true);
    }
    
    public void testLocalVariables() throws Exception {
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
        waitFor(p);
    }
    
    public void testSuspendedThread() throws Exception {
        String[] testContent = {
            "loop do",
            "  a = 2",
            "  sleep 0.1",
            "  puts a",
            "end"
        };
        File testF = createScript(testContent);
        FileObject testFO = FileUtil.toFileObject(testF);
        RubyLineBreakpoint bp3 = addBreakpoint(testFO, 3);
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
        waitFor(p);
    }
    
    public void testSwitchToNonSuspendedThread() throws Exception {
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
        waitFor(p);
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
        waitFor(p);
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
        RubyLineBreakpoint bp = addBreakpoint(testFO, 10);
        Process p = startDebugging(testF);
        doContinue();
        RubyBreakpointManager.removeBreakpoint(bp);
        doContinue();
        waitFor(p);
    }
    
    public void testRunTo() throws Exception {
        String[] testContent = {
            "sleep 0.01",
            "sleep 0.01",
            "sleep 0.01",
            "sleep 0.01",
        };
        final File testF = createScript(testContent);
        FileObject testFO = FileUtil.toFileObject(testF);
        addBreakpoint(testFO, 1);
        Process p = startDebugging(testF);
        final RubySession session = Util.getCurrentSession();
        waitForEvents(session.getProxy(), 1, new Runnable() {

            public void run() {
                session.runningToFile = testF;
                session.runningToLine = 3;
                try {
                    doAction(ActionsManager.ACTION_RUN_TO_CURSOR);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        assertTrue("session suspended", session.isSessionSuspended());
        doContinue();
        waitFor(p);
    }
}
