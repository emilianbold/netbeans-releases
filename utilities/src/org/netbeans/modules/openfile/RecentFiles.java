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

package org.netbeans.modules.openfile;
        
import java.beans.PropertyChangeEvent;
import java.util.prefs.BackingStoreException;
import org.netbeans.modules.openfile.RecentFiles.HistoryItem;
import org.openide.loaders.DataObject;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import java.beans.PropertyChangeListener;
import java.lang.NumberFormatException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * Manages prioritized set of recently closed files.
 *
 * @author Dafe Simonek
 */
public final class RecentFiles {

    /** List of recently closed files */
    private static List<HistoryItem> history = new ArrayList<HistoryItem>();
    
    /** Listener which listen to changes in recent files list */
    private static ChangeListener listener;
    
    /** Preferences node for storing history info */
    private static Preferences prefs;
    
    private static final Object HISTORY_LOCK = new Object();
    
    /** Name of preferences node where we persist history */
    private static final String PREFS_NODE = "RecentFilesHistory";

    /** Separator to encode file path and time into one string in preferences */
    private static final String SEPARATOR = "; time=";

    /** Boundary for items count in history */
    private static final int MAX_HISTORY_ITEMS = 20;
    
    private RecentFiles () {
    }

    /** Starts to listen for recently closed files */
    public static void init () {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            public void run() {
                List<HistoryItem> loaded = load();
                synchronized (HISTORY_LOCK) {
                    history.addAll(0, loaded);
                }
                TopComponent.getRegistry().addPropertyChangeListener(new WindowRegistryL());
                // let UI know about change 
                if (!loaded.isEmpty()) {
                    fireChange(loaded);
                }
            }
        });
    }

    /** Returns read-only list of recently closed files */
    public static List<HistoryItem> getRecentFiles () {
        synchronized (HISTORY_LOCK) {
            return Collections.unmodifiableList(history);
        }
    }

    /** Adds listener to the changes of recently closed list. Supports
     * only one listener.
     */
    public static void addChangeListener (ChangeListener l) throws TooManyListenersException {
        listener = l;
    }
    
    /** Loads list of recent files stored in previous system sessions.
     * @return list of stored recent files
     */
    static List<HistoryItem> load () {
        String[] keys;
        Preferences prefs = getPrefs();
        try {
            keys = prefs.keys();
        }
        catch (BackingStoreException ex) {
            Logger.getLogger(RecentFiles.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
            return Collections.emptyList();
        }
        List<HistoryItem> result = new ArrayList<HistoryItem>(keys.length + 10);
        HistoryItem hItem;
        for (String curKey : keys) {
            hItem = decode(prefs.get(curKey, null));
            if (hItem != null) {
                result.add(hItem);
            } else {
                // decode failed, so clear crippled item
                prefs.remove(curKey);
            }
        }
        Collections.sort(result);
        return result;        
    }
    
    private static HistoryItem decode (String value) {
        int sepIndex = value.lastIndexOf(SEPARATOR);
        if (sepIndex <= 0) {
            return null;
        }
        URL url = null;
        try {
            url = new URL(value.substring(0, sepIndex));
        } catch (MalformedURLException ex) {
            // url corrupted, skip
            Logger.getLogger(RecentFiles.class.getName()).log(Level.INFO, ex.getMessage(), ex);
            return null;
        }
        FileObject fo = URLMapper.findFileObject(url);
        if (fo == null) {
            return null;
        }
        long time = 0;
        try {
            time = Long.decode(value.substring(sepIndex + SEPARATOR.length()));
        } catch (NumberFormatException ex) {
            // stored data corrupted, skip
            Logger.getLogger(RecentFiles.class.getName()).log(Level.INFO, ex.getMessage(), ex);
            return null;
        }
        return new HistoryItem(fo, time);
    }

    static void storeRemoved (HistoryItem hItem) {
        String stringURL = null;
        URL url = URLMapper.findURL(hItem.getFile(), URLMapper.EXTERNAL);
        if (url == null) {
            // not possible to store
            Logger.getLogger(RecentFiles.class.getName()).log(Level.INFO, 
                    "storeRemoved: URL can't be found for FileObject " + hItem.getFile()); // NOI18N
            return;
        }
        stringURL = url.toExternalForm();
        getPrefs().remove(trimToKeySize(stringURL));
    }
    
    static void storeAdded (HistoryItem hItem) {
        String stringURL = null;
        URL url = URLMapper.findURL(hItem.getFile(), URLMapper.EXTERNAL);
        if (url == null) {
            // not possible to store
            Logger.getLogger(RecentFiles.class.getName()).log(Level.INFO, 
                    "storeAdded: URL can't be found for FileObject " + hItem.getFile()); // NOI18N
            return;
        }
        stringURL = url.toExternalForm();
        String value = stringURL + SEPARATOR + String.valueOf(hItem.getTime());
        getPrefs().put(trimToKeySize(stringURL), value);
    }
    
    private static String trimToKeySize (String path) {
        int length = path.length();
        if (length > Preferences.MAX_KEY_LENGTH) {
            path = path.substring(length - Preferences.MAX_KEY_LENGTH, length);
        }
        return path;
    }
    
   static Preferences getPrefs () {
        if (prefs == null) {
            prefs = NbPreferences.forModule(RecentFiles.class).node(PREFS_NODE);
        }
        return prefs;
    }
    
    /** Adds file represented by given TopComponent to the list,
     * if conditions are met.
     */ 
    private static void addFile (TopComponent tc) {
        if (tc instanceof CloneableTopComponent) {
            FileObject fo = obtainFileObject(tc);
            if (fo != null) {
                boolean added = false;
                synchronized (HISTORY_LOCK) {
                    // avoid duplicates
                    HistoryItem hItem = findHistoryItem(fo);
                    if (hItem == null) {
                        hItem = new HistoryItem(fo, System.currentTimeMillis());
                        history.add(0, hItem);
                        storeAdded(hItem);
                        added = true;
                        // keep manageable size of history
                        // remove the oldest item if needed
                        if (history.size() > MAX_HISTORY_ITEMS) {
                            HistoryItem oldest = history.get(history.size() - 1);
                            history.remove(oldest);
                            storeRemoved(oldest);
                        }
                    }
                }
                if (added) {
                    fireChange(fo);
                }
            }
        }
    }

    /** Removes file represented by given TopComponent from the list */
    private static void removeFile (TopComponent tc) {
        if (tc instanceof CloneableTopComponent) {
            FileObject fo = obtainFileObject(tc);
            if (fo != null) {
                boolean removed = false;
                synchronized (HISTORY_LOCK) {
                    HistoryItem hItem = findHistoryItem(fo);
                    if (hItem != null) {
                        history.remove(hItem);
                        storeRemoved(hItem);
                        removed = true;
                    }
                }
                if (removed) {
                    fireChange(fo);
                }
            }
        }
    }
    
    private static FileObject obtainFileObject (TopComponent tc) {
        DataObject dObj = tc.getLookup().lookup(DataObject.class);
        return dObj != null ? dObj.getPrimaryFile() : null;
    }
    
    private static HistoryItem findHistoryItem (FileObject fo) {
        for (HistoryItem hItem : history) {
            if (fo.equals(hItem.getFile())) {
                return hItem;
            }
        }
        return null;
    }
    
    private static void fireChange (Object obj) {
        if (listener != null) {
            listener.stateChanged(new ChangeEvent(obj));
        }
    }

    /** One item of the recently closed files history
     * Comparable by the time field, ascending from most recent to older items.
     */
    public static final class HistoryItem<T extends HistoryItem> implements Comparable<T> {
        
        private long time;
        private FileObject file;
        
        HistoryItem (FileObject file, long time) {
            this.file = file;
            this.time = time;
        }
        
        public FileObject getFile () {
            return file;
        }
        
        public long getTime () {
            return time;
        }

        public int compareTo(T other) {
            long diff = time - other.getTime();
            return diff < 0 ? 1 : diff > 0 ? -1 : 0;
        }
        
    }
    
    /** Receives info about opened and closed TopComponents from window system.
     */ 
    private static class WindowRegistryL implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (TopComponent.Registry.PROP_TC_CLOSED.equals(evt.getPropertyName())) {
                addFile((TopComponent) evt.getNewValue());
            }
            if (TopComponent.Registry.PROP_TC_OPENED.equals(evt.getPropertyName())) {
                removeFile((TopComponent) evt.getNewValue());
            }
        }
    
    }
    
}
