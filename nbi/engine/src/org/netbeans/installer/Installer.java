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
 *
 * $Id$
 */
package org.netbeans.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.downloader.DownloaderConsts;
import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.utils.ErrorLevel;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.wizard.components.actions.FinalizeRegistryAction;
import org.netbeans.installer.wizard.components.actions.InitalizeRegistryAction;
import static org.netbeans.installer.utils.ErrorLevel.DEBUG;
import static org.netbeans.installer.utils.ErrorLevel.MESSAGE;
import static org.netbeans.installer.utils.ErrorLevel.WARNING;
import static org.netbeans.installer.utils.ErrorLevel.ERROR;
import static org.netbeans.installer.utils.ErrorLevel.CRITICAL;
import org.netbeans.installer.wizard.Wizard;

/**
 * The main class of the NBI framework. It represents the installer and
 * provides methods to start the installation/maintenance process as well as to
 * finish/cancel/break the installation.
 *
 * @author Kirill Sorokin
 */
public class Installer {
    /////////////////////////////////////////////////////////////////////////////////
    // Main
    /**
     * The main method. It gets an instance of <code>Installer</code> and calls the
     * <code>start</code> method, passing in the command line arguments.
     *
     * @param arguments The command line arguments
     * @see #start(String[])
     */
    public static void main(String[] arguments) {
        new Installer(arguments).start();
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_LOCAL_DIRECTORY_PATH =
            System.getProperty("user.home") + File.separator + ".nbi";
    
    public static final String LOCAL_DIRECTORY_PATH_PROPERTY =
            "nbi.local.directory.path";
    
    public static final String DEFAULT_NBI_LOOK_AND_FEEL_CLASS_NAME =
            UIManager.getSystemLookAndFeelClassName();
    
    public static final String NBI_LOOK_AND_FEEL_CLASS_NAME_PROPERTY =
            "nbi.look.and.feel";
    
    public static final String CREATE_BUNDLE_PATH_PROPERTY = 
            "nbi.create.bundle.path";
    
    /** Errorcode to be used at normal exit */
    public static final int NORMAL_ERRORCODE = 0;
    
    /** Errorcode to be used when the installer is canceled */
    public static final int CANCEL_ERRORCODE = 1;
    
    /** Errorcode to be used when the installer exits because of a critical error */
    public static final int CRITICAL_ERRORCODE = Integer.MAX_VALUE;
    
    public static final String TARGET_ARG = "--target";
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Installer instance;
    
    private static LogManager   logManager   = LogManager.getInstance();
    private static ErrorManager errorManager = ErrorManager.getInstance();
    
    /**
     * Returns an instance of <code>Installer</code>. If the instance does not
     * exist - it is created.
     *
     * @return An instance of <code>Installer</code>
     */
    public static synchronized Installer getInstance() {
        return instance;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private File localDirectory =
            new File(DEFAULT_LOCAL_DIRECTORY_PATH).getAbsoluteFile();
    
    private File cachedEngine;
    
    private InstallerExecutionMode executionMode = InstallerExecutionMode.NORMAL;
    
    // Constructor //////////////////////////////////////////////////////////////////
    /**
     * The only private constructor - we need to hide the default one as
     * <code>Installer is a singleton.
     */
    private Installer(String[] arguments) {
        logManager.log(MESSAGE, "initializing the installer engine");
        logManager.indent();
        
        dumpSystemInfo();
        
        parseArguments(arguments);
        
        setLookAndFeel();
        
        instance = this;
        
        logManager.unindent();
        logManager.log(MESSAGE, "... finished initializing the installer engine");
    }
    
    // Life cycle control methods ///////////////////////////////////////////////////
    /**
     * Starts the installer. This method parses the passed-in command line arguments,
     * initializes the wizard and the components registry.
     *
     * @param arguments The command line arguments
     */
    public void start() {
        if (!localDirectory.exists()) {
            if (!localDirectory.mkdirs()) {
                ErrorManager.getInstance().notify(CRITICAL, "Cannot create local directory: " + localDirectory);
            }
        } else if (localDirectory.isFile()) {
            ErrorManager.getInstance().notify(CRITICAL, "Local directory exists and is a file: " + localDirectory);
        } else if (!localDirectory.canRead()) {
            ErrorManager.getInstance().notify(CRITICAL, "Cannot read local directory - not enought permissions");
        } else if (!localDirectory.canWrite()) {
            ErrorManager.getInstance().notify(CRITICAL, "Cannot write to local directory - not enought permissions");
        }
        createInstallerLockFile(localDirectory);
        
        DownloaderConsts.setWorkingDirectory(new File(localDirectory, "wd"));
        DownloaderConsts.setOutputDirectory(new File(localDirectory, "downloads"));
        DownloadManager.getInstance().start();
        
        final Wizard wizard = Wizard.getInstance();
        
        wizard.open();
        wizard.executeComponent(new InitalizeRegistryAction());
        wizard.next();
        cacheEngineLocally();
    }
    
    private void createInstallerLockFile(File directory)  {
        LogManager.getInstance().log(ErrorLevel.DEBUG,
                "    creating lock file in " + directory);
        File installerLock = new File(directory,
                ".nbilock");
        if(installerLock.exists()) {
            ErrorManager.getInstance().notify(CRITICAL,
                    "It seems that another instance of installer is already running!\n" +
                    "If you are sure that no other instance is running just remove the file:\n" +
                    installerLock + "\n");
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(installerLock);
        } catch (IOException ex) {
            ErrorManager.getInstance().notify(CRITICAL,
                    "Can`t create lock for the local registry file!");
        } finally {
            try {
                installerLock.deleteOnExit();
                if(fos!=null) {
                    fos.close();
                }
            } catch (IOException ex) {
                LogManager.getInstance().log(ErrorLevel.DEBUG,ex);
            }
        }
        LogManager.getInstance().log(ErrorLevel.DEBUG,
                "    ... lock created " + installerLock);
    }
    /**
     * Cancels the installation. This method cancels the changes that were possibly
     * made to the components registry and exits with the cancel error code.
     *
     * @see #finish()
     * @see #criticalExit()
     */
    public void cancel() {
        // exit with the cancel error code
        DownloadManager.getInstance().shutdown();
        System.exit(CANCEL_ERRORCODE);
    }
    
    /**
     * Finishes the installation. This method finalizes the changes made to the
     * components registry and exits with a normal error code.
     *
     * @see #cancel()
     * @see #criticalExit()
     */
    public void finish() {
        final Wizard wizard = Wizard.getInstance();
        
        wizard.executeComponent(new FinalizeRegistryAction());
        wizard.close();
        DownloadManager.getInstance().shutdown();
        System.exit(NORMAL_ERRORCODE);
    }
    
    /**
     * Critically exists. No changes will be made to the components registry - it
     * will remain at the same state it was at the moment this method was called.
     *
     * @see #cancel()
     * @see #finish()
     */
    public void criticalExit() {
        // exit immediately, as the system is apparently in a crashed state
        DownloadManager.getInstance().shutdown();
        System.exit(CRITICAL_ERRORCODE);
    }
    
    // Getters //////////////////////////////////////////////////////////////////////
    public File getLocalDirectory() {
        return localDirectory;
    }
    
    public File getCachedEngine() {
        return cachedEngine;
    }
    
    public InstallerExecutionMode getExecutionMode() {
        return executionMode;
    }
    
    // Private stuff ////////////////////////////////////////////////////////////////
    /**
     * Parses the command line arguments passed to the installer. All unknown
     * arguments are ignored.
     *
     * @param arguments The command line arguments
     */
    private void parseArguments(String[] arguments) {
        logManager.log(MESSAGE, "parsing command-line arguments");
        logManager.indent();
        
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equalsIgnoreCase("--look-and-feel")) {
                logManager.log(MESSAGE, "parsing command line parameter \"--look-and-feel\"");
                logManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    System.setProperty(NBI_LOOK_AND_FEEL_CLASS_NAME_PROPERTY, value);
                    
                    i = i + 1;
                    
                    logManager.log(MESSAGE, "... class name: " + value);
                } else {
                    ErrorManager.getInstance().notify(WARNING, "Required parameter missing for command line argument \"--look-and-feel\". Should be \"--look-and-feel <look-and-feel-class-name>\".");
                }
                
                logManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(TARGET_ARG)) {
                logManager.log(MESSAGE, "parsing command line parameter \"" + TARGET_ARG + "\"");
                logManager.indent();
                
                if (i < arguments.length - 2) {
                    String uid = arguments[i + 1];
                    String version = arguments[i + 2];
                    System.setProperty(ProductRegistry.TARGET_COMPONENT_UID_PROPERTY, uid);
                    System.setProperty(ProductRegistry.TARGET_COMPONENT_VERSION_PROPERTY, version);
                    
                    i = i + 2;
                    
                    logManager.log(MESSAGE, "... uid:     " + uid);
                    logManager.log(MESSAGE, "... version: " + version);
                }
                
                logManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--locale")) {
                logManager.log(MESSAGE, "parsing command line parameter \"--locale\"");
                logManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    String[] valueParts = value.split("_");
                    
                    Locale targetLocale = null;
                    switch (valueParts.length) {
                        case 1:
                            targetLocale = new Locale(valueParts[0]);
                            break;
                        case 2:
                            targetLocale = new Locale(valueParts[0], valueParts[1]);
                            break;
                        case 3:
                            targetLocale = new Locale(valueParts[0], valueParts[1], valueParts[2]);
                            break;
                        default:
                            ErrorManager.getInstance().notify(WARNING, "Invalid parameter command line argument \"--locale\". Should be \"<language>[_<country>[_<variant>]]\".");
                    }
                    
                    if (targetLocale != null) {
                        Locale.setDefault(targetLocale);
                        logManager.log(MESSAGE, "... locale set to: " + targetLocale);
                    } else {
                        logManager.log(MESSAGE, "... locale is not set, using system default: " + Locale.getDefault());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.getInstance().notify(WARNING, "Required parameter missing for command line argument \"--locale\". Should be \"--locale <locale-name>\".");
                }
                
                logManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--state")) {
                logManager.log(MESSAGE, "parsing command line parameter \"--state\"");
                logManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    
                    File stateFile = new File(value).getAbsoluteFile();
                    if (!stateFile.exists()) {
                        ErrorManager.getInstance().notify(WARNING, "The specified state file \"" + stateFile + "\", does not exist. \"--state\" parameter is ignored.");
                    } else {
                        System.setProperty(ProductRegistry.SOURCE_STATE_FILE_PATH_PROPERTY, stateFile.getAbsolutePath());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.getInstance().notify(WARNING, "Required parameter missing for command line argument \"--state\". Should be \"--state <state-file-path>\".");
                }
                
                logManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--record")) {
                logManager.log(MESSAGE, "parsing command line parameter \"--record\"");
                logManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    
                    File stateFile = new File(value).getAbsoluteFile();
                    if (stateFile.exists()) {
                        ErrorManager.getInstance().notify(WARNING, "The specified state file \"" + stateFile + "\", exists. \"--record\" parameter is ignored.");
                    } else {
                        System.setProperty(ProductRegistry.TARGET_STATE_FILE_PATH_PROPERTY, stateFile.getAbsolutePath());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.getInstance().notify(WARNING, "Required parameter missing for command line argument \"--record\". Should be \"--record <state-file-path>\".");
                }
                
                logManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--silent")) {
                logManager.log(MESSAGE, "parsing command line parameter \"--silent\"");
                logManager.indent();
                
                System.setProperty(Wizard.SILENT_MODE_ACTIVE_PROPERTY, "true");
                
                logManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--create-bundle")) {
                logManager.log(MESSAGE, "parsing command line parameter \"--create-bundle\"");
                logManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    
                    File targetFile = new File(value).getAbsoluteFile();
                    if (targetFile.exists()) {
                        ErrorManager.getInstance().notify(WARNING, "The specified target file \"" + targetFile + "\", exists. \"--create-bundle\" parameter is ignored.");
                    } else {
                        executionMode = InstallerExecutionMode.CREATE_BUNDLE;
                        System.setProperty(CREATE_BUNDLE_PATH_PROPERTY, targetFile.getAbsolutePath());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.getInstance().notify(WARNING, "Required parameter missing for command line argument \"--create-bundle\". Should be \"--create-bundle <target-file-path>\".");
                }
                
                logManager.unindent();
                continue;
            }
        }
        
        if (arguments.length == 0) {
            logManager.log(MESSAGE, "... no command line arguments were specified");
        }
        
        validateArguments();
        
        logManager.unindent();
        logManager.log(MESSAGE, "... finished parsing command line arguments");
    }
    
    private void validateArguments() {
        if (System.getProperty(Wizard.SILENT_MODE_ACTIVE_PROPERTY) != null) {
            if (System.getProperty(ProductRegistry.SOURCE_STATE_FILE_PATH_PROPERTY) == null) {
                System.getProperties().remove(Wizard.SILENT_MODE_ACTIVE_PROPERTY);
                ErrorManager.getInstance().notify(WARNING, "\"--state\" option is required when using \"--silent\". \"--silent\" will be ignored.");
            }
        }
    }
    
    private void setLookAndFeel() {
        logManager.log(MESSAGE, "setting the look and feel");
        logManager.indent();
        
        String className = System.getProperty(NBI_LOOK_AND_FEEL_CLASS_NAME_PROPERTY);
        if (className == null) {
            logManager.log(MESSAGE, "custom look and feel class name was not specified, using system default");
            className = DEFAULT_NBI_LOOK_AND_FEEL_CLASS_NAME;
        }
        
        logManager.log(MESSAGE, "... class name: " + className);
        
        try {
            JFrame.setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel(className);
        } catch (ClassNotFoundException e) {
            ErrorManager.getInstance().notify(WARNING, "Could not set the look and feel.", e);
        } catch (InstantiationException e) {
            ErrorManager.getInstance().notify(WARNING, "Could not set the look and feel.", e);
        } catch (IllegalAccessException e) {
            ErrorManager.getInstance().notify(WARNING, "Could not set the look and feel.", e);
        } catch (UnsupportedLookAndFeelException e) {
            ErrorManager.getInstance().notify(WARNING, "Could not set the look and feel.", e);
        }
        
        logManager.unindent();
        logManager.log(MESSAGE, "... finished setting the look and feel");
    }
    
    private void dumpSystemInfo() {
        logManager.log(MESSAGE, "dumping target system information");
        logManager.indent();
        
        logManager.log(MESSAGE, "system properties:");
        logManager.indent();
        
        Properties properties = System.getProperties();
        for (Object key: properties.keySet()) {
            logManager.log(MESSAGE, key.toString() + " => " + properties.get(key).toString());
        }
        
        logManager.unindent();
        logManager.log(MESSAGE, "... end of system properties");
        
        logManager.unindent();
        logManager.log(MESSAGE, "... end of target system information");
    }
    
    private void initLocalDirectory() {
        logManager.log(MESSAGE, "initializing the local directory");
        logManager.indent();
        
        if (System.getProperty(LOCAL_DIRECTORY_PATH_PROPERTY) != null) {
            localDirectory = new File(System.getProperty(
                    LOCAL_DIRECTORY_PATH_PROPERTY)).getAbsoluteFile();
        } else {
            localDirectory = new File(DEFAULT_LOCAL_DIRECTORY_PATH).getAbsoluteFile();
            
            logManager.log(MESSAGE, "... custom local directory was not specified, using the default");
            logManager.log(MESSAGE, "... local directory: " + localDirectory);
        }
        
        logManager.unindent();
        logManager.log(MESSAGE, "... finished initializing local directory");
    }
    
    private void cacheEngineLocally() {
        logManager.log(MESSAGE, "cache engine data locally to run uninstall in the future");
        logManager.indent();
        try {
            String installerResource = "org/netbeans/installer/Installer.class";
            URL url = this.getClass().getClassLoader().getResource(installerResource);
            if(url == null) {
                throw new IOException("No manifest in the engine");
            }
            if("jar".equals(url.getProtocol())) {
                //we run engine from jar, not from .class
                String path = url.getPath();
                logManager.log(DEBUG, "NBI Engine URL for Installer.Class = " + url);
                logManager.log(DEBUG, "URL Path = " + url.getPath());
                String filePrefix = "file:";
                String httpPrefix = "http://";
                String jarResourceSep = "!/";
                String name = "nbi-engine.jar";
                File dest = null;
                File jarfile = null;
                if(path!=null) {
                    if(path.startsWith(filePrefix)) {
                        jarfile = new File(path.substring(filePrefix.length(),
                                path.indexOf(jarResourceSep + installerResource)));
                        
                    } else if(path.startsWith(httpPrefix)) {
                        String jarLocation = path.substring(
                                path.indexOf(httpPrefix),
                                path.indexOf(jarResourceSep + installerResource));
                        try {
                            logManager.log(MESSAGE,
                                    "Downloading engine jar file from " + jarLocation);
                            jarLocation = URLDecoder.decode(jarLocation,"UTF8");
                            jarfile = FileProxy.getInstance().getFile(jarLocation);
                        }  catch(DownloadException ex) {
                            logManager.log(WARNING,
                                    "Could not download engine jar. \nError = " + ex );
                            jarfile = null;
                        }
                        
                    }
                    
                    dest = new File(getLocalDirectory().getPath() +
                            File.separator + name);
                    
                    if(jarfile!=null) {                        
                        if(!jarfile.getAbsolutePath().equals(dest.getAbsolutePath()) && jarfile.exists()) {                            
                            FileUtils.getInstance().copyFile(jarfile,dest);
                        }
                    }
                    cachedEngine = (!dest.exists()) ? null : dest;
                    logManager.log(MESSAGE, "NBI Engine jar file = [" +
                            cachedEngine + "], exist = " +
                            ((cachedEngine==null) ? false : cachedEngine.exists()));
                }
            }
            
        } catch (IOException ex) {
            logManager.log(CRITICAL, "can`t cache installer engine");
            logManager.log(CRITICAL, ex);
        }
        logManager.unindent();
        logManager.log(MESSAGE, "... finished caching engine data");
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner classes
    public static enum InstallerExecutionMode {
        NORMAL,
        CREATE_BUNDLE;
    }
}
