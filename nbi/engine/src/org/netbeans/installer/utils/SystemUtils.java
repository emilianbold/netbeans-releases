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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.installer.utils.error.ErrorManager;
import org.netbeans.installer.utils.exceptions.UnrecognizedObjectException;
import org.netbeans.installer.utils.log.LogManager;

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
    
    public abstract String getDefaultApplicationsLocation();
    
    public abstract Platform getCurrentPlatform();
    
    public abstract boolean isMacOS();
    
    public abstract void sleep(long millis);
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private static class GenericSystemUtils extends SystemUtils {
        public String parseString(String string) {
            String parsedString = string;
            
            parsedString = parsedString.replace("${install}", getDefaultApplicationsLocation());
            
            return parsedString;
        }
        
        public String parsePath(String path) {
            String parsedPath = path;
            
            parsedPath = parseString(parsedPath);
            
            parsedPath = parsedPath.replace('\\', File.separatorChar);
            parsedPath = parsedPath.replace('/', File.separatorChar);
            
            return parsedPath;
        }
        
        public String getDefaultApplicationsLocation() {
            switch (getCurrentPlatform()) {
                case WINDOWS:
                    return System.getenv("ProgramFiles");
                default:
                    return System.getProperty("user.home");
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
}
