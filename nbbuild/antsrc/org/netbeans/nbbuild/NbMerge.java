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
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Deltree;
import org.apache.tools.ant.taskdefs.Copydir;

/** Pseudo-task to unpack a set of modules.
 * Causes the containing target to both depend on the building of the modules in
 * the first place; and then to unpack them all to a certain location.
 *
 * @author Jesse Glick
 */
public class NbMerge extends Task {
    
    private String dest = "virgin";
    private Vector modules = new Vector (); // Vector<String>
    private String targetprefix = "all-";
    private String topdir = "..";
    
    /** Target directory to unpack to (top of IDE installation). */
    public void setDest (String s) {
        dest = s;
    }
    
    /** Comma-separated list of modules to include. */
    public void setModules (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        modules = new Vector ();
        while (tok.hasMoreTokens ())
            modules.addElement (tok.nextToken ());
    }
    
    /** String which will have a module name appended to it.
     * This will form a target in the same project which should
     * create the <samp>netbeans/</samp> subdirectory.
     */
    public void setTargetprefix (String s) {
        targetprefix = s;
    }
    
    /** Set the top directory.
     * There should be subdirectories under this for each named module.
     */
    public void setTopdir (String s) {
        topdir = s;
    }
    
    public void execute () throws BuildException {
        // Somewhat convoluted code because Project.executeTargets does not
        // eliminate duplicates when analyzing dependencies! Ecch.
        Target dummy = new Target ();
        String dummyName = "nbmerge-" + target.getName ();
        Hashtable targets = project.getTargets ();
        while (targets.contains (dummyName))
            dummyName += "-x";
        dummy.setName (dummyName);
        for (int i = 0; i < modules.size (); i++) {
            String module = (String) modules.elementAt (i);
            dummy.addDependency (targetprefix + module);
        }
        project.addTarget (dummy);
        project.executeTarget (dummyName);

        Deltree deltree = (Deltree) project.createTask ("deltree");
        deltree.setDir (dest);
	deltree.init ();
        deltree.setLocation (location);
	deltree.execute ();

        for (int i = 0; i < modules.size (); i++) {
            String module = (String) modules.elementAt (i);
	    String netbeans = topdir + '/' + module + "/netbeans";
	    if (! new File (netbeans).exists ()) {
		log ("Build product dir " + netbeans + " does not exist, skipping...", Project.MSG_WARN);
		continue;
	    }
            Copydir copydir = (Copydir) project.createTask ("copydir");
            copydir.setSrc (netbeans);
            copydir.setDest (dest);
	    copydir.init ();
            copydir.setLocation (location);
            copydir.execute ();
        }
    }

}
