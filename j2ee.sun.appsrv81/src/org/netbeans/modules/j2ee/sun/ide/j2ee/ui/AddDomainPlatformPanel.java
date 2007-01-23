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
package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Queries the user for the platform directory associated with the
 * instance they are registering.
 */
class AddDomainPlatformPanel implements WizardDescriptor.FinishablePanel,
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
            if (f!=null && f.exists()) {
                defaultLoc = f;
            } 
            component = new AddInstanceVisualPlatformPanel(defaultLoc);
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("AS_RegServ_EnterPlatformDir"); //NOI18N
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
        wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE, null);
        String instLoc = component.getInstallLocation();
        if (instLoc.startsWith("\\\\")) {
            wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AddDomainPlatformPanel.class,
                    "Msg_NoAuthorityComponent"));                               // NOI18N
            retVal = false;
        }
        File location = new File(component.getInstallLocation());
        if (retVal && !ServerLocationManager.isGoodAppServerLocation(location)) {
            Object selectedType = component.getSelectedType();
            if (selectedType == AddDomainWizardIterator.REMOTE){
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainPlatformPanel.class,
                        "Msg_NeedValidInstallEvenForRemote"));
            }else{
                // not valid install directory
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainPlatformPanel.class,
                        "Msg_InValidInstall")); // NOI18N
            }
            component.setDomainsList(new Object[0]);
            retVal = false;
        } else if (retVal) {
            Object oldPlatformLoc = wiz.getProperty(AddDomainWizardIterator.PLATFORM_LOCATION);
            if (!location.equals(oldPlatformLoc) || component.getDomainsListModel().getSize() < 1) {
                Object[] domainsList = getDomainList(Util.getRegisterableDefaultDomains(location));
                component.setDomainsList(domainsList);
            }
            //component.setDomainsList();
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
        }
        if (retVal) {
            wiz.putProperty(AddDomainWizardIterator.PLATFORM_LOCATION,location);
            wiz.putProperty(AddDomainWizardIterator.USER_NAME,
                    AddDomainWizardIterator.BLANK);
            wiz.putProperty(AddDomainWizardIterator.PASSWORD,
                    AddDomainWizardIterator.BLANK);
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
                String dirCandidate = component.getDomainDir();
                if (null != dirCandidate) {
                    File domainDir = new File(dirCandidate);
                    // this should not happen. The previous page of the wizard should
                    // prevent this panel from appearing.
                    if (!Util.rootOfUsableDomain(domainDir)) {
                        wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                                NbBundle.getMessage(AddDomainPlatformPanel.class,
                                "Msg_InValidDomainDir",                                     //NOI18N
                                component.getDomainDir()));
                        retVal = false;
                    } else {
                        //File platformDir = (File) wiz.getProperty(AddDomainWizardIterator.PLATFORM_LOCATION);
                        Util.fillDescriptorFromDomainXml(wiz, domainDir);
                        // fill in the admin name and password from the asadminprefs file
                        String username = "admin";
                        String password = null;
                        File f = new File(System.getProperty("user.home")+"/.asadminprefs"); //NOI18N
                        if (f.exists()){
                            FileInputStream fis = null;
                            try{
                                
                                fis = new FileInputStream(f);
                                Properties p = new Properties();
                                p.load(fis);
                                fis.close();
                                
                                Enumeration e = p.propertyNames() ;
                                for ( ; e.hasMoreElements() ;) {
                                    String v = (String)e.nextElement();
                                    if (v.equals("AS_ADMIN_USER"))//admin user//NOI18N
                                        username = p.getProperty(v );
                                    else if (v.equals("AS_ADMIN_PASSWORD")){ // admin password//NOI18N
                                        password = p.getProperty(v );
                                    }
                                }
                                
                            } catch (Exception e){
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                        e);
                            } finally {
                                if (null != fis) {
                                    try {
                                        fis.close();
                                    } catch (IOException ex) {
                                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                                ex);
                                    }
                                }
                            }
                        }
                        wiz.putProperty(AddDomainWizardIterator.PASSWORD, password);
                        wiz.putProperty(AddDomainWizardIterator.USER_NAME,username);
                    }
                } else {
                    wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                            NbBundle.getMessage(AddDomainPlatformPanel.class,
                            "Msg_NoDefaultDomainsAvailable"));                      //NOI18N
                    retVal = false;
                }
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
        }
        return retVal;
    }
    
    private Object[] getDomainList(File[] dirs){
        return getServerList(dirs);
    }
    
    private Object[] getServerList(File[] dirs){
        java.util.List xmlList = new java.util.ArrayList();
        Object retVal[] = null;
        File platformDir = (File) wiz.getProperty(AddDomainWizardIterator.PLATFORM_LOCATION);
        for(int i=0; platformDir != null && i<dirs.length; i++){
            String hostPort = Util.getHostPort(dirs[i],platformDir);
            if(hostPort != null) {
                xmlList.add(
                        NbBundle.getMessage(AddDomainPlatformPanel.class,
                        "LBL_domainListEntry", new Object[] {hostPort,dirs[i].toString()}));
            }
        }//for
        if(xmlList != null) {
            retVal = xmlList.toArray();
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
    
    public boolean isFinishPanel() {
        Object selectedType = component.getSelectedType();
        return selectedType == AddDomainWizardIterator.DEFAULT;
    }
}

