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
    
    private String installDir;
    
    private String nbDir;
    
    private String clusterName;
    
    private String [] nbClusterDirArray = new String[0];
    
    private FileService fileService = null;
            
    private static final String BUNDLE = "$L(org.netbeans.installer.cluster.Bundle,";
    
    public void build(ProductBuilderSupport support) {
        try {
            support.putClass(PatchProductID.class.getName());
            support.putClass(Util.class.getName());
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    private void init () throws ServiceException {
        fileService = (FileService) getServices().getService(FileService.NAME);
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
            patchNbClusters(true);
        } catch (ServiceException ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    public void uninstall(ProductActionSupport support) throws ProductException {
        try {
            init();
            patchProductID(false);
            patchNbClusters(false);
            logEvent(this, Log.DBG, "uninstall installDir: " + installDir);
            deleteFiles(installDir, new String[] {"_uninst" + File.separator + "install.log"});

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
                    fileService.createAsciiFile(fileName,new String [] {PatchProductID.NB_ID_IDE + "_" + PatchProductID.PACK_ID_MOBILITY});
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
            productID = PatchProductID.add(productID,PatchProductID.PACK_ID_MOBILITY,this);
        } else {
            productID = PatchProductID.remove(productID,PatchProductID.PACK_ID_MOBILITY,this);
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
    
    private void patchNbClusters (boolean add) {
        try {
            String fname = nbDir + File.separator + "etc" + File.separator + "netbeans.clusters";
            logEvent(this,Log.DBG,"Patching " + fname);
            String[] content = fileService.readAsciiFile(fname);
            if (content == null) {
                logEvent(this,Log.ERROR,"Error: Cannot parse " + fname);
                return;
            }
            if (add) {
                //Add mobility cluster to netbeans.clusters
                //First check if mobility cluster is present if yes then do not add it again
                for (int i = 0; i < content.length; i++) {
                    //If line starts with "#" it is comment.
                    if (!content[i].startsWith("#")) {
                        String line = content[i].trim();
                        if (line.equals(clusterName)) {
                            logEvent(this,Log.DBG,"Cluster name is already present");
                            return;
                        }
                    }
                }
                //Not found append it
                fileService.appendToAsciiFile(fname, new String[] {clusterName});
                logEvent(this, Log.DBG, "Append line: '" + clusterName + "'");
            } else {
                //Remove mobility cluster from netbeans.clusters
                //First check if mobility cluster is present if yes then remove it
                int index = -1;
                for (int i = 0; i < content.length; i++) {
                    //If line starts with "#" it is comment.
                    if (!content[i].startsWith("#")) {
                        String line = content[i].trim();
                        if (line.equals(clusterName)) {
                            index = i;
                            break;
                        }
                    }
                }
                if (index < 0) {
                    return;
                }
                String [] update = new String[content.length - 1];
                for (int i = 0, j = 0; i < content.length; i++) {
                    if (i != index) {
                        update[j] = content[i];
                        j++;
                    }
                }
                fileService.createAsciiFile(fname,update);
                logEvent(this, Log.DBG, "Remove line: '" + content[index] + "'");
            }
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
}
