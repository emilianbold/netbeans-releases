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
package org.netbeans.installer.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.netbeans.installer.download.DownloadManager;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.UnrecognizedObjectException;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class SystemUtils {
    ////////////////////////////////////////////////////////////////////////////
    // Static
    private static SystemUtils instance;
    
    public static synchronized SystemUtils getInstance() {
        if (instance == null) {
            switch (Platform.getCurrentPlatform()) {
                case WINDOWS:
                    instance = new WindowsSystemUtils();
                    break;
                default:
                    instance = new GenericSystemUtils();
                    break;
                    
            }
            instance.loadNativeLibrary();
        }
        return instance;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance
    public abstract String parseString(String string);
    
    public abstract String parsePath(String path);
    
    public abstract File resolvePath(String path);
    
    public abstract File getDefaultApplicationsLocation();
    
    public abstract Platform getCurrentPlatform();
    
    public abstract void sleep(long millis);
    
    public abstract String getLineSeparator();
    
    public abstract File getUserHomeDirectory();
    
    public abstract File getCurrentDirectory();
    
    public abstract File getTempDirectory();
    
    public abstract File getSystemDrive();
    
    public abstract long getFreeSpace(File file);
    
    protected abstract String getNativeLibraryPath();
    
    public abstract void loadNativeLibrary();
    
    public abstract int createShortcut(Shortcut shortcut);
    
    public abstract ExecutionResults executeCommand(File workingDirectory, String... command) throws IOException;
    
    public abstract ExecutionResults executeCommand(String... command) throws IOException;
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private static class GenericSystemUtils extends SystemUtils {
        public String parseString(String string) {
            String parsedString = string;
            
            parsedString = parsedString.replaceAll("(?<!\\\\)\\$\\{install\\}", escapeForRE(getDefaultApplicationsLocation().getAbsolutePath()));
            parsedString = parsedString.replaceAll("(?<!\\\\)\\$\\{home\\}", escapeForRE(getUserHomeDirectory().getAbsolutePath()));
            parsedString = parsedString.replaceAll("(?<!\\\\)\\$\\{systemdrive\\}", escapeForRE(getSystemDrive().getAbsolutePath()));
            
            parsedString.replace("\\$", "$");
            parsedString.replace("\\\\", "\\");
            
            return parsedString;
        }
        
        public String parsePath(String path) {
            String parsedPath = path;
            
            parsedPath = parseString(parsedPath);
            
            parsedPath = parsedPath.replace('\\', File.separatorChar);
            parsedPath = parsedPath.replace('/', File.separatorChar);
            
            return parsedPath;
        }
        
        public File resolvePath(String path) {
            return new File(parsePath(path));
        }
        
        private String escapeForRE(String string) {
            return string.replace("\\", "\\\\");
        }
        
        public File getDefaultApplicationsLocation() {
            switch (Platform.getCurrentPlatform()) {
                case WINDOWS:
                    return new File(System.getenv("ProgramFiles"));
                default:
                    return new File(System.getProperty("user.home"));
            }
        }
        
        public Platform getCurrentPlatform() {
            return Platform.getCurrentPlatform();
        }
        
        public void sleep(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                ErrorManager.getInstance().notify(ErrorLevel.DEBUG, "Interrupted while sleeping", e);
            }
        }
        
        public String getLineSeparator() {
            return System.getProperty("line.separator");
        }
        
        
        
        public File getUserHomeDirectory() {
            return new File(System.getProperty("user.home"));
        }
        
        public File getCurrentDirectory() {
            return new File(".");
        }
        
        public File getTempDirectory() {
            return new File(System.getProperty("java.io.tmpdir"));
        }
        
        public long getFreeSpace(File file) {
            return Long.MAX_VALUE;
        }
        protected String getNativeLibraryPath() {
            return null;
        }
        
        public void loadNativeLibrary() {
            
            String libraryPath = getNativeLibraryPath();
            
            if(libraryPath!=null) {
                FileOutputStream outputStream=null;
                File file = null;
                InputStream inputStream= null;
                
                try {
                    inputStream = getClass().
                            getClassLoader().
                            getResource(libraryPath).
                            openStream();
                    file = new File(getTempDirectory().getPath() +
                            File.separator + "nbi-native-lib.tmp");
                    outputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    while (inputStream.available() > 0) {
                        outputStream.write(buffer, 0, inputStream.read(buffer));
                    }
                    outputStream.close();
                    System.load(file.getPath());
                    
                } catch(IOException ex) {
                    System.out.println("Can`t write library.");
                    ex.printStackTrace();
                } catch(UnsatisfiedLinkError ex) {
                    System.out.println("Can`t load native library. ");
                    ex.printStackTrace();
                } finally {
                    try {
                        if(outputStream!=null) outputStream.close();
                    } catch (IOException ex) {
                    }
                    try {
                        if(inputStream!=null) inputStream.close();
                    } catch (IOException ex) {
                    }
                    if(file!=null && !file.delete()) {
                        file.deleteOnExit();
                    }
                }
            }
        }
        
        public int createShortcut(Shortcut shortcut) {
            return 0;
        }
        
        public File getSystemDrive() {
            switch (Platform.getCurrentPlatform()) {
                case WINDOWS:
                    return new File(System.getenv("SystemDrive") + "\\");
                default:
                    return new File("/");
            }
        }
        
        public ExecutionResults executeCommand(File workingDirectory, String... command) throws IOException {
            // construct the initial log message
            StringBuilder stringBuilder = new StringBuilder();
            for (String temp: command) {
                stringBuilder.append(temp).append(" "); //NOI18N
            }
            
            if (workingDirectory == null) {
                workingDirectory = getCurrentDirectory();
            }
            
            LogManager.getInstance().log(ErrorLevel.MESSAGE, "executing command: " + stringBuilder + ", in directory: " + workingDirectory);
            LogManager.getInstance().indent();
            
            StringBuilder processStdOut = new StringBuilder();
            StringBuilder processStdErr = new StringBuilder();
            int           errorLevel    = ExecutionResults.TIMEOUT_ERRORCODE;
            
            Process process = new ProcessBuilder(command).directory(workingDirectory).start();
            
            long runningTime;
            for (runningTime = 0; runningTime < MAX_EXECUTION_TIME; runningTime += DELAY) {
                StringBuilder builder;
                
                builder = readStream(process.getInputStream());
                if (builder.length() > 0) {
                    BufferedReader reader = new BufferedReader(new StringReader(builder.toString()));
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        LogManager.getInstance().log(ErrorLevel.MESSAGE, "[stdout]: " + line);
                    }
                    
                    processStdOut.append(builder);
                }
                
                builder = readStream(process.getErrorStream());
                if (builder.length() > 0) {
                    BufferedReader reader = new BufferedReader(new StringReader(builder.toString()));
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        LogManager.getInstance().log(ErrorLevel.MESSAGE, "[stdout]: " + line);
                    }
                    
                    processStdErr.append(builder);
                }
                
                try {
                    errorLevel = process.exitValue();
                    break;
                } catch (IllegalThreadStateException e) {
                    ; // do nothing - the process is still running
                }
                
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    ErrorManager.getInstance().notify(ErrorLevel.DEBUG, e);
                }
            }
            
            if (runningTime >= MAX_EXECUTION_TIME) {
                process.destroy();
                LogManager.getInstance().log(ErrorLevel.MESSAGE, "[return]: killed by timeout");
            } else {
                LogManager.getInstance().log(ErrorLevel.MESSAGE, "[return]: " + errorLevel);
            }
            
            LogManager.getInstance().unindent();
            LogManager.getInstance().log(ErrorLevel.MESSAGE, "... command execution finished");
            
            return new ExecutionResults(errorLevel, processStdOut.toString(), processStdErr.toString());
        }
        
        public ExecutionResults executeCommand(String... command) throws IOException {
            return executeCommand(null, command);
        }
        
        private StringBuilder readStream(InputStream stream) throws IOException {
            StringBuilder builder = new StringBuilder();
            
            byte[] buffer = new byte[BUFFER_SIZE];
            while (stream.available() > 0) {
                int read = stream.read(buffer);
                
                String readString = new String(buffer, 0, read);
                for (String string: readString.split("(?:\n\r|\r\n|\n|\r)")) {
                    builder.append(string).append("\n");
                }
            }
            
            return builder;
        }
    }
    
    public static class WindowsSystemUtils extends GenericSystemUtils {
        
        public static final String WIN32_DLL_LOCATION = "native/win32.dll";
        
        public static final String UNINSTALL_KEY =
                "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall";//NOI18N
        
        public static final String DISPLAY_NAME = "DisplayName";
        public static final String DISPLAY_ICON = "DisplayIcon";
        public static final String UNINSTALL_STRING = "UninstallString";
        public static final String INSTALL_LOCATION = "InstallLocation";
        
        public static final Win32Registry registry = Win32Registry.getInstance();
        
        private native long getFreeSpace0(String s);
        
        public long getFreeSpace(File file) {
            return (file==null || file.getPath().equals("")) ? 0 :
                getFreeSpace0(file.getPath());
        }
        
        private native int createShortcut0(Shortcut shortcut);
        
        public int createShortcut(Shortcut shortcut) {
            return createShortcut0(shortcut);
        }
        
        protected String getNativeLibraryPath() {
            return WIN32_DLL_LOCATION;
        }
        
        public Win32Registry getWin32Registry() {
            return registry;
        }
        
        private boolean addAditionalParameters(String uid, HashMap <String, Object> additionalParameters) {
            boolean result = true;
            if(additionalParameters==null) {
                return true;
            }
            
            int size = additionalParameters.size();
            if(size==0) {
                return true;
            }
            
            LogManager.getInstance().log(ErrorLevel.MESSAGE,
                    "Trying to set " + size + " additional parameters");
            Object [] keys = additionalParameters.keySet().toArray();
            
            for(int i=0;i<size;i++) {
                LogManager.getInstance().log(ErrorLevel.MESSAGE,
                        SystemUtils.getInstance().getLineSeparator());
                LogManager.getInstance().log(ErrorLevel.MESSAGE,
                        "ValueName = " + keys[i].toString());
                if(! (keys[i] instanceof String)) {
                    continue;
                }
                
                Object value = additionalParameters.get(keys[i]);
                LogManager.getInstance().log(ErrorLevel.MESSAGE,
                        "Value = " + value.toString());
                
                if(value instanceof Short) {
                    LogManager.getInstance().log(ErrorLevel.MESSAGE,
                            "Type is short. Set REG_DWORD value");
                    result = result &&
                            registry.set32BitValue(Win32Registry.HKEY_LOCAL_MACHINE,
                            UNINSTALL_KEY + Win32Registry.WR_SEPARATOR + uid,
                            (String)keys[i], ((Short)value).intValue());
                } else if(value instanceof Integer) {
                    LogManager.getInstance().log(ErrorLevel.MESSAGE,
                            "Type is integer. Set REG_DWORD value");
                    result = result &&
                            registry.set32BitValue(Win32Registry.HKEY_LOCAL_MACHINE,
                            UNINSTALL_KEY + Win32Registry.WR_SEPARATOR + uid,
                            (String)keys[i], ((Integer)value).intValue());
                } else if(value instanceof Long) {
                    LogManager.getInstance().log(ErrorLevel.MESSAGE,
                            "Type is long. Set REG_DWORD value");
                    result = result &&
                            registry.set32BitValue(Win32Registry.HKEY_LOCAL_MACHINE,
                            UNINSTALL_KEY + Win32Registry.WR_SEPARATOR + uid,
                            (String)keys[i], ((Long)value).intValue());
                } else if(value instanceof byte[]) {
                    LogManager.getInstance().log(ErrorLevel.MESSAGE,
                            "Type is byte[]. Set REG_BINARY value");
                    result = result &&
                            registry.setBinaryValue(Win32Registry.HKEY_LOCAL_MACHINE,
                            UNINSTALL_KEY + Win32Registry.WR_SEPARATOR + uid,
                            (String)keys[i], (byte[])value);
                } else if(value instanceof String[]) {
                    LogManager.getInstance().log(ErrorLevel.MESSAGE,
                            "Type is String[]. Set REG_MULTI_SZ value");
                    result = result &&
                            registry.setMultiStringValue(Win32Registry.HKEY_LOCAL_MACHINE,
                            UNINSTALL_KEY + Win32Registry.WR_SEPARATOR + uid,
                            (String)keys[i], (String[]) value);
                } else if(value instanceof String) {
                    LogManager.getInstance().log(ErrorLevel.MESSAGE,
                            "Type is String. Set REG_SZ value");
                    result = result &&
                            registry.setStringValue(Win32Registry.HKEY_LOCAL_MACHINE,
                            UNINSTALL_KEY + Win32Registry.WR_SEPARATOR + uid,
                            (String)keys[i], (String)value, false);
                } else {
                    LogManager.getInstance().log(ErrorLevel.MESSAGE,
                            "Type can`t be determined. Set REG_SZ value");
                    result = result &&
                            registry.setStringValue(Win32Registry.HKEY_LOCAL_MACHINE,
                            UNINSTALL_KEY + Win32Registry.WR_SEPARATOR + uid,
                            (String)keys[i], value.toString(), false);
                }
                
            }
            return result;
        }
        
        /** Add new entry in Add/Remove programs.<br>
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
         *
         *
         * @return <i>true</i> if everything is OK, <i>false</i> otherwise.
         */
        public boolean addRemoveProgramsInstall(String uid,String displayName,
                String displayIcon, String installLocation,
                String uninstallString, HashMap <String, Object> additionalParameters) {
            
            LogManager.getInstance().log(ErrorLevel.MESSAGE,
                    "Add new Add/Remove Programs entry with id [" + uid + "]");
            
            if(registry.isKeyExists(Win32Registry.HKEY_LOCAL_MACHINE,
                    UNINSTALL_KEY + Win32Registry.WR_SEPARATOR + uid)) {
                
                LogManager.getInstance().log(ErrorLevel.MESSAGE,
                        "This entry already exist. No modification is done. Just exit");
                return false;
            }
            boolean result = registry.createKey(Win32Registry.HKEY_LOCAL_MACHINE,
                    UNINSTALL_KEY,uid);
            if(!result) {
                LogManager.getInstance().log(ErrorLevel.MESSAGE,
                        "The new regitsry key can`t be created. Exit.");
                return false;
            }
            if(displayName!=null) {
                LogManager.getInstance().log(ErrorLevel.MESSAGE,
                        "Set '" + DISPLAY_NAME + "' = [" + displayName + "]");
                result = result &&
                        registry.setStringValue(Win32Registry.HKEY_LOCAL_MACHINE,
                        UNINSTALL_KEY + Win32Registry.WR_SEPARATOR + uid,
                        DISPLAY_NAME, displayName, false);
            }
            
            if(installLocation!=null) {
                LogManager.getInstance().log(ErrorLevel.MESSAGE,
                        "Set '" + INSTALL_LOCATION + "' = [" + installLocation+ "]");
                result = result &&
                        registry.setStringValue(Win32Registry.HKEY_LOCAL_MACHINE,
                        UNINSTALL_KEY + Win32Registry.WR_SEPARATOR + uid,
                        INSTALL_LOCATION, installLocation, false);
            }
            if(displayIcon!=null) {
                LogManager.getInstance().log(ErrorLevel.MESSAGE,
                        "Set '" + DISPLAY_ICON + "' = [" + displayIcon+ "]");
                result = result &&
                        registry.setStringValue(Win32Registry.HKEY_LOCAL_MACHINE,
                        UNINSTALL_KEY + Win32Registry.WR_SEPARATOR + uid,
                        DISPLAY_ICON, displayIcon, false);
            }
            if(uninstallString!=null) {
                LogManager.getInstance().log(ErrorLevel.MESSAGE,
                        "Set '" + UNINSTALL_STRING + "' = [" + uninstallString+ "]");
                result = result &&
                        registry.setStringValue(Win32Registry.HKEY_LOCAL_MACHINE,
                        UNINSTALL_KEY + Win32Registry.WR_SEPARATOR + uid,
                        UNINSTALL_STRING, uninstallString, false);
            }
            result = result && addAditionalParameters(uid,additionalParameters);
            return result;
        }
        
        public boolean addRemoveProgramsUninstall(String uid) {
            if(!registry.isKeyExists(Win32Registry.HKEY_LOCAL_MACHINE,
                    UNINSTALL_KEY + Win32Registry.WR_SEPARATOR + uid)) {
                return false;
            }
            return registry.deleteKey(Win32Registry.HKEY_LOCAL_MACHINE,
                    UNINSTALL_KEY + Win32Registry.WR_SEPARATOR + uid);
        }
        
    }
    
    public static enum Platform {
        WINDOWS("windows", "Windows"),
        LINUX("linux", "Linux"),
        SOLARIS_X86("solaris-x86", "Solaris X86"),
        SOLARIS_SPARC("solaris-sparc", "Solaris Sparc"),
        MACOS_X_PPC("macos-x-ppc", "MacOS X (PPC)"),
        MACOS_X_X86("macos-x-x86", "MacOS X (Intel)");
        
        public static Platform parsePlatform(String name) throws UnrecognizedObjectException {
            for (Platform platform: Platform.values()) {
                if (platform.name.equals(name)) {
                    return platform;
                }
            }
            
            throw new UnrecognizedObjectException("Platform \"" + name + "\" is not recognized.");
        }
        
        public static List<Platform> parsePlatforms(String platformsString) throws UnrecognizedObjectException {
            if (platformsString.equals("all")) {
                return Arrays.asList(Platform.values());
            } else {
                List<Platform> platforms = new ArrayList<Platform>();
                
                for (String name: platformsString.split(" ")) {
                    Platform platform = parsePlatform(name);
                    if (!platforms.contains(platform)) {
                        platforms.add(platform);
                    }
                }
                return platforms;
            }
        }
        
        public static Platform getCurrentPlatform() {
            if (System.getProperty("os.name").contains("Windows")) {
                return Platform.WINDOWS;
            }
            if (System.getProperty("os.name").contains("Linux")) {
                return Platform.LINUX;
            }
            if (System.getProperty("os.name").contains("Mac OS X") && System.getProperty("os.arch").contains("ppc")) {
                return Platform.MACOS_X_PPC;
            }
            if (System.getProperty("os.name").contains("Mac OS X") && System.getProperty("os.arch").contains("i386")) {
                return Platform.MACOS_X_X86;
            }
            if (System.getProperty("os.name").contains("SunOS") && System.getProperty("os.arch").contains("sparc")) {
                return Platform.SOLARIS_SPARC;
            }
            if (System.getProperty("os.name").contains("SunOS") && System.getProperty("os.arch").contains("x86")) {
                return Platform.SOLARIS_X86;
            }
            
            return null;
        }
        
        public static boolean isWindows() {
            return Platform.getCurrentPlatform() == Platform.WINDOWS;
        }
        
        public static boolean isMacOS() {
            return (Platform.getCurrentPlatform() == Platform.MACOS_X_X86) ||
                    (Platform.getCurrentPlatform() == Platform.MACOS_X_PPC);
        }
        
        private String name;
        private String displayName;
        
        private Platform(String aName, String aDisplayName) {
            name = aName;
            displayName = aDisplayName;
        }
        
        public boolean equals(Platform platform) {
            return name.equals(platform.name);
        }
        
        public String getName() {
            return name;
        }
        
        public String toString() {
            return displayName;
        }
    }
    
    public static class ExecutionResults {
        public static final int TIMEOUT_ERRORCODE = Integer.MAX_VALUE;
        
        private int    errorCode = TIMEOUT_ERRORCODE;
        private String stdOut    = "";
        private String stdErr    = "";
        
        public ExecutionResults() {
            // do nothing
        }
        
        public ExecutionResults(final int errorCode, final String stdOut, final String stdErr) {
            this.errorCode = errorCode;
            this.stdOut    = stdOut;
            this.stdErr    = stdErr;
        }
        
        public int getErrorCode() {
            return errorCode;
        }
        
        public String getStdOut() {
            return stdOut;
        }
        
        public String getStdErr() {
            return stdErr;
        }
    }
    
    public static class Shortcut {
        private String shortcutName;
        private String shortcutPath;
        private String path;
        private String description;
        private String iconPath;
        private String workingDirectory;
        private String arguments;
        
        public String getShortcutName() {
            return shortcutName;
        }
        
        public void setShortcutName(String shortcutName) {
            this.shortcutName = shortcutName;
        }
        
        public String getShortcutPath() {
            return shortcutPath;
        }
        
        public void setShortcutPath(String shortcutPath) {
            this.shortcutPath = shortcutPath;
        }
        
        public String getPath() {
            return path;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getIconPath() {
            return iconPath;
        }
        
        public void setIconPath(String iconPath) {
            this.iconPath = iconPath;
        }
        
        public String getWorkingDirectory() {
            return workingDirectory;
        }
        
        public void setWorkingDirectory(String workingDirectory) {
            this.workingDirectory = workingDirectory;
        }
        
        public String getArguments() {
            return arguments;
        }
        
        public void setArguments(String arguments) {
            this.arguments = arguments;
        }
        public Shortcut() {
            
        }
    }
    
    public static final long MAX_EXECUTION_TIME = 120000; // 2 minutes seconds
    public static final int  BUFFER_SIZE        = 4096;   // 4 kilobytes
    public static final int  DELAY              = 50;     // 50 milliseconds
}
