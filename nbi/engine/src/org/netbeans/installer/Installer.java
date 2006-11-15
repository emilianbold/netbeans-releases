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
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.wizard.components.actions.FinalizeRegistryAction;
import org.netbeans.installer.wizard.components.actions.InitalizeRegistryAction;
import static org.netbeans.installer.utils.helper.ErrorLevel.DEBUG;
import static org.netbeans.installer.utils.helper.ErrorLevel.MESSAGE;
import static org.netbeans.installer.utils.helper.ErrorLevel.WARNING;
import static org.netbeans.installer.utils.helper.ErrorLevel.ERROR;
import static org.netbeans.installer.utils.helper.ErrorLevel.CRITICAL;
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
        LogManager.log(MESSAGE, "initializing the installer engine");
        LogManager.indent();
        
        instance = this;
        
        dumpSystemInfo();
        
        parseArguments(arguments);
        
        setLookAndFeel();
        
        LogManager.unindent();
        LogManager.log(MESSAGE, "... finished initializing the installer engine");
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
                ErrorManager.notify(CRITICAL, "Cannot create local directory: " + localDirectory);
            }
        } else if (localDirectory.isFile()) {
            ErrorManager.notify(CRITICAL, "Local directory exists and is a file: " + localDirectory);
        } else if (!localDirectory.canRead()) {
            ErrorManager.notify(CRITICAL, "Cannot read local directory - not enought permissions");
        } else if (!localDirectory.canWrite()) {
            ErrorManager.notify(CRITICAL, "Cannot write to local directory - not enought permissions");
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
    
    /**
     * Cancels the installation. This method cancels the changes that were possibly
     * made to the components registry and exits with the cancel error code.
     *
     * @see #finish()
     * @see #criticalExit()
     */
    public void cancel() {
        if (!UiUtils.showYesNoDialog("Are you shure you want to cancel?")) {
            return;
        }
        
        // shut down everything that needs it
        DownloadManager.getInstance().shutdown();
        
        // exit with the cancel error code
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
        LogManager.log(MESSAGE, "parsing command-line arguments");
        LogManager.indent();
        
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equalsIgnoreCase("--look-and-feel")) {
                LogManager.log(MESSAGE, "parsing command line parameter \"--look-and-feel\"");
                LogManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    System.setProperty(NBI_LOOK_AND_FEEL_CLASS_NAME_PROPERTY, value);
                    
                    i = i + 1;
                    
                    LogManager.log(MESSAGE, "... class name: " + value);
                } else {
                    ErrorManager.notify(WARNING, "Required parameter missing for command line argument \"--look-and-feel\". Should be \"--look-and-feel <look-and-feel-class-name>\".");
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(TARGET_ARG)) {
                LogManager.log(MESSAGE, "parsing command line parameter \"" + TARGET_ARG + "\"");
                LogManager.indent();
                
                if (i < arguments.length - 2) {
                    String uid = arguments[i + 1];
                    String version = arguments[i + 2];
                    System.setProperty(ProductRegistry.TARGET_COMPONENT_UID_PROPERTY, uid);
                    System.setProperty(ProductRegistry.TARGET_COMPONENT_VERSION_PROPERTY, version);
                    
                    i = i + 2;
                    
                    LogManager.log(MESSAGE, "... uid:     " + uid);
                    LogManager.log(MESSAGE, "... version: " + version);
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--locale")) {
                LogManager.log(MESSAGE, "parsing command line parameter \"--locale\"");
                LogManager.indent();
                
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
                            ErrorManager.notify(WARNING, "Invalid parameter command line argument \"--locale\". Should be \"<language>[_<country>[_<variant>]]\".");
                    }
                    
                    if (targetLocale != null) {
                        Locale.setDefault(targetLocale);
                        LogManager.log(MESSAGE, "... locale set to: " + targetLocale);
                    } else {
                        LogManager.log(MESSAGE, "... locale is not set, using system default: " + Locale.getDefault());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.notify(WARNING, "Required parameter missing for command line argument \"--locale\". Should be \"--locale <locale-name>\".");
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--state")) {
                LogManager.log(MESSAGE, "parsing command line parameter \"--state\"");
                LogManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    
                    File stateFile = new File(value).getAbsoluteFile();
                    if (!stateFile.exists()) {
                        ErrorManager.notify(WARNING, "The specified state file \"" + stateFile + "\", does not exist. \"--state\" parameter is ignored.");
                    } else {
                        System.setProperty(ProductRegistry.SOURCE_STATE_FILE_PATH_PROPERTY, stateFile.getAbsolutePath());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.notify(WARNING, "Required parameter missing for command line argument \"--state\". Should be \"--state <state-file-path>\".");
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--record")) {
                LogManager.log(MESSAGE, "parsing command line parameter \"--record\"");
                LogManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    
                    File stateFile = new File(value).getAbsoluteFile();
                    if (stateFile.exists()) {
                        ErrorManager.notify(WARNING, "The specified state file \"" + stateFile + "\", exists. \"--record\" parameter is ignored.");
                    } else {
                        System.setProperty(ProductRegistry.TARGET_STATE_FILE_PATH_PROPERTY, stateFile.getAbsolutePath());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.notify(WARNING, "Required parameter missing for command line argument \"--record\". Should be \"--record <state-file-path>\".");
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--silent")) {
                LogManager.log(MESSAGE, "parsing command line parameter \"--silent\"");
                LogManager.indent();
                
                System.setProperty(Wizard.SILENT_MODE_ACTIVE_PROPERTY, "true");
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--create-bundle")) {
                LogManager.log(MESSAGE, "parsing command line parameter \"--create-bundle\"");
                LogManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    
                    File targetFile = new File(value).getAbsoluteFile();
                    if (targetFile.exists()) {
                        ErrorManager.notify(WARNING, "The specified target file \"" + targetFile + "\", exists. \"--create-bundle\" parameter is ignored.");
                    } else {
                        executionMode = InstallerExecutionMode.CREATE_BUNDLE;
                        System.setProperty(CREATE_BUNDLE_PATH_PROPERTY, targetFile.getAbsolutePath());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.notify(WARNING, "Required parameter missing for command line argument \"--create-bundle\". Should be \"--create-bundle <target-file-path>\".");
                }
                
                LogManager.unindent();
                continue;
            }
        }
        
        if (arguments.length == 0) {
            LogManager.log(MESSAGE, "... no command line arguments were specified");
        }
        
        validateArguments();
        
        LogManager.unindent();
        LogManager.log(MESSAGE, "... finished parsing command line arguments");
    }
    
    private void validateArguments() {
        if (System.getProperty(Wizard.SILENT_MODE_ACTIVE_PROPERTY) != null) {
            if (System.getProperty(ProductRegistry.SOURCE_STATE_FILE_PATH_PROPERTY) == null) {
                System.getProperties().remove(Wizard.SILENT_MODE_ACTIVE_PROPERTY);
                ErrorManager.notify(WARNING, "\"--state\" option is required when using \"--silent\". \"--silent\" will be ignored.");
            }
        }
    }
    
    private void setLookAndFeel() {
        LogManager.log(MESSAGE, "setting the look and feel");
        LogManager.indent();
        
        String className = System.getProperty(NBI_LOOK_AND_FEEL_CLASS_NAME_PROPERTY);
        if (className == null) {
            LogManager.log(MESSAGE, "custom look and feel class name was not specified, using system default");
            className = DEFAULT_NBI_LOOK_AND_FEEL_CLASS_NAME;
        }
        
        LogManager.log(MESSAGE, "... class name: " + className);
        
        try {
            JFrame.setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel(className);
        } catch (ClassNotFoundException e) {
            ErrorManager.notify(WARNING, "Could not set the look and feel.", e);
        } catch (InstantiationException e) {
            ErrorManager.notify(WARNING, "Could not set the look and feel.", e);
        } catch (IllegalAccessException e) {
            ErrorManager.notify(WARNING, "Could not set the look and feel.", e);
        } catch (UnsupportedLookAndFeelException e) {
            ErrorManager.notify(WARNING, "Could not set the look and feel.", e);
        }
        
        LogManager.unindent();
        LogManager.log(MESSAGE, "... finished setting the look and feel");
    }
    
    private void dumpSystemInfo() {
        LogManager.log(MESSAGE, "dumping target system information");
        LogManager.indent();
        
        LogManager.log(MESSAGE, "system properties:");
        LogManager.indent();
        
        Properties properties = System.getProperties();
        for (Object key: properties.keySet()) {
            LogManager.log(MESSAGE, key.toString() + " => " + properties.get(key).toString());
        }
        
        LogManager.unindent();
        LogManager.log(MESSAGE, "... end of system properties");
        
        LogManager.unindent();
        LogManager.log(MESSAGE, "... end of target system information");
    }
    
    private void initLocalDirectory() {
        LogManager.log(MESSAGE, "initializing the local directory");
        LogManager.indent();
        
        if (System.getProperty(LOCAL_DIRECTORY_PATH_PROPERTY) != null) {
            localDirectory = new File(System.getProperty(
                    LOCAL_DIRECTORY_PATH_PROPERTY)).getAbsoluteFile();
        } else {
            localDirectory = new File(DEFAULT_LOCAL_DIRECTORY_PATH).getAbsoluteFile();
            
            LogManager.log(MESSAGE, "... custom local directory was not specified, using the default");
            LogManager.log(MESSAGE, "... local directory: " + localDirectory);
        }
        
        LogManager.unindent();
        LogManager.log(MESSAGE, "... finished initializing local directory");
    }
    
    private void cacheEngineLocally() {
        LogManager.log(MESSAGE, "cache engine data locally to run uninstall in the future");
        LogManager.indent();
        try {
            String installerResource = "org/netbeans/installer/Installer.class";
            URL url = this.getClass().getClassLoader().getResource(installerResource);
            if(url == null) {
                throw new IOException("No manifest in the engine");
            }
            
            LogManager.log(DEBUG, "NBI Engine URL for Installer.Class = " + url);
            LogManager.log(DEBUG, "URL Path = " + url.getPath());
            
            if("jar".equals(url.getProtocol())) {
                // we run engine from jar, not from .class
                String path = url.getPath();
                String filePrefix = "file:";
                String httpPrefix = "http://";
                String jarSep = "!/";
                String name = "nbi-engine.jar";
                File dest = null;
                File jarfile = null;
                String jarLocation;
                if (path != null) {
                    if (path.startsWith(filePrefix)) {
                        if (path.indexOf(jarSep) != -1) {
                            jarLocation = path.substring(filePrefix.length(),
                                    path.indexOf(jarSep + installerResource));
                            jarLocation = URLDecoder.decode(jarLocation,"UTF8");
                            jarfile = new File(jarLocation);
                        } else {
                            // a quick hack to allow caching engine when run from
                            // the IDE (i.e. as a .class) - probably to be removed
                            // later. Or maybe not...
                            File root = new File(path.substring(filePrefix.length(),
                                    path.indexOf(installerResource)));
                            jarfile = new File(root, "dist/nbi-engine.jar");
                        }
                    } else if (path.startsWith(httpPrefix)) {
                        jarLocation = path.substring(
                                path.indexOf(httpPrefix),
                                path.indexOf(jarSep + installerResource));
                        try {
                            LogManager.log(MESSAGE,
                                    "Downloading engine jar file from " + jarLocation);
                            jarLocation = URLDecoder.decode(jarLocation,"UTF8");
                            jarfile = FileProxy.getInstance().getFile(jarLocation);
                        }  catch(DownloadException ex) {
                            LogManager.log(WARNING,
                                    "Could not download engine jar. \nError = " + ex );
                            jarfile = null;
                        }
                        
                    }
                    
                    dest = new File(getLocalDirectory().getPath() +
                            File.separator + name);
                    
                    if(jarfile!=null) {
                        if(!jarfile.getAbsolutePath().equals(dest.getAbsolutePath()) && jarfile.exists()) {
                            FileUtils.copyFile(jarfile,dest);
                        }
                    }
                    cachedEngine = (!dest.exists()) ? null : dest;
                    LogManager.log(MESSAGE, "NBI Engine jar file = [" +
                            cachedEngine + "], exist = " +
                            ((cachedEngine==null) ? false : cachedEngine.exists()));
                }
            }
            
            // a quick hack to allow caching engine when run from
            // the IDE (i.e. as a .class) - probably to be removed
            // later. Or maybe not...
            if("file".equals(url.getProtocol())) {
                String path = url.toString();
                String filePrefix = "file:";
                String name = "nbi-engine.jar";
                
                File root = new File(path.substring(filePrefix.length(),
                        path.indexOf("build/classes/" + installerResource)));
                
                File jarfile = new File(root, "dist/nbi-engine.jar");
                
                File dest = new File(getLocalDirectory().getPath() + File.separator + name);
                
                if(!jarfile.getAbsolutePath().equals(dest.getAbsolutePath()) && jarfile.exists()) {
                        FileUtils.copyFile(jarfile, dest);
                    }
                                
                cachedEngine = (!dest.exists()) ? null : dest;
                LogManager.log(MESSAGE, "NBI Engine jar file = [" +
                        cachedEngine + "], exist = " +
                        ((cachedEngine==null) ? false : cachedEngine.exists()));
            }
        } catch (IOException ex) {
            LogManager.log(CRITICAL, "can`t cache installer engine");
            LogManager.log(CRITICAL, ex);
        }
        LogManager.unindent();
        LogManager.log(MESSAGE, "... finished caching engine data");
    }
    
    private void createInstallerLockFile(File directory)  {
        LogManager.log(ErrorLevel.DEBUG, "    creating lock file in " + directory);
        File installerLock = new File(directory,
                ".nbilock");
        if(installerLock.exists()) {
            LogManager.log(ErrorLevel.WARNING, "    lock file already exists");
            if(!UiUtils.showYesNoDialog(
                    "It seems that another instance of installer is already running!\n" +
                    "It can be dangerous running another one in the same time.\n" + 
                    "Are you sure you want to run one more instance?\n\n")) {
                        criticalExit();
                    } 
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(installerLock);
        } catch (IOException ex) {
            ErrorManager.notify(CRITICAL,
                    "Can`t create lock for the local registry file!");
        } finally {
            try {
                installerLock.deleteOnExit();
                if(fos!=null) {
                    fos.close();
                }
            } catch (IOException ex) {
                LogManager.log(ErrorLevel.DEBUG,ex);
            }
        }
        LogManager.log(ErrorLevel.DEBUG,
                "    ... lock created " + installerLock);
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner classes
    public static enum InstallerExecutionMode {
        NORMAL,
        CREATE_BUNDLE;
    }
}
