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

package org.netbeans.installer.cluster;

import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.WizardPanel;
import com.installshield.wizard.platform.win32.Win32RegistryService;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.WizardServicesUI;
import com.installshield.wizard.service.file.FileService;

import java.io.File;
import java.io.IOException;

import org.netbeans.installer.Util;

public class NetBeansDirChooserPanel extends WizardPanel {
    private String nbdir = "";
    
    void setNbDir(String nbdir) {
        this.nbdir = nbdir;
    }
    
    String getNbDir() {
        return nbdir;
    }
    
    public boolean queryEnter(WizardBeanEvent event) {
        setDescription(resolveString("$L(org.netbeans.installer.cluster.Bundle, NetBeansDirChooser.desc)"));
        
        if (Util.isWindowsOS()) {
            nbdir = getNbDirFromWindowsRegistry();
        } else {
            nbdir = getDefaultNbDir();
        }
        
        return true;
    }
    
    public boolean queryExit(WizardBeanEvent event) {
        return validateNbDir();
    }
    
    public void exited(WizardBeanEvent event) {
        try {
            String installDir = getInstallDir();
            
            ProductService service = (ProductService)getService(ProductService.NAME);
            service.setProductBeanProperty(
            ProductService.DEFAULT_PRODUCT_SOURCE,
            null,
            "installLocation",
            resolveString(getInstallDir()));
        } catch (ServiceException e) {
            logEvent(this, Log.ERROR, e);
        }
    }
    
    
    private String getInstallDir() {
        return new File(nbdir, resolveString("$L(org.netbeans.installer.cluster.Bundle, Product.clusterDir)")).getPath();
    }
    
    private boolean validateNbDir() {
        try {
            String nbClusterDir = resolveString("$L(org.netbeans.installer.cluster.Bundle,NetBeans.nbClusterDir)");
            File f = new File(nbdir,nbClusterDir);
            if (!f.exists() || !f.isDirectory()) {
                getWizard().getServices().displayUserMessage(
                resolveString("$L(org.netbeans.installer.cluster.Bundle, NetBeansDirChooser.dirChooserDialogTitle)"),
                resolveString("$L(org.netbeans.installer.cluster.Bundle, NetBeansDirChooser.invalidNbDir)"),
                WizardServicesUI.ERROR);
                return false;
            }
            if (!f.canWrite()) {
                getWizard().getServices().displayUserMessage(
                resolveString("$L(org.netbeans.installer.cluster.Bundle, NetBeansDirChooser.dirChooserDialogTitle)"),
                resolveString("$L(org.netbeans.installer.cluster.Bundle, NetBeansDirChooser.cannotWriteNbDir)"),
                WizardServicesUI.ERROR);
                return false;
            }
        } catch (ServiceException ex) {
            logEvent(this, Log.ERROR, ex);
        }
        
        return true;
    }
    
    /** Validate NB dir. No UI. */
    private boolean validateNbDirNoUI (String dir) {
        String nbClusterDir = resolveString("$L(org.netbeans.installer.cluster.Bundle,NetBeans.nbClusterDir)");
        File f = new File(dir,nbClusterDir);
        if (!f.exists() || !f.isDirectory()) {
            return false;
        }
        if (!f.canWrite()) {
            return false;
        }
        return true;
    }
    
    public void build(WizardBuilderSupport support) {
        super.build(support);
        
        support.putRequiredService(ProductService.NAME);
        support.putRequiredService(FileService.NAME);
        support.putRequiredService(Win32RegistryService.NAME);
        try {
            support.putClass(Util.class.getName());
            support.putClass("org.netbeans.installer.cluster.NetBeansDirChooserPanelSwingImpl$1");
        } catch (IOException ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    /** Get NB install location from Windows Registry.
     * @return path to NB installation or empty string when not found
     */
    private String getNbDirFromWindowsRegistry () {
        try {
            //Product key of NB 4.0
            String uidNB = resolveString("$L(org.netbeans.installer.cluster.Bundle, NetBeans.productKey)");
            String nbInstallDir;
            
            Win32RegistryService regserv = (Win32RegistryService) getService(Win32RegistryService.NAME);
            
            int HKEY = Win32RegistryService.HKEY_LOCAL_MACHINE;
            int HKCU = Win32RegistryService.HKEY_CURRENT_USER;
            
            String HKEY_uninstall = "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + uidNB;
            logEvent(this, Log.DBG,"key -> " + HKEY_uninstall);
            
            if (regserv.keyExists(HKEY, HKEY_uninstall)) {
                nbInstallDir = regserv.getStringValue(HKEY, HKEY_uninstall, "InstallLocation", false);
                logEvent(this, Log.DBG,"Retrieved InstallLocation: " + nbInstallDir);
                if (nbInstallDir == null) {
                    return "";
                } else {
                    return nbInstallDir;
                }
            } else {
                logEvent(this, Log.DBG,"Key does not exist");
                return "";
            }
        } catch (ServiceException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return "";
    }
    
    /** Try to get NB default install location. Check if there is NB installation.
     * @return path to NB installation or empty string if not found
     */
    private String getDefaultNbDir () {
        String nbInstallDir;
        
        //Check home
        nbInstallDir = resolveString("$L(org.netbeans.installer.cluster.Bundle, NetBeans.installLocationForNonRoot)");
        logEvent(this, Log.DBG,"Check InstallLocation1: " + nbInstallDir);
        if (validateNbDirNoUI(nbInstallDir)) {
            return nbInstallDir;
        }
        
        //Check default install location
        nbInstallDir = resolveString("$L(org.netbeans.installer.cluster.Bundle, NetBeans.installLocation)");
        logEvent(this, Log.DBG,"Check InstallLocation2: " + nbInstallDir);
        if (validateNbDirNoUI(nbInstallDir)) {
            return nbInstallDir;
        }
        
        return "";
    }
}
