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
import java.util.*;

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
    
    private File dest;
    private Vector modules = new Vector (); // Vector<String>
    private String targetprefix = "all-";    
    private List topdirs = new ArrayList (); // List<File>
    
    /** Target directory to unpack to (top of IDE installation). */
    public void setDest (File f) {
        dest = f;
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
    public void setTopdir (File t) {
        topdirs.add (t);
    }

    /** Nested topdir addition. */
    public class Topdir {
        /** Path to an extra topdir. */
        public void setPath (File t) {
            topdirs.add (t);
        }
    }
    /** Add a nested topdir.
     * If there is more than one topdir total, build products
     * may be taken from any of them, including from multiple places
     * for the same module. (Later topdirs may override build
     * products in earlier topdirs.)
     */
    public Topdir createTopdir () {
        return new Topdir ();
    }

    public void execute () throws BuildException {
        if (topdirs.isEmpty ()) {
            throw new BuildException ("You must set at least one topdir attribute", location);
        }
        
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
        try {
            try {
                Deltree.class.getMethod ("setDir", new Class[] { File.class }).invoke 
                (deltree, new Object[] { dest });
            } catch (NoSuchMethodException nsme) {
                Deltree.class.getMethod ("setDir", new Class[] { String.class }).invoke 
                (deltree, new Object[] { dest.getAbsolutePath () });
            }
            // deltree.setDir (dest.getAbsolutePath ());
        } catch (Exception e) {
            throw new BuildException ("Could not set directory for deltree", e, location);
        }
        deltree.init ();
        deltree.setLocation (location);
        deltree.execute ();
        
        for (int j = 0; j < topdirs.size (); j++) {
            File topdir = (File) topdirs.get (j);
            for (int i = 0; i < modules.size (); i++) {
                String module = (String) modules.elementAt (i);
                File netbeans = new File (new File (topdir, module), "netbeans");
                if (! netbeans.exists ()) {
                    log ("Build product dir " + netbeans + " does not exist, skipping...", Project.MSG_WARN);
                    continue;
                }
                Copydir copydir = (Copydir) project.createTask ("copydir");
                try {
                    try {
                        Copydir.class.getMethod ("setSrc", new Class[] { File.class }).invoke
                        (copydir, new Object[] { netbeans });
                    } catch (NoSuchMethodException nsme) {
                        Copydir.class.getMethod ("setSrc", new Class[] { String.class }).invoke
                        (copydir, new Object[] { netbeans.getAbsolutePath () });
                    }
                } catch (Exception e) {
                    throw new BuildException ("Could not set 'src' attribute on copydir task", e, location);
                }
                
                try {
                    try {
                        Copydir.class.getMethod ("setDest", new Class[] { File.class }).invoke
                        (copydir, new Object[] { dest });
                    } catch (NoSuchMethodException nsme) {
                        Copydir.class.getMethod ("setDest", new Class[] { String.class }).invoke
                        (copydir, new Object[] { dest.getAbsolutePath () });
                    }
                } catch (Exception e) {
                    throw new BuildException ("Could not set 'dest' attribute on copydir task", e, location);
                }
                
                copydir.init ();
                copydir.setLocation (location);
                copydir.execute ();
            }
        }
    }
}
