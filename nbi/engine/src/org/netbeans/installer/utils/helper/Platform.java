/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.helper;

import java.util.List;
import static org.netbeans.installer.utils.helper.PlatformConstants.*;

public enum Platform {
    /////////////////////////////////////////////////////////////////////////////////
    // Values
    GENERIC(null, null, null, null, "Generic"),
    UNIX(OS_FAMILY_UNIX, null, null, null, "Unix"),
    
    WINDOWS(OS_FAMILY_WINDOWS, null, null, null, "Windows"),
    WINDOWS_X86(OS_FAMILY_WINDOWS, HARDWARE_X86, null, null, "Windows X86"),
    WINDOWS_X64(OS_FAMILY_WINDOWS, HARDWARE_X64, null, null, "Windows X64"),
    
    LINUX(OS_FAMILY_LINUX, null, null, null, "Linux"),
    LINUX_X86(OS_FAMILY_LINUX, HARDWARE_X86, null, null, "Linux X86"),
    LINUX_X64(OS_FAMILY_LINUX, HARDWARE_X64, null, null, "Linux X64"),
    LINUX_PPC(OS_FAMILY_LINUX, HARDWARE_PPC, null, null, "Linux PowerPC"),
    LINUX_PPC64(OS_FAMILY_LINUX, HARDWARE_PPC64, null, null, "Linux PowerPC X64"),
    LINUX_SPARC(OS_FAMILY_LINUX, HARDWARE_SPARC, null, null, "Linux SPARC"),
    
    SOLARIS(OS_FAMILY_SOLARIS, null, null, null, "Solaris"),
    SOLARIS_X86(OS_FAMILY_SOLARIS, HARDWARE_X86, null, null, "Solaris X86"),
    SOLARIS_SPARC(OS_FAMILY_SOLARIS, HARDWARE_SPARC, null, null, "Solaris SPARC"),
    
    MACOSX(OS_FAMILY_MACOSX, null, null, null, "MacOS X"),
    MACOSX_X86(OS_FAMILY_MACOSX, HARDWARE_X86, null, null, "Mac OS X Intel"),
    MACOSX_X64(OS_FAMILY_MACOSX, HARDWARE_X64, null, null, "Mac OS X Intel X64"),
    MACOSX_PPC(OS_FAMILY_MACOSX, HARDWARE_PPC, null, null, "Mac OS X PowerPC"),
    MACOSX_PPC64(OS_FAMILY_MACOSX, HARDWARE_PPC64, null, null, "Mac OS X PowerPC X64"),
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
        if (platform.osFamily!=null &&
                !platform.osFamily.equals(osFamily)) {
            return (platform.osFamily.equals(OS_FAMILY_UNIX) && 
                    !OS_FAMILY_WINDOWS.equals(osFamily));
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

