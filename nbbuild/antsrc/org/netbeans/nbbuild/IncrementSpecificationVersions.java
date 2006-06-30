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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Increments specification versions of all specified modules,
 * in the trunk or in a branch, in a regulated manner.
 * @author Jesse Glick
 */
public final class IncrementSpecificationVersions extends Task {
    
    private File nbroot;
    private List/*<String>*/ modules;
    private int stickyLevel = -1;
    
    public IncrementSpecificationVersions() {}
    
    public void setNbroot(File f) {
        nbroot = f;
    }
    
    public void setModules(String m) {
        modules = Collections.list(new StringTokenizer(m, ", "));
    }
    
    public void setBranch(boolean b) {
        setStickyLevel(b ? 2 : 1);
    }

    /** Number of digits from the begining that are supposed to
     * stay the same
     */
    public void setStickyLevel(int stickyLevel) {
        if (this.stickyLevel != -1) {
            throw new BuildException("Only one stickyLevel or branch attribute can be used!");
        }

        this.stickyLevel = stickyLevel;
    }

    public void execute() throws BuildException {
        if (nbroot == null || modules == null) {
            throw new BuildException("Missing params 'nbroot' or 'modules'", getLocation());
        }
        Iterator it = modules.iterator();
        MODULE: while (it.hasNext()) {
            String module = (String) it.next();
            File dir = new File(nbroot, module.replace('/', File.separatorChar));
            if (!dir.isDirectory()) {
                log("No such directory " + dir + "; skipping", Project.MSG_WARN);
                continue;
            }
            try {
                File pp = new File(dir, "nbproject" + File.separatorChar + "project.properties");
                if (pp.isFile()) {
                    String[] lines = gulp(pp, "ISO-8859-1");
                    for (int i = 0; i < lines.length; i++) {
                        Matcher m1 = Pattern.compile("(spec\\.version\\.base=)(.+)").matcher(lines[i]);
                        if (m1.matches()) {
                            String old = m1.group(2);
                            String nue = increment(old, stickyLevel, false);
                            if (nue != null) {
                                lines[i] = m1.group(1) + nue;
                                spit(pp, "ISO-8859-1", lines);
                                log("Incrementing " + old + " -> " + nue + " in " + pp);
                            } else {
                                log(pp + ":" + (i + 1) + ": Unsupported old version number " + old + " (must be x.y.0 in trunk or x.y.z in branch); skipping", Project.MSG_WARN);
                            }
                            continue MODULE;
                        }
                    }
                } else {
                    if (!new File(dir, "nbproject" + File.separatorChar + "project.xml").isFile()) {
                        log("No such file " + pp + "; unprojectized module?", Project.MSG_WARN);
                    }
                }
                File mf = new File(dir, "manifest.mf");
                if (mf.isFile()) {
                    String[] lines = gulp(mf, "UTF-8");
                    for (int i = 0; i < lines.length; i++) {
                        Matcher m1 = Pattern.compile("(OpenIDE-Module-Specification-Version: )(.+)").matcher(lines[i]);
                        if (m1.matches()) {
                            String old = m1.group(2);
                                String nue = increment(old, stickyLevel, true);
                            if (nue != null) {
                                lines[i] = m1.group(1) + nue;
                                spit(mf, "UTF-8", lines);
                                log("Incrementing " + old + " -> " + nue + " in " + mf);
                            } else {
                                log(mf + ":" + (i + 1) + ": Unsupported old version number " + old + " (must be x.y in trunk or x.y.z in branch); skipping", Project.MSG_WARN);
                            }
                            continue MODULE;
                        }
                    }
                } else {
                    log("No such file " + mf + "; not a real module?", Project.MSG_WARN);
                }
                log("Could not find any specification version in " + dir + "; skipping", Project.MSG_WARN);
            } catch (IOException e) {
                throw new BuildException("While processing " + dir + ": " + e, e, getLocation());
            }
        }
    }

    /** Does the increment of the specification version to new version.
     * @return the new version or null if the increment fails
     */
    static String increment(String old, int stickyLevel, boolean manifest) throws NumberFormatException {
        String nue = null;

        switch (stickyLevel) {
            case 1: // trunk
                if (manifest) {
                    Matcher m2 = Pattern.compile("([0-9]+\\.)([0-9]+)").matcher(old);
                    if (m2.matches()) {
                        nue = m2.group(1) + (Integer.parseInt(m2.group(2)) + 1);
                    }
                } else {
                    Matcher m2 = Pattern.compile("([0-9]+\\.)([0-9]+)(\\.0)").matcher(old);
                    if (m2.matches()) {
                        nue = m2.group(1) + (Integer.parseInt(m2.group(2)) + 1) + m2.group(3);
                    }
                }
                break;
            case 2: // branch
                if (manifest) {
                    Matcher m2 = Pattern.compile("([0-9]+\\.[0-9]+\\.)([0-9]+)").matcher(old);
                    if (m2.matches()) {
                        nue = m2.group(1) + (Integer.parseInt(m2.group(2)) + 1);
                    } else if (old.matches("[0-9]+\\.[0-9]+")) {
                        nue = old + ".1";
                    }
                } else {
                    Matcher m2 = Pattern.compile("([0-9]+\\.[0-9]+\\.)([0-9]+)").matcher(old);
                    if (m2.matches()) {
                        nue = m2.group(1) + (Integer.parseInt(m2.group(2)) + 1);
                    }
                }
                break;
            default:
                if (stickyLevel < 1) {
                    throw new BuildException("Invalid sticky level: " + stickyLevel);
                }
                int[] segments = new int[stickyLevel + 1];
                StringTokenizer tok = new StringTokenizer(old, ".");
                for (int i = 0; i < segments.length && tok.hasMoreElements(); i++) {
                    segments[i] = Integer.parseInt(tok.nextToken());
                }
                segments[stickyLevel]++;
                nue = "";
                String pref = "";
                for (int i = 0; i < segments.length; i++) {
                    nue += pref;
                    nue += segments[i];
                    pref = ".";
                }
                break;
        }

        return nue;
    }

    private static String[] gulp(File file, String enc) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(is, enc));
            List/*<String>*/ l = new ArrayList();
            String line;
            while ((line = r.readLine()) != null) {
                l.add(line);
            }
            return (String[]) l.toArray(new String[l.size()]);
        } finally {
            is.close();
        }
    }
    
    private static void spit(File file, String enc, String[] lines) throws IOException {
        OutputStream os = new FileOutputStream(file);
        try {
            PrintWriter w = new PrintWriter(new OutputStreamWriter(os, enc));
            for (int i = 0; i < lines.length; i++) {
                w.println(lines[i]);
            }
            w.flush();
        } finally {
            os.close();
        }
    }

}
