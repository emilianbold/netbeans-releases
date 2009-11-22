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
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Get;
import org.netbeans.nbbuild.AutoupdateCatalogParser.ModuleItem;
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
    private File dir;
    private URL catalog;

    public void setUpdateCenter(URL u) {
        catalog = u;
    }

    public void setNetBeansDestDir(File dir) {
        this.dir = dir;
    }

    public Modules createModules() {
        final Modules m = new Modules();
        modules.add(m);
        return m;
    }

    @Override
    public void execute() throws BuildException {
        File[] arr = dir == null ? null : dir.listFiles();
        if (arr == null) {
            throw new BuildException("netbeans.dest.dir must existing be directory: " + dir);
        }

        Map<String,String> installed = findExistingModules(dir);

        // no userdir
        Map<String, ModuleItem> units = AutoupdateCatalogParser.getUpdateItems(catalog, catalog, this);
        for (ModuleItem uu : units.values()) {
            if (!matches(uu.getCodeName())) {
                continue;
            }
            log("found module: " + uu, Project.MSG_VERBOSE);
            String version = installed.get(uu.getCodeName());
            if (version != null && !uu.isNewerThan(version)) {
                log("Version " + version + " of " + uu.getCodeName() + " is up to date", Project.MSG_VERBOSE);
                continue;
            }
            if (version == null) {
                log(uu.getCodeName() + " is not present, downloading version " + uu.getSpecVersion(), Project.MSG_INFO);
            } else {
                log("Version " + version + " of " + uu.getCodeName() + " needs update to " + uu.getSpecVersion(), Project.MSG_INFO);
            }

            byte[] bytes = new byte[4096];
            try {
                final String dash = uu.getCodeName().replace('.', '-');
                File f = File.createTempFile(dash, ".nbm");
                f.deleteOnExit();
                Get get = new Get();
                get.setProject(getProject());
                get.setTaskName("get:" + uu.getCodeName());
                get.setSrc(uu.getURL());
                get.setDest(f);
                get.setVerbose(true);
                get.execute();

                File cluster = new File(dir, uu.targetcluster);

                File tracking = new File(new File(cluster, "update_tracking"), dash + ".xml");
                tracking.getParentFile().mkdirs();
                OutputStream config = new BufferedOutputStream(new FileOutputStream(tracking));
                config.write(("<?xml version='1.0' encoding='UTF-8'?>\n" +
                    "<module codename='" + uu.getCodeName() + "'>\n").getBytes("UTF-8"));
                config.write(("  <module_version install_time='" + System.currentTimeMillis() + "' last='true' origin='Ant'" +
                        " specification_version='" + uu.getSpecVersion() + "'>\n").getBytes("UTF-8"));

                ZipFile  zf = new ZipFile(f);
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
                    File trgt = new File(cluster, relName.replace('/', File.separatorChar));
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
                    config.write(("<file crc='" + crc.getValue() + "' name='" + relName + "'/>\n").getBytes("UTF-8"));
                }
                config.write("  </module_version>\n</module>\n".getBytes("UTF-8"));
                config.close();
            } catch (IOException ex) {
                throw new BuildException(ex);
            }
        }
    }

    private boolean matches(String cnb) {
        String dash = cnb.replace('.', '-');
        for (Modules ps : modules) {
            if (ps.pattern.matcher(dash).matches()) {
                return true;
            }
        }
        return false;
    }

    private Map<String,String> findExistingModules(File dir) {
        Map<String,String> all = new HashMap<String, String>();
        for (File cluster : dir.listFiles()) {
            File mc = new File(cluster, "update_tracking");
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

    private void parseVersion(final File config, final Map<String,String> toAdd) throws Exception {
        class P extends DefaultHandler {
            String name;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if ("module".equals(qName)) {
                    name = attributes.getValue("codename");
                    return;
                }
                if ("module_version".equals(qName)) {
                    String version = attributes.getValue("specification_version");
                    if (name == null || version == null) {
                        throw new BuildException("Cannot find version in " + config);
                    }
                    toAdd.put(name, version);
                    return;
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

        public void setIncludes(String regExp) {
            pattern = Pattern.compile(regExp);
        }
    }
}
