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
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.api.ruby.platform.TestUtil;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.modules.ruby.debugger.breakpoints.RubyBreakpoint;
import org.netbeans.modules.ruby.debugger.breakpoints.RubyLineBreakpoint;
import org.netbeans.modules.ruby.debugger.breakpoints.RubyBreakpointManager;
import org.netbeans.modules.ruby.platform.execution.DirectoryFileLocator;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;
import org.rubyforge.debugcommons.RubyDebugEvent;
import org.rubyforge.debugcommons.RubyDebugEventListener;
import org.rubyforge.debugcommons.RubyDebuggerException;
import org.rubyforge.debugcommons.RubyDebuggerProxy;

public abstract class TestBase extends RubyTestBase {

    static {
        RubySession.TEST = true;
        EditorUtil.showLines = false;
    }
    private TestHandler testHandler;
    private boolean verbose;

    protected static boolean watchStepping = false;
    private RubyPlatform platform;
    private String jvmArgs;

    protected TestBase(final String name, final boolean verbose) {
        super(name);
        this.verbose = verbose;
    }

    @Override
    protected void setUp() throws Exception {
        if (verbose) {
            testHandler = new TestHandler(getName());
            Util.LOGGER.setLevel(Level.ALL);
            Util.LOGGER.addHandler(testHandler);
            org.rubyforge.debugcommons.Util.LOGGER.setLevel(Level.ALL);
            org.rubyforge.debugcommons.Util.LOGGER.addHandler(testHandler);
        }
        MockServices.setServices(DialogDisplayerImpl.class, IFL.class);
        touch(getWorkDir(), "config/Services/org-netbeans-modules-debugger-Settings.properties");
        super.setUp();
        File alternative = TestBase.getFile("ruby.executable", false);
        if (alternative != null) {
            platform = RubyPlatformManager.addPlatform(alternative);
        } else {
            platform = RubyPlatformManager.getDefaultPlatform();
        }
        assertTrue(platform + " has RubyGems installed", platform.hasRubyGemsInstalled());
        assertTrue(platform + " has fast debugger installed", platform.hasFastDebuggerInstalled());
        String problems = platform.getFastDebuggerProblemsInHTML();
        assertNull("fast debugger installed: " + problems, problems);

        doCleanUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (verbose) {
            Util.LOGGER.removeHandler(testHandler);
            org.rubyforge.debugcommons.Util.LOGGER.removeHandler(testHandler);
        }
    }

    private void doCleanUp() {
        for (RubyBreakpoint bp : RubyBreakpointManager.getBreakpoints()) {
            try {
                DebuggerManager.getDebuggerManager().removeBreakpoint(bp);
            } catch (Throwable t) {
                Exceptions.printStackTrace(t);
            }
        }
        DebuggerManager.getDebuggerManager().finishAllSessions();
    }

    protected TestBase(final String name) {
        this(name, false);
    }

    public void setJVMArgs(String jvmArgs) {
        this.jvmArgs = jvmArgs;
    }


    protected Process startDebugging(final String[] rubyCode, final int... breakpoints) throws RubyDebuggerException, IOException, InterruptedException {
        File testF = createScript(rubyCode);
        FileObject testFO = FileUtil.toFileObject(testF);
        for (int breakpoint : breakpoints) {
            addBreakpoint(testFO, breakpoint);
        }
        return startDebugging(testF);
    }

    protected Process startDebugging(final File f) throws RubyDebuggerException, IOException, InterruptedException {
        return startDebugging(f, true);
    }

    protected Process startDebugging(final File toTest, final RubyPlatform platform) throws RubyDebuggerException, IOException, InterruptedException {
        return startDebugging(toTest, true, platform);
    }

    protected Process startDebugging(final File toTest, final boolean waitForSuspension) throws RubyDebuggerException, IOException, InterruptedException {
        return startDebugging(toTest, waitForSuspension, platform);
    }

    private Process startDebugging(final File toTest, final boolean waitForSuspension, final RubyPlatform platform) throws RubyDebuggerException, IOException, InterruptedException {
        ExecutionDescriptor desc = new ExecutionDescriptor(platform,
                toTest.getName(), toTest.getParentFile(), toTest.getAbsolutePath());
        assertTrue(platform.hasFastDebuggerInstalled());
        desc.fileLocator(new DirectoryFileLocator(FileUtil.toFileObject(toTest.getParentFile())));
        if (this.jvmArgs != null) {
            desc.jvmArguments(this.jvmArgs);
        }
        RubySession session = RubyDebugger.startDebugging(desc);
        session.getProxy().startDebugging(RubyBreakpointManager.getBreakpoints());
        Process process = session.getProxy().getDebugTarged().getProcess();
        if (waitForSuspension) {
            waitForSuspension();
        }
        return process;
    }

    private void waitForSuspension() throws InterruptedException {
        RubySession session = Util.getCurrentSession();
        //        while (session.getFrames() == null || session.getFrames().length == 0) {
        while (!session.isSessionSuspended()) {
            Thread.sleep(300);
        }
    }

    /**
     * Creates test.rb script in the {@link #getWorkDir} with the given content.
     */
    protected File createScript(final String[] scriptContent) throws IOException {
        return createScript(scriptContent, "test.rb");
    }

    /**
     * Creates script with the given name in the {@link #getWorkDir} with the
     * given content.
     */
    protected File createScript(final String[] scriptContent, final String name) throws IOException {
        FileObject script = FileUtil.createData(FileUtil.toFileObject(getWorkDir()), name);
        PrintWriter pw = new PrintWriter(script.getOutputStream());
        try {
            for (String line : scriptContent) {
                pw.println(line);
            }
        } finally {
            pw.close();
        }
        return FileUtil.toFile(script);
    }

    protected static RubyLineBreakpoint addBreakpoint(final FileObject fo, final int line) throws RubyDebuggerException {
        return RubyBreakpointManager.addLineBreakpoint(createDummyLine(fo, line - 1));
    }

    public static void doAction(final Object action) throws InterruptedException {
        if (watchStepping) {
            Thread.sleep(3000);
        }
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        DebuggerEngine engine = manager.getCurrentEngine();
        ActionsManager actionManager = engine.getActionsManager();
        actionManager.doAction(action);
    }

    protected void doContinue() throws InterruptedException {
        waitForEvents(Util.getCurrentSession().getProxy(), 1, new Runnable() {
            public void run() {
                try {
                    doAction(org.netbeans.api.debugger.ActionsManager.ACTION_CONTINUE);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    protected void waitFor(final Process p) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                try {
                    p.waitFor();
                    latch.countDown();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }).start();
        latch.await(10, TimeUnit.SECONDS);
        if (latch.getCount() > 0) {
            fail("Process " + p + " was not finished.");
            p.destroy();
        }
    }

    protected void waitForEvents(RubyDebuggerProxy proxy, int n, Runnable block) throws InterruptedException {
        final CountDownLatch events = new CountDownLatch(n);
        RubyDebugEventListener listener = new RubyDebugEventListener() {
            public void onDebugEvent(RubyDebugEvent e) {
                Util.finer("Received event: " + e);
                events.countDown();
            }
        };
        proxy.addRubyDebugEventListener(listener);
        block.run();
        events.await();
        proxy.removeRubyDebugEventListener(listener);
    }

    @SuppressWarnings("deprecation")
    static Line createDummyLine(final FileObject fo, final int editorLineNum) {
        return new Line(Lookups.singleton(fo)) {
            public int getLineNumber() { return editorLineNum; }
            public void show(int kind, int column) { throw new UnsupportedOperationException("Not supported."); }
            public void setBreakpoint(boolean b) { throw new UnsupportedOperationException("Not supported."); }
            public boolean isBreakpoint() { throw new UnsupportedOperationException("Not supported."); }
            public void markError() { throw new UnsupportedOperationException("Not supported."); }
            public void unmarkError() { throw new UnsupportedOperationException("Not supported."); }
            public void markCurrentLine() { throw new UnsupportedOperationException("Not supported."); }
            public void unmarkCurrentLine() { throw new UnsupportedOperationException("Not supported."); }
        };
    }

    public static final class IFL extends InstalledFileLocator {

        public IFL() {}
        @Override
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if (relativePath.equals("ruby/debug-commons-0.9.5/classic-debug.rb")) {
                File rubydebugDir = getDirectory("rubydebug.dir", true);
                File cd = new File(rubydebugDir, "classic-debug.rb");
                assertTrue("classic-debug found in " + rubydebugDir, cd.isFile());
                return cd;
            } else if (relativePath.equals("jruby-1.1.4")) {
                return TestUtil.getXTestJRubyHome();
            } else {
                return null;
            }
        }
    }

    private static File resolveFile(final String property, final boolean mandatory) {
        String path = System.getProperty(property);
        assertTrue("must set " + property, !mandatory || (path != null));
        return path == null ? null : new File(path);
    }

    static File getFile(final String property, boolean mandatory) {
        File file = resolveFile(property, mandatory);
        assertTrue(file + " is file", !mandatory || file.isFile());
        return file;

    }

    static File getDirectory(final String property, final boolean mandatory) {
        File directory = resolveFile(property, mandatory);
        assertTrue(directory + " is directory", !mandatory || directory.isDirectory());
        return directory;
    }

    private static class TestHandler extends Handler {

        private final String name;

        TestHandler(final String name) {
            this.name = name;
        }

        public void publish(LogRecord rec) {
            PrintStream os = rec.getLevel().intValue() >= Level.WARNING.intValue() ? System.err : System.out;
            os.println("[" + System.currentTimeMillis() + "::" + name + "::" + rec.getLevel().getName() + "]: " + rec.getMessage());
            Throwable th = rec.getThrown();
            if (th != null) {
                th.printStackTrace(os);
            }
        }

        public void flush() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void close() throws SecurityException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
