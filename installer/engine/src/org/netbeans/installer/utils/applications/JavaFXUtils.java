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
  
    public static boolean isJavaFXSDKInstalled(Platform platform, Version version) {
        return isJavaFXInstalled(platform, version, FXSDK_KEY, VERSION);
    }
    public static boolean isJavaFXRuntimeInstalled(Platform platform, Version version) {
        return isJavaFXInstalled(platform, version, FXRUNTIME_KEY, FX_VERSION);
    }      

    /////////////////////////////////////////////////////////////////////////////////

    private static boolean isJavaFXInstalled(Platform platform, Version version, String productKey, String versionKey) {
        boolean result = false;
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
                LogManager.log("... checking if JavaFX " + version + " is already installed, key: " + productKey);
                if(winreg.keyExists(HKLM, productKey)) {
                    if(winreg.valueExists(HKLM, productKey, versionKey)) {
                        final String versionValue = winreg.getStringValue(HKLM, productKey, versionKey);
                        LogManager.log("... product with version " + versionValue + " is already installed");
                        if(version.toString().startsWith(versionValue)) {
                           result = true;
                        }                        
                    } else {
                        LogManager.log("... cannot find Version value for this product");
                    }
                } else {
                    LogManager.log("... cannot find key for this product");
                }
            }
        } catch (NativeException e) {
            LogManager.log(e);
        }
        return result;
    }

 /* public boolean isCompatibleJDKFoundForComponent(Product product) {
        for (WizardComponent c : product.getWizardComponents()) {
            if (c.getClass().getName().equals(SearchForJavaAction.class.getName())) {
                for (WizardComponent wc : product.getWizardComponents()) {
                    try {
                        Method m = wc.getClass().getMethod("getJdkLocationPanel");
                        Wizard wizard = new Wizard(null, product.getWizardComponents(), -1, product, product.getClassLoader());
                        wc.setWizard(wizard);
                        wc.getWizard().getContext().put(product);
                        wc.initialize();
                        JdkLocationPanel jdkLocationPanel = (JdkLocationPanel) m.invoke(wc);
                        if (jdkLocationPanel.getSelectedLocation().equals(new File(StringUtils.EMPTY_STRING))) {
                            final String jreAllowed = jdkLocationPanel.getProperty(
                                    JdkLocationPanel.JRE_ALLOWED_PROPERTY);
                            lastWarningMessage = StringUtils.format(
                                    getProperty("false".equals(jreAllowed) ?
                                        WARNING_NO_COMPATIBLE_JDK_FOUND :
                                        WARNING_NO_COMPATIBLE_JAVA_FOUND));

                            LogManager.log(lastWarningMessage);
                            return lastWarningMessage;
                        }
                    } catch (NoSuchMethodException e) {
                    } catch (IllegalAccessException e) {
                    } catch (IllegalArgumentException e) {
                    } catch (InvocationTargetException e) {
                    }
                }
            }
        }  */


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

    public static final String VERSION
            = "Version"; // NOI18N
    public static final String FX_VERSION
            = "FXVersion"; // NOI18N
}
