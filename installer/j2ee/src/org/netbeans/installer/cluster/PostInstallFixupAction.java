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

package org.netbeans.installer.cluster;

import com.installshield.product.ProductAction;
import com.installshield.product.ProductActionSupport;
import com.installshield.product.ProductBuilderSupport;
import com.installshield.product.ProductException;
import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.wizard.service.file.FileService;
import com.installshield.wizard.service.ServiceException;
import java.io.File;
import java.util.StringTokenizer;


public class PostInstallFixupAction extends ProductAction {
    public void build(ProductBuilderSupport support) {
    }
    
    public void install(ProductActionSupport support) throws ProductException {
        patchNbConfig(false);
    }
    
    public void uninstall(ProductActionSupport support) throws ProductException {
        String installDir = "";
        try {
            ProductService pservice = (ProductService)getService(ProductService.NAME);
            installDir = (String) pservice.getProductBeanProperty(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                null,
                "absoluteInstallLocation");
        } catch (ServiceException ex) {
            logEvent(this, Log.ERROR, ex);
        }
        logEvent(this, Log.DBG, "uninstall installDir: " + installDir);
        deleteFiles(installDir, new String[] {"_uninst" + File.separator + "install.log"});
        patchNbConfig(true);
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
                }
                else {
                    logEvent(this, Log.DBG, "cannot find " + filename);
                }
            }
        } catch (ServiceException ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    private void patchNbConfig(boolean reverse) {
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
            logEvent(this, Log.DBG, "patching " + configFilename);
            
            String[] content = fileService.readAsciiFile(configFilename);
            
            if (! reverse) {
                int index = -1;
                if (content != null ) {
                    for (int i = 0; i < content.length; i++) {
                        String line = content[i].trim();
                        if (line.startsWith("netbeans_extraclusters=") || line.startsWith("#netbeans_extraclusters=")) {
                            index = i;
                            break;
                        }
                    }
                }
                
                String line = "";
                
                if (index >= 0 && ! content[index].trim().startsWith("#")) {
                    StringTokenizer stok = new StringTokenizer(content[index], psep);
                    while (stok.hasMoreElements()) {
                        if (! "".equals(line))
                            line += psep;
                        line += stok.nextToken();
                    }
                }
                if (! "".equals(line))
                    line += psep;
                line += installDir;
                
                line = "netbeans_extraclusters=" + line;
                
                if (index >= 0) {
                    fileService.updateAsciiFile(configFilename, new String[] {line}, index);
                } else {
                    fileService.appendToAsciiFile(configFilename, new String[] {line});
                }
                
                logEvent(this, Log.DBG, "replace line " + line);
            } else {
                int index = -1;
                if (content != null ) {
                    for (int i = 0; i < content.length; i++) {
                        String line = content[i].trim();
                        if (line.startsWith("netbeans_extraclusters=")) {
                            index = i;
                            break;
                        }
                    }
                }
                
                if (index < 0)
                    return;
                
                File instdir = new File(installDir);
                String line = "";
                StringTokenizer stok = new StringTokenizer(content[index].substring("netbeans_extraclusters=".length()), psep);
                
                while (stok.hasMoreElements()) {
                    String tok = stok.nextToken();
                    if (instdir.equals(new File(tok)))
                        continue;
                    
                    if (! "".equals(line))
                        line += psep;
                    line += tok;
                }
                
                if (! "".equals(line)) {
                    line = "netbeans_extraclusters=" + line;
                }
                
                fileService.updateAsciiFile(configFilename, new String[] {line}, index);
                
                logEvent(this, Log.DBG, "replace line " + line);
            }
        }
        catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
}
