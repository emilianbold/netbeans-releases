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

import java.io.File;

import org.netbeans.installer.PatchProductID;
import org.netbeans.installer.Util;

public class PostInstallFixupAction extends ProductAction {
    
    private static final String BUNDLE = "$L(org.netbeans.installer.cluster.Bundle,";
    
    private static final String CLUSTERS_CONF_FILE_NAME = "netbeans.clusters";
    
    private String [] nbClusterDirArray = new String[0];
    
    private String installDir;
    
    private String nbDir;
    
    private String clusterName;
    
    private FileService fileService;
    
    public void build(ProductBuilderSupport support) {
        try {
            support.putClass(PatchProductID.class.getName());
            support.putClass(Util.class.getName());
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    private void init () throws ServiceException {
        fileService = (FileService)getServices().getService(FileService.NAME);
        ProductService pservice = (ProductService)getService(ProductService.NAME);
        installDir = (String) pservice.getProductBeanProperty(
            ProductService.DEFAULT_PRODUCT_SOURCE,
            null,
            "absoluteInstallLocation");
        nbDir = installDir + File.separator + "..";
        clusterName = resolveString(BUNDLE + "Product.clusterDir)");
    }
    
    public void install(ProductActionSupport support) throws ProductException {
        try {
            init();
            patchProductID(true);
            patchNbConfig(false);
        } catch (ServiceException ex) {
            ex.printStackTrace();
        }
    }
    
    public void uninstall(ProductActionSupport support) throws ProductException {
        try {
            init();
            
            logEvent(this, Log.DBG, "uninstall installDir: " + installDir);
            deleteFiles(installDir, new String[] {"_uninst" + File.separator + "install.log"});
            patchProductID(false);
            patchNbConfig(true);
            
            logEvent(this, Log.DBG, "uninstall Delete install dir on exit: " + installDir);
            SystemUtilService systemUtilService = (SystemUtilService) getServices().getService(SystemUtilService.NAME);
            systemUtilService.deleteDirectoryOnExit(installDir,false);
        } catch (ServiceException ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    private void initClusterDirArray () {
        int arrLength = 0;
        String s = resolveString(BUNDLE + "NetBeans.clusterDirLength)");
        try {
            arrLength = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            logEvent(this, Log.ERROR,"Incorrect number for NetBeans.clusterDirLength: " + s);
        }
        
        //No order is defined.
        if (arrLength == 0) {
            return;
        }
        nbClusterDirArray = new String[arrLength];
        for (int i = 0; i < arrLength; i++) {
            nbClusterDirArray[i] = resolveString(BUNDLE + "NetBeans.nbClusterDir" + i + ")");
            logEvent(this, Log.DBG,"nbClusterDirArray[" + i + "]: " + nbClusterDirArray[i]);
        }
    }
    
    private void patchProductID (boolean add) {
        initClusterDirArray();
        String fileName = "";
        File dir;
        boolean found = false;
        for (int i = 0; i < nbClusterDirArray.length; i++) {
            dir = new File(nbDir + File.separator + nbClusterDirArray[i]);
            if (dir.isDirectory()) {
                fileName = nbDir + File.separator + nbClusterDirArray[i] + File.separator + "config" + File.separator + "productid";
                found = true;
            }
        }
        if (!found) {
            logEvent(this,Log.ERROR,"Error: Cannot find any nb cluster dir. Cannot patch productid.");
            return;
        }
        File f = new File(fileName);
        if (!f.exists()) {
            if (add) {
                logEvent(this,Log.WARNING,"Warning: Cannot find file:" + f + " File will be created.");
                try {
                    fileService.createAsciiFile(fileName,new String [] {PatchProductID.NB_ID_IDE + "_" + PatchProductID.PACK_ID_CDC});
                } catch (ServiceException ex) {
                    logEvent(this,Log.ERROR,"Error: Cannot create file:" + fileName);
                    Util.logStackTrace(this,ex);
                }
                return;
            } else {
                logEvent(this,Log.ERROR,"Error: Cannot find file:" + f + " Cannot patch productid.");
                return;
            }
        }
        String[] content = null;
        try {
            logEvent(this,Log.DBG,"Patching file: " + fileName);
            content = fileService.readAsciiFile(fileName);
        } catch (ServiceException ex) {
            logEvent(this,Log.ERROR,"Error: Cannot parse file:" + fileName);
            Util.logStackTrace(this,ex);
            return;
        }
        if (content == null) {
            logEvent(this,Log.ERROR,"Error: Cannot parse file:" + fileName);
            return;
        }
        if (content.length == 0) {
            logEvent(this,Log.ERROR,"Error: Empty file:" + fileName);
            return;
        }
        if (content.length > 1) {
            logEvent(this,Log.WARNING,"Warning: productid file should contain only one line.");
        }
        String productID = content[0].trim();
        logEvent(this,Log.DBG,"productID before patch: " + productID);
        
        if (add) {
            productID = PatchProductID.add(productID,PatchProductID.PACK_ID_CDC,this);
        } else {
            productID = PatchProductID.remove(productID,PatchProductID.PACK_ID_CDC,this);
        }
        logEvent(this,Log.DBG,"productID after patch: " + productID);
        try {
            fileService.updateAsciiFile(fileName,new String [] { productID },0);
        } catch (ServiceException ex) {
            logEvent(this,Log.ERROR,"Error: Cannot update file:" + fileName);
            Util.logStackTrace(this,ex);
        }
    }
    
    private void deleteFiles(String dir, String[] fileNames) {
        try {
            for (int i = 0; i < fileNames.length; i++) {
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
        String fsep = fileService.getSeparator();
        String psep = fileService.getPathSeparator();
        
        StringBuffer cf = new StringBuffer();
        cf.append(nbDir);
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

