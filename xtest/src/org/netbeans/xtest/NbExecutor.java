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

package org.netbeans.xtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Java;
import org.netbeans.xtest.pe.GetResultsDirsTask;
import org.netbeans.xtest.pe.PEConstants;
import org.netbeans.xtest.pe.TestRunInfoTask;
import org.netbeans.xtest.pe.TransformXMLTask;
import org.netbeans.xtest.pe.xmlbeans.ModuleError;
import org.netbeans.xtest.pe.xmlbeans.TestBag;
import org.netbeans.xtest.pe.xmlbeans.UnitTestCase;
import org.netbeans.xtest.pe.xmlbeans.UnitTestSuite;

/**
 * NbExecutor is a task which reads config from master-config and executes
 * all tests in a loop.
 *
 * @author  lm97939
 */
public class NbExecutor extends Task {
    
    String targetName = null;
    String targetParamModule   = null;
    String targetParamTestType = null;
    String targetParamTestAttributes = null;
    String mode = null;
    File   testrun;
    
    private static final String[] propertiesToPass = {
                                            "netbeans.dest.dir",
                                            "netbeans.home",
                                            "build.sysclasspath",
                                            "ant.home",
                                            "running.mode",
                                            "basedir",
                                            "user.dir",
                                            "jdkhome",
                                            "jemmy.home",
                                            "jellytools.home",
                                            "harness.dir"};

    public void setTargetName(String name) {
        this.targetName = name;
    }
    
    public void setTargetParamModule(String param) {
        this.targetParamModule = param;
    }
    
    public void setTargetParamTesttype(String param) {
        this.targetParamTestType = param;
    }

    public void setTargetParamTestAttributes(String param) {
        this.targetParamTestAttributes = param;
    }
    
    public void setRunningMode(String mode) {
        this.mode = mode;
    }
    
    public void setTestRun(File testrun) {
        if (testrun.getName().startsWith("${"))
            return;
        this.testrun = testrun;
    }
    
    public void execute () throws BuildException {
        if (null == targetName || 0 == targetName.length())
            throw new BuildException("Attribute 'targetname' has to be set.");
        if (null == targetParamModule || 0 == targetParamModule.length())
            throw new BuildException("Attribute 'targetParamModule' has to be set.");
        if (null == targetParamTestType || 0 == targetParamTestType.length())
            throw new BuildException("Attribute 'targetParamTestType' has to be set.");
        
        MConfig cfg = NbTestConfig.getMConfig();                
        
        if (null == cfg)
            throw new BuildException("XTest configuration wasn't chosen, use call xtestconfig task first.", getLocation());        
        
        
        MConfig.Setup setup = cfg.getConfigSetup();
        if (mode.equalsIgnoreCase("run") && setup != null) executeStart(setup);

        Enumeration all_tests = cfg.getAllTests();
        while(all_tests.hasMoreElements()) {
          try {
            MConfig.TestGroup test_group = (MConfig.TestGroup)all_tests.nextElement();
            
            MConfig.Setup msetup = test_group.getSetup();
            if (mode.equalsIgnoreCase("run") && msetup != null) executeStart(msetup);
            
            Hashtable props = test_group.getProperties();
            
            Enumeration tests = test_group.getTests();

            while(tests.hasMoreElements()) {
              MConfig.Test test = (MConfig.Test) tests.nextElement();
              File outputfile = null;
              String debug_level = "";
              try {  
                Java  callee = new Java(); // do not use Project.createTask: CCE with presetdefs
                callee.setProject(getProject());
                
                callee.setOwningTarget(getOwningTarget());
                callee.setTaskName(getTaskName());
                callee.setLocation(getLocation());
                callee.init();

                callee.setClassname("org.apache.tools.ant.Main");
                callee.createClasspath().setPath(getProject().getProperty("java.class.path"));
                callee.setFork(true);
                callee.setFailonerror(true);
                
                callee.setDir(getProject().getBaseDir());
                callee.createArg().setLine("-buildfile " + "\"" + getProject().getProperty("ant.file") + "\"");

                outputfile = getLogFile(test.getModule() + "_" + test.getType());
                if (outputfile != null) {
                    //callee.setOutput(outputfile);
                    callee.createArg().setLine("-logfile " + "\"" + outputfile + "\"");
                }
                
                debug_level = getProject().getProperty("xtest.debug.level");
                if (debug_level != null) {
                    if (debug_level.equals("debug") || debug_level.equals("verbose")) {
                        callee.createArg().setValue("-" + debug_level);
                    }
                }

                callee.createArg().setValue(targetName);

                callee.createArg().setValue("-D"  +targetParamModule + "=" + test.getModule());
                callee.createArg().setValue("-D"  +targetParamTestType + "=" + test.getType());
                callee.createArg().setValue("-D"  +targetParamTestAttributes + "=" + test.getAttributesAsString());

                Set set = props.entrySet();
                Iterator it = set.iterator();
                while (it.hasNext()) {
                    Map.Entry map = (Map.Entry) it.next();
                    callee.createArg().setValue("-D" + (String)map.getKey() + "=" + (String)map.getValue());
                }
                
                Hashtable ps = getProject().getProperties();
                Enumeration en = ps.keys();
                while (en.hasMoreElements()) {
                    String name = (String) en.nextElement();
                    String value = (String) ps.get(name);
                    if (name.startsWith("xtest") || name.startsWith("_xtest")) {
                        callee.createArg().setValue("-D" + name + "=" + value);
                    }
                }
                for (int i=0; i<propertiesToPass.length; i++) {
                    String p = getProject().getProperty(propertiesToPass[i]);
                    if (p != null) {
                        callee.createArg().setValue("-D" + propertiesToPass[i] + "=" + p);
                    }
                }
                
                log("Executing module " + test.getModule() + ", testtype " + test.getType() + " at " + new Date().toString());
                if (outputfile != null) log("Output is redirected to " + outputfile.getAbsolutePath());
                callee.execute(); 
                log("Executed successfully.");
              }
              catch (BuildException e) {
                  String er = findErrorMessage(outputfile);
                  if (er == null) {
                      er = e.toString();
                  }
                  logError(test.getModule(), test.getType(), outputfile, er);
              }
            }
            if (mode.equalsIgnoreCase("run") && msetup != null) executeStop(msetup);
          }
          catch (BuildException e) {
              log("Exception during executiong test:\n"+e.toString(),Project.MSG_ERR);
              logError("unknown", null, null, e.toString());
          }
        } 
        if (mode.equalsIgnoreCase("run") && setup != null) executeStop(setup);
    } 
    
    private String findErrorMessage(File f) {
        if (f == null) return null;
        StringBuffer buff = new StringBuffer();
        try { 
            BufferedReader r = new BufferedReader(new FileReader(f));
            String line = r.readLine();
            while (line != null && !line.trim().equals("BUILD FAILED") ) 
                line = r.readLine();
            if (line == null || !line.trim().equals("BUILD FAILED"))
                return "";
            while ((line = r.readLine()) != null && !line.startsWith("Total time")) {
                if (!line.trim().equals("")) 
                    buff.append(line+"\n");
            }
            r.close();
        }
        catch (IOException e) { return null; }
        return buff.toString();
    }
    
    private File getLogFile(String prefix) {
        String testrundir = getProject().getProperty("xtest.results.testrun.dir");
        if (testrundir == null) return null;
        String dir = testrundir + File.separator + "logs";
        File dirfile = getProject().resolveFile(dir);
        if (!dirfile.exists()) dirfile.mkdirs();
        String new_prefix = prefix.replace('/','_');
        File file = new File(dirfile, new_prefix + ".log");
        int c = 1;
        while (file.exists()) {
            file = new File(dirfile, new_prefix + "_"+ c + ".log");
            c++;
        }
        return file;
    }
    
    private void logError(String module, String testtype, File logfile, String mess) {
        log("ERROR when executing module "+module+", testtype "+testtype+". "+(logfile == null ? "" : "Details in " + logfile.getAbsolutePath()+".") + "\nError message: " +  mess,Project.MSG_ERR);
       
        if (testrun == null)
            return;
        TestRunInfoTask task = (TestRunInfoTask) getProject().createTask( "testruninfo" );
        task.setOwningTarget(getOwningTarget());
        task.setTaskName(getTaskName());
        task.setLocation(getLocation());
        task.init();
        
        //TestRunInfoTask  task = new TestRunInfoTask();
        ModuleError moduleError = new ModuleError(module, testtype, logfile==null?null:logfile.getName(), mess);
        task.setOutFile(testrun);
        task.setModuleError(moduleError);
        task.execute();

        try {
            addErrorTestBag(module, testtype, logfile, mess);
        } catch (Exception e) {
            log("NbExecutor#addErrorTestBag: "+e.getMessage());
            e.printStackTrace();
        }
    }

    /** Create a new test bag results to report unexpected critical error.
     * In most case it is a compilation error but it can be something different.
     * This test bag contains one suite with one test case. It satisfies that
     * critical error appears in summary results.
     */
    private void addErrorTestBag(String module, String testtype, File logfile, String message) throws Exception {
        log("\n\n================== addErrorTestBag ========================", Project.MSG_VERBOSE);
        File resultsDir = new File(getProject().getProperty("xtest.results.testrun.dir"));
        log("xtest.results.testrun.dir="+resultsDir, Project.MSG_VERBOSE);
        // create new testbag dir in results (e.g. testbagDir=results\testrun_060331-120201\testbag_2)
        GetResultsDirsTask grdt = new GetResultsDirsTask();
        grdt.setProject(getProject());
        // property need to be unique. It is used just only inside GetResultsDirsTask.
        grdt.setTestBagDirProperty("dummy.xtest.results.testbag.dir"+System.currentTimeMillis());
        grdt.setTestRunDirProperty("xtest.results.testrun.dir");
        File testbagDir = grdt.createAndSetTestBag();
        log("testbagDir="+testbagDir, Project.MSG_VERBOSE);

        String label = "Critical Error";

        // create test case bean
        UnitTestCase testCaseBean = new UnitTestCase();
        testCaseBean.xmlat_name = label;
        testCaseBean.xmlat_class = label;
        testCaseBean.xmlat_result = UnitTestCase.TEST_ERROR;
        testCaseBean.xmlat_message = "Compilation failed or other critical error appeared. Look at build script log for details.";
        testCaseBean.xml_cdata = message;

        // create test suite bean
        String suiteName = label;
        UnitTestSuite testSuiteBean = new UnitTestSuite();
        testSuiteBean.xmlat_name = suiteName;
        testSuiteBean.xmlat_time = 0;
        testSuiteBean.xmlat_testsTotal = 1;
        testSuiteBean.xmlat_testsPass = 0;
        testSuiteBean.xmlat_testsFail = 0;
        testSuiteBean.xmlat_testsError = 1;
        testSuiteBean.xmlat_testsUnexpectedPass = 0;
        testSuiteBean.xmlat_testsExpectedFail = 0;
        testSuiteBean.xmlat_timeStamp = new java.sql.Timestamp(System.currentTimeMillis());
        testSuiteBean.xmlel_UnitTestCase = new UnitTestCase[] {testCaseBean};

        // create test bag bean
        TestBag testBagBean = new TestBag();
        testBagBean.setModule(module);
        testBagBean.setName(label);
        testBagBean.setTestType(testtype);
        testBagBean.xmlel_UnitTestSuite = new UnitTestSuite[] {testSuiteBean};


        // create results dir for suite
        File suiteResultsDir = new File(testbagDir, PEConstants.XMLRESULTS_DIR+File.separator+"suites");
        if(!suiteResultsDir.mkdir()) {
            log("NbExecutor#addErrorTestBag: Cannot create directory "+suiteResultsDir);
            return;
        }
        File suiteFile = new File(suiteResultsDir, "TEST-"+suiteName+".xml");
        // save created suite to testbag_ID/xmlresults/suites/TEST-Critical Error.xml
        testSuiteBean.saveXMLBean(suiteFile);

        File testbagFile = new File(testbagDir, PEConstants.XMLRESULTS_DIR+File.separator+PEConstants.TESTBAG_XML_FILE);
        testBagBean.saveXMLBean(testbagFile);

        // regenerated testbag to include created suite
        // (Not needed if beans are properly created)
        // RegenerateXMLTask.regenerateTestBag(testbagDir, true, false);
        // transform testbag from XML to HTML
        System.setProperty("xtest.home", getProject().getProperty("xtest.home"));
        TransformXMLTask.transformResults(testbagDir, testbagDir);
        log("================== addErrorTestBag ========================\n\n", Project.MSG_VERBOSE);
    }

    private void executeStart(MConfig.Setup setup) throws BuildException {
        executeSetup(setup.getName()+"_start", setup.getStartDir(), setup.getStartAntfile(), setup.getStartTarget(), setup.getStartOnBackground(), setup.getStartDelay());
    }

    private void executeStop(MConfig.Setup setup) throws BuildException {
        executeSetup(setup.getName()+"_stop", setup.getStopDir(), setup.getStopAntfile(), setup.getStopTarget(), setup.getStopOnBackground(), setup.getStopDelay());
    }
    
    private void executeSetup(final String name, File dir, String antfile, String targetname, boolean onBackground, int delay) throws BuildException {
        if (antfile == null && targetname == null) return;
        final Ant ant = (Ant) getProject().createTask("ant");
        ant.setOwningTarget(getOwningTarget());
        ant.setLocation(getLocation());
        ant.init();
        ant.setDir(dir);
        ant.setAntfile(antfile);
        ant.setTarget(targetname);
        final File outputfile = getLogFile(name);
        if (outputfile != null)
            ant.setOutput(outputfile.getAbsolutePath());
        if (onBackground) {
           Thread thread = new Thread() {
               public void run() {
                 try {
                   ant.execute();
                 }
                 catch (BuildException e) {
                     log("Exception during executiong setup:\n"+e.toString(),Project.MSG_ERR);
                     logError("setup: "+name, null, outputfile, e.toString());
                 }
               }
           };
           thread.start();
           if (delay != 0) {
               try { Thread.currentThread().sleep(delay); }
               catch (InterruptedException e) { throw new BuildException(e);}
           }
        }
        else {
            try {
               ant.execute();
            }              
            catch (BuildException e) {
                log("Exception during executiong setup:\n"+e.toString(),Project.MSG_ERR);
                logError("setup: "+name, null, outputfile, e.toString());
            }
        }
    }
        
}
