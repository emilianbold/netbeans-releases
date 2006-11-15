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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.helper.Shortcut;
import org.netbeans.installer.utils.helper.ShortcutLocationType;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JDKUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.HKCU;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.HKLM;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;

/**
 *
 * @author Dmitry Lipin
 * @author Kirill Sorokin
 */
public class WindowsNativeUtils extends NativeUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String LIBRARY_PATH = "native/windows.dll";
    
    public static final String UNINSTALL_KEY = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall";
    
    public static final String DISPLAY_NAME     = "DisplayName";
    public static final String DISPLAY_ICON     = "DisplayIcon";
    public static final String UNINSTALL_STRING = "UninstallString";
    public static final String INSTALL_LOCATION = "InstallLocation";
    
    private static final String NBI_UID_PREFIX = "nbi-";
    private static final String UID_SEPARATOR  = "-";
    
    private static final int MIN_UID_INDEX = 1;
    private static final int MAX_UID_INDEX = 100;
    
    private static final String SHELL_FOLDERS_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders";
    
    private static final String CURRENT_USER_ENVIRONMENT_KEY = "Environment";
    private static final String ALL_USERS_ENVIRONMENT_KEY    = "SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment";
    
    private static final String RUNONCE_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\RunOnce";
    private static final String RUNONCE_DELETE_VALUE_NAME = "NBI Temporary Files Delete";
    
    private static final WindowsRegistry registry = new WindowsRegistry();
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private File defaultApplicationsLocation;
    
    // constructor //////////////////////////////////////////////////////////////////
    WindowsNativeUtils() {
        loadNativeLibrary(LIBRARY_PATH);
    }
    
    // parent implementation ////////////////////////////////////////////////////////
    public boolean isCurrentUserAdmin() throws NativeException {
        return isCurrentUserAdmin0();
    }
    
    public File getDefaultApplicationsLocation() throws NativeException {
        if (defaultApplicationsLocation == null) {
            defaultApplicationsLocation = SystemUtils.getUserHomeDirectory();
            
            String path = SystemUtils.getEnvironmentVariable("ProgramFiles");
            
            if (path != null) {
                defaultApplicationsLocation = new File(path).getAbsoluteFile();
            } else {
                ErrorManager.notify(ErrorLevel.DEBUG, "Value of the environment variable ProgramFiles is not set");
            }
        }
        
        return defaultApplicationsLocation;
    }
    
    public long getFreeSpace(File file) throws NativeException {
        if ((file == null) || !isPathValid(file.getPath())) {
            return 0;
        } else {
            return getFreeSpace0(file.getPath());
        }
    }
    
    public boolean isPathValid(String path) {
        return path.matches("^[A-Z,a-z]:(\\\\[^\\\\\\/:*\"<>|\\s]([^\\\\\\/:*\"<>|\t]*[^\\\\\\/:*\"<>|\\s])?)*\\\\?$");
    }
    
    public File getShortcutLocation(Shortcut shortcut, ShortcutLocationType locationType) throws NativeException {
        String shortcutPath = shortcut.getRelativePath();
        if (shortcutPath == null) {
            shortcutPath = "";
        }
        
        String shortcutFileName = shortcut.getFileName();
        if (shortcutFileName == null) {
            shortcutFileName = shortcut.getName() + ".lnk";
        }
        
        final String allUsersRootPath = SystemUtils.getEnvironmentVariable("allusersprofile");
        
        switch (locationType) {
            case CURRENT_USER_DESKTOP:
                String userDesktop = registry.getStringValue(HKCU, SHELL_FOLDERS_KEY, "Desktop", false);
                if (userDesktop == null) {
                    userDesktop = SystemUtils.getUserHomeDirectory() + File.separator + "Desktop";
                }
                
                return new File(userDesktop, shortcutFileName);
                
            case ALL_USERS_DESKTOP:
                String commonDesktop = registry.getStringValue(HKLM, SHELL_FOLDERS_KEY, "Common Desktop", false);
                if (commonDesktop == null) {
                    commonDesktop = allUsersRootPath + File.separator + "Desktop";
                }
                
                return new File(commonDesktop, shortcutFileName);
                
            case CURRENT_USER_START_MENU:
                String userStartMenu = registry.getStringValue(HKCU, SHELL_FOLDERS_KEY, "Programs", false);
                if (userStartMenu == null) {
                    userStartMenu = SystemUtils.getUserHomeDirectory() + File.separator + "Start Menu" + File.separator + "Programs";
                }
                
                return new File(userStartMenu, shortcutPath + File.separator + shortcutFileName);
                
            case ALL_USERS_START_MENU:
                String commonStartMenu = registry.getStringValue(HKLM, SHELL_FOLDERS_KEY, "Common Programs", false);
                if (commonStartMenu == null) {
                    commonStartMenu = SystemUtils.getUserHomeDirectory() + File.separator + "Start Menu" + File.separator + "Programs";
                }
                
                return new File(commonStartMenu, shortcutPath + File.separator + shortcutFileName);
        }
        
        return null;
    }
    
    public File createShortcut(Shortcut shortcut, ShortcutLocationType locationType) throws NativeException {
        File shortcutFile = getShortcutLocation(shortcut, locationType);
        
        shortcut.setPath(shortcutFile.getAbsolutePath());
        
        createShortcut0(shortcut);
        
        return shortcutFile;
    }
    
    public void removeShortcut(Shortcut shortcut, ShortcutLocationType locationType, boolean cleanupParents) throws NativeException {
        File shortcutFile = getShortcutLocation(shortcut, locationType);
        
        try {
            FileUtils.deleteFile(shortcutFile);
            
            if (cleanupParents) {
                switch (locationType) {
                    case CURRENT_USER_START_MENU:
                    case ALL_USERS_START_MENU:
                        FileUtils.deleteEmptyParents(shortcutFile);
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            
        }
    }
    
    public void addComponentToSystemInstallManager(ProductComponent component) throws NativeException {
        LogManager.logIndent("adding component to windows registry uninstall section");
        
        String uid         = getUidKey(component);
        String installPath = component.getInstallationLocation().getPath();
        String icon        = null; // use standard icon till we find a way to pass it
        String displayName = component.getDisplayName();
        
        File executable = JDKUtils.getExecutableW(SystemUtils.getCurrentJavaHome());
        File cachedEngine = Installer.getInstance().getCachedEngine();
        
        String uninstallString = null;
        
        if (cachedEngine != null) {
            String sp = " ";
            String br = "\"";
            uninstallString = br + executable.getPath() + br + sp + "-jar" + sp + br + cachedEngine.getPath() + br + sp + Installer.TARGET_ARG + sp + br + component.getUid() + br + sp + br + component.getVersion().toString() + br;
            
            LogManager.logIndent("Adding entry to add/remove programs list with the following data:");
            LogManager.log("uid             = " + uid);
            LogManager.log("installPath     = " + installPath);
            LogManager.log("icon            = " + icon);
            LogManager.log("displayName     = " + displayName);
            LogManager.log("uninstallString = " + uninstallString);
            LogManager.logUnindent("");
            
            addRemoveProgramsInstall(uid, displayName, icon, installPath, uninstallString, new HashMap<String, Object>());
        } else {
            LogManager.log(ErrorLevel.WARNING, "Can't find cached engine.");
            LogManager.log(ErrorLevel.WARNING, "The entry would not be added to the add/remove programs list");
        }
        
        LogManager.logUnindent("... finished adding of the component to windows registry uninstall section");
    }
    
    public void removeComponentFromSystemInstallManager(ProductComponent component) throws NativeException {
        String uid         = getUidKey(component);
        String installPath = component.getInstallationLocation().getPath();
        
        addRemoveProgramsUninstall(uid, installPath);
    }
    
    public String getEnvironmentVariable(String name, EnvironmentScope scope, boolean expand) throws NativeException {
        String value = null;
        
        if ((scope != null) && (name != null)) {
            if (scope == EnvironmentScope.PROCESS) {
                value = System.getenv(name);
            } else {
                String rootKey = null;
                int    section = 0;
                if (scope == EnvironmentScope.CURRENT_USER) {
                    rootKey = CURRENT_USER_ENVIRONMENT_KEY;
                    section = HKCU;
                }
                if (scope == EnvironmentScope.ALL_USERS) {
                    rootKey = ALL_USERS_ENVIRONMENT_KEY;
                    section = HKLM;
                }
                
                if (registry.keyExists(section, rootKey)) {
                    value = registry.getStringValue(section, rootKey, name, expand);
                } else {
                    LogManager.log(ErrorLevel.DEBUG, "Root environment key doesn`t exist. Can`t get environment variable");
                }
            }
        }
        
        return value;
    }
    
    public void setEnvironmentVariable(String name, String value, EnvironmentScope scope, boolean expand) throws NativeException {
        if ((name != null) && (scope != null)) {
            if (scope == EnvironmentScope.PROCESS) {
                SystemUtils.getEnvironment().put(name, value);
            } else {
                String rootKey = null;
                int    section = 0;
                if (scope == EnvironmentScope.CURRENT_USER) {
                    rootKey = CURRENT_USER_ENVIRONMENT_KEY;
                    section = HKCU;
                }
                if (scope == EnvironmentScope.ALL_USERS) {
                    rootKey = ALL_USERS_ENVIRONMENT_KEY;
                    section = HKLM;
                }
                
                if (registry.keyExists(section, rootKey)) {
                    registry.setStringValue(section, rootKey, name, value, expand);
                } else {
                    LogManager.log(ErrorLevel.WARNING, "Root envonment key doesn`t exist. Can`t get environment variable");
                }
            }
        }
    }
    
    public List<File> findIrrelevantFiles(File parent) throws IOException {
        List<File> files = new LinkedList<File>();
        
        if (!parent.exists()) {
            return files;
        }
        
        for(File child : parent.listFiles()) {
            if (child.isDirectory()) {
                files.addAll(findIrrelevantFiles(child));
            } else {
                // name based analysis
                String name = child.getName();
                if (name.endsWith(".sh")) { // shell script
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".so")) { // library
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".dylib")) { // macosx library
                    files.add(child);
                    continue;
                }
                
                // contents based analysis
                String line = FileUtils.readFirstLine(child);
                if (line != null) {
                    if (line.startsWith("#!/bin/sh")) { // shell script
                        files.add(child);
                        continue;
                    }
                }
                
            }
        }
        
        return files;
    }
    
    public List<File> findExecutableFiles(File parent) throws IOException {
        List<File> files = new LinkedList<File>();
        
        if (!parent.exists()) {
            return files;
        }
        
        for(File child : parent.listFiles()) {
            if (child.isDirectory()) {
                files.addAll(findIrrelevantFiles(child));
            } else {
                // name based analysis
                String name = child.getName();
                if (name.endsWith(".exe")) {
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".com")) {
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".bat")) {
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".cmd")) {
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".vbs")) {
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".vbe")) {
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".js")) {
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".jse")) {
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".wsf")) {
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".wsh")) {
                    files.add(child);
                    continue;
                }
            }
        }
        
        return files;
    }
    
    public void correctFilesPermissions(File parent) {
        // does nothing, as there is no such thing as execute permissions
    }
    
    // protected ////////////////////////////////////////////////////////////////////
    protected void scheduleCleanup(String libraryPath) {
        try {
            deleteFileOnReboot(new File(libraryPath));
        } catch (NativeException e) {
            ErrorManager.notify(ErrorLevel.DEBUG, "Cannot schedule native library for deletion", e);
        }
    }
    
    // windows-specific operations //////////////////////////////////////////////////
    public WindowsRegistry getWindowsRegistry() {
        return registry;
    }
    
    public void deleteFileOnReboot(File file) throws NativeException {
        String path = file.getAbsolutePath();
        
        if (isCurrentUserAdmin()) {
            deleteFileOnReboot0(path);
        } else {
            // just in case...
            if (!registry.keyExists(HKCU, RUNONCE_KEY)) {
                registry.createKey(HKCU, RUNONCE_KEY);
            }
            
            // find an appropriate name, which does not exist
            String name = RUNONCE_DELETE_VALUE_NAME;
            for (int i = 0; registry.valueExists(HKCU, RUNONCE_KEY, name); i++) {
                name = RUNONCE_DELETE_VALUE_NAME + UID_SEPARATOR + i;
            }
            
            // set the value
            String command = "cmd /q /c del /F /Q \"" + path + "\"";
            registry.setStringValue(HKCU, RUNONCE_KEY, name, command);
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    
    /**
     * Add new entry in Add/Remove programs.<br>
     *
     * @param uid
     *      The uid of the entry<br>
     *
     * @param displayName
     *      The name that would be displayed in Add/Remove window<br>
     *
     * @param displayIcon
     *      The icon that would be displayed in Add/Remove window<br>
     *
     * @param installLocation
     *      The location of the installation<br>
     *
     * @param uninstallString
     *      The uninstaller location<br>
     *
     * @param additionalParameters
     *      The hashmap of additional parametrs.<br>
     *      The possible values are : <br>
     *      <ul>
     *      <li>int(Integer), long (Long), short(Short) -> REG_DWORD <br></li>
     *      <li>byte[] -> REG_BINARY <br></li>
     *      <li>String[] ->REG_MULTI_SZ <br></li>
     *      <li>String - > REG_SZ <br></li></ul>
     *
     *      Other values would be set as REG_SZ with value .toString()
     */
    private void addRemoveProgramsInstall(String uid, String displayName, String displayIcon, String installLocation, String uninstallString, HashMap<String, Object> parameters) throws NativeException {
        LogManager.log("Add new Add/Remove Programs entry with id [" + uid + "]");
        
        String vacantUid = getVacantUid(uid);
        String key = UNINSTALL_KEY + WindowsRegistry.SEPARATOR + vacantUid;
        
        registry.createKey(HKLM, key);
        
        if (displayName != null) {
            LogManager.log("Set '" + DISPLAY_NAME + "' = [" + displayName + "]");
            
            registry.setStringValue(HKLM, key, DISPLAY_NAME, displayName, false);
        }
        if (installLocation != null) {
            LogManager.log("Set '" + INSTALL_LOCATION + "' = [" + installLocation+ "]");
            
            registry.setStringValue(HKLM, key, INSTALL_LOCATION, installLocation, false);
        }
        if (displayIcon != null) {
            LogManager.log("Set '" + DISPLAY_ICON + "' = [" + displayIcon+ "]");
            
            registry.setStringValue(HKLM, key, DISPLAY_ICON, displayIcon, false);
        }
        if (uninstallString != null) {
            LogManager.log("Set '" + UNINSTALL_STRING + "' = [" + uninstallString+ "]");
            
            registry.setStringValue(HKLM, key, UNINSTALL_STRING, uninstallString, false);
        }
        
        addAditionalParameters(vacantUid, parameters);
    }
    
    private void addRemoveProgramsUninstall(String uid, String installLocation) throws NativeException {
        String properUid = getProperUid(uid, installLocation);
        
        if (properUid != null) {
            registry.deleteKey(HKLM, UNINSTALL_KEY, properUid);
        }
    }
    
    private void addAditionalParameters(String uid, Map<String, Object> parameters) throws NativeException {
        LogManager.log("Trying to set " + parameters.size() + " additional parameters");
        
        String key = UNINSTALL_KEY + WindowsRegistry.SEPARATOR + uid;
        
        for (String name: parameters.keySet()) {
            Object value = parameters.get(name);
            
            LogManager.log(name + " = " + value.toString());
            
            if (value instanceof Short) {
                LogManager.log("Type is short. Set REG_DWORD value");
                
                registry.set32BitValue(HKLM, key, name, ((Short) value).intValue());
            }  else if (value instanceof Integer) {
                LogManager.log("Type is integer. Set REG_DWORD value");
                
                registry.set32BitValue(HKLM, key, name, ((Integer) value).intValue());
            }  else if (value instanceof Long) {
                LogManager.log("Type is long. Set REG_DWORD value");
                
                registry.set32BitValue(HKLM, key, name, ((Long) value).intValue());
            }  else if (value instanceof byte[]) {
                LogManager.log("Type is byte[]. Set REG_BINARY value");
                
                registry.setBinaryValue(HKLM, key, name, (byte[]) value);
            }  else if (value instanceof String[]) {
                LogManager.log("Type is String[]. Set REG_MULTI_SZ value");
                
                registry.setMultiStringValue(HKLM, key, name, (String[]) value);
            }  else if (value instanceof String) {
                LogManager.log("Type is String. Set REG_SZ value");
                
                registry.setStringValue(HKLM, key, name, (String) value, false);
            }  else {
                LogManager.log("Type can`t be determined. Set REG_SZ value");
                
                registry.setStringValue(HKLM, key, name, value.toString(), false);
            }
        }
    }
    
    private String getUidKey(ProductComponent component) {
        return NBI_UID_PREFIX + component.getUid() + "-" + component.getVersion().toString();
    }
    
    private String getVacantUid(final String baseUid) throws NativeException {
        String vacantUid = baseUid;
        
        String key = UNINSTALL_KEY + WindowsRegistry.SEPARATOR + vacantUid;
        if (registry.keyExists(HKLM, key)) {
            for (int index = MIN_UID_INDEX; index < MAX_UID_INDEX; index++) {
                vacantUid = baseUid + UID_SEPARATOR + index;
                key = UNINSTALL_KEY + WindowsRegistry.SEPARATOR + vacantUid;
                
                if (!registry.keyExists(HKLM, key)) {
                    return vacantUid;
                }
            }
            return null;
        } else {
            return vacantUid;
        }
    }
    
    private String getProperUid(final String baseUid, final String installLocation) throws NativeException {
        String properUid = baseUid;
        
        String key = UNINSTALL_KEY + WindowsRegistry.SEPARATOR + properUid;
        if (registry.keyExists(HKLM, key) && registry.getStringValue(HKLM, key, INSTALL_LOCATION).equals(installLocation)) {
            return properUid;
        } else {
            for (int index = MIN_UID_INDEX; index < MAX_UID_INDEX; index++) {
                properUid = baseUid + UID_SEPARATOR + index;
                key = UNINSTALL_KEY + WindowsRegistry.SEPARATOR + properUid;
                
                if (registry.keyExists(HKLM, key) && registry.getStringValue(HKLM, key, INSTALL_LOCATION).equals(installLocation)) {
                    return properUid;
                }
            }
            return null;
        }
    }
    
    // native declarations //////////////////////////////////////////////////////////
    private native boolean isCurrentUserAdmin0();
    
    private native long getFreeSpace0(String string);
    
    private native void createShortcut0(Shortcut shortcut);
    
    private native void deleteFileOnReboot0(String file);
}