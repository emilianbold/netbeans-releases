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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DirSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Removes files from clusters which are not listed as belonging to any NBM.
 * @see <a href="http://www.netbeans.org/nonav/issues/show_bug.cgi?id=111946">issue #111946</a>
 */
public class DeleteUnreferencedClusterFiles extends Task {

    private DirSet clusters;
    /**
     * Set of cluster directories to scan.
     * Any dir lacking an update_tracking subdir is automatically ignored.
     */
    public void addConfiguredClusters(DirSet clusters) {
        this.clusters = clusters;
    }

    private File report;
    /** Write any errors to a JUnit report rather than halting build. */
    public void setReport(File report) {
        this.report = report;
    }

    public @Override void execute() throws BuildException {
        StringBuilder missingFiles = new StringBuilder();
        StringBuilder extraFiles = new StringBuilder();
        StringBuilder duplicatedFiles = new StringBuilder();
        for (String incl : clusters.getDirectoryScanner().getIncludedDirectories()) {
            File cluster = new File(clusters.getDir(), incl);
            File updateTracking = new File(cluster, "update_tracking");
            if (!updateTracking.isDirectory()) {
                continue;
            }
            Map</*path*/String,/*CNB*/String> files = new HashMap<String,String>();
            for (File module : updateTracking.listFiles()) {
                if (!module.getName().endsWith(".xml")) {
                    continue;
                }
                try {
                    Document doc = XMLUtil.parse(new InputSource(module.toURI().toString()), false, false, null, null);
                    String cnb = doc.getDocumentElement().getAttribute("codename").replaceFirst("/[0-9]+$", "");
                    NodeList nl = doc.getElementsByTagName("file");
                    for (int i = 0; i < nl.getLength(); i++) {
                        String file = ((Element) nl.item(i)).getAttribute("name");
                        if (new File(cluster, file).isFile()) {
                            String prev = files.put(file, cnb);
                            if (prev != null) {
                                duplicatedFiles.append("\ntwo registrations of the same file: " + file + " (from " + prev + " and " + cnb + ")");
                            }
                        } else {
                            missingFiles.append("\n" + cnb + ": missing " + file);
                        }
                    }
                } catch (Exception x) {
                    throw new BuildException("Parsing " + module + ": " + x, x, getLocation());
                }
            }
            scanForExtraFiles(cluster, "", files.keySet(), cluster.getName(), extraFiles);
        }
        Map<String,String> pseudoTests = new LinkedHashMap<String,String>();
        pseudoTests.put("testMissingFiles", missingFiles.length() > 0 ? "Some files were missing" + missingFiles : null);
        pseudoTests.put("testExtraFiles", extraFiles.length() > 0 ? "Some extra files were present" + extraFiles : null);
        pseudoTests.put("testDuplicatedFiles", duplicatedFiles.length() > 0 ? "Some files were registered in two or more NBMs" + duplicatedFiles : null);
        JUnitReportWriter.writeReport(this, report, pseudoTests);
    }

    private void scanForExtraFiles(File d, String prefix, Set<String> files, String cluster, StringBuilder extraFiles) {
        if (prefix.equals("update_tracking/")) {
            return;
        }
        for (String n : d.list()) {
            File f = new File(d, n);
            if (f.isDirectory()) {
                scanForExtraFiles(f, prefix + n + "/", files, cluster, extraFiles);
            } else {
                String path = prefix + n;
                if (!files.contains(path)) {
                    extraFiles.append("\n" + cluster + ": untracked file " + path);
                    // XXX uncomment once test is passing: f.delete();
                }
            }
        }
    }

}
