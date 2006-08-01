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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
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
    
    private Lookup.Result mappers = null;
    private L mappersListener = null;

    private final String LOCK = new String("Class2Paths.LOCK"); //NOI18N
    private HashMap mapping = new HashMap();
    
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    /** Creates a new instance of Class2Paths */
    private ClassInfoStorage() {
        mappers = Lookup.getDefault().lookupResult(Class2LayerFolder.class);
        
        mappersListener = new L();
        mappers.addLookupListener((LookupListener) WeakListeners.create(LookupListener.class, mappersListener, mappers));
        
        rebuild();
    }

    public Info getInfo(String className) {
        synchronized (LOCK) {
            if (mapping.containsKey(className)) {
                return (Info) mapping.get(className);
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

    private List rebuild() {
        synchronized (LOCK) {
            // Gather the new mapping information
            HashMap newMapping = new HashMap();
            Collection newMappers = mappers.allInstances();
            
            for (Iterator i = newMappers.iterator(); i.hasNext(); ) {
                Class2LayerFolder mapper = (Class2LayerFolder) i.next();
                
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
            HashSet removed = new HashSet(mapping.keySet());
            removed.removeAll(newMapping.keySet());
            
            HashSet added = new HashSet(newMapping.keySet());
            added.removeAll(mapping.keySet());
            
            HashSet changed = new HashSet();
            for (Iterator i = newMapping.keySet().iterator(); i.hasNext(); ) {
                String className = (String) i.next();
                
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
            ArrayList events = new ArrayList(3);
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
            // Update mapping information
            List events = rebuild();
            
            // Fire change events if neccessary
            for (Iterator i = events.iterator(); i.hasNext(); ) {
                PropertyChangeEvent event = (PropertyChangeEvent) i.next();
                pcs.firePropertyChange(event);
            }
        }
        
    } // End of L class
    
    public static final class Info {
        private ClassInfoStorage storage;
        private String className;
        private String extraPath;
        private String instanceProviderClass;
        private WeakReference ref; // TODO: This should really be a timed-weak-ref
        
        private Info(ClassInfoStorage storage, String className, String extraPath, InstanceProvider instanceProvider) {
            this.storage = storage;
            this.className = className;
            this.extraPath = extraPath == null ? "" : extraPath; //NOI18N
            if (instanceProvider != null) {
                this.instanceProviderClass = instanceProvider.getClass().getName();
                this.ref = new WeakReference(instanceProvider);
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
        
        public InstanceProvider getInstanceProvider() {
            synchronized (storage.LOCK) {
                // There was no instance provider specified
                if (ref == null) {
                    return null;
                }
                
                InstanceProvider ip = (InstanceProvider) ref.get();
                if (ip == null) {
                    // Instance provider has been GCed, recreate it
                    Collection instances = storage.mappers.allInstances();
                    for (Iterator i = instances.iterator(); i.hasNext(); ) {
                        Class2LayerFolder mapper = (Class2LayerFolder) i.next();
                        String className = mapper.getClazz().getName();
                        
                        if (this.className.equals(className)) {
                            ip = mapper.getInstanceProvider();
                            break;
                        }
                    }
                    
                    if (ip != null) {
                        ref = new WeakReference(ip);
                    }
                }
                
                return ip;
            }
        }

        public boolean equals(Object obj) {
            if (obj instanceof Info) {
                Info info = (Info) obj;
                return  this.className.equals(info.className) &&
                        Utilities.compareObjects(this.extraPath, info.extraPath) && 
                        Utilities.compareObjects(this.instanceProviderClass, info.instanceProviderClass);
            } else {
                return false;
            }
        }

        public int hashCode() {
            int hashCode = className.hashCode();
            
            if (extraPath != null) {
                hashCode += 7 * extraPath.hashCode();
            }
            if (instanceProviderClass != null) {
                hashCode += 13 * instanceProviderClass.hashCode();
            }
            
            return hashCode;
        }
    } // End of Info class
}
