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

/** Runs a clean task (for example) in all submodules.
 *
 * @author Jesse Glick
 */
public class CleanAll extends Task {
    
    private Vector modules = new Vector (); // Vector<String>
    private String targetname = "clean";
    private File topdir = null;
    private File [] topdirs = null;
    
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
    
    public void execute () throws BuildException {
        
        if (topdirs == null && topdir != null) {
            topdirs = new File[1];
            topdirs[0] = topdir; 
        }
        
        if (topdir == null && topdirs == null) {
            throw new BuildException ("You must set at least one topdir attribute", location);
        }
        
        for (int j = 0; j < topdirs.length; j++) {
            topdir = topdirs[j];            
            for (int i = 0; i < modules.size (); i++) {
                String module = (String) modules.elementAt (i);
                Ant ant = (Ant) project.createTask ("ant");
                ant.init ();                
                ant.setLocation (location);
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
                    throw new BuildException ("Could not set 'dir' attribute on Ant task", e, location);
                }
                ant.setTarget (targetname);
                ant.execute ();
            }
        }
    }
}
