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

package org.netbeans.nbbuild;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;

/** Runs a clean task (for example) in all submodules.
 *
 * @author Jesse Glick
 */
public class CleanAll extends Task {
    
    private Vector modules = new Vector (); // Vector<String>
    private Vector failedmodules = new Vector (); // Vector of failed modules
    private String targetname = "clean";
    private File topdir = null;
    private File [] topdirs = null;
    private boolean resolvedependencies = false; // resolve compile-time dependencies for clean
    private String deptargetprefix = "";  // target prefix for resolving dependencies
    private Hashtable targets;
    private boolean failonerror = true; // fail if particular module build failed?
    
    /** Comma-separated list of modules to include. */
    public void setModules (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        modules = new Vector ();
        while (tok.hasMoreTokens ())
            modules.addElement (tok.nextToken ());
    }
    
    /** Name of the target to run in each module's build script. */ 
    public void setTargetname (String s) {
        targetname = s;
    }
    
    /** Prefix of compile targets in current build script for 
      * each module.
      */ 
    public void setDepTargetPrefix (String s) {
        deptargetprefix = s;
    }
    
    /** Enable/Disable resolving compile-time dependencies. */
    public void setResolveDependencies (boolean b) {
        resolvedependencies = b;
    }
    
    /** Enable/Disable BUILD FAILED, when particular module's 
      * build failed.
      */
    public void setFailOnError (boolean b) {
        failonerror = b;
    }
    
    /** The top directory containing these modules as subdirectories. */
    public void setTopdir (File f) {
        topdir = f;
    }
    
    public void setTopdirs (String str) {        
        StringTokenizer st = new StringTokenizer(str, ",");
        int count = st.countTokens();
        topdirs = new File [count];
        for (int i = 0; i < count; i++) {
            topdirs[i] = new File (st.nextToken().trim());
        }
    }
 
    /** Resolve compile-time dependencies and use them for cleaning */
    private void resolveDependencies () throws BuildException {
        Target dummy = new Target ();
        String dummyName = "nbmerge-" + getOwningTarget().getName();
        targets = getProject().getTargets();
        while (targets.contains (dummyName))
            dummyName += "-x";
        dummy.setName (dummyName);
        for (int i = 0; i < modules.size (); i++) {
            String module = (String) modules.elementAt (i);
            dummy.addDependency (deptargetprefix + module);
        }
        getProject().addTarget(dummy);
        Vector fullList = getProject().topoSort(dummyName, targets);
        // Now remove earlier ones: already done.
        Vector doneList = getProject().topoSort(getOwningTarget().getName(), targets);
        List todo = new ArrayList(fullList.subList(0, fullList.indexOf(dummy)));
        todo.removeAll(doneList.subList(0, doneList.indexOf(getOwningTarget())));

        Iterator targit = todo.iterator();
        while (targit.hasNext()) {
            String _targetname = ((Target) targit.next()).getName();
            if (_targetname.startsWith(deptargetprefix)) {
                String module = _targetname.substring(deptargetprefix.length());
                if (modules.indexOf(module) < 0) {
                    modules.addElement(module);
                    log("Adding dependency module \"" + module + "\" to the list of modules for cleaning", Project.MSG_VERBOSE);
                }
            }
        }
    }
    
    public void execute () throws BuildException {
        
        if (topdirs == null && topdir != null) {
            topdirs = new File[1];
            topdirs[0] = topdir; 
        }
        
        if (topdir == null && topdirs == null) {
            throw new BuildException("You must set at least one topdir attribute", getLocation());
        }
        
        if (resolvedependencies) resolveDependencies();
            
        for (int j = 0; j < topdirs.length; j++) {
            topdir = topdirs[j];            
            for (int i = 0; i < modules.size (); i++) {
                String module = (String) modules.elementAt (i);
                Ant ant = (Ant) getProject().createTask("ant");
                ant.init ();                
                ant.setLocation(getLocation());
                File fl = new File(topdir.getAbsolutePath () + 
                    File.separatorChar + module + File.separatorChar + "build.xml");                
                if (! fl.exists()) {                    
                    continue;
                }
                try {
                    // This param changed from String to File after Ant 1.1, so to work in both:
                    try {
                        Ant.class.getMethod ("setDir", new Class[] { File.class }).invoke
                        (ant, new Object[] { new File (topdir, module) });
                    } catch (NoSuchMethodException nsme) {
                        Ant.class.getMethod ("setDir", new Class[] { String.class }).invoke
                        (ant, new Object[] { topdir.getAbsolutePath () + File.separatorChar + module });
                    }
                } catch (Exception e) {
                    throw new BuildException("Could not set 'dir' attribute on Ant task", e, getLocation());
                }
                ant.setTarget (targetname);
                try {
                    log("Process '"+ module + "' location with '" + targetname + "' target", Project.MSG_INFO);
                    ant.execute ();
                } catch (BuildException be) {
                    if (failonerror) {
                        throw new BuildException(be.getMessage(), be, getLocation());
                    } else {
                        log("Target \"" + targetname + "\" failed in module \"" + module + "\"", Project.MSG_WARN);
                        log(fl.getAbsolutePath());
                        log(be.getMessage());
                        String fname = fl.getAbsolutePath();
                        failedmodules.addElement( fname );
                    }
                }
            }
        }
        if (failedmodules.size() > 0) {
            log("<cleanall> SOME MODULES FAILED TO BUILD, BUT THEIR BuildException WAS CAUGHT", Project.MSG_WARN);
            log("<cleanall> cleanfailedmodules=\"" + failedmodules.toString() + "\"", Project.MSG_WARN);
        }
    }
}
