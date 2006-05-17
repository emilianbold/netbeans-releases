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
            ProductService productService = (ProductService) getService(ProductService.NAME);
            SoftwareObjectKey key = (SoftwareObjectKey)
            productService.getProductBeanProperty(productSource, null, "key");
            String uid = key.getUID();
            
            Win32RegistryService regService = (Win32RegistryService) getService(Win32RegistryService.NAME);
            
            int HKEY = Win32RegistryService.HKEY_LOCAL_MACHINE;
            int HKCU = Win32RegistryService.HKEY_CURRENT_USER;
            
            String HKEY_uninstall = "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall";
            String [] keyNames = regService.getSubkeyNames(HKEY,HKEY_uninstall);
            
            logEvent(this, Log.DBG,"uid: " + uid);
            logEvent(this, Log.DBG,"Key -> " + HKEY_uninstall);
            logEvent(this, Log.DBG,"nbInstallDir: " + nbInstallDir);
            
            for (int i = 0; i < keyNames.length; i++) {
                if (keyNames[i].startsWith(uid)) {
                    String nbKey = HKEY_uninstall + "\\" + keyNames[i];
                    String value = regService.getStringValue(HKEY,nbKey,"UninstallString",false);
                    logEvent(this, Log.DBG,"First match with UID keyNames[" + i + "]: " + keyNames[i]
                    + " UninstallString: " + value);
                    int pos = value.indexOf("\\_uninst");
                    if (pos == -1) {
                        logEvent(this, Log.ERROR,"Invalid value of UninstallString."
                        + " Cannot locate substring \"\\uninst\".");
                        logEvent(this, Log.ERROR,"Cannot modify values in windows registry.");
                        break;
                    }
                    String strippedValue = value.substring(0,pos);
                    logEvent(this, Log.DBG,"strippedValue: " + strippedValue);
                    if (strippedValue.equals(nbInstallDir)) {
                        logEvent(this, Log.DBG,"Second match with UninstallString. Modify/Add Keys.");
                        
                        String icon = nbInstallDir + File.separator + "bin" + File.separator + "netbeans.exe";
                        logEvent(this, Log.DBG,"Adding value -> " + HKEY_uninstall + "\\" + icon);
                        regService.setStringValue(HKEY, nbKey, "DisplayIcon", false, icon);

                        logEvent(this, Log.DBG,"Adding value -> " + HKEY_uninstall + "\\" + nbInstallDir);
                        regService.setStringValue(HKEY, nbKey, "InstallLocation", false, nbInstallDir);
                        break;
                    }
                }
            }
            
            //regserv.set32BitValue(HKEY, HKEY_uninstall,"EstimatedSize",83000);
        } catch (ServiceException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
}
