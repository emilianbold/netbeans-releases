/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 

package org.netbeans.modules.j2me.cdc.platform.sun;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.netbeans.modules.j2me.cdc.platform.CDCDevice;
import org.netbeans.modules.j2me.cdc.platform.CDCDevice.CDCProfile;
import org.netbeans.modules.j2me.cdc.platform.CDCDevice.Screen;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformUtil;
import org.netbeans.modules.j2me.cdc.platform.system.ExternalProcessing;
import org.openide.filesystems.FileObject;
import org.openide.util.io.ReaderInputStream;

/**
 * Detector for the java ME SDK 3.0
 * 
 * @author Ondrej Nekola
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector.class, position=1100)
public final class JmeSdkCdcDetector extends CDCPlatformDetector {

    private static String PLATFORM_TYPE = "cdc-hi"; //NOI18N

    public String getPlatformName() {
        return "CDC Platform"; //NOI18N
    }

    public String getPlatformType() {
        return PLATFORM_TYPE;
    }

    public boolean accept(final FileObject dir) {
        FileObject tool1 = CDCPlatformUtil.findTool("bin", "emulator.exe", Collections.singleton(dir));            //NOI18N
        FileObject tool1a = CDCPlatformUtil.findTool("bin", "emulator", Collections.singleton(dir));                //NOI18N
        FileObject tool2 = CDCPlatformUtil.findTool("runtimes/cdc-hi/bin", "cvm.exe", Collections.singleton(dir)); //NOI18N
        FileObject tool2a = CDCPlatformUtil.findTool("runtimes/cdc-hi/bin", "cvm", Collections.singleton(dir));     //NOI18N
        final boolean result = ((null != tool1 && null != tool2) || (null != tool1a && null != tool2a));
        return result;
    }

    public CDCPlatform detectPlatform(final FileObject dir) throws IOException {
        assert dir != null;

        final String versionOutput = getEmulatorResult(dir, "-version");
        final String[] versionLines = versionOutput.split("\n");
        final String platformName = "CDC " + versionLines[0]; //without "CDC it colides with the CLDC platform
        final List<FileObject> jdocsAsFO = new ArrayList<FileObject>();
        FileObject javadocBase = dir.getFileObject("docs/api"); //NOI18N
        if (javadocBase != null) {
            findJavaDoc(javadocBase, jdocsAsFO);
        }

        final List<URL> jdocsAsURL = new ArrayList<URL>(jdocsAsFO.size());
        for (final FileObject fileObject : jdocsAsFO) {
            jdocsAsURL.add(fileObject.toURL());
        }
        
        Comparator<URL> urlComparator = new Comparator<URL>() {
            @Override
            public int compare(URL o1, URL o2) {
                return o1.getPath().compareTo(o2.getPath());
            };
        };
        //Order javadoc folders by their names 
        Collections.sort(jdocsAsURL, urlComparator);   

        final String ueiResultS = getEmulatorResult(dir, "-Xquery");
        final Properties ueiResult = new Properties();
        final StringReader ueiStringReader = new StringReader(ueiResultS);
        final ReaderInputStream ueiInputStream = new ReaderInputStream(ueiStringReader);
        ueiResult.load(ueiInputStream);
        ueiInputStream.close();

        final String dispName = platformName;
        final String antName = "Java_ME_SDK_CDC";
        final String platformType = PLATFORM_TYPE;
        final String classVersion = "1.3"; //NOI18N 
        final List<URL> installFolders = Collections.singletonList(dir.getURL());
        final List<URL> sources = Collections.emptyList();
        final CDCDevice[] devices = getJavaMeSdkDevices(ueiResult);
        final boolean fatJar = true;

        final CDCPlatform result = new CDCPlatform(
                dispName,
                antName,
                platformType,
                classVersion,
                installFolders,
                sources,
                jdocsAsURL,
                devices,
                fatJar);

        return result;
    }

    private void addDevice(final Properties p, final String deviceName, final List<CDCDevice> devices) {
        final String configuration = p.getProperty(deviceName + ".version.configuration");
        if (!configuration.startsWith("CDC")) {
            return;
        }

        final String profilesS = p.getProperty(deviceName + ".version.profile");
        final String[] profileNames = profilesS.split(",");
        final CDCProfile[] profiles = new CDCProfile[profileNames.length];
        for (int i = 0; i < profileNames.length; i++) {
            String profileName = profileNames[i];
            profiles[i] = parseProfile(p, deviceName, profileName);
        }
        final String description = p.getProperty(deviceName + ".descriptor");

        final Screen[] screens = addScreens(p, deviceName);

        final CDCDevice device = new CDCDevice(deviceName, description, profiles, screens);
        devices.add(device);
    }

    private Screen[] addScreens(Properties p, String deviceName) {
        final Screen[] result = new Screen[1];

        String width = p.getProperty(deviceName + "screen.width");
        String height = p.getProperty(deviceName + "screen.height");
        String bitDepth = p.getProperty(deviceName + "screen.bitDepth");
        String color = p.getProperty(deviceName + "screen.isColor");
        String touch = p.getProperty(deviceName + "screen.isTouch");
        String main = p.getProperty(deviceName + "screen.isMain", "true");
        Screen screen = new Screen(width, height, bitDepth, color, touch, main);
        result[0] = screen;
        return result;
    }

    private String descriptionOfAProfile(final String profileName) {
        if (profileName.startsWith("PBP-")) {
            return profileName.replace("PBP-", "Personal basis profile ");
        }
        if (profileName.startsWith("PP-")) {
            return profileName.replace("PP-", "Personal profile ");            
        }
        return profileName;
    }
    

    private void findJavaDoc(final FileObject folder, final List<FileObject> folders) {
        if (folder == null) {
            return;
        }
        FileObject[] fileObjects = folder.getChildren();
        for (final FileObject fo : fileObjects) {
            if (fo.isData() && "index".equals(fo.getName())) { //NOI18N
                folders.add(fo.getParent());
            } else if (fo.isData() && fo.hasExt("zip") && fo.getParent().getName().equals("api")) {
                folders.add(fo);
            }
        }
        for (final FileObject fo : fileObjects) {
            if (fo.isFolder() && !folders.contains(fo.getParent())) {
                findJavaDoc(fo, folders);
            }
        }        
    }

    private CDCDevice[] getJavaMeSdkDevices(final Properties p) {
        final String deviceNamesAsString = p.getProperty("device.list");
        String[] deviceNames = deviceNamesAsString.split(",");

        final List<CDCDevice> devices = new ArrayList<CDCDevice>(deviceNames.length);
        for (String deviceName : deviceNames) {
            addDevice(p, deviceName, devices);
        }
        return devices.toArray(new CDCDevice[devices.size()]);
    }

    private String getEmulatorResult(final FileObject dir, final String parameter) throws IOException {
        final FileObject binDir = dir.getFileObject("bin");
        FileObject emulatorExe = binDir.getFileObject("emulator.exe");
        if (null == emulatorExe) {
            emulatorExe = binDir.getFileObject("emulator");
        }
        return ExternalProcessing.callExternal(emulatorExe, dir, parameter);

    }

    private CDCProfile parseProfile(final Properties p, final String deviceName, final String profileName) {
        final String version = profileName.split("-")[1];
        final Map<String, String> executionModes = new HashMap<String, String>();

        //FIXME add modes when supported
        executionModes.put(CDCPlatform.PROP_EXEC_MAIN, ""); //NOI18N

        final String bootClassPath = p.getProperty(deviceName + ".bootclasspath").replace(',', File.pathSeparatorChar); //NOI18N
        final String runClassPath = bootClassPath;
        boolean isDefault = true;
        final String profileDescription = descriptionOfAProfile(profileName);

        return new CDCDevice.CDCProfile(profileName, profileDescription, version, executionModes, bootClassPath, runClassPath, isDefault);
    }
} 
