/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.util.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.FileSet;

import org.apache.regexp.*;

/** Task to search some files for bad constructions.
 * @author Jesse Glick
 */
public class FindBadConstructions extends Task {
    
    private List filesets = new LinkedList(); // List<FileSet>
    private List bad = new LinkedList(); // List<Construction>
    
    /** Add a set of files to scan. */
    public void addFileset(FileSet fs) {
        filesets.add(fs);
    }
    /** Add a construction that is bad. */
    public Construction createConstruction() {
        Construction c = new Construction();
        bad.add(c);
        return c;
    }
    /** One bad construction. */
    public class Construction {
        private boolean caseInsens = false;
        RE regexp;
        String message = null;
        int show = -1;
        public Construction() {}
        /** Set the bad regular expression to search for. */
        public void setRegexp(String r) throws BuildException {
            try {
                regexp = new RE(r);
            } catch (RESyntaxException rese) {
                throw new BuildException(rese, location);
            }
            if (caseInsens) {
                regexp.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
            }
        }
        /** Set whether it is case-insensitive. */
        public void setCaseInsensitive(boolean ci) {
            caseInsens = ci;
            if (ci && regexp != null) {
                regexp.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
            }
        }
        /** Set an optional message to display as output. */
        public void setMessage(String m) {
            message = m;
        }
        /** Set whether to display the matching text (by default no), and if so which part.
         * 0 means complete match; 1 or higher means that-numbered parenthesis.
         */
        public void setShowMatch(int s) {
            show = s;
        }
    }
    
    public void execute() throws BuildException {
        if (filesets.isEmpty()) throw new BuildException("Must give at least one fileset", location);
        if (bad.isEmpty()) throw new BuildException("Must give at least one construction", location);
        Iterator it = filesets.iterator();
        while (it.hasNext()) {
            FileSet fs = (FileSet)it.next();
            FileScanner scanner = fs.getDirectoryScanner(project);
            File dir = scanner.getBasedir();
            String[] files = scanner.getIncludedFiles();
            log("Scanning " + files.length + " files in " + dir);
            for (int i = 0; i < files.length; i++) {
                File f = new File(dir, files[i]);
                try {
                    Iterator it2 = bad.iterator();
                    while (it2.hasNext()) {
                        Construction c = (Construction)it2.next();
                        if (c.regexp == null) throw new BuildException("Must specify regexp on a construction", location);
                        // LineNumberReader is cool, but ReaderCharacterIterator reads well ahead before matching...
                        InputStream is = new FileInputStream(f);
                        try {
                            CharacterIterator cit = new StreamCharacterIterator(is);
                            int idx = 0;
                            while (c.regexp.match(cit, idx)) {
                                idx = c.regexp.getParenEnd(0);
                                StringBuffer message = new StringBuffer(1000);
                                message.append(f.getAbsolutePath());
                                /*
                                message.append(':');
                                message.append(rd.getLineNumber() + 1);
                                 */
                                message.append(": ");
                                if (c.message != null) {
                                    message.append(c.message);
                                }
                                if (c.show != -1) {
                                    if (c.message != null) {
                                        message.append(": ");
                                    }
                                    message.append(c.regexp.getParen(c.show));
                                }
                                if (c.show == -1 && c.message == null) {
                                    message.append("bad construction found");
                                }
                                log(message.toString(), Project.MSG_WARN);
                            }
                        } finally {
                            is.close();
                        }
                    }
                } catch (IOException ioe) {
                    throw new BuildException("Error reading " + f, ioe, location);
                }
            }
        }
    }
    
}
