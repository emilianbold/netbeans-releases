/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2001.
 * All Rights Reserved.
 *
 * Contributor(s): Jayme C. Edwards, Jesse Glick
 */
 
package org.apache.tools.ant.module.run;

import java.io.*;
import java.util.*;
import java.util.Map; // override org.apache.tools.ant.Map

import org.openide.*;
import org.openide.actions.ExecuteAction;
import org.openide.awt.Actions;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.*;
import org.openide.execution.ExecutionEngine;
import org.openide.loaders.DataObject;
import org.openide.util.*;
import org.openide.windows.*;

import org.w3c.dom.Element;

import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.IntrospectedInfo;
import org.apache.tools.ant.module.bridge.AntBridge;

/** Executes an Ant Target asynchronously in the IDE.
 */
public class TargetExecutor implements Runnable {
    
    private AntProjectCookie pcookie;
    private InputOutput io;
    private OutputStream outputStream;
    private boolean ok = false;
    private int verbosity = AntSettings.getDefault ().getVerbosity ();
    private Properties properties = (Properties) AntSettings.getDefault ().getProperties ().clone ();
    private List targetNames;

    /** targets may be null to indicate default target */
    public TargetExecutor (AntProjectCookie pcookie, String[] targets) {
        this.pcookie = pcookie;
        targetNames = ((targets == null) ? null : Arrays.asList (targets));
    }
  
    public void setVerbosity (int v) {
        verbosity = v;
    }
    
    public synchronized void setProperties (Properties p) {
        properties = (Properties) p.clone ();
    }
    
    public synchronized void addProperties (Properties p) {
        if (p.isEmpty ()) return;
        Properties old = properties;
        properties = new Properties ();
        properties.putAll (old);
        properties.putAll (p);
    }
    
    /** If true, switch to the execution workspace when running the target(s).
     * The exact workspace (if any) is that given in the IDE's general settings.
     * By default, false.
     * @since 2.7
     * @see "#17039"
     * @deprecated Meaningless in new window system.
     */
    public void setSwitchWorkspace(boolean sw) {}

    public ExecutorTask execute () throws IOException {
        return execute((String)null);
    }
  
    /** Start it going. */
    ExecutorTask execute (String name) throws IOException {
        //System.err.println("execute #1: " + this);
        if (name == null) {
            
        if (AntSettings.getDefault ().getReuseOutput ()) {
            name = NbBundle.getMessage (TargetExecutor.class, "TITLE_output_reused");
        } else {
            Element projel = pcookie.getProjectElement ();
            String projectName;
            if (projel != null) {
                // remove & if available.
                projectName = Actions.cutAmpersand(projel.getAttribute("name")); // NOI18N
            } else {
                projectName = NbBundle.getMessage (TargetExecutor.class, "LBL_unparseable_proj_name");
            }
            String fileName;
            if (pcookie.getFileObject () != null) {
                fileName = pcookie.getFileObject().getNameExt();
            } else {
                fileName = pcookie.getFile ().getName ();
            }
            if (projectName.equals("")) { // NOI18N
                // No name="..." given, so try the file name instead.
                projectName = fileName;
            }
            if (targetNames != null) {
                StringBuffer targetList = new StringBuffer ();
                Iterator it = targetNames.iterator ();
                if (it.hasNext ()) {
                    targetList.append ((String) it.next ());
                }
                while (it.hasNext ()) {
                    targetList.append (NbBundle.getMessage (TargetExecutor.class, "SEP_output_target"));
                    targetList.append ((String) it.next ());
                }
                name = NbBundle.getMessage (TargetExecutor.class, "TITLE_output_target", projectName, fileName, targetList);
            } else {
                name = NbBundle.getMessage (TargetExecutor.class, "TITLE_output_notarget", projectName, fileName);
            }
        }
        
        }
        final ExecutorTask task;
        synchronized (this) {

            // OutputWindow
            io = IOProvider.getDefault ().getIO (name, false);
            // this will delete the output even if a script is still running.
            io.getOut ().reset ();
            // #16720:
            io.select();
            
            task = ExecutionEngine.getDefault().execute (name, this, InputOutput.NULL);
            //System.err.println("execute #2: " + this);
            //System.err.println("execute #3: " + this);
        }
        //System.err.println("execute #5: " + this);
        WrapperExecutorTask wrapper = new WrapperExecutorTask (task, io);
        RequestProcessor.getDefault().post(wrapper);
        return wrapper;
    }
    
    public ExecutorTask execute(OutputStream outputStream) throws IOException {
        this.outputStream = outputStream;
        ExecutorTask task = ExecutionEngine.getDefault().execute(
            NbBundle.getMessage(TargetExecutor.class, "LABEL_execution_name"), this, InputOutput.NULL);
        return new WrapperExecutorTask(task, null);
    }
    
    private class WrapperExecutorTask extends ExecutorTask {
        private ExecutorTask task;
        private InputOutput inputOutput;
        public WrapperExecutorTask (ExecutorTask task, InputOutput inputOutput) {
            super (new WrapperRunnable (task));
            this.task = task;
            this.inputOutput = inputOutput;
        }
        public void stop () {
            task.stop ();
        }
        public int result () {
            return task.result () + (ok ? 0 : 1);
        }
        public InputOutput getInputOutput () {
            return inputOutput;
        }
    }
    private static class WrapperRunnable implements Runnable {
        private ExecutorTask task;
        public WrapperRunnable (ExecutorTask task) {
            this.task = task;
        }
        public void run () {
            task.waitFinished ();
        }
    }
  
    /** Call execute(), not this method directly!
     */
    synchronized public void run () {
        if (outputStream == null) {
            //System.out.println("run #1: " + this); // NOI18N
            io.setFocusTaken (true);
            io.setErrVisible (false);
            // Generally more annoying than helpful:
            io.setErrSeparated (false);
        }
        
        if (AntSettings.getDefault ().getSaveAll ()) {
            LifecycleManager.getDefault ().saveAll ();
        }
        
        // see the method body for description of this workaround
        regexpLibraryWorkaroundForAnt151();
        
        //PrintStream out = new PrintStream (new OutputWriterOutputStream (io.getOut ()));
        PrintStream err;
        if (outputStream == null) {
            err = new PrintStream (new OutputWriterOutputStream (io.getErr ()));
        } else {
            err = new PrintStream (outputStream);
        }
        PrintStream out = err; // at least for now...
        
        File buildFile = pcookie.getFile ();
        if (buildFile == null) {
            err.println(NbBundle.getMessage(TargetExecutor.class, "EXC_non_local_proj_file"));
            return;
        }
        
        ok = AntBridge.getInterface().run(buildFile, pcookie.getFileObject(), targetNames,
                                          out, err, properties, verbosity, outputStream == null);
    }

    // See #29245 for more details. Relevant only for Ant 1.5.1
    // It checks for presence of misc regexp classes and if some is found
    // and system property "ant.regexp.regexpimpl" is not set then it
    // sets this property. 
    private boolean workarounded = false;
    private void regexpLibraryWorkaroundForAnt151() {
        if (workarounded) {
            return;
        } else {
            workarounded = true;
        }
        if (System.getProperty("ant.regexp.regexpimpl") != null) {
            return;
        }
        
        // We use JDK 1.4 -> this will be available.
        System.setProperty("ant.regexp.regexpimpl", "org.apache.tools.ant.util.regexp.Jdk14RegexpRegexp");
   }
    
}
