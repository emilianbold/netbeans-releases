/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jayme C. Edwards, Jesse Glick
 */
 
package org.apache.tools.ant.module.run;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Map; // override org.apache.tools.ant.Map

import org.openide.*;
import org.openide.execution.ExecutorTask;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

import org.w3c.dom.Element;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.Taskdef;

import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.DefinitionRegistry;
import org.apache.tools.ant.module.api.IntrospectedInfo;

/** Executes an Ant Target asynchronously in the IDE.
 */
public class TargetExecutor implements Runnable {
    
    private AntProjectCookie pcookie;
    private InputOutput io;
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
  
    /** Start it going. */
    public ExecutorTask execute () throws IOException {
        //System.err.println("execute #1: " + this);
        Element projel = pcookie.getProjectElement ();
        String projectName;
        if (projel != null) {
            projectName = projel.getAttribute ("name"); // NOI18N
        } else {
            projectName = NbBundle.getMessage (TargetExecutor.class, "LBL_unparseable_proj_name");
        }
        String fileName;
        if (pcookie.getFileObject () != null) {
            fileName = DataObject.find (pcookie.getFileObject ()).getNodeDelegate ().getDisplayName ();
        } else {
            fileName = pcookie.getFile ().getName ();
        }
        String name;
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
        // remove & if available.
        name = org.openide.awt.Actions.cutAmpersand (name);

        final ExecutorTask task;
        synchronized (this) {
            // OutputWindow
            if (AntSettings.getDefault ().getReuseOutput ()) {
                io = TopManager.getDefault ().getIO (NbBundle.getMessage (TargetExecutor.class, "TITLE_output_reused"), false);
            } else {
                io = TopManager.getDefault ().getIO (name, false);
            }
            // this will delete the output even if a script is still running.
            io.getOut ().reset ();
                
            // [PENDING] note that calls to System.exit() from tasks
            // are apparently not trapped! (#9953)
            task = TopManager.getDefault ().getExecutionEngine ().execute (name, this, InputOutput.NULL);
            //System.err.println("execute #2: " + this);
            //System.err.println("execute #3: " + this);
        }
        //System.err.println("execute #5: " + this);
        return new WrapperExecutorTask (task);
    }
    private class WrapperExecutorTask extends ExecutorTask {
        private ExecutorTask task;
        public WrapperExecutorTask (ExecutorTask task) {
            super (new WrapperRunnable (task));
            this.task = task;
        }
        public void stop () {
            task.stop ();
        }
        public int result () {
            return task.result () + (ok ? 0 : 1);
        }
        public InputOutput getInputOutput () {
            return io;
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
        //System.out.println("run #1: " + this); // NOI18N
        io.setFocusTaken (true);
        io.setErrVisible (false);
        // Generally more annoying than helpful:
        io.setErrSeparated (false);
        
        if (AntSettings.getDefault ().getSaveAll ()) {
            TopManager.getDefault ().saveAll ();
        }
        
        //System.out.println("run #2: " + this); // NOI18N
        Project project = null;

        //PrintStream out = new PrintStream (new OutputWriterOutputStream (io.getOut ()));
        PrintStream err = new PrintStream (new OutputWriterOutputStream (io.getErr ()));
        PrintStream out = err; // at least for now...
    
        // first use the ProjectHelper to create the project object
        // from the given build file.
        BuildLogger logger;
        try {
            //writer.println("#1"); // NOI18N
            File buildFile = pcookie.getFile ();
            if (buildFile == null) {
                throw new BuildException (NbBundle.getMessage (TargetExecutor.class, "EXC_non_local_proj_file"));
            }
            project = new Project ();
            // If ClassCastException is thrown from the following
            // line, it is probably a symptom of a core bug (#10260 I
            // think, or #11920). But this should no longer happen
            // with Ant 1.4 which itself works around such problems.
            project.init ();
            Iterator defs = DefinitionRegistry.getDefs ("task").entrySet ().iterator (); // NOI18N
            while (defs.hasNext ()) {
                Map.Entry entry = (Map.Entry) defs.next ();
                project.addTaskDefinition ((String) entry.getKey (), (Class) entry.getValue ());
            }
            defs = DefinitionRegistry.getDefs ("type").entrySet ().iterator (); // NOI18N
            while (defs.hasNext ()) {
                Map.Entry entry = (Map.Entry) defs.next ();
                project.addDataTypeDefinition ((String) entry.getKey (), (Class) entry.getValue ());
            }
            project.setUserProperty("ant.file", buildFile.getAbsolutePath()); // NOI18N
            // XXX set also ant.version acc. to org/apache/tools/ant/version.properties?
            Iterator it = properties.entrySet ().iterator ();
            while (it.hasNext ()) {
                Map.Entry entry = (Map.Entry) it.next ();
                project.setUserProperty ((String) entry.getKey (), (String) entry.getValue ());
            }
            logger = new NetBeansLogger ();
            logger.setMessageOutputLevel (verbosity);
            logger.setOutputPrintStream (out);
            logger.setErrorPrintStream (err);
            //writer.println("#2"); // NOI18N
            project.addBuildListener (logger);
            ProjectHelper.configureProject(project, buildFile);
            //writer.println("#3"); // NOI18N
        }
        catch (BuildException be) {
            // Write errors to the output window, since 
            // alot of errors could be annoying as dialogs
            if (verbosity >= Project.MSG_VERBOSE) {
                be.printStackTrace (err);
            } else {
                err.println (be);
            }
            return;
        }

        // Interesting fact: Project.build{Started,Finished} is protected!
        // So it must be fired directly on the listener. Poor API design IMHO.
        logger.buildStarted (new BuildEvent (project));
        // Save & restore system output streams.
        PrintStream sysout = System.out;
        PrintStream syserr = System.err;
        System.setOut(new PrintStream(new DemuxOutputStream(project, false)));
        System.setErr(new PrintStream(new DemuxOutputStream(project, true)));
        try {
            // Execute the configured project
            //writer.println("#4"); // NOI18N
            Vector targs;
            if (targetNames != null) {
                targs = new Vector (targetNames);
            } else {
                targs = new Vector (1);
                targs.add (project.getDefaultTarget ());
            }
            project.executeTargets (targs);
            //writer.println("#5"); // NOI18N
            logger.buildFinished (new BuildEvent (project));
            ok = true;
        } catch (ThreadDeath td) {
            TopManager.getDefault ().setStatusText (NbBundle.getMessage (TargetExecutor.class, "MSG_target_failed_status"));
            // don't throw ThreadDeath, just return. ThreadDeath sometimes 
            // generated when killing process in Execution Window
            //throw td;
            return;
        } catch (Throwable t) {
            BuildEvent ev = new BuildEvent (project);
            ev.setException (t);
            logger.buildFinished (ev);
        } finally {
            System.setOut(sysout);
            System.setErr(syserr);
        }

        // Now check to see if the Project defined any cool new custom tasks.
        final Project p2 = project;
        RequestProcessor.postRequest (new Runnable () {
                public void run () {
                    IntrospectedInfo custom = AntSettings.getDefault ().getCustomDefs ();
                    custom.scanProject (p2);
                }
            }, 1000); // a bit later; the target can finish first!
    }

}
