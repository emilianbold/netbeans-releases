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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/** This class is used to initialize some system properties at begining
 * of installation.
 */
public class SetSystemPropertiesAction extends WizardAction {
    String installedJdk = null;
    String installedJre = null;
    
    public void build(WizardBuilderSupport support) {
        try {
            support.putClass(Util.class.getName());
            support.putClass(RunCommand.class.getName());
            support.putRequiredService(SecurityService.NAME);
            support.putRequiredService(Win32RegistryService.NAME);
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    public void execute(WizardBeanEvent evt) {
        //RunnableWizardBeanState state = getState();
        //String msg = resolveString("$L(com.sun.installer.InstallerResources,INIT_PROPS_MSG)");
        //state.setStatusDescription(msg);
        resolveProductBeanProperties();
        checkStorageBuilder();
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
        //It is used to create file 'nb4.1/config/productid'
        Util.setStringPropertyValue("ProductID","NBJDK");
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
        try{
            logEvent(this, Log.DBG,"Checking Win32 Registry ... ");
            File regFile = File.createTempFile("forte",".reg");
            
            String command = "regedit -e " + regFile.getAbsolutePath()
            + " \"HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\"";
            RunCommand runCommand = new RunCommand();
            runCommand.execute(command);
            runCommand.waitFor();
            BufferedReader reader;

            if (Util.isWindows98()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(regFile.getAbsolutePath())));
            } else if (Util.isWindowsNT()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(regFile.getAbsolutePath())));
            } else {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(regFile.getAbsolutePath()),"UTF-16"));
            }
            
            String bundledVersion = resolveString("$L(org.netbeans.installer.Bundle,JDK.version)");
            String line;
            while ((line = reader.readLine()) != null) {
                //logEvent(this, Log.DBG,"findJdkHome line: " + line);
                if (line.startsWith("[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit")){
                    String version = line.substring(line.lastIndexOf("\\") + 1);
                    //Strip off trailing square bracket "]"
                    if (version.lastIndexOf("]") == (version.length() - 1)) {
                        version = version.substring(0, version.length() - 1);
                    }
                    logEvent(this, Log.DBG,"findJdkHome version: '" + version + "'");
                    //Must be equal to match "1.X.X" and not eg."1.X.X_01"
                    if (bundledVersion.equals(version)) {
                        line = reader.readLine();
                        StringTokenizer st = new StringTokenizer(line,"\"");
                        String firstToken="",lastToken="";
                        if (st.hasMoreTokens())  firstToken = st.nextToken();
                        if (firstToken.compareTo("JavaHome") == 0) {
                            while (st.hasMoreTokens())  lastToken = st.nextToken();
                            StringBuffer stringBuffer = new StringBuffer();
                            int index=0;
                            for (int i=0; i<lastToken.length(); i++) {
                                if ((lastToken.charAt(i) == '\\') && (index==0)) {
                                    index++;
                                } else {
                                    stringBuffer.append(lastToken.charAt(i));
                                    if(index > 0) index=0;
                                }
                            }
                            jdkHome=stringBuffer.toString();
                            if (!Util.checkJdkHome(jdkHome)) { 
				System.getProperties().put("corruptJdk",jdkHome);
				logEvent(this, Log.DBG, "corruptJdk: " + System.getProperties().get("corruptJdk"));
				jdkHome = null;
			    }
                        }
                    }
                }
            }
            reader.close();
            regFile.delete();
        } catch(Exception ex){
            ex.printStackTrace();
        }
        return jdkHome;
    }
    
    private String findJreHome() {
        String jreHome = null;
        try {
            logEvent(this, Log.DBG,"Checking Win32 Registry ... ");
            File regFile = File.createTempFile("forte",".reg");
            
            String command = "regedit -e " + regFile.getAbsolutePath()
            + " \"HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Runtime Environment\"";
            RunCommand runCommand = new RunCommand();
            runCommand.execute(command);
            runCommand.waitFor();
            BufferedReader reader;

            if (Util.isWindows98()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(regFile.getAbsolutePath())));
            } else if (Util.isWindowsNT()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(regFile.getAbsolutePath())));
            } else {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(regFile.getAbsolutePath()),"UTF-16"));
            }
            
            String bundledVersion = resolveString("$L(org.netbeans.installer.Bundle,JRE.version)");
            String line;
            while ((line = reader.readLine()) != null) {
                //logEvent(this, Log.DBG,"findJreHome line: " + line);
                if (line.startsWith("[HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Runtime Environment")){
                    String version = line.substring(line.lastIndexOf("\\") + 1);
                    //Strip off trailing square bracket "]"
                    if (version.lastIndexOf("]") == (version.length() - 1)) {
                        version = version.substring(0, version.length() - 1);
                    }
                    logEvent(this, Log.DBG,"findJreHome version: '" + version + "'");
                    //Must be equal to match "1.X.X" and not eg."1.X.X_01"
                    if (bundledVersion.equals(version)) {
                        line = reader.readLine();
                        StringTokenizer st = new StringTokenizer(line,"\"");
                        String firstToken="",lastToken="";
                        if (st.hasMoreTokens())  firstToken = st.nextToken();
                        if (firstToken.compareTo("JavaHome") == 0){
                            while (st.hasMoreTokens())  lastToken = st.nextToken();
                            StringBuffer stringBuffer = new StringBuffer();
                            int index=0;
                            for (int i=0; i<lastToken.length(); i++) {
                                if ((lastToken.charAt(i) == '\\') && (index==0)) {
                                    index++;
                                } else {
                                    stringBuffer.append(lastToken.charAt(i));
                                    if(index > 0) index=0;
                                }
                            }
                            jreHome=stringBuffer.toString();
                            if (!Util.checkJreHome(jreHome)) { 
				System.getProperties().put("corruptJre",jreHome);
				logEvent(this, Log.DBG, "corruptJre: " + System.getProperties().get("corruptJre"));
				jreHome = null;
			    }
                        }
                    }
                }
            }
            reader.close();
            regFile.delete();
        } catch(Exception ex){
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
