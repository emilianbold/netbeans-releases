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
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.utils.*;
import org.netbeans.installer.utils.SystemUtils.EnvironmentVariableScope;
import org.netbeans.installer.utils.SystemUtils.Shortcut;
import org.netbeans.installer.utils.SystemUtils.ShortcutLocationType;
import org.netbeans.installer.utils.applications.JDKUtils;
import org.netbeans.installer.utils.exceptions.UnsupportedActionException;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;

/**
 *
 * @author Dmitry Lipin
 */

public class WindowsNativeUtils extends NativeUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String LIBRARY_PATH = "native/windows.dll";
    
    public static final String UNINSTALL_KEY = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall";//NOI18N
    
    public static final String DISPLAY_NAME = "DisplayName";
    public static final String DISPLAY_ICON = "DisplayIcon";
    public static final String UNINSTALL_STRING = "UninstallString";
    public static final String INSTALL_LOCATION = "InstallLocation";
    
    private static final WindowsRegistry registry = WindowsRegistry.getInstance();
    
    private static final String SHELL_FOLDERS_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders";
    
    private static final String NBI_UID_PREFIX = "nbi-";
    private static final String UID_SEPARATOR = "____";
    
    private static final int INITIAL_UID_INDEX = 1;
    private static final int MAX_UID_INDEX = 100;
    
    private static final String USER_ENVIRONMENT_REGISTRY_KEY = "Environment";
    private static final String SYSTEM_ENVIRONMENT_REGISTRY_KEY = "SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment";
    private static final String RUNONCE_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\RunOnce";
    private static final String RUNONCE_DELETE_VALUE_NAME = "NBI Temporary Files Delete";
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private File defaultApplicationsLocation;
    
    // constructor //////////////////////////////////////////////////////////////////
    WindowsNativeUtils() {
        loadNativeLibrary(LIBRARY_PATH);
    }
    
    // parent implementation ////////////////////////////////////////////////////////
    public boolean isCurrentUserAdmin() {
        return isCurrentUserAdmin0(); // fallback to native code
    }
    
    public File getDefaultApplicationsLocation() {
        if (defaultApplicationsLocation == null) {
            defaultApplicationsLocation = SystemUtils.getUserHomeDirectory();
            
            try {
                String path = SystemUtils.getEnvironmentVariable("ProgramFiles");
                
                if (path != null) {
                    defaultApplicationsLocation = new File(path).getAbsoluteFile();
                } else {
                    ErrorManager.notify(ErrorLevel.DEBUG, "Value of the environment variable ProgramFiles is not set");
                }
            } catch (IOException e) {
                ErrorManager.notify(ErrorLevel.ERROR, "Could not obtain the value of an environment variable", e);
            }
        }
        
        return defaultApplicationsLocation;
    }
    
    public long getFreeSpace(File file) {
        if ((file == null) || file.getPath().equals("") || !isPathValid(file.getPath())) {
            return 0;
        } else {
            return getFreeSpace0(file.getPath());
        }
    }
    
    public boolean isPathValid(String path) {
        return path.matches("^[A-Z,a-z]:(\\\\[^\\\\\\/:*\"<>|\\s]([^\\\\\\/:*\"<>|\t]*[^\\\\\\/:*\"<>|\\s])?)*\\\\?$");
    }
    
    public File getShortcutLocation(Shortcut shortcut, ShortcutLocationType locationType) {
        String shortcutPath = shortcut.getRelativePath();
        if (shortcutPath == null) {
            shortcutPath = "";
        }
        
        String shortcutFileName = shortcut.getFileName();
        if (shortcutFileName == null) {
            shortcutFileName = shortcut.getName() + ".lnk";
        }
        
        try {
            final String allUsersRootPath = SystemUtils.getEnvironmentVariable("allusersprofile");
            
            switch (locationType) {
                case CURRENT_USER_DESKTOP:
                    String userDesktop = registry.getStringValue(WindowsRegistry.HKEY_CURRENT_USER, SHELL_FOLDERS_KEY, "Desktop", false);
                    if (userDesktop == null) {
                        userDesktop = SystemUtils.getUserHomeDirectory() + File.separator + "Desktop";
                    }
                    
                    return new File(userDesktop, shortcutFileName);
                    
                case ALL_USERS_DESKTOP:
                    String commonDesktop = registry.getStringValue(WindowsRegistry.HKEY_LOCAL_MACHINE, SHELL_FOLDERS_KEY, "Common Desktop", false);
                    if (commonDesktop == null) {
                        commonDesktop = allUsersRootPath + File.separator + "Desktop";
                    }
                    
                    return new File(commonDesktop, shortcutFileName);
                    
                case CURRENT_USER_START_MENU:
                    String userStartMenu = registry.getStringValue(WindowsRegistry.HKEY_CURRENT_USER, SHELL_FOLDERS_KEY, "Programs", false);
                    if (userStartMenu == null) {
                        userStartMenu = SystemUtils.getUserHomeDirectory() + File.separator + "Start Menu" + File.separator + "Programs";
                    }
                    
                    return new File(userStartMenu, shortcutPath + File.separator + shortcutFileName);
                    
                case ALL_USERS_START_MENU:
                    String commonStartMenu = registry.getStringValue(WindowsRegistry.HKEY_LOCAL_MACHINE, SHELL_FOLDERS_KEY, "Common Programs", false);
                    if (commonStartMenu == null) {
                        commonStartMenu = SystemUtils.getUserHomeDirectory() + File.separator + "Start Menu" + File.separator + "Programs";
                    }
                    
                    return new File(commonStartMenu, shortcutPath + File.separator + shortcutFileName);
            }
        } catch (IOException e) {
            ErrorManager.notify(ErrorLevel.ERROR, "Could not obtain the value of an environment variable", e);
        }
        
        return null;
    }
    
    public File createShortcut(Shortcut shortcut, ShortcutLocationType locationType) throws IOException {
        File shortcutFile = getShortcutLocation(shortcut, locationType);
        
        shortcut.setPath(shortcutFile.getAbsolutePath());
        
        int nativeResult = createShortcut0(shortcut);
        if (nativeResult == 0) {
            return shortcutFile;
        } else {
            throw new IOException("Cannot create shortcut, error occurred in native code, error code: " + nativeResult);
        }
    }
    
    public void removeShortcut(Shortcut shortcut, ShortcutLocationType locationType, boolean cleanupParents) throws IOException {
        File shortcutFile = getShortcutLocation(shortcut, locationType);
        
        FileUtils.deleteFile(shortcutFile);
        
        if (cleanupParents) {
            switch (locationType) {
                case CURRENT_USER_DESKTOP:
                case ALL_USERS_DESKTOP:
                    break;
                case CURRENT_USER_START_MENU:
                case ALL_USERS_START_MENU:
                    FileUtils.deleteEmptyParents(shortcutFile);
                    break;
            }
        }
    }
    
    public void addComponentToSystemInstallManager(ProductComponent component) {
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
    
    public void removeComponentFromSystemInstallManager(ProductComponent component) {
        String uid         = getUidKey(component);
        String installPath = component.getInstallationLocation().getPath();
        
        addRemoveProgramsUninstall(uid, installPath);
    }
    
    public String getEnvironmentVariable(String name, EnvironmentVariableScope scope, boolean expand) {
        String value = null;
        
        if ((scope != null) && (name != null)) {
            if (scope == EnvironmentVariableScope.PROCESS) {
                value = System.getenv(name);
            } else {
                String rootKey;
                int    section;
                if (scope == EnvironmentVariableScope.CURRENT_USER) {
                    rootKey = USER_ENVIRONMENT_REGISTRY_KEY;
                    section = WindowsRegistry.HKEY_CURRENT_USER;
                }
                if (scope == EnvironmentVariableScope.ALL_USERS) {
                    rootKey = SYSTEM_ENVIRONMENT_REGISTRY_KEY;
                    section = WindowsRegistry.HKEY_LOCAL_MACHINE;
                }
                
                if (registry.isKeyExists(section, rootKey)) {
                    value = registry.getStringValue(section, rootKey, name, expand);
                } else {
                    LogManager.log(ErrorLevel.DEBUG, "Root environment key doesn`t exist. Can`t get environment variable");
                }
            }
        }
        
        return value;
    }
    
    public void setEnvironmentVariable(String name, String value, EnvironmentVariableScope scope, boolean expand) {
        if ((name != null) && (scope != null)) {
            if (scope == EnvironmentVariableScope.PROCESS) {
                SystemUtils.getEnvironment().put(name, value);
            } else {
                String rootKey;
                int    section;
                if (scope == EnvironmentVariableScope.CURRENT_USER) {
                    rootKey = USER_ENVIRONMENT_REGISTRY_KEY;
                    section = WindowsRegistry.HKEY_CURRENT_USER;
                }
                if (scope == EnvironmentVariableScope.ALL_USERS) {
                    rootKey = SYSTEM_ENVIRONMENT_REGISTRY_KEY;
                    section = WindowsRegistry.HKEY_LOCAL_MACHINE;
                }
                
                if (registry.isKeyExists(section, rootKey)) {
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
        deleteFileOnReboot(new File(libraryPath));
    }
    
    // windows-specific operations /////////////////////////////////////////////
    public WindowsRegistry getWindowsRegistry() {
        return registry;
    }
    
    
    public void deleteFileOnReboot(File file) {
        String filename = file.getPath();
        if (isCurrentUserAdmin()) {
            deleteFileOnReboot0(filename);
        } else {
            boolean result = true;
            result = registry.isKeyExists(registry.HKEY_CURRENT_USER,RUNONCE_KEY);
            if (!result) {
                result = registry.createKey(registry.HKEY_CURRENT_USER,RUNONCE_KEY);
            }
            if (result) {
                String name = RUNONCE_DELETE_VALUE_NAME;
                String value = "";
                result = registry.isValueExists(registry.HKEY_CURRENT_USER,RUNONCE_KEY,name);
                if (result) {
                    value = registry.getStringValue(registry.HKEY_CURRENT_USER,RUNONCE_KEY,name);
                }
                if (!result || value==null || value.equals("")) {
                    value = "cmd /q /c del /F /Q";
                }
                registry.setStringValue(registry.HKEY_CURRENT_USER,RUNONCE_KEY,name,
                        value + " \"" + filename + "\"");
            }
            
        }
    }
    
    // private /////////////////////////////////////////////////////////////////
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
    private void addRemoveProgramsInstall(String uid, String displayName, String displayIcon, String installLocation, String uninstallString, HashMap<String, Object> additionalParameters) {
        LogManager.log(ErrorLevel.MESSAGE, "Add new Add/Remove Programs entry with id [" + uid + "]");
        
        String vacantUid = getVacantUid(uid);
        String key = UNINSTALL_KEY + WindowsRegistry.WR_SEPARATOR + vacantUid;
        
        registry.createKey(WindowsRegistry.HKEY_LOCAL_MACHINE, key);
        
        if (displayName != null) {
            LogManager.log("Set '" + DISPLAY_NAME + "' = [" + displayName + "]");
            
            registry.setStringValue(WindowsRegistry.HKEY_LOCAL_MACHINE, key, DISPLAY_NAME, displayName, false);
        }
        if (installLocation != null) {
            LogManager.log("Set '" + INSTALL_LOCATION + "' = [" + installLocation+ "]");
            
            registry.setStringValue(WindowsRegistry.HKEY_LOCAL_MACHINE, key, INSTALL_LOCATION, installLocation, false);
        }
        if (displayIcon != null) {
            LogManager.log("Set '" + DISPLAY_ICON + "' = [" + displayIcon+ "]");
            
            registry.setStringValue(WindowsRegistry.HKEY_LOCAL_MACHINE, key, DISPLAY_ICON, displayIcon, false);
        }
        if (uninstallString != null) {
            LogManager.log(ErrorLevel.MESSAGE, "Set '" + UNINSTALL_STRING + "' = [" + uninstallString+ "]");
            
            registry.setStringValue(WindowsRegistry.HKEY_LOCAL_MACHINE, key, UNINSTALL_STRING, uninstallString, false);
        }
        
        addAditionalParameters(vacantUid, additionalParameters);
    }
    
    private void addRemoveProgramsUninstall(String uidInit, String installLocation) {
        String uid = uidInit;
        boolean finded = false;
        int index = INITIAL_UID_INDEX;
        LogManager.log(ErrorLevel.MESSAGE,
                "Uninstalling add/remove programs entry with uid: " + uid +
                "\ninstallLocation = " + installLocation);
        do {
            if (index > MAX_UID_INDEX) {
                LogManager.log(ErrorLevel.MESSAGE,
                        "Maximim of index has been reached. No neccessary was found.. nothing to remove.");
                break;
            }
            if (registry.isKeyExists(WindowsRegistry.HKEY_LOCAL_MACHINE,
                    UNINSTALL_KEY + WindowsRegistry.WR_SEPARATOR + uid)) {
                String insloc = registry.getStringValue(WindowsRegistry.HKEY_LOCAL_MACHINE,
                        UNINSTALL_KEY + WindowsRegistry.WR_SEPARATOR + uid, INSTALL_LOCATION);
                LogManager.log(ErrorLevel.MESSAGE,
                        "... finded key with uid " + uid +
                        " and install location " + insloc);
                if (insloc != null && insloc.equals(installLocation)) {
                    finded = true;
                    break;
                } else {
                    LogManager.log(ErrorLevel.MESSAGE,
                            "... installLocation differs from the expected one. Try another key..");
                }
            } else {
                LogManager.log(ErrorLevel.MESSAGE,
                        "... key with uid " + uid + "was not found");
            }
            uid = uidInit + UID_SEPARATOR + index;
            index ++ ;
            
        } while(!finded);
        
        return !finded ? false :
            registry.deleteKey(WindowsRegistry.HKEY_LOCAL_MACHINE, UNINSTALL_KEY, uid);
    }
    
    private boolean addAditionalParameters(String uid, HashMap<String, Object> additionalParameters) {
        
        boolean result = true;
        if (additionalParameters==null) {
            return true;
        }
        
        int size = additionalParameters.size();
        if (size==0) {
            return true;
        }
        
        LogManager.log(ErrorLevel.MESSAGE,
                "Trying to set " + size + " additional parameters");
        Object[] keys = additionalParameters.keySet().toArray();
        
        for (int i = 0; i<size; i++) {
            LogManager.log(ErrorLevel.MESSAGE,
                    SystemUtils.getInstance().getLineSeparator());
            LogManager.log(ErrorLevel.MESSAGE,
                    "ValueName = " + keys[i].toString());
            if (!(keys[i] instanceof String)) {
                continue;
            }
            
            Object value = additionalParameters.get(keys[i]);
            LogManager.log(ErrorLevel.MESSAGE,
                    "Value = " + value.toString());
            
            if (value instanceof Short) {
                LogManager.log(ErrorLevel.MESSAGE,
                        "Type is short. Set REG_DWORD value");
                
                result = result &&
                        registry.set32BitValue(WindowsRegistry.HKEY_LOCAL_MACHINE,
                        UNINSTALL_KEY + WindowsRegistry.WR_SEPARATOR + uid,
                        (String) keys[i], ((Short) value).intValue());
            }  else if (value instanceof Integer) {
                LogManager.log(ErrorLevel.MESSAGE,
                        "Type is integer. Set REG_DWORD value");
                result = result &&
                        registry.set32BitValue(WindowsRegistry.HKEY_LOCAL_MACHINE,
                        UNINSTALL_KEY + WindowsRegistry.WR_SEPARATOR + uid,
                        (String) keys[i], ((Integer) value).intValue());
            }  else if (value instanceof Long) {
                LogManager.log(ErrorLevel.MESSAGE,
                        "Type is long. Set REG_DWORD value");
                
                result = result &&
                        registry.set32BitValue(WindowsRegistry.HKEY_LOCAL_MACHINE,
                        UNINSTALL_KEY + WindowsRegistry.WR_SEPARATOR + uid,
                        (String) keys[i], ((Long) value).intValue());
            }  else if (value instanceof byte[]) {
                LogManager.log(ErrorLevel.MESSAGE,
                        "Type is byte[]. Set REG_BINARY value");
                
                result = result &&
                        registry.setBinaryValue(WindowsRegistry.HKEY_LOCAL_MACHINE,
                        UNINSTALL_KEY + WindowsRegistry.WR_SEPARATOR + uid,
                        (String) keys[i], (byte[]) value);
            }  else if (value instanceof String[]) {
                LogManager.log(ErrorLevel.MESSAGE,
                        "Type is String[]. Set REG_MULTI_SZ value");
                
                result = result &&
                        registry.setMultiStringValue(WindowsRegistry.HKEY_LOCAL_MACHINE,
                        UNINSTALL_KEY + WindowsRegistry.WR_SEPARATOR + uid,
                        (String) keys[i], (String[]) value);
            }  else if (value instanceof String) {
                LogManager.log(ErrorLevel.MESSAGE,
                        "Type is String. Set REG_SZ value");
                
                result = result &&
                        registry.setStringValue(WindowsRegistry.HKEY_LOCAL_MACHINE,
                        UNINSTALL_KEY + WindowsRegistry.WR_SEPARATOR + uid,
                        (String) keys[i], (String) value, false);
            }  else {
                LogManager.log(ErrorLevel.MESSAGE,
                        "Type can`t be determined. Set REG_SZ value");
                
                result = result &&
                        registry.setStringValue(WindowsRegistry.HKEY_LOCAL_MACHINE,
                        UNINSTALL_KEY + WindowsRegistry.WR_SEPARATOR + uid,
                        (String) keys[i], value.toString(), false);
            }
            
        }
        return result;
    }
    
    private String getUidKey(ProductComponent component) {
        return NBI_UID_PREFIX + component.getUid() + "-" + component.getVersion().toString();
    }
    
    private String getVacantUid(final String baseUid) {
        String vacantUid = baseUid;
        
        String key = UNINSTALL_KEY + WindowsRegistry.WR_SEPARATOR + vacantUid;
        if (registry.isKeyExists(WindowsRegistry.HKEY_LOCAL_MACHINE, key)) {
            for (int index = INITIAL_UID_INDEX; index < MAX_UID_INDEX; index++) {
                vacantUid = baseUid + UID_SEPARATOR + index;
                key = UNINSTALL_KEY + WindowsRegistry.WR_SEPARATOR + vacantUid;
                
                if (!registry.isKeyExists(WindowsRegistry.HKEY_LOCAL_MACHINE, key)) {
                    return vacantUid;
                }
            }
            return null;
        } else {
            return vacantUid;
        }
    }
    
    private String getProperUid(final String baseUid, final String installLocation) {
        String properUid = baseUid;
        
        String key = UNINSTALL_KEY + WindowsRegistry.WR_SEPARATOR + properUid;
        if (registry.isKeyExists(WindowsRegistry.HKEY_LOCAL_MACHINE, key) && registry.getStringValue(WindowsRegistry.HKEY_LOCAL_MACHINE, key, INSTALL_LOCATION).equals(installLocation)) {
            return properUid;
        } else {
            for (int index = INITIAL_UID_INDEX; index < MAX_UID_INDEX; index++) {
                properUid = baseUid + UID_SEPARATOR + index;
                key = UNINSTALL_KEY + WindowsRegistry.WR_SEPARATOR + properUid;
                
                if (registry.isKeyExists(WindowsRegistry.HKEY_LOCAL_MACHINE, key) && registry.getStringValue(WindowsRegistry.HKEY_LOCAL_MACHINE, key, INSTALL_LOCATION).equals(installLocation)) {
                    return properUid;
                }
            }
            return null;
        }
    }
    
    // native //////////////////////////////////////////////////////////////////
    private native int deleteFileOnReboot0(String file);
    
    private native boolean isCurrentUserAdmin0();
    
    private native long getFreeSpace0(String string);
    
    private native int createShortcut0(Shortcut shortcut);
}