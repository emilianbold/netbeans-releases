/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ide.ergonomics.ant;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Concat;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.FilterSet;
import org.apache.tools.ant.types.FilterSet.Filter;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.ZipResource;
import org.apache.tools.ant.util.FlatFileNameMapper;
import org.apache.tools.zip.ZipEntry;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Extracts icons and bundles from layer.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class ExtractLayer extends Task {
    private List<FileSet> filesets = new ArrayList<FileSet>();
    public void addConfiguredModules(FileSet fs) {
        filesets.add(fs);
    }

    private File layer;
    public void setLayer(File f) {
        layer = f;
    }

    private File output;
    public void setDestDir(File f) {
        output = f;
    }
    private File bundle;
    public void setBundle(File f) {
        bundle = f;
    }
    private FilterChain bundleFilter;
    public void addConfiguredBundleFilter(FilterChain b) {
        bundleFilter = b;
    }

    @Override
    public void execute() throws BuildException {
        if (filesets.isEmpty()) {
            throw new BuildException();
        }
        if (layer == null) {
            throw new BuildException();
        }

        Pattern match;
        try {
            Set<String> regexps = new TreeSet<String>();
            parse(layer, regexps);

            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (String s : regexps) {
                sb.append(sep);
                sb.append(s);
                sep = "|";
            }
            match = Pattern.compile(sb.toString());
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
        ZipArray bundles = new ZipArray();

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
                        Enumeration<JarEntry> en = jf.entries();
                        while (en.hasMoreElements()) {
                            JarEntry je = en.nextElement();
                            if (match.matcher(je.getName()).matches()) {
                                ZipEntry zipEntry = new ZipEntry(je);
                                bundles.add(new ZipResource(jar, "UTF-8", zipEntry));
                            }
                        }
                    } finally {
                        jf.close();
                    }
                } catch (Exception x) {
                    throw new BuildException("Reading " + jar + ": " + x, x, getLocation());
                }
            }
        }

        Concat concat = new Concat();
        concat.add(bundles);
        concat.setDestfile(bundle);
        if (bundleFilter != null) {
            concat.addFilterChain(bundleFilter);
        }
        concat.execute();
    }


    private void parse(File file, final Set<String> regexp) throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setValidating(false);
        f.setNamespaceAware(false);
        f.newSAXParser().parse(file, new DefaultHandler() {
            String prefix = "";
            @Override
            public void startElement(String uri, String localName, String qName,  Attributes attributes) throws SAXException {
                if (qName.equals("folder")) {
                    String n = attributes.getValue("name");
                    prefix += n + "/";
                } else if (qName.equals("file")) {
                    String n = attributes.getValue("name");
                    prefix += n;
                } else if (qName.equals("attr") && attributes.getValue("name").equals("SystemFileSystem.localizingBundle")) {
                    String bundlepath = attributes.getValue("stringvalue").replace('.', '/') + ".*properties";
                    regexp.add(bundlepath);
//                    String key = prefix.replaceAll("/$", "");
                } else if (qName.equals("attr") && attributes.getValue("name").equals("SystemFileSystem.localizingIcon")) {
                    String iconpath = attributes.getValue("stringvalue").replace('.', '/') + ".*properties";
                    regexp.add(iconpath);
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
    private static final class ZipArray extends ArrayList<ZipResource>
    implements ResourceCollection {
        public boolean isFilesystemOnly() {
            return false;
        }
    }
}
