/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.spi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.apache.tools.ant.module.xml.AntProjectSupport;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

// For debugging info, add to nbproject/private/private.properties:
// test-unit-sys-prop.org.apache.tools.ant.module.bridge.impl.NbBuildLogger.LOG_AT_WARNING=true

/**
 * Tests functionality of {@link AntLogger}.
 * Specifically, NbBuildLogger.
 * @author Jesse Glick
 */
public class AntLoggerTest extends NbTestCase {
    
    static {
        AntLoggerTest.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
    }

    private static final TestLogger LOGGER = new TestLogger();

    public AntLoggerTest(String name) {
        super(name);
    }

    private File testdir;
    private FileObject testdirFO;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LOGGER.reset();
        testdir = new File(this.getDataDir(), "antlogger");
        assertTrue("have a dir " + testdir, testdir.isDirectory());
        testdirFO = FileUtil.toFileObject(testdir);
        assertNotNull("have testdirFO", testdirFO);
    }

    private static void run(FileObject script) throws Exception {
        run(script, null);
    }
    private static void run(FileObject script, String[] targets) throws Exception {
        int res = AntTargetExecutor.createTargetExecutor(new AntTargetExecutor.Env()).execute(new AntProjectSupport(script), targets).result();
        if (res != 0) {
            throw new IOException("Nonzero exit code: " + res + "; messages: " + LOGGER.getMessages());
        }
    }

    public void testRunningAnt() throws Exception {
        File something = new File(System.getProperty("java.io.tmpdir"), "something");
        if (something.exists()) {
            something.delete();
        }
        run(testdirFO.getFileObject("trivial.xml"));
        assertTrue("now " + something + " exists", something.isFile());
    }
    
    public void testLocationOfImportedTargetsWithoutLineNumbers() throws Exception {
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        run(testdirFO.getFileObject("importing.xml"));
        File importing = new File(testdir, "importing.xml");
        File imported = new File(testdir, "imported.xml");
        assertEquals("correct 2 targets run (NOTE: you need Ant 1.6.0+)", Arrays.asList(new String[] {
            imported + "#subtarget",
            importing + "#main",
        }), LOGGER.getTargetsStarted());
    }
    
    public void testLocationOfImportedTargetsWithLineNumbers() throws Exception {
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        LOGGER.collectLineNumbersForTargets = true;
        run(testdirFO.getFileObject("importing.xml"));
        File importing = new File(testdir, "importing.xml");
        File imported = new File(testdir, "imported.xml");
        assertEquals("correct 2 targets run (NOTE: you need Ant 1.6.3+)", Arrays.asList(new String[] {
            imported + ":3#subtarget",
            importing + ":4#main",
        }), LOGGER.getTargetsStarted());
    }
    
    public void testTaskdef() throws Exception {
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        LOGGER.interestingTasks = AntLogger.ALL_TASKS;
        LOGGER.interestingLogLevels = new int[] {AntEvent.LOG_INFO, AntEvent.LOG_WARN};
        run(testdirFO.getFileObject("taskdefs.xml"));
        //System.err.println("messages=" + LOGGER.messages);
        assertTrue("got info message", LOGGER.getMessages().contains("mytask:" + AntEvent.LOG_INFO + ":MyTask info message"));
        assertFalse("did not get verbose message", LOGGER.getMessages().contains("mytask:" + AntEvent.LOG_VERBOSE + ":MyTask verbose message"));
        assertTrue("got warn message", LOGGER.getMessages().contains("mytask:" + AntEvent.LOG_WARN + ":MyTask warn message"));
    }
    
    public void testCorrectTaskFromIndirectCall() throws Exception {
        // #49464: if a task calls something which in turn does Project.log w/o the Task handle,
        // you lose all useful information. But you can guess which Task it is - you know some
        // task has been started and not yet finished. Within limits. Imports, <ant>, etc. can
        // screw up the accounting.
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        LOGGER.interestingTasks = AntLogger.ALL_TASKS;
        LOGGER.interestingLogLevels = new int[] {AntEvent.LOG_DEBUG};
        run(testdirFO.getFileObject("property.xml"));
        //System.err.println("messages=" + LOGGER.messages);
        List<String> messages = LOGGER.getMessages();
        assertTrue("have message with task ID in " + messages, messages.contains("property:4:Setting project property: propname -> propval"));
    }

    public void testAntEventDetails() throws Exception {
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        LOGGER.interestingTasks = new String[] {"echo"};
        LOGGER.interestingLogLevels = new int[] {AntEvent.LOG_INFO};
        run(testdirFO.getFileObject("property.xml"));
        assertTrue(LOGGER.antEventDetailsOK);
        LOGGER.antEventDetailsOK = false;
        run(testdirFO.getFileObject("property.xml"), new String[] {"run2"});
        assertTrue("#71816: works even inside <antcall>", LOGGER.antEventDetailsOK);
    }
    
    /**
     * Sample logger which collects results.
     */
    private static final class TestLogger extends AntLogger {
        
        public boolean interestedInSessionFlag;
        public boolean interestedInAllScriptsFlag;
        public Set<File> interestingScripts;
        public String[] interestingTargets;
        public String[] interestingTasks;
        public int[] interestingLogLevels;
        public boolean collectLineNumbersForTargets;
        /** Format of each: "/path/to/file.xml:line#targetName" (line numbers only if collectLineNumbersForTargets) */
        private List<String> targetsStarted;
        /** Format of each: "taskname:level:message" */
        private List<String> messages;
        private boolean antEventDetailsOK;
        
        public TestLogger() {}
        
        /** Set everything back to default values as in AntLogger base class. */
        public synchronized void reset() {
            interestedInSessionFlag = false;
            interestedInAllScriptsFlag = false;
            interestingScripts = new HashSet<File>();
            interestingTargets = AntLogger.NO_TARGETS;
            interestingTasks = AntLogger.NO_TASKS;
            interestingLogLevels = new int[0];
            collectLineNumbersForTargets = false;
            targetsStarted = new ArrayList<String>();
            messages = new ArrayList<String>();
            antEventDetailsOK = false;
        }
        
        public synchronized List<String> getTargetsStarted() {
            return new ArrayList<String>(targetsStarted);
        }
        
        public synchronized List<String> getMessages() {
            return new ArrayList<String>(messages);
        }

        @Override
        public boolean interestedInAllScripts(AntSession session) {
            return interestedInAllScriptsFlag;
        }

        @Override
        public String[] interestedInTasks(AntSession session) {
            return interestingTasks;
        }

        @Override
        public boolean interestedInScript(File script, AntSession session) {
            return interestingScripts.contains(script);
        }

        @Override
        public String[] interestedInTargets(AntSession session) {
            return interestingTargets;
        }

        @Override
        public boolean interestedInSession(AntSession session) {
            return interestedInSessionFlag;
        }

        @Override
        public int[] interestedInLogLevels(AntSession session) {
            return interestingLogLevels;
        }

        @Override
        public synchronized void targetStarted(AntEvent event) {
            int line = event.getLine();
            targetsStarted.add(event.getScriptLocation() +
                (collectLineNumbersForTargets && line != -1 ? ":" + line : "") +
                '#' + event.getTargetName());
        }
        
        @Override
        public synchronized void messageLogged(AntEvent event) {
            String toadd = "" + event.getLogLevel() + ":" + event.getMessage();
            String taskname = event.getTaskName();
            if (taskname != null) {
                toadd = taskname + ":" + toadd;
            }
            messages.add(toadd);
        }

        @Override
        public synchronized void buildFinished(AntEvent event) {
            Throwable t = event.getException();
            if (t != null) {
                messages.add("EXC:" + t);
            }
        }

        @Override
        public void taskStarted(AntEvent event) {
            antEventDetailsOK |=
                    "echo".equals(event.getTaskName()) &&
                    "meaningless".equals(event.getTaskStructure().getText()) &&
                    "info".equals(event.getTaskStructure().getAttribute("level")) &&
                    event.getPropertyNames().contains("propname") &&
                    "propval".equals(event.getProperty("propname"));
        }
        
    }
    
    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            try {
                setLookups(new Lookup[] {
                    Lookups.fixed(new Object[] {
                        new IFL(),
                        Class.forName("org.netbeans.modules.masterfs.MasterURLMapper").newInstance(),
                        LOGGER,
                    }),
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final class IFL extends InstalledFileLocator {
        public IFL() {}
        @Override
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if (relativePath.equals("ant/nblib/bridge.jar")) {
                String path = System.getProperty("test.bridge.jar");
                assertNotNull("must set test.bridge.jar", path);
                return new File(path);
            } else if (relativePath.equals("ant")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path);
            } else if (relativePath.startsWith("ant/")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path, relativePath.substring(4).replace('/', File.separatorChar));
            } else {
                return null;
            }
        }
    }

}
