/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.installer.utils.applications;

import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.PlatformConstants;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.system.WindowsNativeUtils;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.HKLM;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;

public class JavaFXUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static    

    public static String getJavaFXSDKInstallationPath (Platform platform) {
        return getFXRegistryValue(platform, FXSDK_KEY, FX_SDK_HOME_PATH);
    }

    public static String getJavaFXRuntimeInstallationPath (Platform platform) {
        return getFXRegistryMaxValue(platform, FXRUNTIME_INSTALLATION_KEY);
    }

    /////////////////////////////////////////////////////////////////////////////////

    private static String getFXRegistryValue (Platform platform, String registryKey, String registryItemKey) {
        String result = null;
        try {
            if(SystemUtils.isWindows()) {
                String arch = platform.getHardwareArch();
                WindowsRegistry winreg = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
                if(arch != null && winreg.isAlternativeModeSupported()) {
                    final int mode = arch.equals(PlatformConstants.HARDWARE_X86)? WindowsRegistry.MODE_32BIT:
                        WindowsRegistry.MODE_64BIT;
                    LogManager.log("... changing registry mode to: " + mode);
                    winreg.setMode(mode);
                }
                LogManager.log("... getting JavaFX " + registryKey + " value: " + registryItemKey);
                if (winreg.keyExists(HKLM, registryKey)) {
                    if (winreg.valueExists(HKLM, registryKey, registryItemKey)) {
                        result = winreg.getStringValue(HKLM, registryKey, registryItemKey);
                    } else {
                        LogManager.log("... cannot find " + registryItemKey + " value for this product");
                    }                    
                } else {
                    LogManager.log("... cannot find " + registryKey + " for this product");
                }
            }
        } catch (NativeException e) {
            LogManager.log(e);
        }
        return result;
    }

    private static String getFXRegistryMaxValue (Platform platform, String registryKey) {
        String result = null;
        try {
            if(SystemUtils.isWindows()) {
                String arch = platform.getHardwareArch();
                WindowsRegistry winreg = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
                if(arch != null && winreg.isAlternativeModeSupported()) {
                    final int mode = arch.equals(PlatformConstants.HARDWARE_X86)? WindowsRegistry.MODE_32BIT:
                        WindowsRegistry.MODE_64BIT;
                    LogManager.log("... changing registry mode to: " + mode);
                    winreg.setMode(mode);
                }
                
                if (winreg.keyExists(HKLM, registryKey)) {
                    String[] javaFXSubKeys = winreg.getSubKeyNames(HKLM, registryKey);

                    Version prevVersion = null;
                    for (String singleKey : javaFXSubKeys) {
                        LogManager.log("... getting JavaFX " + registryKey + " value: " + singleKey);
                        if (winreg.valueExists(HKLM, registryKey + singleKey, PATH)) {
                            Version actualVersion = Version.getVersion(singleKey);
                            if (actualVersion == null || prevVersion == null || actualVersion.newerThan(prevVersion)) {
                                result = winreg.getStringValue(HKLM, registryKey + singleKey, PATH);
                            }
                            prevVersion = actualVersion;                
                        } else {
                            LogManager.log("... cannot find " + singleKey + " value for this product");
                        }                        
                    }                 
                } else {
                    LogManager.log("... cannot find " + registryKey + " for this product");
                }
            }
        } catch (NativeException e) {
            LogManager.log(e);
        }
        return result;
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private JavaFXUtils() {
        // does nothing
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Constants

    public static final String FXSDK_KEY =
            "SOFTWARE\\JavaSoft\\JavaFX SDK"; // NOI18N
    public static final String FXRUNTIME_KEY =
            "SOFTWARE\\JavaSoft\\JavaFX"; // NOI18N
    public static final String FXRUNTIME_INSTALLATION_KEY =
            "SOFTWARE\\Oracle\\JavaFX\\"; //NOI18N

    public static final String VERSION
            = "Version"; // NOI18N
    public static final String FX_VERSION
            = "FXVersion"; // NOI18N
    public static final String FX_SDK_HOME_PATH
            = "JFXSDKHome"; //NOI18N
    public static final String PATH
            = "Path"; //NOI18N
}
