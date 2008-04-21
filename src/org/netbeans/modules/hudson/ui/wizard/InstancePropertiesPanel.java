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

package org.netbeans.modules.hudson.ui.wizard;

import java.awt.Component;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.netbeans.modules.hudson.impl.HudsonVersionImpl;
import org.netbeans.modules.hudson.util.Utilities;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Hudson wizard instance properties panel
 * 
 * @author Michal Mocnak
 */
public class InstancePropertiesPanel implements WizardDescriptor.Panel, InstanceWizardConstants, ChangeListener {
    
    private final static String HTTP_PREFIX = "http://";
    private final static String HTTPS_PREFIX = "https://";
    
    private InstancePropertiesVisual component;
    
    private WizardDescriptor wizard;
    
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    private boolean checkingFlag = false;
    private boolean checkingState = false;
    private String checkingUrl = null;
    
    public Component getComponent() {
        if (component == null) {
            component = new InstancePropertiesVisual();
            component.addChangeListener(this);
        }
        
        return component;
    }
    
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public void readSettings(Object o) {
        if (o instanceof WizardDescriptor)
            wizard = (WizardDescriptor) o;
    }
    
    public void storeSettings(Object o) {}
    
    public synchronized boolean isValid() {
        String name = getInstancePropertiesVisual().getDisplayName();
        String url = getInstancePropertiesVisual().getUrl();
        String sync = getInstancePropertiesVisual().isSync() ? getInstancePropertiesVisual().getSyncTime() : "0";
        
        if (name.length() == 0) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(InstancePropertiesPanel.class,
                    "MSG_EmptyName"));
            return false;
        }
        
        if (HudsonManagerImpl.getInstance().getInstanceByName(name) != null) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(InstancePropertiesPanel.class,
                    "MSG_ExistName"));
            return false;
        }
        
        if (url.length() == 0) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(InstancePropertiesPanel.class,
                    "MSG_EmptyUrl"));
            return false;
        }
        
        if (!(url.startsWith(HTTP_PREFIX) || url.startsWith(HTTPS_PREFIX)))
            url = HTTP_PREFIX + url;
        
        if (checkingFlag == true) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(InstancePropertiesPanel.class,
                    "MSG_Checking"));
            return false;
        }
        
        if (!url.equals(checkingUrl)) {
            getInstancePropertiesVisual().setChecking(true);
            
            final ProgressHandle handle = ProgressHandleFactory.createHandle(
                    NbBundle.getMessage(InstancePropertiesPanel.class, "MSG_Checking"));
            
            checkingUrl = url;
            
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    // Start the progress handle
                    handle.start();
                    
                    // Set deafault values into flags
                    checkingFlag = true;
                    checkingState = false;
                    
                    // Set wizard tool tip text
                    wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(InstancePropertiesPanel.class,
                            "MSG_Checking"));
                    
                    try {
                        URLConnection connection = new URL(checkingUrl).openConnection();
                        
                        // Resolve Hudson version
                        String sVersion = connection.getHeaderField("X-Hudson");
                        if (sVersion == null) {
                            return;
                        }
                        
                        // Create a HudsonVersion object
                        HudsonVersion version = new HudsonVersionImpl(sVersion);
                        
                        if (!Utilities.isSupportedVersion(version))
                            return;
                    } catch (MalformedURLException e) {
                        return;
                    } catch (IOException e) {
                        return;
                    } finally {
                        // Set checking progress to stopped
                        checkingFlag = false;
                        getInstancePropertiesVisual().setChecking(false);
                        
                        // Stop the progress handle
                        handle.finish();
                        
                        // Fire changes
                        fireChangeEvent();
                    }
                    
                    // Everything is alright
                    checkingState = true;
                }
            });
            
            return false;
        }
        
        if (checkingState == false) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(InstancePropertiesPanel.class,
                    "MSG_WrongVersion", HudsonVersion.SUPPORTED_VERSION));
            return false;
        }
        
        if (HudsonManagerImpl.getDefault().getInstance(url) != null) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(InstancePropertiesPanel.class,
                    "MSG_ExistUrl"));
            return false;
        }
        
        wizard.putProperty(PROP_ERROR_MESSAGE, null);
        wizard.putProperty(PROP_DISPLAY_NAME, name);
        wizard.putProperty(PROP_URL, url);
        wizard.putProperty(PROP_SYNC, sync);
        
        return true;
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private void fireChangeEvent() {
        ArrayList<ChangeListener> tempList;
        
        synchronized (listeners) {
            tempList = new ArrayList<ChangeListener>(listeners);
        }
        
        ChangeEvent event = new ChangeEvent(this);
        
        for (ChangeListener l : tempList) {
            l.stateChanged(event);
        }
    }
    
    public void stateChanged(ChangeEvent arg0) {
        fireChangeEvent();
    }
    
    private InstancePropertiesVisual getInstancePropertiesVisual() {
        return (InstancePropertiesVisual) getComponent();
    }
}