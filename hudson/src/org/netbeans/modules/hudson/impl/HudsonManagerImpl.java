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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.spi.ProjectHudsonProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the HudsonManager
 *
 * @author Michal Mocnak
 */
@ServiceProvider(service=HudsonManager.class)
public class HudsonManagerImpl extends HudsonManager {
    
    /** Init lock */
    private static final Object LOCK_INIT = new Object();
    
    /** The only instance of the hudson manager implementation in the system */
    private static HudsonManagerImpl defaultInstance;
    
    private Map<String, HudsonInstanceImpl> instances;
    private final List<HudsonChangeListener> listeners = new ArrayList<HudsonChangeListener>();
    private PropertyChangeListener projectsListener;
    private Map<Project, HudsonInstanceImpl> projectInstances = new HashMap<Project, HudsonInstanceImpl>();
    
    public HudsonManagerImpl() {
        synchronized(LOCK_INIT) {
            // a static object to synchronize on
            if (null != defaultInstance)
                throw new IllegalStateException("Instance already exists"); // NOI18N
            
            defaultInstance = this;
        }
        projectsListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            checkOpenProjects();
                        }
                    });
                }
            }

        };
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
        storeInstanceDefinition(instance);
        
        // Add property change listener
        instance.getProperties().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                storeInstanceDefinition(instance);
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
        removeInstanceDefinition(instance);
        
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
        OpenProjects.getDefault().removePropertyChangeListener(projectsListener);
        projectInstances.clear();
        // Terminate instances
        for (HudsonInstance instance : getInstances())
            ((HudsonInstanceImpl) instance).terminate();
    }

    private Preferences instancePrefs() {
        return NbPreferences.forModule(HudsonManagerImpl.class).node("instances"); // NOI18N
    }
    
    private void storeInstanceDefinition(HudsonInstanceImpl instance) {
        if (!instance.isPersisted()) {
            return;
        }
        Preferences node = instancePrefs().node(keyName(instance.getName()));
        for (Map.Entry<String,String> entry : instance.getProperties().entrySet()) {
            node.put(entry.getKey(), entry.getValue());
        }
    }

    private String keyName(String name) {
        // http://deadlock.netbeans.org/hudson/ => deadlock.netbeans.org_hudson
        return name.replaceFirst("http://", "").replaceFirst("/$", "").replace('/', '_');
    }
    
    private void removeInstanceDefinition(HudsonInstanceImpl instance) {
        try {
            instancePrefs().node(keyName(instance.getName())).removeNode();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
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

    private boolean starting;
    /** True if manager is starting up at the moment. */
    public boolean isStarting() {return starting;}

    private void init() {
        starting = true;
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    try {
                        for (String kid : instancePrefs().childrenNames()) {
                            Preferences node = instancePrefs().node(kid);
                            Map<String, String> m = new HashMap<String, String>();
                            for (String k : node.keys()) {
                                m.put(k, node.get(k, null));
                            }
                            HudsonInstanceImpl.createHudsonInstance(new HudsonInstanceProperties(m));
                        }
                    } catch (BackingStoreException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } finally {
                    starting = false;
                    checkOpenProjects();
                    OpenProjects.getDefault().addPropertyChangeListener(projectsListener);
                    // Fire changes
                    fireChangeListeners();
                }
            }
        });
    }


    private void checkOpenProjects() {
        try {
            Future<Project[]> fut = OpenProjects.getDefault().openProjects();
            Project[] prjs = fut.get();
            for (Project project : prjs) {
                boolean exists = false;
                if (projectInstances.containsKey(project)) {
                    exists = true;
                }
                ProjectHudsonProvider prov = project.getLookup().lookup(ProjectHudsonProvider.class);
                if (prov != null && !exists) {
                    String url = prov.getServerUrl();
                    HudsonInstance in = getInstance(url);
                    if (in != null && !in.isPersisted()) {
                        ProjectHIP props = (ProjectHIP)((HudsonInstanceImpl)in).getProperties();
                        props.addProvider(project);
                        projectInstances.put(project, (HudsonInstanceImpl)in);
                    } else if (in == null) {
                        ProjectHIP props = new ProjectHIP();
                        props.addProvider(project);
                        addInstance(HudsonInstanceImpl.createHudsonInstance(props));
                        HudsonInstanceImpl impl = (HudsonInstanceImpl) getInstance(props.get(HudsonInstanceProperties.HUDSON_INSTANCE_URL));
                        projectInstances.put(project, impl);
                    }
                } else if (prov == null && exists) {
                    HudsonInstanceImpl remove = projectInstances.remove(project);
                    if (remove != null && !remove.isPersisted()) {
                        ProjectHIP props = (ProjectHIP)remove.getProperties();
                        props.removeProvider(project);
                        if (props.getProviders().isEmpty()) {
                            removeInstance(remove);
                        }
                    }
                }
            }
            ArrayList<Project> newprjs = new ArrayList<Project>(projectInstances.keySet());
            newprjs.removeAll(Arrays.asList(prjs));
            for (Project project : newprjs) {
                HudsonInstanceImpl remove = projectInstances.remove(project);
                if (remove != null && !remove.isPersisted()) {
                    ProjectHIP props = (ProjectHIP)remove.getProperties();
                    props.removeProvider(project);
                    if (props.getProviders().isEmpty()) {
                        removeInstance(remove);
                    }
                }
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
