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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Task to scan all XML layers in a NB installation
 * and report on which modules registers which files.
 * @author Jesse Glick
 */
public class LayerIndex extends Task {

    public LayerIndex() {}

    List<FileSet> filesets = new ArrayList<FileSet>();
    public void addConfiguredModules(FileSet fs) {
        filesets.add(fs);
    }

    private File output;
    public void setOutput(File f) {
        output = f;
    }

    @Override
    public void execute() throws BuildException {
        if (filesets.isEmpty()) {
            throw new BuildException();
        }
        SortedMap<String,String> files = new TreeMap<String,String>(); // layer path -> cnb
        SortedMap<String,SortedMap<String,String>> labels = new TreeMap<String,SortedMap<String,String>>(); // layer path -> cnb -> label
        final Map<String,Integer> positions = new TreeMap<String,Integer>(); // layer path -> position
        for (FileSet fs : filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File basedir = ds.getBasedir();
            for (String path : ds.getIncludedFiles()) {
                File jar = new File(basedir, path);
                try {
                    JarFile jf = new JarFile(jar);
                    try {
                        Manifest mf = jf.getManifest();
                        if (mf == null) {
                            continue;
                        }
                        String modname = mf.getMainAttributes().getValue("OpenIDE-Module");
                        if (modname == null) {
                            continue;
                        }
                        String cnb = modname.replaceFirst("/\\d+$", "");
                        String layer = mf.getMainAttributes().getValue("OpenIDE-Module-Layer");
                        if (layer == null) {
                            continue;
                        }
                        parse(jf.getInputStream(jf.getEntry(layer)), files, labels, positions, cnb, jf);
                    } finally {
                        jf.close();
                    }
                } catch (Exception x) {
                    throw new BuildException("Reading " + jar + ": " + x, x, getLocation());
                }
            }
        }
        int maxlength = 0;
        for (String cnb : files.values()) {
            if (cnb != null) {
                maxlength = Math.max(maxlength, cnb.length());
            }
        }
        try {
            PrintWriter pw = output != null ? new PrintWriter(output) : null;
            SortedSet<String> layerPaths = new TreeSet<String>(new Comparator<String>() {
                public int compare(String p1, String p2) {
                    StringTokenizer tok1 = new StringTokenizer(p1, "/");
                    StringTokenizer tok2 = new StringTokenizer(p2, "/");
                    String prefix = "";
                    while (tok1.hasMoreTokens()) {
                        String piece1 = tok1.nextToken();
                        if (tok2.hasMoreTokens()) {
                            String piece2 = tok2.nextToken();
                            if (piece1.equals(piece2)) {
                                prefix += piece1 + "/";
                            } else {
                                Integer pos1 = pos(prefix + piece1);
                                Integer pos2 = pos(prefix + piece2);
                                if (pos1 == null) {
                                    if (pos2 == null) {
                                        return piece1.compareTo(piece2);
                                    } else {
                                        return 1;
                                    }
                                } else {
                                    if (pos2 == null) {
                                        return -1;
                                    } else {
                                        int diff = pos1 - pos2;
                                        if (diff != 0) {
                                            return diff;
                                        } else {
                                            return piece1.compareTo(piece2);
                                        }
                                    }
                                }
                            }
                        } else {
                            return 1;
                        }
                    }
                    if (tok2.hasMoreTokens()) {
                        return -1;
                    }
                    assert p1.equals(p2) : p1 + " vs. " + p2;
                    return 0;
                }
                Integer pos(String path) {
                    return positions.containsKey(path) ? positions.get(path) : positions.get(path + "/");
                }
            });
            layerPaths.addAll(files.keySet());
            SortedSet<String> remaining = new TreeSet<String>(files.keySet());
            remaining.removeAll(layerPaths);
            assert remaining.isEmpty() : remaining;
            for (String path : layerPaths) {
                String cnb = files.get(path);
                if (cnb == null) {
                    cnb = "";
                }
                String line = String.format("%-" + maxlength + "s %s", cnb, path);
                SortedMap<String,String> cnb2Label = labels.get(path);
                if (cnb2Label != null) {
                    if (cnb2Label.size() == 1 && cnb2Label.keySet().iterator().next().equals(cnb)) {
                        line += String.format(" (\"%s\")", cnb2Label.values().iterator().next());
                    } else {
                        for (Map.Entry<String,String> labelEntry : cnb2Label.entrySet()) {
                            line += String.format(" (%s: \"%s\")", labelEntry.getKey(), labelEntry.getValue());
                        }
                    }
                }
                Integer pos = positions.get(path);
                if (pos != null) {
                    line += String.format(" (position #%d)", pos);
                }
                if (pw != null) {
                    pw.println(line);
                } else {
                    log(line);
                }
            }
            if (pw != null) {
                pw.close();
            }
        } catch (FileNotFoundException x) {
            throw new BuildException(x, getLocation());
        }
        if (output != null) {
            log(output + ": layer index written");
        }
    }

    private void parse(InputStream is, final Map<String,String> files, final SortedMap<String,SortedMap<String,String>> labels,
            final Map<String,Integer> positions, final String cnb, final JarFile jf) throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setValidating(false);
        f.setNamespaceAware(false);
        f.newSAXParser().parse(is, new DefaultHandler() {
            String prefix = "";
            void register(String path) {
                if (files.containsKey(path)) {
                    files.put(path, null); // >1 owner
                } else {
                    files.put(path, cnb);
                }
            }
            @Override
            public void startElement(String uri, String localName, String qName,  Attributes attributes) throws SAXException {
                if (qName.equals("folder")) {
                    String n = attributes.getValue("name");
                    prefix += n + "/";
                    register(prefix);
                } else if (qName.equals("file")) {
                    String n = attributes.getValue("name");
                    prefix += n;
                    register(prefix);
                } else if (qName.equals("attr") && attributes.getValue("name").equals("SystemFileSystem.localizingBundle")) {
                    String bundlepath = attributes.getValue("stringvalue").replace('.', '/') + ".properties";
                    Properties props = new Properties();
                    try {
                        ZipEntry entry = jf.getEntry(bundlepath);
                        if (entry == null) {
                            log(bundlepath + " not found in reference from " + prefix + " in " + cnb, Project.MSG_WARN);
                            return;
                        }
                        props.load(jf.getInputStream(entry));
                    } catch (IOException x) {
                        throw new SAXException(x);
                    }
                    String key = prefix.replaceAll("/$", "");
                    String label = props.getProperty(key);
                    if (label == null) {
                        log("Key " + key + " not found in " + bundlepath + " from " + cnb, Project.MSG_WARN);
                        return;
                    }
                    SortedMap<String,String> cnb2label = labels.get(prefix);
                    if (cnb2label == null) {
                        cnb2label = new TreeMap<String,String>();
                        labels.put(prefix, cnb2label);
                    }
                    cnb2label.put(cnb, label);
                } else if (qName.equals("attr") && attributes.getValue("name").equals("position")) {
                    String intvalue = attributes.getValue("intvalue");
                    if (intvalue != null && /* #107550 */ !intvalue.equals("0")) {
                        try {
                            positions.put(prefix, Integer.parseInt(intvalue));
                        } catch (NumberFormatException x) {
                            throw new SAXException(x);
                        }
                    }
                }
            }
            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if (qName.equals("folder")) {
                    prefix = prefix.replaceFirst("[^/]+/$", "");
                } else if (qName.equals("file")) {
                    prefix = prefix.replaceFirst("[^/]+$", "");
                }
            }
            @Override
            public InputSource resolveEntity(String pub, String sys) throws IOException, SAXException {
                return new InputSource(new StringReader(""));
            }
        });
    }

}
