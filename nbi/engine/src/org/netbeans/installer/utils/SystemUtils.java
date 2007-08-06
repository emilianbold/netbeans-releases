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

package org.netbeans.installer.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.CodeSigner;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.utils.UiUtils.CertificateAcceptanceStatus;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.ApplicationDescriptor;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.FinishHandler;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.system.shortcut.FileShortcut;
import org.netbeans.installer.utils.system.shortcut.Shortcut;
import org.netbeans.installer.utils.helper.ShortcutLocationType;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.NativeUtils;
import org.netbeans.installer.utils.system.launchers.Launcher;
import org.netbeans.installer.utils.system.launchers.LauncherFactory;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
import org.netbeans.installer.utils.system.shortcut.LocationType;

/**
 *
 * @author Kirill Sorokin
 */
public final class SystemUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Map<String, String> environment =
            new ProcessBuilder().environment();
    
    private static NativeUtils nativeUtils;
    
    private static KeyStore caStore;
    private static KeyStore permanentTrustedStore;
    private static KeyStore sessionTrustedStore;
    private static KeyStore deniedStore;
    
    private static Platform currentPlatform;
    
    private static File localDirectory;
    private static FinishHandler finishHandler;
    
    // string resolution ////////////////////////////////////////////////////////////
    public static String resolveString(String string) {
        return resolveString(string, SystemUtils.class.getClassLoader());
    }
    
    public static String resolveString(String string, ClassLoader loader) {
        String parsed = string;
            
        if (parsed == null) {
            return null;
        }
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
    
    public static File resolvePath(String string) {
        return resolvePath(string, SystemUtils.class.getClassLoader());
    }
    
    public static File resolvePath(String path, ClassLoader loader) {
        final String separator = getFileSeparator();
        
        String parsed = resolveString(path, loader);
        
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
    
    // system info //////////////////////////////////////////////////////////////////
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
    
    public static boolean isCurrentJava64Bit() {
        return System.getProperty("os.arch").equals("amd64") ||
                System.getProperty("os.arch").equals("sparcv9");
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
        LogManager.log("[SystemUtils] getFreeSpace");
        LogManager.indent();
        LogManager.log(ErrorLevel.DEBUG,
                "... getting free space [requested path]  : " + file.getPath());
        File directory = file;
        while (!directory.exists() || !directory.isDirectory()) {
            directory = directory.getParentFile();
        }
        LogManager.log(ErrorLevel.DEBUG,
                "... getting free space [existing parent] : " + directory.getPath());
        long space = getNativeUtils().getFreeSpace(directory);
        LogManager.unindent();
        LogManager.log(ErrorLevel.DEBUG, "... free space is : " + space);
        return space;
    }
    
    public static ExecutionResults executeCommand(String... command) throws IOException {
        return executeCommand(null, command);
    }
    
    public static ExecutionResults executeCommand(File workingDirectory, String... command) throws IOException {
        // construct the initial log message
        String commandString = StringUtils.asString(command, StringUtils.SPACE);
        
        if (workingDirectory == null) {
            workingDirectory = getCurrentDirectory();
        }
        
        LogManager.log(ErrorLevel.MESSAGE,
                "executing command: " + commandString +
                ", in directory: " + workingDirectory);
        LogManager.indent();
        
        StringBuilder processStdOut = new StringBuilder();
        StringBuilder processStdErr = new StringBuilder();
        int           errorLevel = ExecutionResults.TIMEOUT_ERRORCODE;
        
        ProcessBuilder builder= new ProcessBuilder(command).directory(workingDirectory);
        
        builder.environment().clear();
        builder.environment().putAll(environment);
        setDefaultEnvironment();
        
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
                    LogManager.log(ErrorLevel.MESSAGE, "[stderr]: " + line);
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
        
        // if the port is not in the allowed range - return false
        if ((port < 0) && (port > 65535)) {
            return false;
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
        // increment the port value until we find an available port or stumble into
        // the upper bound
        int port = basePort;
        while ((port < 65535) && !isPortAvailable(port, forbiddenPorts)) {
            port++;
        }
        
        if (port == 65535) {
            port = 0;
            while ((port < basePort) && !isPortAvailable(port, forbiddenPorts)) {
                port++;
            }
            
            if (port == basePort) {
                return -1;
            } else {
                return port;
            }
        } else {
            return port;
        }
    }
    
    public static boolean isDeletingAllowed(File file) {
        return getNativeUtils().isDeletingAllowed(file);
    }
    
    @Deprecated
    private static LocationType toLocationType(ShortcutLocationType type) {
        LocationType tp = null;
        switch(type) {
            case CURRENT_USER_DESKTOP :
                tp = LocationType.CURRENT_USER_DESKTOP;
                break;
            case CURRENT_USER_START_MENU :
                tp = LocationType.CURRENT_USER_START_MENU;
                break;
            case ALL_USERS_DESKTOP :
                tp = LocationType.ALL_USERS_DESKTOP;
                break;
            case ALL_USERS_START_MENU :
                tp = LocationType.ALL_USERS_START_MENU;
                break;
        }
        return tp;
    }
    
    @Deprecated
    public static File getShortcutLocation(org.netbeans.installer.utils.helper.Shortcut shortcut, ShortcutLocationType locationType) throws NativeException {
        return getNativeUtils().getShortcutLocation((FileShortcut)shortcut, toLocationType(locationType));
    }
    
    @Deprecated
    public static File createShortcut(org.netbeans.installer.utils.helper.Shortcut shortcut, ShortcutLocationType locationType) throws NativeException {
        return getNativeUtils().createShortcut((FileShortcut)shortcut, toLocationType(locationType));
    }
    
    @Deprecated
    public static void removeShortcut(org.netbeans.installer.utils.helper.Shortcut shortcut, ShortcutLocationType locationType, boolean deleteEmptyParents) throws NativeException {
        getNativeUtils().removeShortcut((FileShortcut)shortcut, toLocationType(locationType), deleteEmptyParents);
    }
    
    public static File getShortcutLocation(Shortcut shortcut, LocationType locationType) throws NativeException {
        return getNativeUtils().getShortcutLocation(shortcut, locationType);
    }
    
    /**
     * Create shortcut at the specified location that is set using <code>locationType</code>.
     * <br>For the current moment the following logic is implemented:
     * <ul>
     * <li> For Windows FileShortcut is created as an <i>.lnk</i> file.<br>
     *      InternetShortcut is created as a standard <i>.url</i> file.<br></li>
     * <li> For Linux/Solaris FileShortcut is created as a <i>.desktop</i> entry with
     *      type <b>Application</b> if the target is normal file.<br>
     *      If the file is actually a directory then a symlink is created <br>
     *      InternetShortcut is created as a <i>.desktop</i> entry with type
     *      <b>Link</b>.</li>
     * <li> For MacOS FileShortcut on desktop is created as a symlink
     *      (with, possibly, moving up-parents to the first .app).<br>
     *      InternetShortcut on desktop is created as a standard <i>.url</i> file.<br>
     *      "Start Menu" file shortcuts for MacOS are created at Dock.<br>
     *      InternetShortcut creation in Dock actually does nothing since it
     *      seems that there is no way add an internet shortcut ot the Dock
     *      at all.</li>
     * </ul>
     *
     *
     */
    public static File createShortcut(
            final Shortcut shortcut,
            final LocationType locationType) throws NativeException {
        return getNativeUtils().createShortcut(shortcut, locationType);
    }
    
    public static void removeShortcut(Shortcut shortcut, LocationType locationType, boolean deleteEmptyParents) throws NativeException {
        getNativeUtils().removeShortcut(shortcut, locationType, deleteEmptyParents);
    }
    
    public static FilesList addComponentToSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException {
        return getNativeUtils().addComponentToSystemInstallManager(descriptor);
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
    
    public static void deleteFilesOnExit() {
        getNativeUtils().deleteFilesOnExit();
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
    
    public static void setPermissions(final File file, final int mode, final int change) throws IOException {
        getNativeUtils().setPermissions(file, mode, change);
    }
    
    public static int getPermissions(final File file) throws IOException {
        return getNativeUtils().getPermissions(file);
    }
    
    public static Launcher createLauncher(LauncherProperties props, Progress progress) throws IOException {
        return createLauncher(props, getCurrentPlatform(), progress);
    }
    
    public static Launcher createLauncher(LauncherProperties props, Platform platform, Progress progress) throws IOException {
        Progress prg = (progress == null) ? new Progress() : progress;
        LogManager.log("Create native launcher for " + platform.toString());
        Launcher launcher  =null;
        try {
            LogManager.indent();
            launcher = LauncherFactory.newLauncher(props, platform);
            long start = System.currentTimeMillis();
            launcher.initialize();
            launcher.create(progress);
            long seconds = System.currentTimeMillis() - start ;
            LogManager.unindent();
            LogManager.log("[launcher] Time : " + (seconds/1000) + "."+ (seconds%1000)+ " seconds");
        } catch (IOException e) {
            LogManager.unindent();
            LogManager.log("[launcher] Build failed with the following exception :");
            LogManager.log(e);
            throw e;
        }
        return launcher;
    }
    
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }  catch (InterruptedException e) {
            ErrorManager.notify(ErrorLevel.DEBUG,
                    "Interrupted while sleeping", e);
        }
    }
    
    public static void setDefaultEnvironment() {
        environment = new ProcessBuilder().environment();
    }
    
    public static Map<String, String> getEnvironment() {
        return environment;
    }
    
    public static Platform getCurrentPlatform() {
        if (currentPlatform == null) {
            boolean is64bit = System.getProperty("os.arch").equals("amd64") ||
                    System.getProperty("os.arch").equals("sparcv9");
            
            if (System.getProperty("os.name").contains("Windows")) {
                currentPlatform =
                        is64bit ? Platform.WINDOWS_X64 : Platform.WINDOWS_X86;
            }
            if (System.getProperty("os.name").contains("Linux")) {
                currentPlatform =
                        is64bit ? Platform.LINUX_X64 : Platform.LINUX_X86;
            }
            if (System.getProperty("os.name").contains("Mac OS X") &&
                    System.getProperty("os.arch").contains("ppc")) {
                currentPlatform = Platform.MACOSX_PPC;
            }
            if (System.getProperty("os.name").contains("Mac OS X") &&
                    System.getProperty("os.arch").contains("i386")) {
                currentPlatform = Platform.MACOSX_X86;
            }
            if (System.getProperty("os.name").contains("SunOS")) {
                if(System.getProperty("os.arch").contains("sparc")) {
                    currentPlatform = Platform.SOLARIS_SPARC;
                } else {
                    currentPlatform = Platform.SOLARIS_X86;
                }
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
    
    public static List<File> getFileSystemRoots() throws IOException {
        return getNativeUtils().getFileSystemRoots();
    }
    
    public static boolean isJarSignatureVeryfied(
            final File file,
            final String description) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        if (caStore == null) {
            caStore = KeyStore.getInstance(KeyStore.getDefaultType());
            caStore.load(new FileInputStream(new File(System.getProperty(
                    "java.home") + "/lib/security/cacerts")), null);
            
            permanentTrustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
            permanentTrustedStore.load(null, null);
            
            sessionTrustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
            sessionTrustedStore.load(null, null);
            
            deniedStore = KeyStore.getInstance(KeyStore.getDefaultType());
            deniedStore.load(null, null);
        }
        
        final JarFile jar = new JarFile(file);
        try {
            // first we should fetch all certificates that are present in the jar
            // file skipping duplicates
            Certificate[] certificates = null;
            CodeSigner[] codeSigners = null;
            for (JarEntry entry: Collections.list(jar.entries())) {
                readFully(jar.getInputStream(entry));
                
                certificates = entry.getCertificates();
                codeSigners = entry.getCodeSigners();
                
                if (certificates != null) {
                    break;
                }
            }
            
            // if there are no certificates -- we should pop up the dialog warning
            // that the jar is not signed and ask the user whether he wants to
            // accept this
            if (certificates == null) {
                // todo
            }
            
            // check the permanent and session trusted stores
            int chainStart = 0;
            int chainEnd = 0;
            int	chainNum = 0;
            
            // iterate over the certificate chains that are present in the
            // certificate arrays
            while (chainEnd < certificates.length) {
                // determine the start and end of the current certificates chain
                int i = chainStart;
                while (i < certificates.length - 1) {
                    final boolean isIssuer = isIssuerOf(
                            (X509Certificate) certificates[i],
                            (X509Certificate) certificates[i+1]);
                    
                    if ((certificates[i] instanceof X509Certificate)
                    && (certificates[i+1] instanceof X509Certificate)
                    && isIssuer) {
                        i++;
                    } else {
                        break;
                    }
                }
                chainEnd = i + 1;
                
                // if the denied certificates store contains the
                if (containsCertificate(deniedStore, certificates[chainStart])) {
                    return false;
                } else if (containsCertificate(permanentTrustedStore, certificates[chainStart]) ||
                        containsCertificate(sessionTrustedStore, certificates[chainStart])) {
                    return true;
                }
                
                chainStart = chainEnd;
                chainNum++;
            }
            
            // If we get here, no cert in chain has been stored in Session or Permanent store.
            // If they are not in Deny store either, we have to pop up security dialog box
            // for each signer's certificate one by one.
            boolean rootCANotValid = false;
            boolean timeNotValid = false;
            
            chainStart = 0;
            chainEnd = 0;
            chainNum = 0;
            while (chainEnd < certificates.length) {
                int i = chainStart;
                
                for (i = chainStart; i < certificates.length; i++) {
                    X509Certificate currentCert = null;
                    X509Certificate issuerCert = null;
                    
                    if (certificates[i] instanceof X509Certificate)
                        currentCert = (X509Certificate) certificates[i];
                    
                    if ((i < certificates.length - 1) &&
                            (certificates[i + 1] instanceof X509Certificate)) {
                        issuerCert = (X509Certificate) certificates[i+1];
                    } else {
                        issuerCert = currentCert;
                    }
                    
                    // check if the certificate is valid and has not expired
                    try {
                        currentCert.checkValidity();
                    } catch (CertificateExpiredException e1) {
                        timeNotValid = true;
                    } catch (CertificateNotYetValidException e2) {
                        timeNotValid = true;
                    }
                    
                    if (isIssuerOf(currentCert, issuerCert)) {
                        // check the current certificate's signature -- verify that
                        // this issuer did indeed sign the certificate.
                        try {
                            currentCert.verify(issuerCert.getPublicKey());
                        } catch (GeneralSecurityException e) {
                            return false;
                        }
                    } else {
                        break;
                    }
                }
                chainEnd = (i < certificates.length) ? (i + 1) : i;
                
                // we need to verify if the certificate chain is signed by a CA
                rootCANotValid = !verifyCertificate(caStore, certificates[chainEnd-1]);
                
                Date timestamp = null;
                if (codeSigners[chainNum].getTimestamp() != null) {
                    timestamp = codeSigners[chainNum].getTimestamp().getTimestamp();
                }
                
                CertificateAcceptanceStatus status = UiUtils.showCertificateAcceptanceDialog(
                        certificates,
                        chainStart,
                        chainEnd,
                        rootCANotValid,
                        timeNotValid,
                        timestamp,
                        description);
                
                
                // If user Grant permission, just pass all security checks.
                // If user Deny first signer, pop up security box for second signer certs
                if (status == CertificateAcceptanceStatus.ACCEPT_PERMANENTLY) {
                    addCertificate(permanentTrustedStore, certificates[chainStart]);
                    return true;
                } else if (status == CertificateAcceptanceStatus.ACCEPT_FOR_THIS_SESSION) {
                    addCertificate(sessionTrustedStore, certificates[chainStart]);
                    return true;
                } else {
                    addCertificate(deniedStore, certificates[chainStart]);
                }
                
                chainStart = chainEnd;
                chainNum++;
            }
            
            return false;
        } finally {
            jar.close();
        }
    }
    
    private static void readFully(
            final InputStream stream) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        while(stream.read(buffer) != -1) {
            ; // do this!
        }
    }
    
    private static boolean isIssuerOf(
            final X509Certificate certificate1,
            final X509Certificate certificate2) {
        return certificate1.getIssuerDN().equals(certificate2.getSubjectDN());
    }
    
    private static boolean containsCertificate(
            final KeyStore store,
            final Certificate certificate) throws KeyStoreException {
        return store.getCertificateAlias(certificate) != null;
    }
    
    private static void addCertificate(
            final KeyStore store,
            final Certificate certificate) throws KeyStoreException {
        if (store.getCertificateAlias(certificate) == null) {
            store.setCertificateEntry(
                    "alias" + new Random().nextLong(),
                    certificate);
        }
    }
    
    private static boolean verifyCertificate(
            final KeyStore store,
            final Certificate certificate) throws KeyStoreException {
        for (String alias: Collections.list(store.aliases())) {
            try {
                certificate.verify(store.getCertificate(alias).getPublicKey());
                return true;
            } catch (GeneralSecurityException e) {
                // we must ignore this exception as it is VERY expected -- will
                // happen N-1 times at least
            }
        }
        
        return false;
    }
    
    // platforms probes /////////////////////////////////////////////////////////////
    public static boolean isWindows() {
        return getCurrentPlatform().isCompatibleWith(Platform.WINDOWS);
    }
    
    public static boolean isMacOS() {
        return getCurrentPlatform().isCompatibleWith(Platform.MACOSX);
    }
    
    public static boolean isLinux() {
        return getCurrentPlatform().isCompatibleWith(Platform.LINUX);
    }
    
    public static boolean isSolaris() {
        return getCurrentPlatform().isCompatibleWith(Platform.SOLARIS);
    }
    
    // miscellanea //////////////////////////////////////////////////////////////////
    public static boolean intersects(
            final List<? extends Object> list1,
            final List<? extends Object> list2) {
        for (int i = 0; i < list1.size(); i++) {
            for (int j = 0; j < list2.size(); j++) {
                if (list1.get(i).equals(list2.get(j))) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public static <T> List<T> intersect(
            final List<? extends T> list1,
            final List<? extends T> list2) {
        final List<T> intersection = new LinkedList<T>();
        
        for (T item: list1) {
            if (list2.contains(item)) {
                intersection.add(item);
            }
        }
        
        return intersection;
    }
    
    public static <T> List<T> substract(
            final List<? extends T> list1,
            final List<? extends T> list2) {
        final List<T> result = new LinkedList<T>();
        
        for (T item1: list1) {
            boolean found = false;
            
            for (T item2: list2) {
                if (item1.equals(item2)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                result.add(item1);
            }
        }
        
        return result;
    }
    
    // native accessor //////////////////////////////////////////////////////////////
    public static synchronized NativeUtils getNativeUtils() {
        if (nativeUtils == null) {
            nativeUtils = NativeUtils.getInstance();
        }
        return nativeUtils;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final long MAX_EXECUTION_TIME = 600000;
    
    public static final int BUFFER_SIZE = 4096;
    
    public static final int DELAY = 50;
}
