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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.nbbuild.JUnitReportWriter;

/**
 * Creates a list of external binaries and their licenses.
 */
public class CreateLicenseSummary extends Task {

    private File nball;
    public void setNball(File nball) {
        this.nball = nball;
    }

    private File build;
    public void setBuild(File build) {
        this.build = build;
    }

    private File summary;
    public void setSummary(File summary) {
        this.summary = summary;
    }

    private File reportFile;
    public void setReport(File report) {
        this.reportFile = report;
    }

    private Map<String,String> pseudoTests;

    public @Override void execute() throws BuildException {
        pseudoTests = new LinkedHashMap<String,String>();
        try {
            Map<Long,String> crc2License = findCrc2LicenseMapping();
            Map<String,Set<String>> license2Binaries = new TreeMap<String,Set<String>>();
            StringBuilder testBinariesAreUnique = new StringBuilder();
            findBinaries(build, license2Binaries, crc2License, new HashMap<Long,String>(), "", testBinariesAreUnique);
            pseudoTests.put("testBinariesAreUnique", testBinariesAreUnique.length() > 0 ? "Some binaries are duplicated" + testBinariesAreUnique : null);
            OutputStream os = new FileOutputStream(summary);
            try {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
                pw.println("DO NOT TRANSLATE OR LOCALIZE.");
                File licenses = new File(new File(nball, "nbbuild"), "licenses");
                for (Map.Entry<String,Set<String>> entry : license2Binaries.entrySet()) {
                    String licenseName = entry.getKey();
                    pw.println();
                    pw.println("=========== " + licenseName + " ===========");
                    for (String binary : entry.getValue()) {
                        File f = new File(build, binary.replace('/', File.separatorChar));
                        MessageDigest digest;
                        try {
                            digest = MessageDigest.getInstance("SHA-1");
                        } catch (NoSuchAlgorithmException x) {
                            throw new BuildException(x, getLocation());
                        }
                        FileInputStream is = new FileInputStream(f);
                        try {
                            digest.update(is.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, f.length()));
                        } finally {
                            is.close();
                        }
                        pw.printf("%s (size: %d; SHA-1: %040X)\n", binary, f.length(), new BigInteger(1, digest.digest()));
                    }
                    pw.println();
                    File license = new File(licenses, licenseName);
                    if (!license.isFile()) {
                        continue;
                    }
                    InputStream is = new FileInputStream(license);
                    try {
                        BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        String line;
                        while ((line = r.readLine()) != null) {
                            pw.println(line);
                        }
                    } finally {
                        is.close();
                    }
                }
                pw.flush();
            } finally {
                os.close();
            }
            log(summary + ": written");
        } catch (IOException x) {
            throw new BuildException(x, getLocation());
        }
        JUnitReportWriter.writeReport(this, reportFile, pseudoTests);
    }

    private Map<Long,String> findCrc2LicenseMapping() throws IOException {
        Map<Long,String> crc2License = new HashMap<Long,String>();
        for (String cluster : getProject().getProperty("nb.clusters.list").split("[, ]+")) {
            for (String module : getProject().getProperty(cluster).split("[, ]+")) {
                File d = new File(new File(nball, module), "external");
                Set<String> cvsFiles = VerifyLibsAndLicenses.findCvsControlledFiles(d);
                Map<String,String> binary2License = findBinary2LicenseMapping(cvsFiles, d);
                for (String n : cvsFiles) {
                    if (!n.endsWith(".jar") && !n.endsWith(".zip")) {
                        continue;
                    }
                    String license = binary2License.get(n);
                    if (license == null) {
                        continue;
                    }
                    File f = new File(d, n);
                    InputStream is = new FileInputStream(f);
                    try {
                        crc2License.put(computeCRC32(is), license);
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
                            is = zf.getInputStream(entry);
                            try {
                                crc2License.put(computeCRC32(is), license);
                            } finally {
                                is.close();
                            }
                        }
                    } finally {
                        zf.close();
                    }
                }
            }
        }
        return crc2License;
    }

    private long computeCRC32(InputStream is) throws IOException {
        byte[] buf = new byte[4096];
        CRC32 crc32 = new CRC32();
        int read;
        while ((read = is.read(buf)) != -1) {
            crc32.update(buf, 0, read);
        }
        return crc32.getValue();
    }

    private Map<String, String> findBinary2LicenseMapping(Set<String> cvsFiles, File d) throws IOException {
        Map<String,String> binary2License = new HashMap<String,String>();
        for (String n : cvsFiles) {
            if (!n.endsWith("-license.txt")) {
                continue;
            }
            Map<String,String> headers = new HashMap<String,String>();
            InputStream is = new FileInputStream(new File(d, n));
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = r.readLine()) != null && line.length() > 0) {
                    Matcher m = Pattern.compile("([a-zA-Z]+): (.+)").matcher(line);
                    if (m.matches()) {
                        headers.put(m.group(1), m.group(2));
                    }
                }
            } finally {
                is.close();
            }
            String license = headers.get("License");
            if (license == null) {
                continue;
            }
            String files = headers.get("Files");
            if (files != null) {
                for (String file : files.split("[, ]+")) {
                    binary2License.put(file, license);
                }
            } else {
                binary2License.put(n.replaceFirst("-license\\.txt$", ".jar"), license);
                binary2License.put(n.replaceFirst("-license\\.txt$", ".zip"), license);
            }
        }
        return binary2License;
    }

    private void findBinaries(File d, Map<String,Set<String>> license2Binaries, Map<Long,String> crc2License,
            Map<Long,String> crc2Binary, String prefix, StringBuilder testBinariesAreUnique) throws IOException {
        for (String n : d.list()) {
            File f = new File(d, n);
            if (f.isDirectory()) {
                findBinaries(f, license2Binaries, crc2License, crc2Binary, prefix + n + "/", testBinariesAreUnique);
            } else if (n.endsWith(".jar") || n.endsWith(".zip")) {
                InputStream is = new FileInputStream(f);
                try {
                    long crc = computeCRC32(is);
                    String license = crc2License.get(crc);
                    if (license != null) {
                        Set<String> binaries = license2Binaries.get(license);
                        if (binaries == null) {
                            binaries = new TreeSet<String>();
                            license2Binaries.put(license, binaries);
                        }
                        String path = prefix + n;
                        binaries.add(path);
                        String otherPath = crc2Binary.put(crc, path);
                        if (otherPath != null) {
                            testBinariesAreUnique.append("\n" + otherPath + " and " + path + " are identical");
                        }
                    }
                } finally {
                    is.close();
                }
            }
        }
    }

}
