/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.FileSet;

/** Task to search some files for bad constructions.
 * @author Jesse Glick
 */
public class FindBadConstructions extends Task {
    
    private static final Pattern lineBreak = Pattern.compile("^", Pattern.MULTILINE);
    
    private List filesets = new LinkedList(); // List<FileSet>
    private List bad = new LinkedList(); // List<Construction>
    
    /** Add a set of files to scan. */
    public void addFileset(FileSet fs) {
        filesets.add(fs);
    }
    /** Add a set of files to scan, according to CVS status. */
    public void addCvsFileset(CvsFileSet fs) {
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
        Pattern regexp;
        String message = null;
        int show = -1;
        public Construction() {}
        /**
         * Set the bad regular expression to search for.
         * Use embedded flags to set any desired pattern behaviors like case insensitivity;
         * multiline mode is always on.
         */
        public void setRegexp(String r) throws BuildException {
            try {
                regexp = Pattern.compile(r, Pattern.MULTILINE);
            } catch (PatternSyntaxException rese) {
                throw new BuildException(rese, getLocation());
            }
        }
        /** Set an optional message to display as output. */
        public void setMessage(String m) {
            message = m;
        }
        /** Set whether to display the matching text (by default no), and if so which part.
         * @param s 0 means complete match; 1 or higher means that-numbered parenthesis
         */
        public void setShowMatch(int s) {
            show = s;
        }
    }
    
    public void execute() throws BuildException {
        if (filesets.isEmpty()) throw new BuildException("Must give at least one fileset", getLocation());
        if (bad.isEmpty()) throw new BuildException("Must give at least one construction", getLocation());
        Iterator it = filesets.iterator();
        while (it.hasNext()) {
            FileSet fs = (FileSet)it.next();
            FileScanner scanner = fs.getDirectoryScanner(getProject());
            File dir = scanner.getBasedir();
            String[] files = scanner.getIncludedFiles();
            log("Scanning " + files.length + " files in " + dir);
            for (int i = 0; i < files.length; i++) {
                File f = new File(dir, files[i]);
                //System.err.println("working on " + f);
                try {
                    Iterator it2 = bad.iterator();
                    while (it2.hasNext()) {
                        Construction c = (Construction)it2.next();
                        if (c.regexp == null) throw new BuildException("Must specify regexp on a construction", getLocation());
                        FileInputStream fis = new FileInputStream(f);
                        FileChannel fc = fis.getChannel();
                        try {
                            ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0L, fc.size());
                            Charset cs = Charset.forName("UTF-8");
                            CharBuffer content = cs.decode(bb);
                            Matcher m = c.regexp.matcher(content);
                            while (m.find()) {
                                StringBuffer message = new StringBuffer(1000);
                                message.append(f.getAbsolutePath());
                                message.append(':');
                                Matcher lbm = lineBreak.matcher(content);
                                int line = 0;
                                int col = 1;
                                while (lbm.find()) {
                                    if (lbm.start() <= m.start()) {
                                        line++;
                                        col = m.start() - lbm.start() + 1;
                                    } else {
                                        break;
                                    }
                                }
                                message.append(line);
                                message.append(":");
                                message.append(col);
                                message.append(": ");
                                if (c.message != null) {
                                    message.append(c.message);
                                }
                                if (c.show != -1) {
                                    if (c.message != null) {
                                        message.append(": ");
                                    }
                                    message.append(m.group(c.show));
                                }
                                if (c.show == -1 && c.message == null) {
                                    message.append("bad construction found");
                                }
                                log(message.toString(), Project.MSG_WARN);
                            }
                        } finally {
                            fc.close();
                            fis.close();
                        }
                    }
                } catch (IOException ioe) {
                    throw new BuildException("Error reading " + f, ioe, getLocation());
                }
            }
        }
    }
    
}
