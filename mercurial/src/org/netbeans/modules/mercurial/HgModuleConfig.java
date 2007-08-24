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

package org.netbeans.modules.mercurial;


import java.util.regex.Pattern;
import java.util.*;
import java.util.prefs.Preferences;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.logging.Level;
import java.net.InetAddress;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
//import org.netbeans.modules.mercurial.options.AnnotationExpression;
import org.netbeans.modules.mercurial.ui.repository.RepositoryConnection;
import org.openide.util.NbPreferences;
import org.netbeans.modules.versioning.util.TableSorter;
import org.netbeans.modules.versioning.util.Utils;

/**
 * Stores Mercurial module configuration.
 *
 * @author Padraig O'Briain
 */
public class HgModuleConfig {
    
    public static final String PROP_IGNORED_FILEPATTERNS    = "ignoredFilePatterns";                        // NOI18N
    public static final String PROP_COMMIT_EXCLUSIONS       = "commitExclusions";                           // NOI18N
    public static final String PROP_DEFAULT_VALUES          = "defaultValues";                              // NOI18N
    public static final String PROP_RUN_VERSION             = "runVersion";                                 // NOI18N
    public static final String KEY_EXECUTABLE_BINARY        = "hgExecBinary";                              // NOI18N
    public static final String KEY_EXPORT_FILENAME          = "hgExportFilename";                          // NOI18N
    public static final String KEY_ANNOTATION_FORMAT        = "annotationFormat";                           // NOI18N
            
    private static final String RECENT_URL = "repository.recentURL";                                        // NOI18N
    private static final String SHOW_CHECKOUT_COMPLETED = "checkoutCompleted.showCheckoutCompleted";        // NOI18N  

    private static final String URL_EXP = "annotator.urlExp";                                               // NOI18N
    private static final String ANNOTATION_EXP = "annotator.annotationExp";                                 // NOI18N
    
    public static final String TEXT_ANNOTATIONS_FORMAT_DEFAULT = "{DEFAULT}";                               // NOI18N           

    private static final String DEFAULT_EXPORT_FILENAME = "/tmp/%b_%r_%h";                                  // NOI18N
    private static final HgModuleConfig INSTANCE = new HgModuleConfig();    
    
    private static String userName;

    public static HgModuleConfig getDefault() {
        return INSTANCE;
    }
    
    private Set<String> exclusions;

    // properties ~~~~~~~~~~~~~~~~~~~~~~~~~

    public Preferences getPreferences() {
        return NbPreferences.forModule(HgModuleConfig.class);
    }
    
    public boolean getShowCheckoutCompleted() {
        return getPreferences().getBoolean(SHOW_CHECKOUT_COMPLETED, true);
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
        return (String) getPreferences().get(KEY_EXECUTABLE_BINARY, ""); // NOI18N
    }
    
    public void setExecutableBinaryPath(String path) {
        getPreferences().put(KEY_EXECUTABLE_BINARY, path);
    }

    public String getExportFilename() {
        String str = (String) getPreferences().get(KEY_EXPORT_FILENAME, ""); // NOI18N
        if (str.trim().length() == 0) str = DEFAULT_EXPORT_FILENAME;
        return str;
    }
    
    public void setExportFilename(String path) {
        getPreferences().put(KEY_EXPORT_FILENAME, path);
    }

    /**
     * This method returns the username specified in $HOME/.hgrc
     * or /etc/mercurial/hgrc 
     * or a default username if none is found.
     */
    public String getUserName() {
        userName = HgConfigFiles.getInstance().getUserName();
        if (userName.length() == 0) {
            String userId = System.getProperty("user.name"); // NOI18N
            String hostName;
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (Exception ex) {
                return userName;
            }
            userName = userId + " <" + userId + "@" + hostName + ">"; // NOI18N
        }
        return userName;
    }

    public void setUserName(String name) {
        HgConfigFiles.getInstance().setUserName(name);
    }

    public Boolean isUserNameValid(String name) {
        if (name.equals(userName)) return true;
        if (name.length() == 0) return true;
        return HgMail.isUserNameValid(name);
    }

    public Boolean isExecPathValid(String name) {
        if (name.length() == 0) return true;
        File file = new File(name, "hg"); // NOI18N
        // I would like to call canExecute but that requires Java SE 6.
        return file.exists() && file.isFile();
    }

    public Properties getProperties(File file) {
        Properties props = new Properties();
        HgConfigFiles hgconfig = new HgConfigFiles(file); 
        String name = hgconfig.getUserName(false);
        if (name.length() == 0) 
            name = getUserName();
        if (name.length() > 0) 
            props.setProperty("username", name); // NOI18N
        name = hgconfig.getDefaultPull(false);
        if (name.length() > 0) 
            props.setProperty("default-pull", name); // NOI18N
        name = hgconfig.getDefaultPush(false);
        if (name.length() > 0) 
            props.setProperty("default-push", name); // NOI18N
        return props;
    }

    public void clearProperties(File file, String section) {
        getHgConfigFiles(file).clearProperties(section);
    }

    public void removeProperty(File file, String section, String name) {
        getHgConfigFiles(file).removeProperty(section, name);
    }

    public void setProperty(File file, String name, String value) {
        getHgConfigFiles(file).setProperty(name, value);
    }

    public void setProperty(File file, String section, String name, String value) {
        getHgConfigFiles(file).setProperty(section, name, value);
    }

    /*
     * Get all properties for a particular section
     */
    public Properties getProperties(File file, String section) {
        return getHgConfigFiles(file).getProperties(section);
    }

    private HgConfigFiles getHgConfigFiles(File file) {
        if (file == null) {
            return HgConfigFiles.getInstance();
        } else {
            return new HgConfigFiles(file); 
        }
    }

    public String getAnnotationFormat() {
        return (String) getPreferences().get(KEY_ANNOTATION_FORMAT, getDefaultAnnotationFormat());                
    }
    
    public String getDefaultAnnotationFormat() {
        return "[{" + MercurialAnnotator.ANNOTATION_STATUS + "} {" + MercurialAnnotator.ANNOTATION_FOLDER + "}]"; // NOI18N
    }

    public void setAnnotationFormat(String annotationFormat) {
        getPreferences().put(KEY_ANNOTATION_FORMAT, annotationFormat);        
    }

    public void setShowCheckoutCompleted(boolean bl) {
        getPreferences().putBoolean(SHOW_CHECKOUT_COMPLETED, bl);
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
            
    //public void setAnnotationExpresions(List<AnnotationExpression> exps) {
    //    List<String> urlExp = new ArrayList<String>(exps.size());
    //    List<String> annotationExp = new ArrayList<String>(exps.size());        
        
    //    int idx = 0;
    //    for (Iterator<AnnotationExpression> it = exps.iterator(); it.hasNext();) {
    //        idx++;
    //        AnnotationExpression exp = it.next();            
    //        urlExp.add(exp.getUrlExp());
    //        annotationExp.add(exp.getAnnotationExp());            
    //    }

    //    Preferences prefs = getPreferences();
    //    Utils.put(prefs, URL_EXP, urlExp);        
    //    Utils.put(prefs, ANNOTATION_EXP, annotationExp);                
    //}

    //public List<AnnotationExpression> getAnnotationExpresions() {
    //    Preferences prefs = getPreferences();
    //    List<String> urlExp = Utils.getStringList(prefs, URL_EXP);
    //    List<String> annotationExp = Utils.getStringList(prefs, ANNOTATION_EXP);        
              
    //    List<AnnotationExpression> ret = new ArrayList<AnnotationExpression>(urlExp.size());                
    //    for (int i = 0; i < urlExp.size(); i++) {                                        
    //        ret.add(new AnnotationExpression(urlExp.get(i), annotationExp.get(i)));
    //    }
    //    if(ret.size() < 1) {
    //        ret = getDefaultAnnotationExpresions();
    //    }
    //    return ret;
    //}

    //public List<AnnotationExpression> getDefaultAnnotationExpresions() {
    //    List<AnnotationExpression> ret = new ArrayList<AnnotationExpression>(1);
    //    ret.add(new AnnotationExpression(".*/(branches|tags)/(.+?)/.*", "\\2"));     // NOI18N 
    //    return ret;
    //}
    
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
