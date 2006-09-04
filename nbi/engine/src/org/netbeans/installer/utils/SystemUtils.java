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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
            instance = new GenericSystemUtils();
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
    
    public abstract boolean isWindows();
    
    public abstract boolean isMacOS();
    
    public abstract void sleep(long millis);
    
    public abstract String getLineSeparator();
    
    public abstract File getUserHomeDirectory();
    
    public abstract File getCurrentDirectory();
    
    public abstract File getTempDirectory();
    
    public abstract File getSystemDrive();
    
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
            switch (getCurrentPlatform()) {
                case WINDOWS:
                    return new File(System.getenv("ProgramFiles"));
                default:
                    return new File(System.getProperty("user.home"));
            }
        }
        
        public Platform getCurrentPlatform() {
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
        
        public boolean isWindows() {
            return getCurrentPlatform() == Platform.WINDOWS;
        }
        
        public boolean isMacOS() {
            return (getCurrentPlatform() == Platform.MACOS_X_X86) ||
                    (getCurrentPlatform() == Platform.MACOS_X_PPC);
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
        
        public File getSystemDrive() {
            switch (getCurrentPlatform()) {
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
            int           errorLevel    = Integer.MAX_VALUE;
            
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
            
            throw new UnrecognizedObjectException("Platform " + name + " is not recognized.");
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
        private int    errorCode = Integer.MAX_VALUE;
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
    
    public static final long MAX_EXECUTION_TIME = 30000; //30 seconds
    public static final int  BUFFER_SIZE        = 4096; // 4 kilobytes
    public static final int  DELAY              = 50; //50 milliseconds
}
