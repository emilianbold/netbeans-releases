/*
 *
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

package org.netbeans.xtest.plugin.ide;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Environment;
import org.netbeans.xtest.testrunner.JUnitTestRunner;
import org.netbeans.xtest.testrunner.JUnitTestRunnerProperties;
import org.netbeans.xtest.testrunner.TestBoardLauncher;
import org.netbeans.xtest.testrunner.TestBoardLauncherException;
import org.netbeans.xtest.testrunner.TestRunnerHarness;

/**
 * @author mb115822
 */
public class IdeTestRunnerTask extends ExecTask implements TestBoardLauncher {
    
    // workdir where the runner can 
    private File workDir;    
    
    // test mode (e.g. tastbag, testsuite ....)
    private String testMode = TestRunnerHarness.TESTRUN_MODE_TESTBAG;
    //private String testMode = TestRunnerHarness.TESTRUN_MODE_TESTSUITE;
    // testlist filename
    private static final String TESTLIST_FILENAME = "testrunner.testlist";
    
    
protected ExecuteWatchdog watchdog;
    
    protected Long ideTimeout = null;
    
    protected String ideUserdir = null;
    
    // ide should be started with -ea switch
    protected boolean enableAssertions = true;
    
    // debug should suspend code execution
    protected boolean debugSuspend = false;
    
    // debug port to which debugger is connected. When 0 - debugging is not started
    protected int debugPort = 0;    
    
    // system properties arraylist 
    private ArrayList sysProperties = new ArrayList();
    
    /**
     * Create IdeWatchdog to kill a IDE     
     */
    /* #70954 - Used IdeWatchdog run in separate VM instead of this ant's watchdog.
     * If IdeWatchdog is OK, this method and IdeExecWatchdog can be removed.
    protected ExecuteWatchdog createWatchdog() throws BuildException {
        if ( ideTimeout == null)  return null;
        IdeExecWatchdog ideWatchdog = new IdeExecWatchdog(ideTimeout.longValue(), getProject());
        ideWatchdog.setIdeUserdir(ideUserdir);
        watchdog =  ideWatchdog;        
        return watchdog;
    }
     */
    
    /**
    * Timeout in milliseconds after which the process will be killed.
     * this method is redefined, so I can remember the timeout value
    */
    public void setTimeout(Long value) {
        if (value.longValue() == 0) return;
        // add 5 minutes to the hard limit (300000 miliseconds)
        ideTimeout = new Long(value.longValue() + 300000);
        super.setTimeout(ideTimeout);       
    }    
    
    /**
     * set ide userdir - we need this property to know where to
     * store screen dump when ide is forcibly killed
     */
    public void setIdeUserdir(String userdir) {
       this.ideUserdir = userdir;
    }
    
    /**
     * when set to true, -ea switch is used to run the IDE
     */
    public void setEnableAssertions(boolean enableAssertions) {
        this.enableAssertions = enableAssertions;
    }
    
    /**
     * when port is set > 0, IDE is started in debugging mode
     */
    public void setDebugPort(int port) {
        this.debugPort=port;
    }
    
    /**
     * suspend switch of debug
     */
    public void setDebugSuspend(boolean suspend) {
        this.debugSuspend = suspend;
    }        
    
    /**
     * add additional arguments to command line, e.g. -ea, debug ...
     */     
    protected String prepareCommandLineArguments() {
        StringBuffer arg = new StringBuffer();
        if (enableAssertions) {
            arg.append(" -J-ea");
        }
        if (debugPort > 0) {
            String suspendArg = debugSuspend ? "y" : "n";
            arg.append(" -J-Xdebug -J-Xnoagent -J-Xrunjdwp:transport=dt_socket,server=y,suspend="+suspendArg+",address="+debugPort);
        }
        // add userdata.properties to system properties
        Iterator i  = sysProperties.iterator();
        while (i.hasNext()) {
            Environment.Variable var = (Environment.Variable)i.next();
            arg.append(" \"-J-D"+var.getKey()+"="+var.getValue()+"\"");
        }
        return arg.toString();
    }    
    
    // work directory
    public void setWorkDir(File workDir) {
        log("Setting workDir to "+workDir, Project.MSG_DEBUG);
        this.workDir = workDir;
    }    
    
    // test mode
    public void setTestMode(String testMode) {
        if ((testMode != null) & (!testMode.equals(""))) {
            log("Setting test modes to "+testMode, Project.MSG_DEBUG);
            this.testMode = testMode;
        } else {
            log("Leaving default setting.", Project.MSG_DEBUG);
        }
    }
    
    // sys property file
    /**
     * Add a nested syspropertyfile element. This might be useful to tranfer
     * Ant properties from file to the testcases.
     */
    public void addConfiguredSysPropertyFile(FileVariable  fileVariable) throws IOException {
        log("Adding sys property file "+fileVariable.file,Project.MSG_DEBUG);
        Properties props = new Properties();
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileVariable.file));
        props.load(bis);
        bis.close();
        Iterator iter = props.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            Environment.Variable var = new Environment.Variable();
            var.setKey((String)entry.getKey());
            var.setValue((String)entry.getValue());
            sysProperties.add(var);    
        }
    }
        
    
    
    public void execute() throws BuildException {
        if (isValidOs()) {
            try {
                log("IDE Plugin: Running JUnit in IDE.",Project.MSG_INFO);
                // check validity of input values
                checkInputValuesValidity();                
                // log input Values
                logInputValues(Project.MSG_VERBOSE);
                // execute test run (this depends on testmode)
                TestRunnerHarness testRunnerHarness = new TestRunnerHarness(this, workDir, testMode);
                testRunnerHarness.runTests();
            } catch (IOException ioe) {
                throw new BuildException(ioe.getMessage()==null?"IOException caught":ioe.getMessage(), ioe);
            }
        } else {
            log("This is not valid OS for running this task", Project.MSG_VERBOSE);
        }
    }
    
    
    public void launchTestBoard(JUnitTestRunnerProperties testsToBeExecuted) throws TestBoardLauncherException {
        File runnerPropertiesFile = new File(workDir,TESTLIST_FILENAME);
        try {
            // save runner properties to a file
            testsToBeExecuted.save(runnerPropertiesFile);
            // add runnerPropertiesFile to sys properties
            // need to be definitely fixed
            Commandline.Argument arg = this.createArg();
            arg.setValue("-J-D"+JUnitTestRunner.TESTRUNNER_PROPERTIES_FILENAME_KEY+"="+runnerPropertiesFile.getAbsolutePath());
            arg = this.createArg();
            arg.setLine(prepareCommandLineArguments());
            //
            executeIDE();
        } catch (IOException ioe) {
            throw new TestBoardLauncherException("During launching tests caught IOException"+ioe.getMessage(),ioe);
        } finally {
            // delete runner properties
            runnerPropertiesFile.delete();
        }        
    }
    
    
    // private stuff
    //
    protected void checkInputValuesValidity() throws BuildException {
        // workDir
        if (workDir == null) {
            throw new BuildException("WorkDir is not set");
        }
    }
    
    // verbose output of execute (overview of task arguments)
    protected void logInputValues(int logLevel) {
        // verbose logs        
        log("IdeTestRunner Input values:",logLevel);
        log("  timeout = "+ideTimeout,logLevel);        
        log("Using work dir: "+workDir.getPath(),logLevel);
        // sys properties
        Iterator i  = sysProperties.iterator();
        while (i.hasNext()) {
            Environment.Variable var = (Environment.Variable)i.next();
            log("Using system property (key=value): "+var.getKey()+"="+var.getValue(),logLevel);
        } 
        // test mode
        if (testMode != null) {
            log("Using test mode: "+testMode,logLevel);
        }
    }
    
    // execute ide
    private void executeIDE() throws BuildException {
        log("Running IDE");        
        //        
        log("Executing: "+arrayToString(this.cmdl.getCommandline()),Project.MSG_INFO);        
        // execute
        super.execute();
    }

    public static String arrayToString(String[] array) {        
        if (array != null) {
            StringBuffer buf = new StringBuffer();
            for (int i=0; i < array.length; i++) {
                buf.append(array[i]);
                buf.append(' ');
            }
            return buf.toString();
        } else {
            return null;
        } 
    }
    
    /////////////////////////
    ///////// inner classes
    /////////////////////////
    
    /**
     * Nested 'syspropertyfile' element. It has only one attribute, file.
     */
    public static class FileVariable  {
        private File file;
          
        public void setFile(java.io.File file) {
            this.file = file;
        }
    }
}
