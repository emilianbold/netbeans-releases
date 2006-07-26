/*
 * SystemUtils.java
 *
 * $Id$
 */
package org.netbeans.installer.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.installer.utils.exceptions.UnrecognizedPlatformException;

/**
 *
 * @author Kirill Sorokin
 */
public class SystemUtils {
    
    public static String parseString(String string) {
        String parsedString = string;
        
        parsedString = parsedString.replace("${install}", getDefaultApplicationsLocation());
        
        return parsedString;
    }
    
    public static String parsePath(String path) {
        String parsedPath = path;
        
        parsedPath = parseString(parsedPath);
        
        parsedPath = parsedPath.replace('\\', File.separatorChar);
        parsedPath = parsedPath.replace('/', File.separatorChar);
        
        return parsedPath;
    }
    
    public static String getDefaultApplicationsLocation() {
        switch (getCurrentPlatform()) {
            case WINDOWS:
                return System.getenv("ProgramFiles");
            default:
                return System.getProperty("user.home");
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
    
    public static boolean isMacOS() {
        return (getCurrentPlatform() == Platform.MACOS_X_X86) || 
                (getCurrentPlatform() == Platform.MACOS_X_PPC);
    }
    
    public static enum Platform {
        WINDOWS("windows", "Windows"),
        LINUX("linux", "Linux"),
        SOLARIS_X86("solaris-x86", "Solaris X86"),
        SOLARIS_SPARC("solaris-sparc", "Solaris Sparc"),
        MACOS_X_PPC("macos-x-ppc", "MacOS X (PPC)"),
        MACOS_X_X86("macos-x-x86", "MacOS X (Intel)");
        
        public static Platform parsePlatform(String name) throws UnrecognizedPlatformException {
            for (Platform platform: Platform.values()) {
                if (platform.name.equals(name)) {
                    return platform;
                }
            }
            
            throw new UnrecognizedPlatformException("Platform " + name + " is not recognized.");
        }
        
        public static List<Platform> parsePlatforms(String platformsString) throws UnrecognizedPlatformException {
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
