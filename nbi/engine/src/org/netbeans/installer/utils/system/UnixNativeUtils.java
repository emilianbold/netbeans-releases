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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.installer.utils.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.SystemUtils.EnvironmentVariableScope;
import org.netbeans.installer.utils.SystemUtils.ExecutionResults;
import org.netbeans.installer.utils.SystemUtils.Platform;
import org.netbeans.installer.utils.SystemUtils.Shortcut;
import org.netbeans.installer.utils.SystemUtils.ShortcutLocationType;
import org.netbeans.installer.utils.exceptions.UnsupportedActionException;
import org.netbeans.installer.utils.system.unix.shell.BourneShell;
import org.netbeans.installer.utils.system.unix.shell.CShell;
import org.netbeans.installer.utils.system.unix.shell.KornShell;
import org.netbeans.installer.utils.system.unix.shell.Shell;
import org.netbeans.installer.utils.system.unix.shell.TCShell;

/**
 *
 * @author Dmitry Lipin
 */

public class UnixNativeUtils extends NativeUtils {
    public static final String LIBRARY_PATH_LINUX = "native/linux.so";
    public static final String LIBRARY_PATH_SOLARIS_SPARC = "native/solaris-sparc.so";
    public static final String LIBRARY_PATH_SOLARIX_X86 = "native/solaris-x86.so";
    public static final String LIBRARY_PATH_MACOSX = "native/libmacosx.dylib";
    
    public UnixNativeUtils() {
    }
    
    protected String getNativeLibraryPath() {
        switch (Platform.getCurrentPlatform()) {
            case LINUX:
                return LIBRARY_PATH_LINUX;
            case SOLARIS_SPARC:
                return LIBRARY_PATH_SOLARIS_SPARC;
            case SOLARIS_X86:
                return LIBRARY_PATH_SOLARIX_X86;
            case MACOS_X_PPC:
            case MACOS_X_X86:
                return LIBRARY_PATH_MACOSX;
            default:
                ErrorManager.notify(ErrorLevel.CRITICAL,
                        "Cannot load native library for UnixSystemUtils - unknown platform");
                return null;
        }
    }
    
    protected void scheduleCleanupNativeLibrary() {
        if(nativeLibraryPath!=null) {
            File file = new File(nativeLibraryPath);
            if(file.exists()) {
                file.deleteOnExit();
            }
        }
    }
    
    public long getFreeSpace(File file) {
        return (file==null || file.getPath().equals("")) ? 0 :
            getFreeSpace0(file.getPath());
    }
    
    public boolean isCurrentUserAdmin() {
        if(isUserAdminSet) {
            return isUserAdmin;
        }
        boolean result = false;
        try {
            ExecutionResults resRealID = executeCommand("id","-ru");
            ExecutionResults resEffID = executeCommand("id","-u");
            String realID = resRealID.getStdOut();
            String effID = resRealID.getStdOut();
            if(realID!=null && effID!=null) {
                result = (realID.equals("0") && effID.equals("0"));
            }
            
        } catch (IOException ex) {
            LogManager.log(ErrorLevel.CRITICAL,
                    "Can`t execute id command");
            LogManager.log(ErrorLevel.CRITICAL,ex);
        }
        isUserAdmin = result;
        isUserAdminSet = true;
        return result;
    }
    
    public void updateApplicationsMenu() {
        try {
            SystemUtils.getInstance().executeCommand(null,new String [] {
                "pkill", "-u", getUserName(), "panel"});
        } catch (IOException ex) {
            LogManager.log(ErrorLevel.WARNING,ex);
        }
    }
    
    public File getShortcutLocation(Shortcut shortcut, ShortcutLocationType locationType) {
        try {
            final String XDG_DATA_HOME = getEnvironmentVariable("XDG_DATA_HOME");
            final String XDG_DATA_DIRS = getEnvironmentVariable("XDG_DATA_DIRS");
            
            File currentUserLocation;
            if (XDG_DATA_HOME == null) {
                currentUserLocation = new File(getUserHomeDirectory(), ".local/share");
            } else {
                currentUserLocation = new File(XDG_DATA_HOME);
            }
            
            File allUsersLocation;
            if (XDG_DATA_DIRS == null) {
                allUsersLocation = new File("/usr/share");
            } else {
                allUsersLocation = new File(XDG_DATA_DIRS.split(getPathSeparator())[0]);
            }
            
            String fileName = shortcut.getFileName();
            if (fileName == null) {
                fileName = shortcut.getExecutable().getName() + ".desktop";
            }
            
            switch (locationType) {
                case CURRENT_USER_DESKTOP:
                    return new File(getUserHomeDirectory(), "Desktop/" + fileName);
                case ALL_USERS_DESKTOP:
                    return new File(getUserHomeDirectory(), "Desktop/" + fileName);
                case CURRENT_USER_START_MENU:
                    return new File(currentUserLocation, "applications/" + fileName);
                case ALL_USERS_START_MENU:
                    return new File(allUsersLocation, "applications/" + fileName);
            }
        } catch (IOException e) {
            ErrorManager.notify(ErrorLevel.ERROR, "Could not obtain the value of an environment variable", e);
        } catch (UnsupportedActionException e) {
            ErrorManager.notify(ErrorLevel.ERROR, "Could not obtain the value of an environment variable", e);
        }
        
        return null;
    }
    
    public File createShortcut(Shortcut shortcut, ShortcutLocationType locationType) throws IOException {
        final File shortcutFile = getShortcutLocation(shortcut, locationType);
        final StringBuilder contents = new StringBuilder();
        
        contents.append("[Desktop Entry]").append(getLineSeparator());
        contents.append("Encoding=UTF-8").append(getLineSeparator());
        
        contents.append("Name=" + shortcut.getName()).append(getLineSeparator());
        contents.append("Exec=" + shortcut.getExecutable()).append(getLineSeparator());
        
        FileUtils.writeFile(shortcutFile, contents);
        
        return shortcutFile;
    }
    
    public void removeShortcut(Shortcut shortcut, ShortcutLocationType locationType, boolean deleteEmptyParents) throws IOException {
        File shortcutFile = getShortcutLocation(shortcut, locationType);
        FileUtils.deleteFile(shortcutFile);
        if(deleteEmptyParents &&
                (locationType == ShortcutLocationType.ALL_USERS_START_MENU ||
                locationType == ShortcutLocationType.CURRENT_USER_START_MENU)) {
            FileUtils.deleteEmptyParents(shortcutFile);
        }
    }
    
    public List<File> findExecutableFiles(File parent) throws IOException {
        List<File> files = new ArrayList<File>();
        
        if (!parent.exists()) {
            return files;
        }
        
        for(File child : parent.listFiles()) {
            if (child.isDirectory()) {
                files.addAll(findExecutableFiles(child));
            } else {
                // name based analysis
                String name = child.getName();
                if (name.endsWith(".sh")) { // shell script
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".pl")) { // perl script
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".py")) { // python script
                    files.add(child);
                    continue;
                }
                
                // contents based analysis
                String line = FileUtils.readFirstLine(child);
                if (line != null) {
                    if (line.startsWith("#!")) { // a script of some sort
                        files.add(child);
                        continue;
                    }
                }
            }
        }
        
        return files;
    }
    
    public List<File> findNonUnixFiles(File parent) throws IOException {
        List<File> files = new ArrayList<File>();
        
        if (!parent.exists()) {
            return files;
        }
        
        for(File child : parent.listFiles()) {
            if (child.isDirectory()) {
                files.addAll(findNonUnixFiles(child));
            } else {
                // name based analysis
                String name = child.getName();
                if (name.endsWith(".bat")) { // dos batch file
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".cmd")) { // windows batch file
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".dll")) { // windows library
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".exe")) { // windows executable
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".com")) { // windows executable
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".vbs")) { // windows script
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".vbe")) { // windows script
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".wsf")) { // windows script
                    files.add(child);
                    continue;
                }
                if (name.endsWith(".wsh")) { // windows script
                    files.add(child);
                    continue;
                }
                
                // contents based analysis - none at this point
            }
        }
        
        return files;
    }
    
    public void chmod(File file, String mode) throws IOException {
        chmod(Arrays.asList(file), mode);
    }
    
    public void chmod(File file, int mode) throws IOException {
        chmod(file, Integer.toString(mode));
    }
    
    public void chmod(List<File> files, String mode) throws IOException {
        for(File file : files) {
            File   directory = file.getParentFile();
            String name      = file.getName();
            
            SystemUtils.getInstance().executeCommand(directory,
                    "chmod",
                    mode,
                    name);
        }
    }
    
    public void removeIrrelevantFiles(File parent) throws IOException {
        FileUtils.deleteFiles(findNonUnixFiles(parent));
    }
    
    public void correctFilesPermissions(File parent) throws IOException {
        chmod(findExecutableFiles(parent), "ugo+x");
    }
    
    // native //////////////////////////////////////////////////////////////////
    private native long getFreeSpace0(String s);
    
    public String getEnvironmentVariable(String name, EnvironmentVariableScope scope, boolean flag) throws IOException, UnsupportedActionException {
        if(EnvironmentVariableScope.PROCESS == scope) {
            return super.getEnvironmentVariable(name,scope,flag);
        } else {
            return System.getenv(name);
        }
    }
    
    public boolean setEnvironmentVariable(String name, String value, EnvironmentVariableScope scope, boolean flag) throws IOException, UnsupportedActionException {
        if(EnvironmentVariableScope.PROCESS == scope) {
            return super.setEnvironmentVariable(name,value,scope,flag);
        } else {
            boolean  result = getCurrentShell().setVar(name,value,scope);
            LogManager.log(ErrorLevel.DEBUG,
                    "... the setting of environment variable " +
                    (result ? "was successfull" : "failed"));
            return result;
            
        }
    }
    
    public Shell getCurrentShell() {
        LogManager.log(ErrorLevel.DEBUG,
                "Getting current shell..");
        LogManager.indent();
        Shell [] avaliableShells =  {
            new BourneShell(),
            new CShell() ,
            new TCShell(),
            new KornShell()
        };
        String shell = System.getenv("SHELL");
        Shell result = null;
        if(shell == null) {
            shell = System.getenv("shell");
        }
        LogManager.log(ErrorLevel.DEBUG,
                "... shell env variable = " + shell);
        
        if(shell != null) {
            if(shell.lastIndexOf(File.separator)!=-1) {
                shell = shell.substring(shell.lastIndexOf(File.separator) + 1);
            }
            LogManager.log(ErrorLevel.DEBUG,
                    "... searching for the shell with name [" + shell +  "] " +
                    "among available shells names");
            for(Shell sh : avaliableShells) {
                if(sh.isCurrentShell(shell)) {
                    result = sh;
                    LogManager.log(ErrorLevel.DEBUG,
                            "... detected shell: " +
                            sh.getClass().getSimpleName());
                    break;
                }
            }
            
        }
        if(result == null) {
            LogManager.log(ErrorLevel.DEBUG,
                    "... no shell found");
        }
        LogManager.unindent();
        LogManager.log(ErrorLevel.DEBUG,
                "... finished detecting shell");
        return result;
    }
}