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
import org.netbeans.modules.subversion.options.AnnotationExpression;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;
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
            
    private static final String RECENT_URL = "repository.recentURL";                            // NOI18N
    private static final String RECENT_PASSWORD = "repository.recentPassword";                  // NOI18N
    private static final String RECENT_USERNAME = "repository.recentUsername";                  // NOI18N
    private static final String RECENT_EXTERNAL_COMMAND = "repository.recentExternalCommand";   // NOI18N
    private static final String RECENT_PROXY = "repository.recentProxy";                        // NOI18N

    private static final String URL_EXP = "annotator.urlExp";                                   // NOI18N
    private static final String ANNOTATION_EXP = "annotator.annotationExp";                     // NOI18N
    
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

    public RepositoryConnection getRepositoryConnection(String url) {
        List<RepositoryConnection> rcs = getRecentUrls();
        for (Iterator<RepositoryConnection> it = rcs.iterator(); it.hasNext();) {
            RepositoryConnection rc = it.next();
            if(url.equals(rc.getUrl())) {
                return rc;
            }            
        }
        return null;
    }            
    
    public void insertRecentUrl(RepositoryConnection rc) {        
        Preferences prefs = getPreferences();
        
        List<String> urlValues = Utils.getStringList(prefs, RECENT_URL);        
        for (Iterator<String> it = urlValues.iterator(); it.hasNext();) {
            String rcOldString = it.next();
            RepositoryConnection rcOld =  RepositoryConnection.parse(rcOldString);
            if(rcOld.equals(rc)) {
                Utils.removeFromArray(prefs, RECENT_URL, rcOldString);
            }
        }        
        Utils.insert(prefs, RECENT_URL, RepositoryConnection.getString(rc), -1);                
    }    

    public void setRecentUrls(List<RepositoryConnection> recentUrls) {
        List<String> urls = new ArrayList<String>(recentUrls.size());
        
        int idx = 0;
        for (Iterator<RepositoryConnection> it = recentUrls.iterator(); it.hasNext();) {
            idx++;
            RepositoryConnection rc = it.next();
            urls.add(RepositoryConnection.getString(rc));            
        }
        Preferences prefs = getPreferences();
        Utils.put(prefs, RECENT_URL, urls);            
    }
    
    public List<RepositoryConnection> getRecentUrls() {
        Preferences prefs = getPreferences();
        List<String> urls = Utils.getStringList(prefs, RECENT_URL);                
        List<RepositoryConnection> ret = new ArrayList<RepositoryConnection>(urls.size());
        for (Iterator<String> it = urls.iterator(); it.hasNext();) {
            RepositoryConnection rc = RepositoryConnection.parse(it.next());
            ret.add(rc);
        }
        return ret;
    }
            
    public void setAnnotationExpresions(List<AnnotationExpression> exps) {
        List<String> urlExp = new ArrayList<String>(exps.size());
        List<String> annotationExp = new ArrayList<String>(exps.size());        
        
        int idx = 0;
        for (Iterator<AnnotationExpression> it = exps.iterator(); it.hasNext();) {
            idx++;
            AnnotationExpression exp = it.next();            
            urlExp.add(exp.getUrlExp());
            annotationExp.add(exp.getAnnotationExp());            
        }

        Preferences prefs = getPreferences();
        Utils.put(prefs, URL_EXP, urlExp);        
        Utils.put(prefs, ANNOTATION_EXP, annotationExp);                
    }

    public List<AnnotationExpression> getAnnotationExpresions() {
        Preferences prefs = getPreferences();
        List<String> urlExp = Utils.getStringList(prefs, URL_EXP);
        List<String> annotationExp = Utils.getStringList(prefs, ANNOTATION_EXP);        
        
        List<AnnotationExpression> ret = new ArrayList<AnnotationExpression>(urlExp.size());        
        for (int i = 0; i < urlExp.size(); i++) {                                        
            ret.add(new AnnotationExpression(urlExp.get(i), annotationExp.get(i)));
        }
        return ret;
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
