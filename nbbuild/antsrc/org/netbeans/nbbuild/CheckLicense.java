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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/** Check source files for a license notice.
 * @author Jesse Glick
 */
public class CheckLicense extends Task {

    private final List filesets = new ArrayList (1); // List<FileSet>
    private String fragment;

    /** Add a file set of source files to check.
     * @param fs set of files to check licenses of
     */
    public void addFileSet (FileSet fs) {
        filesets.add (fs);
    }
    
    /** Add a file set of CVS-controlled source files to check.
     * @param fs set of files to check licenses of
     */
    public void addCvsFileSet(CvsFileSet fs) {
        filesets.add(fs);
    }

    /** Set the fragment of license notice which is expected
     * to be found in each source file.
     * @param f the fragment
     */
    public void setFragment (String f) {
        fragment = f;
    }

    public void execute () throws BuildException {
        if (fragment == null) throw new BuildException ("You must supply a fragment", location);
        if (filesets.isEmpty ()) throw new BuildException ("You must supply at least one fileset", location);
        Iterator it = filesets.iterator ();
        try {
            while (it.hasNext ()) {
                FileScanner scanner = ((FileSet) it.next ()).getDirectoryScanner (project);
                File baseDir = scanner.getBasedir ();
                String[] files = scanner.getIncludedFiles ();
                log ("Looking for " + fragment + " in " + files.length + " files in " + baseDir.getAbsolutePath ());
                for (int i = 0; i < files.length; i++) {
                    File f = new File (baseDir, files[i]);
                    //log("Scanning " + f, Project.MSG_VERBOSE);
                    BufferedReader br = new BufferedReader (new FileReader (f));
                    try {
                        String line;
                        while ((line = br.readLine ()) != null) {
                            if (line.indexOf (fragment) != -1) {
                                // Found it.
                            break;
                            }
                        }
                        if (line == null) {
                            // Scanned whole file without finding it.
                            log (f.getAbsolutePath () + ":1: no license notice found", Project.MSG_ERR);
                        }
                    } finally {
                        br.close ();
                    }
                }
            }
        } catch (IOException ioe) {
            throw new BuildException ("Could not open files to check licenses", ioe, location);
        }
    }

}
