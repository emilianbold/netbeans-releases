/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Manages the loaded property files for {@link AntProjectHelper}.
 * @author Jesse Glick
 */
final class ProjectProperties {
    
    private final FileObject dir;
    
    /**
     * Properties loaded from metadata files on disk.
     * Keys are project-relative paths such as {@link #PROJECT_PROPERTIES_PATH}.
     * Values are loaded property providers.
     */
    private final Map/*<String,PP>*/ properties = new HashMap();
    
    /**
     * Create a project properties helper object.
     * @param dir the project directory
     */
    public ProjectProperties(FileObject dir) {
        this.dir = dir;
    }

    /**
     * Get properties from a given path.
     * @param path the project-relative path
     * @return the applicable properties (created if empty; never null)
     */
    public EditableProperties getProperties(String path) {
        return getPP(path).getEditableProperties();
    }
    
    /**
     * Store properties in memory.
     * @param path the project-relative path
     * @param props the new properties, or null to remove the properties file
     * @return true if an actual change was made
     */
    public boolean putProperties(String path, EditableProperties props) {
        return getPP(path).put(props);
    }
    
    /**
     * Write cached properties to disk.
     * @param the project-relative path
     * @throws IOException if the file could not be written
     */
    public void write(String path) throws IOException {
        assert properties.containsKey(path);
        getPP(path).write();
    }
    
    /**
     * Make a property provider that loads from this file
     * and fires changes when it is written to (even in memory).
     */
    public PropertyProvider getPropertyProvider(String path) {
        return getPP(path);
    }
    
    private PP getPP(String path) {
        PP pp = (PP)properties.get(path);
        if (pp == null) {
            pp = new PP(path, dir);
            properties.put(path, pp);
        }
        return pp;
    }
    
    private static final class PP implements PropertyProvider {
        
        // XXX lock any loaded property files while the project is modified, to prevent manual editing,
        // and reload any modified files if the project is unmodified

        private final String path;
        private final FileObject dir;
        private EditableProperties properties = null;
        private boolean loaded = false;
        private final List/*<ChangeListener>*/ listeners = new ArrayList();
        
        public PP(String path, FileObject dir) {
            this.path = path;
            this.dir = dir;
        }
        
        public EditableProperties getEditableProperties() {
            if (!loaded) {
                FileObject fo = dir.getFileObject(path);
                if (fo != null) {
                    try {
                        EditableProperties p;
                        InputStream is = fo.getInputStream();
                        try {
                            p = new EditableProperties(true);
                            p.load(is);
                        } finally {
                            is.close();
                        }
                        properties = p;
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
                loaded = true;
            }
            if (properties != null) {
                return properties.cloneProperties();
            } else {
                return new EditableProperties(true);
            }
        }
        
        public boolean put(EditableProperties nue) {
            loaded = true;
            boolean modifying = !Utilities.compareObjects(nue, properties);
            if (modifying) {
                if (nue != null) {
                    properties = nue.cloneProperties();
                } else {
                    properties = null;
                }
                fireChange();
            }
            return modifying;
        }
        
        public void write() throws IOException {
            assert loaded;
            FileObject f = dir.getFileObject(path);
            if (properties != null) {
                // Supposed to create/modify the file.
                if (f == null) {
                    f = FileUtil.createData(dir, path);
                }
                FileLock lock = f.lock();
                try {
                    OutputStream os = f.getOutputStream(lock);
                    try {
                        properties.store(os);
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            } else {
                // We are supposed to remove any existing file.
                if (f != null) {
                    f.delete();
                }
            }
        }
        
        public Map getProperties() {
            return getEditableProperties();
        }
        
        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        
        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        private void fireChange() {
            ChangeListener[] ls;
            synchronized (this) {
                if (listeners.isEmpty()) {
                    return;
                }
                ls = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
            }
            ChangeEvent ev = new ChangeEvent(this);
            for (int i = 0; i < ls.length; i++) {
                ls[i].stateChanged(ev);
            }
        }
        
    }
    
}
