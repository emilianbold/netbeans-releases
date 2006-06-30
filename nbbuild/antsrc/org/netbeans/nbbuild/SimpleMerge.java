/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/** Copies content of subdirectories of defined name from set of directories
 * to a certain location.
 *
 * @author Jesse Glick, Rudolf Balada
 *
 * Copied and changed from NbMerge.java
 */
public class SimpleMerge extends Task {
    
    private File dest;
    private Vector modules = new Vector (); // Vector<String>
    private List topdirs = new ArrayList (); // List<File>
    private List subdirs = new ArrayList (); // List<File>
    
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

    /** Set the subdirectory.
     * There should be these subdirectories in each named module.
     */
    public void setSubdir (String t) {
        subdirs.add (t);
    }

    /** Nested subdir addition. */
    public class Subdir {
        /** Path to an extra Subdir. */
        public void setPath (String t) {
            subdirs.add (t);
        }
    }
    /** Add a nested subdir.
     * If there is more than one subdir total, build products
     * may be taken from any of them, including from multiple places
     * for the same module. (Later subdirs may override build
     * products in earlier subdirs.)
     */
    public Subdir createSubdir () {
        return new Subdir ();
    }

    public void execute () throws BuildException {
        if (topdirs.isEmpty ()) {
            throw new BuildException("You must set at least one topdir attribute", getLocation());
        }
        
        if (subdirs.isEmpty ()) {
            throw new BuildException("You must set at least one subdir attribute", getLocation());
        }

        log ( "Starting merge to " + dest.getAbsolutePath() );
        for (int j = 0; j < topdirs.size (); j++) {
            File topdir = (File) topdirs.get (j);
            for (int i = 0; i < modules.size (); i++) {
                String module = (String) modules.elementAt (i);
                for (int h = 0; h < subdirs.size (); h++) {
                    String sdir = (String) subdirs.get (h);
		    File subdir = new File (new File (topdir, module), sdir );
		    if (! subdir.exists ()) {
			log ("Dir " + subdir + " does not exist, skipping...", Project.MSG_WARN);
			continue;
		    }
		    Copy copy = (Copy) getProject().createTask("copy");
		    FileSet fs = new FileSet ();
		    fs.setDir (subdir);
		    copy.addFileset (fs);
		    copy.setTodir (dest);
		    copy.setIncludeEmptyDirs (true);
		    copy.init ();
		    copy.setLocation(getLocation());
		    copy.execute ();
                }
            }
        }
        log ( "Merge finished" );
    }
}
