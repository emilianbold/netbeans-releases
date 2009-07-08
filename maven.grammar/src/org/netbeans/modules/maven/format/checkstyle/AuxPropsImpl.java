/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import org.netbeans.api.project.Project;
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
                    String loc = PluginPropertyUtils.getReportPluginProperty(project, "org.apache.maven.plugins", "maven-checkstyle-plugin", "configLocation", null);
                    if (loc != null && defaults.contains(loc)) {
                        InputStream in = getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/maven/checkstylebridge/" + loc);
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
