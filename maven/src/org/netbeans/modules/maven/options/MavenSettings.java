/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.maven.options;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.apache.maven.execution.MavenExecutionRequest;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbPreferences;

/**
 * a netbeans settings for global options that cannot be put into the settings file.
 * @author mkleint
 */
public class MavenSettings  {
    private static final String PROP_DEFAULT_OPTIONS = "defaultOptions"; // NOI18N
    private static final String PROP_COMMANDLINE_PATH = "commandLineMavenPath"; //NOI18N
    private static final String PROP_SOURCE_DOWNLOAD = "sourceDownload"; //NOI18N
    private static final String PROP_JAVADOC_DOWNLOAD = "javadocDownload"; //NOI18N
    private static final String PROP_BINARY_DOWNLOAD = "binaryDownload"; //NOI18N
    private static final String PROP_LAST_ARCHETYPE_GROUPID = "lastArchetypeGroupId"; //NOI18N
    private static final String PROP_CUSTOM_LOCAL_REPOSITORY = "localRepository"; //NOI18N
    private static final String PROP_SKIP_TESTS = "skipTests"; //NOI18N
    private static final String PROP_MAVEN_RUNTIMES = "mavenRuntimes"; //NOI18N

    //these are from former versions (6.5) and are here only for conversion
    private static final String PROP_DEBUG = "showDebug"; // NOI18N
    private static final String PROP_ERRORS = "showErrors"; //NOI18N
    private static final String PROP_CHECKSUM_POLICY = "checksumPolicy"; //NOI18N
    private static final String PROP_PLUGIN_POLICY = "pluginUpdatePolicy"; //NOI18N
    private static final String PROP_FAILURE_BEHAVIOUR = "failureBehaviour"; //NOI18N
    private static final String PROP_USE_REGISTRY = "usePluginRegistry"; //NOI18N
    
    private static final MavenSettings INSTANCE = new MavenSettings();
    
    public static MavenSettings getDefault() {
        return INSTANCE;
    }

    public boolean isInteractive() {
        return !hasOption("--batch", "-B"); //NOI18N
    }

    public Boolean isOffline() {
        if (hasOption("--offline", "-o")) { //NOI18N
            return Boolean.TRUE;
        }
        return null;
    }

    public boolean isShowDebug() {
        return hasOption("--debug", "-X"); //NOI18N
    }

    public boolean isShowErrors() {
        return hasOption("--errors", "-e"); //NOI18N
    }

    public boolean isUpdateSnapshots() {
        return hasOption("--update-snapshots", "-U"); //NOI18N
    }

    private boolean hasOption(String longName, String shortName) {
        String defOpts = getDefaultOptions();
        if (defOpts != null) {
            try {
                String[] strs = CommandLineUtils.translateCommandline(defOpts);
                for (String s : strs) {
                    s = s.trim();
                    if (s.startsWith(shortName) || s.startsWith(longName)) {
                        return true;
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(MavenSettings.class.getName()).fine("Error parsing global options:" + defOpts);
                //will check for contains of -X be enough?
                return defOpts.contains(longName) || defOpts.contains(shortName);
            }
        }
        return false;
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(MavenSettings.class);
    }
    
    private String putProperty(String key, String value) {
        String retval = getProperty(key);
        if (value != null) {
            getPreferences().put(key, value);
        } else {
            getPreferences().remove(key);
        }
        return retval;
    }

    private String getProperty(String key) {
        return getPreferences().get(key, null);
    }    
    
    private MavenSettings() {
        //import from older versions
        String defOpts = getPreferences().get(PROP_DEFAULT_OPTIONS, null);
        if (defOpts == null) {
            defOpts = "";
            //only when not already set by user or by previous import
            String debug = getPreferences().get(PROP_DEBUG, null);
            if (debug != null) {
                boolean val = Boolean.parseBoolean(debug);
                if (val) {
                    defOpts = defOpts + " --debug";//NOI18N
                }
                getPreferences().remove(PROP_DEBUG);
            }
            String error = getPreferences().get(PROP_ERRORS, null);
            if (error != null) {
                boolean val = Boolean.parseBoolean(error);
                if (val) {
                    defOpts = defOpts + " --errors"; //NOI18N
                }
                getPreferences().remove(PROP_ERRORS);
            }
            String checksum = getPreferences().get(PROP_CHECKSUM_POLICY, null);
            if (checksum != null) {
                if (MavenExecutionRequest.CHECKSUM_POLICY_FAIL.equals(checksum)) {
                    defOpts = defOpts + " --strict-checksums";//NOI18N
                } else if (MavenExecutionRequest.CHECKSUM_POLICY_WARN.equals(checksum)) {
                    defOpts = defOpts + " --lax-checksums";//NOI18N
                }
                getPreferences().remove(PROP_CHECKSUM_POLICY);
            }
            String fail = getPreferences().get(PROP_FAILURE_BEHAVIOUR, null);
            if (fail != null) {
                if (MavenExecutionRequest.REACTOR_FAIL_NEVER.equals(fail)) {
                    defOpts = defOpts + " --fail-never";//NOI18N
                } else if (MavenExecutionRequest.REACTOR_FAIL_FAST.equals(fail)) {
                    defOpts = defOpts + " --fail-fast";//NOI18N
                } else if (MavenExecutionRequest.REACTOR_FAIL_AT_END.equals(fail)) {
                    defOpts = defOpts + " --fail-at-end";//NOI18N
                }
                getPreferences().remove(PROP_FAILURE_BEHAVIOUR);
            }
            String pluginUpdate = getPreferences().get(PROP_PLUGIN_POLICY, null);
            if (pluginUpdate != null) {
                if (Boolean.parseBoolean(pluginUpdate)) {
                    defOpts = defOpts + " --check-plugin-updates";//NOI18N
                } else {
                    defOpts = defOpts + " --no-plugin-updates";//NOI18N
                }
                getPreferences().remove(PROP_PLUGIN_POLICY);
            }
            String registry = getPreferences().get(PROP_USE_REGISTRY, null);
            if (registry != null) {
                if (!Boolean.parseBoolean(registry)) {
                    defOpts = defOpts + " --no-plugin-registry";//NOI18N
                }
                getPreferences().remove(PROP_USE_REGISTRY);
            }
            setDefaultOptions(defOpts);
            try {
                getPreferences().flush();
            } catch (BackingStoreException ex) {
//                Exceptions.printStackTrace(ex);
            }
        }
    }

    public String getDefaultOptions() {
        return getPreferences().get(PROP_DEFAULT_OPTIONS, ""); //NOI18N
    }

    public void setDefaultOptions(String options) {
        putProperty(PROP_DEFAULT_OPTIONS, options);
    }
    

    public String getLastArchetypeGroupId() {
        return getPreferences().get(PROP_LAST_ARCHETYPE_GROUPID, Boolean.getBoolean("netbeans.full.hack") ? "test" : "com.mycompany"); //NOI18N
    }

    public void setLastArchetypeGroupId(String groupId) {
        putProperty(PROP_LAST_ARCHETYPE_GROUPID, groupId);
    }

    public void setCustomLocalRepository(String text) {
        if (text != null && text.trim().length() == 0) {
            text = null;
        }
        String oldText = getCustomLocalRepository();
        putProperty(PROP_CUSTOM_LOCAL_REPOSITORY, text);
        //reset the project embedder to use the new local repo value.
        if (!StringUtils.equals(oldText, text)) {
            EmbedderFactory.resetProjectEmbedder();
        }
    }
    
    public String getCustomLocalRepository() {
        return getPreferences().get(PROP_CUSTOM_LOCAL_REPOSITORY, null);
    }
    
    public static File getDefaultMavenHome() {
        return InstalledFileLocator.getDefault().locate("maven", "org.netbeans.modules.maven.embedder", false); // NOI18N
    }
    
    public File getMavenHome() {
        String str =  getPreferences().get(PROP_COMMANDLINE_PATH, null);
        if (str != null) {
            return FileUtil.normalizeFile(new File(str));
        } else {
            return getDefaultMavenHome();
        }
    }

    public void setMavenHome(File path) {
        if (path == null || path.equals(getDefaultMavenHome())) {
            getPreferences().remove(PROP_COMMANDLINE_PATH);
        } else {
            putProperty(PROP_COMMANDLINE_PATH, FileUtil.normalizeFile(path).getAbsolutePath());
        }
    }

    public boolean isSkipTests() {
        return getPreferences().getBoolean(PROP_SKIP_TESTS, false);
    }

    public void setSkipTests(boolean skipped) {
        getPreferences().putBoolean(PROP_SKIP_TESTS, skipped);
    }

    public static enum DownloadStrategy {
        NEVER,
        FIRST_OPEN,
        EVERY_OPEN
    }

    public DownloadStrategy getSourceDownloadStrategy() {
        String val = getPreferences().get(PROP_SOURCE_DOWNLOAD, DownloadStrategy.NEVER.name());
        try {
            return DownloadStrategy.valueOf(val);
        } catch (IllegalArgumentException ex) {
            return DownloadStrategy.NEVER;
        }
    }

    public void setSourceDownloadStrategy(DownloadStrategy ds) {
        if (ds != null) {
            getPreferences().put(PROP_SOURCE_DOWNLOAD, ds.name());
        } else {
            getPreferences().remove(PROP_SOURCE_DOWNLOAD);
        }
    }

    public DownloadStrategy getJavadocDownloadStrategy() {
        String val = getPreferences().get(PROP_JAVADOC_DOWNLOAD, DownloadStrategy.NEVER.name());
        try {
            return DownloadStrategy.valueOf(val);
        } catch (IllegalArgumentException ex) {
            return DownloadStrategy.NEVER;
        }
    }

    public void setJavadocDownloadStrategy(DownloadStrategy ds) {
        if (ds != null) {
            getPreferences().put(PROP_JAVADOC_DOWNLOAD, ds.name());
        } else {
            getPreferences().remove(PROP_JAVADOC_DOWNLOAD);
        }
    }

    public DownloadStrategy getBinaryDownloadStrategy() {
        String val = getPreferences().get(PROP_BINARY_DOWNLOAD, DownloadStrategy.NEVER.name());
        try {
            return DownloadStrategy.valueOf(val);
        } catch (IllegalArgumentException ex) {
            return DownloadStrategy.NEVER;
        }
    }
    
    public void setBinaryDownloadStrategy(DownloadStrategy ds) {
        if (ds != null) {
            getPreferences().put(PROP_BINARY_DOWNLOAD, ds.name());
        } else {
            getPreferences().remove(PROP_BINARY_DOWNLOAD);
        }
    }

    public static @CheckForNull String getCommandLineMavenVersion() {
        return getCommandLineMavenVersion(getDefault().getMavenHome());
    }
    
    public static @CheckForNull String getCommandLineMavenVersion(File mavenHome) {
        File[] jars = new File(mavenHome, "lib").listFiles(new FilenameFilter() { // NOI18N
            public @Override boolean accept(File dir, String name) {
                return name.endsWith(".jar"); // NOI18N
            }
        });
        if (jars == null) {
            return null;
        }
        for (File jar : jars) {
            try {
                // Prefer to use this rather than raw ZipFile since URLMapper since ArchiveURLMapper will cache JARs:
                FileObject entry = URLMapper.findFileObject(new URL(FileUtil.urlForArchiveOrDir(jar), "META-INF/maven/org.apache.maven/maven-core/pom.properties")); // NOI18N
                if (entry != null) {
                    InputStream is = entry.getInputStream();
                    try {
                        Properties properties = new Properties();
                        properties.load(is);
                        return properties.getProperty("version"); // NOI18N
                    } finally {
                        is.close();
                    }
                }
            } catch (IOException x) {
                // ignore for now
            }
        }
        return null;
    }

    private static List<String> searchMavenRuntimes(String[] paths, boolean stopOnFirstValid) {
        List<String> runtimes = new ArrayList<String>();
        for (String path : paths) {
            File file = new File(path);
            path = FileUtil.normalizeFile(file).getAbsolutePath();
            String version = getCommandLineMavenVersion(new File(path));
            if (version != null) {
                runtimes.add(path);
                if (stopOnFirstValid) {
                    break;
                }
            }
        }

        return runtimes;
    }

	/**
	 * Searches for Maven Runtimes by the environment settings and returns the first valid one.
	 *
	 * <p>It searches in this order:
	 * <ul>
	 * <li>MAVEN_HOME</li>
	 * <li>M2_HOME</li>
	 * <li>PATH</li></ul>
	 * </p>
	 * <p>Only the first appereance will be appended.</p>
	 *
	 * @returns the default external Maven runtime on the path.
	 */
    public static String getDefaultExternalMavenRuntime() {
        String paths = System.getenv("PATH"); // NOI18N
        String mavenHome = System.getenv("MAVEN_HOME"); // NOI18N
        String m2Home = System.getenv("M2_HOME"); // NOI18N

        List<String> mavenEnvDirs = new ArrayList<String>();
        if (mavenHome != null) {
            mavenEnvDirs.add(mavenHome);
        }
        if (m2Home != null) {
            mavenEnvDirs.add(m2Home);
        }
        if (paths != null) {
            for (String path : paths.split(File.pathSeparator)) {
                if (!path.endsWith("bin")) { // NOI18N
                    continue;
                }

                mavenEnvDirs.add(path.substring(0,
                        path.length() - "bin".length() - File.pathSeparator.length()));
            }
        }

        List<String> runtimes = searchMavenRuntimes(mavenEnvDirs.toArray(new String[0]), true);
        return !runtimes.isEmpty() ? runtimes.get(0) : null;
    }
    
    public List<String> getUserDefinedMavenRuntimes() {
        List<String> runtimes = new ArrayList<String>();

        String defaultRuntimePath = getDefaultExternalMavenRuntime();
        String runtimesPref = getPreferences().get(PROP_MAVEN_RUNTIMES, null);
        if (runtimesPref != null) {
            for (String runtimePath : runtimesPref.split(File.pathSeparator)) {
                if (!"".equals(runtimePath) && !runtimePath.equals(defaultRuntimePath)) {
                    runtimes.add(runtimePath);
                }
            }
        }

        return Collections.unmodifiableList(runtimes);
    }

    public void setMavenRuntimes(List<String> runtimes) {
        if (runtimes == null) {
            getPreferences().remove(PROP_MAVEN_RUNTIMES);
        } else {
            String runtimesPref = "";
            for (String path : runtimes) {
                runtimesPref += path + File.pathSeparator;
            }
            if (runtimesPref.endsWith(File.pathSeparator)) {
                runtimesPref = runtimesPref.substring(0, runtimesPref.length() - 1);
            }
            putProperty(PROP_MAVEN_RUNTIMES, runtimesPref);
        }
    }
    
}
