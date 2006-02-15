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
import com.installshield.product.ProductBuilderSupport;
import com.installshield.product.ProductException;
import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.wizard.service.file.FileService;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.system.SystemUtilService;

import java.io.File;
import java.util.StringTokenizer;

public class PostInstallFixupAction extends ProductAction {
    public void build(ProductBuilderSupport support) {
    }
    
    public void install(ProductActionSupport support) throws ProductException {
        patchNbConfig(false);
    }
    
    public void uninstall(ProductActionSupport support) throws ProductException {
        patchNbConfig(true);
        String installDir = "";
        try {
            ProductService pservice = (ProductService)getService(ProductService.NAME);
            installDir = (String) pservice.getProductBeanProperty(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                null,
                "absoluteInstallLocation");
            
            logEvent(this, Log.DBG, "uninstall installDir: " + installDir);
            deleteFiles(installDir, new String[] {"_uninst" + File.separator + "install.log"});

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
    
    private void patchNbConfig (boolean reverse) {
        try {
            FileService fileService = (FileService) getServices().getService(FileService.NAME);
            ProductService pservice = (ProductService)getService(ProductService.NAME);
            
            String fsep = fileService.getSeparator();
            String psep = fileService.getPathSeparator();
            
            String installDir = resolveString((String)pservice.getProductBeanProperty(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                null,
                "absoluteInstallLocation"));
            
            String nbdir = resolveString(installDir + fsep + "..");
            
            String configFilename = nbdir + fsep + "etc" + fsep + "netbeans.conf";
            logEvent(this,Log.DBG,"Patching " + configFilename);
            String[] content = fileService.readAsciiFile(configFilename);
            if (content == null) {
                logEvent(this,Log.ERROR,"Error: Cannot parse " + configFilename);
                return;
            }
            if (!reverse) {
                //Add cnd cluster to netbeans_extraclusters
                int index = -1;
                for (int i = 0; i < content.length; i++) {
                    String line = content[i].trim();
                    if (line.startsWith("netbeans_extraclusters=")) {
                        index = i;
                        break;
                    }
                }
                String line = "";
                if (index >= 0) {
                    StringTokenizer stok = new StringTokenizer(content[index].trim(), psep);
                    while (stok.hasMoreElements()) {
                        if (!"".equals(line)) {
                            line += psep;
                        }
                        String s = stok.nextToken();
                        if (s.endsWith("\"")) {
                            line += s.substring(0,s.length() - 1);
                        } else {
                            line += s;
                        }
                    }
                }
                if (!"".equals(line)) {
                    line += psep;
                }
                line += installDir;
                if (index >= 0) {
                    line = line + "\"";
                    fileService.updateAsciiFile(configFilename, new String[] {line}, index);
                } else {
                    line = "netbeans_extraclusters=\"" + line + "\"";
                    fileService.appendToAsciiFile(configFilename, new String[] {line});
                }
                logEvent(this, Log.DBG, "Replace line " + line);
          } else {
                //Remove cnd cluster from netbeans_extraclusters
                int index = -1;
                for (int i = 0; i < content.length; i++) {
                    String line = content[i].trim();
                    if (line.startsWith("netbeans_extraclusters=")) {
                        index = i;
                        break;
                    }
                }
                if (index < 0) {
                    return;
                }
                File instdir = new File(installDir);
                String line = "";
                StringTokenizer stok = new StringTokenizer(content[index].trim().substring("netbeans_extraclusters=".length()), psep);
                boolean update = false;
                while (stok.hasMoreElements()) {
                    String tok = stok.nextToken();
                    if (tok.startsWith("\"")) {
                        tok = tok.substring(1,tok.length());
                    }
                    if (tok.endsWith("\"")) {
                        tok = tok.substring(0,tok.length() - 1);
                    }
                    if (instdir.equals(new File(tok))) {
                        update = true;
                        continue;
                    }
                    if (!"".equals(line)) {
                        line += psep;
                    }
                    line += tok;
                }
                if (!"".equals(line)) {
                    line = "netbeans_extraclusters=\"" + line + "\"";
                }
                if (update) {
                    //Update only when cnd cluster path is found and removed from netbeans_extraclusters
                    fileService.updateAsciiFile(configFilename, new String[] {line}, index);
                    logEvent(this, Log.DBG, "Replace line " + line);
                }
            }
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
}
