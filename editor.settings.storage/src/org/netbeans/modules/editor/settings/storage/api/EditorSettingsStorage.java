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

package org.netbeans.modules.editor.settings.storage.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.settings.storage.SettingsType;
import org.netbeans.modules.editor.settings.storage.StorageImpl;
import org.netbeans.modules.editor.settings.storage.spi.StorageDescription;

/**
 *
 * @author vita
 */
public final class EditorSettingsStorage <K extends Object, V extends Object> {

    public static final String PROP_DATA = "EditorSettingsStorage.PROP_DATA"; //NOI18N
    
    public static <K extends Object, V extends Object> EditorSettingsStorage<K, V> get(String settingsTypeId) {
        EditorSettingsStorage<K, V> ess = EditorSettingsStorage.<K, V>find(settingsTypeId);
        assert ess != null : "Invalid settings type Id: '" + settingsTypeId + "'"; //NOI18N
        return ess;
    }
    
   @SuppressWarnings("unchecked")
    public static <K extends Object, V extends Object> EditorSettingsStorage<K, V> find(String settingsTypeId) {
        synchronized (cache) {
            EditorSettingsStorage<K, V> ess = null;
            StorageDescription<K, V> sd = SettingsType.<K, V>find(settingsTypeId);
            
            if (sd != null) {
                ess = cache.get(sd);
                if (ess == null) {
                    ess = new EditorSettingsStorage<K, V>(sd);
                    cache.put(sd, ess);
                }
            }            
            
            return ess;
        }
    }
    
    public Map<K, V> load(MimePath mimePath, String profile, boolean defaults) throws IOException {
        return storageImpl.load(mimePath, profile, defaults);
    }
    
    public void save(MimePath mimePath, String profile, boolean defaults, Map<K, V> data) throws IOException {
        storageImpl.save(mimePath, profile, defaults, data);
    }

    public void delete(MimePath mimePath, String profile, boolean defaults) throws IOException {
        storageImpl.delete(mimePath, profile, defaults);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        PCS.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        PCS.removePropertyChangeListener(l);
    }
    
    // ------------------------------------------
    // private implementation
    // ------------------------------------------

    private static final Map<StorageDescription<?, ?>, EditorSettingsStorage> cache = new HashMap<StorageDescription<?, ?>, EditorSettingsStorage>();
    
    private final PropertyChangeSupport PCS = new PropertyChangeSupport(this);
    private final StorageImpl<K, V> storageImpl;
    
    private EditorSettingsStorage(StorageDescription<K, V> storageDescription) {
        this.storageImpl = new StorageImpl<K, V>(storageDescription, new Callable<Void>() {
            public Void call() {
                PCS.firePropertyChange(PROP_DATA, null, null);
                return null;
            }
        });
    }
}
