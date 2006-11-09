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
import org.netbeans.installer.utils.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.SystemUtils.EnvironmentScope;
import org.netbeans.installer.utils.SystemUtils.Shortcut;
import org.netbeans.installer.utils.SystemUtils.ShortcutLocationType;
import org.netbeans.installer.utils.applications.JDKUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import static org.netbeans.installer.utils.system.WindowsNativeUtils.WindowsRegistry.HKCU;
import static org.netbeans.installer.utils.system.WindowsNativeUtils.WindowsRegistry.HKLM;

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
            
            addRemoveProgramsInstall(uid, displayName, icon, installPath, uninstallString, null);
        } else {
            LogManager.log(ErrorLevel.WARNING, "Can`t find cached engine.");
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
    private native boolean isCurrentUserAdmin0() throws NativeException;
    
    private native long getFreeSpace0(String string) throws NativeException;
    
    private native void createShortcut0(Shortcut shortcut) throws NativeException;
    
    private native void deleteFileOnReboot0(String file) throws NativeException;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    /**
     *
     * @author Dmitry Lipin
     * @author Kirill Sorokin
     */
    public static class WindowsRegistry {
        /////////////////////////////////////////////////////////////////////////////
        // Constants
        public static final int HKEY_CLASSES_ROOT              = 0;
        public static final int HKEY_CURRENT_USER              = 1;
        public static final int HKEY_LOCAL_MACHINE             = 2;
        public static final int HKEY_USERS                     = 3;
        public static final int HKEY_CURRENT_CONFIG            = 4;
        
        public static final int HKEY_DYN_DATA                  = 5;
        public static final int HKEY_PERFORMANCE_DATA          = 6;
        public static final int HKEY_PERFORMANCE_NLSTEXT       = 7;
        public static final int HKEY_PERFORMANCE_TEXT          = 8;
        
        public static final int HKCR                           = HKEY_CLASSES_ROOT;
        public static final int HKCU                           = HKEY_CURRENT_USER;
        public static final int HKLM                           = HKEY_LOCAL_MACHINE;
        
        public static final int REG_NONE                       = 0;
        public static final int REG_SZ                         = 1;
        public static final int REG_EXPAND_SZ                  = 2;
        public static final int REG_BINARY                     = 3;
        public static final int REG_DWORD_LITTLE_ENDIAN        = 4;
        public static final int REG_DWORD                      = 4;
        public static final int REG_DWORD_BIG_ENDIAN           = 5;
        public static final int REG_LINK                       = 6;
        public static final int REG_MULTI_SZ                   = 7;
        public static final int REG_RESOURCE_LIST              = 8;
        public static final int REG_FULL_RESOURCE_DESCRIPTOR   = 9;
        public static final int REG_RESOURCE_REQUIREMENTS_LIST = 10;
        public static final int REG_QWORD_LITTLE_ENDIAN        = 11;
        public static final int REG_QWORD                      = 11;
        
        public static final String SEPARATOR = "\\";
        
        /////////////////////////////////////////////////////////////////////////////
        // Instance
        
        // queries //////////////////////////////////////////////////////////////////
        /**
         * Checks whether the specified key exists in the registry.
         *
         * @param section The section of the registry
         * @param key The specified key
         * @return <i>true</i> if the specified key exists, <i>false</i> otherwise
         */
        public boolean keyExists(int section, String key) throws NativeException {
            try {
                return keyExists0(section, key);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /**
         * Checks whether the specified value exists in the registry.
         *
         * @param section The section of the registry
         * @param key The specified key
         * @param value The specified value
         * @return <i>true</i> if the specified value exists, <i>false</i> otherwise
         */
        public boolean valueExists(int section, String key, String value) throws NativeException {
            try {
                return valueExists0(section, key, value);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /**
         * Checks whether the specified value exists in the registry.
         *
         * @param section The section of the registry
         * @param key The specified key
         * @param value The specified value
         * @return <i>true</i> if the specified value exists, <i>false</i> otherwise
         */
        public boolean keyEmpty(int section, String key) throws NativeException {
            try {
                return keyEmpty0(section, key);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /**
         * Get the number of the subkeys of the specified key.
         *
         * @param section The section of the registry
         * @param key The specified key
         * @return If the key doesn`t exist or can`t be accessed then return -1.
         * <br>Otherwise return the number of subkeys
         */
        public int countSubKeys(int section, String key) throws NativeException {
            try {
                return countSubKeys0(section, key);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /** Get the number of the values of the specified key.
         * @param section The section of the registry
         * @param key The specified key
         * @return If the key doesn`t exist or can`t be accessed then return -1.
         * <br>Otherwise return the number of values
         */
        public int countValues(int section, String key) throws NativeException {
            try {
                return countValues0(section, key);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /**
         * Get the array of subkey names of the specified key.
         *
         * @param section The section of the registry
         * @param key The specified key
         * @return If the key doesn`t exist or can`t be accessed then return <i>null</i>
         * <br>Otherwise return the array of subkey names
         */
        public String[] getSubKeyNames(int section, String key) throws NativeException {
            try {
                return getSubkeyNames0(section, key);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /** Get the array of values names of the specified key.
         * @param section The section of the registry
         * @param key The specified key
         * @return If the key doesn`t exist or can`t be accessed then return <i>null</i>
         * <br>Otherwise return the array of value names
         */
        public String[] getValueNames(int section, String key) throws NativeException {
            try {
                return getValueNames0(section, key);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /**
         * Returns the type of the value.
         *
         * @param section The section of the registry
         * @param key     The specified key
         * @param value   The specified value
         *
         * @return The possible values are:<br>
         *
         * <code>REG_NONE</code><br>
         * <code>REG_SZ</code><br>
         * <code>REG_EXPAND_SZ</code><br>
         * <code>REG_BINARY</code><br>
         * <code>REG_DWORD</code>=<code>REG_DWORD_LITTLE_ENDIAN</code><br>
         * <code>REG_DWORD_BIG_ENDIAN</code><br>
         * <code>REG_LINK</code><br>
         * <code>REG_MULTI_SZ</code><br>
         * <code>REG_RESOURCE_LIST</code><br>
         * <code>REG_FULL_RESOURCE_DESCRIPTOR</code><br>
         * <code>REG_RESOURCE_REQUIREMENTS_LIST</code><br>
         * <code>REG_QWORD</code>=<code>REG_QWORD_LITTLE_ENDIAN</code>
         */
        public int getValueType(int section, String key, String value) throws NativeException {
            try {
                return getValueType0(section, key, value);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        // key operations ///////////////////////////////////////////////////////////
        /**
         * Create the new key in the registry.
         *
         * @param section The section of the registry
         * @param key The specified key
         * @return <i>true</i> if the key was successfully created,
         * <br> <i>false</i> otherwise
         */
        public void createKey(int section, String key) throws NativeException {
            createKey(section, getParentKey(key), getChildKey(key));
        }
        
        /**
         * Create the new key in the registry.
         *
         * @param section The section of the registry
         * @param parent key The specified parent key
         * @param parent key The specified child key
         * @return <i>true</i> if the key was successfully created,
         * <br> <i>false</i> otherwise
         */
        public void createKey(int section, String parent, String child) throws NativeException {
            try {
                createKey0(section, parent, child);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /**
         * Delete the specified key exists in the registry. Note that if the key
         * contains subkeys then it would not be deleted.
         *
         * @param section The section of the registry
         * @param key The specified key
         * @return <i>true</i> if the specified key was deleted, <i>false</i> otherwise
         */
        public void deleteKey(int section, String key) throws NativeException {
            deleteKey(section, getParentKey(key), getChildKey(key));
        }
        
        /**
         * Delete the specified key exists in the registry.
         *
         * @param section The section of the registry
         * @param parentKey The specified parent key
         * @param childKey The specified child key
         * @return <i>true</i> if the specified key was deleted, <i>false</i> otherwise
         */
        public void deleteKey(int section, String parent, String child) throws NativeException {
            try {
                deleteKey0(section, parent, child);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        // value operations /////////////////////////////////////////////////////////
        /**
         * Delete the specified value exists in the registry.
         *
         * @param section The section of the registry
         * @param key The specified key
         * @param value The specified value
         * @return <i>true</i> if the specified value was deleted, <i>false</i> otherwise
         */
        public void deleteValue(int section, String key, String value) throws NativeException {
            try {
                deleteValue0(section, key, value);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /**
         *
         * @param section
         * @param key
         * @param name
         * @return
         */
        public String getStringValue(int section, String key, String name) throws NativeException {
            return getStringValue(section, key, name, false);
        }
        
        /** Get string value.
         * @param section The section of the registry
         * @param key The specified key
         * @param name The specified value
         * @param expandable
         *      If <code>expandable</code> is <i>true</i> and
         *      the type of the value is REG_EXPAND_SZ the value would be expanded
         * @return The value of the name, <i>null</i> in case of any error
         */
        public String getStringValue(int section, String key, String name, boolean expand) throws NativeException {
            try {
                return getStringValue0(section, key, name, expand);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /**
         *
         * @param section
         * @param key
         * @param name
         * @param value
         */
        public void setStringValue(int section, String key, String name, String value) throws NativeException {
            setStringValue(section, key, name, value, false);
        }
        
        /** Set string value.
         * @param section The section of the registry
         * @param key The specified key
         * @param name The specified value
         * @param value The specified value of the <code>name</code>
         * @param expandable
         *      If <code>expandable</code> is <i>true</i> then the type would be
         *       <code>REG_EXPAND_SZ</code> or <code>REG_SZ</code> otherwise
         * @return <i>true</i> if the value was successfully set
         * <br> <i>false</i> otherwise
         */
        public void setStringValue(int section, String key, String name, String value, boolean expandable) throws NativeException {
            try {
                setStringValue0(section, key, name, value, expandable);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /** Get integer value.
         * @param section The section of the registry
         * @param key The specified key
         * @param name The specified value
         * @return The value of the name, <i>-1</i> in case of any error
         */
        public int get32BitValue(int section, String key, String name) throws NativeException {
            try {
                return get32BitValue0(section, key, name);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /** Set REG_DWORD value.
         * @param section The section of the registry
         * @param key The specified key
         * @param name The specified value
         * @param value The specified value of the <code>name</code>
         * @return <i>true</i> if the value was successfully set
         * <br> <i>false</i> otherwise
         */
        public void set32BitValue(int section, String key, String name, int value) throws NativeException {
            try {
                set32BitValue0(section, key, name, value);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /** Get the array of strings of the specified value
         * @param section The section of the registry
         * @param key The specified key
         * @param name The specified value
         * @return The multri-string value of the name, <i>null</i> in case of any error
         */
        public String[] getMultiStringValue(int section, String key, String name) throws NativeException {
            try {
                return getMultiStringValue0(section, key, name);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /** Set REG_MULTI_SZ value.
         * @param section The section of the registry
         * @param key The specified key
         * @param name The specified value
         * @param value The specified value of the <code>name</code>
         * @return <i>true</i> if the value was successfully set
         * <br> <i>false</i> otherwise
         */
        public void setMultiStringValue(int section, String key, String name, String[] value) throws NativeException {
            try {
                setMultiStringValue0(section, key, name, value);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /**
         * Get binary value.
         *
         * @param section The section of the registry
         * @param key The specified key
         * @param name The specified value
         * @return The binary value of the name, <i>null</i> in case of any error
         */
        public byte[] getBinaryValue(int section, String key, String name) throws NativeException {
            try {
                return getBinaryValue0(section, key, name);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        /** Set binary (REG_BINARY) value.
         * @param section The section of the registry
         * @param key The specified key
         * @param name The specified value
         * @param value The specified value of the <code>name</code>
         * @return <i>true</i> if the value was successfully set
         * <br> <i>false</i> otherwise
         */
        public void setBinaryValue(int section, String key, String name, byte[] value) throws NativeException {
            try {
                setBinaryValue0(section, key, name, value);
            } catch (UnsatisfiedLinkError e) {
                throw new NativeException("Cannot access native method", e);
            }
        }
        
        // private //////////////////////////////////////////////////////////////////
        /**
         *
         */
        private WindowsRegistry() {
            // does nothing
        }
        
        /**
         *
         * @param key
         * @return
         */
        private String getParentKey(String key) {
            String temp = key;
            
            // strip the trailing separators
            while (temp.endsWith(SEPARATOR)) {
                temp = temp.substring(0, temp.length() - 1);
            }
            
            int index = temp.indexOf(SEPARATOR);
            if (index != -1) {
                return temp.substring(0, index);
            } else {
                return null;
            }
        }
        
        /**
         *
         * @param key
         * @return
         */
        private String getChildKey(String key) {
            String temp = key;
            
            // strip the trailing separators
            while (temp.endsWith(SEPARATOR)) {
                temp = temp.substring(0, temp.length() - 1);
            }
            
            int index = temp.indexOf(SEPARATOR);
            if (index != -1) {
                return temp.substring(index + 1);
            } else {
                return null;
            }
        }
        
        // native declarations //////////////////////////////////////////////////////
        private native boolean keyExists0(int section, String key) throws NativeException;
        
        private native boolean valueExists0(int section, String key, String value) throws NativeException;
        
        private native boolean keyEmpty0(int section, String key) throws NativeException;
        
        private native int countSubKeys0(int section, String key) throws NativeException;
        
        private native int countValues0(int section, String key) throws NativeException;
        
        private native String[] getSubkeyNames0(int section, String key) throws NativeException;
        
        private native String[] getValueNames0  (int section, String key) throws NativeException;
        
        private native int getValueType0(int section, String key, String value) throws NativeException;
        
        private native void createKey0(int section, String parent, String child) throws NativeException;
        
        private native void deleteKey0(int section, String parent, String child) throws NativeException;
        
        private native void deleteValue0(int section, String key, String value) throws NativeException;
        
        private native String getStringValue0(int section, String key, String name, boolean expand) throws NativeException;
        
        private native void setStringValue0(int section, String key, String name, String value, boolean expandable);
        
        private native int get32BitValue0(int section, String key, String name) throws NativeException;
        
        private native void set32BitValue0(int section, String key, String name, int value) throws NativeException;
        
        private native String[] getMultiStringValue0(int section, String key, String name) throws NativeException;
        
        private native void setMultiStringValue0(int section, String key, String name, String[] value) throws NativeException;
        
        private native byte[] getBinaryValue0(int section, String key, String name) throws NativeException;
        
        private native void setBinaryValue0(int section, String key, String name, byte[] value) throws NativeException;
    }
}