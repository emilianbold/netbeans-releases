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
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ruby.debugger.breakpoints.RubyBreakpoint;
import org.netbeans.modules.ruby.rubyproject.execution.DirectoryFileLocator;
import org.netbeans.modules.ruby.rubyproject.execution.ExecutionDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.text.Line;
import org.openide.util.lookup.Lookups;
import org.rubyforge.debugcommons.RubyDebugEvent;
import org.rubyforge.debugcommons.RubyDebugEventListener;
import org.rubyforge.debugcommons.RubyDebuggerException;
import org.rubyforge.debugcommons.RubyDebuggerProxy;

/**
 * @author Martin Krauskopf
 */
public abstract class TestBase extends NbTestCase {
    
    private enum Engine { CLASSIC, RDEBUG_IDE }
    
    protected static boolean watchStepping = false;
    private Stack<Engine> engines;

    protected TestBase(final String name, final boolean verbose) {
        super(name);
        MockServices.setServices(IFL.class);
        if (verbose) {
            Util.LOGGER.setLevel(Level.ALL);
            Util.LOGGER.addHandler(new TestHandler(getName()));
            org.rubyforge.debugcommons.Util.LOGGER.setLevel(Level.ALL);
            org.rubyforge.debugcommons.Util.LOGGER.addHandler(new TestHandler(getName()));
        }
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("ruby.interpreter", TestBase.getFile("ruby.executable", true).getAbsolutePath());
        engines = new Stack<Engine>();
        engines.push(Engine.CLASSIC);
        if (isRDebugExecutableCorrectlySet()) {
            engines.push(Engine.RDEBUG_IDE);
        }
        doCleanUp();
    }
    
    private void doCleanUp() {
        for (RubyBreakpoint bp : RubyBreakpoint.getBreakpoints()) {
            RubyBreakpoint.removeBreakpoint(bp);
        }
        DebuggerManager.getDebuggerManager().finishAllSessions();
    }
    
    protected TestBase(final String name) {
        this(name, false);
    }
    
    
    protected Process startDebugging(final File f) throws RubyDebuggerException, IOException, InterruptedException {
        return startDebugging(f, true);
    }
    
    protected Process startDebugging(final File f, final boolean waitForSuspension) throws RubyDebuggerException, IOException, InterruptedException {
        ExecutionDescriptor desc = new ExecutionDescriptor(
                f.getName(), f.getParentFile(), f.getAbsolutePath());
        desc.fileLocator(new DirectoryFileLocator(FileUtil.toFileObject(f.getParentFile())));
        Process process = RubyDebugger.startDebugging(desc);
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
    
    protected boolean switchToNextEngine() {
        if (engines.isEmpty()) {
            return false;
        }
        Engine engine = engines.pop();
        switch (engine) {
            case CLASSIC:
                DebuggerPreferences.getInstance().setUseClassicDebugger(true);
                break;
            case RDEBUG_IDE:
                switchToRDebugIDE();
                break;
            default:
                fail("Unknown engine type: " + engine);
        }
        return true;
    }
    
    protected boolean tryToSwitchToRDebugIDE() {
        boolean available = isRDebugExecutableCorrectlySet();
        if (available) {
            switchToRDebugIDE();
        }
        return available;
    }
    
    protected void switchToRDebugIDE() {
        DebuggerPreferences.getInstance().setUseClassicDebugger(false);
        File rubyExecutable = TestBase.getFile("ruby.executable", true);
        RubyInstallation.getInstance().setRuby(rubyExecutable.getAbsolutePath());
    }

    private File getRDebugExecutable(boolean failIfNotAvailable) {
        return TestBase.getFile("rdebug.executable", failIfNotAvailable);
    }

    private boolean isRDebugExecutableCorrectlySet() {
        File rdebugExecutable = getRDebugExecutable(false);
        return rdebugExecutable != null && rdebugExecutable.isFile();
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
        File script = new File(getWorkDir(), name);
        PrintWriter pw = new PrintWriter(script);
        try {
            for (String line : scriptContent) {
                pw.println(line);
            }
        } finally {
            pw.close();
        }
        return script;
    }
    
    protected static RubyBreakpoint addBreakpoint(final FileObject fo, final int line) throws RubyDebuggerException {
        return RubyBreakpoint.addBreakpoint(createDummyLine(fo, line - 1));
    }
    
    static void doAction(final Object action) throws InterruptedException {
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
    
    private void waitForEvents(RubyDebuggerProxy proxy, int n, Runnable block) throws InterruptedException {
        final CountDownLatch events = new CountDownLatch(n);
        RubyDebugEventListener listener = new RubyDebugEventListener() {
            public void onDebugEvent(RubyDebugEvent e) {
                Util.finest("Received event: " + e);
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
            if (relativePath.equals("ruby/debug-commons-0.9.4/classic-debug.rb")) {
                File rubydebugDir = getDirectory("rubydebug.dir", true);
                File cd = new File(rubydebugDir, "classic-debug.rb");
                assertTrue("classic-debug found in " + rubydebugDir, cd.isFile());
                return cd;
            } else if (relativePath.equals("jruby-1.0")) {
                return getDirectory("jrubyhome.dir", true);
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
