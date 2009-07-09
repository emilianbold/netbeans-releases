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


package org.netbeans.modules.maven.format.checkstyle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author mkleint
 */
@org.netbeans.spi.project.ProjectServiceProvider(projectType="org-netbeans-modules-maven", service=AuxiliaryProperties.class)
public class AuxPropsImpl implements AuxiliaryProperties, PropertyChangeListener {
    public static final String PROP_ENABLE = "enable";
    private final Project project;

    private Properties cache;
    private boolean recheck = true;
    private List<String> defaults = new ArrayList<String>();

    public AuxPropsImpl(Project prj) {
        this.project = prj;
        defaults.add("config/sun_checks.xml");
        defaults.add("config/maven_checks.xml");
        defaults.add("config/avalon_checks.xml");
        defaults.add("config/turbine_checks.xml");
        if (NbPreferences.forModule(AuxPropsImpl.class).getBoolean(PROP_ENABLE, true)) {
            NbMavenProject.addPropertyChangeListener(prj, this);
        }
    }

    private FileObject copyToCacheDir(FileObject fo) throws IOException {
        CacheDirectoryProvider prov = project.getLookup().lookup(CacheDirectoryProvider.class);
        return FileUtil.copyFile(fo, prov.getCacheDirectory(), "checkstyle-checker", "xml");
    }

    private FileObject copyToCacheDir(InputStream in) throws IOException {
        CacheDirectoryProvider prov = project.getLookup().lookup(CacheDirectoryProvider.class);
        FileObject file = prov.getCacheDirectory().getFileObject("checkstyle-checker", "xml");
        if (file == null) {
            file = prov.getCacheDirectory().createData("checkstyle-checker", "xml");
        }
        FileUtil.copy(in, file.getOutputStream());
        return file;
    }

    private Properties convert() {
        try {
            CacheDirectoryProvider prov = project.getLookup().lookup(CacheDirectoryProvider.class);
            FileObject cachedFile = prov.getCacheDirectory().getFileObject("checkstyle-checker", "xml");
            boolean hasCached = cachedFile != null && cache != null;
            ModuleConvertor mc = new ModuleConvertor();
            FileObject fo = project.getProjectDirectory().getFileObject("target/checkstyle-checker.xml");
            if (fo != null) {
                //somehow check that the cached file is same as the output dir one..
                if (hasCached && cachedFile.getSize() == fo.getSize()) {
                    return cache;
                } else {
                    // no cached file or the current one is different..
                    fo = copyToCacheDir(fo);
                }
            } else {
                if (hasCached && cachedFile.lastModified().after(project.getProjectDirectory().getFileObject("pom.xml").lastModified())) {
                    //sort of simplistic
                    return cache;
                } else {
                    //TODO we should also check the plugin's classpath for the file.
                    String loc = PluginPropertyUtils.getReportPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_CHECKSTYLE, "configLocation", null);
                    if (loc == null && definesCheckStyle(project)) {
                        loc = "config/sun_checks.xml"; //this is the default NOI18N
                    }
                    if (loc != null && defaults.contains(loc)) {
                        InputStream in = getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/maven/format/checkstyle/" + loc);
                        fo = copyToCacheDir(in);
                    } else if (loc != null) {
                        //find in local fs
                        File file = FileUtilities.resolveFilePath(FileUtil.toFile(project.getProjectDirectory()), loc);
                        if (file != null && file.exists()) {
                            fo = copyToCacheDir(FileUtil.toFileObject(file));
                        } else {
                            //try downloading url
                            URL url = new URL(loc);
                            fo = copyToCacheDir(url.openStream());
                        }
                    }
                }
            }
            if (fo != null) {
                return mc.convert(FileUtil.toFile(fo));
            }
        } catch (IOException io) {
            Exceptions.printStackTrace(io);
        }
        return new Properties();
    }

    static boolean definesCheckStyle(Project prj) {
        NbMavenProject project = prj.getLookup().lookup(NbMavenProject.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return definesCheckStyle(project.getMavenProject());
    }

    static boolean definesCheckStyle(MavenProject prj) {
        if (prj.getReportPlugins() != null) {
            for (Object obj : prj.getReportPlugins()) {
                ReportPlugin plug = (ReportPlugin) obj;
                if (Constants.GROUP_APACHE_PLUGINS.equals(plug.getGroupId()) &&
                        Constants.PLUGIN_CHECKSTYLE.equals(plug.getArtifactId())) { //NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    synchronized Properties getCache() {
        if (cache == null || recheck) {
            if (NbPreferences.forModule(AuxPropsImpl.class).getBoolean(PROP_ENABLE, true)) {
                cache = convert();
            } else {
                cache = new Properties();
            }
            recheck = false;
        }
        return cache;
    }

    public String get(String key, boolean shared) {
        if (shared) {
            return getCache().getProperty(key);
        }
        return null;
    }

    public void put(String key, String value, boolean shared) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @SuppressWarnings("unchecked")
    public Iterable<String> listKeys(boolean shared) {
        if (shared) {
            List<String> str = new ArrayList<String>();
            for (Object k : getCache().keySet()) {
                str.add((String)k);
            }
            return str;
        }
        return new ArrayList<String>();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
            synchronized (this) {
                recheck = true;
            }
        }
    }

}
