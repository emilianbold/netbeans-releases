/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.hudson.ui.wizard;

import java.awt.Component;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.impl.HudsonConnector;
import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.netbeans.modules.hudson.impl.HudsonVersionImpl;
import org.netbeans.modules.hudson.util.Utilities;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Hudson wizard instance properties panel
 * 
 * @author Michal Mocnak
 */
public class InstancePropertiesPanel implements WizardDescriptor.Panel<InstanceWizard>, ChangeListener {
    
    private final static String HTTP_PREFIX = "http://";
    private final static String HTTPS_PREFIX = "https://";
    
    private InstancePropertiesVisual component;
    
    private InstanceWizard wizard;
    
    private final ChangeSupport cs = new ChangeSupport(this);
    
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
    
    public void readSettings(InstanceWizard wiz) {
        wizard = wiz;
    }
    
    public void storeSettings(InstanceWizard wiz) {}
    
    public synchronized boolean isValid() {
        String name = getInstancePropertiesVisual().getDisplayName();
        String url = getInstancePropertiesVisual().getUrl();
        String sync = getInstancePropertiesVisual().isSync() ? getInstancePropertiesVisual().getSyncTime() : "0";
        
        if (name.length() == 0) {
            wizard.setErrorMessage(NbBundle.getMessage(InstancePropertiesPanel.class, "MSG_EmptyName"));
            return false;
        }
        
        if (HudsonManagerImpl.getInstance().getInstanceByName(name) != null) {
            wizard.setErrorMessage(NbBundle.getMessage(InstancePropertiesPanel.class, "MSG_ExistName"));
            return false;
        }
        
        if (url.length() == 0) {
            wizard.setErrorMessage(NbBundle.getMessage(InstancePropertiesPanel.class, "MSG_EmptyUrl"));
            return false;
        }
        
        if (!(url.startsWith(HTTP_PREFIX) || url.startsWith(HTTPS_PREFIX)))
            url = HTTP_PREFIX + url;
        
        if (checkingFlag == true) {
            wizard.setErrorMessage(NbBundle.getMessage(InstancePropertiesPanel.class, "MSG_Checking"));
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
                    wizard.setErrorMessage(NbBundle.getMessage(InstancePropertiesPanel.class, "MSG_Checking"));
                    
                    try {
                        URLConnection connection = HudsonConnector.followRedirects(new URL(checkingUrl).openConnection());
                        
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
                    } catch (IllegalArgumentException e) { // JRE #6797318
                        return;
                    } finally {
                        // Set checking progress to stopped
                        checkingFlag = false;
                        getInstancePropertiesVisual().setChecking(false);
                        
                        // Stop the progress handle
                        handle.finish();
                        
                        // Fire changes
                        cs.fireChange();
                    }
                    
                    // Everything is alright
                    checkingState = true;
                }
            });
            
            return false;
        }
        
        if (checkingState == false) {
            wizard.setErrorMessage(NbBundle.getMessage(InstancePropertiesPanel.class, "MSG_WrongVersion", HudsonVersion.SUPPORTED_VERSION));
            return false;
        }
        
        if (HudsonManagerImpl.getDefault().getInstance(url) != null) {
            wizard.setErrorMessage(NbBundle.getMessage(InstancePropertiesPanel.class, "MSG_ExistUrl"));
            return false;
        }
        
        wizard.setErrorMessage(null);
        wizard.name = name;
        wizard.url = url;
        wizard.sync = sync;
        
        return true;
    }
    
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    public void stateChanged(ChangeEvent arg0) {
        cs.fireChange();
    }
    
    private InstancePropertiesVisual getInstancePropertiesVisual() {
        return (InstancePropertiesVisual) getComponent();
    }
}