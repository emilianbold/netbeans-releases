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

package org.netbeans.modules.hudson.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

/**
 * Implementation of the HudsonManager
 *
 * @author Michal Mocnak
 */
public class HudsonManagerImpl implements HudsonManager {
    
    private static final String DIR_INSTANCES = "/Hudson/Instances"; //NOI18N
    
    private static HudsonManagerImpl instance;
    
    private Map<String, HudsonInstance> instances;
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    private HudsonManagerImpl() {}
    
    /**
     *
     * @return
     */
    public synchronized static HudsonManagerImpl getDefault() {
        if (null == instance)
            instance = new HudsonManagerImpl();
        
        return instance;
    }
    
    public HudsonInstanceImpl addInstance(final HudsonInstanceImpl instance) {
        if (null == instance || null != instancesMap().get(instance.getUrl()))
            return null;
        
        if (null != instancesMap().put(instance.getUrl(), instance))
            return null;
        
        fireChangeListeners();
        
        // Strore instance file
        storeInstanceFile(instance);
        
        // Add change listener
        instance.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                storeInstanceFile(instance);
            }
        });
        
        return instance;
    }
    
    public HudsonInstanceImpl removeInstance(HudsonInstanceImpl instance) {
        if (null == instance || null == instancesMap().get(instance.getUrl()))
            return null;
        
        if (null == instancesMap().remove(instance.getUrl()))
            return null;
        
        // Stop autosynchronization if it's running
        instance.stopAutoSynchronization();
        
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
        return instancesMap().get(url);
    }
    
    /**
     *
     * @return
     */
    public Collection<HudsonInstance> getInstances() {
        return instancesMap().values();
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
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChangeListeners() {
        ChangeEvent event = new ChangeEvent(this);
        ArrayList<ChangeListener> tempList;
        
        synchronized (listeners) {
            tempList = new ArrayList<ChangeListener>(listeners);
        }
        
        for (ChangeListener l : tempList) {
            l.stateChanged(event);
        }
    }
    
    private Map<String, HudsonInstance> instancesMap() {
        if (null == instances) {
            instances = new HashMap<String, HudsonInstance>();
            
            Repository repository = Lookup.getDefault().lookup(Repository.class);
            FileObject directory = repository.getDefaultFileSystem().findResource(DIR_INSTANCES);
            
            for (FileObject f : directory.getChildren())
                HudsonInstanceImpl.createHudsonInstance(new HudsonInstanceProperties(f));
        }
        
        return instances;
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
}