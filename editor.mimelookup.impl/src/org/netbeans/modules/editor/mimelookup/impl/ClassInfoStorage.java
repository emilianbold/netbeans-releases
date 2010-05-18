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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author vita
 */
public final class ClassInfoStorage {
    
    public static final String PROP_CLASS_INFO_CHANGED = "Class2Paths.PROP_CLASS_MAPPING_CHANGED"; //NOI18N
    public static final String PROP_CLASS_INFO_ADDED = "Class2Paths.PROP_CLASS_MAPPING_ADDED"; //NOI18N
    public static final String PROP_CLASS_INFO_REMOVED = "Class2Paths.PROP_CLASS_MAPPING_REMOVED"; //NOI18N
    
    private static Logger LOG = Logger.getLogger(ClassInfoStorage.class.getName());
    
    private static ClassInfoStorage instance = null;
    
    public static synchronized ClassInfoStorage getInstance() {
        if (instance == null) {
            instance = new ClassInfoStorage();
        }
        return instance;
    }
    
    private Lookup.Result<Class2LayerFolder> mappers = null;
    private L mappersListener = null;

    private final String LOCK = new String("Class2Paths.LOCK"); //NOI18N
    private Map<String,Info> mapping = new HashMap<String,Info>();
    
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    /** Creates a new instance of Class2Paths */
    private ClassInfoStorage() {
        mappers = Lookup.getDefault().lookupResult(Class2LayerFolder.class);
        
        mappersListener = new L();
        mappers.addLookupListener(WeakListeners.create(LookupListener.class, mappersListener, mappers));
        
        rebuild();
    }

    public Info getInfo(String className) {
        synchronized (LOCK) {
            if (mapping.containsKey(className)) {
                return mapping.get(className);
            } else {
                return new Info(this, className, null, null);
            }
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private List<PropertyChangeEvent> rebuild() {
        synchronized (LOCK) {
            // Gather the new mapping information
            Map<String,Info> newMapping = new HashMap<String,Info>();
            for (Class2LayerFolder mapper : mappers.allInstances()) {
                String className = mapper.getClazz().getName();
                String path = mapper.getLayerFolderName();
                InstanceProvider ip = mapper.getInstanceProvider();

                if (path != null) {
                    path = path.trim();
                }
                
                if ((path == null || path.length() == 0) && ip == null) {
                    // Hmm, why anybody registered mapper, which doesn't provide any info.
                    continue;
                }
                
                if (!newMapping.containsKey(className)) {
                    newMapping.put(className, new Info(this, className, path, ip));
                } else {
                    LOG.warning("The mapping for class '" + className + "' to folder '" +  //NOI18N
                        path + "' and InstanceProvider '" + ip + "' has already been " + //NOI18N
                        "defined by another mapper. Ignoring mapper " + mapper); //NOI18N
                }
            }
        
            // Compute differences
            Set<String> removed = new HashSet<String>(mapping.keySet());
            removed.removeAll(newMapping.keySet());
            
            Set<String> added = new HashSet<String>(newMapping.keySet());
            added.removeAll(mapping.keySet());
            
            Set<String> changed = new HashSet<String>();
            for (String className : newMapping.keySet()) {
                if (mapping.containsKey(className) && 
                    !Utilities.compareObjects(newMapping.get(className), mapping.get(className)))
                {
                    changed.add(className);
                }
            }
            
            // Update the mapping
            mapping.clear();
            mapping.putAll(newMapping);
            
            // Generate events
            List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>(3);
            if (!removed.isEmpty()) {
                events.add(new PropertyChangeEvent(this, PROP_CLASS_INFO_REMOVED, null, removed));
            }
            if (!added.isEmpty()) {
                events.add(new PropertyChangeEvent(this, PROP_CLASS_INFO_ADDED, null, added));
            }
            if (!changed.isEmpty()) {
                events.add(new PropertyChangeEvent(this, PROP_CLASS_INFO_CHANGED, null, changed));
            }
            
            return events;
        }
    }
    
    private class L implements LookupListener {

        public void resultChanged(LookupEvent ev) {
            // Update mapping information & fire change events if neccessary
            for (PropertyChangeEvent event : rebuild()) {
                pcs.firePropertyChange(event);
            }
        }
        
    } // End of L class
    
    public static final class Info {
        private ClassInfoStorage storage;
        private String className;
        private String extraPath;
        private String instanceProviderClass;
        private Reference<InstanceProvider<?>> ref; // TODO: This should really be a timed-weak-ref
        
        private Info(ClassInfoStorage storage, String className, String extraPath, InstanceProvider<?> instanceProvider) {
            this.storage = storage;
            this.className = className;
            this.extraPath = extraPath == null ? "" : extraPath; //NOI18N
            if (instanceProvider != null) {
                this.instanceProviderClass = instanceProvider.getClass().getName();
                this.ref = new WeakReference<InstanceProvider<?>>(instanceProvider);
            }
        }
        
        public String getClassName() {
            return className;
        }
        
        public String getExtraPath() {
            return extraPath;
        }
        
        public String getInstanceProviderClass() {
            return instanceProviderClass;
        }
        
        public InstanceProvider<?> getInstanceProvider() {
            synchronized (storage.LOCK) {
                // There was no instance provider specified
                if (ref == null) {
                    return null;
                }
                
                InstanceProvider ip = (InstanceProvider) ref.get();
                if (ip == null) {
                    // Instance provider has been GCed, recreate it
                    for (Class2LayerFolder mapper : storage.mappers.allInstances()) {
                        String className = mapper.getClazz().getName();
                        
                        if (this.className.equals(className)) {
                            ip = mapper.getInstanceProvider();
                            break;
                        }
                    }
                    
                    if (ip != null) {
                        ref = new WeakReference<InstanceProvider<?>>(ip);
                    }
                }
                
                return ip;
            }
        }

        public @Override boolean equals(Object obj) {
            if (obj instanceof Info) {
                Info info = (Info) obj;
                return  this.className.equals(info.className) &&
                        Utilities.compareObjects(this.extraPath, info.extraPath) && 
                        Utilities.compareObjects(this.instanceProviderClass, info.instanceProviderClass);
            } else {
                return false;
            }
        }

        public @Override int hashCode() {
            int hashCode = className.hashCode();
            
            if (extraPath != null) {
                hashCode += 7 * extraPath.hashCode();
            }
            if (instanceProviderClass != null) {
                hashCode += 13 * instanceProviderClass.hashCode();
            }
            
            return hashCode;
        }

        public @Override String toString() {
            return "ClassInfoStorage.Info[className=" + className + ",extraPath=" + extraPath + ",instanceProviderClass=" + instanceProviderClass + "]"; // NOI18N
        }

    } // End of Info class
}
