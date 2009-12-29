/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Get;
import org.apache.tools.ant.types.FileSet;
import org.netbeans.nbbuild.AutoUpdateCatalogParser.ModuleItem;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AutoUpdate extends Task {
    private List<Modules> modules = new ArrayList<Modules>();
    private FileSet nbmSet;
    private File dir;
    private File cluster;
    private URL catalog;
    private boolean force;

    public void setUpdateCenter(URL u) {
        catalog = u;
    }

    public FileSet createNbms() {
        if (nbmSet != null) {
            throw new BuildException("Just one nbms set allowed");
        }
        nbmSet = new FileSet();
        return nbmSet;
    }

    public void setNBM(File nbm) {
        if (nbmSet != null) {
            throw new BuildException("Just one nbms set allowed");
        }
        nbmSet = new FileSet();
        nbmSet.setDir(nbm.getParentFile());
        nbmSet.setIncludes(nbm.getName());
    }

    public void setInstallDir(File dir) {
        this.dir = dir;
    }

    public void setToDir(File dir) {
        this.cluster = dir;
    }

    /** Forces rewrite even the version of a module is not newer */
    public void setForce(boolean force) {
        this.force = force;
    }

    public Modules createModules() {
        final Modules m = new Modules();
        modules.add(m);
        return m;
    }

    @Override
    public void execute() throws BuildException {
        if ((dir != null) == (cluster != null)) {
            throw new BuildException("Specify either todir or installdir");
        }
        Map<String, ModuleItem> units;
        if (catalog != null) {
            try {
                units = AutoUpdateCatalogParser.getUpdateItems(catalog, catalog, this);
            } catch (IOException ex) {
                throw new BuildException(ex.getMessage(), ex);
            }
        } else {
            if (nbmSet == null) {
                throw new BuildException("Specify updatecenter or list of NBMs");
            }
            DirectoryScanner s = nbmSet.getDirectoryScanner(getProject());
            File basedir = s.getBasedir();
            units = new HashMap<String, ModuleItem>();
            for (String incl : s.getIncludedFiles()) {
                File nbm = new File(basedir, incl);
                try {
                    URL u = new URL("jar:" + nbm.toURI() + "!/Info/info.xml");
                    Map<String, ModuleItem> map;
                    final URL url = nbm.toURI().toURL();
                    map = AutoUpdateCatalogParser.getUpdateItems(u, url, this);
                    assert map.size() == 1;
                    Map.Entry<String,ModuleItem> entry = map.entrySet().iterator().next();
                    units.put(entry.getKey(), entry.getValue().changeDistribution(url));
                } catch (IOException ex) {
                    throw new BuildException(ex);
                }
            }
        }

        Map<String,List<String>> installed;
        if (dir != null) {
            File[] arr = dir.listFiles();
            if (arr == null) {
                throw new BuildException("installdir must be existing directory: " + dir);
            }
            installed = findExistingModules(arr);
        } else {
            installed = findExistingModules(cluster);
        }


        for (ModuleItem uu : units.values()) {
            if (!matches(uu.getCodeName(), uu.targetcluster)) {
                continue;
            }
            log("found module: " + uu, Project.MSG_VERBOSE);
            List<String> info = installed.get(uu.getCodeName());
            if (info != null && !uu.isNewerThan(info.get(0))) {
                log("Version " + info.get(0) + " of " + uu.getCodeName() + " is up to date", Project.MSG_VERBOSE);
                if (!force) {
                    continue;
                }
            }
            if (info == null) {
                log(uu.getCodeName() + " is not present, downloading version " + uu.getSpecVersion(), Project.MSG_INFO);
            } else {
                log("Version " + info.get(0) + " of " + uu.getCodeName() + " needs update to " + uu.getSpecVersion(), Project.MSG_INFO);
            }

            byte[] bytes = new byte[4096];
            File tmp = null;
            boolean delete = false;
            File lastM = null;
            try {
                if (uu.getURL().getProtocol().equals("file")) {
                    try {
                        tmp = new File(uu.getURL().toURI());
                    } catch (URISyntaxException ex) {
                        tmp = null;
                    }
                    if (!tmp.exists()) {
                        tmp = null;
                    }
                }
                final String dash = uu.getCodeName().replace('.', '-');
                if (tmp == null) {
                    tmp = File.createTempFile(dash, ".nbm");
                    tmp.deleteOnExit();
                    delete = true;
                    Get get = new Get();
                    get.setProject(getProject());
                    get.setTaskName("get:" + uu.getCodeName());
                    get.setSrc(uu.getURL());
                    get.setDest(tmp);
                    get.setVerbose(true);
                    get.execute();
                }

                File whereTo = dir != null ? new File(dir, uu.targetcluster) : cluster;
                whereTo.mkdirs();
                lastM = new File(whereTo, ".lastModified");
                lastM.createNewFile();

                if (info != null) {
                    for (int i = 1; i < info.size(); i++) {
                        File oldFile = new File(whereTo, info.get(i).replace('/', File.separatorChar));
                        oldFile.delete();
                    }
                }

                File tracking = new File(new File(whereTo, "update_tracking"), dash + ".xml");
                log("Writing tracking file " + tracking, Project.MSG_VERBOSE);
                tracking.getParentFile().mkdirs();
                OutputStream config = new BufferedOutputStream(new FileOutputStream(tracking));
                config.write(("<?xml version='1.0' encoding='UTF-8'?>\n" +
                    "<module codename='" + uu.getCodeName() + "'>\n").getBytes("UTF-8"));
                config.write(("  <module_version install_time='" + System.currentTimeMillis() + "' last='true' origin='Ant'" +
                        " specification_version='" + uu.getSpecVersion() + "'>\n").getBytes("UTF-8"));

                ZipFile  zf = new ZipFile(tmp);
                Enumeration<? extends ZipEntry> en = zf.entries();
                while (en.hasMoreElements()) {
                    ZipEntry zipEntry = en.nextElement();
                    if (!zipEntry.getName().startsWith("netbeans/")) {
                        continue;
                    }
                    if (zipEntry.getName().endsWith("/")) {
                        continue;
                    }
                    final String relName = zipEntry.getName().substring(9);
                    File trgt = new File(whereTo, relName.replace('/', File.separatorChar));
                    trgt.getParentFile().mkdirs();
                    log("Writing " + trgt, Project.MSG_VERBOSE);

                    InputStream is = zf.getInputStream(zipEntry);
                    OutputStream os = new FileOutputStream(trgt);
                    CRC32 crc = new CRC32();
                    for (;;) {
                        int len = is.read(bytes);
                        if (len == -1) {
                            break;
                        }
                        crc.update(bytes, 0, len);
                        os.write(bytes, 0, len);
                    }
                    is.close();
                    os.close();
                    config.write(("    <file crc='" + crc.getValue() + "' name='" + relName + "'/>\n").getBytes("UTF-8"));
                }
                config.write("  </module_version>\n</module>\n".getBytes("UTF-8"));
                config.close();
            } catch (IOException ex) {
                throw new BuildException(ex);
            } finally {
                if (delete && tmp != null) {
                    tmp.delete();
                }
                if (lastM != null) {
                    lastM.setLastModified(System.currentTimeMillis());
                }
            }
        }
    }

    private boolean matches(String cnb, String targetCluster) {
        for (Modules ps : modules) {
            if (ps.clusters != null) {
                if (targetCluster == null) {
                    continue;
                }
                if (!ps.clusters.matcher(targetCluster).matches()) {
                    continue;
                }
            }

            if (ps.pattern.matcher(cnb).matches()) {
                return true;
            }
        }
        return false;
    }

    private Map<String,List<String>> findExistingModules(File... clusters) {
        Map<String,List<String>> all = new HashMap<String, List<String>>();
        for (File c : clusters) {
            File mc = new File(c, "update_tracking");
            final File[] arr = mc.listFiles();
            if (arr == null) {
                continue;
            }
            for (File m : arr) {
                try {
                    parseVersion(m, all);
                } catch (Exception ex) {
                    log("Cannot parse " + m, ex, Project.MSG_WARN);
                }
            }
        }
        return all;
    }

    private void parseVersion(final File config, final Map<String,List<String>> toAdd) throws Exception {
        class P extends DefaultHandler {
            String name;
            List<String> arr;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if ("module".equals(qName)) {
                    name = attributes.getValue("codename");
                    int slash = name.indexOf('/');
                    if (slash > 0) {
                        name = name.substring(0, slash);
                    }
                    return;
                }
                if ("module_version".equals(qName)) {
                    String version = attributes.getValue("specification_version");
                    if (name == null || version == null) {
                        throw new BuildException("Cannot find version in " + config);
                    }
                    arr = new ArrayList<String>();
                    arr.add(version);
                    toAdd.put(name, arr);
                    return;
                }
                if ("file".equals(qName)) {
                    arr.add(attributes.getValue("name"));
                }
            }

            @Override
            public InputSource resolveEntity(String string, String string1) throws IOException, SAXException {
                return new InputSource(new ByteArrayInputStream(new byte[0]));
            }
        }
        P p = new P();
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(config, p);
    }

    public static final class Modules {
        Pattern pattern;
        Pattern clusters;

        public void setIncludes(String regExp) {
            pattern = Pattern.compile(regExp);
        }

        public void setClusters(String regExp) {
            clusters = Pattern.compile(regExp);
        }
    }
}
