/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.mercurial.remote;


import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.netbeans.modules.mercurial.remote.config.HgConfigFiles;
import org.netbeans.modules.mercurial.remote.ui.repository.RepositoryConnection;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.KeyringSupport;
import org.netbeans.modules.versioning.util.TableSorter;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Stores Mercurial module configuration.
 *
 * 
 */
public class HgModuleConfig {
    
    public static final String PROP_IGNORED_FILEPATTERNS    = "ignoredFilePatterns";                        // NOI18N
    public static final String PROP_COMMIT_EXCLUSIONS       = "commitExclusions";                           // NOI18N
    public static final String PROP_DEFAULT_VALUES          = "defaultValues";                              // NOI18N
    public static final String PROP_RUN_VERSION             = "runVersion";                                 // NOI18N
    public static final String KEY_EXECUTABLE_BINARY        = "hgExecBinary";                              // NOI18N
    public static final String KEY_EXPORT_FILENAME          = "hgExportFilename";                          // NOI18N
    public static final String KEY_EXPORT_FOLDER            = "hgExportFolder";                          // NOI18N
    public static final String KEY_IMPORT_FOLDER            = "hgImportFolder";                          // NOI18N
    public static final String KEY_ANNOTATION_FORMAT        = "annotationFormat";                           // NOI18N
    public static final String SAVE_PASSWORD                = "savePassword";                               // NOI18N
    public static final String KEY_BACKUP_ON_REVERTMODS = "backupOnRevert";                               // NOI18N
    public static final String KEY_SHOW_HITORY_MERGES = "showHistoryMerges";                               // NOI18N
    private static final String KEY_SHOW_FILE_INFO = "showFileInfo";        // NOI18N
    private static final String AUTO_OPEN_OUTPUT_WINDOW = "autoOpenOutput";        // NOI18N
    private static final String CONFIRM_BEFORE_COMMIT_AFTER_MERGE = "confirmBeforeCommitAfterMerge"; //NOI18N
    private static final String KEY_INTERNAL_MERGE_TOOL_ENABLED = "hgmerge.internalTool.enabled"; //NOI18N
    private static final String PROP_EXCLUDE_NEW_FILES = "excludeNewFiles"; //NOI18N
    private static final String PROP_DIFF_VIEW_MODE = "diffViewMode"; //NOI18N
    private static final String KEY_QPATCH_MESSAGE = "qpatch.message."; //NOI18N
    private static final String PROP_RECENT_COMMIT_AUTHORS = "recentCommitAuhtors";// NOI18N

    private static final String RECENT_URL = "repository.recentURL";                                        // NOI18N
    private static final String SHOW_CLONE_COMPLETED = "cloneCompleted.showCloneCompleted";        // NOI18N  

    private static final String URL_EXP = "annotator.urlExp";                                               // NOI18N
    private static final String ANNOTATION_EXP = "annotator.annotationExp";                                 // NOI18N
    
    public static final String TEXT_ANNOTATIONS_FORMAT_DEFAULT = "{DEFAULT}";                               // NOI18N           

    private static final String DEFAULT_EXPORT_FILENAME = "%b_%r_%h";                                  // NOI18N
    private static final Map<FileSystem,HgModuleConfig> INSTANCE = new HashMap<>();
    private static final String KEY_SEARCH_ON_BRANCH = "searchOnBranch.enabled."; //NOI18N
    private static final String KEY_REMOVE_NEW_FILES_ON_REVERT = "removeNewFilesOnRevert"; //NOI18N
    
    private String userName;
    private final FileSystem fileSystem;

    public static synchronized HgModuleConfig getDefault(VCSFileProxy root) {
        FileSystem fileSystem = null;
        if (root != null) {
            fileSystem = VCSFileProxySupport.getFileSystem(root);
        }
        HgModuleConfig res = INSTANCE.get(fileSystem);
        if (res == null) {
            res = new HgModuleConfig(fileSystem);
            INSTANCE.put(fileSystem, res);
        }
        return res;
    }
    
    private HgModuleConfig(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        setDefaultPath();
        MercurialVCS mercurialVCS = Mercurial.getInstance().getMercurialVCS();
        if (mercurialVCS != null) {
            getPreferences().addPreferenceChangeListener(mercurialVCS);
        }
    }

    private void setDefaultPath() {
        // Set default executable location for mercurial on mac
        if (fileSystem != null && VCSFileProxySupport.isMac(VCSFileProxy.createFileProxy(fileSystem.getRoot()))) { // NOI18N
            String defaultPath = getExecutableBinaryPath ();
            if (defaultPath == null || defaultPath.length() == 0) {
                String[] pathNames  = {"/Library/Frameworks/Python.framework/Versions/Current/bin", // NOI18N
                                        "/usr/bin", "/usr/local/bin","/opt/local/bin/", "/sw/bin"}; // NOI18N
                for (int i = 0; i < pathNames.length; i++) {
                    if (isExecPathValid(pathNames[i])) {
                        setExecutableBinaryPath (pathNames[i]); // NOI18N
                        break;
                     }
                 }
            }
        }
    }
    
    private Set<String> exclusions;
    private final Map<String, String> lastCanceledCommitMessages = new HashMap<>(5);

    // properties ~~~~~~~~~~~~~~~~~~~~~~~~~

    public final Preferences getPreferences() {
        if (fileSystem == null) {
            return NbPreferences.forModule(HgModuleConfig.class).node("commonRemoteMercurial"); //NOI18N
        } else {
            return NbPreferences.forModule(HgModuleConfig.class).node(VCSFileProxySupport.getFileSystemKey(fileSystem));
        }
    }
    
    public boolean getShowCloneCompleted() {
        return getPreferences().getBoolean(SHOW_CLONE_COMPLETED, true);
    }
    
    public Pattern [] getIgnoredFilePatterns() {
        return getDefaultFilePatterns();
    }

    public boolean getShowFileInfo() {
        return getPreferences().getBoolean(KEY_SHOW_FILE_INFO, false);
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
            Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<>(exclusions));
        }
    }

    /**
     * @param paths collection of paths, File.getAbsolutePath()
     */
    public void removeExclusionPaths(Collection<String> paths) {
        Set<String> exclusions = getCommitExclusions();
        if (exclusions.removeAll(paths)) {
            Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<>(exclusions));
        }
    }

    public String getExecutableBinaryPath() {
        return (String) getPreferences().get(KEY_EXECUTABLE_BINARY, ""); // NOI18N
    }
    public boolean getBackupOnRevertModifications() {
        return getPreferences().getBoolean(KEY_BACKUP_ON_REVERTMODS, true);
    }
    
    public void setBackupOnRevertModifications(boolean bBackup) {
        getPreferences().putBoolean(KEY_BACKUP_ON_REVERTMODS, bBackup);
    }
    
    public boolean getShowHistoryMerges() {
        return getPreferences().getBoolean(KEY_SHOW_HITORY_MERGES, true);
    }

    public void setShowHistoryMerges(boolean bShowMerges) {
        getPreferences().putBoolean(KEY_SHOW_HITORY_MERGES, bShowMerges);
    }
    
    public void setShowFileInfo(boolean info) {
        getPreferences().putBoolean(KEY_SHOW_FILE_INFO, info);
    }

    public void setExecutableBinaryPath(String path) {
        if(path.endsWith(HgCommand.HG_COMMAND)){
            path = path.substring(0, path.length() - HgCommand.HG_COMMAND.length());            
        }
        getPreferences().put(KEY_EXECUTABLE_BINARY, path);
    }

    public String getExportFolder() {
        return (String) getPreferences().get(KEY_EXPORT_FOLDER, System.getProperty("user.home")); // NOI18N
    }
    
    public void setExportFolder(String path) {
        getPreferences().put(KEY_EXPORT_FOLDER, path);
    }

    public String getImportFolder() {
        return (String) getPreferences().get(KEY_IMPORT_FOLDER, System.getProperty("user.home")); // NOI18N
    }
    
    public void setImportFolder(String path) {
        getPreferences().put(KEY_IMPORT_FOLDER, path);
    }

    public String getExportFilename() {
        String str = (String) getPreferences().get(KEY_EXPORT_FILENAME, ""); // NOI18N
        if (str.trim().length() == 0) {
            str = DEFAULT_EXPORT_FILENAME;
        }
        return str;
    }
    
    public void setExportFilename(String path) {
        getPreferences().put(KEY_EXPORT_FILENAME, path);
    }

    /**
     * one for all file systems
     * @return 
     */
    public boolean getAutoOpenOutput() {
        return getPreferences().getBoolean(AUTO_OPEN_OUTPUT_WINDOW, true);
    }

    public void setAutoOpenOutput(boolean value) {
        getPreferences().putBoolean(AUTO_OPEN_OUTPUT_WINDOW, value);
    }

    public boolean getConfirmCommitAfterMerge () {
        return getPreferences().getBoolean(CONFIRM_BEFORE_COMMIT_AFTER_MERGE, false);
    }

    public void setConfirmCommitAfterMerge (boolean value) {
        getPreferences().putBoolean(CONFIRM_BEFORE_COMMIT_AFTER_MERGE, value);
    }


    /**
     * This method returns the username specified in $HOME/.hgrc
     * or /etc/mercurial/hgrc 
     * or a default username if none is found.
     */
    public String getSysUserName() {
        userName = HgConfigFiles.getSysInstance(VCSFileProxy.createFileProxy(fileSystem.getRoot())).getSysUserName();
        if (userName.length() == 0) {
            String userId = System.getProperty("user.name"); // NOI18N
            String hostName;
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (Exception ex) {
                hostName = "localhost"; //NOI18N
            }
            userName = userId + "@" + hostName; // NOI18N
        }
        return userName;
    }

    private String getSysPushPath(VCSFileProxy file) {
        return HgConfigFiles.getSysInstance(file).getSysPushPath();
    }
    
    private String getSysPullPath(VCSFileProxy file) {
        return HgConfigFiles.getSysInstance(file).getSysPullPath();
    }

    public void setUserName(String name) throws IOException {
        HgConfigFiles hcf = HgConfigFiles.getSysInstance(VCSFileProxy.createFileProxy(fileSystem.getRoot()));
        if (hcf.getException() == null) {
            hcf.setUserName(name);
        } else {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": Cannot set username property"); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hcf.getException());
            throw hcf.getException();
        }
    }

    public Boolean isUserNameValid(String name) {
        if (userName == null) {
            getSysUserName();
        }
        if (name.equals(userName)) {
            return true;
        }
        return !name.trim().isEmpty();
    }

    public Boolean isExecPathValid(String name) {
        if (name.length() == 0) {
            return true;
        }
        VCSFileProxy file = VCSFileProxySupport.getResource(fileSystem, name + "/" + HgCommand.HG_COMMAND); // NOI18N
        // I would like to call canExecute but that requires Java SE 6.
        if(file.exists() && file.isFile()) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param file
     * @return null in case of a parsing error
     */
    public Properties getProperties(VCSFileProxy file) {
        Properties props = new Properties();
        HgConfigFiles hgconfig = new HgConfigFiles(file);
        if (hgconfig.getException() != null) {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": cannot load configuration file"); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hgconfig.getException());
            notifyParsingError();
            return null;
        }
        String name = hgconfig.getUserName(false);
        if (name.length() == 0) {
            name = getSysUserName();
        }
        if (name.length() > 0) { 
            props.setProperty("username", name); // NOI18N
        } else {
            props.setProperty("username", ""); // NOI18N
        }
        
        name = hgconfig.getDefaultPull(false);
        if (name.length() == 0) {
            name = getSysPullPath(file);
        }
        if (name.length() > 0) { 
            props.setProperty("default-pull", name); // NOI18N
        } else {
            props.setProperty("default-pull", ""); // NOI18N
        }
        
        name = hgconfig.getDefaultPush(false);
        if (name.length() == 0) {
            name = getSysPushPath(file);
        }
        if (name.length() > 0) { 
            props.setProperty("default-push", name); // NOI18N
        } else {
            props.setProperty("default-push", ""); // NOI18N
        }
        
        return props;
    }

    public void clearProperties(VCSFileProxy file, String section) throws IOException {
        HgConfigFiles hcf = getHgConfigFiles(file);
        if (hcf.getException() == null) {
            hcf.clearProperties(section);
        } else {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": cannot clear properties for {0}", new VCSFileProxy[] {file}); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hcf.getException());
            throw hcf.getException();
        }
    }

    public void removeProperty(VCSFileProxy file, String section, String name) throws IOException {
        HgConfigFiles hcf = getHgConfigFiles(file);
        if (hcf.getException() == null) {
            hcf.removeProperty(section, name);
        } else {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": cannot remove property {0} for {1}", new Object[] {name, file}); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hcf.getException());
            throw hcf.getException();
        }
    }

    public void setProperty(VCSFileProxy file, String name, String value) throws IOException {
        HgConfigFiles hcf = getHgConfigFiles(file);
        if (hcf.getException() == null) {
            hcf.setProperty(name, value);
        } else {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": cannot set property {0}:{1} for {2}", new Object[] {name, value, file}); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hcf.getException());
            throw hcf.getException();
        }
    }

    public void setProperty(VCSFileProxy file, String section, String name, String value, boolean allowEmpty) throws IOException {
        HgConfigFiles hcf = getHgConfigFiles(file);
        if (hcf.getException() == null) {
            hcf.setProperty(section, name, value, allowEmpty);
        } else {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": cannot set property {0}:{1} for {2}", new Object[] {name, value, file}); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hcf.getException());
            throw hcf.getException();
        }
    }

    public void setProperty(VCSFileProxy file, String section, String name, String value) throws IOException {
        HgConfigFiles hcf = getHgConfigFiles(file);
        if (hcf.getException() == null) {
            hcf.setProperty(section, name, value);
        } else {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": cannot set property {0}:{1} for {2}", new Object[] {name, value, file}); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hcf.getException());
            throw hcf.getException();
        }
    }

    /*
     * Get all properties for a particular section
     */
    public Properties getProperties(VCSFileProxy file, String section) {
        return getHgConfigFiles(file).getProperties(section);
    }

    public boolean getExludeNewFiles () {
        return getPreferences().getBoolean(PROP_EXCLUDE_NEW_FILES, false);
    }

    public void setExcludeNewFiles (boolean excludeNewFiles) {
        getPreferences().putBoolean(PROP_EXCLUDE_NEW_FILES, excludeNewFiles);
    }

    public int getDiffViewMode (int def) {
        return getPreferences().getInt(PROP_DIFF_VIEW_MODE, def);
    }

    public void setDiffViewMode (int value) {
        getPreferences().putInt(PROP_DIFF_VIEW_MODE, value);
    }

    public void putRecentCommitAuthors (String user) {
        Utils.insert(getPreferences(), PROP_RECENT_COMMIT_AUTHORS, user, 10);
    }

    public List<String> getRecentCommitAuthors () {
        return Utils.getStringList(getPreferences(), PROP_RECENT_COMMIT_AUTHORS);
    }

    private HgConfigFiles getHgConfigFiles(VCSFileProxy file) {
        if (file == null) {
            return HgConfigFiles.getSysInstance(VCSFileProxy.createFileProxy(fileSystem.getRoot()));
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

    public boolean isInternalMergeToolEnabled() {
        return getPreferences().getBoolean(KEY_INTERNAL_MERGE_TOOL_ENABLED, true);
    }

    public void setInternalMergeToolEnabled (boolean enabled) {
        getPreferences().putBoolean(KEY_INTERNAL_MERGE_TOOL_ENABLED, enabled);
    }

    public boolean getSavePassword() {
        return getPreferences().getBoolean(SAVE_PASSWORD, true);
    }

    public void setSavePassword(boolean bl) {
        getPreferences().putBoolean(SAVE_PASSWORD, bl);
    }

    public void setShowCloneCompleted(boolean bl) {
        getPreferences().putBoolean(SHOW_CLONE_COMPLETED, bl);
    }
    
    public void insertRecentUrl(final RepositoryConnection rc) {
        Preferences prefs = getPreferences();

        for (String rcOldString : Utils.getStringList(prefs, RECENT_URL)) {
            RepositoryConnection rcOld;
            try {
                rcOld = RepositoryConnection.parse(rcOldString);
            } catch (URISyntaxException ex) {
                Mercurial.LOG.throwing(getClass().getName(),
                                       "insertRecentUrl",               //NOI18N
                                       ex);
                continue;
            }
            if(rcOld.equals(rc)) {
                Utils.removeFromArray(prefs, RECENT_URL, rcOldString);
            }
        }
        final char[] password = rc.getUrl().getPassword();
        if (password != null) {
            Runnable outOfAWT = new Runnable() {
                @Override
                public void run() {
                    KeyringSupport.save(HgUtils.PREFIX_VERSIONING_MERCURIAL_URL, rc.getUrl().toHgCommandStringWithNoPassword(), password.clone(), null);
                }
            };
            // keyring should be called only in a background thread
            if (EventQueue.isDispatchThread()) {
                Mercurial.getInstance().getRequestProcessor().post(outOfAWT);
            } else {
                outOfAWT.run();
            }
        }
        Utils.insert(prefs, RECENT_URL, RepositoryConnection.getString(rc), -1);
    }    

    public List<RepositoryConnection> getRecentUrls() {
        Preferences prefs = getPreferences();
        List<String> urls = Utils.getStringList(prefs, RECENT_URL);
        List<RepositoryConnection> ret = new ArrayList<>(urls.size());
        List<RepositoryConnection> withPassword = new LinkedList<>();
        for (String urlString : urls) {
            try {
                RepositoryConnection conn = RepositoryConnection.parse(urlString);
                char[] password = conn.getUrl().getPassword();
                if (password != null) {
                    withPassword.add(conn);
                } else {
                    ret.add(conn);
                }
            } catch (URISyntaxException ex) {
                Mercurial.LOG.throwing(getClass().getName(),
                                       "getRecentUrls",                 //NOI18N
                                       ex);
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

    /**
     * Notifies user of parsing error.
     */
    public static void notifyParsingError() {
        NotifyDescriptor nd = new NotifyDescriptor(
                NbBundle.getMessage(HgModuleConfig.class, "MSG_ParsingError"), // NOI18N
                NbBundle.getMessage(HgModuleConfig.class, "LBL_ParsingError"), // NOI18N
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.ERROR_MESSAGE,
                new Object[]{NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION},
                NotifyDescriptor.OK_OPTION);
        if (EventQueue.isDispatchThread()) {
            DialogDisplayer.getDefault().notify(nd);
        } else {
            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }

    public Color getColor(String colorName, Color defaultColor) {
         int colorRGB = getPreferences().getInt(colorName, defaultColor.getRGB());
         return new Color(colorRGB);
    }

    public void setColor(String colorName, Color value) {
         getPreferences().putInt(colorName, value.getRGB());
    }

    public String getLastCanceledCommitMessage (String key) {
        String lastCanceledCommitMessage = lastCanceledCommitMessages.get(key);
        return lastCanceledCommitMessage == null ? "" : lastCanceledCommitMessage; //NOI18N
    }

    public void setLastCanceledCommitMessage (String key, String message) {
        if (message == null || message.isEmpty()) {
            lastCanceledCommitMessages.remove(key);
        } else {
            lastCanceledCommitMessages.put(key, message);
        }
    }
    
    public String getLastUsedQPatchMessage (String patchName) {
        return getPreferences().get(KEY_QPATCH_MESSAGE + patchName, ""); //NOI18N
    }
    
    public void setLastUsedQPatchMessage (String patchName, String message) {
        if (message == null) {
            getPreferences().remove(KEY_QPATCH_MESSAGE + patchName);
        } else {
            getPreferences().put(KEY_QPATCH_MESSAGE + patchName, message);
        }
    }

    public boolean isSearchOnBranchEnabled (String branchName) {
        return getPreferences().getBoolean(KEY_SEARCH_ON_BRANCH + branchName, true);
    }

    public void setSearchOnBranchEnabled (String branchName, boolean enabled) {
        getPreferences().putBoolean(KEY_SEARCH_ON_BRANCH + branchName, enabled);
    }

    public boolean isRemoveNewFilesOnRevertModifications () {
        return getPreferences().getBoolean(KEY_REMOVE_NEW_FILES_ON_REVERT, true);
    }

    public void setRemoveNewFilesOnRevertModifications (boolean flag) {
        getPreferences().putBoolean(KEY_REMOVE_NEW_FILES_ON_REVERT, flag);
    }
    
    synchronized Set<String> getCommitExclusions() {
        if (exclusions == null) {
            exclusions = new HashSet<>(Utils.getStringList(getPreferences(), PROP_COMMIT_EXCLUSIONS));
        }
        return exclusions;
    }
    
    // private methods ~~~~~~~~~~~~~~~~~~

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
