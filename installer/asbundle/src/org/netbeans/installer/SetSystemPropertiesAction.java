/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.wizard.WizardAction;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.security.SecurityService;


/** This class is used to initialize some system properties at beginning
 * of installation.
 */
public class SetSystemPropertiesAction extends WizardAction {
    
    public void build(WizardBuilderSupport support) {
        try {
            support.putClass(Util.class.getName());
            support.putRequiredService(SecurityService.NAME);
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    public void execute(WizardBeanEvent evt) {
        //RunnableWizardBeanState state = getState();
        //String msg = resolveString("$L(com.sun.installer.InstallerResources,INIT_PROPS_MSG)");
        //state.setStatusDescription(msg);
        checkStorageBuilder();
        setDesktopIconName();
        setAdminProperties();
        
        //It is used to create file 'nb4.1/config/productid'
        Util.setStringPropertyValue("ProductID","NBAS");
    }
    
    private void checkStorageBuilder () {
        if (Util.isWindows95() || Util.isWindows98() || Util.isWindowsME()) {
            try {
                logEvent(this, Log.DBG,"Disable Storage Builder for Win95, Win98, WinME.");
                ProductService service = (ProductService) getService(ProductService.NAME);
                service.setRetainedProductBeanProperty
                (ProductService.DEFAULT_PRODUCT_SOURCE, "storageBuilder", "active", Boolean.FALSE);
            } catch(ServiceException ex) {
                ex.printStackTrace();
                Util.logStackTrace(this,ex);
            }
        }
    }
    
    private void setDesktopIconName () {
        if (Util.isWindowsOS()) {
            try {
                String name = resolveString("$L(org.netbeans.installer.Bundle,Product.desktopIconName)");
                logEvent(this, Log.DBG,"Set Desktop Icon Name: " + name);
                ProductService service = (ProductService) getService(ProductService.NAME);
                service.setRetainedProductBeanProperty
                (ProductService.DEFAULT_PRODUCT_SOURCE, "desktopicon", "name", name);
                
                String folder = resolveString("$L(org.netbeans.installer.Bundle,Product.programsMenuFolderName)");
                logEvent(this, Log.DBG,"Set Programs Menu Folder Name: " + folder);
                service.setRetainedProductBeanProperty
                (ProductService.DEFAULT_PRODUCT_SOURCE, "programsmenu", "folder", folder);
            } catch (ServiceException ex) {
                ex.printStackTrace();
                Util.logStackTrace(this,ex);
            }
        }
    }
    
    private void setAdminProperties () {
        try {
            boolean isAdmin;

            SecurityService secService = (SecurityService)getServices().getService(SecurityService.NAME);
            isAdmin = secService.isCurrentUserAdmin();
	    Util.setBooleanPropertyValue("isAdmin",isAdmin);
            logEvent(this, Log.DBG,"isAdmin: " + isAdmin);
        }
        catch(ServiceException ex) {
            Util.logStackTrace(this, ex);                
        }
    }
    
}
