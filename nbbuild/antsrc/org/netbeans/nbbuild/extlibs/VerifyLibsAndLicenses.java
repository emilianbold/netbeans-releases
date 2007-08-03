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

package org.netbeans.nbbuild.extlibs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.netbeans.nbbuild.JUnitReportWriter;

/**
 * Task to check that external libraries have legitimate licenses, etc.
 */
public class VerifyLibsAndLicenses extends Task {

    private File nball;
    public void setNball(File nball) {
        this.nball = nball;
    }

    private File reportFile;
    /** JUnit-format XML result file to generate, rather than halting the build. */
    public void setReport(File report) {
        this.reportFile = report;
    }

    private Map<String,String> pseudoTests;
    private Set<String> modules;

    public @Override void execute() throws BuildException {
        pseudoTests = new LinkedHashMap<String,String>();
        modules = new TreeSet<String>();
        modules.add("nbbuild");
        for (String cluster : getProject().getProperty("nb.clusters.list").split("[, ]+")) {
            modules.addAll(Arrays.asList(getProject().getProperty(cluster).split("[, ]+")));
        }
        try {
            testNoStrayThirdPartyBinaries();
            testLicenseFilesAreProperlyFormattedPhysically();
            testLicenses();
            testBinaryUniqueness();
        } catch (IOException x) {
            throw new BuildException(x, getLocation());
        }
        JUnitReportWriter.writeReport(this, reportFile, pseudoTests);
    }

    private void testBinaryUniqueness() throws IOException {
        StringBuffer msg = new StringBuffer();
        Map<Long,String> binaries = new HashMap<Long,String>();
        for (String module : modules) {
            File d = new File(new File(nball, module), "external");
            Set<String> cvsFiles = findCvsControlledFiles(d);
            for (String n : cvsFiles) {
                if (!n.endsWith(".jar") && !n.endsWith(".zip")) {
                    continue;
                }
                File f = new File(d, n);
                String path = module + "/external/" + n;
                InputStream is = new FileInputStream(f);
                try {
                    byte[] buf = new byte[4096];
                    int read;
                    CRC32 crc = new CRC32();
                    while ((read = is.read(buf)) != -1) {
                        crc.update(buf, 0, read);
                    }
                    String existing = binaries.put(crc.getValue(), path);
                    if (existing != null) {
                        msg.append("\n" + existing + " and " + path + " are identical");
                    }
                } finally {
                    is.close();
                }
                ZipFile zf = new ZipFile(f);
                try {
                    Enumeration<? extends ZipEntry> entries = zf.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        String innerName = entry.getName();
                        if (!innerName.endsWith(".jar") && !innerName.endsWith(".zip")) {
                            continue;
                        }
                        String innerPath = innerName + " in " + path;
                        is = zf.getInputStream(entry);
                        try {
                            byte[] buf = new byte[4096];
                            int read;
                            CRC32 crc = new CRC32();
                            while ((read = is.read(buf)) != -1) {
                                crc.update(buf, 0, read);
                            }
                            String existing = binaries.put(crc.getValue(), innerPath);
                            if (existing != null) {
                                msg.append("\n" + existing + " and " + innerPath + " are identical");
                            }
                        } finally {
                            is.close();
                        }
                    }
                } finally {
                    zf.close();
                }
            }
        }
        //System.err.println("binaries: " + new TreeSet<String>(binaries.values()));
        pseudoTests.put("testBinaryUniqueness", msg.length() > 0 ? "Some binaries are duplicated" + msg : null);
    }

    private void testLicenseFilesAreProperlyFormattedPhysically() throws IOException {
        StringBuffer msg = new StringBuffer();
        for (String module : modules) {
            File d = new File(new File(nball, module), "external");
            FILE: for (String n : findCvsControlledFiles(d)) {
                if (!n.endsWith("-license.txt")) {
                    continue;
                }
                File f = new File(d, n);
                String path = module + "/external/" + n;
                InputStream is = new FileInputStream(f);
                int line = 1;
                try {
                    CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder().
                            onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
                    Reader r = new InputStreamReader(is, decoder);
                    int column = 0;
                    boolean pastHeader = false;
                    boolean trailingSpace = false;
                    int c;
                    while ((c = r.read()) != -1) {
                        if (trailingSpace && (c == '\r' || c == '\n')) {
                            trailingSpace = false;
                            msg.append("\n" + path + " has a trailing space on line #" + line);
                            continue FILE;
                        }
                        if (c == '\r') {
                            column = 0;
                        } else if (c == '\n') {
                            if (column == 0 && line > 1 && !pastHeader) {
                                pastHeader = true;
                                //System.err.println("encountered header end in " + path + " at line " + line);
                            }
                            column = 0;
                            line++;
                        } else {
                            trailingSpace = c == ' ';
                            column++;
                            if (pastHeader && column > 80) {
                                msg.append("\n" + path + " has line #" + line + " longer than 80 characters");
                                continue FILE;
                            }
                        }
                    }
                    if (column > 0) {
                        msg.append("\n" + path + " must end in a newline");
                    }
                } catch (IOException x) {
                    msg.append("\n" + path + " at line #" + line + ": " + x);
                } finally {
                    is.close();
                }
            }
        }
        pseudoTests.put("testLicenseFilesAreProperlyFormattedPhysically", msg.length() > 0 ? "Some license files were badly formatted" + msg : null);
    }

    private void testLicenses() throws IOException {
        File licenses = new File(new File(nball, "nbbuild"), "licenses");
        Set<String> requiredHeaders = new TreeSet<String>(Arrays.asList("Name", "Version", "Description", "License", "OSR", "Origin"));
        Set<String> optionalHeaders = new HashSet<String>(Arrays.asList("Files", "Source", "Comment"));
        StringBuffer msg = new StringBuffer();
        for (String module : modules) {
            File d = new File(new File(nball, module), "external");
            Set<String> cvsFiles = findCvsControlledFiles(d);
            Set<String> referencedBinaries = new HashSet<String>();
            for (String n : cvsFiles) {
                if (!n.endsWith("-license.txt")) {
                    continue;
                }
                File f = new File(d, n);
                String path = module + "/external/" + n;
                Map<String,String> headers = new HashMap<String,String>();
                InputStream is = new FileInputStream(f);
                StringBuffer body = new StringBuffer();
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String line;
                    while ((line = r.readLine()) != null && line.length() > 0) {
                        Matcher m = Pattern.compile("([a-zA-Z]+): (.+)").matcher(line);
                        if (!m.matches()) {
                            msg.append("\n" + path + " has a non-header line in the header block: \"" + line + "\"");
                            headers = null;
                            break;
                        }
                        headers.put(m.group(1), m.group(2));
                    }
                    while ((line = r.readLine()) != null) {
                        body.append(line).append('\n');
                    }
                } finally {
                    is.close();
                }
                if (headers == null) {
                    headers = Collections.emptyMap();
                } else if (headers.isEmpty()) {
                    msg.append("\n" + path + " has no headers");
                } else {
                    for (String header : requiredHeaders) {
                        if (!headers.containsKey(header)) {
                            msg.append("\n" + path + " is missing a required header: " + header);
                        }
                    }
                }
                for (String header : headers.keySet()) {
                    if (!requiredHeaders.contains(header) && !optionalHeaders.contains(header)) {
                        msg.append("\n" + path + " has an unrecognized header: " + header);
                        continue;
                    }
                }
                String version = headers.get("Version");
                if (version != null && !n.contains(version)) {
                    msg.append("\n" + path + " does not contain the version " + version + " in its name");
                }
                String license = headers.get("License");
                if (license != null) {
                    if (license.contains("GPL") && !headers.containsKey("Source")) {
                        msg.append("\n" + path + " has a GPL-family license but is missing the Source header");
                    }
                    File licenseFile = new File(licenses, license);
                    if (licenseFile.isFile()) {
                        StringBuffer masterBody = new StringBuffer();
                        is = new FileInputStream(licenseFile);
                        try {
                            BufferedReader r = new BufferedReader(new InputStreamReader(is));
                            int c;
                            while ((c = r.read()) != -1) {
                                masterBody.append((char) c);
                            }
                        } finally {
                            is.close();
                        }
                        String expected = masterBody.toString().replaceAll("[ \n\t]+", " ").trim()
                                .replaceAll("([\\\\\\[\\].^$?*+{}()|])", "\\\\$1")
                                .replaceAll("__[A-Z]__", ".+");
                        String actual = body.toString().replaceAll("[ \n\t]+", " ").trim();
                        if (!actual.matches(expected)) {
                            msg.append("\n" + path + " contains a license body which does not match that in nbbuild/licenses/" + license);
                        }
                    } else {
                        msg.append("\n" + path + " refers to nonexistent nbbuild/licenses/" + license);
                    }
                }
                String source = headers.get("Source");
                if (source != null) {
                    try {
                        new URL(source);
                    } catch (MalformedURLException x) {
                        msg.append("\n" + path + " has malformed Source value: " + source);
                    }
                }
                String files = headers.get("Files");
                if (files != null) {
                    for (String file : files.split("[, ]+")) {
                        referencedBinaries.add(file);
                        if (!cvsFiles.contains(file)) {
                            msg.append("\n" + path + " mentions a nonexistent binary in Files: " + file);
                        }
                    }
                } else {
                    String matchingJar = n.replaceFirst("-license\\.txt$", ".jar");
                    String matchingZip = n.replaceFirst("-license\\.txt$", ".zip");
                    referencedBinaries.add(matchingJar);
                    referencedBinaries.add(matchingZip);
                    if (!cvsFiles.contains(matchingJar) && !cvsFiles.contains(matchingZip)) {
                        msg.append("\n" + path + " has no Files header and no corresponding " + matchingJar + " or " + matchingZip + " could be found");
                    }
                }
            }
            for (String n : cvsFiles) {
                if (!n.endsWith(".jar") && !n.endsWith(".zip")) {
                    continue;
                }
                String path = module + "/external/" + n;
                if (!n.matches(".+[0-9].+")) {
                    msg.append("\n" + path + " does not appear to include a version number");
                }
                if (!referencedBinaries.contains(n)) {
                    msg.append("\n" + path + " is not associated with any license file");
                }
            }
        }
        pseudoTests.put("testLicenses", msg.length() > 0 ? "Some license files have incorrect headers" + msg : null);
    }

    private void testNoStrayThirdPartyBinaries() throws IOException {
        List<String> ignoredPatterns = new ArrayList<String>();
        InputStream is = VerifyLibsAndLicenses.class.getResourceAsStream("ignored-binaries");
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = r.readLine().trim()) != null) {
                if (!line.startsWith("#") && line.length() > 0) {
                    if (line.endsWith("/")) {
                        line += "**";
                    }
                    ignoredPatterns.add(line);
                }
            }
        } finally {
            is.close();
        }
        Set<String> topLevelModules = new TreeSet<String>();
        for (String module : modules) {
            topLevelModules.add(module.replaceFirst("/.+", ""));
        }
        Set<String> violations = new TreeSet<String>();
        for (String module : topLevelModules) {
            findStrayThirdPartyBinaries(new File(nball, module.replace('/', File.separatorChar)), module + "/", violations, ignoredPatterns);
        }
        if (violations.isEmpty()) {
            pseudoTests.put("testNoStrayThirdPartyBinaries", null);
        } else {
            StringBuffer msg = new StringBuffer("Some binaries were found outside of <module>/external/ directories");
            for (String v : violations) {
                msg.append("\n" + v);
            }
            pseudoTests.put("testNoStrayThirdPartyBinaries", msg.toString());
        }
    }
    private void findStrayThirdPartyBinaries(File dir, String prefix, Set<String> violations, List<String> ignoredPatterns) throws IOException {
        for (String n : findCvsControlledFiles(dir)) {
            File f = new File(dir, n);
            if (f.isDirectory()) {
                findStrayThirdPartyBinaries(f, prefix + n + "/", violations, ignoredPatterns);
            } else if (n.matches(".*\\.(jar|zip)")) {
                String path = prefix + n;
                boolean ignored = false;
                for (String pattern : ignoredPatterns) {
                    if (SelectorUtils.matchPath(pattern, path)) {
                        ignored = true;
                        break;
                    }
                }
                if (!ignored && dir.getName().equals("external") && new File(new File(dir.getParentFile(), "nbproject"), "project.xml").isFile()) {
                    ignored = true;
                }
                if (!ignored) {
                    violations.add(path);
                } else {
                    //System.err.println("accepted: " + path);
                }
            }
        }
    }

    static Set<String> findCvsControlledFiles(File dir) throws IOException {
        File efile = new File(new File(dir, "CVS"), "Entries");
        File elfile = new File(new File(dir, "CVS"), "Entries.Log");
        if (!efile.isFile() && !elfile.isFile()) {
            return Collections.emptySet();
        }
	List<File> files = new ArrayList<File>(2);
	if (efile.isFile())
	    files.add(efile);
        if (elfile.isFile())
            files.add(elfile);

        Set<String> entries = new TreeSet<String>();
	for (File f : files){
            Reader r = new FileReader(f);
            try {
                BufferedReader buf = new BufferedReader(r);
                String line;
                while ((line = buf.readLine()) != null) {
                    String[] components = line.split("/");
                    if (components.length > 1) {
                        String n = components[1];
                    	if (new File(dir, n).exists()) {
                            entries.add(n);
                    	}
                    }
            	}
            } finally {
                r.close();
            }
	}
        return entries;
    }

}
