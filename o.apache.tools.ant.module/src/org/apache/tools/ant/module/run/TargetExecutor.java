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

import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetCookie;
import org.apache.tools.ant.module.api.DefinitionRegistry;
import org.apache.tools.ant.module.api.IntrospectedInfo;

/** Executes an Ant Target asynchronously in the IDE.
 */
public class TargetExecutor implements Runnable {

    private AntTargetCookie cookie;
    private InputOutput io;
    private boolean ok = false;
    private int verbosity = AntSettings.getDefault ().getVerbosity ();
    private Properties properties = AntSettings.getDefault ().getProperties ();
    private List targetNames;
  
    public TargetExecutor (AntTargetCookie cookie) throws IOException {
        this.cookie = cookie;
        Element el = cookie.getTargetElement ();
        if (el == null) throw new IOException ("No <target> found"); // NOI18N
        String targetName = el.getAttribute ("name"); // NOI18N
        if (targetName.length () == 0) throw new IOException ("<target> had no name"); // NOI18N
        targetNames = Collections.singletonList (targetName);
    }
    
    public void setVerbosity (int v) {
        verbosity = v;
    }
    
    public synchronized void setProperties (Properties p) {
        properties = p;
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
        AntProjectCookie projcookie = cookie.getProjectCookie ();
        Throwable misparse = projcookie.getParseException ();
        if (misparse != null) {
            IOException ioe = new IOException ();
            TopManager.getDefault ().getErrorManager ().annotate (ioe, misparse);
            throw ioe;
        }
        String projectName = projcookie.getProjectElement ().getAttribute ("name"); // NOI18N
        String fileName;
        if (projcookie.getFileObject () != null) {
            fileName = DataObject.find (projcookie.getFileObject ()).getNodeDelegate ().getDisplayName ();
        } else {
            fileName = projcookie.getFile ().getName ();
        }
        /*
        String name;
        if (target != null) {
            name = NbBundle.getMessage (TargetExecutor.class, "TITLE_output_target", projectName, fileName, target);
        } else {
            name = NbBundle.getMessage (TargetExecutor.class, "TITLE_output_notarget", projectName, fileName);
        }
         */
        String name = NbBundle.getMessage (TargetExecutor.class, "TITLE_output_target",
                                           projectName, fileName, cookie.getTargetElement ().getAttribute ("name")); // NOI18N
        final ExecutorTask task;
        synchronized (this) {
            // Note that this redirects stdout/stderr from
            // Ant. This ought not be necessary, as the logger also
            // redirects to the Output Window, but <echo> in Ant 1.2
            // prints to stdout. (Subsequently fixed.)
            // [PENDING] note that calls to System.exit() from tasks
            // are apparently not trapped!
            task = TopManager.getDefault ().getExecutionEngine ().execute (name, this, null);
            //System.err.println("execute #2: " + this);
            io = task.getInputOutput ();
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
        final Project project = new Project ();

        //PrintStream out = new PrintStream (new OutputWriterOutputStream (io.getOut ()));
        PrintStream err = new PrintStream (new OutputWriterOutputStream (io.getErr ()));
        PrintStream out = err; // at least for now...
    
        // first use the ProjectHelper to create the project object
        // from the given build file.
        BuildLogger logger;
        try {
            //writer.println("#1"); // NOI18N
            File buildFile = cookie.getProjectCookie ().getFile ();
            if (buildFile == null) throw new IllegalArgumentException ();
            project.init();
            Iterator defs = DefinitionRegistry.getDefs (true).entrySet ().iterator ();
            while (defs.hasNext ()) {
                Map.Entry entry = (Map.Entry) defs.next ();
                project.addTaskDefinition ((String) entry.getKey (), (Class) entry.getValue ());
            }
            defs = DefinitionRegistry.getDefs (false).entrySet ().iterator ();
            while (defs.hasNext ()) {
                Map.Entry entry = (Map.Entry) defs.next ();
                project.addDataTypeDefinition ((String) entry.getKey (), (Class) entry.getValue ());
            }
            project.setUserProperty("ant.file", buildFile.getAbsolutePath()); // NOI18N
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
        try {
            // Execute the configured project
            //writer.println("#4"); // NOI18N
            project.executeTargets (new Vector (targetNames));
            //writer.println("#5"); // NOI18N
            logger.buildFinished (new BuildEvent (project));
            ok = true;
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            BuildEvent ev = new BuildEvent (project);
            ev.setException (t);
            logger.buildFinished (ev);
        }

        // Now check to see if the Project defined any cool new custom tasks.
        RequestProcessor.postRequest (new Runnable () {
                public void run () {
                    IntrospectedInfo custom = AntSettings.getDefault ().getCustomDefs ();
                    custom.scanProject (project);
                }
            }, 1000); // a bit later; the target can finish first!
    }

}
