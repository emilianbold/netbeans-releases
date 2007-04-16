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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.api.HudsonView;
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
    private Collection<HudsonJob> jobs = new ArrayList<HudsonJob>();
    private Collection<HudsonView> views = new ArrayList<HudsonView>();
    private Collection<HudsonChangeListener> listeners = new ArrayList<HudsonChangeListener>();
    
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
                    if (!synchronization.isRunning())
                        synchronization.start();
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
    
    protected void stopAutoSynchronization() {
        if (synchronization.isRunning())
            synchronization.stop();
    }
    
    public HudsonConnector getConnector() {
        return connector;
    }
    
    public HudsonVersion getVersion() {
        return getConnector().getHudsonVersion();
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
    
    public synchronized Collection<HudsonJob> getJobs() {
        return jobs;
    }
    
    public synchronized Collection<HudsonView> getViews() {
        return views;
    }
    
    public synchronized void setViews(Collection<HudsonView> views) {
        this.views = views;
    }
    
    public synchronized void synchronize() {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(HudsonInstanceImpl.class, "MSG_Synchronizing", getName()));
        
        handle.start();
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    // Get actual views
                    Collection<HudsonView> oldViews = getViews();
                    
                    // Retrieve jobs
                    Collection<HudsonJob> retrieved = getConnector().getAllJobs();
                    
                    // Update state
                    fireStateChanges();
                    
                    // Sort retrieved list
                    Collections.sort(Arrays.asList(retrieved.toArray(new HudsonJob[] {})));
                    
                    // When there are no changes return and do not fire changes
                    if (getJobs().equals(retrieved) && oldViews.equals(getViews()))
                        return;
                    
                    // Update jobs
                    jobs = retrieved;
                    
                    // Fire all changes
                    fireContentChanges();
                } finally {
                    handle.finish();
                }
            }
        });
    }
    
    public void addHudsonChangeListener(HudsonChangeListener l) {
        listeners.add(l);
    }
    
    public void removeHudsonChangeListener(HudsonChangeListener l) {
        listeners.remove(l);
    }
    
    protected void fireStateChanges() {
        ArrayList<HudsonChangeListener> tempList;
        
        synchronized (listeners) {
            tempList = new ArrayList<HudsonChangeListener>(listeners);
        }
        
        for (HudsonChangeListener l : tempList) {
            l.stateChanged();
        }
    }
    
    protected void fireContentChanges() {
        ArrayList<HudsonChangeListener> tempList;
        
        synchronized (listeners) {
            tempList = new ArrayList<HudsonChangeListener>(listeners);
        }
        
        for (HudsonChangeListener l : tempList) {
            l.contentChanged();
        }
    }
    
    /**
     *
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof HudsonInstance))
            return false;
        
        final HudsonInstance other = (HudsonInstance) obj;
        
        if (getUrl() != other.getUrl() &&
                (getUrl() == null || !getUrl().equals(other.getUrl())))
            return false;
        return true;
    }
    
    public int compareTo(HudsonInstance o) {
        return getName().compareTo(o.getName());
    }
    
    private class Synchronization implements Runnable {
        
        private boolean runningFlag = false;
        
        public synchronized void start() {
            if (!runningFlag)
                RequestProcessor.getDefault().post(this);
        }
        
        public synchronized void stop() {
            runningFlag = false;
        }
        
        public synchronized boolean isRunning() {
            return runningFlag;
        }
        
        public void run() {
            // Activate
            runningFlag = true;
            long milis = 0;
            
            try {
                do {
                    // Synchronize
                    synchronize();
                    
                    // Refresh wait time
                    String s = getProperties().getProperty(HudsonInstanceProperties.PROP_SYNC);
                    milis = Integer.parseInt(s) * 60 * 1000;
                    
                    // Wait for the specified amount of time
                    Thread.sleep(milis);
                } while (runningFlag && milis > 0);
            } catch (InterruptedException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } finally {
                // Deactivate
                runningFlag = false;
            }
        }
    }
}