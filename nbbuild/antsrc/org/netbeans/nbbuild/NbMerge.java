/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Deltree;
import org.apache.tools.ant.taskdefs.Expand;

/** Pseudo-task to unpack a set of modules.
 * Causes the containing target to both depend on the building of the modules in
 * the first place; and then to unpack them all to a certain location.
 *
 * @author Jesse Glick
 */
public class NbMerge extends Task {
    
    private String upperdir = ".";
    private Vector modules = new Vector (); // Vector<String>
    private String targetprefix = "all-";
    private String topdir = "..";
    private Vector tasks = null;
    
    public void setUpperdir (String s) {
        upperdir = s;
    }
    
    public void setModules (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        modules = new Vector ();
        while (tok.hasMoreTokens ())
            modules.addElement (tok.nextToken ());
    }
    
    public void setTargetprefix (String s) {
        targetprefix = s;
    }
    
    public void setTopdir (String s) {
        topdir = s;
    }
    
    public void init () throws BuildException {
        tasks = new Vector ();
        Deltree deltree = (Deltree) project.createTask ("deltree");
        deltree.setLocation (location);
        // Yes, this is fixed by .nbm format:
        deltree.setDir (upperdir + "/netbeans");
        tasks.addElement (deltree);
        for (int i = 0; i < modules.size (); i++) {
            String module = (String) modules.elementAt (i);
            Expand expand = (Expand) project.createTask ("expand");
            expand.setLocation (location);
            expand.setDest (upperdir);
            expand.setSrc (topdir + '/' + module + '/' + module + ".nbm");
            tasks.addElement (expand);
        }
    }
    
    // Admittedly this is ugly, but the target is not set when init() is called...
    public void setOwningTarget (Target t) {
        super.setOwningTarget (t);
        for (int i = 0; i < modules.size (); i++) {
            String module = (String) modules.elementAt (i);
            t.addDependency (targetprefix + module);
        }
        for (int i = 0; i < tasks.size (); i++) {
            Task task = (Task) tasks.elementAt (i);
            t.addTask (task);
        }
    }
    
    // No execute method...the real tasks take care of that.
    
}
