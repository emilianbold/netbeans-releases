/*
 * KitsTracker.java
 *
 * Created on February 21, 2007, 1:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.editor.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.Repository;

/**
 *
 * @author vita
 */
public final class KitsTracker {
        
    private static final Logger LOG = Logger.getLogger(KitsTracker.class.getName());
    private static final Set<String> ALREADY_LOGGED = Collections.synchronizedSet(new HashSet<String>(10));
    
    private static KitsTracker instance = null;
    
    /**
     * Gets the <code>KitsTracker</code> singleton instance.
     * @return The <code>KitsTracker</code> instance.
     */
    public static synchronized KitsTracker getInstance() {
        if (instance == null) {
            instance = new KitsTracker();
        }
        return instance;
    }
    
    public static String getGenericPartOfCompoundMimeType(String mimeType) {
        int plusIdx = mimeType.lastIndexOf('+'); //NOI18N
        if (plusIdx != -1 && plusIdx < mimeType.length() - 1) {
            int slashIdx = mimeType.indexOf('/'); //NOI18N
            String prefix = mimeType.substring(0, slashIdx + 1);
            String suffix = mimeType.substring(plusIdx + 1);

            // fix for #61245
            if (suffix.equals("xml")) { //NOI18N
                prefix = "text/"; //NOI18N
            }

            return prefix + suffix;
        } else {
            return null;
        }
    }
    
    /**
     * Gets the list of mime types (<code>String</code>s) that use the given
     * class as an editor kit implementation.
     * 
     * @param kitClass The editor kit class to get mime types for.
     * @return The <code>List&lt;String&gt;</code> of mime types.
     */
    @SuppressWarnings("unchecked")
    public List<String> getMimeTypesForKitClass(Class kitClass) {
        if (kitClass != null) {
            return (List<String>) updateAndGet(kitClass);
        } else {
            return Collections.singletonList(""); //NOI18N
        }
    }

    /**
     * Find mime type for a given editor kit implementation class.
     * 
     * @param kitClass The editor kit class to get the mime type for.
     * @return The mime type or <code>null</code> if the mime type can't be
     *   resolved for the given kit class.
     */
    public String findMimeType(Class kitClass) {
        if (kitClass != null) {
            List mimeTypes = getMimeTypesForKitClass(kitClass);
            if (mimeTypes.size() == 0) {
                if (LOG.isLoggable(Level.WARNING)) {
                    logOnce(Level.WARNING, "No mime type uses editor kit implementation class: " + kitClass); //NOI18N
                }
                return null;
            } else if (mimeTypes.size() == 1) {
                return (String) mimeTypes.get(0);
            } else {
                if (LOG.isLoggable(Level.WARNING)) {
    //                Throwable t = new Throwable("Stacktrace"); //NOI18N
    //                LOG.log(Level.WARNING, "Ambiguous mime types for editor kit implementation class: " + kitClass + "; mime types: " + mimeTypes, t); //NOI18N
                    logOnce(Level.WARNING, "Ambiguous mime types for editor kit implementation class: " + kitClass + "; mime types: " + mimeTypes); //NOI18N
                }
                return null;
            }
        } else {
            return ""; //NOI18N
        }
    }

    /**
     * Gets all know mime types registered in the system.
     * 
     * @return The set of registered mimne types.
     */
    @SuppressWarnings("unchecked")
    public Set<String> getMimeTypes() {
        return (Set<String>) updateAndGet(null);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        PCS.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        PCS.removePropertyChangeListener(l);
    }
    
    // ------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------
    
    // The map of mime type -> kit class
    private final Map<String, Class> mimeType2kitClass = new HashMap<String, Class>();
    private final Set<String> knownMimeTypes = new HashSet<String>();
    private List<FileObject> eventSources = null;
    private boolean needsReloading = true;
    private final PropertyChangeSupport PCS = new PropertyChangeSupport(this);
    
    private final FileChangeListener fcl = new FileChangeAdapter() {
        @Override
        public void fileFolderCreated(FileEvent fe) {
            invalidateCache();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (fe.getFile().isFolder()) {
                invalidateCache();
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            if (fe.getFile().isFolder()) {
                invalidateCache();
            }
        }
    };
    
    private KitsTracker() {

    }

    private static final ThreadLocal<Boolean> inReload = new  ThreadLocal<Boolean>() {
        protected @Override Boolean initialValue() {
            return false;
        }
    };
    
    /**
     * Scans fonlders under 'Editors' and finds <code>EditorKit</code>s for
     * each mime type.
     * 
     * @param map The map of a mime type to its registered <code>EditorKit</code>.
     * @param eventSources The list of folders with registered <code>EditorKits</code>.
     *   Changes in these folders mean that the map may need to be recalculated.
     */
    private static void reload(Map<String, Class> map, Set<String> set, List<FileObject> eventSources) {
        assert !inReload.get() : "Re-entering KitsTracker.reload() is prohibited. This situation usually indicates wrong initialization of some setting."; //NOI18N
        
        inReload.set(true);
        try {
            _reload(map, set, eventSources);
        } finally {
            inReload.set(false);
        }
    }
    
    private static void _reload(Map<String, Class> map, Set<String> set, List<FileObject> eventSources) {
        // Get the root of the MimeLookup registry
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("Editors"); //NOI18N

        // Generally may not exist (e.g. in tests)
        if (fo != null) {
            // Go through mime type types
            FileObject [] types = fo.getChildren();
            for(int i = 0; i < types.length; i++) {
                if (!isValidType(types[i])) {
                    continue;
                }

                // Go through mime type subtypes
                FileObject [] subTypes = types[i].getChildren();
                for(int j = 0; j < subTypes.length; j++) {
                    if (!isValidSubtype(subTypes[j])) {
                        continue;
                    }

                    String mimeType = types[i].getNameExt() + "/" + subTypes[j].getNameExt(); //NOI18N
                    MimePath mimePath = MimePath.parse(mimeType);
                    EditorKit kit = MimeLookup.getLookup(mimePath).lookup(EditorKit.class);

                    if (kit != null) {
                        String genericMimeType;
                        if (!kit.getContentType().equals(mimeType) && 
                            !(null != (genericMimeType = getGenericPartOfCompoundMimeType(mimeType)) && genericMimeType.equals(kit.getContentType())))
                        {
                            logOnce(Level.WARNING, "Inconsistent mime type declaration for the kit class: " + kit.getClass().getName() + //NOI18N
                                "; mimeType from the kit is '" + kit.getContentType() + //NOI18N
                                ", but the kit is registered for '" + mimeType + "'"); //NOI18N
                        }
                        map.put(mimeType, kit.getClass());
                    } else {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("No kit for '" + mimeType + "'");
                        }
                    }
                    
                    set.add(mimeType);
                }

                eventSources.add(types[i]);
            }

            eventSources.add(fo);
        }
    }

    private void invalidateCache() {
        synchronized (mimeType2kitClass) {
            needsReloading = true;
        }
        PCS.firePropertyChange(null, null, null);
    }

    private static boolean isValidType(FileObject typeFile) {
        if (!typeFile.isFolder()) {
            return false;
        }

        String typeName = typeFile.getNameExt();
        return MimePath.validate(typeName, null);
    }

    private static boolean isValidSubtype(FileObject subtypeFile) {
        if (!subtypeFile.isFolder()) {
            return false;
        }

        String typeName = subtypeFile.getNameExt();
        return MimePath.validate(null, typeName);
    }        
    
    private static void logOnce(Level level, String msg) {
        if (!ALREADY_LOGGED.contains(msg)) {
            LOG.log(level, msg);
            ALREADY_LOGGED.add(msg);
        }
    }

    private Object updateAndGet(Class kitClass) {
        boolean reload;
        Map<String, Class> reloadedMap = new HashMap<String, Class>();
        Set<String> reloadedSet = new HashSet<String>();
        List<FileObject> newEventSources = new ArrayList<FileObject>();
        
        ArrayList<String> list = new ArrayList<String>();
        HashSet<String> set = new HashSet<String>();
        
        synchronized (mimeType2kitClass) {
            reload = needsReloading;
        }
        
        // This needs to be outside of the synchronized block to prevent deadlocks
        // See eg #107400
        if (reload) {
            reload(reloadedMap, reloadedSet, newEventSources);
        }
            
        synchronized (mimeType2kitClass) {
            if (reload) {
                // Stop listening
                if (eventSources != null) {
                    for(FileObject fo : eventSources) {
                        fo.removeFileChangeListener(fcl);
                    }
                }

                // Update the cache
                mimeType2kitClass.clear();
                mimeType2kitClass.putAll(reloadedMap);
                knownMimeTypes.clear();
                knownMimeTypes.addAll(reloadedSet);

                // Start listening again
                eventSources = newEventSources;
                for(FileObject fo : eventSources) {
                    fo.addFileChangeListener(fcl);
                }

                // Set the flag
                needsReloading = false;
            }
            
            // Compute the list
            if (kitClass != null) {
                for(String mimeType : mimeType2kitClass.keySet()) {
                    Class clazz = mimeType2kitClass.get(mimeType);
                    if (kitClass == clazz) {
                        list.add(mimeType);
                    }
                }
            } else {
                set.addAll(knownMimeTypes);
            }
        }
        
        return kitClass != null ? list : set;
    }
}
