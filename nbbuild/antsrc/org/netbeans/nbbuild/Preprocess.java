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

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.MatchingTask;

/** Preprocesses content of file replacing all conditional blocks.
* <PRE>
* $if <name of property>$
* $else$
* $end$
* </PRE>
* @author Jaroslav Tulach
*/
public class Preprocess extends MatchingTask {
    /** the format of begining of substitution */
    private static final String F_BEGIN = "/*nbif"; // NOI18N
    /** format of else statement */
    private static final String F_ELSE = "nbelse*/"; // NOI18N
    /** how to replace the else */
    private static final String R_ELSE = "/*nbelse"; // NOI18N
    /** format of end statement */
    private static final String F_END = "/*nbend*/"; // NOI18N
    
    
    
    /** source directory to scan files from */
    private File src;
    /** target directory */
    private File dest;
    /** copy all/copy modified */
    private boolean copyAll = false;
    
    public Preprocess () {
    }
    
    
    /** Setter for the source directory to scan */
    public void setSrcDir (File f) {
        src = f;
    }
    
    /** Setter for the target directory to create processed files in
    */
    public void setDestDir (File f) {
        dest = f;
    }
    
    /** Can be set to copy all files, if necessary.
    * @ param copyAll true if all files (even unmodified) should be copied
    */
    public void setCopyAll (boolean copyAll) {
        this.copyAll = copyAll;
    }
    
    /** Executes the task.
     */
    public void execute () throws BuildException {
        if (src == null || dest == null) {
            throw new BuildException ("src and dest must be specified");
        }
        
        DirectoryScanner scanner = getDirectoryScanner (this.src);
        scanner.scan ();
        String[] files = scanner.getIncludedFiles ();

        log (
            "Processing " + files.length + 
            " file(s) from directory " + this.src + " to " + this.dest
        );
        

        java.util.Map map = new java.util.HashMap ();
        map.putAll (getProject ().getUserProperties ());
        map.putAll (getProject ().getProperties ());
        
        try {
            for (int i = 0; i < files.length; i++) {
                File src = new File (this.src, files[i]);

                int size = (int)src.length () + 500;
                BufferedReader r = new BufferedReader (
                    new FileReader (src), size
                );
                StringWriter w = new StringWriter (size);

                boolean modified = replace (r, w, map);
                w.close ();
                r.close ();
                
                if (modified) {
                    log ("  Modified: " + files[i]);
                }

                if (modified || copyAll) {
                    // the file has been modified
                    File dest = new File (this.dest, files[i]);
                    
                    // ensure the directories exists
                    File dir = dest.getParentFile ();
                    dir.mkdirs ();
                    
                    Writer file = new FileWriter (dest);
                    file.write (w.getBuffer().toString());
                    file.close ();
                }
            }
        } catch (IOException ex) {
            throw new BuildException (ex);
        }
    }
    
    
    
    
/*nbif test
    public static void main (String[] args) throws IOException {
        BufferedReader r = new BufferedReader (
            new FileReader (args[0])
        );
        
        BufferedWriter w = new BufferedWriter (
            new OutputStreamWriter (System.out), System.getProperties ()
        );
        
        replace (r, w);
        
        w.close ();
    }
/*nbend*/
    
    
    
    
    /** Reads a content of a file and produces replaces given lines
     *
     * @param r reader to read from
     * @param w writer to write to
     * @param props properties to check
     * @return true if the content of r has been modified
     */
    private static boolean replace (
        BufferedReader r, Writer w, java.util.Map props
    ) throws IOException {
        boolean modified = false;
        
        int state = 0;
        
        for (;;) {
            String line = r.readLine ();
            if (line == null) {
                return modified;
            }
            
            switch (state) {
                case 0: // regular text
                    if (line.startsWith (F_BEGIN)) {
                        String rest = line.substring(F_BEGIN.length ()).trim ();
                        if (props.get (rest) != null) {
                            // successful test, the content of line should
                            // be included
                            line += "*/"; // NOI18N
                            modified = true;
                            state = 1;
                        }
                    }
                    break;
                case 1: // waiting for the $else$ statement
                    if (line.startsWith (F_ELSE)) {
                        line = R_ELSE;
                        state = 2;
                    }
                case 2: // inside the else, waiting for end
                    if (line.startsWith (F_END)) {
                        state = 0;
                    }
            }
            
            w.write (line);
            w.write ('\n');
        }
    }
}

/*
* Log
* $
*/
