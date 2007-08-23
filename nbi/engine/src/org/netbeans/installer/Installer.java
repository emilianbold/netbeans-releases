/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.DateUtils;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.EngineResources;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.ExecutionMode;
import org.netbeans.installer.utils.helper.FinishHandler;
import org.netbeans.installer.utils.helper.UiMode;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;

/**
 * The main class of the NBI engine. It represents the installer and provides
 * methods to start the installation/maintenance process as well as to
 * finish/cancel/break the installation.
 *
 * @author Kirill Sorokin
 */
public class Installer implements FinishHandler {
    /////////////////////////////////////////////////////////////////////////////////
    // Main
    /**
     * The main method. It creates an instance of {@link Installer} and calls
     * the {@link #start()} method, passing in the command line arguments.
     *
     * @param arguments The command line arguments
     */
    public static void main(String[] arguments) {
        new Installer(arguments).start();
    }
    
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
    
    // Constructor //////////////////////////////////////////////////////////////////
    /**
     * The only private constructor - we need to hide the default one as
     * <code>Installer is a singleton.
     *
     * @param arguments Command-line parameters.
     */
    private Installer(String[] arguments) {
        LogManager.logEntry(
                "initializing the installer engine"); // NOI18N
        
        // initialize the error manager
        ErrorManager.setFinishHandler(this);
        ErrorManager.setExceptionHandler(new ErrorManager.ExceptionHandler());
        
        // attach a handler for uncaught exceptions in the main thread
        Thread.currentThread().setUncaughtExceptionHandler(
                ErrorManager.getExceptionHandler());
        
        // check whether we can safely execute on the current platform, exit in
        // panic otherwise
        if (SystemUtils.getCurrentPlatform() == null) {
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    Installer.class,
                    ERROR_UNSUPPORTED_PLATFORM_KEY));
        }
        
        instance = this;
        
        // output all the possible target system information -- this may help to
        // devise the configuration differences in case of errors
        dumpSystemInfo();
        
        loadProperties();
        parseArguments(arguments);
        setLocalDirectory();
        
        // once we have set the local directory (and therefore devised the log
        // file path) we can start logging safely
        LogManager.setLogFile(new File(localDirectory, LOG_FILE_NAME));
        LogManager.start();
        
        // initialize the download manager module
        final DownloadManager downloadManager = DownloadManager.getInstance();
        downloadManager.setLocalDirectory(localDirectory);
        downloadManager.setFinishHandler(this);
        downloadManager.init();
        
        // initialize the product registry module
        final Registry registry = Registry.getInstance();
        registry.setLocalDirectory(localDirectory);
        registry.setFinishHandler(this);
        
        // initialize the wizard module
        final Wizard wizard = Wizard.getInstance();
        wizard.setFinishHandler(this);
        wizard.getContext().put(registry);
        
        // create the lock file
        createLockFile();
        
        // perform some additional intiialization for Mac OS
        initializeMacOS();
        
        LogManager.logExit("... finished initializing the engine"); // NOI18N
    }
    
    // Life cycle control methods ///////////////////////////////////////////////////
    /**
     * Starts the installer.
     */
    public void start() {
        LogManager.logEntry("starting the installer"); // NOI18N
        
        Wizard.getInstance().open();
        
        LogManager.logExit("... finished starting the installer"); // NOI18N
    }
    
    /**
     * Cancels the installation. This method cancels the changes that were possibly
     * made to the components registry and exits with the cancel error code.
     *
     * This method is not logged.
     *
     * @see #finish()
     * @see #criticalExit()
     */
    public void cancel() {
        exitNormally(CANCEL_ERRORCODE);
    }
    
    /**
     * Finishes the installation. This method finalizes the changes made to the
     * components registry and exits with a normal error code.
     *
     * This method is not logged.
     *
     * @see #cancel()
     * @see #criticalExit()
     */
    public void finish() {
        exitNormally(NORMAL_ERRORCODE);
    }
    
    /**
     * Critically exists. No changes will be made to the components registry - it
     * will remain at the same state it was at the moment this method was called.
     *
     * This method is not logged.
     *
     * @see #cancel()
     * @see #finish()
     */
    public void criticalExit() {
        // exit immediately, as the system is apparently in a crashed state
        exitImmediately(CRITICAL_ERRORCODE);
    }
    
    public File getLocalDirectory() {
        return localDirectory;
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private void exitNormally(int errorCode) {
        Wizard.getInstance().close();
        DownloadManager.getInstance().terminate();
        SystemUtils.deleteFilesOnExit();
        
        LogManager.stop();
        
        exitImmediately(errorCode);
    }
    
    private void exitImmediately(int errorCode) {
        if (Boolean.getBoolean(DONT_USE_SYSTEM_EXIT_PROPERTY) &&
                (errorCode != CRITICAL_ERRORCODE)) {
            System.getProperties().put(
                    EXIT_CODE_PROPERTY,
                    new Integer(errorCode));
        } else {
            System.exit(errorCode);
        }
    }
    
    private void dumpSystemInfo() {
        LogManager.logEntry("dumping target system information"); // NOI18N
        
        LogManager.logIndent("system properties:"); // NOI18N
        
        for (Object key: new TreeSet<Object>(System.getProperties().keySet())) {
            LogManager.log(key.toString() + " => " +  // NOI18N
                    System.getProperties().get(key).toString());
        }
        
        LogManager.unindent();
        
        LogManager.logExit("... end of target system information"); // NOI18N
    }
    
    private void loadProperties() {
        LogManager.logEntry("loading engine properties"); // NOI18N
        
        try {
            LogManager.logIndent("loading properties file from " + // NOI18N
                    EngineResources.ENGINE_PROPERTIES);
            
            final InputStream input = getClass().getClassLoader().
                    getResourceAsStream(EngineResources.ENGINE_PROPERTIES);
            
            if (input != null) {
                final Properties properties = new Properties();
                
                properties.load(input);
                for (Object key: properties.keySet()) {
                    LogManager.log("loading " + // NOI18N
                            key + " => " + properties.get(key)); // NOI18N
                    
                    System.setProperty(
                            key.toString(),
                            properties.get(key).toString());
                }
            }
        } catch (IOException e) {
            final String message = ResourceUtils.getString(
                    Installer.class,
                    ERROR_LOAD_ENGINE_PROPERTIES_KEY);
            
            ErrorManager.notifyWarning(message, e);
        }
        
        LogManager.unindent();
        
        LogManager.logExit("... finished loading engine properties"); // NOI18N
    }
    
    /**
     * Parses the command line arguments passed to the installer. All unknown
     * arguments are ignored.
     *
     * @param arguments The command line arguments
     */
    private void parseArguments(String[] arguments) {
        LogManager.logEntry("parsing command-line arguments"); // NOI18N
        
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equalsIgnoreCase(LOOK_AND_FEEL_ARG)) {
                LogManager.logIndent(
                        "parsing command line parameter \"" + // NOI18N
                        LOOK_AND_FEEL_ARG + "\""); // NOI18N
                
                if (i < arguments.length - 1) {
                    final String value = arguments[i + 1];
                    System.setProperty(
                            UiUtils.LAF_CLASS_NAME_PROPERTY,
                            value);
                    
                    i = i + 1;
                    
                    LogManager.log(
                            "... class name: " + value); // NOI18N
                } else {
                    final String message = ResourceUtils.getString(
                            Installer.class,
                            WARNING_BAD_LOOK_AND_FEEL_ARG_KEY,
                            LOOK_AND_FEEL_ARG);
                    
                    ErrorManager.notifyWarning(message);
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(TARGET_ARG)) {
                LogManager.logIndent(
                        "parsing command line parameter \"" + // NOI18N
                        TARGET_ARG + "\""); // NOI18N
                
                if (i < arguments.length - 2) {
                    final String uid = arguments[i + 1];
                    final String version = arguments[i + 2];
                    
                    System.setProperty(
                            Registry.TARGET_COMPONENT_UID_PROPERTY,
                            uid);
                    System.setProperty(
                            Registry.TARGET_COMPONENT_VERSION_PROPERTY,
                            version);
                    
                    i = i + 2;
                    
                    LogManager.log(
                            "target component:"); // NOI18N
                    LogManager.log(
                            "... uid:     " + uid); // NOI18N
                    LogManager.log(
                            "... version: " + version); // NOI18N
                } else {
                    ErrorManager.notifyWarning(ResourceUtils.getString(
                            Installer.class,
                            WARNING_BAD_TARGET_ARG_KEY,
                            TARGET_ARG));
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(LOCALE_ARG)) {
                LogManager.logIndent(
                        "parsing command line parameter \"" + // NOI18N
                        LOCALE_ARG + "\""); // NOI18N
                
                if (i < arguments.length - 1) {
                    final String value = arguments[i + 1];
                    final String[] valueParts = value.split(StringUtils.UNDERSCORE);
                    
                    final Locale targetLocale;
                    switch (valueParts.length) {
                        case 1:
                            targetLocale = new Locale(
                                    valueParts[0]);
                            break;
                        case 2:
                            targetLocale = new Locale(
                                    valueParts[0],
                                    valueParts[1]);
                            break;
                        case 3:
                            targetLocale = new Locale(
                                    valueParts[0],
                                    valueParts[1],
                                    valueParts[2]);
                            break;
                        default:
                            targetLocale = null;
                            
                            final String message = ResourceUtils.getString(
                                    Installer.class,
                                    WARNING_BAD_LOCALE_ARG_PARAM_KEY,
                                    LOCALE_ARG,
                                    value);
                            ErrorManager.notifyWarning(message);
                    }
                    
                    if (targetLocale != null) {
                        Locale.setDefault(targetLocale);
                        
                        LogManager.log(
                                "... locale set to: " + targetLocale); // NOI18N
                    } else {
                        LogManager.log(
                                "... locale is not set, using " + // NOI18N
                                "system default: " + Locale.getDefault()); // NOI18N
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.notifyWarning(ResourceUtils.getString(
                            Installer.class,
                            WARNING_BAD_LOCALE_ARG_KEY,
                            LOCALE_ARG));
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(STATE_ARG)) {
                LogManager.logIndent("parsing command line parameter \"" + // NOI18N
                        STATE_ARG + "\""); // NOI18N
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    
                    File stateFile = new File(value).getAbsoluteFile();
                    if (!stateFile.exists()) {
                        ErrorManager.notifyWarning(ResourceUtils.getString(
                                Installer.class,
                                WARNING_MISSING_STATE_FILE_KEY,
                                STATE_ARG,
                                stateFile));
                    } else {
                        System.setProperty(
                                Registry.SOURCE_STATE_FILE_PATH_PROPERTY,
                                stateFile.getAbsolutePath());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.notifyWarning(ResourceUtils.getString(
                            Installer.class,
                            WARNING_BAD_STATE_FILE_ARG_KEY,
                            STATE_ARG));
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(RECORD_ARG)) {
                LogManager.logIndent("parsing command line parameter \"" + // NOI18N
                        RECORD_ARG + "\""); // NOI18N
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    
                    File stateFile = new File(value).getAbsoluteFile();
                    if (stateFile.exists()) {
                        ErrorManager.notifyWarning(ResourceUtils.getString(
                                Installer.class,
                                WARNING_TARGET_STATE_FILE_EXISTS_KEY,
                                RECORD_ARG,
                                stateFile));
                    } else {
                        System.setProperty(
                                Registry.TARGET_STATE_FILE_PATH_PROPERTY,
                                stateFile.getAbsolutePath());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.notifyWarning(ResourceUtils.getString(
                            Installer.class,
                            WARNING_BAD_TARGET_STATE_FILE_ARG_KEY,
                            RECORD_ARG));
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(SILENT_ARG)) {
                LogManager.logIndent("parsing command line parameter \"" +
                        SILENT_ARG + "\"");
                
                UiMode.setCurrentUiMode(UiMode.SILENT);
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(CREATE_BUNDLE_ARG)) {
                LogManager.logIndent("parsing command line parameter \"" + // NOI18N
                        CREATE_BUNDLE_ARG + "\""); // NOI18N
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    
                    File targetFile = new File(value).getAbsoluteFile();
                    if (targetFile.exists()) {
                        ErrorManager.notifyWarning(ResourceUtils.getString(
                                Installer.class,
                                WARNING_BUNDLE_FILE_EXISTS_KEY,
                                CREATE_BUNDLE_ARG,
                                targetFile));
                    } else {
                        ExecutionMode.setCurrentExecutionMode(
                                ExecutionMode.CREATE_BUNDLE);
                        System.setProperty(
                                Registry.CREATE_BUNDLE_PATH_PROPERTY,
                                targetFile.getAbsolutePath());
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.notifyWarning(ResourceUtils.getString(
                            Installer.class,
                            WARNING_BAD_CREATE_BUNDLE_ARG_KEY,
                            CREATE_BUNDLE_ARG));
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(IGNORE_LOCK_ARG)) {
                LogManager.logIndent("parsing command line parameter \"" + // NOI18N
                        IGNORE_LOCK_ARG + "\""); // NOI18N
                
                System.setProperty(IGNORE_LOCK_FILE_PROPERTY,
                        UNARY_ARG_VALUE);
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(USERDIR_ARG)) {
                LogManager.logIndent("parsing command line parameter \"" + // NOI18N
                        USERDIR_ARG + "\""); // NOI18N
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    File   file  = new File(value);
                    
                    System.setProperty(LOCAL_DIRECTORY_PATH_PROPERTY,
                            file.getAbsolutePath());
                    
                    i = i + 1;
                } else {
                    ErrorManager.notifyWarning(ResourceUtils.getString(
                            Installer.class,
                            WARNING_BAD_USERDIR_ARG_KEY,
                            USERDIR_ARG));
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(PLATFORM_ARG)) {
                LogManager.logIndent("parsing command line parameter \"" + // NOI18N
                        PLATFORM_ARG + "\""); // NOI18N
                
                if (i < arguments.length - 1) {
                    String value = arguments[i + 1];
                    
                    System.setProperty(Registry.TARGET_PLATFORM_PROPERTY,
                            value);
                    
                    i = i + 1;
                } else {
                    ErrorManager.notifyWarning(ResourceUtils.getString(
                            Installer.class,
                            WARNING_BAD_PLATFORM_ARG_KEY,
                            PLATFORM_ARG));
                }
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(SUGGEST_INSTALL_ARG)) {
                LogManager.logIndent("parsing command line parameter \"" + // NOI18N
                        SUGGEST_INSTALL_ARG + "\""); // NOI18N
                
                System.setProperty(
                        Registry.SUGGEST_INSTALL_PROPERTY,
                        UNARY_ARG_VALUE);
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(SUGGEST_UNINSTALL_ARG)) {
                LogManager.logIndent("parsing command line parameter \"" + // NOI18N
                        SUGGEST_UNINSTALL_ARG + "\""); // NOI18N
                
                System.setProperty(
                        Registry.SUGGEST_UNINSTALL_PROPERTY,
                        UNARY_ARG_VALUE);
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(FORCE_INSTALL_ARG)) {
                LogManager.logIndent("parsing command line parameter \"" + // NOI18N
                        FORCE_INSTALL_ARG + "\""); // NOI18N
                
                System.setProperty(
                        Registry.FORCE_INSTALL_PROPERTY,
                        UNARY_ARG_VALUE);
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(FORCE_UNINSTALL_ARG)) {
                LogManager.logIndent("parsing command line parameter \"" + // NOI18N
                        FORCE_UNINSTALL_ARG + "\""); // NOI18N
                
                System.setProperty(
                        Registry.FORCE_UNINSTALL_PROPERTY,
                        UNARY_ARG_VALUE);
                
                LogManager.unindent();
                continue;
            }
            
            if (arguments[i].equalsIgnoreCase(REGISTRY_ARG)) {
                LogManager.logIndent("parsing command line parameter \"" + // NOI18N
                        REGISTRY_ARG + "\""); // NOI18N
                
                if (i < arguments.length - 1) {
                    final String value = arguments[i + 1];
                    
                    final String existing = System.getProperty(
                            Registry.REMOTE_PRODUCT_REGISTRIES_PROPERTY);
                    
                    if (existing == null) {
                        System.setProperty(
                                Registry.REMOTE_PRODUCT_REGISTRIES_PROPERTY,
                                value);
                    } else {
                        if (!Arrays.asList(
                                existing.split(StringUtils.LF)).contains(value)) {
                            System.setProperty(
                                    Registry.REMOTE_PRODUCT_REGISTRIES_PROPERTY,
                                    existing + StringUtils.LF + value);
                        }
                    }
                    
                    i = i + 1;
                } else {
                    ErrorManager.notifyWarning(ResourceUtils.getString(
                            Installer.class,
                            WARNING_BAD_REGISTRY_ARG_KEY,
                            REGISTRY_ARG));
                }
                
                LogManager.unindent();
                continue;
            }
        }
        
        if (arguments.length == 0) {
            LogManager.log(
                    "... no command line arguments were specified"); // NOI18N
        }
        
        // validate arguments ///////////////////////////////////////////////////////
        if (UiMode.getCurrentUiMode() != UiMode.DEFAULT_MODE) {
            if (System.getProperty(
                    Registry.SOURCE_STATE_FILE_PATH_PROPERTY) == null) {
                UiMode.setCurrentUiMode(UiMode.DEFAULT_MODE);
                ErrorManager.notifyWarning(ResourceUtils.getString(
                        Installer.class,
                        WARNING_SILENT_WITHOUT_STATE_KEY,
                        SILENT_ARG,
                        STATE_ARG));
            }
        }
        
        LogManager.logExit(
                "... finished parsing command line arguments"); // NOI18N
    }
    
    private void setLocalDirectory() {
        LogManager.logIndent("initializing the local directory"); // NOI18N
        
        if (System.getProperty(LOCAL_DIRECTORY_PATH_PROPERTY) != null) {
            localDirectory = new File(System.getProperty(
                    LOCAL_DIRECTORY_PATH_PROPERTY)).getAbsoluteFile();
        } else {
            LogManager.log("... custom local directory was " + // NOI18N
                    "not specified, using the default"); // NOI18N
            
            localDirectory =
                    new File(DEFAULT_LOCAL_DIRECTORY_PATH).getAbsoluteFile();
            System.setProperty(
                    LOCAL_DIRECTORY_PATH_PROPERTY,
                    localDirectory.getAbsolutePath());
        }
        
        LogManager.log("... local directory: " + localDirectory); // NOI18N
        
        if (!localDirectory.exists()) {
            if (!localDirectory.mkdirs()) {
                ErrorManager.notifyCritical(ResourceUtils.getString(
                        Installer.class,
                        ERROR_CANNOT_CREATE_LOCAL_DIR_KEY,
                        localDirectory));
            }
        } else if (localDirectory.isFile()) {
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    Installer.class,
                    ERROR_LOCAL_DIR_IS_FILE_KEY,
                    localDirectory));
        } else if (!localDirectory.canRead()) {
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    Installer.class,
                    ERROR_NO_READ_PERMISSIONS_FOR_LOCAL_DIR_KEY,
                    localDirectory));
        } else if (!localDirectory.canWrite()) {
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    Installer.class,
                    ERROR_NO_WRITE_PERMISSIONS_FOR_LOCAL_DIR_KEY,
                    localDirectory));
        }
        
        LogManager.logUnindent(
                "... finished initializing local directory"); // NOI18N
    }
    
    private void createLockFile()  {
        LogManager.logIndent("creating lock file"); // NOI18N
        
        if (System.getProperty(IGNORE_LOCK_FILE_PROPERTY) == null) {
            final File lock = new File(localDirectory, LOCK_FILE_NAME);
            
            if (lock.exists()) {
                LogManager.log("... lock file already exists"); // NOI18N
                
                final String dialogTitle = ResourceUtils.getString(
                        Installer.class,
                        LOCK_FILE_EXISTS_DIALOG_TITLE_KEY);
                final String dialogText = ResourceUtils.getString(
                        Installer.class,
                        LOCK_FILE_EXISTS_DIALOG_TEXT_KEY);
                if(!UiUtils.showYesNoDialog(dialogTitle, dialogText)) {
                    cancel();
                }
            } else {
                try {
                    lock.createNewFile();
                } catch (IOException e) {
                    ErrorManager.notifyCritical(ResourceUtils.getString(
                            Installer.class,
                            ERROR_CANNOT_CREATE_LOCK_FILE_KEY), e);
                }
                
                LogManager.log("... created lock file: " + lock); // NOI18N
            }
            
            lock.deleteOnExit();
        } else {
            LogManager.log("... running with " + // NOI18N
                    IGNORE_LOCK_ARG + ", skipping this step"); // NOI18N
        }
        
        LogManager.logUnindent("finished creating lock file"); // NOI18N
    }
    
    private void initializeMacOS() {
        if (SystemUtils.isMacOS()) {
            final Application application = Application.getApplication();
            
            application.removeAboutMenuItem();
            application.removePreferencesMenuItem();
            
            application.addApplicationListener(new ApplicationAdapter() {
                @Override
                public void handleQuit(ApplicationEvent event) {
                    final String dialogTitle = ResourceUtils.getString(
                            WizardComponent.class,
                            WizardComponent.RESOURCE_CANCEL_DIALOG_TITLE);
                    final String dialogText = ResourceUtils.getString(
                            WizardComponent.class,
                            
                            WizardComponent.RESOURCE_CANCEL_DIALOG_TEXT);
                    
                    if (UiUtils.showYesNoDialog(dialogTitle, dialogText)) {
                        cancel();
                    }
                }
            });
        }
    }
    
    /**
     * Cache installer at NBI`s home directory.
     */
    public static File cacheInstallerEngine(Progress progress) throws IOException {
        final String propName = EngineResources.LOCAL_ENGINE_PATH_PROPERTY;
        File cachedEngine = null;
        
        if ( System.getProperty(propName) == null ) {
            cachedEngine = new File(System.getProperty(
                    LOCAL_DIRECTORY_PATH_PROPERTY),
                    "nbi-engine.jar");
            System.setProperty(propName, cachedEngine.getAbsolutePath());
        }  else {
            cachedEngine = new File(System.getProperty(propName));
        }
        
        if(!FileUtils.exists(cachedEngine)) {
            cacheInstallerEngine(cachedEngine, progress);
        }
        
        return new File(System.getProperty(propName));
    }
    
    private static void cacheInstallerEngineJar(File dest, Progress progress) throws IOException {
        LogManager.log("... starting copying engine content to the new jar file");
        String [] entries = StreamUtils.readStream(
                ResourceUtils.getResource(EngineResources.ENGINE_CONTENTS_LIST)).
                toString().split(StringUtils.NEW_LINE_PATTERN);
        
        JarOutputStream jos = null;
        
        try {
            Manifest mf = new Manifest();
            mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, Installer.class.getName());
            mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            mf.getMainAttributes().put(Attributes.Name.CLASS_PATH, "");
            
            dest.getParentFile().mkdirs();
            jos = new JarOutputStream(new FileOutputStream(dest),mf);
            LogManager.log("... total entries : " + entries.length);
            for(int i=0;i<entries.length;i++) {
                progress.setPercentage((i * 100) /entries.length);
                String name = entries[i];
                if(name.length() > 0) {
                    String dataDir = EngineResources.DATA_DIRECTORY +
                            StringUtils.FORWARD_SLASH;
                    if(!name.startsWith(dataDir) || // all except "data/""
                            name.equals(dataDir) || // "data/"
                            name.equals(EngineResources.ENGINE_PROPERTIES)) { // "data/engine.properties"
                        jos.putNextEntry(new JarEntry(name));
                        if(!name.endsWith(StringUtils.FORWARD_SLASH)) {
                            StreamUtils.transferData(ResourceUtils.getResource(name), jos);
                        }
                    }
                }
            }
            LogManager.log("... adding content list and some other stuff");
            
            jos.putNextEntry(new JarEntry(
                    EngineResources.DATA_DIRECTORY + StringUtils.FORWARD_SLASH +
                    "registry.xml"));
            
            XMLUtils.saveXMLDocument(
                    Registry.getInstance().getEmptyRegistryDocument(),
                    jos);
            
            jos.putNextEntry(new JarEntry(EngineResources.ENGINE_CONTENTS_LIST));
            jos.write(StringUtils.asString(entries, SystemUtils.getLineSeparator()).getBytes());
        }  catch (XMLException e){
            IOException ex = new IOException();
            ex.initCause(e);
            throw ex;
        } finally {
            if(jos!=null) {
                try {
                    jos.close();
                } catch (IOException ex) {
                    LogManager.log(ex);
                }
                
            }
        }
        
        LogManager.log("Installer Engine has been cached to " + dest);
    }
    
    
    
    public static void cacheInstallerEngine(File dest, Progress progress) throws IOException {
        LogManager.logIndent("cache engine data locally to run uninstall in the future");
        
        String filePrefix = "file:";
        String httpPrefix = "http://";
        String jarSep     = "!/";
        
        String installerResource = Installer.class.getName().replace(".","/") + ".class";
        URL url = Installer.class.getClassLoader().getResource(installerResource);
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
                    jarLocation = URLDecoder.decode(jarLocation, StringUtils.ENCODING_UTF8);
                    File jarfile = new File(jarLocation);
                    LogManager.log("... checking if it runs from cached engine");
                    if(jarfile.getAbsolutePath().equals(
                            dest.getAbsolutePath())) {
                        needCache = false; // we already run cached version
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
            cacheInstallerEngineJar(dest, progress);
        }
        
        LogManager.logUnindent("... finished caching engine data");
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    
    // errorcodes ///////////////////////////////////////////////////////////////////
    /** Errorcode to be used at normal exit */
    public static final int NORMAL_ERRORCODE =
            0;
    
    /** Errorcode to be used when the installer is canceled */
    public static final int CANCEL_ERRORCODE =
            1;
    
    /** Errorcode to be used when the installer exits because of a critical error */
    public static final int CRITICAL_ERRORCODE =
            255;
    
    // command line arguments ///////////////////////////////////////////////////////
    public static final String TARGET_ARG =
            "--target"; // NOI18N
    
    public static final String LOOK_AND_FEEL_ARG =
            "--look-and-feel"; // NOI18N
    
    public static final String LOCALE_ARG =
            "--locale"; // NOI18N
    
    public static final String STATE_ARG =
            "--state"; // NOI18N
    
    public static final String RECORD_ARG =
            "--record"; // NOI18N
    
    public static final String SILENT_ARG =
            "--silent"; // NOI18N
    
    public static final String CREATE_BUNDLE_ARG =
            "--create-bundle"; // NOI18N
    
    public static final String IGNORE_LOCK_ARG =
            "--ignore-lock"; // NOI18N
    
    public static final String USERDIR_ARG =
            "--userdir"; // NOI18N
    
    public static final String PLATFORM_ARG =
            "--platform"; // NOI18N
    
    public static final String SUGGEST_INSTALL_ARG =
            "--suggest-install"; // NOI18N
    
    public static final String SUGGEST_UNINSTALL_ARG =
            "--suggest-uninstall"; // NOI18N
    
    public static final String FORCE_INSTALL_ARG =
            "--force-install"; // NOI18N
    
    public static final String FORCE_UNINSTALL_ARG =
            "--force-uninstall"; // NOI18N
    
    public static final String REGISTRY_ARG =
            "--registry"; // NOI18N
    
    public static final String UNARY_ARG_VALUE =
            "true"; // NOI18N
    
    // lock file ////////////////////////////////////////////////////////////////////
    public static final String LOCK_FILE_NAME =
            ".nbilock"; // NOI18N
    
    public static final String IGNORE_LOCK_FILE_PROPERTY =
            "nbi.ignore.lock.file"; // NOI18N
    
    // local working directory //////////////////////////////////////////////////////
    public static final String DEFAULT_LOCAL_DIRECTORY_PATH =
            System.getProperty("user.home") + File.separator + ".nbi";
    
    public static final String LOCAL_DIRECTORY_PATH_PROPERTY =
            "nbi.local.directory.path"; // NOI18N
    
    // miscellaneous ////////////////////////////////////////////////////////////////
    public static final String DONT_USE_SYSTEM_EXIT_PROPERTY =
            "nbi.dont.use.system.exit"; // NOI18N
    
    public static final String EXIT_CODE_PROPERTY =
            "nbi.exit.code"; // NOI18N
    
    public static final String LOG_FILE_NAME =
            "log/" + DateUtils.getTimestamp() + ".log";
    
    // resource bundle keys /////////////////////////////////////////////////////////
    private static final String ERROR_UNSUPPORTED_PLATFORM_KEY =
            "I.error.unsupported.platform"; // NOI18N
    
    private static final String ERROR_LOAD_ENGINE_PROPERTIES_KEY =
            "I.error.load.engine.properties"; // NOI18N
    
    private static final String WARNING_BAD_LOOK_AND_FEEL_ARG_KEY =
            "I.warning.bad.look.and.feel.arg"; // NOI18N
    
    private static final String WARNING_BAD_TARGET_ARG_KEY =
            "I.warning.bad.target.arg"; // NOI18N
    
    private static final String WARNING_BAD_LOCALE_ARG_PARAM_KEY =
            "I.warning.bad.locale.arg.param"; // NOI18N
    
    private static final String WARNING_BAD_LOCALE_ARG_KEY =
            "I.warning.bad.locale.arg"; // NOI18N
    
    private static final String WARNING_MISSING_STATE_FILE_KEY =
            "I.warning.missing.state.file"; // NOI18N
    
    private static final String WARNING_BAD_STATE_FILE_ARG_KEY =
            "I.warning.bag.state.file.arg"; // NOI18N
    
    private static final String WARNING_TARGET_STATE_FILE_EXISTS_KEY =
            "I.warning.target.state.file.exists"; // NOI18N
    
    private static final String WARNING_BAD_TARGET_STATE_FILE_ARG_KEY =
            "I.warning.bad.target.state.file.arg"; // NOI18N
    
    private static final String WARNING_BUNDLE_FILE_EXISTS_KEY =
            "I.warning.bundle.file.exists"; // NOI18N
    
    private static final String WARNING_BAD_CREATE_BUNDLE_ARG_KEY =
            "I.warning.bad.create.bundle.arg"; // NOI18N
    
    private static final String WARNING_BAD_USERDIR_ARG_KEY =
            "I.warning.bad.userdir.arg"; // NOI18N
    
    private static final String WARNING_BAD_PLATFORM_ARG_KEY =
            "I.warning.bad.platform.arg"; // NOI18N
    
    private static final String WARNING_BAD_REGISTRY_ARG_KEY =
            "I.warning.bad.registry.arg"; // NOI18N
    
    private static final String WARNING_SILENT_WITHOUT_STATE_KEY =
            "I.warning.silent.without.state"; // NOI18N
    
    private static final String ERROR_CANNOT_CREATE_LOCAL_DIR_KEY =
            "I.error.cannot.create.local.dir"; // NOI18N
    
    private static final String ERROR_LOCAL_DIR_IS_FILE_KEY =
            "I.error.local.dir.is.file"; // NOI18N
    
    private static final String ERROR_NO_READ_PERMISSIONS_FOR_LOCAL_DIR_KEY =
            "I.error.no.read.permissions.for.local.dir"; // NOI18N
    
    private static final String ERROR_NO_WRITE_PERMISSIONS_FOR_LOCAL_DIR_KEY =
            "I.error.no.write.permissions.for.local.dir"; // NOI18N
    
    private static final String LOCK_FILE_EXISTS_DIALOG_TITLE_KEY =
            "I.lock.file.exists.dialog.title"; // NOI18N
    
    private static final String LOCK_FILE_EXISTS_DIALOG_TEXT_KEY =
            "I.lock.file.exists.dialog.text"; // NOI18N
    
    private static final String ERROR_CANNOT_CREATE_LOCK_FILE_KEY =
            "I.error.cannot.create.lock.file"; // NOI18N
    
}
