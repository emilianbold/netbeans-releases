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
import org.apache.tools.ant.types.*;

/** A special fileset permitting exclusions based on CVS characteristics.
 * @author Jesse Glick
 */
public class CvsFileSet extends FileSet {
    
    /** Filtering mode for CVS filesets.
     */
    public static final class Mode extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {
                "controlled",
                "uncontrolled",
                "text",
                "binary",
            };
        }
    }
    
    private String mode = "controlled";
    
    /** Set filtering mode.
     * @param m The mode to use:
     * <dl>
     * <dt><code>controlled</code> (default)
     * <dd>Only files under CVS control are matched.
     * <dt><code>uncontrolled</code>
     * <dd>Only files not under CVS control are matched.
     * <dt><code>text</code>
     * <dd>Only files under CVS control and marked as textual are matched.
     * <dt><code>binary</code>
     * <dd>Only files under CVS control and marked as binary are matched.
     * </dl>
     */
    public void setMode(Mode m) {
        mode = m.getValue();
    }
    
    public DirectoryScanner getDirectoryScanner(Project proj) throws BuildException {
        DirectoryScanner scan = new CvsDirectoryScanner();
        setupDirectoryScanner(scan, proj);
        scan.scan();
        return scan;
    }
    
    private static final Set[] NO_ENTRIES = new Set[] {Collections.EMPTY_SET, Collections.EMPTY_SET};
        
    private class CvsDirectoryScanner extends DirectoryScanner {
        
        // Map from dirs to parsed CVS/Entries, being set of text and binary filenames
        private final Map entries = new HashMap(100); // Map<File,Set<String>[2]>
        
        protected boolean isIncluded(String name) throws BuildException {
            if (! super.isIncluded(name)) return false;
            File f = new File(getBasedir(), name);
            if (! f.exists()) throw new IllegalStateException();
            if (! f.isFile()) return false;
            Set[] entries = loadEntries(f.getParentFile());
            Set text = entries[0];
            Set binary = entries[1];
            String bname = f.getName();
            if (mode.equals("controlled")) {
                return text.contains(bname) || binary.contains(bname);
            } else if (mode.equals("uncontrolled")) {
                return ! text.contains(bname) && ! binary.contains(bname);
            } else if (mode.equals("text")) {
                return text.contains(bname);
            } else if (mode.equals("binary")) {
                return binary.contains(bname);
            } else {
                throw new IllegalStateException(mode);
            }
        }
        
        private Set[] loadEntries(File dir) throws BuildException {
            Set[] tb = (Set[])entries.get(dir);
            if (tb == null) {
                File efile = new File(new File(dir, "CVS"), "Entries");
                if (efile.exists()) {
                    tb = new Set[] {new HashSet(10), new HashSet(10)};
                    try {
                        Reader r = new FileReader(efile);
                        try {
                            BufferedReader buf = new BufferedReader(r);
                            String line;
                            while ((line = buf.readLine()) != null) {
                                if (line.startsWith("/")) {
                                    line = line.substring(1);
                                    int idx = line.indexOf('/');
                                    String name = line.substring(0, idx);
                                    idx = line.lastIndexOf('/');
                                    line = line.substring(0, idx);
                                    idx = line.lastIndexOf('/');
                                    String subst = line.substring(idx + 1);
                                    if (subst.equals("")) {
                                        tb[0].add(name);
                                    } else if (subst.equals("-kb")) {
                                        tb[1].add(name);
                                    } else {
                                        throw new BuildException("Strange key subst mode in " + efile + ": " + subst);
                                    }
                                }
                            }
                        } finally {
                            r.close();
                        }
                    } catch (IOException ioe) {
                        throw new BuildException("While reading " + efile, ioe);
                    }
                } else {
                    tb = NO_ENTRIES;
                }
                entries.put(dir, tb);
            }
            return tb;
        }
        
        protected boolean couldHoldIncluded(String name) {
            if (! super.couldHoldIncluded(name)) return false;
            // Do not look into CVS meta directories.
            return ! name.endsWith(File.separatorChar + "CVS");
        }
        
    }
    
}
