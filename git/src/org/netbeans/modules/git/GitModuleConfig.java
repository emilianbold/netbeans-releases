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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git;

import java.awt.Color;
import java.awt.EventQueue;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import org.netbeans.libs.git.utils.GitURI;
import org.netbeans.modules.git.FileInformation.Mode;
import org.netbeans.modules.versioning.util.KeyringSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbPreferences;

/**
 *
 * @author ondra
 */
public final class GitModuleConfig {
    
    private static GitModuleConfig instance;
    private static final String AUTO_OPEN_OUTPUT_WINDOW = "autoOpenOutput";     // NOI18N
    private static final String PROP_COMMIT_EXCLUSIONS  = "commitExclusions";   // NOI18N
    private static final String PROP_LAST_USED_MODE     = "lastUsedMode";       // NOI18N
    private static final String EXCLUDE_NEW_FILES       = "excludeNewFiles";    // NOI18N
    private static final String RECENT_COMMIT_AUTHORS   = "recentCommitAuhtors";// NOI18N
    private static final String RECENT_COMMITERS        = "recentCommiters";    // NOI18N
    private static final String RECENT_GURI             = "recent_guri";    
    private static final String SIGN_OFF                = "signOff";            // NOI18N
    private static final String REVERT_ALL              = "revertAll";          // NOI18N
    private static final String REMOVE_ALL_NEW          = "removeAllNew";       // NOI18N
    private static final String REVERT_INDEX            = "revertIndex";        // NOI18N
    private static final String REVERT_WT               = "revertWT";           // NOI18N
    private static final String REMOVE_WT_NEW           = "removeWTNew";        // NOI18N
    private static final String PROP_LAST_USED_COMMIT_VIEW_MODE = "lastUsedCommitViewMode"; //NOI18N
    private static final String AUTO_IGNORE_FILES       = "autoIgnoreFiles"; //NOI18N
    private static final String SHOW_CLONE_COMPLETED    = "cloneCompleted.showCloneCompleted";        // NOI18N  
    private static final String GURI_PASSWORD           = "guri_password";
    
    private static final String DELIMITER               = "<=~=>";              // NOI18N
    private static final String KEY_SHOW_HISTORY_MERGES = "showHistoryMerges"; //NOI18N
    private static final String KEY_SHOW_FILE_INFO = "showFileInfo"; //NOI18N
    
    private String lastCanceledCommitMessage;
    
    public static GitModuleConfig getDefault () {
        if (instance == null) {
            instance = new GitModuleConfig();
        }
        return instance;
    }
    private Set<String> exclusions;
    
    private GitModuleConfig() { }

    public boolean isExcludedFromCommit(String path) {
        Set<String> commitExclusions = getCommitExclusions();        
        return commitExclusions.contains(path);
    }

    public Color getColor(String colorName, Color defaultColor) {
         int colorRGB = getPreferences().getInt(colorName, defaultColor.getRGB());
         return new Color(colorRGB);
    }

    public void setColor(String colorName, Color value) {
         getPreferences().putInt(colorName, value.getRGB());
    }

    public Preferences getPreferences() {
        return NbPreferences.forModule(GitModuleConfig.class);
    }

    public boolean getExludeNewFiles() {
        return getPreferences().getBoolean(EXCLUDE_NEW_FILES, false);
    }

    public void setExcludeNewFiles(boolean value) {
        getPreferences().putBoolean(EXCLUDE_NEW_FILES, value);
    }
    
    public String getLastCanceledCommitMessage() {
        return lastCanceledCommitMessage == null ? "" : lastCanceledCommitMessage; //NOI18N
    }

    public void setLastCanceledCommitMessage(String message) {
        lastCanceledCommitMessage = message;
    }
    
    /**
     * @param paths collection of paths, of File.getAbsolutePath()
     */
    public void addExclusionPaths(Collection<String> paths) {
        Set<String> commitExclusions = getCommitExclusions();
        if (commitExclusions.addAll(paths)) {
            Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<String>(commitExclusions));
        }
    }

    /**
     * @param paths collection of paths, File.getAbsolutePath()
     */
    public void removeExclusionPaths(Collection<String> paths) {
        Set<String> commitExclusions = getCommitExclusions();
        if (commitExclusions.removeAll(paths)) {
            Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<String>(commitExclusions));
        }
    }   
    
    synchronized Set<String> getCommitExclusions() {
        if (exclusions == null) {
            exclusions = new HashSet<String>(Utils.getStringList(getPreferences(), PROP_COMMIT_EXCLUSIONS));
        }
        return exclusions;
    }  
    
    public Mode getLastUsedModificationContext () {
        Mode mode;
        try {
            mode = Mode.valueOf(getPreferences().get(PROP_LAST_USED_MODE, Mode.HEAD_VS_WORKING_TREE.name()));
        } catch (IllegalArgumentException ex) {
            mode = null;
        }
        return mode == null ? Mode.HEAD_VS_WORKING_TREE : mode;
    }

    public void setLastUsedModificationContext (Mode mode) {
        getPreferences().put(PROP_LAST_USED_MODE, mode.name());
    }    

    public Mode getLastUsedCommitViewMode () {
        Mode mode;
        try {
            mode = Mode.valueOf(getPreferences().get(PROP_LAST_USED_COMMIT_VIEW_MODE, Mode.HEAD_VS_WORKING_TREE.name()));
        } catch (IllegalArgumentException ex) {
            mode = null;
        }
        return mode == null ? Mode.HEAD_VS_WORKING_TREE : mode;
    }

    public void setLastUsedCommitViewMode (Mode mode) {
        getPreferences().put(PROP_LAST_USED_COMMIT_VIEW_MODE, mode.name());
    }
    
    public boolean getAutoOpenOutput() {
        return getPreferences().getBoolean(AUTO_OPEN_OUTPUT_WINDOW, true);
    }

    public void setAutoOpenOutput(boolean value) {
        getPreferences().putBoolean(AUTO_OPEN_OUTPUT_WINDOW, value);
    }

    public void putRecentCommitAuthors(String author) {
        if(author == null) return;
        author = author.trim();
        if(author.isEmpty()) return;
        Utils.insert(getPreferences(), RECENT_COMMIT_AUTHORS, author, 10);
    }
    
    public void putRecentCommiter(String commiter) {
        if(commiter == null) return;
        commiter = commiter.trim();
        if(commiter.isEmpty()) return;
        Utils.insert(getPreferences(), RECENT_COMMITERS, commiter, 10);
    }
    
    public List<String> getRecentCommitAuthors() {
        return Utils.getStringList(getPreferences(), RECENT_COMMIT_AUTHORS);
    }
    
    public List<String> getRecentCommiters() {
        return Utils.getStringList(getPreferences(), RECENT_COMMITERS);
    }

    public void setSignOff(boolean value) {
        getPreferences().putBoolean(SIGN_OFF, value);
    }
    
    public boolean getSignOff() {
        return getPreferences().getBoolean(SIGN_OFF, false);
    }
    
    public void putRevertAll(boolean value) {
        getPreferences().putBoolean(REVERT_ALL, value);        
    }
    
    public void putRevertIndex(boolean value) {
        getPreferences().putBoolean(REVERT_INDEX, value);        
    }
    
    public void putRevertWT(boolean value) {
        getPreferences().putBoolean(REVERT_WT, value);        
    }
    
    public void putRemoveAllNew(boolean value) {
        getPreferences().putBoolean(REMOVE_ALL_NEW, value);        
    }
    
    public void putRemoveWTNew(boolean value) {
        getPreferences().putBoolean(REMOVE_WT_NEW, value);        
    }
    
    public boolean getRevertAll() {
        return getPreferences().getBoolean(REVERT_ALL, true);        
    }
    
    public boolean getRevertIndex() {
        return getPreferences().getBoolean(REVERT_INDEX, false);        
    }
    
    public boolean getRevertWT() {
        return getPreferences().getBoolean(REVERT_WT, true);        
    }
        
    public boolean getRemoveWTNew() {
        return getPreferences().getBoolean(REMOVE_WT_NEW, false);                
    }
    
    public boolean getRemoveAllNew() {
        return getPreferences().getBoolean(REMOVE_ALL_NEW, false);                
    }

    public boolean getAutoIgnoreFiles () {
        return getPreferences().getBoolean(AUTO_IGNORE_FILES, true);
    }

    public void setAutoIgnoreFiles (boolean flag) {
        getPreferences().putBoolean(AUTO_IGNORE_FILES, flag);
    }

    public boolean getShowCloneCompleted() {
        return getPreferences().getBoolean(SHOW_CLONE_COMPLETED, true);
    }
    
    public void setShowCloneCompleted(boolean bl) {
        getPreferences().putBoolean(SHOW_CLONE_COMPLETED, bl);
    }

    public boolean getShowHistoryMerges() {
        return getPreferences().getBoolean(KEY_SHOW_HISTORY_MERGES, true);
    }

    public void setShowHistoryMerges(boolean bShowMerges) {
        getPreferences().putBoolean(KEY_SHOW_HISTORY_MERGES, bShowMerges);
    }
    public boolean getShowFileInfo() {
        return getPreferences().getBoolean(KEY_SHOW_FILE_INFO, false);
    }
    public void setShowFileInfo(boolean info) {
        getPreferences().putBoolean(KEY_SHOW_FILE_INFO, info);
    }
    
    private final HashMap<String, GitURI> cachedUris = new HashMap<String, GitURI>(5);
    public void insertRecentGitURI(GitURI guri, boolean saveCredentials) {
        assert !EventQueue.isDispatchThread();
        String guriString = getUriStringWithoutCredentials(guri);
        if(guriString == null) {
            return;
        }        
        Preferences prefs = getPreferences();
        removeStaleEntries(prefs, guriString);
        
        if(saveCredentials && guri.getPass() != null) {
            // keep permanently and remove from cache
            storeCredentials(guri);
            cachedUris.remove(guriString);
        } else {
            // remove from perm storage, we should not keep it forever
            deleteCredentials(guri);
            // but keep until end of session
            cachedUris.put(guriString, guri);
        }
        
        if (!"".equals(guriString)) {                                           //NOI18N
            Utils.insert(prefs, RECENT_GURI, new GitURIEntry(guriString, guri.getUser(), saveCredentials).toString() , -1);
        }
    }

    public void removeRecentGitURI (GitURI guri) {
        assert !EventQueue.isDispatchThread();
        String guriString = getUriStringWithoutCredentials(guri);
        if(guriString == null) {
            return;
        }        
        Preferences prefs = getPreferences();
        removeStaleEntries(prefs, guriString);
        
        cachedUris.remove(guriString);
        deleteCredentials(guri);
    }

    private void removeStaleEntries (Preferences prefs, String guriString) {
        List<String> urlValues = Utils.getStringList(prefs, RECENT_GURI);
        for (Iterator<String> it = urlValues.iterator(); it.hasNext();) {
            String rcOldString = it.next();
            GitURI guriOld = null;
            try {
                GitURIEntry entry = GitURIEntry.create(rcOldString);
                if(entry != null) {
                    guriOld = new GitURI(entry.guriString);
                }
            } catch (URISyntaxException ex) {
                Git.LOG.log(Level.WARNING, rcOldString, ex);
            }
            if(guriString.equals(guriOld.toString())) {
                Utils.removeFromArray(prefs, RECENT_GURI, rcOldString);
            }
        }
    }    
    
    public List<GitURI> getRecentUrls() {
        assert !EventQueue.isDispatchThread();
        
        Preferences prefs = getPreferences();
        List<String> urls = Utils.getStringList(prefs, RECENT_GURI);
        List<GitURI> ret = new ArrayList<GitURI>(urls.size());
        for (String guriString : urls) {
            GitURIEntry entry = GitURIEntry.create(guriString);
            // before we contact keyring, check the cache:
            GitURI guri = cachedUris.get(entry.guriString);
            if (guri == null) {
                try {
                    guri = new GitURI(entry.guriString);
                    if (!entry.username.isEmpty()) {
                        guri = guri.setUser(entry.username);
                    }
                } catch (URISyntaxException ex) {
                    Git.LOG.log(Level.WARNING, guriString, ex);
                    continue;
                }
                char[] password = KeyringSupport.read(GURI_PASSWORD, guri.toString());
                if(password != null) {
                    guri = guri.setPass(new String(password));
                }
            }
            ret.add(guri);
        }
        return ret;
    }
    
    public GitURI getRecentUri (String uriString) {
        assert !EventQueue.isDispatchThread();
        GitURI retval = null;
        String username = null;
        try {
            GitURI uri = new GitURI(uriString);
            username = uri.getUser();
            uriString = uri.setUser(null).setPass(null).toString();
        } catch (URISyntaxException ex) {
            //
        }
        
        // before we contact keyring, check the cache:
        GitURI cachedUri = cachedUris.get(uriString);
        if (cachedUri != null && (username == null || cachedUri.getUser() == null || username.equals(cachedUri.getUser()))) {
            return cachedUri;
        }
        
        Preferences prefs = getPreferences();
        List<String> urls = Utils.getStringList(prefs, RECENT_GURI);
        for (String guriString : urls) {
            GitURIEntry entry = GitURIEntry.create(guriString);
            GitURI storedUri;
            try {
                storedUri = new GitURI(entry.guriString);
            } catch (URISyntaxException ex) {
                Git.LOG.log(Level.WARNING, guriString, ex);
                continue;
            }
            if (uriString.equals(storedUri.toString()) && (username == null || entry.username == null || username.equals(entry.username))) {
                if (!entry.username.isEmpty()) {
                    storedUri = storedUri.setUser(entry.username);
                }
                char[] password = KeyringSupport.read(GURI_PASSWORD, storedUri.toString());
                if(password != null) {
                    storedUri = storedUri.setPass(new String(password));
                }
                retval = storedUri;
                break;
            }
        }
        return retval;
    }

    private void storeCredentials (final GitURI guri) {
        assert guri.getPass() != null;
        assert !EventQueue.isDispatchThread();
        String passwd = guri.getPass();
        KeyringSupport.save(GURI_PASSWORD, guri.toString(), passwd.toCharArray(), null);
    }
    
    private void deleteCredentials (final GitURI guri) {
        assert !EventQueue.isDispatchThread();
        KeyringSupport.save(GURI_PASSWORD, guri.toString(), null, null);
    }

    private String getUriStringWithoutCredentials (GitURI guri) {
        String guriString = null;
        if(guri != null) {
            try {
                // we need to compare all uris with the one without username or password
                guriString = new GitURI(guri.toString()).setUser(null).setPass(null).toString();
            } catch (URISyntaxException ex) {
                Git.LOG.log(Level.WARNING, guri.toPrivateString(), ex);
            }
        }
        return guriString;
    }
    
    private static class GitURIEntry {
        final String guriString;
        private final boolean saveCredentials;
        private String stringValue;
        private final String username;
        static GitURIEntry create(String entryString) {
            String[] s = entryString.split(DELIMITER);
            assert s.length == 2 || s.length == 3;
            if(s.length < 2) {
                return null;
            }
            return s.length == 2 ? new GitURIEntry(s[0], null, Boolean.parseBoolean(s[1])) : new GitURIEntry(s[0], s[1], Boolean.parseBoolean(s[2]));
        }
        GitURIEntry(String guriString, String username, boolean saveCredentials) {
            this.guriString = guriString;
            this.username = username == null ? "" : username; //NOI18N
            this.saveCredentials = saveCredentials;
        }
        @Override
        public String toString() {
            if(stringValue == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(guriString);
                sb.append(DELIMITER);
                sb.append(saveCredentials ? username : ""); //NOI18N
                sb.append(DELIMITER);
                sb.append(saveCredentials);
                stringValue = sb.toString();
            }
            return stringValue;
        }
    }
    
}
