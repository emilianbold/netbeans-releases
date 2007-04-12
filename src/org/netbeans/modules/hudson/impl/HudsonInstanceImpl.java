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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.ui.nodes.OpenableInBrowser;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Implementation of the HudsonInstacne
 *
 * @author Michal Mocnak
 */
public class HudsonInstanceImpl implements HudsonInstance, OpenableInBrowser {
    
    private HudsonInstanceProperties properties;
    private HudsonConnector connector;
    
    private Synchronization synchronization = new Synchronization();
    private List<HudsonJob> jobs = new ArrayList<HudsonJob>();
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    private HudsonInstanceImpl(String name, String url) {
        this(new HudsonInstanceProperties(name, url));
    }
    
    private HudsonInstanceImpl(HudsonInstanceProperties properties) {
        this.properties = properties;
        this.connector = new HudsonConnector(this);
        
        // Start synchronization
        synchronization.start();
        
        // Add property listener
        this.properties.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(HudsonInstanceProperties.PROP_SYNC))
                    synchronization.start();
                
                fireChangeListeners();
            }
        });
    }
    
    /**
     *
     * @param name
     * @param url
     * @return
     */
    public static HudsonInstanceImpl createHudsonInstance(String name, String url) {
        return createHudsonInstance(new HudsonInstanceProperties(name, url));
    }
    
    /**
     *
     * @param name
     * @param url
     * @return
     */
    public static HudsonInstanceImpl createHudsonInstance(String name, String url, String sync) {
        return createHudsonInstance(new HudsonInstanceProperties(name, url, sync));
    }
    
    /**
     *
     * @param name
     * @param url
     * @return
     */
    public static HudsonInstanceImpl createHudsonInstance(HudsonInstanceProperties properties) {
        HudsonInstanceImpl instance = new HudsonInstanceImpl(properties);
        
        if (null == HudsonManagerImpl.getDefault().addInstance(instance))
            return null;
        
        return instance;
    }
    
    public HudsonConnector getConnector() {
        return connector;
    }
    
    public boolean isConnected() {
        return getConnector().isConnected();
    }
    
    public HudsonInstanceProperties getProperties() {
        return properties;
    }
    
    public String getName() {
        return getProperties().getProperty(HudsonInstanceProperties.PROP_NAME);
    }
    
    public String getUrl() {
        return getProperties().getProperty(HudsonInstanceProperties.PROP_URL);
    }
    
    public Collection<HudsonJob> getJobs() {
        if (null == jobs || jobs.size() == 0)
            synchronize();
        
        return jobs;
    }
    
    public synchronized void synchronize() {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(HudsonInstanceImpl.class, "MSG_Synchronizing", getName()));
        
        handle.start();
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {                    
                    List<HudsonJob> retrieved = getConnector().getAllJobs();
                    
                    if (jobs.equals(retrieved))
                        return;
                    
                    jobs = retrieved;
                    
                    fireChangeListeners();
                } finally {
                    handle.finish();
                }
            }
        });
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
    
    /**
     *
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final HudsonInstanceImpl other = (HudsonInstanceImpl) obj;
        
        if (getUrl() != other.getUrl() &&
                (getUrl() == null || !getUrl().equals(other.getUrl())))
            return false;
        return true;
    }
    
    /**
     *
     * @return
     */
    public int hashCode() {
        int hash = 7;
        
        hash = 79 * hash + (getUrl() != null ? getUrl().hashCode() : 0);
        return hash;
    }
    
    public int compareTo(HudsonInstance o) {
        return getName().compareTo(o.getName());
    }
    
    private class Synchronization implements Runnable {
        
        private boolean active = false;
        
        public synchronized void start() {
            if (!active)
                RequestProcessor.getDefault().post(this);
        }
        
        public void run() {
            // Activate
            active = true;
            long milis = 0;
            
            try {
                do {
                    // Synchronize
                    synchronize();
                    
                    // Refresh wait time
                    milis = Integer.parseInt(getProperties().getProperty(HudsonInstanceProperties.PROP_SYNC)) * 60 * 1000;
                    
                    // Wait for the specified amount of time
                    Thread.sleep(milis);
                } while (milis > 0);
            } catch (InterruptedException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } finally {
                // Deactivate
                active = false;
            }
        }
    }
}