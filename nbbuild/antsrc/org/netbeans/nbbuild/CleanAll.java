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
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;

/** Runs a clean task in all submodules.
 *
 * @author Jesse Glick
 */
public class CleanAll extends Task {
    
    private Vector modules = new Vector (); // Vector<String>
    private String targetname = "clean";
    private String topdir = "..";
    
    public void setModules (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        modules = new Vector ();
        while (tok.hasMoreTokens ())
            modules.addElement (tok.nextToken ());
    }
    
    public void setTargetname (String s) {
        targetname = s;
    }
    
    public void setTopdir (String s) {
        topdir = s;
    }
    
    public void execute () throws BuildException {
        for (int i = 0; i < modules.size (); i++) {
            String module = (String) modules.elementAt (i);
	    Ant ant = (Ant) project.createTask ("ant");
	    ant.init ();
	    ant.setLocation (location);
	    ant.setDir (topdir + '/' + module);
	    ant.setTarget (targetname);
	    ant.execute ();
	}
    }

}
