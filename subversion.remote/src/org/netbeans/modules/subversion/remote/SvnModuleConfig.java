/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote;

import org.netbeans.modules.subversion.remote.client.SvnClientFactory;
import java.awt.Color;
import java.awt.EventQueue;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.netbeans.modules.subversion.remote.config.SvnConfigFiles;
import org.netbeans.modules.subversion.remote.options.AnnotationExpression;
import org.netbeans.modules.subversion.remote.ui.diff.Setup;
import org.netbeans.modules.subversion.remote.ui.repository.RepositoryConnection;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.util.KeyringSupport;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbPreferences;

/**
 * Stores Subversion module configuration.
 *
 * 
 */
public class SvnModuleConfig {
    
    public static final String PROP_IGNORED_FILEPATTERNS    = "ignoredFilePatterns";                        // NOI18N
    public static final String PROP_COMMIT_EXCLUSIONS       = "commitExclusions";                           // NOI18N
    public static final String PROP_DEFAULT_VALUES          = "defaultValues";                              // NOI18N
    public static final String KEY_EXECUTABLE_BINARY        = "svnExecBinary";                              // NOI18N
    public static final String KEY_ANNOTATION_FORMAT        = "annotationFormat";                           // NOI18N
    public static final String SAVE_PASSWORD                = "savePassword";                               // NOI18N
    private static final String FILE_SELECTOR_PREFIX        = "fileSelector";                               // NOI18N
    private static final String SEARCH_HISTORY_ALL_INFO     = "histAllInfo";                                // NOI18N
    
    public static final String KEY_RECENT_URL = "repository.recentURL";                                     // NOI18N
    private static final String SHOW_CHECKOUT_COMPLETED = "checkoutCompleted.showCheckoutCompleted";        // NOI18N  

    private static final String URL_EXP = "annotator.urlExp";                                               // NOI18N
    private static final String ANNOTATION_EXP = "annotator.annotationExp";                                 // NOI18N
    
    public static final String TEXT_ANNOTATIONS_FORMAT_DEFAULT = "{DEFAULT}";                               // NOI18N
    private static final String AUTO_OPEN_OUTPUT_WINDOW = "autoOpenOutput";                                 // NOI18N
    private static final String LAST_USED_MODIFICATION_CONTEXT = "lastUsedModificationContext"; //NOI18N
    public static final String KEY_PASSWORD = "versioning.subversion."; //NOI18N
    public static final String KEY_CERT_PASSWORD = "versioning.subversion.cert."; //NOI18N
    private static final String PROP_EXCLUDE_NEW_FILES = "excludeNewFiles"; //NOI18N
    private static final String PROP_GET_REMOTE_LOCKS = "getRemoteLocks"; //NOI18N
    private static final String PROP_AUTO_LOCK = "autoLockFiles"; //NOI18N
    private static final String PREFIX_REPOSITORY_PATH = "prefixRepositoryPath"; //NOI18N
    private static final String SEPARATOR = "###"; //NOI18N
    private static final String KEY_SORTING = "sortingStatus."; //NOI18N
    private static final String PROP_FORCE_COMMANDLINE = "forcedCommandline"; //NOI18N
    private static final String PROP_PREFERRED_FACTORY = "preferredFactory"; //NOI18N
    private static final String PROP_FILTER_PROPERTIES_ENABLED = "filterProperties.enabled"; //NOI18N
    private static final String PROP_DETERMINE_BRANCHES_ENABLED = "determineBranch.enabled"; //NOI18N

    private static final Map<FileSystem,SvnModuleConfig> INSTANCE = new HashMap<>();
        
    private Map<String, Object[]> urlCredentials;
    private final FileSystem fileSystem;
    
    public static SvnModuleConfig getDefault(FileSystem fileSystem) {
        SvnModuleConfig res = INSTANCE.get(fileSystem);
        if (res == null) {
            res = new SvnModuleConfig(fileSystem);
            INSTANCE.put(fileSystem, res);
        }
        return res;
    }
    
    public SvnModuleConfig(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
    
    private Set<String> exclusions;
    private String lastCanceledCommitMessage;
    private String factory;

    // properties ~~~~~~~~~~~~~~~~~~~~~~~~~

    public Preferences getPreferences() {
        if (fileSystem == null) {
            return NbPreferences.forModule(SvnModuleConfig.class).node("commonRemoteSubversion"); //NOI18N
        } else {
            return NbPreferences.forModule(SvnModuleConfig.class).node(VCSFileProxySupport.getFileSystemKey(fileSystem));
        }
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
            org.netbeans.modules.subversion.remote.versioning.util.Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<>(exclusions));
        }
    }

    /**
     * @param paths collection of paths, File.getAbsolutePath()
     */
    public void removeExclusionPaths(Collection<String> paths) {
        Set<String> exclusions = getCommitExclusions();
        if (exclusions.removeAll(paths)) {
            org.netbeans.modules.subversion.remote.versioning.util.Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<>(exclusions));
        }
    }

    public String getExecutableBinaryPath() {
        return getPreferences().get(KEY_EXECUTABLE_BINARY, "");        
    }
    
    public void setExecutableBinaryPath(String path) {
        getPreferences().put(KEY_EXECUTABLE_BINARY, path);        
    }

    public String getAnnotationFormat() {
        return getPreferences().get(KEY_ANNOTATION_FORMAT, getDefaultAnnotationFormat());                
    }
    
    public String getDefaultAnnotationFormat() {
        return "[{" + Annotator.ANNOTATION_STATUS + "} {" + Annotator.ANNOTATION_FOLDER + "}]"; //NOI18N
    }

    public void setAnnotationFormat(String annotationFormat) {
        getPreferences().put(KEY_ANNOTATION_FORMAT, annotationFormat);        
    }

    public void setShowCheckoutCompleted(boolean bl) {
        getPreferences().putBoolean(SHOW_CHECKOUT_COMPLETED, bl);
    }
    
    public boolean getSavePassword() {
        return getPreferences().getBoolean(SAVE_PASSWORD, true);
    }

    public void setSavePassword(boolean bl) {
        getPreferences().putBoolean(SAVE_PASSWORD, bl);
    }

    public String getFileSelectorPreset(String hash) {
        return getPreferences().get(FILE_SELECTOR_PREFIX + "-" + hash, ""); //NOI18N
    }

    public void setFileSelectorPreset(String hash, String path) {
        getPreferences().put(FILE_SELECTOR_PREFIX + "-" + hash, path); //NOI18N
    }
    
    public boolean getShowFileAllInfo() {
        return getPreferences().getBoolean(SEARCH_HISTORY_ALL_INFO, false);
    }

    public void setShowFileAllInfo(boolean value) {
        getPreferences().putBoolean(SEARCH_HISTORY_ALL_INFO, value);
    }

    public boolean getAutoOpenOutput() {
        return getPreferences().getBoolean(AUTO_OPEN_OUTPUT_WINDOW, true);
    }

    public void setAutoOpenOutputo(boolean value) {
        getPreferences().putBoolean(AUTO_OPEN_OUTPUT_WINDOW, value);
    }

    public RepositoryConnection getRepositoryConnection(String url) {
        RepositoryConnection rc = getRepositoryConnectionIntern(url);
        if (rc == null) {
            try {
                // this will remove username from the hostname
                rc = getRepositoryConnectionIntern(new RepositoryConnection(url).getSvnUrl().toString());
            } catch (MalformedURLException ex) {
                // not interested
            }
        }
        return rc;
    }      
    
    public void insertRecentUrl(final RepositoryConnection rc) {
        Preferences prefs = getPreferences();

        List<String> urlValues = org.netbeans.modules.subversion.remote.versioning.util.Utils.getStringList(prefs, KEY_RECENT_URL);
        for (Iterator<String> it = urlValues.iterator(); it.hasNext();) {
            String rcOldString = it.next();
            RepositoryConnection rcOld =  RepositoryConnection.parse(rcOldString);
            if(rcOld.equals(rc)) {
                try {
                    org.netbeans.modules.versioning.util.Utils.removeFromArray(prefs, KEY_RECENT_URL, rcOldString);
                } finally {
                    SvnConfigFiles.getInstance(fileSystem).reset();
                }
            }
        }
        handleCredentials(rc);
        storeCredentials(rc);

        String url = RepositoryConnection.getString(rc);
        if (!"".equals(url)) {                                          //NOI18N
            try {
                org.netbeans.modules.versioning.util.Utils.insert(prefs, KEY_RECENT_URL, url, -1);
            } finally {
                SvnConfigFiles.getInstance(fileSystem).reset();
            }
        }
    }    

    public void setRecentUrls(List<RepositoryConnection> recentUrls) {
        List<String> urls = new ArrayList<>(recentUrls.size());
        
        int idx = 0;
        for (Iterator<RepositoryConnection> it = recentUrls.iterator(); it.hasNext();) {
            idx++;
            RepositoryConnection rc = it.next();
            handleCredentials(rc);
            storeCredentials(rc);
            String url = RepositoryConnection.getString(rc);
            if (!"".equals(url)) {                                      //NOI18N
                urls.add(url);
            }
        }
        Preferences prefs = getPreferences();
        try {
            org.netbeans.modules.subversion.remote.versioning.util.Utils.put(prefs, KEY_RECENT_URL, urls);
        } finally {
            SvnConfigFiles.getInstance(fileSystem).reset();
        }
    }
    
    public List<RepositoryConnection> getRecentUrls() {
        Preferences prefs = getPreferences();
        List<String> urls = org.netbeans.modules.subversion.remote.versioning.util.Utils.getStringList(prefs, KEY_RECENT_URL);
        List<RepositoryConnection> ret = new ArrayList<>(urls.size());
        List<RepositoryConnection> withPassword = new LinkedList<>();
        for (String urlString : urls) {
            RepositoryConnection rc = RepositoryConnection.parse(urlString);
            if (rc.getPassword() != null || rc.getCertPassword() != null) {
                withPassword.add(rc);
            } else {
                if(getUrlCredentials().containsKey(rc.getUrl())) {
                    Object[] creds = getUrlCredentials().get(rc.getUrl());
                    if(creds.length < 3) {
                        continue; //skip garbage
                    }
                    rc = new RepositoryConnection(rc.getUrl(), (String)creds[0], (char[])creds[1], rc.getExternalCommand(), rc.getSavePassword(), rc.getCertFile(), (char[])creds[2], rc.getSshPortNumber());
                } else if (!EventQueue.isDispatchThread()) {
                    char[] password = rc.getSavePassword() ? KeyringSupport.read(KEY_PASSWORD, rc.getUrl()) : null;
                    char[] certPassword = rc.getCertFile().isEmpty() ? null : KeyringSupport.read(KEY_CERT_PASSWORD, rc.getUrl());
                    rc = new RepositoryConnection(rc.getUrl(), rc.getUsername(), password, rc.getExternalCommand(), rc.getSavePassword(), rc.getCertFile(), certPassword, rc.getSshPortNumber());
                }
                ret.add(rc);
            }
        }
        // there's an old-style connection with password set
        // rewrite these connections with the new version with no password included
        if (withPassword.size() > 0) {
            for (RepositoryConnection conn : withPassword) {
                insertRecentUrl(conn);
            }
            return getRecentUrls();
        }
        return ret;
    }
            
    public void setAnnotationExpresions(List<AnnotationExpression> exps) {
        List<String> urlExp = new ArrayList<>(exps.size());
        List<String> annotationExp = new ArrayList<>(exps.size());        
        
        int idx = 0;
        for (Iterator<AnnotationExpression> it = exps.iterator(); it.hasNext();) {
            idx++;
            AnnotationExpression exp = it.next();            
            urlExp.add(exp.getUrlExp());
            annotationExp.add(exp.getAnnotationExp());            
        }

        Preferences prefs = getPreferences();
        org.netbeans.modules.subversion.remote.versioning.util.Utils.put(prefs, URL_EXP, urlExp);        
        org.netbeans.modules.subversion.remote.versioning.util.Utils.put(prefs, ANNOTATION_EXP, annotationExp);                
    }

    public List<AnnotationExpression> getAnnotationExpresions() {
        Preferences prefs = getPreferences();
        List<String> urlExp = org.netbeans.modules.subversion.remote.versioning.util.Utils.getStringList(prefs, URL_EXP);
        List<String> annotationExp = org.netbeans.modules.subversion.remote.versioning.util.Utils.getStringList(prefs, ANNOTATION_EXP);        
                
        List<AnnotationExpression> ret = new ArrayList<>(urlExp.size());                
        for (int i = 0; i < urlExp.size(); i++) {                                        
            ret.add(new AnnotationExpression(urlExp.get(i), annotationExp.get(i)));
        }
        if(ret.size() < 1) {
            ret = getDefaultAnnotationExpresions();
        }
        return ret;
    }

    public List<AnnotationExpression> getDefaultAnnotationExpresions() {
        List<AnnotationExpression> ret = new ArrayList<>(1);
        ret.add(new AnnotationExpression(".*?/(?<!/src/.{1,200})(branches|tags)/(.+?)(/.*)?", "\\2")); //NOI18N
        return ret;
    }
    
    public boolean isDetermineBranchesEnabled () {
        return getPreferences().getBoolean(PROP_DETERMINE_BRANCHES_ENABLED, true);
    }
    
    public void setDetermineBranchesEnabled (boolean enabled) {
        getPreferences().putBoolean(PROP_DETERMINE_BRANCHES_ENABLED, enabled);
    }

    public int getLastUsedModificationContext () {
        int lastUsedContext = getPreferences().getInt(LAST_USED_MODIFICATION_CONTEXT, Setup.DIFFTYPE_LOCAL);
        if (lastUsedContext != Setup.DIFFTYPE_LOCAL
                && lastUsedContext != Setup.DIFFTYPE_REMOTE
                && lastUsedContext != Setup.DIFFTYPE_ALL) {
            lastUsedContext = Setup.DIFFTYPE_LOCAL;
        }
        return lastUsedContext;
    }

    public void setLastUsedModificationContext (int lastUsedContext) {
        getPreferences().putInt(LAST_USED_MODIFICATION_CONTEXT, lastUsedContext);
    }

    public boolean getExludeNewFiles () {
        return getPreferences().getBoolean(PROP_EXCLUDE_NEW_FILES, false);
    }

    public void setExcludeNewFiles (boolean excludeNewFiles) {
        getPreferences().putBoolean(PROP_EXCLUDE_NEW_FILES, excludeNewFiles);
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

    public boolean isRepositoryPathPrefixed() {
        return getPreferences().getBoolean(PREFIX_REPOSITORY_PATH, false);
    }

    public void setRepositoryPathPrefixed(boolean prefixRepositoryPath) {
        getPreferences().putBoolean(PREFIX_REPOSITORY_PATH, prefixRepositoryPath);
    }

    public LinkedHashMap<String, Integer> getSortingStatus(String panel) {
        LinkedHashMap<String, Integer> sortingState = null;
        String packed = getPreferences().get(KEY_SORTING + panel, null);
        if (packed != null) {
            String[] tokens = packed.split(SEPARATOR);
            sortingState = new LinkedHashMap<>(tokens.length >> 1);
            for (int i = 0; i < tokens.length - 1;) {
                String column = tokens[i++];
                try {
                    Integer colState = Integer.parseInt(tokens[i++]);
                    sortingState.put(column, colState);
                } catch (NumberFormatException ex) {
                    //
                }
            }
        }
        return sortingState;
    }

    public void setSortingStatus (String panel, Map<String, Integer> sortingState) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : sortingState.entrySet()) {
            sb.append(e.getKey()).append(SEPARATOR).append(e.getValue().toString()).append(SEPARATOR);
        }
        if (sb.length() > 0) {
            getPreferences().put(KEY_SORTING + panel, sb.toString());
        } else {
            getPreferences().remove(KEY_SORTING + panel);
        }
    }

    public boolean isGetRemoteLocks () {
        return getPreferences().getBoolean(PROP_GET_REMOTE_LOCKS, false);
    }

    public void setGetRemoteLocks (boolean flag) {
        getPreferences().putBoolean(PROP_GET_REMOTE_LOCKS, flag);
    }

    public boolean isAutoLock () {
        return getPreferences().getBoolean(PROP_AUTO_LOCK, false);
    }

    public void setAutoLock (boolean flag) {
        getPreferences().putBoolean(PROP_AUTO_LOCK, flag);
    }

    public boolean isFilterPropertiesEnabled () {
        return getPreferences().getBoolean(PROP_FILTER_PROPERTIES_ENABLED, true);
    }

    public void setFilterPropertiesEnabled (boolean enabled) {
        getPreferences().putBoolean(PROP_FILTER_PROPERTIES_ENABLED, enabled);
    }

    // private methods ~~~~~~~~~~~~~~~~~~
    
    private synchronized Set<String> getCommitExclusions() {
        if (exclusions == null) {
            exclusions = new HashSet<>(org.netbeans.modules.subversion.remote.versioning.util.Utils.getStringList(getPreferences(), PROP_COMMIT_EXCLUSIONS));
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
    
    private void handleCredentials(RepositoryConnection rc) {
        String url;
        try {
            url = rc.getSvnUrl().toString();
        } catch (MalformedURLException ex) {
            url = rc.getUrl();
        }
        if(!rc.getSavePassword()) {
            getUrlCredentials().put(rc.getUrl(), new Object[]{rc.getUsername(), rc.getPassword(), rc.getCertPassword()});
            if (!url.equals(rc.getUrl())) {
                getUrlCredentials().put(url, new Object[]{rc.getUsername(), rc.getPassword(), rc.getCertPassword()});
            }
        } else {
            getUrlCredentials().remove(rc.getUrl());
            getUrlCredentials().remove(url);
        }                      
    }    
    
    private Map<String, Object[]> getUrlCredentials() {
        if(urlCredentials == null) {
            urlCredentials =  new HashMap<>();
        }
        return urlCredentials;
    }    
    
    private RepositoryConnection getRepositoryConnectionIntern (String url) {
        if(url.endsWith("/")) url = url.substring(0, url.length() - 1); //NOI18N
        List<RepositoryConnection> rcs = getRecentUrls();
        for (Iterator<RepositoryConnection> it = rcs.iterator(); it.hasNext();) {
            RepositoryConnection rc = it.next();
            String rcUrl = rc.getUrl();
            if(rcUrl.endsWith("/")) rcUrl = rcUrl.substring(0, rcUrl.length() - 1); //NOI18N
            if(url.equals(rcUrl)) {
                return rc; // exact match
            }
        }
        for (Iterator<RepositoryConnection> it = rcs.iterator(); it.hasNext();) {
            RepositoryConnection rc = it.next();
            String rcUrl = rc.getUrl();
            if(rcUrl.endsWith("/")) rcUrl = rcUrl.substring(0, rcUrl.length() - 1); //NOI18N
            if(rcUrl.startsWith(url)) {
                return rc; // try this
            }
        }
        return null;
    }

    private void storeCredentials (final RepositoryConnection rc) {
        if ((rc.getSavePassword() && rc.getPassword() != null) || rc.getCertPassword() != null) {
            Runnable outOfAWT = new Runnable() {
                @Override
                public void run() {
                    String url;
                    try {
                        url = rc.getSvnUrl().toString();
                    } catch (MalformedURLException ex) {
                        url = rc.getUrl();
                    }
                    if (rc.getSavePassword() && rc.getPassword() != null) {
                        KeyringSupport.save(KEY_PASSWORD, url, rc.getPassword().clone(), null);
                    }
                    if (rc.getCertPassword() != null) {
                        KeyringSupport.save(KEY_CERT_PASSWORD, url, rc.getCertPassword().clone(), null);
                    }
                }
            };
            // keyring should be called only in a background thread
            if (EventQueue.isDispatchThread()) {
                Subversion.getInstance().getRequestProcessor().post(outOfAWT);
            } else {
                outOfAWT.run();
            }
        }
    }

    public void setForceCommnandlineClient (boolean forced) {
        if (forced) {
            getPreferences().putBoolean(PROP_FORCE_COMMANDLINE, true);
        } else {
            getPreferences().remove(PROP_FORCE_COMMANDLINE);
        }
    }

    public boolean isForcedCommandlineClient () {
        return getPreferences().getBoolean(PROP_FORCE_COMMANDLINE, false);
    }

    public String getPreferredFactoryType (String defaultFactory) {
        return getPreferences().get(PROP_PREFERRED_FACTORY, defaultFactory);
    }

    public void setPreferredFactoryType (String preferredFactory) {
        if (preferredFactory == null) {
            getPreferences().remove(PROP_PREFERRED_FACTORY);
        } else {
            assert SvnClientFactory.FACTORY_TYPE_COMMANDLINE.equals(preferredFactory);
            getPreferences().put(PROP_PREFERRED_FACTORY, preferredFactory);
            setForceCommnandlineClient(false);
            factory = ""; // override the global setting
        }
    }

    public String getGlobalSvnFactory () {
        if (factory == null) {
            factory = System.getProperty("svnClientAdapterFactory", ""); //NOI18N
        }
        return factory;
    }
}
