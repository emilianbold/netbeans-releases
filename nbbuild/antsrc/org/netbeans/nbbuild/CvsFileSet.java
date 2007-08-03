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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;

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
    
    private static final List<Set<String>> NO_ENTRIES = new ArrayList<Set<String>>();
    static {
        NO_ENTRIES.add(Collections.<String>emptySet());
        NO_ENTRIES.add(Collections.<String>emptySet());
    }
        
    private class CvsDirectoryScanner extends DirectoryScanner {
        
        // Map from dirs to parsed CVS/Entries, being set of text and binary filenames
        private final Map<File,List/*2*/<Set<String>>> entries = new HashMap<File,List<Set<String>>>(100);
        
        protected boolean isIncluded(String name) throws BuildException {
            if (! super.isIncluded(name)) return false;
            File f = new File(getBasedir(), name);
            if (! f.exists()) throw new IllegalStateException();
            if (!f.isFile()) {
                // Need to say it is included so that <delete includeemptydirs="true"> will work as expected:
                return true;
            }
            List<Set<String>> entries = loadEntries(f.getParentFile());
            Set<String> text = entries.get(0);
            Set<String> binary = entries.get(1);
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
        
        private List<Set<String>> loadEntries(File dir) throws BuildException {
            List<Set<String>> tb = entries.get(dir);
            if (tb == null) {
                File efile = new File(new File(dir, "CVS"), "Entries");
                // XXX check also Entries.Log?
                if (efile.exists()) {
                    tb = new ArrayList<Set<String>>();
                    tb.add(new HashSet<String>(10));
                    tb.add(new HashSet<String>(10));
                    try {
                        Reader r = new FileReader(efile);
                        try {
                            BufferedReader buf = new BufferedReader(r);
                            String line;
                            int lineNumber = 0;
                            while ((line = buf.readLine()) != null) {
                                lineNumber++;
                                if (line.startsWith("/")) {
                                    line = line.substring(1);
                                    int idx = line.indexOf('/');
                                    String name = line.substring(0, idx);
                                    idx = line.lastIndexOf('/');
                                    line = line.substring(0, idx);
                                    idx = line.lastIndexOf('/');
                                    String subst = line.substring(idx + 1);
                                    if (subst.equals("-kb")) {
                                        tb.get(1).add(name);
                                    } else {
                                        // Usually "", but occasionally "-ko", "-kv", etc.
                                        tb.get(0).add(name);
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
