/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.projects;

import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.util.WeakListener;

import java.util.WeakHashMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.lang.ref.WeakReference;
import java.io.IOException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/** Scans positions of FileObject-delegates for FileObjects from SystemFileSystem. Each
 *
 * @author  Vitezslav Stejskal
 */
final class FileStateManager {
    
    /** Identification of filesystem representing Project */
    public static final int LAYER_PROJECT = 0;
    /** Identification of filesystem representing Session */
    public static final int LAYER_SESSION = 1;
    /** Identification of filesystem representing XML-layers from all installed modules */
    public static final int LAYER_MODULES = 2;
    
    /** File State - file is defined on the layer (top-most layer containing the file) */
    public static final int FSTATE_DEFINED = 0;
    /** File State - file is ignored on the layer (higher layer contains file too) */
    public static final int FSTATE_IGNORED = 1;
    /** File State - file is inherited on the layer (file doesn't exist on the layer and exists on lower layer) */
    public static final int FSTATE_INHERITED = 2;
    /** File State - file is not defined on the layer (file doesn't exist on the layer and exists on higher layer) */
    public static final int FSTATE_UNDEFINED = 3;
    
    /** Singleton instance of FileStateManager */
    private static FileStateManager manager = null;
    /** Cache of collected information */
    private WeakHashMap info = new WeakHashMap ();
    /** Number of layers on {@link SystemFileSystem} */
    private static final int LAYERS_COUNT = 3;
    /** Layers of {@link SystemFileSystem}, LAYER_* constants can be used as indexes. */
    private FileSystem layers [] = new FileSystem [LAYERS_COUNT];
    /** List of listeners listening on changes in file state */
    private HashMap listeners = new HashMap (10);
    /** Listener attached to SessionManager, it refreshes list of layers after the project is switched */
    private PropertyChangeListener propL = null;

    public static synchronized FileStateManager getDefault () {
        if (manager == null) {
            manager = new FileStateManager ();
        }
        return manager;
    }

    /** Creates new FileStateManager */
    private FileStateManager () {
        // set layers
        getLayers ();

        // listen on changes of layers made through the SessionManager
        propL = new PropL ();
        SessionManager.getDefault ().addPropertyChangeListener (
            WeakListener.propertyChange (propL, SessionManager.getDefault ()));
    }

    public void define (FileObject mfo, int layer, boolean revert) throws IOException {
        // ignore request when file is already defined on layer
        if (FSTATE_DEFINED == getFileState (mfo, layer))
            return;

        // find file on specified layer
        FileObject fo = layers [layer].findResource (mfo.getPackageNameExt ('/', '.'));
        
        // remove the file if it exists and current definition should be preserved
        if (fo != null && !revert) {
            delete (mfo, layer);
            fo = null;
        }

        // create file on specified layer if it doesn't exist
        if (fo == null) {
            String parent = mfo.getParent ().getPackageNameExt ('/', '.');
            FileObject fparent = FileUtil.createFolder (layers [layer].getRoot (), parent);
            mfo.copy (fparent, mfo.getName (), mfo.getExt ());
        }

        // remove above defined files
        for (int i = 0; i < layer; i++) {
            delete (mfo, i);
        }
    }

    public void delete (FileObject mfo, int layer) throws IOException {
        FileObject fo = layers [layer].findResource (mfo.getPackageNameExt ('/', '.'));
        if (fo != null) {
            FileLock lock = null;
            try {
                lock = fo.lock ();
                fo.delete (lock);
            } finally {
                if (lock != null)
                    lock.releaseLock ();
            }
        }
    }
    
    public int getFileState (FileObject mfo, int layer) {
        // check if the FileObject is from SystemFileSystem
        FileSystem fs = null;
        FileInfo finf = null;

        try {
            fs = mfo.getFileSystem ();
        } catch (FileStateInvalidException e) {
            // ignore, will be handled later
        }

        if (fs == null || !TopManager.getDefault ().getRepository ().getDefaultFileSystem ().equals (fs))
            throw new IllegalArgumentException ("FileObject has to be from DefaultFileSystem - " + mfo);
        
        synchronized (info) {
            if (null == (finf = (FileInfo) info.get (mfo))) {
                finf = new FileInfo (mfo);
                info.put (mfo, finf);
            }
        }

        return finf.getState (layer);
    }
    
    public final void addFileStatusListener (FileStatusListener l, FileObject mfo) {
        synchronized (listeners) {
            LinkedList lst = null;
            if (!listeners.containsKey (l)) {
                lst = new LinkedList ();
                listeners.put (l, lst);
            }
            else
                lst = (LinkedList)listeners.get (l);
            
            if (!lst.contains (mfo))
                lst.add (mfo);
        }
    }
    
    public final void removeFileStatusListener (FileStatusListener l, FileObject mfo) {
        synchronized (listeners) {
            if (mfo == null)
                listeners.remove (l);
            else {
                LinkedList lst = (LinkedList) listeners.get (l);
                if (lst != null) {
                   lst.remove (mfo);
                   if (lst.isEmpty ())
                       listeners.remove (l);
                }
            }
        }
    }

    private void fireFileStatusChanged (FileObject mfo) {
        HashMap h = null;
        
        synchronized (listeners) {
            h = (HashMap)listeners.clone ();
        }
        
        Iterator i = h.keySet ().iterator ();
        while (i.hasNext ()) {
            FileStatusListener l = (FileStatusListener)i.next ();
            LinkedList lst = (LinkedList)h.get (l);
            if (lst.contains (mfo))
                l.fileStatusChanged (mfo);
        }
    }

    private void discard (FileObject mfo) {
        synchronized (info) {
            info.remove (mfo);
        }
    }

    private void getLayers () {
        layers [LAYER_PROJECT] = SessionManager.getDefault ().getLayer (SessionManager.LAYER_PROJECT);
        layers [LAYER_SESSION] = SessionManager.getDefault ().getLayer (SessionManager.LAYER_SESSION);
        layers [LAYER_MODULES] = SessionManager.getDefault ().getLayer (SessionManager.LAYER_INSTALL);
    }

    private class PropL implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent evt) {
            if (SessionManager.PROP_OPEN.equals (evt.getPropertyName ())) {
                FileObject mfos [] = null;

                // [PENDING] this should be better synchronized
                getLayers ();
                
                synchronized (info) {
                    mfos = (FileObject [])info.keySet ().toArray (new FileObject [0]);
                    info.clear ();
                }
                
                for (int i = 0; i < mfos.length; i++)
                    fireFileStatusChanged (mfos [i]);
            }
        }
    }

    public static interface FileStatusListener {
        public void fileStatusChanged (FileObject mfo);
    }
    
    private class FileInfo extends FileChangeAdapter {
        private WeakReference file = null;
        
        private int state [] = new int [LAYERS_COUNT];
        private final Object LOCK = new Object ();

        private FileObject notifiers [] = new FileObject [LAYERS_COUNT];
        private FileChangeListener weakL [] = new FileChangeListener [LAYERS_COUNT];
        
        public FileInfo (FileObject mfo) {
            file = new WeakReference (mfo);
            
            // get initial state
            for (int i = 0; i < LAYERS_COUNT; i++) {
                state [i] = getStateImpl (mfo, i);
            }
            
            // attach FileInfo to interesting FileObject on each layer
            for (int i = 0; i < LAYERS_COUNT; i++) {
                attachNotifier (mfo, i);
            }
        }
        
        public int getState (int layer) {
            synchronized (LOCK) {
                return state [layer];
            }
        }

        private void rescan (FileObject mfo) {
            boolean changed = false;
            
            synchronized (LOCK) {
                for (int i = 0; i < LAYERS_COUNT; i++) {
                    int ns = getStateImpl (mfo, i);
                    if (state [i] != ns) {
                        state [i] = ns;
                        changed = true;
                    }
                }
            }
            
            if (changed)
                fireFileStatusChanged (mfo);
        }

        private int getStateImpl (FileObject mfo, int layer) {
            boolean above = false;
            boolean below = false;

            // scan higher layers
            for (int i = 0; i < layer; i++) {
                if (isOnLayer (mfo, i)) {
                    above = true;
                    break;
                }
            }

            // scan lower layers
            for (int i = layer + 1; i < LAYERS_COUNT; i++) {
                if (isOnLayer (mfo, i)) {
                    below = true;
                    break;
                }
            }

            if (isOnLayer (mfo, layer)) {
                return above ? FSTATE_IGNORED : FSTATE_DEFINED;
            }
            else {
                return below && !above ? FSTATE_INHERITED : FSTATE_UNDEFINED;
            }
        }
        
        private boolean isOnLayer (FileObject mfo, int layer) {
            return null != layers [layer].findResource (mfo.getPackageNameExt ('/', '.'));
        }
        
        /**
         * @return true if attached notifier is the delegate FO
         */
        private boolean attachNotifier (FileObject mfo, int layer) {
            String fn = mfo.getPackageNameExt ('/', '.');
            FileObject fo = null;
            boolean isDelegate = true;

            // find new notifier - the FileObject with closest match to getFile ()
            while (fn.length () > 0 && null == (fo = layers [layer].findResource (fn))) {
                int pos = fn.lastIndexOf ('/');
                isDelegate = false;

                if (-1 == pos)
                    break;
                
                fn = fn.substring (0, pos);
            }
            
            if (fo == null)
                fo = layers [layer].getRoot ();

            if (fo != notifiers [layer]) {
                // remove listener from existing notifier if any
                if (notifiers [layer] != null)
                    notifiers [layer].removeFileChangeListener (weakL [layer]);

                // create new listener and attach it to new notifier
                weakL [layer] = WeakListener.fileChange (this, fo);
                fo.addFileChangeListener (weakL [layer]);
                notifiers [layer] = fo;
            }
            
            return isDelegate;
        }

        private void detachAllNotifiers () {
            for (int i = 0; i < LAYERS_COUNT; i++) {
                if (notifiers [i] != null) {
                    notifiers [i].removeFileChangeListener (weakL [i]);
                    notifiers [i] = null;
                    weakL [i] = null;
                }
            }
        }
        
        private int layerOfFile (FileObject fo) {
            try {
                FileSystem fs = fo.getFileSystem ();
                for (int i = 0; i < LAYERS_COUNT; i++) {
                    if (fs.equals (layers [i]))
                        return i;
                }
            } catch (FileStateInvalidException e) {
                IllegalStateException ex = new IllegalStateException ("Invalid file - " + fo); // NOI81N
                TopManager.getDefault ().getErrorManager ().annotate (ex, e);
                throw ex;
            }
            throw new IllegalStateException ("File isn't from any layer in DefaultFileSystem - " + fo); // NOI18N
        }

        // ---------------------- FileChangeListener events -----------------------------

        public void fileRenamed (FileRenameEvent fe) {
            // rename can be caused either by renaming fo or by deleting mfo,
            // thus the safe way is to discard this FileInfo from the map and
            // notify listeners about the change 
            FileObject mfo = (FileObject) file.get ();
            if (mfo != null) {
                discard (mfo);
                fireFileStatusChanged (mfo);
            }
            else
                detachAllNotifiers ();
        }
        
        public void fileDataCreated (FileEvent fe) {
            FileObject mfo = (FileObject) file.get ();
            if (mfo != null) {
                String created = fe.getFile ().getPackageNameExt ('/', '.');
                String mfoname = mfo.getPackageNameExt ('/', '.');

                if (created.equals (mfoname)) {
                    int layer = layerOfFile (fe.getFile ());
                    attachNotifier (mfo, layer);

                    rescan (mfo);
                }
            }
            else
                detachAllNotifiers ();
        }
        
        public void fileFolderCreated (FileEvent fe) {
            FileObject mfo = (FileObject) file.get ();
            if (mfo != null) {
                String created = fe.getFile ().getPackageNameExt ('/', '.');
                String mfoname = mfo.getPackageNameExt ('/', '.');

                if (mfoname.startsWith (created)) {
                    int layer = layerOfFile (fe.getFile ());
                    if (attachNotifier (mfo, layer)) {
                        // delegate was created -> rescan
                        rescan (mfo);
                    }
                }
            }
            else
                detachAllNotifiers ();
        }
        
        public void fileDeleted (FileEvent fe) {
            FileObject mfo = (FileObject) file.get ();
            if (mfo != null) {
                String deleted = fe.getFile ().getPackageNameExt ('/', '.');
                String mfoname = mfo.getPackageNameExt ('/', '.');

                if (deleted.equals (mfoname)) {
                    int layer = layerOfFile (fe.getFile ());
                    attachNotifier (mfo, layer);

                    rescan (mfo);
                }
            }
            else
                detachAllNotifiers ();
        }
    }
}
