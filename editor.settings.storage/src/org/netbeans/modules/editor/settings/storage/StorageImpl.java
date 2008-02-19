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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.settings.storage;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.settings.storage.spi.StorageDescription;
import org.netbeans.modules.editor.settings.storage.spi.StorageReader;
import org.netbeans.modules.editor.settings.storage.spi.StorageWriter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Vita Stejskal
 */
public final class StorageImpl <K extends Object, V extends Object> {
    
    // -J-Dorg.netbeans.modules.editor.settings.storage.StorageImpl.level=FINE
    private static final Logger LOG = Logger.getLogger(StorageImpl.class.getName());

    public StorageImpl(StorageDescription<K, V> sd) {
        this.storageDescription = sd;
        this.sfs = Repository.getDefault().getDefaultFileSystem();
        this.baseFolder = sfs.findResource("Editors"); //NOI18N
    }

    public Map<K, V> load(MimePath mimePath, String profile, boolean defaults) throws IOException {
        assert mimePath != null : "The parameter mimePath must not be null"; //NOI18N
        if (storageDescription.isUsingProfiles()) {
            assert profile != null : "The parameter profile must not be null"; //NOI18N
        } else {
            assert profile == null : "The '" + storageDescription.getId() + "' settings type does not use profiles."; //NOI18N
        }
        
        synchronized (lock) {
            Map<K, V> data;
            Map<CacheKey, Map<K, V>> profilesData = profilesCache.get(mimePath);
            CacheKey cacheKey = cacheKey(profile, defaults);
            
            if (profilesData == null) {
                data = null;
                profilesData = new HashMap<CacheKey, Map<K, V>>();
                profilesCache.put(mimePath, profilesData);
            } else {
                data = profilesData.get(cacheKey);
            }

            if (data == null) {
                data = _load(mimePath, profile, defaults);
                profilesData.put(cacheKey, data);
            }
            
            return data;
        }
    }
    
    public void save(MimePath mimePath, String profile, boolean defaults, Map<K, V> data) throws IOException {
        assert mimePath != null : "The parameter mimePath must not be null"; //NOI18N
        if (storageDescription.isUsingProfiles()) {
            assert profile != null : "The parameter profile must not be null"; //NOI18N
        } else {
            assert profile == null : "The '" + storageDescription.getId() + "' settings type does not use profiles."; //NOI18N
        }
        
        synchronized (lock) {
            Map<CacheKey, Map<K, V>> profilesData = profilesCache.get(mimePath);
            if (profilesData == null) {
                profilesData = new HashMap<CacheKey, Map<K, V>>();
                profilesCache.put(mimePath, profilesData);
            }

            boolean resetCache = _save(mimePath, profile, defaults, data);
            if (!resetCache) {
                profilesData.put(cacheKey(profile, defaults), data);
            } else {
                profilesData.remove(cacheKey(profile, defaults));
            }
        }
    }

    public void delete(MimePath mimePath, String profile, boolean defaults) throws IOException {
        assert mimePath != null : "The parameter mimePath must not be null"; //NOI18N
        if (storageDescription.isUsingProfiles()) {
            assert profile != null : "The parameter profile must not be null"; //NOI18N
        } else {
            assert profile == null : "The '" + storageDescription.getId() + "' settings type does not use profiles."; //NOI18N
        }
        
        synchronized (lock) {
            Map<CacheKey, Map<K, V>> profilesData = profilesCache.get(mimePath);
            if (profilesData != null) {
                profilesData.remove(cacheKey(profile, defaults));
            }
            _delete(mimePath, profile, defaults);
        }
    }

    public static interface Operations<K extends Object, V extends Object> {
        public Map<K, V> load(MimePath mimePath, String profile, boolean defaults) throws IOException;
        public boolean save(MimePath mimePath, String profile, boolean defaults, Map<K, V> data, Map<K, V> defaultData) throws IOException;
        public void delete(MimePath mimePath, String profile, boolean defaults) throws IOException;
    } // End of Operations interface
    
    // ------------------------------------------
    // private implementation
    // ------------------------------------------
    
    private final StorageDescription<K, V> storageDescription;
    private final FileSystem sfs;
    private final FileObject baseFolder;
    
    private final Object lock = new String("StorageImpl.lock"); //NOI18N
    private final Map<MimePath, Map<CacheKey, Map<K, V>>> profilesCache = new WeakHashMap<MimePath, Map<CacheKey, Map<K, V>>>();

    private List<Object []> scan(MimePath mimePath, String profile, boolean scanModules, boolean scanUsers) {
        Map<String, List<Object []>> files = new HashMap<String, List<Object []>>();

        SettingsType.getLocator(storageDescription).scan(baseFolder, mimePath.getPath(), profile, true, scanModules, scanUsers, mimePath.size() > 1, files);
        assert files.size() <= 1 : "Too many results in the scan"; //NOI18N
        
        return files.get(profile);
    }
    
    private Map<K, V> _load(MimePath mimePath, String profile, boolean defaults) throws IOException {
        if (storageDescription instanceof Operations) {
            @SuppressWarnings("unchecked")
            Operations<K, V> operations = (Operations<K, V>) storageDescription;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Forwarding loading of '" + storageDescription.getId() + "' to: " + operations); //NOI18N
            }
            return operations.load(mimePath, profile, defaults);
        } else {
            // Perform the operation
            List<Object []> profileInfos = scan(mimePath, profile, true, !defaults);
            Map<K, V> map = new HashMap<K, V>();

            if (profileInfos != null) {
                for(Object [] info : profileInfos) {
                    assert info.length == 5;
                    FileObject profileHome = (FileObject) info[0];
                    FileObject settingFile = (FileObject) info[1];
                    boolean modulesFile = ((Boolean) info[2]).booleanValue();
                    FileObject linkTarget = (FileObject) info[3];
                    boolean legacyFile = ((Boolean) info[4]).booleanValue();
                    
                    if (linkTarget != null) {
                        // link to another mimetype
                        MimePath linkedMimePath = MimePath.parse(linkTarget.getPath().substring(baseFolder.getPath().length() + 1));
                        assert linkedMimePath != mimePath : "linkedMimePath should not be the same as the original one"; //NOI18N
                        
                        if (linkedMimePath.size() == 1) {
                            Map<K, V> linkedMap = load(linkedMimePath, profile, defaults);
                            map.putAll(linkedMap);
                            LOG.fine("Adding linked '" + storageDescription.getId() + "' from: '" + linkedMimePath.getPath() + "'"); //NOI18N
                        } else {
                            if (LOG.isLoggable(Level.WARNING)) {
                                LOG.warning("Linking to other than top level mime types is prohibited. " //NOI18N
                                    + "Ignoring editor settings link from '" + mimePath.getPath() //NOI18N
                                    + "' to '" + linkedMimePath.getPath() + "'"); //NOI18N
                            }
                        }
                    } else {
                        // real settings file
                        StorageReader<? extends K, ? extends V> reader = storageDescription.createReader(settingFile, mimePath.getPath());

                        // Load data from the settingFile
                        Utils.load(settingFile, reader, !legacyFile);
                        Map<? extends K, ? extends V> added = reader.getAdded();
                        Set<? extends K> removed = reader.getRemoved();

                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Loading '" + storageDescription.getId() + "' from: '" + settingFile.getPath() + "'"); //NOI18N
                        }

                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.finest("--- Removing '" + storageDescription.getId() + "': " + removed); //NOI18N
                        }

                        // First remove all entries marked as removed
                        for(K key : removed) {
                            map.remove(key);
                        }

                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.finest("--- Adding '" + storageDescription.getId() + "': " + added); //NOI18N
                        }

                        // Then add all new entries
                        for (K key : added.keySet()) {
                            V value = added.get(key);
                            V origValue = map.put(key, value);
                            if (LOG.isLoggable(Level.FINEST) && origValue != null && !origValue.equals(value)) {
                                LOG.finest("--- Replacing old entry for '" + key + "', orig value = '" + origValue + "', new value = '" + value + "'"); //NOI18N
                            }
                        }

                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.finest("-------------------------------------"); //NOI18N
                        }
                    }
                }
            }

            return Collections.unmodifiableMap(map);
        }
    }
    
    private boolean _save(MimePath mimePath, String profile, boolean defaults, Map<K, V> data) throws IOException {
        Map<K, V> defaultData = load(mimePath, profile, true);
        
        if (storageDescription instanceof Operations) {
            @SuppressWarnings("unchecked")
            Operations<K, V> operations = (Operations<K, V>) storageDescription;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Forwarding saving of '" + storageDescription.getId() + "' to: " + operations); //NOI18N
            }
            return operations.save(mimePath, profile, defaults, data, defaultData);
        } else {
            final Map<K, V> added = new HashMap<K, V>();
            final Map<K, V> removed = new HashMap<K, V>();
            Utils.diff(defaultData, data, added, removed);

            // Perform the operation
            final String mimePathString = mimePath.getPath();
            final String settingFileName = SettingsType.getLocator(storageDescription).getWritableFileName(
                mimePathString, profile, null, defaults);
            
            sfs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    if (added.size() > 0 || removed.size() > 0) {
                        FileObject f = FileUtil.createData(baseFolder, settingFileName);
                        StorageWriter<K, V> writer = storageDescription.createWriter(f, mimePathString);
                        writer.setAdded(added);
                        writer.setRemoved(removed.keySet());
                        Utils.save(f, writer);
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Saving '" + storageDescription.getId() + "' to: '" + f.getPath() + "'"); //NOI18N
                        }
                    } else {
                        FileObject f = baseFolder.getFileObject(settingFileName);
                        if (f != null) {
                            f.delete();
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Saving '" + storageDescription.getId() + 
                                    "', no changes from defaults therefore deleting: '" + f.getPath() + "'"); //NOI18N
                            }
                        }
                    }
                }
            });
            
            return false;
        }
    }

    private void _delete(MimePath mimePath, String profile, boolean defaults) throws IOException {
        if (storageDescription instanceof Operations) {
            @SuppressWarnings("unchecked")
            Operations<K, V> operations = (Operations<K, V>) storageDescription;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Forwarding deletion of '" + storageDescription.getId() + "' to: " + operations); //NOI18N
            }
            operations.delete(mimePath, profile, defaults);
        } else {
            // Perform the operation
            final List<Object []> profileInfos = scan(mimePath, profile, defaults, !defaults);
            if (profileInfos != null) {
                sfs.runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        for(Object [] info : profileInfos) {
                            assert info.length == 5;
                            FileObject profileHome = (FileObject) info[0];
                            FileObject settingFile = (FileObject) info[1];
                            boolean modulesFile = ((Boolean) info[2]).booleanValue();
                            
                            // will delete either a real settings file or a link file (.shadow)
                            settingFile.delete();
                            
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Deleting '" + storageDescription.getId() + "' file: '" + settingFile.getPath() + "'"); //NOI18N
                            }
                        }
                    }
                });
            }
        }
    }

    private static CacheKey cacheKey(String profile, boolean defaults) {
        return new CacheKey(profile, defaults);
    }
    
    private static final class CacheKey {
        private final String profile;
        private final boolean defaults;
        
        public CacheKey(String profile, boolean defaults) {
            this.profile = profile;
            this.defaults = defaults;
        }

        public @Override boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CacheKey other = (CacheKey) obj;
            if ((this.profile == null && other.profile != null) ||
                (this.profile != null && other.profile == null) ||
                (this.profile != null && !this.profile.equals(other.profile))
            ) {
                return false;
            }
            if (this.defaults != other.defaults) {
                return false;
            }
            return true;
        }

        public @Override int hashCode() {
            return this.profile != null ? this.profile.hashCode() : 7;
        }
        
    } // End of CacheKey class
}
