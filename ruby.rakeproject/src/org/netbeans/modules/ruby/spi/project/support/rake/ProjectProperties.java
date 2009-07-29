/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.ruby.spi.project.support.rake;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ruby.modules.project.rake.UserQuestionHandler;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.EditableProperties;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;
import org.openide.util.Utilities;

/**
 * Manages the loaded property files for {@link RakeProjectHelper}.
 * @author Jesse Glick
 */
final class ProjectProperties {
    
    /** Associated helper. */
    private final RakeProjectHelper helper;
    
    /**
     * Properties loaded from metadata files on disk.
     * Keys are project-relative paths such as {@link #PROJECT_PROPERTIES_PATH}.
     * Values are loaded property providers.
     */
    private final Map<String,PP> properties = new HashMap<String,PP>();
    
    /** @see #getStockPropertyPreprovider */
    private PropertyProvider stockPropertyPreprovider = null;
    
    /** @see #getStandardPropertyEvaluator */
    private PropertyEvaluator standardPropertyEvaluator = null;
    
    /**
     * Create a project properties helper object.
     * @param helper the associated helper
     */
    public ProjectProperties(RakeProjectHelper helper) {
        this.helper = helper;
    }
    
    /**
     * Get properties from a given path.
     * @param path the project-relative path
     * @return the applicable properties (created if empty; never null)
     */
    public EditableProperties getProperties(String path) {
        EditableProperties ep = getPP(path).getEditablePropertiesOrNull();
        if (ep != null) {
            return ep.cloneProperties();
        } else {
            return new EditableProperties(true);
        }
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
    public FileLock write(String path) throws IOException {
        assert properties.containsKey(path);
        return getPP(path).write();
    }
    
    /**
     * Make a property provider that loads from this file
     * and fires changes when it is written to (even in memory).
     */
    public PropertyProvider getPropertyProvider(String path) {
        return getPP(path);
    }
    
    private PP getPP(String path) {
        PP pp = properties.get(path);
        if (pp == null) {
            pp = new PP(path, helper);
            properties.put(path, pp);
        }
        return pp;
    }
    
    private static final class PP implements PropertyProvider, FileChangeListener {
        
        private static final RequestProcessor RP = new RequestProcessor("ProjectProperties.PP.RP"); // NOI18N
        
        // XXX lock any loaded property files while the project is modified, to prevent manual editing,
        // and reload any modified files if the project is unmodified

        private final String path;
        private final RakeProjectHelper helper;
        private EditableProperties properties = null;
        private boolean loaded = false;
        private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private boolean writing = false;
        
        public PP(String path, RakeProjectHelper helper) {
            this.path = path;
            this.helper = helper;
            FileUtil.addFileChangeListener(this, new File(FileUtil.toFile(dir()), path.replace('/', File.separatorChar)));
        }
        
        private FileObject dir() {
            return helper.getProjectDirectory();
        }
        
        public EditableProperties getEditablePropertiesOrNull() {
            if (!loaded) {
                properties = null;
                FileObject fo = dir().getFileObject(path);
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
            return properties;
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
        
        public FileLock write() throws IOException {
            assert loaded;
            final FileObject f = dir().getFileObject(path);
            assert !writing;
            final FileLock[] _lock = new FileLock[1];
            writing = true;
            try {
                if (properties != null) {
                    // Supposed to create/modify the file.
                    // Need to use an atomic action - otherwise listeners will first
                    // receive an event that the file has been written to zero length
                    // (which for *.properties means no keys), which is wrong.
                    dir().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            final FileObject _f;
                            if (f == null) {
                                _f = FileUtil.createData(dir(), path);
                                assert _f != null : "FU.cD must not return null; called on " + dir() + " + " + path; // #50802
                            } else {
                                _f = f;
                            }
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            properties.store(baos);
                            final byte[] data = baos.toByteArray();
                            try {
                                _lock[0] = _f.lock(); // released by {@link RakeProjectHelper#save}
                                OutputStream os = _f.getOutputStream(_lock[0]);
                                try {
                                    os.write(data);
                                } finally {
                                    os.close();
                                }
                            } catch (UserQuestionException uqe) { // #46089
                                helper.needPendingHook();
                                UserQuestionHandler.handle(uqe, new UserQuestionHandler.Callback() {
                                    public void accepted() {
                                        // Try again.
                                        assert !writing;
                                        writing = true;
                                        try {
                                            FileLock lock = _f.lock();
                                            try {
                                                OutputStream os = _f.getOutputStream(lock);
                                                try {
                                                    os.write(data);
                                                } finally {
                                                    os.close();
                                                }
                                            } finally {
                                                lock.releaseLock();
                                            }
                                            helper.maybeCallPendingHook();
                                        } catch (IOException e) {
                                            // Oh well.
                                            ErrorManager.getDefault().notify(e);
                                            reload();
                                        } finally {
                                            writing = false;
                                        }
                                    }
                                    public void denied() {
                                        reload();
                                    }
                                    public void error(IOException e) {
                                        ErrorManager.getDefault().notify(e);
                                        reload();
                                    }
                                    private void reload() {
                                        helper.cancelPendingHook();
                                        // Revert the save.
                                        diskChange();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    // We are supposed to remove any existing file.
                    if (f != null) {
                        f.delete();
                    }
                }
            } catch (IOException e) {
                if (_lock[0] != null) {
                    // Release it now, since no one else will.
                    _lock[0].releaseLock();
                }
                throw e;
            } finally {
                writing = false;
            }
            return _lock[0];
        }
        
        public Map<String,String> getProperties() {
            Map<String,String> props = getEditablePropertiesOrNull();
            if (props != null) {
                return Collections.unmodifiableMap(props);
            } else {
                return Collections.emptyMap();
            }
        }
        
        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        
        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        private void fireChange() {
            final ChangeListener[] ls;
            synchronized (this) {
                if (listeners.isEmpty()) {
                    return;
                }
                ls = listeners.toArray(new ChangeListener[listeners.size()]);
            }
            final ChangeEvent ev = new ChangeEvent(this);
            final Mutex.Action<Void> action = new Mutex.Action<Void>() {
                public Void run() {
                    for (ChangeListener l : ls) {
                        l.stateChanged(ev);
                    }
                    return null;
                }
            };
            if (ProjectManager.mutex().isWriteAccess()) {
                // Run it right now. postReadRequest would be too late.
                ProjectManager.mutex().readAccess(action);
            } else if (ProjectManager.mutex().isReadAccess()) {
                // Run immediately also. No need to switch to read access.
                action.run();
            } else {
                // Not safe to acquire a new lock, so run later in read access.
                RP.post(new Runnable() {
                    public void run() {
                        ProjectManager.mutex().readAccess(action);
                    }
                });
            }
        }
        
        private void diskChange() {
            // XXX should check for a possible clobber from in-memory data
            if (!writing) {
                loaded = false;
            }
            fireChange();
            if (!writing) {
                helper.fireExternalChange(path);
            }
        }

        public void fileFolderCreated(FileEvent fe) {
            diskChange();
        }

        public void fileDataCreated(FileEvent fe) {
            diskChange();
        }

        public void fileChanged(FileEvent fe) {
            diskChange();
        }

        public void fileRenamed(FileRenameEvent fe) {
            diskChange();
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            diskChange();
        }

        public void fileDeleted(FileEvent fe) {
            diskChange();
        }
        
    }

    /**
     * See {@link RakeProjectHelper#getStockPropertyPreprovider}.
     */
    public PropertyProvider getStockPropertyPreprovider() {
        if (stockPropertyPreprovider == null) {
            Map<String,String> m = new HashMap<String,String>();
            Properties p = System.getProperties();
            synchronized (p) {
                for (Map.Entry<Object,Object> entry : p.entrySet()) {
                    try {
                        m.put((String) entry.getKey(), (String) entry.getValue());
                    } catch (ClassCastException e) {
                        Logger.getLogger(ProjectProperties.class.getName()).warning(
                                "WARNING: removing non-String-valued system property " + entry.getKey() + "=" + entry.getValue() + " (cf. #45788)");
                    }
                }
            }
            m.put("basedir", FileUtil.toFile(helper.getProjectDirectory()).getAbsolutePath()); // NOI18N
            File antJar = InstalledFileLocator.getDefault().locate("ant/lib/ant.jar", "org.apache.tools.ant.module", false); // NOI18N
            if (antJar != null) {
                File antHome = antJar.getParentFile().getParentFile();
                m.put("ant.home", antHome.getAbsolutePath()); // NOI18N
            }
            stockPropertyPreprovider = PropertyUtils.fixedPropertyProvider(m);
        }
        return stockPropertyPreprovider;
    }
    
    /**
     * See {@link RakeProjectHelper#getStandardPropertyEvaluator}.
     */
    public PropertyEvaluator getStandardPropertyEvaluator() {
        if (standardPropertyEvaluator == null) {
            PropertyEvaluator findUserPropertiesFile = PropertyUtils.sequentialPropertyEvaluator(
                getStockPropertyPreprovider(),
                getPropertyProvider(RakeProjectHelper.PRIVATE_PROPERTIES_PATH));
            PropertyProvider globalProperties = PropertyUtils.userPropertiesProvider(findUserPropertiesFile,
                    "user.properties.file", FileUtil.toFile(helper.getProjectDirectory())); // NOI18N
            standardPropertyEvaluator = PropertyUtils.sequentialPropertyEvaluator(
                getStockPropertyPreprovider(),
                getPropertyProvider(RakeProjectHelper.PRIVATE_PROPERTIES_PATH),
                globalProperties,
                getPropertyProvider(RakeProjectHelper.PROJECT_PROPERTIES_PATH));
        }
        return standardPropertyEvaluator;
    }
    
}
