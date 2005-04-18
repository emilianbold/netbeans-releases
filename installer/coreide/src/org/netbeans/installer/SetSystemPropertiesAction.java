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

import com.installshield.product.SoftwareObjectKey;
import com.installshield.product.SoftwareVersion;
import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.wizard.WizardAction;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.service.ServiceException;

/** This class is used to initialize some system properties at beginning
 * of installation.
 */
public class SetSystemPropertiesAction extends WizardAction {
    
    public void build(WizardBuilderSupport support) {
        try {
            support.putClass(Util.class.getName());
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    public void execute(WizardBeanEvent evt) {
        resolveProductBeanProperties();
        checkStorageBuilder();
        setDesktopIconName();
    }
    
    private void checkStorageBuilder () {
        if (Util.isWindows95() || Util.isWindows98() || Util.isWindowsME()) {
            try {
                logEvent(this, Log.DBG,"Disable Storage Builder for Win95, Win98, WinME.");
                ProductService service = (ProductService) getService(ProductService.NAME);
                service.setRetainedProductBeanProperty(ProductService.DEFAULT_PRODUCT_SOURCE,
                Names.STORAGE_BUILDER_ID, "active", Boolean.FALSE);
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
    
    private void resolveProductBeanProperties () {
        try {
            ProductService service = (ProductService) getService(ProductService.NAME);
            String prop;
            String resolvedProp;
            
            prop = (String) service.getProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.PRODUCT_ROOT_ID, "name");
            resolvedProp = resolveString(prop);
            logEvent(this, Log.DBG,"prop: " + prop + " resolvedProp: " + resolvedProp);
            service.setRetainedProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.PRODUCT_ROOT_ID, "name", resolvedProp);

            prop = (String) service.getProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.PRODUCT_ROOT_ID, "description");
            resolvedProp = resolveString(prop);
            logEvent(this, Log.DBG,"prop: " + prop + " resolvedProp: " + resolvedProp);
            service.setRetainedProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.PRODUCT_ROOT_ID, "description", resolvedProp);
            
            prop = (String) service.getProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.PRODUCT_ROOT_ID, "productNumber");
            resolvedProp = resolveString(prop);
            logEvent(this, Log.DBG,"prop: " + prop + " resolvedProp: " + resolvedProp);
            service.setRetainedProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.PRODUCT_ROOT_ID, "productNumber", resolvedProp);
            
            prop = (String) service.getProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.PRODUCT_ROOT_ID, "vendor");
            resolvedProp = resolveString(prop);
            logEvent(this, Log.DBG,"prop: " + prop + " resolvedProp: " + resolvedProp);
            service.setRetainedProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.PRODUCT_ROOT_ID, "vendor", resolvedProp);
            
            prop = (String) service.getProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.PRODUCT_ROOT_ID, "vendorWebsite");
            resolvedProp = resolveString(prop);
            logEvent(this, Log.DBG,"prop: " + prop + " resolvedProp: " + resolvedProp);
            service.setRetainedProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.PRODUCT_ROOT_ID, "vendorWebsite", resolvedProp);
            
            SoftwareObjectKey keyObject;
            SoftwareVersion version;
            String major, minor, maintenance, key;

            // ---------------------- Product ---------------------------
            keyObject = (SoftwareObjectKey) service.getProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.PRODUCT_ROOT_ID, "key");
            key = resolveString("$L(org.netbeans.installer.Bundle,Product.UID)");
            logEvent(this, Log.DBG,"Product UID: " + key);
            keyObject.setUID(key);
            
            major = resolveString("$L(org.netbeans.installer.Bundle,Product.major)");
            minor = resolveString("$L(org.netbeans.installer.Bundle,Product.minor)");
            maintenance = resolveString("$L(org.netbeans.installer.Bundle,Product.maintenance)");
            
            version = new SoftwareVersion();
            version.setMajor(major);
            version.setMinor(minor);
            version.setMaintenance(maintenance);
            logEvent(this, Log.DBG,"Product version: " + getStringForm(version));
            keyObject.setVersion(version);
            
            service.setRetainedProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.PRODUCT_ROOT_ID, "key", keyObject);
            
            // ------------------ Core IDE -----------------------------
            keyObject = (SoftwareObjectKey) service.getProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, "coreide", "key");
            key = resolveString("$L(org.netbeans.installer.Bundle,CoreIDE.UID)");
            logEvent(this, Log.DBG,"Core IDE UID: " + key);
            keyObject.setUID(key);
            
            version = new SoftwareVersion();
            version.setMajor(major);
            version.setMinor(minor);
            version.setMaintenance(maintenance);
            logEvent(this, Log.DBG,"Core IDE version: " + getStringForm(version));
            keyObject.setVersion(version);
            
            service.setRetainedProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, "coreide", "key", keyObject);
            
            // ----------------- Storage Builder ------------------------
            keyObject = (SoftwareObjectKey) service.getProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.STORAGE_BUILDER_ID, "key");
            key = resolveString("$L(org.netbeans.installer.Bundle,StorageBuilder.UID)");
            logEvent(this, Log.DBG,"Storage Builder UID: " + key);
            keyObject.setUID(key);
            
            version = new SoftwareVersion();
            version.setMajor(major);
            version.setMinor(minor);
            version.setMaintenance(maintenance);
            logEvent(this, Log.DBG,"Storage Builder version: " + getStringForm(version));
            keyObject.setVersion(version);
            
            service.setRetainedProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.STORAGE_BUILDER_ID, "key", keyObject);
        } catch (ServiceException ex) {
            ex.printStackTrace();
            Util.logStackTrace(this,ex);
        }
    }
    
    /** Simplified conversion of SoftwareVersion instance to String. Just for logging. */
    private String getStringForm (SoftwareVersion version) {
        String ret = "";
        if (version.getMajor().length() == 0) {
            return ret;
        } else {
            ret = version.getMajor();
        }
        if (version.getMinor().length() == 0) {
            return ret;
        } else {
            ret += "." + version.getMinor();
        }
        if (version.getMaintenance().length() == 0) {
            return ret;
        } else {
            ret += "." + version.getMaintenance();
        }
        return ret;
    }
    
}
