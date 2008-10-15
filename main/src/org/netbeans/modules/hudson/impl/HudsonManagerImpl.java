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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.hudson.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * Implementation of the HudsonManager
 *
 * @author Michal Mocnak
 */
public class HudsonManagerImpl extends HudsonManager {
    
    /** Startup flag property */
    public static final String STARTUP_PROP = "startup";
    
    /** Init lock */
    private static final Object LOCK_INIT = new Object();
    
    /** Directory where instances are stored */
    private static final String DIR_INSTANCES = "/Hudson/Instances"; //NOI18N
    
    /** The only instance of the hudson manager implementation in the system */
    private static HudsonManagerImpl defaultInstance;
    
    private Map<String, HudsonInstanceImpl> instances;
    private List<HudsonChangeListener> listeners = new ArrayList<HudsonChangeListener>();
    
    public HudsonManagerImpl() {
        synchronized(LOCK_INIT) {
            // a static object to synchronize on
            if (null != defaultInstance)
                throw new IllegalStateException("Instance already exists"); // NOI18N
            
            defaultInstance = this;
        }
    }
    
    /**
     * Singleton accessor
     *
     * @return instance of hudson manager implementation
     */
    public static HudsonManagerImpl getInstance() {
        if (null != defaultInstance) {
            // Save a bunch of time accessing global lookup, acc. to profiler.
            return defaultInstance;
        }
        
        return (HudsonManagerImpl) Lookup.getDefault().lookup(HudsonManager.class);
    }
    
    public HudsonInstanceImpl addInstance(final HudsonInstanceImpl instance) {
        if (null == instance || null != getInstancesMap().get(instance.getUrl()))
            return null;
        
        if (null != getInstancesMap().put(instance.getUrl(), instance))
            return null;
        
        fireChangeListeners();
        
        // Strore instance file
        storeInstanceFile(instance);
        
        // Add property change listener
        instance.getProperties().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                storeInstanceFile(instance);
            }
        });
        
        return instance;
    }
    
    public HudsonInstanceImpl removeInstance(HudsonInstanceImpl instance) {
        if (null == instance || null == getInstancesMap().get(instance.getUrl()))
            return null;
        
        if (null == getInstancesMap().remove(instance.getUrl()))
            return null;
        
        // Stop autosynchronization if it's running
        instance.terminate();
        
        // Fire changes into all listeners
        fireChangeListeners();
        
        // Remove instance file
        removeInstanceFile(instance);
        
        return instance;
    }
    
    /**
     *
     * @param url
     * @return
     */
    public HudsonInstance getInstance(String url) {
        return getInstancesMap().get(url);
    }
    
    /**
     *
     * @return
     */
    public synchronized Collection<HudsonInstance> getInstances() {
        return Arrays.asList(getInstancesMap().values().toArray(new HudsonInstance[] {}));
    }
    
    /**
     *
     * @param name
     * @return
     */
    public HudsonInstance getInstanceByName(String name) {
        for (HudsonInstance h : getInstances()) {
            if (h.getName().equals(name))
                return h;
        }
        
        return null;
    }
    
    public void addHudsonChangeListener(HudsonChangeListener l) {
        listeners.add(l);
    }
    
    public void removeHudsonChangeListener(HudsonChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChangeListeners() {
        ArrayList<HudsonChangeListener> tempList;
        
        synchronized (listeners) {
            tempList = new ArrayList<HudsonChangeListener>(listeners);
        }
        
        for (HudsonChangeListener l : tempList) {
            l.stateChanged();
            l.contentChanged();
        }
    }
    
    public void terminate() {
        // Clear default instance
        defaultInstance = null;
        
        // Terminate instances
        for (HudsonInstance instance : getInstances())
            ((HudsonInstanceImpl) instance).terminate();
    }
    
    private void storeInstanceFile(HudsonInstanceImpl instance) {
        Repository repository = Lookup.getDefault().lookup(Repository.class);
        FileObject directory = repository.getDefaultFileSystem().findResource(DIR_INSTANCES);
        
        String fileName = instance.getName().replace(" ", "_").toLowerCase() + ".xml";
        
        try {
            FileObject file = directory.getFileObject(fileName);
            
            if (null == file)
                file = directory.createData(fileName);
            
            // Write data to the file
            instance.getProperties().storeToXML(new FileOutputStream(FileUtil.toFile(file)), instance.getName());
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    private void removeInstanceFile(HudsonInstanceImpl instance) {
        Repository repository = Lookup.getDefault().lookup(Repository.class);
        FileObject directory = repository.getDefaultFileSystem().findResource(DIR_INSTANCES);
        
        String fileName = instance.getName().replace(" ", "_").toLowerCase() + ".xml";
        
        try {
            FileObject file = directory.getFileObject(fileName);
            
            if (null == file)
                return;
            
            file.delete();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    private Map<String, HudsonInstanceImpl> getInstancesMap() {
        if (null == instances) {
            instances = new HashMap<String, HudsonInstanceImpl>();
            
            // initialization
            init();
        }
        
        return instances;
    }
    
    private void init() {
        // activate startup flag
        NbPreferences.forModule(HudsonManager.class).putBoolean(STARTUP_PROP, true);
        
        Repository repository = Lookup.getDefault().lookup(Repository.class);
        final FileObject directory = repository.getDefaultFileSystem().findResource(DIR_INSTANCES);
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    for (FileObject f : directory.getChildren())
                        HudsonInstanceImpl.createHudsonInstance(new HudsonInstanceProperties(f));
                } finally {
                    // Deactivate startup flag
                    NbPreferences.forModule(HudsonManager.class).putBoolean(STARTUP_PROP, false);
                    
                    // Fire changes
                    fireChangeListeners();
                }
            }
        });
    }
}