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
     * Update etc/netbeans.conf is necessary
     *
     * @param reverse whether cluster entry should be removed or added
     * @author Anton Chechel
     */
    private void patchNbConfig(boolean reverse) throws ServiceException {
        FileService fileService = (FileService) getServices().getService(FileService.NAME);
        ProductService pservice = (ProductService)getService(ProductService.NAME);
        
        String fsep = fileService.getSeparator();
        String psep = fileService.getPathSeparator();
        
        String installDir = resolveString((String)pservice.getProductBeanProperty(
                ProductService.DEFAULT_PRODUCT_SOURCE, null, "absoluteInstallLocation"));
        
        String nbdir = resolveString(installDir + fsep + "..");
        
        StringBuffer cf = new StringBuffer();
        cf.append(nbdir);
        cf.append(fsep);
        cf.append("etc");
        cf.append(fsep);
        cf.append("netbeans.conf");
        
        String configFilename = cf.toString();
        logEvent(this, Log.DBG, "patching " + configFilename);
        
        String[] content = fileService.readAsciiFile(configFilename);
        
        if (! reverse) { // add new cluster entry
            StringBuffer lineToAdd = new StringBuffer();
            if (null != content) {
                boolean beenUpdated = false;
                for (int i = 0; i < content.length; i++) {
                    Matcher matcher = Pattern.compile("(#*)\\s*netbeans_extraclusters\\s*=\\s*\\\"([^\\\"]*)\\\"").matcher(content[i]);
                    if (matcher.find()) {
                        if (!beenUpdated) {
                            logEvent(this, Log.DBG, "netbeans_extraclusters found");
                            logEvent(this, Log.DBG, "value: " + matcher.group());
                            
                            if (matcher.group(1).length() == 0) { // uncommented line
                                lineToAdd.append("netbeans_extraclusters=");
                                lineToAdd.append('"');
                                if (matcher.group(2).length() > 0) {
                                    lineToAdd.append(matcher.group(2));
                                    lineToAdd.append(psep);
                                }
                                lineToAdd.append(installDir);
                                lineToAdd.append('"');
                                lineToAdd.append('\n');
                                
                                fileService.updateAsciiFile(configFilename, new String[] {lineToAdd.toString()}, i);
                                logEvent(this, Log.DBG, "update line: " + lineToAdd);
                                beenUpdated = true;
                                break;
                            } else { // commented line
                                logEvent(this, Log.DBG, "commented line skiped");
                            }
                        }
                    }
                }
                
                if (!beenUpdated) { // no uncommented line was found
                    lineToAdd.append('\n');
                    lineToAdd.append("netbeans_extraclusters=");
                    lineToAdd.append('"');
                    lineToAdd.append(installDir);
                    lineToAdd.append('"');
                    lineToAdd.append('\n');
                    
                    logEvent(this, Log.DBG, "matcher was not found");
                    fileService.appendToAsciiFile(configFilename, new String[] {lineToAdd.toString()});
                    logEvent(this, Log.DBG, "append line: " + lineToAdd);
                }
            } else { // empty file
                lineToAdd.append('\n');
                lineToAdd.append("netbeans_extraclusters=");
                lineToAdd.append('"');
                lineToAdd.append(installDir);
                lineToAdd.append('"');
                lineToAdd.append('\n');
                
                logEvent(this, Log.DBG, "config file is empty");
                fileService.appendToAsciiFile(configFilename, new String[] {lineToAdd.toString()});
                logEvent(this, Log.DBG, "append line: " + lineToAdd);
            }
        } else { // remove cluster entry
            if (null == content) { // empty file
                logEvent(this, Log.DBG, "there is nothing to patch");
                return;
            }
            
            for (int i = 0; i < content.length; i++) { // update any line which contains our cluster path
                int index = content[i].indexOf(installDir);
                if (index != -1) {
                    logEvent(this, Log.DBG, installDir + " entry found");
                    logEvent(this, Log.DBG, "value: " + content[i]);
                    
                    String line = content[i].replaceAll(psep + '?' + prepareString(installDir), "");
                    if (line.matches("\\s*netbeans_extraclusters\\s*=\\s*\\\"{2}")) { // remove empty uncommented entries
                        line = "";
                    }
                    
                    fileService.updateAsciiFile(configFilename, new String[] {line}, i);
                    logEvent(this, Log.DBG, "update line: " + line);
                } else {
                    logEvent(this, Log.DBG, "nothing to update"); // no any entry
                }
            }
        }
        logEvent(this, Log.DBG, "update end");
    }
    
    private String prepareString(String str) {
        char[] c = str.toCharArray();
        List l = new ArrayList(c.length);
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\\' || c[i] == '.' || c[i] == '+') {
                l.add(new Character('\\'));
            }
            l.add(new Character(c[i]));
        }
        char[] cc = new char[l.size()];
        for (int i = 0; i < cc.length; i++) {
            cc[i] = ((Character) l.get(i)).charValue();
        }
        return new String(cc);
    }
    
//    private void patchNbConfig(boolean reverse) {
//        try {
//            FileService fileService = (FileService) getServices().getService(FileService.NAME);
//            ProductService pservice = (ProductService)getService(ProductService.NAME);
//
//            String fsep = fileService.getSeparator();
//            String psep = fileService.getPathSeparator();
//
//            String installDir = resolveString((String)pservice.getProductBeanProperty(
//                    ProductService.DEFAULT_PRODUCT_SOURCE,
//                    null,
//                    "absoluteInstallLocation"));
//
//            String nbdir = resolveString(installDir + fsep + "..");
//
//            String configFilename = nbdir + fsep + "etc" + fsep + "netbeans.conf";
//            logEvent(this, Log.DBG, "patching " + configFilename);
//
//            String[] content = fileService.readAsciiFile(configFilename);
//
//            if (! reverse) {
//                int index = -1;
//                if (content != null ) {
//                    for (int i = 0; i < content.length; i++) {
//                        String line = content[i].trim();
//                        if (line.startsWith("netbeans_extraclusters=") || line.startsWith("#netbeans_extraclusters=")) {
//                            index = i;
//                            break;
//                        }
//                    }
//                }
//
//                String line = "";
//
//                if (index >= 0 && ! content[index].trim().startsWith("#")) {
//                    StringTokenizer stok = new StringTokenizer(content[index], psep);
//                    while (stok.hasMoreElements()) {
//                        if (! "".equals(line))
//                            line += psep;
//                        line += stok.nextToken();
//                    }
//                }
//                if (! "".equals(line))
//                    line += psep;
//                line += installDir;
//
//                line = "netbeans_extraclusters=" + line;
//
//                if (index >= 0) {
//                    fileService.updateAsciiFile(configFilename, new String[] {line}, index);
//                } else {
//                    fileService.appendToAsciiFile(configFilename, new String[] {line});
//                }
//
//                logEvent(this, Log.DBG, "replace line " + line);
//            } else {
//                int index = -1;
//                if (content != null ) {
//                    for (int i = 0; i < content.length; i++) {
//                        String line = content[i].trim();
//                        if (line.startsWith("netbeans_extraclusters=")) {
//                            index = i;
//                            break;
//                        }
//                    }
//                }
//
//                if (index < 0)
//                    return;
//
//                File instdir = new File(installDir);
//                String line = "";
//                StringTokenizer stok = new StringTokenizer(content[index].substring("netbeans_extraclusters=".length()), psep);
//
//                while (stok.hasMoreElements()) {
//                    String tok = stok.nextToken();
//                    if (instdir.equals(new File(tok)))
//                        continue;
//
//                    if (! "".equals(line))
//                        line += psep;
//                    line += tok;
//                }
//
//                if (! "".equals(line)) {
//                    line = "netbeans_extraclusters=" + line;
//                }
//
//                fileService.updateAsciiFile(configFilename, new String[] {line}, index);
//
//                logEvent(this, Log.DBG, "replace line " + line);
//            }
//        } catch (Exception ex) {
//            logEvent(this, Log.ERROR, ex);
//        }
//    }
}

