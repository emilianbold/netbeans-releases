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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.DateUtils;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.helper.EngineResources;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.ExecutionMode;
import org.netbeans.installer.utils.helper.FinishHandler;
import org.netbeans.installer.utils.helper.UiMode;
import org.netbeans.installer.wizard.Wizard;
import static org.netbeans.installer.utils.StringUtils.LF;

/**
 * The main class of the NBI framework. It represents the installer and
 * provides methods to start the installation/maintenance process as well as to
 * finish/cancel/break the installation.
 *
 * @author Kirill Sorokin
 */
public class Installer implements FinishHandler {
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
        if (instance == null) {
            instance = new Installer(new String[0]);
        }
        
        return instance;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private File localDirectory =
            new File(DEFAULT_LOCAL_DIRECTORY_PATH).getAbsoluteFile();
    
    private File cachedEngine;
    
    // Constructor //////////////////////////////////////////////////////////////////
    /**
     * The only private constructor - we need to hide the default one as
     * <code>Installer is a singleton.
     */
    private Installer(String[] arguments) {
        LogManager.log("initializing the installer engine");
        LogManager.indent();
        
        // init the error manager
        ErrorManager.setFinishHandler(this);
        ErrorManager.setExceptionHandler(new ErrorManager.ExceptionHandler());
        
        // attach a handler for uncaught exceptions in the main thread
        Thread.currentThread().setUncaughtExceptionHandler(
                ErrorManager.getExceptionHandler());
        
        // check whether we can safely execute on the current platform, exit in
        // panic otherwise
        if (SystemUtils.getCurrentPlatform() == null) {
            ErrorManager.notifyCritical("The current platform is not supported.");
        }
        
        instance = this;
        
        dumpSystemInfo();
        
        loadProperties();
        parseArguments(arguments);
        setLocalDirectory();
        
        // once we have set the local directory we can start logging safely
        LogManager.setLogFile(new File(
                localDirectory,
                "log/" + DateUtils.getTimestamp() + ".log"));
        LogManager.start();
        
        setLookAndFeel();
        
        final DownloadManager downloadManager = DownloadManager.getInstance();
        downloadManager.setLocalDirectory(localDirectory);
        downloadManager.setFinishHandler(this);
        downloadManager.init();
        
        final Registry registry = Registry.getInstance();
        registry.setLocalDirectory(localDirectory);
        registry.setFinishHandler(this);
        
        final Wizard wizard = Wizard.getInstance();
        wizard.setFinishHandler(this);
        wizard.getContext().put(Registry.getInstance());
        
        cacheEngineLocally();
        
        createInstallerLockFile();
        
        LogManager.unindent();
        LogManager.log("... finished initializing the installer engine");
    }
    
    // Life cycle control methods ///////////////////////////////////////////////////
    /**
     * Starts the installer. This method parses the passed-in command line arguments,
     * initializes the wizard and the components registry.
     *
     * @param arguments The command line arguments
     */
    public void start() {
        Wizard.getInstance().open();
    }
    
    /**
     * Cancels the installation. This method cancels the changes that were possibly
     * made to the components registry and exits with the cancel error code.
     *
     * @see #finish()
     * @see #criticalExit()
     */
    public void cancel() {
        // shut down everything that needs it
        DownloadManager.instance.terminate();
        
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
        Wizard.getInstance().close();
        DownloadManager.instance.terminate();
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
        DownloadManager.instance.terminate();
        System.exit(CRITICAL_ERRORCODE);
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private void dumpSystemInfo() {
        LogManager.log("dumping target system information");
        LogManager.indent();
        
        LogManager.log("system properties:");
        LogManager.indent();
        
        Properties properties = System.getProperties();
        for (Object key: properties.keySet()) {
            LogManager.log(key.toString() + " => " + properties.get(key).toString());
        }
        
        LogManager.unindent();
        LogManager.log("... end of system properties");
        
        LogManager.unindent();
        LogManager.log("... end of target system information");
    }
    
    private void loadProperties() {
        final Properties properties = new Properties();
        
        try {
            InputStream input =
                    getClass().getClassLoader().getResourceAsStream(EngineResources.ENGINE_PROPERTIES);
            
            if (input != null) {
                properties.load(input);
                
                for (Object key: properties.keySet()) {
                    System.setProperty(
                            key.toString(),
                            properties.get(key).toString());
                }
            }
        } catch (IOException e) {
            ErrorManager.notifyWarning(
                    "Could not load the engine properties file",
                    e);
        }
    }
    
    /**
     * Parses the command line arguments passed to the installer. All unknown
     * arguments are ignored.
     *
     * @param arguments The command line arguments
     */
    private void parseArguments(String[] arguments) {
        LogManager.log("parsing command-line arguments");
        LogManager.indent();
        
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equalsIgnoreCase("--look-and-feel")) {
                LogManager.log("parsing command line parameter \"--look-and-feel\"");
                LogManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    System.setProperty(NBI_LOOK_AND_FEEL_CLASS_NAME_PROPERTY, value);
                    
                    i = i + 1;
                    
                    LogManager.log("... class name: " + value);
                } else {
                    ErrorManager.notifyWarning("Required parameter missing for command line argument \"--look-and-feel\". Should be \"--look-and-feel <look-and-feel-class-name>\".");
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(TARGET_ARG)) {
                LogManager.log("parsing command line parameter \"" + TARGET_ARG + "\"");
                LogManager.indent();
                
                if (i < arguments.length - 2) {
                    String uid = arguments[i + 1];
                    String version = arguments[i + 2];
                    System.setProperty(Registry.TARGET_COMPONENT_UID_PROPERTY, uid);
                    System.setProperty(Registry.TARGET_COMPONENT_VERSION_PROPERTY, version);
                    
                    i = i + 2;
                    
                    LogManager.log("... uid:     " + uid);
                    LogManager.log("... version: " + version);
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--locale")) {
                LogManager.log("parsing command line parameter \"--locale\"");
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
                        ErrorManager.notifyWarning("Invalid parameter command line argument \"--locale\". Should be \"<language>[_<country>[_<variant>]]\".");
                    }
                    
                    if (targetLocale != null) {
                        Locale.setDefault(targetLocale);
                        LogManager.log("... locale set to: " + targetLocale);
                    } else {
                        LogManager.log("... locale is not set, using system default: " + Locale.getDefault());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.notifyWarning("Required parameter missing for command line argument \"--locale\". Should be \"--locale <locale-name>\".");
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--state")) {
                LogManager.log("parsing command line parameter \"--state\"");
                LogManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    
                    File stateFile = new File(value).getAbsoluteFile();
                    if (!stateFile.exists()) {
                        ErrorManager.notifyWarning("The specified state file \"" + stateFile + "\", does not exist. \"--state\" parameter is ignored.");
                    } else {
                        System.setProperty(Registry.SOURCE_STATE_FILE_PATH_PROPERTY, stateFile.getAbsolutePath());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.notifyWarning("Required parameter missing for command line argument \"--state\". Should be \"--state <state-file-path>\".");
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--record")) {
                LogManager.log("parsing command line parameter \"--record\"");
                LogManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    
                    File stateFile = new File(value).getAbsoluteFile();
                    if (stateFile.exists()) {
                        ErrorManager.notifyWarning("The specified state file \"" + stateFile + "\", exists. \"--record\" parameter is ignored.");
                    } else {
                        System.setProperty(Registry.TARGET_STATE_FILE_PATH_PROPERTY, stateFile.getAbsolutePath());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.notifyWarning("Required parameter missing for command line argument \"--record\". Should be \"--record <state-file-path>\".");
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--silent")) {
                LogManager.log("parsing command line parameter \"--silent\"");
                LogManager.indent();
                
                UiMode.setCurrentUiMode(UiMode.SILENT);
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--create-bundle")) {
                LogManager.log("parsing command line parameter \"--create-bundle\"");
                LogManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    
                    File targetFile = new File(value).getAbsoluteFile();
                    if (targetFile.exists()) {
                        ErrorManager.notifyWarning("The specified target file \"" + targetFile + "\", exists. \"--create-bundle\" parameter is ignored.");
                    } else {
                        ExecutionMode.setCurrentExecutionMode(ExecutionMode.CREATE_BUNDLE);
                        System.setProperty(
                                Registry.CREATE_BUNDLE_PATH_PROPERTY,
                                targetFile.getAbsolutePath());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.notifyWarning("Required parameter missing for command line argument \"--create-bundle\". Should be \"--create-bundle <target-file-path>\".");
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--ignore-lock")) {
                LogManager.log("parsing command line parameter \"--ignore-lock\"");
                LogManager.indent();
                
                System.setProperty(IGNORE_LOCK_FILE_PROPERTY, "true");
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--userdir")) {
                LogManager.log("parsing command line parameter \"--userdir\"");
                LogManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    File   file  = new File(value);
                    
                    System.setProperty(LOCAL_DIRECTORY_PATH_PROPERTY, file.getAbsolutePath());
                    
                    i = i + 1;
                } else {
                    ErrorManager.notifyWarning("required parameter missing for command line argument \"--userdir\". Should be \"--userdir <userdir-path>\".");
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--platform")) {
                LogManager.log("parsing command line parameter \"--platform\"");
                LogManager.indent();
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    
                    System.setProperty(Registry.TARGET_PLATFORM_PROPERTY, value);
                    
                    i = i + 1;
                } else {
                    ErrorManager.notifyWarning("required parameter missing for command line argument \"--platform\". Should be \"--platform <target-platform>\".");
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--suggest-install")) {
                LogManager.log("parsing command line parameter \"--suggest-install\"");
                LogManager.indent();
                
                System.setProperty(Registry.SUGGEST_INSTALL_PROPERTY, "true");
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--suggest-uninstall")) {
                LogManager.log("parsing command line parameter \"--suggest-uninstall\"");
                LogManager.indent();
                
                System.setProperty(Registry.SUGGEST_UNINSTALL_PROPERTY, "true");
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--force-install")) {
                LogManager.log("parsing command line parameter \"--force-install\"");
                LogManager.indent();
                
                System.setProperty(Registry.FORCE_INSTALL_PROPERTY, "true");
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--force-uninstall")) {
                LogManager.log("parsing command line parameter \"--force-uninstall\"");
                LogManager.indent();
                
                System.setProperty(Registry.FORCE_UNINSTALL_PROPERTY, "true");
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase("--registry")) {
                LogManager.log("parsing command line parameter \"--registry\"");
                LogManager.indent();
                
                if (i < arguments.length - 1) {
                    final String value = arguments[i + 1];
                    
                    final String existing = System.getProperty(
                            Registry.REMOTE_PRODUCT_REGISTRIES_PROPERTY);
                    
                    if (existing == null) {
                        System.setProperty(
                                Registry.REMOTE_PRODUCT_REGISTRIES_PROPERTY,
                                value);
                    } else {
                        if (!Arrays.asList(existing.split(LF)).contains(value)) {
                            System.setProperty(
                                    Registry.REMOTE_PRODUCT_REGISTRIES_PROPERTY,
                                    existing + LF + value);
                        }
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.notifyWarning("required parameter missing for command line argument \"--registry\". Should be \"--registry <remote-registry-url>\".");
                }
                
                LogManager.unindent();
                continue;
            }
        }
        
        if (arguments.length == 0) {
            LogManager.log("... no command line arguments were specified");
        }
        
        // validate arguments ///////////////////////////////////////////////////////
        if (UiMode.getCurrentUiMode() != UiMode.DEFAULT_MODE) {
            if (System.getProperty(Registry.SOURCE_STATE_FILE_PATH_PROPERTY) == null) {
                UiMode.setCurrentUiMode(UiMode.DEFAULT_MODE);
                ErrorManager.notifyWarning("\"--state\" option is required when using \"--silent\". \"--silent\" will be ignored.");
            }
        }
        
        LogManager.unindent();
        LogManager.log("... finished parsing command line arguments");
    }
    
    private void setLocalDirectory() {
        LogManager.log("initializing the local directory");
        LogManager.indent();
        
        if (System.getProperty(LOCAL_DIRECTORY_PATH_PROPERTY) != null) {
            localDirectory = new File(System.getProperty(
                    LOCAL_DIRECTORY_PATH_PROPERTY)).getAbsoluteFile();
        } else {
            localDirectory = new File(DEFAULT_LOCAL_DIRECTORY_PATH).getAbsoluteFile();
            
            LogManager.log("... custom local directory was not specified, using the default");
            LogManager.log("... local directory: " + localDirectory);
        }
        
        if (!localDirectory.exists()) {
            if (!localDirectory.mkdirs()) {
                ErrorManager.notifyCritical("Cannot create local directory: " + localDirectory);
            }
        } else if (localDirectory.isFile()) {
            ErrorManager.notifyCritical("Local directory exists and is a file: " + localDirectory);
        } else if (!localDirectory.canRead()) {
            ErrorManager.notifyCritical("Cannot read local directory - not enought permissions");
        } else if (!localDirectory.canWrite()) {
            ErrorManager.notifyCritical("Cannot write to local directory - not enought permissions");
        }
        
        LogManager.unindent();
        LogManager.log("... finished initializing local directory");
    }
    
    private void setLookAndFeel() {
        LogManager.logIndent("setting the look and feel");
        
        String className = System.getProperty(NBI_LOOK_AND_FEEL_CLASS_NAME_PROPERTY);
        if (className == null) {
            LogManager.log("... custom look and feel class name was not specified, using system default");
            className = DEFAULT_NBI_LOOK_AND_FEEL_CLASS_NAME;
        }
        
        LogManager.log("... class name: " + className);
        
        try {
            JFrame.setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel(className);
        } catch (ClassNotFoundException e) {
            ErrorManager.notifyWarning("Could not set the look and feel.", e);
        } catch (InstantiationException e) {
            ErrorManager.notifyWarning("Could not set the look and feel.", e);
        } catch (IllegalAccessException e) {
            ErrorManager.notifyWarning("Could not set the look and feel.", e);
        } catch (UnsupportedLookAndFeelException e) {
            ErrorManager.notifyWarning("Could not set the look and feel.", e);
        }
        
        LogManager.logUnindent("... finished setting the look and feel");
    }
    
    private void cacheEngineJar() throws IOException, DownloadException {
        LogManager.log("... starting copying engine content to the new jar file");
        String [] entries = StreamUtils.readStream(
                ResourceUtils.getResource(EngineResources.ENGINE_CONTENTS_LIST)).
                toString().split(StringUtils.NEW_LINE_PATTERN);
        
        File dest = getCacheExpectedFile();
        
        JarOutputStream jos = null;
        
        try {
            Manifest mf = new Manifest(new ByteArrayInputStream(
                    DEFAULT_INSTALLER_MANIFEST.getBytes()));
            dest.getParentFile().mkdirs();
            jos = new JarOutputStream(new FileOutputStream(dest),mf);
            LogManager.log("... total entries : " + entries.length);
            for(int i=0;i<entries.length;i++) {
                String name = entries[i];
                if(name.length() > 0 && !name.startsWith(EngineResources.DATA_DIRECTORY)) {
                    jos.putNextEntry(new JarEntry(name));
                    if(!name.endsWith(StringUtils.FORWARD_SLASH)) {
                        StreamUtils.transferData(ResourceUtils.getResource(name), jos);
                    }
                }
            }
            LogManager.log("... adding content list and some other stuff");
            jos.putNextEntry(new JarEntry(EngineResources.DATA_DIRECTORY + StringUtils.FORWARD_SLASH));
            
            jos.putNextEntry(new JarEntry(EngineResources.DATA_DIRECTORY + StringUtils.FORWARD_SLASH +
                    "registry.xml"));
            
            XMLUtils.saveXMLDocument(
                    Registry.getInstance().getEmptyRegistryDocument(),
                    jos);
            
            jos.putNextEntry(new JarEntry(EngineResources.ENGINE_CONTENTS_LIST));
            jos.write(StringUtils.asString(entries, SystemUtils.getLineSeparator()).getBytes());
        } catch (XMLException ex) {
            throw new IOException(ex.toString());
        } finally {
            if(jos!=null) {
                try {
                    jos.close();
                } catch (IOException ex) {
                    LogManager.log(ex);
                }
                
            }
        }
        
        cachedEngine = (!dest.exists()) ? null : dest;
        
        LogManager.log("NBI Engine jar file = [" +
                cachedEngine + "], exist = " +
                ((cachedEngine==null) ? false : cachedEngine.exists()));
    }
    
    private File getCacheExpectedFile() {
        return new File(localDirectory, "nbi-engine.jar");
    }
    
    private void cacheEngineLocally() {
        LogManager.logIndent("cache engine data locally to run uninstall in the future");
        
        String filePrefix = "file:";
        String httpPrefix = "http://";
        String jarSep = "!/";
        
        try {
            String installerResource = Installer.class.getName().replace(".","/") + ".class";
            URL url = this.getClass().getClassLoader().getResource(installerResource);
            if(url == null) {
                throw new IOException("No main Installer class in the engine");
            }
            
            LogManager.log(ErrorLevel.DEBUG, "NBI Engine URL for Installer.Class = " + url);
            LogManager.log(ErrorLevel.DEBUG, "URL Path = " + url.getPath());
            
            boolean needCache = true;
            
            if("jar".equals(url.getProtocol())) {
                LogManager.log("... running engine as a .jar file");
                // we run engine from jar, not from .class
                String path = url.getPath();
                String jarLocation;
                
                if (path.startsWith(filePrefix)) {
                    LogManager.log("... classloader says that jar file is on the disk");
                    if (path.indexOf(jarSep) != -1) {
                        jarLocation = path.substring(filePrefix.length(),
                                path.indexOf(jarSep + installerResource));
                        jarLocation = URLDecoder.decode(jarLocation,"UTF8");
                        File jarfile = new File(jarLocation);
                        LogManager.log("... checking if it runs from cached engine");
                        if(jarfile.getAbsolutePath().equals(
                                getCacheExpectedFile().getAbsolutePath())) {
                            needCache = false; // we already run cached version
                            cachedEngine = jarfile;
                        }
                        LogManager.log("... " + !needCache);
                    } else {
                        throw new IOException("JAR path " + path +
                                " doesn`t contaion jar-separator " + jarSep);
                    }
                } else if (path.startsWith(httpPrefix)) {
                    LogManager.log("... classloader says that jar file is on remote server");
                }
            } else {
                // a quick hack to allow caching engine when run from the IDE (i.e.
                // as a .class) - probably to be removed later. Or maybe not...
                LogManager.log("... running engine as a .class file");
            }
            
            if (needCache) {
                try {
                    cacheEngineJar();
                } catch (DownloadException ex) {
                    LogManager.log("Can`t load engine jar content list");
                    LogManager.log(ex);
                    throw new IOException(ex.toString());
                }
            }
            
            final String java = JavaUtils.getExecutableW(
                    SystemUtils.getCurrentJavaHome()).getAbsolutePath();
            
            final String modifyCommand =
                    "\"" + java + "\" " +
                    "-jar \"" + cachedEngine.getAbsolutePath() + "\" " +
                    "--target \"{0}\" \"{1}\"";
            
            final String uninstallCommand =
                    modifyCommand + StringUtils.SPACE +
                    "--force-uninstall";
            
            System.setProperty(
                    EngineResources.LOCAL_ENGINE_PATH_PROPERTY,
                    cachedEngine.getAbsolutePath());
            
            System.setProperty(
                    EngineResources.LOCAL_ENGINE_MODIFY_COMMAND_PROPERTY,
                    modifyCommand);
            System.setProperty(
                    EngineResources.LOCAL_ENGINE_UNINSTALL_COMMAND_PROPERTY,
                    uninstallCommand);
        } catch (IOException ex) {
            ErrorManager.notifyCritical("can`t cache installer engine", ex);
        }
        
        LogManager.logUnindent("... finished caching engine data");
    }
    
    private void createInstallerLockFile()  {
        LogManager.logIndent("creating lock file");
        
        if (System.getProperty(IGNORE_LOCK_FILE_PROPERTY) == null) {
            File lock = new File(localDirectory, ".nbilock");
            
            if (lock.exists()) {
                LogManager.log("... lock file already exists");
                
                if(!UiUtils.showYesNoDialog(
                        "NetBeans Installer is already running",
                        "It seems that another instance of installer is already " +
                        "running!\nIt can be dangerous running another one in " +
                        "the same time.\nAre you sure you want to run one more " +
                        "instance?\n\n")) {
                    cancel();
                }
            } else {
                try {
                    lock.createNewFile();
                } catch (IOException e) {
                    ErrorManager.notifyCritical(
                            "Can't create lock for the local registry file!",
                            e);
                }
                
                LogManager.log("... created lock file: " + lock);
            }
            
            lock.deleteOnExit();
        } else {
            LogManager.log("... running with --ignore-lock, skipping this step");
        }
        
        LogManager.logUnindent("finished creating lock file");
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
    
    public static final String IGNORE_LOCK_FILE_PROPERTY =
            "nbi.ignore.lock.file";
    
    private static final String DEFAULT_INSTALLER_MANIFEST =
            "Manifest-Version: 1.0" + SystemUtils.getLineSeparator() +
            "Main-Class: " + Installer.class.getName() + SystemUtils.getLineSeparator() +
            "Class-Path: " + SystemUtils.getLineSeparator();
}
