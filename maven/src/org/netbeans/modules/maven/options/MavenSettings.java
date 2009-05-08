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

package org.netbeans.modules.maven.options;

import hidden.org.codehaus.plexus.util.StringUtils;
import hidden.org.codehaus.plexus.util.cli.Arg;
import hidden.org.codehaus.plexus.util.cli.CommandLineException;
import hidden.org.codehaus.plexus.util.cli.CommandLineUtils;
import hidden.org.codehaus.plexus.util.cli.Commandline;
import hidden.org.codehaus.plexus.util.cli.StreamConsumer;
import java.io.File;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 * a netbeans settings for global options that cannot be put into the settings file.
 * @author mkleint
 */
public class MavenSettings  {
    public static final String PROP_DEFAULT_OPTIONS = "defaultOptions"; // NOI18N
    public static final String PROP_SYNCH_PROXY = "synchronizeProxySettings"; //NOI18N
    public static final String PROP_USE_COMMANDLINE = "useCommandLineMaven"; //NOI18N
    public static final String PROP_COMMANDLINE_PATH = "commandLineMavenPath"; //NOI18N
    public static final String PROP_SHOW_RUN_DIALOG = "showRunDialog"; //NOI18N
    public static final String PROP_SOURCE_DOWNLOAD = "sourceDownload"; //NOI18N
    public static final String PROP_JAVADOC_DOWNLOAD = "javadocDownload"; //NOI18N
    public static final String PROP_BINARY_DOWNLOAD = "binaryDownload"; //NOI18N
    public static final String PROP_LAST_ARCHETYPE_GROUPID = "lastArchetypeGroupId"; //NOI18N
    public static final String PROP_CUSTOM_LOCAL_REPOSITORY = "localRepository"; //NOI18N
    public static final String PROP_SKIP_TESTS = "skipTests"; //NOI18N

    
    private static final MavenSettings INSTANCE = new MavenSettings();
    
    public static MavenSettings getDefault() {
        return INSTANCE;
    }

    public boolean isInteractive() {
        return !hasOption("--batch", "-B");
    }

    public Boolean isOffline() {
        if (hasOption("--offline", "-o")) {
            return Boolean.TRUE;
        }
        return null;
    }

    public boolean isShowDebug() {
        return hasOption("--debug", "-X");
    }

    public boolean isShowErrors() {
        return hasOption("--errors", "-e");
    }

    public boolean isUpdateSnapshots() {
        return hasOption("--update-snapshots", "-U");
    }


    public boolean hasOption(String longName, String shortName) {
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

    
    protected final Preferences getPreferences() {
        return NbPreferences.forModule(MavenSettings.class);
    }
    
    protected final String putProperty(String key, String value) {
        String retval = getProperty(key);
        if (value != null) {
            getPreferences().put(key, value);
        } else {
            getPreferences().remove(key);
        }
        return retval;
    }

    protected final String getProperty(String key) {
        return getPreferences().get(key, null);
    }    
    
    private MavenSettings() {
    }

    public String getDefaultOptions() {
        return getPreferences().get(PROP_DEFAULT_OPTIONS, ""); //NOI18N
    }

    public void setDefaultOptions(String options) {
        putProperty(PROP_DEFAULT_OPTIONS, options);
    }
    

    public String getLastArchetypeGroupId() {
        return getPreferences().get(PROP_LAST_ARCHETYPE_GROUPID, "com.mycompany"); //NOI18N
    }

    public void setLastArchetypeGroupId(String groupId) {
        putProperty(PROP_LAST_ARCHETYPE_GROUPID, groupId);
    }

    
    public void setSynchronizeProxy(boolean sync) {
        getPreferences().putBoolean(PROP_SYNCH_PROXY, sync);
    }
    
    public boolean isSynchronizeProxy() {
        return getPreferences().getBoolean(PROP_SYNCH_PROXY, true);
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
    
    public File getCommandLinePath() {
        String str =  getPreferences().get(PROP_COMMANDLINE_PATH, null);
        if (str != null) {
            return FileUtil.normalizeFile(new File(str));
        }
        return null;
    }

    public void setCommandLinePath(File path) {
        if (path == null) {
            getPreferences().remove(PROP_COMMANDLINE_PATH);
        } else {
            putProperty(PROP_COMMANDLINE_PATH, FileUtil.normalizeFile(path).getAbsolutePath());
        }
    }
    
    public boolean isShowRunDialog(){
     return getPreferences().getBoolean(PROP_SHOW_RUN_DIALOG, false);
    }
    public void setShowRunDialog(boolean  b){
      getPreferences().putBoolean(PROP_SHOW_RUN_DIALOG, b);
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

    private static Boolean cachedMaven = null;
    
    public static boolean canFindExternalMaven() {
        File home = MavenSettings.getDefault().getCommandLinePath();
        String ex = Utilities.isWindows() ? "mvn.bat" : "mvn"; //NOI18N
        if (home != null && home.exists()) {
            File bin = new File(home, "bin" + File.separator + ex);//NOI18N
            if (bin.exists()) {
                return true;
            }
        }
        if (cachedMaven != null) {
            return cachedMaven.booleanValue();
        }
        Commandline cmdline = new Commandline();
        cmdline.setExecutable(ex);
        Arg arg = cmdline.createArg();
        arg.setValue("--version"); //NOI18N
        cmdline.addArg(arg);
        RegExpConsumer cons = new RegExpConsumer();
        try {
            int ret = CommandLineUtils.executeCommandLine(cmdline, cons, cons);
            cachedMaven = cons.hasMavenAround;
            return cons.hasMavenAround;
        } catch (CommandLineException ex1) {
            Exceptions.printStackTrace(ex1);
            cachedMaven = false;
            return false;
        }
    }
    
    static String getDefaultMavenInstanceVersion() {
        String ex = Utilities.isWindows() ? "mvn.bat" : "mvn"; //NOI18N
        return getMavenVersion(ex);
    }

    private static String getMavenVersion(String ex) {
        Commandline cmdline = new Commandline();
        cmdline.setExecutable(ex);
        Arg arg = cmdline.createArg();
        arg.setValue("--version"); //NOI18N
        cmdline.addArg(arg);
        RegExpConsumer cons = new RegExpConsumer();
        try {
            int ret = CommandLineUtils.executeCommandLine(cmdline, cons, cons);
            return cons.version != null ? cons.version.trim() : null;
        } catch (CommandLineException ex1) {
            Exceptions.printStackTrace(ex1);
            return null;
        }
        
    }

    public static String getCommandLineMavenVersion() {
        File path = getDefault().getCommandLinePath();
        if (path == null) {
            return getDefaultMavenInstanceVersion();
        }
        String pathString = path.getAbsolutePath() + File.separator + "bin" + File.separator + (Utilities.isWindows() ? "mvn.bat" : "mvn"); //NOI18N
        String ver = getMavenVersion(pathString);
        if (ver != null) {
            return ver;
        }
        //TODO examine the version's lib folder and the prop file in there.. see SettingsPanel.java
        return null;
    }
    
    private static class RegExpConsumer implements StreamConsumer {

        private static final Pattern PATTERN = Pattern.compile("^Maven version:(.*)");
        private static final Pattern PATTERN_210 = Pattern.compile("^Apache Maven ([0-9\\.]*) .*");
        boolean hasMavenAround = false;
        String version = null;

        public void consumeLine(String line) {
            Matcher match = PATTERN.matcher(line);
            if (!match.matches()) {
                match = PATTERN_210.matcher(line);
            }
            if (match.matches()) {
                hasMavenAround = true;
                version = match.group(1);
            }
        }
    };

    
}
