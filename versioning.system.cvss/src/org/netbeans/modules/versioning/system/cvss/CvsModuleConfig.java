/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss;

import java.util.regex.Pattern;
import java.util.*;
import java.lang.String;
import java.util.prefs.Preferences;
import java.io.File;

import org.openide.util.NbPreferences;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.FileCollection;
import org.netbeans.lib.cvsclient.CVSRoot;

/**
 * Stores CVS module configuration.
 *
 * @author Maros Sandor
 */
public class CvsModuleConfig {
    
    public static final String PROP_IGNORED_FILEPATTERNS    = "ignoredFilePatterns";  // NOI18N
    public static final String PROP_COMMIT_EXCLUSIONS       = "commitExclusions";  // NOI18N
    public static final String PROP_TEXT_ANNOTATIONS_FORMAT = "textAnnotations";  // NOI18N
    public static final String PROP_SEARCHHISTORY_FETCHTAGS = "searchHistory.fetchTags";  // NOI18N
    public static final String ROOTS_CONFIG = "rootsConfig";  // NOI18N
    
    private static final String FIELD_SEPARATOR = "<~>";
    
    private static final CvsModuleConfig INSTANCE = new CvsModuleConfig();

    public static CvsModuleConfig getDefault() {
        return INSTANCE;
    }

    private FileCollection excludedFiles;
    
    private Map<String, RootSettings> rootsMap;


    public CvsModuleConfig() {
        excludedFiles = new FileCollection();
        excludedFiles.load(getPreferences(), PROP_COMMIT_EXCLUSIONS);
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
            if (fields.length >= 8) {
                // TODO: old settings, remove this block after 6.0
                RootSettings rs = new RootSettings();
                map.put(fields[0], rs);
                if (fields.length >= 11) {
                    ExtSettings es = new ExtSettings();
                    rs.extSettings = es;
                    es.extUseInternalSsh = Boolean.valueOf(fields[8]);
                    es.extRememberPassword = Boolean.valueOf(fields[9]);
                    es.extCommand = fields[10];
                    if (fields.length >= 12) {
                        es.extPassword = fields[11];
                    }
                }
            } else {
                if (fields.length >= 4) {
                    RootSettings rs = new RootSettings();
                    map.put(fields[0], rs);
                    ExtSettings es = new ExtSettings();
                    rs.extSettings = es;
                    es.extUseInternalSsh = Boolean.valueOf(fields[1]);
                    es.extRememberPassword = Boolean.valueOf(fields[2]);
                    es.extCommand = fields[3];
                    if (fields.length >= 5) {
                        es.extPassword = fields[4];
                    }
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
                    es.append(settings.extSettings.extPassword);
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
       
    private Pattern[] getDefaultFilePatterns() {
        return new Pattern [] {
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
        public String extPassword;

        /** Makes sense if extUseInternalSsh == false */
        public String extCommand;
    }
}

