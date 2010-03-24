/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.versioning.system.cvss;

import java.awt.Color;
import java.util.regex.Pattern;
import java.util.*;
import java.util.prefs.Preferences;
import java.io.File;

import org.openide.util.NbPreferences;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.FileCollection;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.modules.versioning.util.KeyringSupport;

/**
 * Stores CVS module configuration.
 *
 * @author Maros Sandor
 */
public class CvsModuleConfig {
    
    public static final String PROP_IGNORED_FILEPATTERNS    = "ignoredFilePatterns";  // NOI18N
    public static final String PROP_COMMIT_EXCLUSIONS       = "commitExclusions";  // NOI18N
    public static final String PROP_SEARCHHISTORY_FETCHTAGS = "searchHistory.fetchTags";  // NOI18N
    public static final String PROP_EXCLUDE_NEW_FILES = "excludeNewFiles";  // NOI18N
    public static final String ROOTS_CONFIG = "rootsConfig";  // NOI18N
    public static final String PROP_ANNOTATIONS_FORMAT = "annotationsFormat";  // NOI18N
    public static final String PROP_PRUNE_DIRECTORIES = "autoPruneDirectories";  // NOI18N
    private static final String PROP_WRAP_COMMIT_MESSAGE_LENGTH = "wrapCommitMessageLength";  // NOI18N
    
    public static final String DEFAULT_ANNOTATIONS_FORMAT = "[{status}; {tag}]";
    
    private static final String FIELD_SEPARATOR = "<~>";
    
    private static final CvsModuleConfig INSTANCE = new CvsModuleConfig();
    public static final String PREFIX_KEYRING_KEY = "versioning.cvs."; //NOI18N

    public static CvsModuleConfig getDefault() {
        return INSTANCE;
    }

    private FileCollection excludedFiles;
    
    private Map<String, RootSettings> rootsMap;
    private String lastCanceledCommitMessage;


    public CvsModuleConfig() {
        excludedFiles = new FileCollection();
        excludedFiles.load(getPreferences(), PROP_COMMIT_EXCLUSIONS);
    }

    /**
     * @return 0 = do NOT wrap commit message
     */
    public int getWrapCommitMessagelength() {
        return getPreferences().getInt(PROP_WRAP_COMMIT_MESSAGE_LENGTH, 0);
    }
    
    /**
     * @param length 0 = do NOT wrap commit message
     */
    public void setWrapCommitMessagelength(int length) {
        if (length < 0) throw new IllegalArgumentException();
        getPreferences().putInt(PROP_WRAP_COMMIT_MESSAGE_LENGTH, length);
    }

    public void setAutoPruneDirectories (boolean b) {
        getPreferences().putBoolean(PROP_PRUNE_DIRECTORIES, b);
    }

    public boolean getAutoPruneDirectories () {
        return getPreferences().getBoolean(PROP_PRUNE_DIRECTORIES, true);
    }

    public Pattern [] getIgnoredFilePatterns() {
        return getDefaultFilePatterns();
    }

    public boolean isExcludedFromCommit(File file) {
        return excludedFiles.contains(file);
    }
    
    /**
     * @param file file to exclude from commit
     */
    public void addExclusion(File file) {
        excludedFiles.add(file);
        excludedFiles.save(getPreferences(), PROP_COMMIT_EXCLUSIONS);
    }

    /**
     * @param file file to include in commit
     */
    public void removeExclusion(File file) {
        excludedFiles.remove(file);
        excludedFiles.save(getPreferences(), PROP_COMMIT_EXCLUSIONS);
    }
    
    // clients code ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public synchronized boolean hasExtSettingsFor(CVSRoot root) {
        assert "ext".equals(root.getMethod());  // NOI18N
        Map<String, RootSettings> rootsMap = getRootsMap();
        String rootString = root.toString();
        RootSettings rootSettings = (RootSettings) rootsMap.get(rootString);
        if (rootSettings != null) {
            ExtSettings extSettings = rootSettings.extSettings;
            return extSettings != null;
        }
        return false;
    }

    /**
     * Loads a value set by {@link #setExtSettingsFor}.
     *
     * @param root cvs root with <code>:ext:</code> connection method
     * @return  additional ext settings or their default value
     */
    public synchronized ExtSettings getExtSettingsFor(CVSRoot root) {
        assert "ext".equals(root.getMethod());  // NOI18N
        Map<String, RootSettings> rootsMap = getRootsMap();
        String rootString = root.toString();
        RootSettings rootSettings = (RootSettings) rootsMap.get(rootString);
        if (rootSettings != null) {
            ExtSettings extSettings = rootSettings.extSettings;
            if (extSettings != null) {
                if (extSettings.extUseInternalSsh == false && extSettings.extCommand == null) {
                    extSettings.extCommand = System.getenv("CVS_RSH"); // NOI18N
                }
                return extSettings;
            }
        }

        // hardcoded default value
        ExtSettings defaults = new ExtSettings();
        defaults.extRememberPassword = false;
        defaults.extCommand = System.getenv("CVS_RSH"); // NOI18N
        defaults.extUseInternalSsh = true;
        defaults.extPassword = null;
        return defaults;
    }

    public synchronized void setExtSettingsFor(CVSRoot root, ExtSettings extSettings) {
        assert "ext".equals(root.getMethod());  // NOI18N
        Map<String, RootSettings> map = getRootsMap();
        String key = root.toString();
        RootSettings settings = (RootSettings) map.get(key);
        if (settings == null) {
            settings = new RootSettings();
        }
        settings.extSettings = extSettings;
        map.put(key, settings);

        storeRootsMap();
    }
    
    public Color getColor(String colorName, Color defaultColor) {
         int colorRGB = getPreferences().getInt(colorName, defaultColor.getRGB());
         return new Color(colorRGB);
    }

    public void setColor(String colorName, Color value) {
         getPreferences().putInt(colorName, value.getRGB());
    }

    public String getLastCanceledCommitMessage() {
        return lastCanceledCommitMessage == null ? "" : lastCanceledCommitMessage; //NOI18N
    }

    public void setLastCanceledCommitMessage(String message) {
        lastCanceledCommitMessage = message;
    }

    private Map<String, RootSettings> getRootsMap() {
        if (rootsMap == null) {
            rootsMap = loadRootsMap();
        }
        return rootsMap;
    }

    private Map<String, RootSettings> loadRootsMap() {
        List<String> smap = Utils.getStringList(getPreferences(), "cvsRootSettings");
        Map<String, RootSettings> map = new HashMap<String, RootSettings>(smap.size());
        for (String s : smap) {
            String [] fields = s.split(FIELD_SEPARATOR);
            if (fields.length < 8 && fields.length >= 3) {
                RootSettings rs = new RootSettings();
                map.put(fields[0], rs);
                ExtSettings es = new ExtSettings();
                rs.extSettings = es;
                es.extUseInternalSsh = Boolean.valueOf(fields[1]);
                es.extRememberPassword = Boolean.valueOf(fields[2]);
                es.extCommand = fields.length >= 4 ? fields[3] : ""; //NOI18N
                es.extPassword = KeyringSupport.read(PREFIX_KEYRING_KEY, fields[0]);
                if (fields.length >= 5 && !"".equals(fields[4])) {
                    es.extPassword = fields[4].toCharArray();
                    KeyringSupport.save(PREFIX_KEYRING_KEY, fields[0], es.extPassword.clone(), null);
                }
            }
        }
        return map;
    }

    private void storeRootsMap() {
        List<String> smap = new ArrayList<String>();
        for (Map.Entry<String, RootSettings> entry : rootsMap.entrySet()) {
            StringBuffer es = new StringBuffer(100);
            es.append(entry.getKey());
            RootSettings settings = entry.getValue();
            if (settings.extSettings != null) {
                es.append(FIELD_SEPARATOR);
                es.append(settings.extSettings.extUseInternalSsh);
                es.append(FIELD_SEPARATOR);
                es.append(settings.extSettings.extRememberPassword);
                es.append(FIELD_SEPARATOR);
                es.append(settings.extSettings.extCommand);
                if (settings.extSettings.extRememberPassword) {
                    es.append(FIELD_SEPARATOR);
                    KeyringSupport.save(PREFIX_KEYRING_KEY, entry.getKey(), settings.extSettings.extPassword.clone(), null);
                }
            }
            smap.add(es.toString());
        }
        Utils.put(getPreferences(), "cvsRootSettings", smap);
    }
    
    /**
     * Gets the backing store of module preferences, use this to store and retrieve simple properties and stored values. 
     *  
     * @return Preferences backing store
     */
    public Preferences getPreferences() {
        return NbPreferences.forModule(CvsModuleConfig.class);
    }
    
    // private methods ~~~~~~~~~~~~~~~~~~
       
    private static final Pattern [] defaultIgnoredPatterns = new Pattern [] {
                    Pattern.compile("cvslog\\..*"),  // NOI18N
                    Pattern.compile("\\.make\\.state"), // NOI18N
                    Pattern.compile("\\.nse_depinfo"), // NOI18N
                    Pattern.compile(".*~"), // NOI18N
                    Pattern.compile("#.*"), // NOI18N
                    Pattern.compile("\\.#.*"), // NOI18N
                    Pattern.compile(",.*"), // NOI18N
                    Pattern.compile("_\\$.*"), // NOI18N
                    Pattern.compile(".*\\$"), // NOI18N
                    Pattern.compile(".*\\.old"), // NOI18N
                    Pattern.compile(".*\\.bak"), // NOI18N
                    Pattern.compile(".*\\.BAK"), // NOI18N
                    Pattern.compile(".*\\.orig"), // NOI18N
                    Pattern.compile(".*\\.rej"), // NOI18N
                    Pattern.compile(".*\\.del-.*"), // NOI18N
                    Pattern.compile(".*\\.a"), // NOI18N
                    Pattern.compile(".*\\.olb"), // NOI18N
                    Pattern.compile(".*\\.o"), // NOI18N
                    Pattern.compile(".*\\.obj"), // NOI18N
                    Pattern.compile(".*\\.so"), // NOI18N
                    Pattern.compile(".*\\.exe"), // NOI18N
                    Pattern.compile(".*\\.Z"), // NOI18N
                    Pattern.compile(".*\\.elc"), // NOI18N
                    Pattern.compile(".*\\.ln"), // NOI18N
                };
    
    private Pattern[] getDefaultFilePatterns() {
        return defaultIgnoredPatterns;
    }

    /**
     * Holds associated settings.
     */
    private final static class RootSettings {

        private ExtSettings extSettings;
    }

    /** External method additional settings */
    public final static class ExtSettings {

        public boolean extUseInternalSsh;

        /** Makes sense if extUseInternalSsh == true */
        public boolean extRememberPassword;

        /** Makes sense if extUseInternalSsh == true */
        public char[] extPassword;

        /** Makes sense if extUseInternalSsh == false */
        public String extCommand;
    }
}

