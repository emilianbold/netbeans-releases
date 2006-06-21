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

import com.installshield.product.ProductAction;
import com.installshield.product.ProductActionSupport;
import com.installshield.product.ProductBuilderSupport;
import com.installshield.product.ProductException;
import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.wizard.service.file.FileService;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.system.SystemUtilService;
import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostInstallFixupAction extends ProductAction {
    public void build(ProductBuilderSupport support) {
    }
    
    public void install(ProductActionSupport support) throws ProductException {
        try {
            patchNbConfig(false);
        } catch (ServiceException ex) {
            ex.printStackTrace();
        }
    }
    
    public void uninstall(ProductActionSupport support) throws ProductException {
        String installDir = "";
        try {
            ProductService pservice = (ProductService)getService(ProductService.NAME);
            installDir = (String) pservice.getProductBeanProperty(
                    ProductService.DEFAULT_PRODUCT_SOURCE,
                    null,
                    "absoluteInstallLocation");
            
            logEvent(this, Log.DBG, "uninstall installDir: " + installDir);
            deleteFiles(installDir, new String[] {"_uninst" + File.separator + "install.log"});
            patchNbConfig(true);
            
            logEvent(this, Log.DBG, "uninstall Delete install dir on exit: " + installDir);
            SystemUtilService systemUtilService = (SystemUtilService) getServices().getService(SystemUtilService.NAME);
            systemUtilService.deleteDirectoryOnExit(installDir,false);
        } catch (ServiceException ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    public void deleteFiles(String dir, String[] fileNames) {
        try {
            FileService fileService = (FileService) getServices().getService(FileService.NAME);
            for (int i=0; i< fileNames.length; i++) {
                if (fileNames[i] == null) {  //array bigger than num objs in array.
                    return;
                }
                String filename = dir + File.separator + fileNames[i];
                if (fileService.fileExists(filename)) {
                    logEvent(this, Log.DBG, "deleting " + filename);
                    fileService.deleteFile(filename);
                } else {
                    logEvent(this, Log.DBG, "cannot find " + filename);
                }
            }
        } catch (ServiceException ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    /**
     * Update etc/netbeans.clusters is necessary
     *
     * @param reverse whether cluster entry should be removed or added
     * @author Anton Chechel
     */
    private void patchNbConfig(boolean reverse) throws ServiceException {
        FileService fileService = (FileService) getServices().getService(FileService.NAME);
        ProductService pservice = (ProductService) getService(ProductService.NAME);
        
        String fsep = fileService.getSeparator();
        String psep = fileService.getPathSeparator();
        
        String installDir = resolveString((String) pservice.getProductBeanProperty(
                ProductService.DEFAULT_PRODUCT_SOURCE, null, "absoluteInstallLocation")); //NOI18N
        
        String nbdir = resolveString(installDir + fsep + ".."); //NOI18N
        String clusterName = resolveString(BUNDLE + "Product.clusterDir)"); //NOI18N
        
        StringBuffer cf = new StringBuffer();
        cf.append(nbdir);
        cf.append(fsep);
        cf.append("etc"); //NOI18N
        cf.append(fsep);
        cf.append(CLUSTERS_CONF_FILE_NAME); //NOI18N
        String configFilename = cf.toString();
        
        logEvent(this, Log.DBG, "patching " + configFilename); //NOI18N
        String[] content = fileService.readAsciiFile(configFilename);
        
        if (! reverse) { // add new cluster name
            StringBuffer lineToAdd = new StringBuffer();
//            lineToAdd.append('\n'); //NOI18N
            lineToAdd.append(clusterName);
//            lineToAdd.append('\n'); //NOI18N
            
            if (null != content) {
                boolean found = false;
                for (int i = 0; i < content.length; i++) {
                    if (content[i].contains(clusterName)) {
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    logEvent(this, Log.DBG, clusterName + " added"); //NOI18N
                    fileService.appendToAsciiFile(configFilename, new String[] {lineToAdd.toString()});
                } else {
                    logEvent(this, Log.DBG, clusterName + " cluster already exists, appending content..."); //NOI18N
                }
            } else { // empty file
                logEvent(this, Log.DBG, "clusters file is empty"); //NOI18N
                logEvent(this, Log.DBG, clusterName + " added"); //NOI18N
                fileService.appendToAsciiFile(configFilename, new String[] {lineToAdd.toString()});
            }
        } else { // remove cluster entry
            if (null == content) { // empty file
                logEvent(this, Log.DBG, "there is nothing to patch"); //NOI18N
                return;
            }
            
            for (int i = 0; i < content.length; i++) { // remove any line which contains our cluster name
                int index = content[i].indexOf(clusterName);
                if (index != -1) {
                    fileService.updateAsciiFile(configFilename, new String[] {""}, i); //NOI18N
                }
            }
        }
        logEvent(this, Log.DBG, "patching finished"); //NOI18N
    }
}

