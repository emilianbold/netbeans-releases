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

package org.netbeans.api.autoupdate.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Get;
import org.netbeans.api.autoupdate.ant.AutoupdateCatalogParser.ModuleItem;

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

        // no userdir
        Map<String, ModuleItem> units = AutoupdateCatalogParser.getUpdateItems(catalog, catalog);
        for (ModuleItem uu : units.values()) {
            log("found module: " + uu, Project.MSG_VERBOSE);
            if (!matches(uu.getCodeName())) {
                continue;
            }

            byte[] bytes = new byte[4096];
            try {
                File f = File.createTempFile(uu.getCodeName().replace('.', '-'), ".nbm");
                f.deleteOnExit();
                Get get = new Get();
                get.setProject(getProject());
                get.setTaskName("get:" + uu.getCodeName());
                get.setSrc(uu.getURL());
                get.setDest(f);
                get.execute();

                File cluster = new File(dir, uu.targetcluster);

                ZipFile zf = new ZipFile(f);
                Enumeration<? extends ZipEntry> en = zf.entries();
                while (en.hasMoreElements()) {
                    ZipEntry zipEntry = en.nextElement();
                    if (!zipEntry.getName().startsWith("netbeans/")) {
                        continue;
                    }
                    File trgt = new File(cluster, zipEntry.getName().substring(9).replace('/', File.separatorChar));
                    trgt.getParentFile().mkdirs();
                    log("Writing " + trgt, Project.MSG_VERBOSE);

                    InputStream is = zf.getInputStream(zipEntry);
                    OutputStream os = new FileOutputStream(trgt);
                    for (;;) {
                        int len = is.read(bytes);
                        if (len == -1) {
                            break;
                        }
                        os.write(bytes, 0, len);
                    }
                }
                
            } catch (IOException ex) {
                throw new BuildException(ex);
            }
        }
    }

    private boolean matches(String cnb) {
        for (Modules ps : modules) {
            if (ps.pattern.matcher(cnb).matches()) {
                return true;
            }
        }
        return false;
    }

    public static final class Modules {
        Pattern pattern;

        public void setIncludes(String regExp) {
            pattern = Pattern.compile(regExp);
        }
    }
}
