/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.awt.Component;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PlatformValidator;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** A single panel descriptor for a wizard.
 * You probably want to make a wizard iterator to hold it.
 *
 * @author vkraemer
 */
public class AddDomainPortsDefPanel implements WizardDescriptor.Panel,
        ChangeListener {
    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private CreateServerVisualPanel component;
    private WizardDescriptor wiz;
    private PlatformValidator pv;
    private String serverVersion;
//    private TargetServerData targetData;
    
    /** Create the wizard panel descriptor. */
    public AddDomainPortsDefPanel() { //TargetServerData data) {
//        targetData = data;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new CreateServerVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("AS_RegServ_DefinePorts"); //NOI18N
    }
    
    public boolean isValid() {
        // make sure the values are unique
        Set portsUsed = new HashSet(7);
        portsUsed.add(component.getAdminPort());
        
        if (isPortReused(portsUsed, component.getAdminJmxPort(), "ERR_AdminJmxPort"))   // NOI18N
            return false;
        
        if (isPortReused(portsUsed, component.getInstanceHttpPort(), "ERR_InstancePort"))   // NOI18N
            return false;
        
        if (isPortReused(portsUsed, component.getJmsPort(), "ERR_JmsPort"))     // NOI18N
            return false;
        
        if (isPortReused(portsUsed,component.getOrbPort(), "ERR_OrbListenerPort"))  // NOI18N
            return false;
        
        if (isPortReused(portsUsed,component.getHttpSslPort(), "ERR_HttpSslPort"))  // NOI18N
            return false;
        
        if (isPortReused(portsUsed,component.getOrbSslPort(), "ERR_OrbSslPort"))    // NOI18N
            return false;
        if (isPortReused(portsUsed,component.getOrbMutualAuthPort(), "ERR_OrbMutualAutPort"))   // NOI18N
            return false;
        
        if (PlatformValidator.SAILFIN_V1.equals(serverVersion)) {        
            if (isPortReused(portsUsed,component.getSipPort(), "ERR_SipPort"))  // NOI18N
                return false;

            if (isPortReused(portsUsed,component.getSipSslPort(), "ERR_SipSslPort"))    // NOI18N
                return false;
        }

        wiz.putProperty(AddDomainWizardIterator.ADMIN_JMX_PORT,
                component.getAdminJmxPort().toString());
        wiz.putProperty(AddDomainWizardIterator.HTTP_SSL_PORT,
                component.getHttpSslPort().toString());
        wiz.putProperty(AddDomainWizardIterator.INSTANCE_PORT,
                component.getInstanceHttpPort().toString());
        wiz.putProperty(AddDomainWizardIterator.JMS_PORT,
                component.getJmsPort().toString());
        wiz.putProperty(AddDomainWizardIterator.ORB_LISTENER_PORT,
                component.getOrbPort().toString());
        wiz.putProperty(AddDomainWizardIterator.ORB_SSL_PORT,
                component.getOrbSslPort().toString());
        wiz.putProperty(AddDomainWizardIterator.ORB_MUTUAL_AUTH_PORT,
                component.getOrbMutualAuthPort().toString());
        wiz.putProperty(AddDomainWizardIterator.PORT,
                component.getAdminPort().toString());
        
        // these values are ignored for non-sailfin platforms...
        wiz.putProperty(AddDomainWizardIterator.SIP_PORT,
                component.getSipPort().toString());
        wiz.putProperty(AddDomainWizardIterator.SIP_SSL_PORT,
                component.getSipSslPort().toString());
        
        wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,null);
        wiz.putProperty(AddDomainWizardIterator.HOST,"localhost");            //NOI18N
        return true;
    }

    void setPlatformValidator(PlatformValidator pv, String serverVersion) {
        this.pv = pv;
        this.serverVersion = serverVersion;
    }

    private boolean isPortReused(Set portsUsed, Object newVal, String id) {
        if (portsUsed.contains(newVal)) {
            // create the error message
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AddDomainPortsDefPanel.class, id));
            return true;
        } else {
            portsUsed.add(newVal);
            return false;
        }

    }
 
    private final Set listeners = new HashSet(1);
    
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }
    
    // You can use a settings object to keep track of state.
    // Normally the settings object will be the WizardDescriptor,
    // so you can use WizardDescriptor.getProperty & putProperty
    // to store information entered by the user.
    public void readSettings(Object settings) {
        wiz = (WizardDescriptor) settings;
        component.includeSip(PlatformValidator.SAILFIN_V1.equals(serverVersion));
    }
    
    public void storeSettings(Object settings) {
    }

    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
    
}
