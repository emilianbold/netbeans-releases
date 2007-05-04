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
package org.netbeans.installer.utils.system;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.netbeans.installer.Installer;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.system.shortcut.Shortcut;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.ApplicationDescriptor;
import org.netbeans.installer.utils.helper.EngineResources;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.system.launchers.Launcher;
import org.netbeans.installer.utils.system.launchers.LauncherResource;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.cleaner.OnExitCleanerHandler;
import org.netbeans.installer.utils.system.cleaner.JavaOnExitCleanerHandler;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
import org.netbeans.installer.utils.system.shortcut.LocationType;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class NativeUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static NativeUtils instance;
    private static HashSet<File> forbiddenDeletingFiles = new HashSet<File>();
    private static List <File> deleteOnExitFiles = new ArrayList <File> ();
    public final static String NATIVE_RESOURCE_SUFFIX = "native/"; // NOI18N
    public final static String NATIVE_JNILIB_RESOURCE_SUFFIX =
            NATIVE_RESOURCE_SUFFIX +
            "jnilib/"; // NOI18N
    public final static String NATIVE_LAUNCHER_RESOURCE_SUFFIX =
            NATIVE_RESOURCE_SUFFIX +
            "launcher/"; // NOI18N
    public final static String NATIVE_CLEANER_RESOURCE_SUFFIX =
            NATIVE_RESOURCE_SUFFIX +
            "cleaner/"; // NOI18N
    
    
    public static synchronized NativeUtils getInstance() {
        switch (SystemUtils.getCurrentPlatform()) {
            case WINDOWS:
                instance = new WindowsNativeUtils();
                break;
            case LINUX:
                instance = new LinuxNativeUtils();
                break;
            case SOLARIS_X86:
                instance = new SolarisX86NativeUtils();
                break;
            case SOLARIS_SPARC:
                instance = new SolarisSparcNativeUtils();
                break;
            case MACOS_X_PPC:
            case MACOS_X_X86:
                instance = new MacOsNativeUtils();
                break;
        }
        
        return instance;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    
    // constructor //////////////////////////////////////////////////////////////////
    protected NativeUtils() {
        // does nothing
    }
    
    // abstract /////////////////////////////////////////////////////////////////////
    public abstract boolean isCurrentUserAdmin() throws NativeException;
    
    public abstract File getDefaultApplicationsLocation() throws NativeException;
    
    public abstract long getFreeSpace(File file) throws NativeException;
    
    public abstract boolean isPathValid(String path);
    
    public abstract File getShortcutLocation(Shortcut shortcut, LocationType locationType) throws NativeException;
    
    public abstract File createShortcut(Shortcut shortcut, LocationType locationType) throws NativeException;
    
    public abstract void removeShortcut(Shortcut shortcut, LocationType locationType, boolean deleteEmptyParents) throws NativeException;
    
    protected Launcher createUninstaller(ApplicationDescriptor descriptor, boolean uninstall, Progress progress) throws IOException {
        LogManager.log("creating uninstaller...");
        
        final File engine = new File(System.getProperty(
                EngineResources.LOCAL_ENGINE_PATH_PROPERTY));
        final LauncherProperties props = new LauncherProperties();
        
        props.addJVM(new LauncherResource(false, SystemUtils.getCurrentJavaHome()));
        props.addJar(new LauncherResource(false, engine));
        props.setJvmArguments(new String[]{
            "-Xmx256m",
            "-Xms64m"});
        props.setMainClass(Installer.class.getName());
        
        if (uninstall) {
            props.setAppArguments(descriptor.getUninstallCommand());
            props.setOutput(
                    new File(descriptor.getInstallPath(), "uninstall"),
                    true);
        } else {
            props.setAppArguments(descriptor.getModifyCommand());
            props.setOutput(
                    new File(descriptor.getInstallPath(), "modify-install"),
                    true);
        }
        
        return SystemUtils.createLauncher(props, progress);
    }
    
    public abstract FilesList addComponentToSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException;
    
    public abstract void removeComponentFromSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException;
    
    public abstract String getEnvironmentVariable(String name, EnvironmentScope scope, boolean expand) throws NativeException;
    
    public abstract void setEnvironmentVariable(String name, String value, EnvironmentScope scope, boolean expand) throws NativeException;
    
    public abstract List<File> findIrrelevantFiles(File parent) throws IOException;
    
    public abstract List<File> findExecutableFiles(File parent) throws IOException;
    
    public abstract void correctFilesPermissions(File parent) throws IOException;
    
    public boolean checkFileAccess(File file, boolean isReadNotModify) throws NativeException {
        return true;
    }
    
    public final void addDeleteOnExitFile(File file) {
        if(!deleteOnExitFiles.contains(file)) {
            deleteOnExitFiles.add(file);
        }
    }
    
    public final void removeDeleteOnExitFile(File file) {
        deleteOnExitFiles.remove(file);
    }
    
    protected OnExitCleanerHandler getDeleteOnExit() {
        return new JavaOnExitCleanerHandler();
    }
    
    public void deleteFilesOnExit() {
        OnExitCleanerHandler deleteOnExit = getDeleteOnExit();
        try {
            deleteOnExit.initialize(deleteOnExitFiles);
            deleteOnExit.run();
        } catch (IOException ex) {
            LogManager.log(ex);
        }
        
    }
    
    public abstract List<File> getFileSystemRoots() throws IOException;
    
    // protected ////////////////////////////////////////////////////////////////////
    protected void loadNativeLibrary(String libraryPath) {
        if (libraryPath != null) {
            InputStream input = null;
            
            try {
                File file = FileUtils.createTempFile();
                
                input = getClass().getClassLoader().getResourceAsStream(libraryPath);
                
                FileUtils.writeFile(file, input);
                
                System.load(file.getAbsolutePath());
                addDeleteOnExitFile(file);
            } catch (IOException e) {
                ErrorManager.notify(ErrorLevel.CRITICAL, "Cannot load native library from path: " + libraryPath, e);
            } catch (UnsatisfiedLinkError e) {
                ErrorManager.notify(ErrorLevel.CRITICAL, "Cannot load native library from path: " + libraryPath, e);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        ErrorManager.notify(ErrorLevel.DEBUG, e);
                    }
                }
            }
        }
    }
    
    protected void initializeForbiddenFiles(String ... filepaths) {
        for (String path : filepaths) {
            if(path!=null) {
                File file = new File(path);
                if(file.exists()) {
                    forbiddenDeletingFiles.add(file);
                }
            }
        }
    }
    
    public boolean isDeletingAllowed(File file) {
        return !(forbiddenDeletingFiles.contains(file));
    }
}
