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
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

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
    private List suppress = new LinkedList (); // List<Suppress>
    
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

    /** Locale to suppress. */
    public class Suppress {
        // [PENDING] also support branding here
        String locale;
        String iftest;
        String unlesstest;
        /** Name of the locale, e.g. <samp>ja</samp>. */
        public void setLocale (String l) {
            locale = l;
        }
        /** Property which if set will enable the suppression. */
        public void setIf (String p) {
            iftest = p;
        }
        /** Property which if set will disable the suppression. */
        public void setUnless (String p) {
            unlesstest = p;
        }
    }
    /** Add a locale to suppress.
     * Files matching this locale suffix will not be merged in.
     * E.g. for the locale <samp>ja</samp>, this will exclude
     * all files and directories ending in <samp>_ja</samp> as well
     * as files ending in <samp>_ja.</samp> plus some extension.
     */
    public Suppress createSuppress () {
        Suppress s = new Suppress ();
        suppress.add (s);
        return s;
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
        
        Delete delete = (Delete) project.createTask ("delete");
        delete.setDir (dest);
        delete.init ();
        delete.setLocation (location);
        delete.execute ();

        List suppressedlocales = new LinkedList (); // List<String>
        Iterator it = suppress.iterator ();
        while (it.hasNext ()) {
            Suppress s = (Suppress) it.next ();
            if (s.iftest != null && project.getProperty (s.iftest) == null) {
                continue;
            } else if (s.unlesstest != null && project.getProperty (s.unlesstest) != null) {
                continue;
            }
            log ("Suppressing locale: " + s.locale);
            suppressedlocales.add (s.locale);
        }
        
        for (int j = 0; j < topdirs.size (); j++) {
            File topdir = (File) topdirs.get (j);
            for (int i = 0; i < modules.size (); i++) {
                String module = (String) modules.elementAt (i);
                File netbeans = new File (new File (topdir, module), "netbeans");
                if (! netbeans.exists ()) {
                    log ("Build product dir " + netbeans + " does not exist, skipping...", Project.MSG_WARN);
                    continue;
                }
                Copy copy = (Copy) project.createTask ("copy");
                FileSet fs = new FileSet ();
                fs.setDir (netbeans);
                it = suppressedlocales.iterator ();
                while (it.hasNext ()) {
                    String locale = (String) it.next ();
                    fs.createExclude ().setName ("**/*_" + locale);
                    fs.createExclude ().setName ("**/*_" + locale + ".*");
                    fs.createExclude ().setName ("**/*_" + locale + "/");
                }
                copy.addFileset (fs);
                copy.setTodir (dest);
                copy.setIncludeEmptyDirs (true);
                copy.init ();
                copy.setLocation (location);
                copy.execute ();
            }
        }
    }
}
