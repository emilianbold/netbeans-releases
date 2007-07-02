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

package org.netbeans.installer.utils.helper;

import java.util.List;
import static org.netbeans.installer.utils.helper.PlatformConstants.*;

public enum Platform {
    /////////////////////////////////////////////////////////////////////////////////
    // Values
    GENERIC(null, null, null, null, "Generic"),
    
    WINDOWS(OS_FAMILY_WINDOWS, null, null, null, "Windows"),
    WINDOWS_X86(OS_FAMILY_WINDOWS, HARDWARE_X86, null, null, "Windows X86"),
    WINDOWS_X64(OS_FAMILY_WINDOWS, HARDWARE_X64, null, null, "Windows X64"),
    
    LINUX(OS_FAMILY_LINUX, null, null, null, "Linux"),
    LINUX_X86(OS_FAMILY_LINUX, HARDWARE_X86, null, null, "Linux X86"),
    LINUX_X64(OS_FAMILY_LINUX, HARDWARE_X64, null, null, "Linux X64"),
    
    SOLARIS(OS_FAMILY_SOLARIS, null, null, null, "Solaris"),
    SOLARIS_X86(OS_FAMILY_SOLARIS, HARDWARE_X86, null, null, "Solaris X86"),
    SOLARIS_SPARC(OS_FAMILY_SOLARIS, HARDWARE_SPARC, null, null, "Solaris SPARC"),
    
    MACOSX(OS_FAMILY_MACOSX, null, null, null, "MacOS X"),
    MACOSX_X86(OS_FAMILY_MACOSX, HARDWARE_X86, null, null, "MacOS X Intel"),
    MACOSX_X64(OS_FAMILY_MACOSX, HARDWARE_X64, null, null, "MacOS X Intel X64"),
    MACOSX_PPC(OS_FAMILY_MACOSX, HARDWARE_PPC, null, null, "MacOS X PowerPC"),
    ;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private String osFamily;
    private String hardwareArch;
    private String osVersion;
    private String osFlavor;
    
    private String codeName;
    private String displayName;
    
    private Platform(
            final String osFamily,
            final String hardwareArch,
            final String osVersion,
            final String osFlavor,
            final String displayName) {
        this.osFamily = osFamily;
        this.hardwareArch = hardwareArch;
        this.osVersion = osVersion;
        this.osFlavor = osFlavor;
        
        if (osFamily != null) {
            this.codeName = osFamily;
            
            if (hardwareArch != null) {
                this.codeName += SEPARATOR + hardwareArch;
                
                if (osVersion != null) {
                    this.codeName += SEPARATOR + osVersion;
                    
                    if (osFlavor != null) {
                        this.codeName += SEPARATOR + osFlavor;
                    }
                }
            }
        } else {
            this.codeName = "generic"; 
        }
        
        this.displayName = displayName;
    }
    
    public String getOsFamily() {
        return osFamily;
    }
    
    public String getHardwareArch() {
        return hardwareArch;
    }
    
    public String getOsVersion() {
        return osVersion;
    }
    
    public String getOsFlavor() {
        return osFlavor;
    }
    
    public String getCodeName() {
        return codeName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isCompatibleWith(final Platform platform) {
        if (!platform.osFamily.equals(osFamily)) {
            return false;
        }
        
        if ((platform.hardwareArch != null) &&
                !platform.hardwareArch.equals(hardwareArch)) {
            return false;
        }
        
        if ((platform.osVersion != null) &&
                !platform.osVersion.equals(osVersion)) {
            return false;
        }
        
        if ((platform.osFlavor != null) &&
                !platform.osFlavor.equals(osFlavor)) {
            return false;
        }
        
        return true;
    }
    
    public boolean isCompatibleWith(final List<Platform> platforms) {
        for (Platform candidate: platforms) {
            if (isCompatibleWith(candidate)) {
                return true;
            }
        }
        
        return false;
    }
    
    
    @Override
    public String toString() {
        return codeName;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String SEPARATOR = 
            "-";
}

