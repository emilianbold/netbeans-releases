package org.netbeans.installer;

import java.io.*;
import java.util.Date;
import java.lang.Runtime;
import com.installshield.wizard.*;
import com.installshield.wizard.service.*;
import com.installshield.product.service.product.*;
import com.installshield.product.*;
import com.installshield.util.*;
import com.installshield.archive.*;
import com.installshield.wizard.awt.*;
import com.installshield.wizard.console.*;
import com.installshield.wizard.platform.win32.*;

public class PostInstallWizardAction extends WizardAction {
    
    private String nbInstallDir = null;

    public void build(WizardBuilderSupport support) {
	try {
            support.putRequiredService(Win32RegistryService.NAME);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public void execute(WizardBeanEvent evt) {
        try {
            ProductService pservice = (ProductService)getService(ProductService.NAME);
            nbInstallDir = (String) pservice.getProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE,null,"absoluteInstallLocation");
        } catch (Exception e) {
            logEvent(this, Log.ERROR, e);
            nbInstallDir = Util.getNbInstallDir();
        }

 	if (Util.isWindowsOS()) {
	    setWinAddRemoveFolderInfo();
	}
    } // end execute()
    
    /** Set the Windows Uninstall InstallLocation to fix size of product in Add/Remove Programs
     * and icon in Add/Remove Programs - issue #46025.
     */
    private void setWinAddRemoveFolderInfo() {
        try {
            String productSource = ProductService.DEFAULT_PRODUCT_SOURCE;
            ProductService productService = (ProductService)getService(ProductService.NAME);
            SoftwareObjectKey key = (SoftwareObjectKey)productService.getProductBeanProperty(productSource,null,"key"); //NOI18N
            String uid = key.getUID();
            
            Win32RegistryService regserv = (Win32RegistryService)getService(Win32RegistryService.NAME);
            
            int HKEY = Win32RegistryService.HKEY_LOCAL_MACHINE;
            int HKCU = Win32RegistryService.HKEY_CURRENT_USER;
            
            String HKEY_uninstall = "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + uid;
            logEvent(this, Log.DBG,"Key -> " + HKEY_uninstall);
            
            if (regserv.keyExists(HKEY, HKEY_uninstall)) {
                String icon = nbInstallDir + File.separator + "bin" + File.separator + "netbeans.exe";
                logEvent(this, Log.DBG,"Adding value -> " + HKEY_uninstall + "\\" + icon);
                regserv.setStringValue(HKEY, HKEY_uninstall, "DisplayIcon", false, icon);

                logEvent(this, Log.DBG,"Adding value -> " + HKEY_uninstall + "\\" + nbInstallDir);
                regserv.setStringValue(HKEY, HKEY_uninstall, "InstallLocation", false, nbInstallDir);
            } else {
                logEvent(this, Log.DBG,"Key does not exist -> " + HKEY_uninstall);
            }
            //regserv.set32BitValue(HKEY, HKEY_uninstall,"EstimatedSize",83000);
        } catch (ServiceException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
}
