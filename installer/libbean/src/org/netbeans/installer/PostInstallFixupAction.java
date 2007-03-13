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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

public class PostInstallFixupAction extends ProductAction {
    
    private String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
    
    private String nbInstallDir = null;
    private String rootInstallDir = null;
    private String binDir = null;
    private String configDir = null;
    private String uninstallDir = null;
    private String nbClusterDir = null;
    
    private String jdkHome = null;
    
    private FileService fileService;
    private String psep;
    private String sep;
    
    private static final String BUNDLE = "$L(org.netbeans.installer.Bundle,";

    public void build(ProductBuilderSupport support) {
        try {
            support.putClass(Util.class.getName());
            support.putClass("org.netbeans.installer.PostInstallFixupAction$LnHandleOutput");
            support.putRequiredService(FileService.NAME);
            support.putRequiredService(ProductService.NAME);
            support.putRequiredService(Win32RegistryService.NAME);
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    public void init(ProductActionSupport support) throws ProductException {
        try {
            // need to get absoluteInstallLocation because uninstaller doesn't know the system properties.
            fileService = (FileService)getServices().getService(FileService.NAME);
            ProductService pservice = (ProductService)getService(ProductService.NAME);
            
            psep = fileService.getPathSeparator();
            sep = fileService.getSeparator();
            rootInstallDir = (String) pservice.getProductBeanProperty(productURL,null,"absoluteInstallLocation");
            if (Util.isMacOSX()) {
                nbInstallDir = rootInstallDir + sep 
                + resolveString(BUNDLE + "Product.nbLocationBelowInstallRoot)");
            } else {
                nbInstallDir = rootInstallDir;
            }
        } catch (Exception e) {
            logEvent(this, Log.ERROR, e);
        }
        
        logEvent(this, Log.DBG,"nbInstallDir is " + nbInstallDir);
        
        binDir = nbInstallDir + sep + "bin";
        configDir = nbInstallDir + sep + "etc";
        uninstallDir = rootInstallDir + sep + "_uninst";
        nbClusterDir = resolveString(BUNDLE + "NetBeans.nbClusterDir)");
    }
    
    public void install(ProductActionSupport support) throws ProductException {
        logEvent(this, Log.DBG,"install, support is " + support +" ...");
        
        init(support);
        // XXX - Watch out for this with detecting previous jdk on Windows
        // only needed for installs, not uninstalls
        String tmpJdkHome = Util.getInstalledJdk();
        if (tmpJdkHome != null && tmpJdkHome.length() > 0) {
            jdkHome = tmpJdkHome;
        } else {
            jdkHome = Util.getJdkHome();
        }
        logEvent(this, Log.DBG, "jdkHome = " + jdkHome);
        
        //This file is used by IDE Update Center.
        String productID = Util.getStringPropertyValue("ProductID");
        if (productID != null) {
            try {
                String fileName = nbInstallDir + sep + nbClusterDir + sep + "config" + sep + "productid";
                logEvent(this, Log.DBG, "create file: " + fileName + " content: '" + productID + "'");
                fileService.createAsciiFile(fileName,new String[] { productID });
            } catch (Exception ex) {
                logEvent(this, Log.ERROR, ex);
            }
        }
        //Create file 'license_accepted'. It gives info to IDE that user
        //accepted license during installation
        try {
            String dir = nbInstallDir + sep + nbClusterDir + sep + "var";
            if (!fileService.fileExists(dir)) {
                fileService.createDirectory(dir);
            } else if (!fileService.isDirectory(dir)) {
                logEvent(this, Log.WARNING, "File: " + dir + " already exists but is not directory.");
            }
            String fileName = nbInstallDir + sep + nbClusterDir + sep + "var" + sep + "license_accepted";
            logEvent(this, Log.DBG, "create file: " + fileName);
            fileService.createBinaryFile(fileName,new byte[0]);
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
        
        addJDKHomeToIDEConfigFile();
        
        if (Util.isMacOSX()) {
            createSymbolicLink();
        }
        if (Util.isUnixOS()) {
            installGnomeIcon();
        }
        if (Util.isWindowsOS()) {
            registerOpenWith();
        }
        deleteUnusedFiles();
    }
    
    public void uninstall(ProductActionSupport support) throws ProductException {
        logEvent(this, Log.DBG,"uninstall, support is " + support +" ...");
        
        try {
            init(support);
            
            deleteFiles(nbInstallDir, new String[] {"etc" + sep + "netbeans.conf"});
            deleteFiles(nbInstallDir, new String[] {"etc" + sep + "netbeans.clusters"});
            deleteFiles(uninstallDir, new String[] {"install.log"});
            deleteFiles(nbInstallDir, new String[] {nbClusterDir + sep + "config" + sep + "productid" });
            deleteFiles(nbInstallDir, new String[] {nbClusterDir + sep + "var" + sep + "license_accepted" });
            //Delete only this dir and only if empty
            String dirName = nbInstallDir + sep + nbClusterDir + sep + "var";
            logEvent(this, Log.DBG, "Trying to delete: " + dirName);
            fileService.deleteDirectory(dirName,true,false);
            
            SystemUtilService systemUtilService = (SystemUtilService) getServices().getService(SystemUtilService.NAME);
            if (Util.isMacOSX()) {
                deleteSymbolicLink();
                
                dirName = rootInstallDir + sep + "Contents/MacOS";
                logEvent(this, Log.DBG, "uninstall Delete dir on exit: " + dirName);
                systemUtilService.deleteDirectoryOnExit(dirName,false);

                dirName = rootInstallDir + sep + "Contents";
                logEvent(this, Log.DBG, "uninstall Delete dir on exit: " + dirName);
                systemUtilService.deleteDirectoryOnExit(dirName,false);
            }
            if (Util.isUnixOS()) {
                uninstallGnomeIcon();
            }
            
            logEvent(this, Log.DBG, "uninstall Delete install dir on exit: " + rootInstallDir);
            systemUtilService.deleteDirectoryOnExit(rootInstallDir,false);
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    public void replace(ProductAction oldAction, ProductActionSupport support) throws ProductException {
        logEvent(this, Log.DBG,"replace, oldAction is " + oldAction +", support is " + support + " ...");
        
        // TODO might be modify config file
    }

    /** Create symbolic link for NetBeans start script on Mac OS X. */
    private void createSymbolicLink () {
        // Create the command to exec by looking for ln in /bin and /usr/bin
        String target = "../Resources/NetBeans/bin/netbeans";
        String linkName = rootInstallDir + sep + "Contents/MacOS/netbeans";
        try {
            //Create dirs for symbolic link if they do not exist already
            String dir = rootInstallDir + sep + "Contents";
            if (!fileService.fileExists(dir)) {
                logEvent(this, Log.DBG, "Creating directory: " + dir);
                fileService.createDirectory(dir);
            }
            dir = rootInstallDir + sep + "Contents/MacOS";
            if (!fileService.fileExists(dir)) {
                logEvent(this, Log.DBG, "Creating directory: " + dir);
                fileService.createDirectory(dir);
            }
            
            String command = null;
            String[] args = { "-s", target, linkName };
            if (fileService.fileExists("/bin/ln")) {
                command = "/bin/ln";
            } else if (fileService.fileExists("/usr/bin/ln")) {
                command = "/usr/bin/ln";
            } else {
                logEvent(this, Log.ERROR, "Cannot find 'ln' command. => Cannot create symbolic link.");
                return;
            }

            logEvent(this, Log.DBG, "Running Symbolic Link Command: " + command);

            // exec the process
            ProcessExec ps = new ProcessExec(command, args);
            ps.setProcessOutputHandler(new LnHandleOutput());
            try {
                ps.executeProcess();
            } catch (ProcessExecException pe) {
                logEvent(this, Log.ERROR, "Could not exec ln process.");
            }
            logEvent(this, Log.DBG, "Symbolic link created Target: " + target
            + " Link: " + linkName);
        } catch (ServiceException se) {
            logEvent(this, Log.ERROR, se);
        }
    }
    
    private void deleteSymbolicLink () {
        String linkName = rootInstallDir + sep + "Contents/MacOS/netbeans";
        try {
            if (fileService.fileExists(linkName)) {
                fileService.deleteFile(linkName);
            }
        } catch (ServiceException se) {
            logEvent(this, Log.ERROR, "Cannot delete symbolic link: " + linkName);
            Util.logStackTrace(this,se);
            return;
        }
        logEvent(this, Log.DBG, "Symbolic link deleted Link: " + linkName);
    }
    
    private void addJDKHomeToIDEConfigFile() {
        try {
            String configFilename = configDir + sep + "netbeans.conf";
            logEvent(this, Log.DBG, "patching " + configFilename);
            
            int whereIsComment = -1;
            int whereToReplace = -1;
            String newLine = "netbeans_jdkhome=\"" + jdkHome + "\"";
            
            String[] content = fileService.readAsciiFile(configFilename);
            if (content != null ) {
                for (int i = 0; i < content.length; i++) {
                    if (content[i].trim().startsWith("netbeans_jdkhome")) {
                        whereToReplace = i;
                        break;
                    }
                    if (content[i].trim().startsWith("#netbeans_jdkhome")) {
                        whereIsComment = i;
                    }
                }
            }
            
            if (whereToReplace < 0) {
                logEvent(this, Log.DBG, "not found netbeans_jdkhome");
                
                if (whereIsComment >= 0) {
                    fileService.updateAsciiFile(configFilename, new String[] {newLine}, whereIsComment);
                }
                else {
                    fileService.appendToAsciiFile(configFilename, new String[] {newLine});
                }
                return;
            }
            
            String line = content[whereToReplace].trim();
            logEvent(this, Log.DBG, "replace line "+line);
            fileService.updateAsciiFile(configFilename, new String[] {newLine}, whereToReplace);
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    private void registerOpenWith() {
        try {
            logEvent(this, Log.DBG,"Checking Win32 Registry ... ");
            Win32RegistryService rs = (Win32RegistryService)getService(Win32RegistryService.NAME);
            int HKCR = Win32RegistryService.HKEY_CLASSES_ROOT;
            String HKEY_nb = "Applications\\netbeans.exe";
            if (!rs.keyExists(HKCR,"Applications\\netbeans.exe")) {
                logEvent(this, Log.DBG,"Creating key [Applications\\netbeans.exe]");
                rs.createKey(HKCR,"Applications","netbeans.exe");
            } else {
                logEvent(this, Log.DBG,"Key [Applications\\netbeans.exe] already exists");
            }
            if (!rs.keyExists(HKCR,"Applications\\netbeans.exe\\shell")) {
                logEvent(this, Log.DBG,"Creating [Applications\\netbeans.exe\\shell]");
                rs.createKey(HKCR,"Applications\\netbeans.exe","shell");
            } else {
                logEvent(this, Log.DBG,"Key [Applications\\netbeans.exe\\shell] already exists");
            }
            if (!rs.keyExists(HKCR,"Applications\\netbeans.exe\\shell\\open")) {
                logEvent(this, Log.DBG,"Creating key [Applications\\netbeans.exe\\shell\\open]");
                rs.createKey(HKCR,"Applications\\netbeans.exe\\shell","open");
            } else {
                logEvent(this, Log.DBG,"Key [Applications\\netbeans.exe\\shell\\open] already exists");
            }
            if (!rs.keyExists(HKCR,"Applications\\netbeans.exe\\shell\\open\\command")) {
                logEvent(this, Log.DBG,"Creating key [Applications\\netbeans.exe\\shell\\open\\command]");
                rs.createKey(HKCR,"Applications\\netbeans.exe\\shell\\open","command");
            } else {
                logEvent(this, Log.DBG,"Key [Applications\\netbeans.exe\\shell\\open\\command] already exists");
            }
            //Set values
            if (rs.keyExists(HKCR,"Applications\\netbeans.exe\\shell\\open")) {
                logEvent(this, Log.DBG,"Set default value for [Applications\\netbeans.exe\\shell\\open] \"FriendlyAppName\"");
                rs.setStringValue(HKCR,"Applications\\netbeans.exe\\shell\\open","FriendlyAppName",false,"NetBeans IDE");
            }
            if (rs.keyExists(HKCR,"Applications\\netbeans.exe\\shell\\open\\command")) {
                logEvent(this, Log.DBG,"Set default value for [Applications\\netbeans.exe\\shell\\open\\command]");
                rs.setStringValue(HKCR,"Applications\\netbeans.exe\\shell\\open\\command","",false,
                "\"" + binDir + sep + "netbeans.exe\" --open \"%1\"");
            }
            //Text types
            setKey(rs,".java",true,"text");
            setKey(rs,".dtd",true,"text");
            setKey(rs,".xml",true,"text");
            setKey(rs,".form",true,"text");
            setKey(rs,".properties",true,"text");
            setKey(rs,".htm",true,"text");
            setKey(rs,".html",true,"text");
            setKey(rs,".xsl",true,"text");
            setKey(rs,".css",true,"text");
            setKey(rs,".jsp",true,"text");
            //Image types
            setKey(rs,".gif",true,"image");
            setKey(rs,".jpg",true,"image");
            setKey(rs,".jpeg",true,"image");
            setKey(rs,".png",true,"image");
            //Others
            setKey(rs,".ear",false,"");
            setKey(rs,".har",false,"");
            setKey(rs,".jar",false,"");
            setKey(rs,".war",false,"");
            setKey(rs,".zip",false,"");
            setKey(rs,".nbm",false,"");
            setKey(rs,".class",false,"");
            setKey(rs,".ser",false,"");
            setKey(rs,".url",false,"");
            setKey(rs,".pdf",false,"");
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
            Util.logStackTrace(this,ex);
        }
    }
    
    private void setKey (Win32RegistryService rs, String key, boolean setPerceivedType, String perceivedType) throws ServiceException {
        int HKCR = Win32RegistryService.HKEY_CLASSES_ROOT;
        if (!rs.keyExists(HKCR,key)) {
            logEvent(this, Log.DBG,"Creating key [" + key + "]");
            rs.createKey(HKCR,"",key);
        } else {
            logEvent(this, Log.DBG,"Key [" + key + "] already exists");
        }
        if (setPerceivedType && rs.keyExists(HKCR,key)) {
            if (!rs.valueExists(HKCR,key,"PerceivedType")) {
                logEvent(this, Log.DBG,"Set value for " + key + " \"PerceivedType\" to \"" + perceivedType + "\"");
                rs.setStringValue(HKCR,key,"PerceivedType",false,perceivedType);
            } else {
                logEvent(this, Log.DBG,"Value for [" + key + "] \"PerceivedType\" is already set");
            }
        }

        String keyOWL = "OpenWithList";
        if (!rs.keyExists(HKCR,key + "\\" + keyOWL)) {
            logEvent(this, Log.DBG,"Creating key [" + key + "\\" + keyOWL + "]");
            rs.createKey(HKCR,key,keyOWL);
        } else {
            logEvent(this, Log.DBG,"Key [" + key + "\\" + keyOWL + "] already exists");
        }

        String keyNB = "netbeans.exe";
        if (!rs.keyExists(HKCR, key + "\\" + keyOWL + "\\" + keyNB)) {
            logEvent(this, Log.DBG,"Creating key [" + key + "\\" + keyOWL + "\\" + keyNB + "]");
            rs.createKey(HKCR, key + "\\" + keyOWL, keyNB);
        } else {
            logEvent(this, Log.DBG,"Key [" + key + "\\" + keyOWL + "\\" + keyNB + "] already exists");
        }
    }
    
    /** Delete unneeded files after installation.
     */
    public void deleteUnusedFiles() {
        try {
            fileService.deleteFile(rootInstallDir + sep + "netbeans.desktop");
            
            String[] binFiles = new String [20];
            int i = -1;
            if (!Util.isWindowsOS()) {
                binFiles[++i] = "nb.exe";
                binFiles[++i] = "netbeans.exe";
            }
            if (Util.isWindowsOS()) {
                binFiles[++i] = "netbeans";
            }
            binFiles[++i] = null;
            deleteFiles(binDir, binFiles);
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    public void deleteFiles(String dir, String[] fileNames) {
        for (int i = 0; i < fileNames.length; i++) {
            if (fileNames[i] == null) { //array bigger than num objs in array.
                return;
            }
            String filename = dir + sep + fileNames[i];
            try {
                if (fileService.fileExists(filename)) {
                    logEvent(this, Log.DBG, "deleting " + filename);
                    fileService.deleteFile(filename);
                } else {
                    logEvent(this, Log.DBG, "cannot find " + filename);
                }
            }
            catch (ServiceException ex) {
                logEvent(this, Log.ERROR, ex);
            }
        }
    }
    
    private static final String GNOMEAPPDIR = "/usr/share/applications";
    
    private void installGnomeIcon() {
        java.io.File appDir = new java.io.File(GNOMEAPPDIR);
        
        if (!appDir.exists()) {
            return;
        }
        
        try {
            String iconDir = null;
            String menuDir = null;
            
            if (appDir.canWrite()) {
                iconDir = GNOMEAPPDIR;
            } else {
                String desktopDir;
                desktopDir = resolveString("$D(home)$J(file.separator)Desktop");
                if (! fileService.fileExists(desktopDir)) {
                    fileService.createDirectory(desktopDir);
                }
                iconDir = desktopDir;
                
                desktopDir = resolveString
                ("$D(home)$J(file.separator).local$J(file.separator)share$J(file.separator)applications");
                if (! fileService.fileExists(desktopDir)) {
                    fileService.createDirectory(desktopDir);
                }
                menuDir = desktopDir;
            }
            
            //Icon file in install dir
            String iconFileOrig = nbInstallDir + sep + "netbeans.desktop";
            
            //Patch desktop file, replace templates with actual values
            logEvent(this, Log.DBG,"Patch desktop file: " + iconFileOrig);
            String[] content = fileService.readAsciiFile(iconFileOrig);
            if (content == null) {
                return;
            }
            String desktopIconName = resolveString(BUNDLE + "Product.desktopIconName)");
            String productName = resolveString(BUNDLE + "Product.displayName)");
            for (int i = 0; i < content.length; i++) {
                content[i] = content[i].replaceAll("@absoluteInstallLocation@", nbInstallDir);
                content[i] = content[i].replaceAll("@desktopIconName@", desktopIconName);
                content[i] = content[i].replaceAll("@productName@", productName);
                content[i] = content[i].replaceAll("@nbClusterDir@", nbClusterDir);
            }
            fileService.updateAsciiFile(iconFileOrig, content, 0);
            
            String iconFileDest;
            iconFileDest = iconDir + sep
            + resolveString(BUNDLE + "Product.desktopFileName)");
            logEvent(this, Log.DBG,"Copy desktop file to: " + iconFileDest);
            fileService.copyFile(iconFileOrig, iconFileDest, true);
            if (menuDir != null) {
                iconFileDest = menuDir + sep
                + resolveString(BUNDLE + "Product.desktopFileName)");
                logEvent(this, Log.DBG,"Copy desktop file to: " + iconFileDest);
                fileService.copyFile(iconFileOrig, iconFileDest, true);
            }
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    private void uninstallGnomeIcon() {
        java.io.File appDir = new java.io.File(GNOMEAPPDIR);
        
        if (!appDir.exists()) {
            return;
        }
        
        try {
            String iconDir = null;
            String menuDir = null;
            if (appDir.canWrite()) {
                iconDir = GNOMEAPPDIR;
            } else {
                iconDir = resolveString("$D(home)$J(file.separator)Desktop");
                menuDir = resolveString
                ("$D(home)$J(file.separator).local$J(file.separator)share$J(file.separator)applications");
            }
            
            String iconFile;
            iconFile = iconDir + sep
            + resolveString(BUNDLE + "Product.desktopFileName)");
            if (fileService.fileExists(iconFile)) {
                logEvent(this, Log.DBG,"Delete desktop file: " + iconFile);
                fileService.deleteFile(iconFile);
            }
            if (menuDir != null) {
                iconFile = menuDir + sep
                + resolveString(BUNDLE + "Product.desktopFileName)");
                if (fileService.fileExists(iconFile)) {
                    logEvent(this, Log.DBG,"Delete desktop file: " + iconFile);
                    fileService.deleteFile(iconFile);
                }
            }
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }
    
    /**
     * Inner helper class which performs output handling for the ISMP ProcessExec Class
     */
    private class LnHandleOutput implements ProcessOutputHandler {
        public void processOutputData(java.io.InputStream ipStream) {

        }
        public void processErrorData(java.io.InputStream ipStream){
            try {
                InputStreamReader isr = new InputStreamReader(ipStream);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    logEvent(this, Log.WARNING, line);
                }
            } catch (IOException ioe) {
                PostInstallFixupAction.this.logEvent(this, Log.ERROR, "Reading of ln output failed");
            }

        }
    }
}
