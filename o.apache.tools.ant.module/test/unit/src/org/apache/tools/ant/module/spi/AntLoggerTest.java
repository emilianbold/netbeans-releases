/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.spi;

import java.io.File;
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

    protected void setUp() throws Exception {
        super.setUp();
        LOGGER.reset();
        testdir = new File(this.getDataDir(), "antlogger");
        assertTrue("have a dir " + testdir, testdir.isDirectory());
        testdirFO = FileUtil.toFileObject(testdir);
        assertNotNull("have testdirFO", testdirFO);
    }

    private static void run(FileObject script) throws Exception {
        AntTargetExecutor.createTargetExecutor(new AntTargetExecutor.Env()).execute(new AntProjectSupport(script), null);
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
        System.err.println("NOTE: testLocationOfImportedTargetsWithoutLineNumbers will fail unless you use Ant 1.6.0+!");
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        run(testdirFO.getFileObject("importing.xml"));
        File importing = new File(testdir, "importing.xml");
        File imported = new File(testdir, "imported.xml");
        assertEquals("correct 2 targets run", Arrays.asList(new String[] {
            imported + "#subtarget",
            importing + "#main",
        }), LOGGER.targetsStarted);
    }
    
    public void testLocationOfImportedTargetsWithLineNumbers() throws Exception {
        System.err.println("NOTE: testLocationOfImportedTargetsWithLineNumbers will fail unless you use Ant 1.6.3+!");
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        LOGGER.collectLineNumbersForTargets = true;
        run(testdirFO.getFileObject("importing.xml"));
        File importing = new File(testdir, "importing.xml");
        File imported = new File(testdir, "imported.xml");
        assertEquals("correct 2 targets run", Arrays.asList(new String[] {
            imported + ":3#subtarget",
            importing + ":4#main",
        }), LOGGER.targetsStarted);
    }
    
    public void testTaskdef() throws Exception {
        LOGGER.interestedInSessionFlag = true;
        LOGGER.interestedInAllScriptsFlag = true;
        LOGGER.interestingTargets = AntLogger.ALL_TARGETS;
        LOGGER.interestingTasks = AntLogger.ALL_TASKS;
        LOGGER.interestingLogLevels = new int[] {AntEvent.LOG_INFO, AntEvent.LOG_WARN};
        run(testdirFO.getFileObject("taskdefs.xml"));
        //System.err.println("messages=" + LOGGER.messages);
        assertTrue("got info message", LOGGER.messages.contains("mytask:" + AntEvent.LOG_INFO + ":MyTask info message"));
        assertFalse("did not get verbose message", LOGGER.messages.contains("mytask:" + AntEvent.LOG_VERBOSE + ":MyTask verbose message"));
        assertTrue("got warn message", LOGGER.messages.contains("mytask:" + AntEvent.LOG_WARN + ":MyTask warn message"));
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
        assertTrue("have message with task ID in " + LOGGER.messages, LOGGER.messages.contains("property:4:Setting project property: propname -> propval"));
    }
    
    /**
     * Sample logger which collects results.
     */
    private static final class TestLogger extends AntLogger {
        
        public boolean interestedInSessionFlag;
        public boolean interestedInAllScriptsFlag;
        public Set/*<File>*/ interestingScripts;
        public String[] interestingTargets;
        public String[] interestingTasks;
        public int[] interestingLogLevels;
        public boolean collectLineNumbersForTargets;
        /** Format of each: "/path/to/file.xml:line#targetName" (line numbers only if collectLineNumbersForTargets) */
        public List/*<String>*/ targetsStarted;
        /** Format of each: "taskname:level:message" */
        public List/*<String>*/ messages;
        
        public TestLogger() {}
        
        /** Set everything back to default values as in AntLogger base class. */
        public void reset() {
            interestedInSessionFlag = false;
            interestedInAllScriptsFlag = false;
            interestingScripts = new HashSet();
            interestingTargets = AntLogger.NO_TARGETS;
            interestingTasks = AntLogger.NO_TASKS;
            interestingLogLevels = new int[0];
            collectLineNumbersForTargets = false;
            targetsStarted = new ArrayList();
            messages = new ArrayList();
        }

        public boolean interestedInAllScripts(AntSession session) {
            return interestedInAllScriptsFlag;
        }

        public String[] interestedInTasks(AntSession session) {
            return interestingTasks;
        }

        public boolean interestedInScript(File script, AntSession session) {
            return interestingScripts.contains(script);
        }

        public String[] interestedInTargets(AntSession session) {
            return interestingTargets;
        }

        public boolean interestedInSession(AntSession session) {
            return interestedInSessionFlag;
        }

        public int[] interestedInLogLevels(AntSession session) {
            return interestingLogLevels;
        }

        public void targetStarted(AntEvent event) {
            int line = event.getLine();
            targetsStarted.add(event.getScriptLocation() +
                (collectLineNumbersForTargets && line != -1 ? ":" + line : "") +
                '#' + event.getTargetName());
        }
        
        public void messageLogged(AntEvent event) {
            String toadd = "" + event.getLogLevel() + ":" + event.getMessage();
            String taskname = event.getTaskName();
            if (taskname != null) {
                toadd = taskname + ":" + toadd;
            }
            messages.add(toadd);
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
