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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.filters.LineContainsRegExp;
import org.apache.tools.ant.taskdefs.Concat;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.RegularExpression;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.StringResource;
import org.apache.tools.ant.types.resources.ZipResource;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.zip.ZipEntry;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Extracts icons and bundles from layer.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class ExtractLayer extends Task
implements FileNameMapper {
    private List<FileSet> moduleSet = new ArrayList<FileSet>();
    public void addConfiguredModules(FileSet fs) {
        moduleSet.add(fs);
    }
    private List<FileSet> entries = new ArrayList<FileSet>();
    public void addConfiguredEntries(FileSet fs) {
        entries.add(fs);
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
    private String clusterName;
    public void setClusterName(String n) {
        clusterName = n;
    }
    private FilterChain bundleFilter;
    public void addConfiguredBundleFilter(FilterChain b) {
        bundleFilter = b;
    }

    private File badgeFile;
    public void setBadgeIcon(File f) {
        badgeFile = f;
    }

    @Override
    public void execute() throws BuildException {
        if (moduleSet.isEmpty()) {
            throw new BuildException();
        }
        if (layer == null) {
            throw new BuildException();
        }
        if (output == null) {
            throw new BuildException();
        }
        if (clusterName == null) {
            throw new BuildException();
        }
        BufferedImage badgeIcon;
        try {
            badgeIcon = badgeFile == null ? null : ImageIO.read(badgeFile);
        } catch (IOException ex) {
            throw new BuildException("Error reading " + badgeFile, ex);
        }

        Pattern concatPattern;
        Pattern copyPattern;
        RegularExpression linePattern = new RegularExpression();
        try {
            Set<String> concatregs = new TreeSet<String>();
            Set<String> copyregs = new TreeSet<String>();
            Set<String> keys = new TreeSet<String>();
            parse(layer, concatregs, copyregs, keys);

            log("Concats: " + concatregs, Project.MSG_VERBOSE);
            log("Copies : " + copyregs, Project.MSG_VERBOSE);

            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (String s : concatregs) {
                sb.append(sep);
                sb.append(s);
                sep = "|";
            }
            concatPattern = Pattern.compile(sb.toString());

            sb = new StringBuilder();
            sep = "";
            for (String s : copyregs) {
                sb.append(sep);
                sb.append(s);
                sep = "|";
            }
            copyPattern = Pattern.compile(sb.toString());

            sb = new StringBuilder();
            sep = "";
            for (String s : keys) {
                sb.append(sep);
                sb.append(s);
                sep = "|";
            }
            linePattern.setPattern("(" + sb + ") *=");
        } catch (Exception ex) {
            throw new BuildException("Cannot parse " + layer, ex);
        }
        Map<String,ResArray> bundles = new HashMap<String,ResArray>();
        bundles.put("", new ResArray());
        ResArray icons = new ResArray();
        StringBuilder modules = new StringBuilder();
        String sep = "\n    ";

        for (FileSet fs : moduleSet) {
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
                        modules.append(sep).append(modname.replaceFirst("/[0-9]+$", ""));
                        sep = ",\\\n    ";
                    } finally {
                        jf.close();
                    }
                } catch (Exception x) {
                    throw new BuildException("Reading " + jar + ": " + x, x, getLocation());
                }
            }
        }
        for (FileSet fs : entries == null ? moduleSet : entries) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File basedir = ds.getBasedir();
            for (String path : ds.getIncludedFiles()) {
                File jar = new File(basedir, path);
                try {
                    JarFile jf = new JarFile(jar);
                    try {
                        Enumeration<JarEntry> en = jf.entries();
                        while (en.hasMoreElements()) {
                            JarEntry je = en.nextElement();
                            if (concatPattern.matcher(je.getName()).matches()) {
                                ZipEntry zipEntry = new ZipEntry(je);
                                String noExt = je.getName().replaceFirst("\\.[^\\.]*$", "");
                                int index = noExt.indexOf("_");
                                String suffix = index == -1 ? "" : noExt.substring(index + 1);
                                ResArray ra = bundles.get(suffix);
                                if (ra == null) {
                                    ra = new ResArray();
                                    bundles.put(suffix, ra);
                                }
                                ra.add(new ZipResource(jar, "UTF-8", zipEntry));
                            }
                            if (copyPattern.matcher(je.getName()).matches()) {
                                ZipEntry zipEntry = new ZipEntry(je);
                                Resource zr = new ZipResource(jar, "UTF-8", zipEntry);
                                if (badgeIcon != null) {
                                    icons.add(new IconResource(zr, badgeIcon));
                                } else {
                                    icons.add(zr);
                                }
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

        for (Map.Entry<String, ResArray> entry : bundles.entrySet()) {
            ResArray ra = entry.getValue();
            
            Concat concat = new Concat();
            concat.setProject(getProject());
            ra.add(new StringResource(""));
            concat.add(ra);
            concat.setDestfile(localeVariant(bundle, entry.getKey()));
            {
                FilterChain ch = new FilterChain();
                LineContainsRegExp filter = new LineContainsRegExp();
                filter.addConfiguredRegexp(linePattern);
                ch.addLineContainsRegExp(filter);
                concat.addFilterChain(ch);
                concat.addFilterChain(bundleFilter);
            }
            Concat.TextElement te = new Concat.TextElement();
            te.setProject(getProject());
            te.addText("\n\n\ncnbs=\\" + modules + "\n\n");
            te.setFiltering(false);
            concat.addFooter(te);
            concat.execute();
        }

        {
            HashMap<String,Resource> names = new HashMap<String,Resource>();
            HashSet<String> duplicates = new HashSet<String>();
            for (Resource r : icons) {
                String name = r.getName();
                Resource prev = names.put(name, r);
                if (prev != null) {
                    if (prev.getName().equals(r.getName())) {
                        continue;
                    }
                    duplicates.add(r.getName());
                    duplicates.add(prev.getName());
                }
            }
            if (!duplicates.isEmpty()) {
                throw new BuildException("Duplicated resources are forbidden: " + duplicates.toString().replace(',', '\n'));
            }
        }

        Copy copy = new Copy();
        copy.setProject(getProject());
        copy.add(icons);
        copy.setTodir(output);
        copy.add(this);
        copy.execute();

        try {
            URL u = ExtractLayer.class.getResource("relative-refs.xsl");
            StreamSource xslt = new StreamSource(u.openStream());

            TransformerFactory fack = TransformerFactory.newInstance();
            Transformer t = fack.newTransformer(xslt);
            t.setParameter("cluster.name", clusterName);

            StreamSource orig = new StreamSource(layer);
            StreamResult gen = new StreamResult(new File(output, "layer.xml"));
            t.transform(orig, gen);
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }


    private void parse(
        final File file,
        final Set<String> concat, final Set<String> copy,
        final Set<String> additionalKeys
    ) throws Exception {
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
                    addResource(attributes.getValue("url"), true);
                    prefix += n;
                } else if (qName.equals("attr")) {
                    String name = attributes.getValue("name");
                    if (name.equals("SystemFileSystem.localizingBundle")) {
                        String bundlepath = attributes.getValue("stringvalue").replace('.', '/') + ".*properties";
                        concat.add(bundlepath);
                        if (prefix.endsWith("/")) {
                            additionalKeys.add(prefix.substring(0, prefix.length() - 1));
                        } else {
                            additionalKeys.add(prefix);
                        }
                    } else if (name.equals("iconResource") || name.equals("iconBase")) {
                        String s = attributes.getValue("stringvalue");
                        if (s == null) {
                            throw new BuildException("No stringvalue attribute for " + name);
                        }
                        addResource("nbresloc:" + s, false);
                    } else if (attributes.getValue("bundlevalue") != null) {
                        String bundlevalue = attributes.getValue("bundlevalue");
                        int idx = bundlevalue.indexOf('#');
                        String bundle = bundlevalue.substring(0, idx);
                        String key = bundlevalue.substring(idx + 1);
                        String bundlepath = bundle.replace('.', '/') + ".*properties";
                        concat.add(bundlepath);
                        additionalKeys.add(key);
                    } else {
                        addResource(attributes.getValue("urlvalue"), false);
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

            private void addResource(String url, boolean localAllowed) throws BuildException {
                if (url == null) {
                    return;
                }
                if (url.startsWith("nbres:")) {
                    url = "nbresloc:" + url.substring(6);
                }
                final String prfx = "nbresloc:";
                if (!url.startsWith(prfx)) {
                    if (localAllowed) {
                        copy.add(".*/" + url);
                        return;
                    } else {
                        throw new BuildException("Unknown urlvalue in " + file + " was: " + url);
                    }
                } else {
                    url = url.substring(prfx.length());
                    if (url.startsWith("/")) {
                        url = url.substring(1);
                    }
                }
                url = url.replaceFirst("(\\.[^\\.])+$*", ".*$1");
                copy.add(url);
            }
        });
    }

    private static File localeVariant(File base, String locale) {
        if (locale.length() == 0) {
            return base;
        }
        String name = base.getName().replaceFirst("\\.", "_" + locale + ".");
        return new File(base.getParentFile(), name);
    }

    public void setFrom(String arg0) {
    }

    public void setTo(String arg0) {
    }

    /** Dash instead of slash file mapper */
    public String[] mapFileName(String fileName) {
        return new String[] { fileName.replace('/', '-') };
    }

    private static final class ResArray extends ArrayList<Resource>
    implements ResourceCollection {
        public boolean isFilesystemOnly() {
            return false;
        }
    }
}
