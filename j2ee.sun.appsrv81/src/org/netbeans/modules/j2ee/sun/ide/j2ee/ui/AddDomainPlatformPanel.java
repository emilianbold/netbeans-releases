// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
// </editor-fold>

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
import org.netbeans.modules.j2ee.sun.api.Asenv;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.ide.Installer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PlatformValidator;

/** Queries the user for the platform directory associated with the
 * instance they are registering.
 */
class AddDomainPlatformPanel implements WizardDescriptor.FinishablePanel,
        ChangeListener {
    
    private static Profile[] SHORT_PROFILES_LIST = { Profile.DEFAULT };
    private static Profile[] LONG_PROFILES_LIST = { Profile.DEFAULT, Profile.DEVELOPER,
        Profile.CLUSTER, Profile.ENTERPRISE };
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private AddInstanceVisualPlatformPanel component;
    private WizardDescriptor wiz;
    
   /*  Get the visual component for the panel. In this template, the component
     is kept separate. This can be more efficient: if the wizard is created
     but never displayed, or not all panels are displayed, it is better to
     create only those which really need to be visible.
    */
    public Component getComponent() {
        return getAIVPP();
    }
    
    private AddInstanceVisualPlatformPanel getAIVPP() {
        if (component == null) {
            File f = ServerLocationManager.getLatestPlatformLocation();
            File defaultLoc = null;
            if (null == f || !f.exists()) {
                String prop = System.getProperty(ServerLocationManager.INSTALL_ROOT_PROP_NAME);
                if (null != prop && prop.length() > 0) {
                    // there is a possible root directory for the AS
                    File installRoot = new File(prop);
                    if (ServerLocationManager.isGoodAppServerLocation(installRoot)) {
                        defaultLoc = installRoot;
                    }
                }
                if (null == defaultLoc) {
                    defaultLoc = new File(guessDefaultInstall());//NOI18N
                }
            } else {
                defaultLoc = f;
            }
            component = new AddInstanceVisualPlatformPanel(defaultLoc);
            component.addChangeListener(this);
        }
        return component;
    }
    
    private String guessDefaultInstall() {
        if (serverVersion.equals(PlatformValidator.APPSERVERSJS)) {
            // use the sunw name
            if (org.openide.util.Utilities.isWindows()) {
                return "C:\\Sun\\AppServer";    // NOI18N
            } else {
                return System.getProperty("user.home") + File.separator + "SUNWappserver";  // NOI18N
            }
        } else if (serverVersion.equals(PlatformValidator.SAILFIN_V1)) {
            // use sailfin name
            return System.getProperty("user.home") + File.separator + "sailfin";  // NOI18N
        } else {
            // use glassfish name
            return System.getProperty("user.home") + File.separator + "glassfish";  // NOI18N
        }
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
        if (null == wiz) {
            return false;
        }
        wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE, null);
        String instLoc = getAIVPP().getInstallLocation();
        if (instLoc.startsWith("\\\\")) {
            wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AddDomainPlatformPanel.class,
                    "Msg_NoAuthorityComponent"));                               // NOI18N
            retVal = false;
        }
        File location = new File(getAIVPP().getInstallLocation());
        if (retVal && !platformValidator.isGoodAppServerLocation(location)) {
            Object selectedType = getAIVPP().getSelectedType();
            if (selectedType == AddDomainWizardIterator.REMOTE){
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainPlatformPanel.class,
                        "Msg_NeedValidInstallEvenForRemote"));
            }else{
                // not valid install directory
                String errMsg = NbBundle.getMessage(AddDomainPlatformPanel.class,
                        "Msg_InValidInstall");
                if(! serverVersion.equals("")) { //NOI18N
                    String serverType = platformValidator.getServerTypeName(serverVersion);
                    errMsg = NbBundle.getMessage(AddDomainPlatformPanel.class,
                        "Msg_InValidInstallForServerType", serverType);
                }
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        errMsg); 
            }
            getAIVPP().setDomainsList(new Object[0],false);
            getAIVPP().setProfilesList(new Profile[0],false);
            retVal = false;
        } else if (retVal) {
            Object oldPlatformLoc = wiz.getProperty(AddDomainWizardIterator.PLATFORM_LOCATION);
            if (!location.equals(oldPlatformLoc) || getAIVPP().getDomainsListModel().getSize() < 1) {
                Object[] domainsList = getDomainList(Util.getRegisterableDefaultDomains(location),location);
                getAIVPP().setDomainsList(domainsList,true);
                if (ServerLocationManager.getAppServerPlatformVersion(location) !=
                        ServerLocationManager.GF_V2) {
                    getAIVPP().setProfilesList(SHORT_PROFILES_LIST,true);
                } else {
                    getAIVPP().setProfilesList(LONG_PROFILES_LIST,true);
                }
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
            Object selectedType = getAIVPP().getSelectedType();
            if (selectedType == AddDomainWizardIterator.DEFAULT) {
                File[] usableDomains = Util.getRegisterableDefaultDomains(location);
                if (usableDomains.length == 0) {
                    wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                            NbBundle.getMessage(AddDomainPlatformPanel.class,
                            "Msg_NoDefaultDomainsAvailable"));                      //NOI18N
                    retVal = false;
                }
                wiz.putProperty(AddDomainWizardIterator.TYPE, selectedType);
                String dirCandidate = getAIVPP().getDomainDir().toString();
                if (null != dirCandidate) {
                    File domainDir = getAIVPP().getDomainDir(); // new File(dirCandidate);
                    // this should not happen. The previous page of the wizard should
                    // prevent this panel from appearing.
                    String mess = Util.rootOfUsableDomain(domainDir);
                    if (null != mess) {
                        wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                                mess);
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
//                                fis.close();
                                
                                Enumeration e = p.propertyNames() ;
                                while(e.hasMoreElements()) {
                                    String v = (String)e.nextElement();
                                    if ("AS_ADMIN_USER".equals(v))//admin user//NOI18N
                                        username = p.getProperty(v );
                                    else if ("AS_ADMIN_PASSWORD".equals(v)){ // admin password//NOI18N
                                        password = p.getProperty(v );
                                    }
                                }
                                
                            } catch (IOException e){
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
                wiz.putProperty(AddDomainWizardIterator.INSTALL_LOCATION,"");
                wiz.putProperty(AddDomainWizardIterator.DOMAIN,"");
            } else if (selectedType == AddDomainWizardIterator.PERSONAL) {
                wiz.putProperty(AddDomainWizardIterator.TYPE, selectedType);
                wiz.putProperty(AddDomainWizardIterator.INSTALL_LOCATION,"");
                wiz.putProperty(AddDomainWizardIterator.DOMAIN,"");
                Profile p =  getAIVPP().getProfile();
                wiz.putProperty(AddDomainWizardIterator.PROFILE,p);
                if (p == Profile.ENTERPRISE) {
                    Asenv asenv = new Asenv(location);
                    String nss = asenv.get(Asenv.AS_NS_BIN);
                    String hadb = asenv.get(Asenv.AS_HADB);
                    File nssDir = new File(nss);
                    File hadbDir = new File(hadb);
                    if (nssDir.exists() && nssDir.isDirectory() && hadbDir.exists() 
                            && hadbDir.isDirectory()) {
                        retVal = true;
                    } else {
                        wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                                NbBundle.getMessage(AddDomainPlatformPanel.class,
                                "Msg_UnsupportedProfile"));                                    //NOI18N   
                        retVal = false;
                    }
                }
            } else {
                wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AddDomainPlatformPanel.class,
                        "Msg_UnsupportedType"));                                    //NOI18N
                retVal = false;
            }
        }
        return retVal;
    }
    
    private Object[] getDomainList(File[] dirs, File location){
        return getServerList(dirs,location);
    }
    
    private Object[] getServerList(File[] dirs,File location){
        java.util.List xmlList = new java.util.ArrayList();
        Object retVal[];
        for(int i=0; location != null && i<dirs.length; i++){
            String hostPort = Util.getHostPort(dirs[i],location);
            if(hostPort != null) {
                xmlList.add(new DomainListEntry(hostPort,dirs[i],location));                    
            }
        }//for
        retVal = xmlList.toArray();
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
        if(! serverVersion.equals("")) { //NOI18N
            platformValidator = new PlatformValidator() {
                @Override
                public boolean isGoodAppServerLocation(File loc) {
                    return super.isDescriminatorPresent(loc, serverVersion);
                }
            };
        }
    }

    public void storeSettings(Object settings) {
        // TODO implement?
    }
    
    public void stateChanged(ChangeEvent e) {
//        System.out.println("PP stateChanged");
        if (null != wiz) {
            wiz.putProperty(AddDomainWizardIterator.TYPE, getAIVPP().getSelectedType());
            fireChangeEvent(); //e);
        }
    }
    
    public boolean isFinishPanel() {
        Object selectedType = getAIVPP().getSelectedType();
        return selectedType == AddDomainWizardIterator.DEFAULT;
    }
    
    private PlatformValidator platformValidator;
    private String serverVersion = "";
    
    public void setPlatformValidator(PlatformValidator pv) {
        platformValidator = pv;
    }
    
    public void setPlatformValidator(PlatformValidator pv, String sversion) {
        platformValidator = pv;
        serverVersion = sversion;
    }
    
}

