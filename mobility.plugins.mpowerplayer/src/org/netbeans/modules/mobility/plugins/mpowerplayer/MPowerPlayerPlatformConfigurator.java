/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.mobility.plugins.mpowerplayer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import java.util.Properties;
import java.util.StringTokenizer;
import org.netbeans.spi.mobility.cldcplatform.CLDCPlatformDescriptor;
import org.netbeans.spi.mobility.cldcplatform.CustomCLDCPlatformConfigurator;
import org.openide.util.NbBundle;

public final class MPowerPlayerPlatformConfigurator implements CustomCLDCPlatformConfigurator {

    private static final String DISPLY_NAME = "Emulator_displayname"; //NOI18N
    private static final String DESCRIPTION = "Emulator_description"; //NOI18N
    private static final String CONFIGURATIONS = "configurations"; //NOI18N
    private static final String OPTIONAL = "optional"; //NOI18N 
    private static final String PROFILES = "profiles"; //NOI18N
    private static final String PREVERIFY_COMMAND = "preverify.command"; //NOI18N
    private static final String RUN_COMMAND = "run.command"; //NOI18N
    private static final String DEBUG_COMMAND = "debug.command"; //NOI18N
    private static final String TYPE = "CUSTOM"; //NOI18N
    private static final String HEIGHT_SCREEN = "height.screen"; //NOI18N
    private static final String WIDTH_SCREEN = "width.screen"; //NOI18N
    private static final String BIT_DEPTH_SCREEN = "bitDepth.screen"; //NOI18N
    private static final String COLOR_SCREEN = "color.screen"; //NOI18N
    private static final String TOUCH_SCREEN = "touch.screen";//NOI18N
    private static final String CHECK_FOLDER = "check.folder"; //NOI18N
    private static final String JAVADOCS_FOLDER = "javadoc.folder"; //NOI18N
    private static final String MAJOR_CHECK_FOLDER = "majorCheck.folder"; //NOI18N
    private static final String OS_SUPPORTED = "os.supported"; //NOI18N
    
    private final String srcPath = ""; //not available for MPowerPlayer
    private final String displayName;
    private String docPath;
    private String preverifyCommand;
    private String runCommand;
    private String debugCommand;
    private int heightScreen;
    private int widthScreen;
    private int bitDepthScreen;
    private boolean colorScreen;
    private boolean touchScreen;
    private Properties apiSettings;
    private List<CLDCPlatformDescriptor.Device> devices;

    public MPowerPlayerPlatformConfigurator() {
        this.displayName = NbBundle.getMessage(MPowerPlayerPlatformConfigurator.class, DISPLY_NAME);
    }

    private List<CLDCPlatformDescriptor.Profile> createAPISettings() {

        runCommand = apiSettings.getProperty(RUN_COMMAND);
        debugCommand = apiSettings.getProperty(DEBUG_COMMAND);
        preverifyCommand = apiSettings.getProperty(PREVERIFY_COMMAND);
        heightScreen = Integer.parseInt(apiSettings.getProperty(HEIGHT_SCREEN));
        widthScreen = Integer.parseInt(apiSettings.getProperty(WIDTH_SCREEN));
        bitDepthScreen = Integer.parseInt(apiSettings.getProperty(BIT_DEPTH_SCREEN));
        colorScreen = Boolean.getBoolean(apiSettings.getProperty(COLOR_SCREEN));
        touchScreen = Boolean.getBoolean(apiSettings.getProperty(TOUCH_SCREEN));

        String allProperties = apiSettings.getProperty(CONFIGURATIONS) + "," + apiSettings.getProperty(PROFILES) + "," + apiSettings.getProperty(OPTIONAL); //NOI18N
        StringTokenizer propertiesTokenizer = new StringTokenizer(allProperties, ","); //NOI18N 

        List<CLDCPlatformDescriptor.Profile> descriptors = new ArrayList<CLDCPlatformDescriptor.Profile>(propertiesTokenizer.countTokens());

        while (propertiesTokenizer.hasMoreTokens()) {
            String token = propertiesTokenizer.nextToken().trim();
            CLDCPlatformDescriptor.ProfileType profileType;
            if (apiSettings.getProperty(CONFIGURATIONS).contains(token)) {
                profileType = CLDCPlatformDescriptor.ProfileType.Configuration;
            } else if (apiSettings.getProperty(PROFILES).contains(token)) {
                profileType = CLDCPlatformDescriptor.ProfileType.Profile;
            } else if (apiSettings.getProperty(OPTIONAL).contains(token)) {
                profileType = CLDCPlatformDescriptor.ProfileType.Optional;
            } else {
                throw new IllegalStateException("Type of profile is necessary to create Platform Descriptor"); //NOI18N
            }

            descriptors.add(new CLDCPlatformDescriptor.Profile(
                    apiSettings.getProperty(token + ".displayname"), //NOI18N
                    apiSettings.getProperty(token + ".version"), //NOI18N
                    apiSettings.getProperty(token + ".description"), //NOI18N
                    profileType,
                    apiSettings.getProperty(token + ".dependencies"), //NOI18N
                    apiSettings.getProperty(token + ".bcp"), //NOI18N
                    apiSettings.getProperty("default").contains(token))); //NOI18N
        }
        return Collections.unmodifiableList(descriptors);
    }

    public static void main(final String args[]) {
        MPowerPlayerPlatformConfigurator mppc = new MPowerPlayerPlatformConfigurator();
        if (args.length == 1) {
            if (mppc.isPossiblePlatform(new File(args[0]))) {
                System.out.println("Found valid MPowerPlayer SDK at " + args[0]); //NOI18N
            } else {
                System.out.println("Did not find valid MPowerPlayer SDK at " + args[0]); //NOI18N
            }
        } else {
            System.out.println("Usage: MPowerPlayerConfigurator <mpp-sdk root>"); //NOI18N
        }
    }

    public boolean isPossiblePlatform(File file) {
        if (apiSettings == null) {
            apiSettings = new Properties();
            try {
                apiSettings.load(MPowerPlayerPlatformConfigurator.class.getResourceAsStream("Configuration.properties"));//NOI18N
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        String folderCheck = apiSettings.getProperty(CHECK_FOLDER);
        String osSupported = apiSettings.getProperty(OS_SUPPORTED);
        String osName = (String) System.getProperties().get("os.name"); //NOI18N
        if (!osSupported.equalsIgnoreCase(osName))
            return false;
        if (folderCheck == null)
            throw new IllegalArgumentException("Null folder platform check"); //NOI18N
        if (file.isDirectory()) {
            if (file.listFiles(new JarFilenameFilter()).length == 4 && file.listFiles(new PlatformFileFilter()).length == 1) {
                File preverifier = new File(file.getPath() + folderCheck); 
                if (preverifier.exists()) {
                    return true;
                }
            }
        }
        return false;
    }

    public CLDCPlatformDescriptor getPlatform(File file) {
        String home = file.getAbsolutePath();
        String javadocFolder = apiSettings.getProperty(JAVADOCS_FOLDER);
        if (javadocFolder == null)
            throw new IllegalArgumentException("null javadocs folder"); //NOI18N
        this.docPath = home + javadocFolder; 

        if (devices == null) {
            devices = Collections.singletonList(
                    new CLDCPlatformDescriptor.Device(
                    this.displayName,
                    NbBundle.getMessage(MPowerPlayerPlatformConfigurator.class, DESCRIPTION),
                    Collections.EMPTY_LIST,
                    createAPISettings(),
                    new CLDCPlatformDescriptor.Screen(widthScreen, heightScreen, bitDepthScreen, colorScreen, touchScreen)));
        }

        return new CLDCPlatformDescriptor(this.displayName, home, TYPE,
                this.srcPath, this.docPath, this.preverifyCommand, this.runCommand, this.debugCommand, this.devices);
    }

    public String getRegistryProviderName() {
        return null;
    }

    private class JarFilenameFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".jar"); //NOI18N
        }
    }

    private class PlatformFileFilter implements FileFilter {

        public boolean accept(File file) {
            String majorCheckFolder = apiSettings.getProperty(MAJOR_CHECK_FOLDER);
            return (file.getName().equalsIgnoreCase(majorCheckFolder) && file.isDirectory()); 
        }
    }
}
