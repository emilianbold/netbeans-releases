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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.glassfish.common.wizards;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.glassfish.common.CommonServerSupport;
import org.netbeans.modules.glassfish.common.GlassfishInstanceProvider;

/**
 * @author Ludo
 */
public class AddServerLocationPanel implements WizardDescriptor.Panel, ChangeListener {
    
    private final static String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18   
    
    private ServerWizardIterator wizardIterator;
    private AddServerLocationVisualPanel component;
    private WizardDescriptor wizard;
    private transient Set <ChangeListener>listeners = new HashSet<ChangeListener>(1);

    /**
     * 
     * @param instantiatingIterator 
     */
    public AddServerLocationPanel(ServerWizardIterator wizardIterator){
        this.wizardIterator = wizardIterator;
    }
    
    /**
     * 
     * @param ev 
     */
    public void stateChanged(ChangeEvent ev) {
        fireChangeEvent(ev);
    }
    
    private void fireChangeEvent(ChangeEvent ev) {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    /**
     * 
     * @return 
     */
    public Component getComponent() {
        if (component == null) {
            component = new AddServerLocationVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }
    
    /**
     * 
     * @return 
     */
    public HelpCtx getHelp() {
        // !PW FIXME correct help context
        return new HelpCtx("registering_app_server_hk2_location"); //NOI18N
    }
    
    /**
     * 
     * @return 
     */
    public boolean isValid() {
        AddServerLocationVisualPanel component = (AddServerLocationVisualPanel) getComponent();
        String locationStr = component.getHk2HomeLocation();
        locationStr = (locationStr != null) ? locationStr.trim() : null;
        if(locationStr == null || locationStr.length() ==0) {
            wizard.putProperty(PROP_ERROR_MESSAGE, "Install location cannot be empty.");
            return false;
        }
            
        File installDir = new File(locationStr);
        if(!installDir.exists()) {
            wizard.putProperty(PROP_ERROR_MESSAGE, locationStr + " does not exist.");
            return false;
        } else if(!isValidV3Install(installDir)) {
            wizard.putProperty(PROP_ERROR_MESSAGE, locationStr + " is not a valid V3 installation.");
            return false;
        } else {
            // !PW FIXME derive hostname & port fields properly from domain.xml or wizard
            String uri = "[" + installDir + "]" + CommonServerSupport.URI_PREFIX + ":localhost:8080";
            if(GlassfishInstanceProvider.getDefault().hasServer(uri)) {
                wizard.putProperty(PROP_ERROR_MESSAGE, "The server at " + locationStr + " is already added to the server tab.");
                return false;
            } else {
                String statusText = component.getStatusText();
                if(statusText != null && statusText.length() > 0) {
                    wizard.putProperty(PROP_ERROR_MESSAGE, statusText);
                    return false;
                }
            }
        }
        
        wizard.putProperty(PROP_ERROR_MESSAGE, null);
        wizardIterator.setHk2HomeLocation(locationStr);
        return true;
    }
    
    private boolean isValidV3Install(File installDir) {
        File glassfishRef = new File(installDir, "modules" + File.separator + "glassfish-10.0-SNAPSHOT.jar");
        if(!glassfishRef.exists()) {
            // !PW Older V3 installs (pre 12/01/07) put snapshot jar in lib folder.
            glassfishRef = new File(installDir, "lib" + File.separator + "glassfish-10.0-SNAPSHOT.jar");
            if(!glassfishRef.exists()) {
                return false;
            }
        }
        
        File containerRef = new File(installDir, "config" + File.separator + "glassfish.container");
        if(!containerRef.exists()) {
            return false;
        }
        
        File domainRef = new File(installDir, "domains" + File.separator + "domain1");
        if(!domainRef.exists()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 
     * @param l 
     */
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    /**
     * 
     * @param l 
     */
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    /**
     * 
     * @param settings 
     */
    public void readSettings(Object settings) {
        if (wizard == null)
            wizard = (WizardDescriptor)settings;
    }
    
    /**
     * 
     * @param settings 
     */
    public void storeSettings(Object settings) {
    }
}