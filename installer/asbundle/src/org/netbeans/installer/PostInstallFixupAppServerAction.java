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

import com.installshield.product.ProductAction;
import com.installshield.product.ProductActionSupport;
import com.installshield.product.ProductBuilderSupport;
import com.installshield.product.ProductException;
import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.util.ProcessExec;
import com.installshield.util.ProcessExecException;
import com.installshield.util.ProcessOutputHandler;
import com.installshield.wizard.platform.win32.Win32RegistryService;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.file.FileService;
import com.installshield.wizard.service.system.SystemUtilService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PostInstallFixupAppServerAction extends ProductAction {
    
    private String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
    
    private String nbInstallDir = null;
    private String rootInstallDir = null;
    private String configDir = null;
    
    private FileService fileService;
    private String psep;
    private String sep;
    
    
    public void build(ProductBuilderSupport support) {
        try {
            support.putClass(Util.class.getName());
            support.putRequiredService(FileService.NAME);
            support.putRequiredService(ProductService.NAME);
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    public void init(ProductActionSupport support) throws ProductException {
        try {
            // need to get absoluteInstallLocation because uninstaller doesn't know the system properties.
            fileService = (FileService) getServices().getService(FileService.NAME);
            ProductService productService = (ProductService) getService(ProductService.NAME);
            
            psep = fileService.getPathSeparator();
            sep = fileService.getSeparator();
            rootInstallDir = resolveString((String) productService.getProductBeanProperty(productURL,null,"absoluteInstallLocation"));
            if (Util.isMacOSX()) {
                nbInstallDir = rootInstallDir + sep 
                + resolveString("$L(org.netbeans.installer.Bundle,Product.nbLocationBelowInstallRoot)");
            } else {
                nbInstallDir = rootInstallDir;
            }
        } catch (Exception e) {
            logEvent(this, Log.ERROR, e);
        }
        
        logEvent(this, Log.DBG,"nbInstallDir is " + nbInstallDir);
        
        configDir = nbInstallDir + sep + "etc";
    }
    
    public void install(ProductActionSupport support) throws ProductException {
        logEvent(this, Log.DBG,"install, support is " + support +" ...");
        
        init(support);
        
        if (Names.INSTALLER_AS_BUNDLE.equals(Util.getStringPropertyValue(Names.INSTALLER_TYPE))) {
            addASInstallDirToIDEConfigFile();
        }
        
    }
    
    public void uninstall(ProductActionSupport support) throws ProductException {
        logEvent(this, Log.DBG,"uninstall, support is " + support +" ...");
        
        try {
            init(support);
            
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    public void replace(ProductAction oldAction, ProductActionSupport support) throws ProductException {
        logEvent(this, Log.DBG,"replace, oldAction is " + oldAction +", support is " + support + " ...");
        
        // TODO might be modify config file
    }
    
    /** Adds value for system property com.sun.aas.installRoot to netbeans.conf.
     * Path (property value) is surrounded by double quotes on Windows.
     */
    //-J-Dcom.sun.aas.installRoot=\"E:\software\appserver81_ur2\"
    private void addASInstallDirToIDEConfigFile() {
        //If not set do not do anything (eg. if SJS AS installer was cancelled)
        String asInstallDir = Util.getASInstallDir();
        if (asInstallDir == null) {
            return;
        }
        try {
            String configFilename = configDir + sep + "netbeans.conf";
            logEvent(this, Log.DBG, "Patching: " + configFilename);
            
            int whereToReplace = -1;
            
            String[] content = fileService.readAsciiFile(configFilename);
            if (content != null ) {
                for (int i = 0; i < content.length; i++) {
                    if (content[i].trim().startsWith("netbeans_default_options")) {
                        whereToReplace = i;
                        break;
                    }
                }
            }
            
            if (whereToReplace >= 0) {
                String line = content[whereToReplace].trim();
                //Locate " from end of line
                int pos = line.lastIndexOf('"');
                String newLine = "";
                //Add " around path on Windows due to possible space(s) in path
                //On Linux/Solaris/Mac it is not necessary as SJS AS installer does
                //not allow space in path. If it will be changed it must be tested
                //also with IDE launcher.
                if (Util.isWindowsOS()) {
                    newLine = line.substring(0, pos) + " " 
                    + "-J-Dcom.sun.aas.installRoot=\"" + asInstallDir + "\"\"";
                } else {
                    newLine = line.substring(0, pos) + " " 
                    + "-J-Dcom.sun.aas.installRoot=" + asInstallDir + "\"";
                }
                logEvent(this, Log.DBG, "newLine: " + newLine);
                fileService.updateAsciiFile(configFilename, new String[] {newLine}, whereToReplace);
            }
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
}
