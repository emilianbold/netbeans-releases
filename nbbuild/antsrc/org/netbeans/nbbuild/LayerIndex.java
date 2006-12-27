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
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
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
                        parse(jf.getInputStream(jf.getEntry(layer)), files, cnb);
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
            for (Map.Entry<String,String> entry : files.entrySet()) {
                String path = entry.getKey();
                String cnb = entry.getValue();
                if (cnb == null) {
                    cnb = "";
                }
                String line = String.format("%-" + maxlength + "s %s", cnb, path);
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

    private void parse(InputStream is, final Map<String,String> files, final String cnb) throws Exception {
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
                    register(prefix + n);
                }
            }
            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if (qName.equals("folder")) {
                    prefix = prefix.replaceFirst("[^/]+/$", "");
                }
            }
            @Override
            public InputSource resolveEntity(String pub, String sys) throws IOException, SAXException {
                return new InputSource(new StringReader(""));
            }
        });
    }

}
