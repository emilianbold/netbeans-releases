/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer.cluster;

import com.installshield.product.ProductAction;
import com.installshield.product.ProductActionSupport;
import com.installshield.product.ProductException;
import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.wizard.service.file.FileService;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.system.SystemUtilService;

import java.io.File;

public class PostInstallFixupAction extends ProductAction {
    
    private String installDir;
    
    private String nbDir;
    
    private String clusterName;
    
    private static final String BUNDLE = "$L(org.netbeans.installer.cluster.Bundle,";
    
    private void init () throws ServiceException {
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
            patchNbClusters(true);
        } catch (ServiceException ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    public void uninstall(ProductActionSupport support) throws ProductException {
        try {
            init();
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
    
    private void deleteFiles(String dir, String[] fileNames) {
        try {
            FileService fileService = (FileService) getServices().getService(FileService.NAME);
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
            FileService fileService = (FileService) getServices().getService(FileService.NAME);
            
            String fname = nbDir + File.separator + "etc" + File.separator + "netbeans.clusters";
            logEvent(this,Log.DBG,"Patching " + fname);
            String[] content = fileService.readAsciiFile(fname);
            if (content == null) {
                logEvent(this,Log.ERROR,"Error: Cannot parse " + fname);
                return;
            }
            if (add) {
                //Add profiler cluster to netbeans.clusters
                //First check if profiler cluster is present if yes then do not add it again
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
                //Remove profiler cluster from netbeans.clusters
                //First check if profiler cluster is present if yes then remove it
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
