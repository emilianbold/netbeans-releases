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

package org.netbeans.modules.subversion;


import java.util.regex.Pattern;
import java.util.*;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.netbeans.modules.versioning.util.TableSorter;
import org.netbeans.modules.versioning.util.Utils;

/**
 * Stores Subversion module configuration.
 *
 * @author Maros Sandor
 */
public class SvnModuleConfig {
    
    public static final String PROP_IGNORED_FILEPATTERNS    = "ignoredFilePatterns";    // NOI18N
    public static final String PROP_COMMIT_EXCLUSIONS       = "commitExclusions";       // NOI18N
    public static final String PROP_DEFAULT_VALUES          = "defaultValues";          // NOI18N
    public static final String PROP_TEXT_ANNOTATIONS_FORMAT = "textAnnotations";        // NOI18N
    public static final String KEY_EXECUTABLE_BINARY        = "svnExecBinary";          // NOI18N
    public static final String KEY_ANNOTATION_FORMAT        = "annotationFormat";       // NOI18N
            
    private static final String RECENT_URL = "repository.recentURL";                                // NOI18N

    public static final String TEXT_ANNOTATIONS_FORMAT_DEFAULT = "{DEFAULT}";           // NOI18N

    private static final SvnModuleConfig INSTANCE = new SvnModuleConfig();    
    
    public static SvnModuleConfig getDefault() {
        return INSTANCE;
    }
    
    private Set<String> exclusions;

    // properties ~~~~~~~~~~~~~~~~~~~~~~~~~

    public Preferences getPreferences() {
        return NbPreferences.forModule(SvnModuleConfig.class);
    }
    
    public Pattern [] getIgnoredFilePatterns() {
        return getDefaultFilePatterns();
    }
    
    public boolean isExcludedFromCommit(String path) {
        return getCommitExclusions().contains(path);
    }
    
    /**
     * @param paths collection of paths, of File.getAbsolutePath()
     */
    public void addExclusionPaths(Collection<String> paths) {
        Set<String> exclusions = getCommitExclusions();
        if (exclusions.addAll(paths)) {
            Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<String>(exclusions));
        }
    }

    /**
     * @param paths collection of paths, File.getAbsolutePath()
     */
    public void removeExclusionPaths(Collection<String> paths) {
        Set<String> exclusions = getCommitExclusions();
        if (exclusions.removeAll(paths)) {
            Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<String>(exclusions));
        }
    }

    public String getExecutableBinaryPath() {
        return (String) getPreferences().get(KEY_EXECUTABLE_BINARY, "");        
    }
    
    public void setExecutableBinaryPath(String path) {
        getPreferences().put(KEY_EXECUTABLE_BINARY, path);        
    }

    public String getAnnotationFormat() {
        return (String) getPreferences().get(KEY_ANNOTATION_FORMAT, "");        
    }
    
    public void setAnnotationFormat(String annotationFormat) {
        getPreferences().put(KEY_ANNOTATION_FORMAT, annotationFormat);        
    }
            
    public void insertRecentUrl(String url) {
        Utils.insert(SvnModuleConfig.getDefault().getPreferences(), RECENT_URL, url, -1);        
    }
    
    public void removeFromRecentUrls(String[] toRemove) {
        Utils.removeFromArray(SvnModuleConfig.getDefault().getPreferences(), RECENT_URL, toRemove);        
    }

    public List<String> getRecentUrls() {
        return Utils.getStringList(SvnModuleConfig.getDefault().getPreferences(), RECENT_URL);
    }            
            
    // TODO: persist state

    private TableSorter importTableSorter;
    private TableSorter commitTableSorter;
    
    public TableSorter getImportTableSorter() {
        return importTableSorter;        
    }

    public void setImportTableSorter(TableSorter sorter) {
        importTableSorter = sorter;        
    }

    public TableSorter getCommitTableSorter() {
        return commitTableSorter;
    }

    public void setCommitTableSorter(TableSorter sorter) {
        commitTableSorter = sorter;
    }
    
    // private methods ~~~~~~~~~~~~~~~~~~
    
    private synchronized Set<String> getCommitExclusions() {
        if (exclusions == null) {
            exclusions = new HashSet<String>(Utils.getStringList(getPreferences(), PROP_COMMIT_EXCLUSIONS));
        }
        return exclusions;
    }
    
    private static Pattern[] getDefaultFilePatterns() {
        return new Pattern [] {
                        Pattern.compile("cvslog\\..*"), // NOI18N
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
}
