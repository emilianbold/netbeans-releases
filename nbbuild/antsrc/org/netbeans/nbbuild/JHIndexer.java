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

import java.io.*;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Mkdir;

// ToDo:
// stopwords configuration
// locale setting
// add branding suffix to each generated file
// verbose mode

/** Task to run JavaHelp search indexer.
 * Creates the proper binary search database from source HTML.
 * @author Jesse Glick
 * @see <a href="http://java.sun.com/products/javahelp/">JavaHelp home page</a>
 */
public class JHIndexer extends MatchingTask {

    private File jhall;
    private File db;
    private File basedir;

    /** Set the location of <samp>jhall.jar</samp> (JavaHelp tools library). */
    public void setJhall (File jhall) {
        // JavaHelp release notes say jhtools.jar is enough, but class NoClassDefFoundError
        // on javax.help.search.IndexBuilder when I tried it...
        this.jhall = jhall;
    }

    /** Set the location of the output database.
     * E.g. <samp>JavaHelpSearch</samp>).
     * <strong>Warning:</strong> the directory will be deleted and recreated.
     */
    public void setDb (File db) {
        this.db = db;
    }

    /** Set the base directory from which to scan files.
     * This should be the directory containing the helpset for the database to work correctly.
     */
    public void setBasedir (File basedir) {
        this.basedir = basedir;
    }

    public void execute () throws BuildException {
        if (jhall == null) throw new BuildException ("Must specify the jhall attribute");
        if (db == null) throw new BuildException ("Must specify the db attribute");
        if (basedir == null) throw new BuildException ("Must specify the basedir attribute");
        FileScanner scanner = getDirectoryScanner (basedir);
        scanner.scan ();
        String[] files = scanner.getIncludedFiles ();
        // First, an up-to-date check. ;-)
        if (basedir.exists ()) {
            long lastModified = Long.MIN_VALUE;
            // First scan output dir for any files.
            FileScanner output = new DirectoryScanner ();
            output.setBasedir (db);
            output.scan ();
            String[] outfiles = output.getIncludedFiles ();
            if (outfiles.length > 0) {
                for (int i = 0; i < outfiles.length; i++) {
                    long mod = new File (db, outfiles[i]).lastModified ();
                    if (mod > lastModified) {
                        lastModified = mod;
                    }
                }
                // Now check to see if any source files are newer.
                boolean ok = true;
                for (int i = 0; i < files.length; i++) {
                    long mod = new File (basedir, files[i]).lastModified ();
                    if (mod > lastModified) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    // No need to rebuild.
                    return;
                }
            }
        }
        Delete delete = (Delete) project.createTask ("delete");
        delete.setDir (db);
        delete.init ();
        delete.setLocation (location);
        delete.execute ();
        Mkdir mkdir = (Mkdir) project.createTask ("mkdir");
        mkdir.setDir (db);
        mkdir.init ();
        mkdir.setLocation (location);
        mkdir.execute ();
        log ("Running JavaHelp search database indexer...");
        try {
            File config = File.createTempFile ("jhindexer-config", ".txt");
            try {
                OutputStream os = new FileOutputStream (config);
                try {
                    PrintWriter pw = new PrintWriter (os);
                    pw.println ("IndexRemove " + basedir + File.separator);
                    String message = "Files to be indexed:";
                    for (int i = 0; i < files.length; i++) {
                        // [PENDING] JavaHelp docs say to use / as file sep for File directives;
                        // so what should the complete path be? Someone should test this on Windoze...
                        String path = basedir + File.separator + files[i];
                        pw.println ("File " + path);
                        message += "\n\t" + path;
                    }
                    log (message, Project.MSG_VERBOSE);
                    pw.flush ();
                } finally {
                    os.close ();
                }
                Java java = (Java) project.createTask ("java");
                java.createClasspath ().createPathElement ().setLocation (jhall);
                java.setClassname ("com.sun.java.help.search.Indexer");
                java.createArg ().setValue ("-c");
                java.createArg ().setFile (config);
                java.createArg ().setValue ("-db");
                java.createArg ().setFile (db);
                java.setFailonerror (true);
                java.init ();
                java.setLocation (location);
                java.execute ();
            } finally {
                config.delete ();
            }
        } catch (IOException ioe) {
            throw new BuildException ("Could not make temporary config file", ioe, location);
        }
    }

}
