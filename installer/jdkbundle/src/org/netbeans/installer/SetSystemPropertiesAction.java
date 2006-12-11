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

package org.netbeans.installer;

import com.installshield.product.SoftwareObjectKey;
import com.installshield.product.SoftwareVersion;
import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.wizard.WizardAction;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.platform.win32.Win32RegistryService;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.security.SecurityService;
import java.util.Locale;

/** This class is used to initialize some system properties at begining
 * of installation.
 */
public class SetSystemPropertiesAction extends WizardAction {
    String installedJdk = null;
    String installedJre = null;
    
    public void build(WizardBuilderSupport support) {
        Locale [] locales = new Locale[3];
        locales[0] = new Locale("zh","HK");
        locales[1] = new Locale("zh","TW");
        locales[2] = new Locale("zh","CN");
        try {
            support.putClass(Util.class.getName());
            support.putClass(RunCommand.class.getName());
            support.putRequiredService(SecurityService.NAME);
            support.putRequiredService(Win32RegistryService.NAME);
            support.putResourceBundles("org.netbeans.installer.Bundle",locales);
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    public void execute(WizardBeanEvent evt) {
        Util.logSystemInfo(this);
        resolveProductBeanProperties();
        setDesktopIconName();
        setAdminProperties();
        
	if (Util.isWindowsOS()) {
            installedJdk = findJdkHome();
            if (installedJdk != null)  {
                //If JDK is already installed then only installing NetBeans
                Util.setInstalledJdk(installedJdk);
                logEvent(this, Log.DBG,"-- installedJDK: " + installedJdk);
                Util.setJDKAlreadyInstalled(true);
            } else {
                logEvent(this, Log.DBG,"-- installedJDK NOT FOUND");
            }
            installedJre = findJreHome();
            if (installedJre != null)  {
                Util.setInstalledJre(installedJre);
                logEvent(this, Log.DBG,"-- installedJRE: " + installedJre);
                Util.setJREAlreadyInstalled(true);
            } else {
                logEvent(this, Log.DBG,"-- installedJRE NOT FOUND");
            }
	}
        //It is used to create file nbClusterDir + '/config/productid'
        Util.setStringPropertyValue("ProductID","NBJDK");
        Util.setStringPropertyValue(Names.INSTALLER_TYPE,Names.INSTALLER_JDK_BUNDLE);
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
    
    private String findJdkHome() {
        String jdkHome = null;
        try {
            Win32RegistryService regService = (Win32RegistryService) getService(Win32RegistryService.NAME);
            int HKLM = Win32RegistryService.HKEY_LOCAL_MACHINE;
            String HKEY_JDK = "SOFTWARE\\JavaSoft\\Java Development Kit";
            
            if (!regService.keyExists(HKLM,HKEY_JDK)) {
                return null;
            }
            
            String [] keys = regService.getSubkeyNames(HKLM,HKEY_JDK);
                        
            for (int i = 0; i < keys.length; i++) {
                logEvent(this, Log.DBG,"findJdkHome keys[" + i + "]: " + keys[i]);
            }
            String bundledVersion = resolveString("$L(org.netbeans.installer.Bundle,JDK.version)");
            for (int i = 0; i < keys.length; i++) {
                if (keys[i].equals(bundledVersion)) {
                    String javaVersionKey = HKEY_JDK + "\\" + keys[i];
                    jdkHome = regService.getStringValue(HKLM,javaVersionKey,"JavaHome",false);
                    logEvent(this, Log.DBG,"jdkHome:" + jdkHome);
                    if (!Util.checkJdkHome(jdkHome)) { 
                        System.getProperties().put("corruptJdk",jdkHome);
                        logEvent(this, Log.DBG, "corruptJdk: " + System.getProperties().get("corruptJdk"));
                        jdkHome = null;
                    }
                }
            }
            
        } catch (ServiceException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return jdkHome;
    }
    
    private String findJreHome() {
        String jreHome = null;
        try {
            Win32RegistryService regService = (Win32RegistryService) getService(Win32RegistryService.NAME);
            int HKLM = Win32RegistryService.HKEY_LOCAL_MACHINE;
            String HKEY_JRE = "SOFTWARE\\JavaSoft\\Java Runtime Environment";
            
            if (!regService.keyExists(HKLM,HKEY_JRE)) {
                return null;
            }
            
            String [] keys = regService.getSubkeyNames(HKLM,HKEY_JRE);
                        
            for (int i = 0; i < keys.length; i++) {
                logEvent(this, Log.DBG,"findJdkHome keys[" + i + "]: " + keys[i]);
            }
            String bundledVersion = resolveString("$L(org.netbeans.installer.Bundle,JRE.version)");
            for (int i = 0; i < keys.length; i++) {
                if (keys[i].equals(bundledVersion)) {
                    String javaVersionKey = HKEY_JRE + "\\" + keys[i];
                    jreHome = regService.getStringValue(HKLM,javaVersionKey,"JavaHome",false);
                    logEvent(this, Log.DBG,"jreHome:" + jreHome);
                    if (!Util.checkJreHome(jreHome)) { 
                        System.getProperties().put("corruptJre",jreHome);
                        logEvent(this, Log.DBG, "corruptJre: " + System.getProperties().get("corruptJre"));
                        jreHome = null;
                    }
                }
            }
            
        } catch (ServiceException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return jreHome;
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
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.CORE_IDE_ID, "key");
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
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.CORE_IDE_ID, "key", keyObject);
            
            // ------------------ J2SE -----------------------------
            keyObject = (SoftwareObjectKey) service.getProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.J2SE_ID, "key");
            key = resolveString("$L(org.netbeans.installer.Bundle,J2SE.UID)");
            logEvent(this, Log.DBG,"J2SE UID: " + key);
            keyObject.setUID(key);
            
            service.setRetainedProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.J2SE_ID, "key", keyObject);
            
            // ----------------- Unpack Jars ------------------------
            keyObject = (SoftwareObjectKey) service.getProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.UNPACK_JARS_ID, "key");
            key = resolveString("$L(org.netbeans.installer.Bundle,UnpackJars.UID)");
            logEvent(this, Log.DBG,"Unpack Jars UID: " + key);
            keyObject.setUID(key);
            
            version = new SoftwareVersion();
            version.setMajor(major);
            version.setMinor(minor);
            version.setMaintenance(maintenance);
            logEvent(this, Log.DBG,"Unpack Jars version: " + getStringForm(version));
            keyObject.setVersion(version);
            
            service.setRetainedProductBeanProperty
            (ProductService.DEFAULT_PRODUCT_SOURCE, Names.UNPACK_JARS_ID, "key", keyObject);
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
