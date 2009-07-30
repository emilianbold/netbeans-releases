/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.client.javascript.debugger.ui;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.javascript.editing.JsTestBase;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSBreakpoint;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSBreakpointManager;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSFileObjectBreakpoint;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.text.Annotatable;
import org.openide.text.Line;
import org.openide.util.lookup.Lookups;

/**
 * @author joelle
 */
public class NbJSDTestBase extends JsTestBase {

    /** Default constructor.
     * @param testName name of particular test case
     */
    public NbJSDTestBase(String testName) {
        super(testName);
    }

    /** Called before every test case. */
    public void setUp() throws Exception {
        System.out.println("########  NBJSTest Suite:" + getName() + "  #######");
        super.setUp();

        MockServices.setServices(DialogDisplayerImpl.class, IFL.class);
        touch(getWorkDir(), "config/Services/org-netbeans-modules-debugger-Settings.properties");
        //super.setUp();
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());

//        platform = RubyPlatformManager.addPlatform(TestBase.getFile("ruby.executable", true));
//        assertFalse("is native Ruby", platform.isJRuby());
//        assertTrue(platform.getInterpreter() + " has RubyGems installed", platform.hasRubyGemsInstalled());
//        String problems = platform.getFastDebuggerProblemsInHTML();
//        assertNull("fast debugger installed: " + problems, problems);
//        
//        engines = new Stack<Engine>();
//        engines.push(Engine.CLASSIC);
//        if (isRDebugExecutableCorrectlySet()) {
//            engines.push(Engine.RDEBUG_IDE);
//        }
        doCleanUp();
    }

    /** Called after every test case. */
    public void tearDown() throws Exception {
        super.tearDown();
    }

    protected FileObject touch(final File dir, final String binary) throws IOException {
        if (!dir.isDirectory()) {
            assertTrue("success to create " + dir, dir.mkdirs());
        }
        FileObject dirFO = FileUtil.toFileObject(FileUtil.normalizeFile(dir));
        return FileUtil.createData(dirFO, binary);
    }

    private void doCleanUp() {
        for (NbJSBreakpoint bp : NbJSBreakpointManager.getBreakpoints()) {
            NbJSBreakpointManager.removeBreakpoint(bp);
        }
        DebuggerManager.getDebuggerManager().finishAllSessions();
    }

//    protected Process startDebugging(final File f) throws IOException, InterruptedException {
//        return startDebugging(f, true);
//    }
//    
//    protected Process startDebugging(final File toTest, final boolean waitForSuspension) throws RubyDebuggerException, IOException, InterruptedException {
//        ExecutionDescriptor desc = new ExecutionDescriptor(platform,
//                toTest.getName(), toTest.getParentFile(), toTest.getAbsolutePath());
//        desc.fileLocator(new DirectoryFileLocator(FileUtil.toFileObject(toTest.getParentFile())));
//        Process process = RubyDebugger.startDebugging(desc);
//        if (waitForSuspension) {
//            waitForSuspension();
//        }
//        return process;
//    }
//    private void waitForSuspension() throws InterruptedException {
//        RubySession session = Util.getCurrentSession();
//        //        while (session.getFrames() == null || session.getFrames().length == 0) {
//        while (!session.isSessionSuspended()) {
//            Thread.sleep(300);
//        }
//    }
//    
//    protected boolean switchToNextEngine() {
//        if (engines.isEmpty()) {
//            return false;
//        }
//        Engine engine = engines.pop();
//        switch (engine) {
//            case CLASSIC:
//                forceClassicDebugger(true);
//                break;
//            case RDEBUG_IDE:
//                forceClassicDebugger(false);
//                break;
//            default:
//                fail("Unknown engine type: " + engine);
//        }
//        return true;
//    }
//    
//    protected boolean tryToSwitchToRDebugIDE() {
//        assertFalse("JRuby Fast debugger not supported yet", platform.isJRuby());
//        boolean available = isRDebugExecutableCorrectlySet();
//        if (available) {
//            forceClassicDebugger(false);
//        }
//        return available;
//    }
//    
//    protected void switchToJRuby() {
//        platform = RubyPlatformManager.getDefaultPlatform();
//    }
//
//    private File getRDebugExecutable() {
//        String rdebug = Util.findRDebugExecutable(platform);
//        return rdebug == null ? null : new File(rdebug);
//    }
//
//    private boolean isRDebugExecutableCorrectlySet() {
//        File rdebugExecutable = getRDebugExecutable();
//        return rdebugExecutable != null && rdebugExecutable.isFile();
//    }
    /**
     * Creates test.js script in the {@link #getWorkDir} with the given content.
     */
    protected File createJavaScript(final String[] scriptContent) throws IOException {
        return createJavaScript(scriptContent, "test.js");
    }

    /**
     * Creates script with the given name in the {@link #getWorkDir} with the
     * given content.
     */
    protected File createJavaScript(final String[] scriptContent, final String name) throws IOException {
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

    protected static NbJSFileObjectBreakpoint addBreakpoint(final FileObject fo, final int line) {
        return (NbJSFileObjectBreakpoint)NbJSBreakpointManager.addBreakpoint(createDummyLine(fo, line - 1));
    }

//    static void doAction(final Object action) throws InterruptedException {
//        if (watchStepping) {
//            Thread.sleep(3000);
//        }
//        DebuggerManager manager = DebuggerManager.getDebuggerManager();
//        DebuggerEngine engine = manager.getCurrentEngine();
//        ActionsManager actionManager = engine.getActionsManager();
//        actionManager.doAction(action);
//    }
//    
//    protected void doContinue() throws InterruptedException {
//        waitForEvents(Util.getCurrentSession().getProxy(), 1, new Runnable() {
//            public void run() {
//                try {
//                    doAction(org.netbeans.api.debugger.ActionsManager.ACTION_CONTINUE);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//    }
//    
//    protected void waitForEvents(RubyDebuggerProxy proxy, int n, Runnable block) throws InterruptedException {
//        final CountDownLatch events = new CountDownLatch(n);
//        RubyDebugEventListener listener = new RubyDebugEventListener() {
//            public void onDebugEvent(RubyDebugEvent e) {
//                Util.finest("Received event: " + e);
//                events.countDown();
//            }
//        };
//        proxy.addRubyDebugEventListener(listener);
//        block.run();
//        events.await();
//        proxy.removeRubyDebugEventListener(listener);
//    }
    @SuppressWarnings("deprecation")
    protected static Line createDummyLine(final FileObject fo, final int editorLineNum) {

        return new DummyLine(fo, editorLineNum);
//        return new Line(Lookups.singleton(fo)) {
//            public int getLineNumber() { return editorLineNum; }
//            public void show(int kind, int column) { throw new UnsupportedOperationException("Not supported."); }
//            public void setBreakpoint(boolean b) { throw new UnsupportedOperationException("Not supported."); }
//            public boolean isBreakpoint() { throw new UnsupportedOperationException("Not supported."); }
//            public void markError() { throw new UnsupportedOperationException("Not supported."); }
//            public void unmarkError() { throw new UnsupportedOperationException("Not supported."); }
//            public void markCurrentLine() { throw new UnsupportedOperationException("Not supported."); }
//            public void unmarkCurrentLine() { throw new UnsupportedOperationException("Not supported."); }
//        };
    }

    public static class DummyLine extends Line {

        int editorLineNum;

        public DummyLine(final FileObject fo, final int editorLineNum) {
            super(Lookups.singleton(fo));

            this.editorLineNum = editorLineNum;
        }

        public int getLineNumber() {
            return editorLineNum;
        }

        public void show(int kind, int column) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Deprecated
        public void setBreakpoint(boolean b) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Deprecated
        public boolean isBreakpoint() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Deprecated
        public void markError() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Deprecated
        public void unmarkError() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Deprecated
        public void markCurrentLine() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Deprecated
        public void unmarkCurrentLine() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Deprecated
        public void moveLine() {
            firePropertyChange(Annotatable.PROP_TEXT, null, null);
        }
    }

//    protected void forceClassicDebugger(boolean force) {
//        RubyDebugger.FORCE_CLASSIC = force;
//    }
    public static final class IFL extends InstalledFileLocator {

        public IFL() {
        }

        @Override
        public File locate(String relativePath, String codeNameBase, boolean localized) {
//            if (relativePath.equals("ruby/debug-commons-0.9.5/classic-debug.rb")) {
//                File rubydebugDir = getDirectory("rubydebug.dir", true);
//                File cd = new File(rubydebugDir, "classic-debug.rb");
//                assertTrue("classic-debug found in " + rubydebugDir, cd.isFile());
//                return cd;
//            }
////            } else if (relativePath.equals("jruby-1.1RC1")) {
////                return TestUtil.getXTestJRubyHome();
//            } else {
//                return null;
//            }
            return null;
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

    public FileObject createJSFO() throws IOException {
        String[] fruitContent = {
            "document.write(\"<h1>This is a header</h1>\");",
            "document.write(\"<p>This is a paragraph</p>\");",
            "document.write(\"<p>This is another paragraph</p>\");",
            "var firstname;",
            "firstname=\"Hege\";",
            "ocument.write(firstname);",
            "document.write(\"<br />\");",
            "firstname=\"Tove\";",
            "document.write(firstname);",
        };
        File fruitF = createJavaScript(fruitContent, "mytestfile.js");
        FileObject fruitFO = FileUtil.toFileObject(fruitF);
        return fruitFO;
    }
}
