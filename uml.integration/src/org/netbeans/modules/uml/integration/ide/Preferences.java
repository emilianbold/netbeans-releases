/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * File         : Preferences.java
 * Version      : 1.00
 * Description  : Describe preference manager for preferences affecting
 *                integrations.
 * Author       : Gautam Sabba
 */
package org.netbeans.modules.uml.integration.ide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;

/**
 * Describe preference manager for preferences affecting integrations.
 */
public class Preferences {
    /**
     * Interface for classes that want to be notified of when a preference
     * changes.
     *
     * @author darshans
     */
    public static interface PreferenceWatcher {
        /**
         * Notification that the preference specified by the given preference
         * key has changed from oldV to newV.
         *
         * @param preferencePath The preference key as its absolute path.
         * @param oldV           The old value of the preference. This is not
         *                       guaranteed to be accurate; if not available,
         *                       <code>null</code> is passed.
         * @param newV           The new value of the preference.
         */
        public void preferenceChanged(String preferencePath, String oldV,
                                      String newV);
    }

    public static final String PSK_YES = "PSK_YES";
    public static final String PSK_NO  = "PSK_NO";
    public static final String PSK_ASK  = "PSK_ASK";
    public static final String PSK_NEVER  = "PSK_NEVER";
    public static final String PSK_ALWAYS  = "PSK_ALWAYS";

    public static final String PROMPT_SAVE_WKS =
                                "Workspace|ProjectSave";
    public static final String PROMPT_WKS_LOC =
                                "Workspace|PromptForTargetWS";
    public static final String PROMPT_PRJ_LOC =
                                "Workspace|ProjectLocationQuery";
    public static final String DEFAULT_WKS_LOC =
                                "Integrations|NewProject|DefaultWSLocation";
    public static final String DEFAULT_ELEM_NAME =
                                "NewProject|DefaultElementName";
    public static final String COLLECTION_OVERRIDE =
                                "RoundTrip|Java|COLLECTION_OVERRIDE_DEFAULT";
    public static final String USE_GENERICS_DEFAULT =
                                "RoundTrip|Java|USE_GENERICS_DEFAULT";
    public static final String QUERY_NEW_DIAGRAM =
                                "NewProject|QueryForNewDiagram";
    public static final String RECONNECT_LINKS =
                                "Diagrams|ReconnectToNodeBoundary";
    public static final String LOG_DESCRIBE_MESSAGES =
                   "LoggingInformation|LogOutputDescribeMessages";
    public static final String CONFIRM_SOURCE_DELETE =
                   "ArtifactDeleteDeletesFile";


    /**
     * Adds a preference watcher for the given preference. Watchers are notified
     * whenever their associated preference changes (or if their associated
     * preference is <code>null</code>, whenever any <em>watched</em> preference
     * changes). Explicitly specifying a preference to this function
     * automatically adds it to the list of watched preferences; if you specify
     * <code>null</code>, you'll have to add the list of preferences you're
     * interested in to the watch list separately.
     * <br/>
     * The list of watched preferences is independent of whether we're
     * connected to Describe (i.e., disconnect/reconnect will not affect the
     * watch list).
     * <br/>
     * Preference change notifications are fired only when readPreferences()
     * is called, i.e., there is no magic behind the scenes to fire events as
     * soon as the user/code changes a preference. However, integrations are
     * expected to call readPreferences() each time the Describe preferences
     * dialog is dismissed, so that should be academic.
     *
     * @param pref The preference to watch. If not <code>null</code>, this
     *             preference is added to the watch list.
     * @param watcher The watcher to be notified when the associated preference
     *                changes.
     * @param fireInitial If <code>true</code>, immediately fire a
     *                    preferenceChanged() to the watcher with the current
     *                    preference value.
     */
    synchronized public static void addPreferenceWatcher(String pref,
                                                    PreferenceWatcher watcher,
                                                    boolean fireInitial) {
        if (watcher == null) {
            throw new IllegalArgumentException("Can't add null watcher");
        }
        addWatch(pref);

        if (pref == null)
            pref = "";
        if (watchers == null)
            watchers = new HashMap();

        ArrayList iwatchers = null;
        if (watchers.containsKey(pref))
            iwatchers = (ArrayList) watchers.get(pref);
        else {
            iwatchers = new ArrayList();
            watchers.put(pref, iwatchers);
        }

        if (!iwatchers.contains(watcher))
            iwatchers.add(watcher);

        if (fireInitial && pref.length() > 0) {
            Log.out("Preferences.addPreferenceWatcher: Firing initial event " +
                "for " + pref + " to " + watcher);
            watcher.preferenceChanged(pref, null, getPreference(pref));
        }
    }

    /**
     * Removes a property watcher. Any keys which were exclusively watched by
     * this watcher will also be removed from the watch list.
     *
     * @param watcher The watcher that's no longer wanted.
     */
    synchronized public static void removePreferenceWatcher(
                                        PreferenceWatcher watcher) {
        if (watcher == null)
            throw new IllegalArgumentException("Can't remove null watcher");
        if (watchers == null)
            return ;

        Iterator ikey = watchers.keySet().iterator();
        while (ikey.hasNext()) {
            String pref = ikey.next().toString();

            ArrayList a = (ArrayList) watchers.get(pref);
            if (a == null)
                continue;

            if (a.remove(watcher) && a.size() == 0) {
                try {
                    ikey.remove();
                    removeWatch(pref);
                }
                catch (Exception e) {
                    Log.stackTrace(e);
                }
            }
        }
    }

    /**
     * Adds the given preference to the list of preferences that are being
     * watched. Changes to watched preferences fire events to their
     * corresponding watchers.
     *
     * @param pref The preference to watch. If <code>null</code>, this function
     *             returns silently.
     */
    synchronized public static void addWatch(String pref) {
        if (pref == null) return;
        if (watches == null)
            watches = new HashMap();
        if (!watches.containsKey(pref))
            watches.put(pref, null);
    }

    /**
     * Removes the given preference from the list of preferences that are being
     * watched.
     *
     * @param pref The preference that needn't be watched any longer. Silently
     *             ignores error conditions such as a <code>null</code> pref, or
     *             attempts to remove a preference that's not being watched.
     */
    synchronized public static void removeWatch(String pref) {
        if (watches != null && pref != null)
            watches.remove(pref);
    }

    /**
     * Reads all Describe preferences relevant to IDE integrations from the
     * Describe preference manager, if the integration is currently connected
     * to Describe. If not connected to Describe, this is a silent no-op. If
     * the default workspace location is unset, will call the integration
     * using IIDEManager.getDefaultWorkspaceDirectory() to get a default path.
     */
    public static void readPreferences() {
        if (!initPreferenceManager())
            return;

        try {
            clearCache();

            reconnectLinks = getBooleanPreference(RECONNECT_LINKS);
            promptSaveWorkspace = getBooleanPreference(PROMPT_SAVE_WKS);
            promptWksLocation = getBooleanPreference(PROMPT_WKS_LOC);
            promptProjectLocation = getBooleanPreference(PROMPT_PRJ_LOC);
            createNewDiagram = getBooleanPreference(QUERY_NEW_DIAGRAM);

            defaultWorkspaceLocation = getPreference(DEFAULT_WKS_LOC);
            defaultElementName = getPreference(DEFAULT_ELEM_NAME);
            collectionOverride = getPreference(COLLECTION_OVERRIDE);
            confirmSourceDelete = getPreference(CONFIRM_SOURCE_DELETE);

            readWatchedPreferences();
        }
        catch (ClobberedException ex) {
            Log.stackTrace(ex);
            UMLSupport.reviveDescribe();
            readPreferences();
        }
    }

    /**
     * Clears all cached preference values and forces requerying the Describe
     * preference manager the next time any preference is asked for. This is
     * called automatically by readPreferences().
     */
    public static void clearCache() {
        preferenceCache.clear();
    }

    /**
     * Reads all preferences that are being watched, and fires events for those
     * that have changed.
     */
    synchronized private static void readWatchedPreferences() {
        if (watches == null || watchers == null)
            return ;

        Iterator prefs = watches.keySet().iterator();
        while (prefs.hasNext()) {
            String pref = (String) prefs.next();
            if (pref == null) {
                try {
                    prefs.remove();
                }
                catch (Exception ignored) { }
                continue;
            }

            String newVal = getPreference(pref);
            String oldVal = (String) watches.get(pref);

            if ((newVal == null && oldVal == null) ||
                    (newVal != null && newVal.equals(oldVal)) ||
                    (oldVal != null && oldVal.equals(newVal))) {
                continue;
            }

            watches.put(pref, newVal);
            firePreferenceChanged(pref, oldVal, newVal);
        }
    }

    private static void firePreferenceChanged(String pref, String oldV,
                                              String newV) {
        if (watchers == null)
            return ;
        Collection ws = getWatchers(pref);
        firePreferenceChanged(ws, pref, oldV, newV);
        ws = getWatchers("");
        firePreferenceChanged(ws, pref, oldV, newV);
    }

    private static void firePreferenceChanged(Collection coll, String pref,
                                              String oldV, String newV) {
        if (coll == null)
            return ;
        Iterator iter = coll.iterator();
        while (iter.hasNext()) {
            PreferenceWatcher watcher = (PreferenceWatcher) iter.next();
            if (watcher != null) {
                try {
                    watcher.preferenceChanged(pref, oldV, newV);
                }
                catch (Exception e) {
                    Log.stackTrace(e);
                }
            }
        }
    }

    private static Collection getWatchers(String pref) {
        return (Collection) watchers.get(pref);
    }

    /**
     * Forgets the cached Describe preference manager. This should before
     * disconnecting from Describe (so that the preference manager proxy can be
     * garbage collected), and after a clobber-revive.
     */
    public static void reset() {
        prefMan = null;
    }

    /**
     * Returns whether integrations should prompt for the location to create a
     * new Describe project, when creating a Describe project for a newly
     * created IDE project.
     * @return <code>true</code> if the integration should prompt the user to
     *         choose a location for the new Describe project.
     */
    public static boolean isPromptProjectLocation() {
        return promptProjectLocation;
    }

    /**
     * Returns whether integrations should prompt to save the current Describe
     * workspace/project when switching between IDE projects.
     * @return <code>true</code> if the integration should prompt to save
     *         Describe metadata.
     */
    public static boolean isPromptSaveWorkspace() {
        return promptSaveWorkspace;
    }

    /**
     * Returns whether integrations should prompt for the location to create a
     * new Describe workspace, when creating or opening a Describe workspace for
     * a newly created IDE project.
     * @return <code>true</code> if the integration should prompt the user to
     *         choose a location for the new Describe workspace.
     */
    public static boolean isPromptWksLocation() {
        return promptWksLocation;
    }

    /**
     * Returns whether links are reconnected to presentations element
     * boundaries.
     *
     * @return <code>true</code> if the reconnect links preference is set.
     */
    public static boolean isReconnectLinks() {
        return reconnectLinks;
    }

    /**
     * Returns whether integrations should prompt the user to create a new
     * Describe diagram when connecting a new IDE project to Describe.
     * @return <code>true</code> if the integration should prompt the user to
     *         create a new diagram.
     */
    public static boolean isCreateNewDiagram() {
        //return createNewDiagram;
        // I wasn't sure how the whole watched preferences was working, but it
        // doesn't seem to be working in this case.  Not sure how this variable
        // would ever be updated.  So, changed to get the preference value whenever
        // this method is called
        preferenceCache.remove(QUERY_NEW_DIAGRAM);
	return getBooleanPreference(QUERY_NEW_DIAGRAM);
    }
    
    public static boolean getUseGenericsDefault()
    {
        preferenceCache.remove(USE_GENERICS_DEFAULT);
	return getBooleanPreference(USE_GENERICS_DEFAULT);
    }


    /**
     * Returns the absolute path to the Describe workspace to be used by default
     * when connecting to new IDE projects.
     *
     * @return <code>String</code> - the absolute path to the default Describe
     *         workspace .etw file.
     */
    public static String getDefaultWorkspacePath() {
        // Point to the location specified (i.e. <workspaceFolder>/Default.etw)
        // else if workspaceFolder == null
        // Point at default workspace:
        // ${DESCRIBEHOME}/Workspaces/Default/Default.etw

        if (defaultWorkspaceLocation == null
            || defaultWorkspaceLocation.trim().length() == 0) {
            UMLSupport gps = UMLSupport.getUMLSupport();

            String workspaceFolder = UMLSupport.getUMLSupport()
                .getIDEManager().getDefaultWorkspaceDirectory();
            Log.out("readPreferences: IDE's workspace folder : "
                    + workspaceFolder);
            try {
                File workspaceLocation = null;

                if (workspaceFolder != null &&
                    workspaceFolder.trim().length() > 0)
                    workspaceLocation = new File(new File(workspaceFolder),
                                                 DEF_WKS_FILE);
                else
                    workspaceLocation = new File(new File(gps.getApplication().
                        getInstallLocation()).getParentFile().getParentFile(),
                                                 REL_DEF_WKS_LOC);

                return workspaceLocation.toString();
            }
            catch (Exception ignored) {}
        }

        if (defaultWorkspaceLocation != null &&
            defaultWorkspaceLocation.trim().length() == 0)
            defaultWorkspaceLocation = null;
        return defaultWorkspaceLocation;
    }

    /**
     * Checks if the default location preference is set.     *
     */
    public static boolean isDefaultWorkspacePathEmpty() {
        return (defaultWorkspaceLocation == null ||
                defaultWorkspaceLocation.trim().length() == 0);
    }

    /**
     * Sets the absolute path to the Describe workspace to be used as the
     * default when connecting to new IDE projects.
     *
     * @param path A <code>String</code> with the absolute path to the workspace
     *             .etw file.
     */
    public static void setDefaultWorkspacePath(String path) {
        defaultWorkspaceLocation = path;
        setPreference(DEFAULT_WKS_LOC, path);
    }

    /**
     * Sets the preference state of "Prompt for workspace path".
     *
     * @param path A <code>true</code> for PSK_YES, <code>false</code> for
     *             PSK_NO.
     */
    public static void setPromptWksLocation(boolean state) {
        promptWksLocation = state;
        String val = (state)? PSK_YES : PSK_NO;
        setPreference(PROMPT_WKS_LOC, val);
    }

    /**
     * Sets the preference state of "Delete file with artifacts".
     *
     * @param val PSK_ASK, PSK_NEVER and PSK_ALWAYS.
     */
    public static void setDeleteFileWithArtifact(String val) {
        confirmSourceDelete = val;
        setPreference(CONFIRM_SOURCE_DELETE, val);
    }


    public static String getDeleteFileWithArtifact() {
        return confirmSourceDelete;
    }



    /**
     * Sets whether links are reconnected to presentation element boundaries.
     *
     * @param reconnectLinks <code>true</code> to reconnect links.
     */
    public static void setReconnectLinks(boolean reconnectLinks) {
        Preferences.reconnectLinks = reconnectLinks;
        setPreference(RECONNECT_LINKS, reconnectLinks? PSK_YES : PSK_NO);
    }

    /**
     * Returns the default name for unnamed elements in the Describe model.
     *
     * @return A <code>String</code> of the default name for unnamed elements.
     */
    public static String getDefaultElementName() {
        return defaultElementName;
    }

    /**
     * Returns the type to be used when substituting collection types for
     * array-type attributes.
     * @return <code>String</code> The name of the collection class to be used
     *         instead of arrays.
     */
    public static String getCollectionOverride() 
    {
        if (collectionOverride == null)
            collectionOverride =  getPreference(COLLECTION_OVERRIDE);
        
        return collectionOverride;
    }

    public static void removePreference(String prefKey) {
        if (prefKey == null || !initPreferenceManager())
            return;

        int pathDelimPos = prefKey.lastIndexOf("|");
        String path = (pathDelimPos != -1 ?
                       prefKey.substring(0, pathDelimPos) : ""),
            key = prefKey.substring(pathDelimPos + 1);

        IPropertyElement pe =  prefMan.getPreferenceElement(path, key);
        if(pe != null) {
            Log.out("removePreference(): Removing preference - " + prefKey);
            prefMan.removePreference(pe);
        }
        else
            Log.out("removePreference(): Could not locate preference"
                    + " to delete - " + prefKey);
    }


    private static boolean getBooleanPreference(String prefKey){
        String val = getPreference(prefKey);
        if(val == null && UMLSupport.isClobbered())
            throw new ClobberedException();
        return PSK_YES.equals(getPreference(prefKey));
    }

    private static String getPreference(String prefKey) {
        if (prefKey == null || !initPreferenceManager()) return null;

        if (preferenceCache.containsKey(prefKey))
            return (String) preferenceCache.get(prefKey);

        int pathDelimPos = prefKey.lastIndexOf("|");
        String path = (pathDelimPos != -1?
                              prefKey.substring(0, pathDelimPos) : ""),
               key  = prefKey.substring(pathDelimPos + 1);
        try {
            String val = prefMan.getPreferenceValue(path, key);
            if (val != null && val.trim().length() == 0)
                val = null;

            preferenceCache.put(prefKey, val);
            return val;
        }
        catch (Exception ex) {
            Log.stackTrace(ex);
            return null;
        }
    }

    private static void setPreference(String prefKey, String prefValue) {
        if (prefKey == null || !initPreferenceManager()) return ;
        if (prefValue == null) prefValue = "";

        int pathDelimPos = prefKey.lastIndexOf("|");
        String path = (pathDelimPos != -1?
                              prefKey.substring(0, pathDelimPos) : ""),
               key  = prefKey.substring(pathDelimPos + 1);
        try {
            prefMan.setPreferenceValue(path, key, prefValue);
            preferenceCache.put(prefKey, prefValue);
        }
        catch (Exception ex) {
            Log.stackTrace(ex);
        }
    }

    private static boolean initPreferenceManager() {
        //if (prefMan != null) return true;
        if (!UMLSupport.getUMLSupport().isConnected()) return false;

        prefMan = null;
        try {
            prefMan = UMLSupport.getUMLSupport()
                .getProduct()
                .getPreferenceManager();
        }
        catch (Exception ex) {
            Log.stackTrace(ex);
        }
        boolean retVal = prefMan != null;
        return retVal;
    }


    // Individual preference properties
    private static boolean createNewDiagram;
    private static boolean promptSaveWorkspace;
    private static boolean promptWksLocation;
    private static boolean promptProjectLocation;
    private static boolean reconnectLinks;
//    private static boolean useGenericsForCollections;

    private static String  defaultWorkspaceLocation;
    private static String  defaultElementName;
    private static String  collectionOverride;
    private static String  confirmSourceDelete;
    private static IPreferenceManager2 prefMan = null;
    private static String REL_DEF_WKS_LOC = "Workspaces/Default/Default.etw";
    private static String DEF_WKS_FILE = "Default.etw";

    private static HashMap watches;
    private static HashMap watchers;

    private static HashMap preferenceCache = new HashMap();

    static class ClobberedException extends RuntimeException {

    }
}