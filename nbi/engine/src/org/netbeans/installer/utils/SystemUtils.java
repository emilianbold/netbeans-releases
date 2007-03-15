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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.ApplicationDescriptor;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.Shortcut;
import org.netbeans.installer.utils.helper.ShortcutLocationType;
import org.netbeans.installer.utils.system.NativeUtils;

/**
 *
 * @author Kirill Sorokin
 */
public final class SystemUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final long MAX_EXECUTION_TIME = 120000; // 2 minutes
    public static final int  BUFFER_SIZE        = 4096;   // 4 kilobytes
    public static final int  DELAY              = 50;     // 50 milliseconds
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Map<String, String> environment = 
            new ProcessBuilder().environment();
    
    private static NativeUtils nativeUtils;
    
    private static Platform currentPlatform;
    
    // string resolution ////////////////////////////////////////////////////////////
    public static String parseString(String string) {
        return parseString(string, SystemUtils.class.getClassLoader());
    }
    
    public static String parseString(String string, ClassLoader loader) {
        String parsed = string;
        
        // N for Name
        try {
            parsed = parsed.replaceAll("(?<!\\\\)\\$N\\{install\\}", StringUtils.escapeRegExp(getDefaultApplicationsLocation().getAbsolutePath()));
        } catch (NativeException e) {
            ErrorManager.notifyError("Cannot obtain default applications location", e);
        }
        
        parsed = parsed.replaceAll("(?<!\\\\)\\$N\\{home\\}", StringUtils.escapeRegExp(getUserHomeDirectory().getAbsolutePath()));
        parsed = parsed.replaceAll("(?<!\\\\)\\$N\\{temp\\}", StringUtils.escapeRegExp(getTempDirectory().getAbsolutePath()));
        parsed = parsed.replaceAll("(?<!\\\\)\\$N\\{current\\}", StringUtils.escapeRegExp(getCurrentDirectory().getAbsolutePath()));
        
        Matcher matcher;
        
        // P for Properties
        matcher = Pattern.compile("(?<!\\\\)\\$P\\{(.*?), (.*?)(?:, (.*?))?\\}").matcher(parsed);
        while (matcher.find()) {
            String basename        = matcher.group(1);
            String key             = matcher.group(2);
            String argumentsString = matcher.group(3);
            
            if (argumentsString == null) {
                parsed = parsed.replace(matcher.group(), ResourceUtils.getString(basename, key, loader));
            } else {
                Object[] arguments = (Object[]) argumentsString.split(", ?");
                
                parsed = parsed.replace(matcher.group(), ResourceUtils.getString(basename, key, loader, arguments));
            }
        }
        
        // F for Field
        matcher = Pattern.compile("(?<!\\\\)\\$F\\{((?:[a-zA-Z_][a-zA-Z_0-9]*\\.)+[a-zA-Z_][a-zA-Z_0-9]*)\\.([a-zA-Z_][a-zA-Z_0-9]*)\\}").matcher(parsed);
        while (matcher.find()) {
            String classname = matcher.group(1);
            String fieldname = matcher.group(2);
            
            try {
                Object object = loader.loadClass(classname).getField(fieldname).get(null);
                if (object != null) {
                    String value = object.toString();
                    
                    parsed = parsed.replace(matcher.group(), value);
                }
            } catch (IllegalArgumentException e) {
                ErrorManager.notifyDebug("Cannot parse pattern: " + matcher.group(), e);
            } catch (SecurityException e) {
                ErrorManager.notifyDebug("Cannot parse pattern: " + matcher.group(), e);
            } catch (ClassNotFoundException e) {
                ErrorManager.notifyDebug("Cannot parse pattern: " + matcher.group(), e);
            } catch (IllegalAccessException e) {
                ErrorManager.notifyDebug("Cannot parse pattern: " + matcher.group(), e);
            } catch (NoSuchFieldException e) {
                ErrorManager.notifyDebug("Cannot parse pattern: " + matcher.group(), e);
            }
        }
        
        // M for Method
        matcher = Pattern.compile("(?<!\\\\)\\$M\\{((?:[a-zA-Z_][a-zA-Z_0-9]*\\.)+[a-zA-Z_][a-zA-Z_0-9]*)\\.([a-zA-Z_][a-zA-Z_0-9]*)\\(\\)\\}").matcher(parsed);
        while (matcher.find()) {
            String classname = matcher.group(1);
            String methodname = matcher.group(2);
            
            try {
                Method method = loader.loadClass(classname).getMethod(methodname);
                if (method != null) {
                    Object object = method.invoke(null);
                    
                    if (object != null) {
                        String value = object.toString();
                        
                        parsed = parsed.replace(matcher.group(), value);
                    }
                }
            } catch (IllegalArgumentException e) {
                ErrorManager.notifyDebug("Cannot parse pattern: " + matcher.group(), e);
            } catch (SecurityException e) {
                ErrorManager.notifyDebug("Cannot parse pattern: " + matcher.group(), e);
            } catch (ClassNotFoundException e) {
                ErrorManager.notifyDebug("Cannot parse pattern: " + matcher.group(), e);
            } catch (IllegalAccessException e) {
                ErrorManager.notifyDebug("Cannot parse pattern: " + matcher.group(), e);
            } catch (NoSuchMethodException e) {
                ErrorManager.notifyDebug("Cannot parse pattern: " + matcher.group(), e);
            } catch (InvocationTargetException e) {
                ErrorManager.notifyDebug("Cannot parse pattern: " + matcher.group(), e);
            }
        }
        
        // R for Resource
        matcher = Pattern.compile("(?<!\\\\)\\$R\\{(.*?)\\}").matcher(parsed);
        while (matcher.find()) {
            String path = matcher.group(1);
            
            InputStream inputStream = null;
            try {
                inputStream  = ResourceUtils.getResource(path, loader);
                parsed = parsed.replace(matcher.group(), StringUtils.readStream(inputStream));
            } catch (IOException e) {
                ErrorManager.notifyDebug("Cannot parse pattern: " + matcher.group(), e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        ErrorManager.notifyDebug("Cannot close input stream after reading resource: " + matcher.group(), e);
                    }
                }
            }
        }
        
        // S for System Property
        matcher = Pattern.compile("(?<!\\\\)\\$S\\{(.*?)\\}").matcher(parsed);
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = System.getProperty(name);
            
            parsed = parsed.replace(matcher.group(), value);
        }
        
        // E for Environment Variable
        matcher = Pattern.compile("(?<!\\\\)\\$E\\{(.*?)\\}").matcher(parsed);
        while (matcher.find()) {
            try {
                String name = matcher.group(1);
                String value = getEnvironmentVariable(name);
                
                parsed = parsed.replace(matcher.group(), value);
            } catch (NativeException e) {
                ErrorManager.notifyDebug("Cannot parse pattern: " + matcher.group(), e);
            }
        }
        
        parsed.replace("\\$", "$");
        parsed.replace("\\\\", "\\");
        
        return parsed;
    }
    
    public static File parsePath(String string) {
        return parsePath(string, SystemUtils.class.getClassLoader());
    }
    
    public static File parsePath(String path, ClassLoader loader) {
        final String separator = getFileSeparator();
        
        String parsed = parseString(path, loader);
        
        parsed = parsed.replace("\\", separator);
        parsed = parsed.replace("/", separator);
        
        try {
            if (parsed.contains(separator + ".." + separator) ||
                    parsed.contains(separator + "." + separator) ||
                    parsed.endsWith(separator + "..") ||
                    parsed.endsWith(separator + ".")) {
                return new File(parsed).getCanonicalFile();
            }
        } catch (IOException e) {
            ErrorManager.notifyDebug("Could not get the cannonical path", e);
        }
        
        return new File(parsed).getAbsoluteFile();
    }
    
    public static File getUserHomeDirectory() {
        return new File(System.getProperty("user.home"));
    }
    
    public static String getUserName() {
        return System.getProperty("user.name");
    }
    
    public static boolean isCurrentUserAdmin() throws NativeException {
        return getNativeUtils().isCurrentUserAdmin();
    }
    
    public static File getCurrentDirectory() {
        return new File(".");
    }
    
    public static File getTempDirectory() {
        return new File(System.getProperty("java.io.tmpdir"));
    }
    
    public static File getDefaultApplicationsLocation() throws NativeException {
        return getNativeUtils().getDefaultApplicationsLocation();
    }
    
    public static File getCurrentJavaHome() {
        return new File(System.getProperty("java.home"));
    }
    
    public static File getPacker() {
        if (isWindows()) {
            return new File(getCurrentJavaHome(), "bin/pack200.exe");
        } else {
            return new File(getCurrentJavaHome(), "bin/pack200");
        }
    }
    
    public static File getUnpacker() {
        if (isWindows()) {
            return new File(getCurrentJavaHome(), "bin/unpack200.exe");
        } else {
            return new File(getCurrentJavaHome(), "bin/unpack200");
        }
    }
    
    public static String getLineSeparator() {
        return System.getProperty("line.separator");
    }
    
    public static String getFileSeparator() {
        return System.getProperty("file.separator");
    }
    
    public static String getPathSeparator() {
        return System.getProperty("path.separator");
    }
    
    public static long getFreeSpace(File file) throws NativeException {
        return getNativeUtils().getFreeSpace(file);
    }
    
    public static ExecutionResults executeCommand(String... command) throws IOException {
        return executeCommand(null, command);
    }
    
    public static ExecutionResults executeCommand(File workingDirectory, String... command) throws IOException {
        // construct the initial log message
        StringBuilder stringBuilder = new StringBuilder();
        for(String temp : command) {
            stringBuilder.append(temp).append(" "); //NOI18N
        }
        
        if (workingDirectory == null) {
            workingDirectory = getCurrentDirectory();
        }
        
        LogManager.log(ErrorLevel.MESSAGE, "executing command: " + stringBuilder + ", in directory: " + workingDirectory);
        LogManager.indent();
        
        StringBuilder processStdOut = new StringBuilder();
        StringBuilder processStdErr = new StringBuilder();
        int           errorLevel = ExecutionResults.TIMEOUT_ERRORCODE;
        
        ProcessBuilder builder= new ProcessBuilder(command).directory(workingDirectory);
        
        if (environment != null) {
            builder.environment().clear();
            builder.environment().putAll(environment);
            environment = null;
        }
        Process process = builder.start();
        
        long runningTime;
        for (runningTime = 0; runningTime < MAX_EXECUTION_TIME; runningTime += DELAY) {
            String string;
            
            string = StringUtils.readStream(process.getInputStream());
            if (string.length() > 0) {
                BufferedReader reader = new BufferedReader(new StringReader(string));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    LogManager.log(ErrorLevel.MESSAGE, "[stdout]: " + line);
                }
                
                processStdOut.append(string);
            }
            
            string = StringUtils.readStream(process.getErrorStream());
            if (string.length() > 0) {
                BufferedReader reader = new BufferedReader(new StringReader(string));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    LogManager.log(ErrorLevel.MESSAGE, "[stdout]: " + line);
                }
                
                processStdErr.append(string);
            }
            
            try {
                errorLevel = process.exitValue();
                break;
            } catch (IllegalThreadStateException e) {
                ; // do nothing - the process is still running
            }
            
            try {
                Thread.sleep(DELAY);
            }  catch (InterruptedException e) {
                ErrorManager.notifyDebug("Interrupted", e);
            }
        }
        
        if (runningTime >= MAX_EXECUTION_TIME) {
            process.destroy();
            LogManager.log(ErrorLevel.MESSAGE, "[return]: killed by timeout");
        }  else {
            LogManager.log(ErrorLevel.MESSAGE, "[return]: " + errorLevel);
        }
        
        process.destroy();
        
        LogManager.unindent();
        LogManager.log(ErrorLevel.MESSAGE, "... command execution finished");
        
        return new ExecutionResults(errorLevel, processStdOut.toString(), processStdErr.toString());
    }
    
    public static boolean isPathValid(String path) {
        return getNativeUtils().isPathValid(path);
    }
    
    public static boolean isPortAvailable(int port, int... forbiddenPorts) {
        // check whether the port is in the restricted list, if it is, there is no
        // sense to check whether it is physically available
        for (int forbidden: forbiddenPorts) {
            if (port == forbidden) {
                return false;
            }
        }
        
        // if the port is not in the restricted list, we'll try to open a server
        // socket on it, if we fail, then someone is already listening on this port 
        // and it is occupied
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(port);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    ErrorManager.notifyError(
                            "Could not close server socket on port " + port, 
                            e);
                }
            }
        }
    }
    
    public static int getAvailablePort(int basePort, int... forbiddenPorts) {
        // increment the port value until we find an available port
        int port = basePort;
        while (!isPortAvailable(port, forbiddenPorts)) {
            port++;
        }
        
        return port;
    }
    
    public static boolean isDeletingAllowed(File file) {
        return getNativeUtils().isDeletingAllowed(file);
    }
    
    public static File getShortcutLocation(Shortcut shortcut, ShortcutLocationType locationType) throws NativeException {
        return getNativeUtils().getShortcutLocation(shortcut, locationType);
    }
    
    public static File createShortcut(Shortcut shortcut, ShortcutLocationType locationType) throws NativeException {
        return getNativeUtils().createShortcut(shortcut, locationType);
    }
    
    public static void removeShortcut(Shortcut shortcut, ShortcutLocationType locationType, boolean deleteEmptyParents) throws NativeException {
        getNativeUtils().removeShortcut(shortcut, locationType, deleteEmptyParents);
    }
    
    public static void addComponentToSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException {
        getNativeUtils().addComponentToSystemInstallManager(descriptor);
    }
    
    public static void removeComponentFromSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException {
        getNativeUtils().removeComponentFromSystemInstallManager(descriptor);
    }
    
    public static String getEnvironmentVariable(String name) throws NativeException {
        return getEnvironmentVariable(name, EnvironmentScope.PROCESS, true);
    }
    
    public static String getEnvironmentVariable(String name, EnvironmentScope scope, boolean expand) throws NativeException {
        return getNativeUtils().getEnvironmentVariable(name, scope, expand);
    }
    
    public static void setEnvironmentVariable(String name, String value) throws NativeException {
        setEnvironmentVariable(name, value, EnvironmentScope.PROCESS, true);
    }
    
    public static void setEnvironmentVariable(String name, String value, EnvironmentScope scope, boolean expand) throws NativeException {
        getNativeUtils().setEnvironmentVariable(name, value, scope, expand);
    }
    
    public static List<File> findIrrelevantFiles(File parent) throws IOException {
        return getNativeUtils().findIrrelevantFiles(parent);
    }
    
    public static List<File> findIrrelevantFiles(File... parents) throws IOException {
        List<File> list = new LinkedList<File>();
        
        for (File parent: parents) {
            list.addAll(findIrrelevantFiles(parent));
        }
        
        return list;
    }
    
    public static void removeIrrelevantFiles(File parent) throws IOException {
        FileUtils.deleteFiles(findIrrelevantFiles(parent));
    }
    
    public static void removeIrrelevantFiles(File... parents) throws IOException {
        for (File file: parents) {
            removeIrrelevantFiles(file);
        }
    }
    
    public static List<File> findExecutableFiles(File parent) throws IOException {
        return getNativeUtils().findExecutableFiles(parent);
    }
    
    public static List<File> findExecutableFiles(File... parents) throws IOException {
        List<File> list = new LinkedList<File>();
        
        for (File parent: parents) {
            list.addAll(findExecutableFiles(parent));
        }
        
        return list;
    }
    
    public static void correctFilesPermissions(File parent) throws IOException {
        getNativeUtils().correctFilesPermissions(parent);
    }
    
    public static void correctFilesPermissions(File... parents) throws IOException {
        for (File file: parents) {
            correctFilesPermissions(file);
        }
    }
    
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }  catch (InterruptedException e) {
            ErrorManager.notify(ErrorLevel.DEBUG,
                    "Interrupted while sleeping", e);
        }
    }
    
    public static Map<String, String> getEnvironment() {
        return environment;
    }
    
    public static Platform getCurrentPlatform() {
        if (currentPlatform == null) {
            if (System.getProperty("os.name").contains("Windows")) {
                currentPlatform = Platform.WINDOWS;
            }
            if (System.getProperty("os.name").contains("Linux")) {
                currentPlatform = Platform.LINUX;
            }
            if (System.getProperty("os.name").contains("Mac OS X") && 
                    System.getProperty("os.arch").contains("ppc")) {
                currentPlatform = Platform.MACOS_X_PPC;
            }
            if (System.getProperty("os.name").contains("Mac OS X") && 
                    System.getProperty("os.arch").contains("i386")) {
                currentPlatform = Platform.MACOS_X_X86;
            }
            if (System.getProperty("os.name").contains("SunOS") && 
                    System.getProperty("os.arch").contains("sparc")) {
                currentPlatform = Platform.SOLARIS_SPARC;
            }
            if (System.getProperty("os.name").contains("SunOS") && 
                    System.getProperty("os.arch").contains("x86")) {
                currentPlatform = Platform.SOLARIS_X86;
            }
        }
        
        return currentPlatform;
    }
    
    public static String getHostName() {
    try {
      String hostName = InetAddress.getLocalHost().getHostName();
      if (hostName != null) {
        return hostName;
      }
    } catch (UnknownHostException e) {
      LogManager.log(ErrorLevel.MESSAGE, e);      
    }
    
    return "localhost"; //NOI18N
  }
    
    // platforms probes /////////////////////////////////////////////////////////////
    public static boolean isWindows() {
        return getCurrentPlatform() == Platform.WINDOWS;
    }
    
    public static boolean isMacOS() {
        return (getCurrentPlatform() == Platform.MACOS_X_X86) ||
                (getCurrentPlatform() == Platform.MACOS_X_PPC);
    }
    
    // miscellanea //////////////////////////////////////////////////////////////////
    public static boolean intersects(List<? extends Object> list1, List<? extends Object> list2) {
        for (int i = 0; i < list1.size(); i++) {
            for (int j = 0; j < list2.size(); j++) {
                if (list1.get(i).equals(list2.get(j))) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public static <T> List<T> intersect(List<? extends T> list1, List<? extends T> list2) {
        final List<T> intersection = new LinkedList<T>();
        
        for (T item: list1) {
            if (list2.contains(item)) {
                intersection.add(item);
            }
        }
        
        return intersection;
    }
    
    // native accessor //////////////////////////////////////////////////////////////
    public static synchronized NativeUtils getNativeUtils() {
        if (nativeUtils == null) {
            nativeUtils = NativeUtils.getInstance();
        }
        
        return nativeUtils;
    }
}
