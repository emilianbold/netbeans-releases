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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import java.io.File;
import java.util.Properties;
import org.netbeans.modules.project.ant.FileChangeSupport;
import org.netbeans.modules.project.ant.FileChangeSupportListener;
import org.netbeans.modules.project.ant.FileChangeSupportEvent;
import org.openide.filesystems.FileSystem;
import java.io.OutputStream;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileLock;
import org.openide.modules.InstalledFileLocator;

/**
 * Manages the loaded property files for {@link AntProjectHelper}.
 * @author Jesse Glick
 */
final class ProjectProperties {
    
    /** Project directory. */
    private final FileObject dir;
    
    /**
     * Properties loaded from metadata files on disk.
     * Keys are project-relative paths such as {@link #PROJECT_PROPERTIES_PATH}.
     * Values are loaded property providers.
     */
    private final Map/*<String,PP>*/ properties = new HashMap();
    
    /** @see #getStockPropertyPreprovider */
    private PropertyProvider stockPropertyPreprovider = null;
    
    /** @see #getStandardPropertyEvaluator */
    private PropertyEvaluator standardPropertyEvaluator = null;
    
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
    
    private static final class PP implements PropertyProvider, FileChangeSupportListener {
        
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
            FileChangeSupport.DEFAULT.addListener(this, new File(FileUtil.toFile(dir), path.replace('/', File.separatorChar)));
        }
        
        public EditableProperties getEditableProperties() {
            if (!loaded) {
                properties = null;
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
            final FileObject f = dir.getFileObject(path);
            if (properties != null) {
                // Supposed to create/modify the file.
                // Need to use an atomic action - otherwise listeners will first
                // receive an event that the file has been written to zero length
                // (which for *.properties means no keys), which is wrong.
                dir.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        FileObject _f;
                        if (f == null) {
                            _f = FileUtil.createData(dir, path);
                        } else {
                            _f = f;
                        }
                        FileLock lock = _f.lock();
                        try {
                            OutputStream os = _f.getOutputStream(lock);
                            try {
                                properties.store(os);
                            } finally {
                                os.close();
                            }
                        } finally {
                            lock.releaseLock();
                        }
                    }
                });
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
        
        private void diskChange() {
            // XXX should check for a possible clobber from in-memory data
            loaded = false;
            fireChange();
        }

        public void fileCreated(FileChangeSupportEvent event) {
            diskChange();
        }

        public void fileDeleted(FileChangeSupportEvent event) {
            diskChange();
        }

        public void fileModified(FileChangeSupportEvent event) {
            diskChange();
        }
        
    }

    /**
     * See {@link AntProjectHelper#getStockPropertyPreprovider}.
     */
    public PropertyProvider getStockPropertyPreprovider() {
        if (stockPropertyPreprovider == null) {
            Map/*<String,String>*/ m = new HashMap();
            Properties p = System.getProperties();
            synchronized (p) {
                m.putAll(p);
            }
            m.put("basedir", FileUtil.toFile(dir).getAbsolutePath()); // NOI18N
            File antHome = InstalledFileLocator.getDefault().locate("ant", "org.apache.tools.ant.module", false); // NOI18N
            if (antHome != null) {
                m.put("ant.home", antHome.getAbsolutePath()); // NOI18N
            }
            stockPropertyPreprovider = PropertyUtils.fixedPropertyProvider(m);
        }
        return stockPropertyPreprovider;
    }
    
    /**
     * See {@link AntProjectHelper#getStandardPropertyEvaluator}.
     */
    public PropertyEvaluator getStandardPropertyEvaluator() {
        if (standardPropertyEvaluator == null) {
            PropertyEvaluator findUserPropertiesFile = PropertyUtils.sequentialPropertyEvaluator(
                getStockPropertyPreprovider(),
                new PropertyProvider[] {
                    getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                }
            );
            PropertyProvider globalProperties = new UserPropertiesProvider(findUserPropertiesFile);
            standardPropertyEvaluator = PropertyUtils.sequentialPropertyEvaluator(
                getStockPropertyPreprovider(),
                new PropertyProvider[] {
                    getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                    globalProperties,
                    getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH),
                }
            );
        }
        return standardPropertyEvaluator;
    }
    private PropertyProvider computeDelegate(PropertyEvaluator findUserPropertiesFile) {
        String userPropertiesFile = findUserPropertiesFile.getProperty("user.properties.file"); // NOI18N
        if (userPropertiesFile != null) {
            // Have some defined global properties file, so read it and listen to changes in it.
            File f = PropertyUtils.resolveFile(FileUtil.toFile(dir), userPropertiesFile);
            if (f.equals(PropertyUtils.USER_BUILD_PROPERTIES)) {
                // Just to share the cache.
                return PropertyUtils.globalPropertyProvider();
            } else {
                return PropertyUtils.propertiesFilePropertyProvider(f);
            }
        } else {
            // Use the in-IDE default.
            return PropertyUtils.globalPropertyProvider();
        }
    }
    private final class UserPropertiesProvider extends PropertyUtils.DelegatingPropertyProvider implements PropertyChangeListener {
        private final PropertyEvaluator findUserPropertiesFile;
        public UserPropertiesProvider(PropertyEvaluator findUserPropertiesFile) {
            super(computeDelegate(findUserPropertiesFile));
            this.findUserPropertiesFile = findUserPropertiesFile;
            findUserPropertiesFile.addPropertyChangeListener(this);
        }
        public void propertyChange(PropertyChangeEvent ev) {
            if ("user.properties.file".equals(ev.getPropertyName())) { // NOI18N
                setDelegate(computeDelegate(findUserPropertiesFile));
            }
        }
    }
    
}
