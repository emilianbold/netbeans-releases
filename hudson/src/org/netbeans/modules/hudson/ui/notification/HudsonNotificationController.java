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

package org.netbeans.modules.hudson.ui.notification;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.openide.util.ChangeSupport;

/**
 * Hudson notification controller
 *
 * @author Michal Mocnak
 */
public class HudsonNotificationController implements ChangeListener {
    
    private static HudsonNotificationController instance;
    
    private Component component;
    private ChangeSupport change = new ChangeSupport(this);
    private Map<String, HudsonJob> jobs = new HashMap<String, HudsonJob>();
    
    private HudsonNotificationController() {
        change.addChangeListener(this);
    }
    
    protected synchronized Component getVisualComponent() {
        if (null == component) {
            component = new HudsonNotificationPanel();
            change.addChangeListener((HudsonNotificationPanel) component);
        }
        
        return component;
    }
    
    public synchronized static HudsonNotificationController getDefault() {
        if (null == instance)
            instance = new HudsonNotificationController();
        
        return instance;
    }
    
    public synchronized Collection<HudsonJob> getFailedJobs() {
        return new ArrayList<HudsonJob>(jobs.values());
    }
    
    public synchronized void notify(HudsonJob job) {
        // put job into notification
        jobs.put(job.getUrl(), job);
        
        // fire changes
        change.fireChange();
    }
    
    public synchronized void notify(Collection<HudsonJob> jobs) {
        // put jobs into notification
        for (HudsonJob job : jobs)
            this.jobs.put(job.getUrl(), job);
        
        // fire changes
        change.fireChange();
    }
    
    public synchronized void stateChanged(ChangeEvent e) {
        Iterator<HudsonJob> jobsIt = jobs.values().iterator();
        while (jobsIt.hasNext()) {
            HudsonJob job = jobsIt.next();
            HudsonInstance hudsonInstance = job.getLookup().lookup(HudsonInstance.class);
            
            if (null == hudsonInstance)
                continue;
            
            boolean exists = false;
            boolean passed = false;
            Collection<HudsonJob> instjobs = hudsonInstance.getPreferredJobs();
            if (instjobs == null || instjobs.size() == 0) {
                instjobs = hudsonInstance.getJobs();
            }
            
            for (HudsonJob j : instjobs) {
                if (j.getUrl().equals(job.getUrl())) {
                    exists = true;
                    
                    if (!j.getColor().equals(Color.red) && !j.getColor().equals(Color.red_anime))
                        passed = true;
                    
                    break;
                }
            }
            
            if (!exists || passed) {
                jobsIt.remove();
            }
        }
    }
}