/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * NbExecutor.java
 *
 * Created on March 28, 2001, 6:57 PM
 */

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.*;

import java.util.*;
import java.io.*;
import org.netbeans.xtest.pe.TestRunInfoTask;
import org.netbeans.xtest.pe.xmlbeans.ModuleError;

/**
 *
 * @author  lm97939
 * @version 
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
                                            "scrambler2",
                                            "jdkhome"};

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
              try {  
                Java  callee = (Java) getProject().createTask( "java" );
                
                callee.setOwningTarget(target);
                callee.setTaskName(getTaskName());
                callee.setLocation(location);
                callee.init();

                callee.setClassname("org.apache.tools.ant.Main");
                callee.createClasspath().setPath(project.getProperty("java.class.path"));
                callee.setFork(true);
                callee.setFailonerror(true);
                
                callee.setDir(project.getBaseDir());
                callee.createArg().setLine("-buildfile " + project.getProperty("ant.file"));

                outputfile = getLogFile(test.getModule() + "_" + test.getType());
                if (outputfile != null) {
                    //callee.setOutput(outputfile);
                    callee.createArg().setLine("-logfile " + outputfile);
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
                
                Hashtable ps = project.getProperties();
                Enumeration en = ps.keys();
                while (en.hasMoreElements()) {
                    String name = (String) en.nextElement();
                    String value = (String) ps.get(name);
                    if (name.startsWith("xtest") || name.startsWith("_xtest")) {
                        callee.createArg().setValue("-D" + name + "=" + value);
                    }
                }
                for (int i=0; i<propertiesToPass.length; i++) {
                    String p = project.getProperty(propertiesToPass[i]);
                    if (p != null) {
                        callee.createArg().setValue("-D" + propertiesToPass[i] + "=" + p);
                    }
                }
                
                log("Executing module " + test.getModule() + ", testtype " + test.getType() + " at " + new Date().toLocaleString());
                if (outputfile != null) log("Output is redirected to " + outputfile.getAbsolutePath());
                callee.execute(); 
                log("Executed successfully.");
              }
              catch (BuildException e) {
                  String er = findErrorMessage(outputfile);
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
        if (f == null) return "";
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
        catch (IOException e) { return ""; }
        return buff.toString();
    }
    
    private File getLogFile(String prefix) {
        String testrundir = project.getProperty("xtest.results.testrun.dir");
        if (testrundir == null) return null;
        String dir = testrundir + File.separator + "logs";
        File dirfile = project.resolveFile(dir);
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
        task.setOwningTarget(target);
        task.setTaskName(getTaskName());
        task.setLocation(location);
        task.init();
        
        //TestRunInfoTask  task = new TestRunInfoTask();
        ModuleError moduleError = new ModuleError(module, testtype, logfile==null?null:logfile.getName(), mess);
        task.setOutFile(testrun);
        task.setModuleError(moduleError);
        task.execute();
    }
    
    private void executeStart(MConfig.Setup setup) throws BuildException {
        executeSetup(setup.getName()+"_start", setup.getStartDir(), setup.getStartAntfile(), setup.getStartTarget(), setup.getStartOnBackground(), setup.getStartDelay());
    }

    private void executeStop(MConfig.Setup setup) throws BuildException {
        executeSetup(setup.getName()+"_stop", setup.getStopDir(), setup.getStopAntfile(), setup.getStopTarget(), setup.getStopOnBackground(), setup.getStopDelay());
    }
    
    private void executeSetup(final String name, File dir, String antfile, String targetname, boolean onBackground, int delay) throws BuildException {
        if (antfile == null && targetname == null) return;
        final Ant ant = (Ant) project.createTask("ant");
        ant.setOwningTarget(target);
        ant.setLocation(location);
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
