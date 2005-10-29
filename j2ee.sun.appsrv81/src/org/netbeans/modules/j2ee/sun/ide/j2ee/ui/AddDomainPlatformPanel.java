/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Queries the user for the platform directory associated with the
 * instance they are registering.
 */
class AddDomainPlatformPanel implements WizardDescriptor.Panel, 
        ChangeListener {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private AddInstanceVisualPlatformPanel component;
    private WizardDescriptor wiz;
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            File f = ServerLocationManager.getLatestPlatformLocation();
            File defaultLoc = new File(System.getProperty("user.home"));//NOI18N
            if (f!=null && f.exists())
                defaultLoc = f;
            component = new AddInstanceVisualPlatformPanel(defaultLoc);
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        // TODO get help content?
        return HelpCtx.DEFAULT_HELP; // new HelpCtx(SampleWizardPanel1.class);
    }
    
    /** Determine if the input is valid.
     *
     * Is the user entered directory an app server install directory?
     *
     * If the install directory is a GlassFish install, is the IDE running in 
     * a J2SE 5.0 VM?
     *
     * If the user attempts to register a default instance, is there a usable one?
     *   See Util.getRegisterableDefaultDomains(File)
     *
     * Is the user asking for an unsupported instance type?
     */
    public boolean isValid() {
        boolean retVal = true;
        File location = new File(component.getInstallLocation());
        wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE, null);
        Object selectedType = component.getSelectedType();
        if (selectedType == AddDomainWizardIterator.DEFAULT) {
            File[] usableDomains = Util.getRegisterableDefaultDomains(location);
            if (usableDomains.length == 0) {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE, 
                        NbBundle.getMessage(AddDomainPlatformPanel.class, 
                        "Msg_NoDefaultDomainsAvailable"));                      //NOI18N
                retVal = false;
            }
            wiz.putProperty(AddDomainWizardIterator.TYPE, selectedType);
        } else if (selectedType == AddDomainWizardIterator.REMOTE) {
            wiz.putProperty(AddDomainWizardIterator.TYPE, selectedType);
            wiz.putProperty(AddDomainWizardIterator.INSTALL_LOCATION,"");
            wiz.putProperty(AddDomainWizardIterator.DOMAIN,"");
        } else if (selectedType == AddDomainWizardIterator.LOCAL) {
            wiz.putProperty(AddDomainWizardIterator.TYPE, selectedType);
        } else if (selectedType == AddDomainWizardIterator.PERSONAL) {
            wiz.putProperty(AddDomainWizardIterator.TYPE, selectedType);
        } else {
            wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE, 
                    NbBundle.getMessage(AddDomainPlatformPanel.class, 
                    "Msg_UnsupportedType"));                                    //NOI18N
            retVal = false;
        }
        if (!ServerLocationManager.isGoodAppServerLocation(location)) {
            // not valid install directory
            wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE, 
                    NbBundle.getMessage(AddDomainPlatformPanel.class, 
                    "Msg_InValidInstall"));                                     // NOI18N
            retVal = false;
        } else {
            if (ServerLocationManager.isGlassFish(location)) {
                String javaClassVersion = 
                        System.getProperty("java.class.version");               // NOI18N
                double jcv = Double.parseDouble(javaClassVersion);
                if (jcv < 49.0) {
                    // prevent ClassVersionUnsupportedError....
                    wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE, 
                            NbBundle.getMessage(AddDomainPlatformPanel.class, 
                            "Msg_RequireJ2SE5"));                               // NOI18N
                    retVal = false;
                }
            } 
            wiz.putProperty(AddDomainWizardIterator.PLATFORM_LOCATION,location);
        }
        return retVal;
    }
    
    // Event Handling
    private final Set/*<ChangeListener>*/ listeners = new HashSet/*<ChangeListener>*/(1);
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
        Iterator/*<ChangeListener>*/ it;
        synchronized (listeners) {
            it = new HashSet/*<ChangeListener>*/(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
//        System.out.println("PP fireChangeEvent on");
        while (it.hasNext()) {
            ChangeListener l = (ChangeListener) it.next();
//            System.out.println("    "+l);
            l.stateChanged(ev);
        }
    }
    

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        wiz = (WizardDescriptor) settings;
    }
    public void storeSettings(Object settings) {
        // TODO implement?
    }

    public void stateChanged(ChangeEvent e) {
//        System.out.println("PP stateChanged");
        wiz.putProperty(AddDomainWizardIterator.TYPE, component.getSelectedType());
        fireChangeEvent(); //e);
    }    
}

