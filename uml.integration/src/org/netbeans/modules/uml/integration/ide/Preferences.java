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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

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

    public static final String COLLECTION_OVERRIDE =
                                "UML_COLLECTION_OVERRIDE_DEFAULT";
    public static final String USE_GENERICS_DEFAULT =
                                "UML_USE_GENERICS_DEFAULT";
    public static final String RECONNECT_LINKS = "UML_Reconnect to Presentation Boundary" ;


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

            java.util.prefs.Preferences prefs = NbPreferences.forModule (DummyCorePreference.class) ;
            
            reconnectLinks = prefs.getBoolean(RECONNECT_LINKS, true);
            
            collectionOverride = prefs.get(COLLECTION_OVERRIDE,"java.util.ArrayList"); // NOI18N

            readWatchedPreferences();
        }
        catch (RuntimeException ex) {
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
        //kris richards - "QueryForNewDiagram" pref expunged. Set to true.
        return true;
    }
    
    public static boolean getUseGenericsDefault()
    {
        //kris richards - changing to use NbPreferences
        //preferenceCache.remove(USE_GENERICS_DEFAULT);
	return NbPreferences.forModule(DummyCorePreference.class).getBoolean("UML_USE_GENERICS_DEFAULT",true);
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
        //kris richards - "DefaultElementName" pref expunged. Set to "Unnamed".
        return NbBundle.getMessage(Preferences.class, "UNNAMED");
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
            prefMan = UMLSupport
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
    private static boolean reconnectLinks;
//    private static boolean useGenericsForCollections;

    private static String  collectionOverride;
    private static IPreferenceManager2 prefMan = null;

    private static HashMap watches;
    private static HashMap watchers;

    private static HashMap preferenceCache = new HashMap();
}
