/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.junit.ide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Profile;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2me.project.api.J2MEProjectBuilder;
import org.netbeans.modules.j2me.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.cldcplatform.UEIEmulatorConfiguratorImpl;
import org.openide.ErrorManager;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Roman Svitanic
 */
public class MEProjectSupport {

    /**
     * Path to local ME SDK.
     */
    private static final String PLATFORM_HOME = "";
    /**
     * URL for J2ME Platform download. This will be used only in case that local
     * copy of ME SDK does not exists.
     */
    private static final String PLATFORM_URL = "";

    private MEProjectSupport() {
        throw new UnsupportedOperationException("It is just a helper class.");
    }

    /**
     * Creates an empty Java project in specified directory and opens it. Its
     * name is defined by name parameter.
     *
     * @param projectParentPath path to directory where to create name
     * subdirectory and new project structure in that subdirectory.
     * @param name name of the project
     * @return Project instance of created project
     */
    public static Object createProject(String projectParentPath, String name) {
        return createProject(new File(projectParentPath), name);
    }

    /**
     * Creates an empty Java project in specified directory and opens it. Its
     * name is defined by name parameter.
     *
     * @param projectParentDir directory where to create name subdirectory and
     * new project structure in that subdirectory.
     * @param name name of the project
     * @return Project instance of created project
     */
    public static Object createProject(File projectParentDir, String name) {
        try {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(null, new Specification(J2MEPlatform.SPECIFICATION_NAME, new SpecificationVersion("8.0"))); //NOI18N
            JavaPlatform platform = null;
            String platformHome = new File(PLATFORM_HOME).exists() ? PLATFORM_HOME : System.getProperty("java.io.tmpdir") + "\\J2MEPlatform";
            if (platforms == null || platforms.length == 0) {
                platform = getMockJ2MEPlatform(platformHome);
            } else {
                platform = platforms[0];
            }
            if (platform == null) {
                throw new RuntimeException("No JavaME  platform can be found.");
            }
            System.out.println("J2ME Platform: " + platform.getProperties().get(J2MEProjectProperties.PLATFORM_ANT_NAME));
            File projectDir = new File(projectParentDir, name);
            J2MEProjectBuilder.forDirectory(projectDir, name, platform).
                    addDefaultSourceRoots().
                    setSDKPlatform(JavaPlatformManager.getDefault().getDefaultPlatform()).
                    addCustomProjectProperties(addCustomMEProperties((J2MEPlatform) platform, platformHome)).
                    build();
            return org.netbeans.modules.project.ui.test.ProjectSupport.openProject(projectDir);
        } catch (RuntimeException | IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            return null;
        }
    }

    private static J2MEPlatform getMockJ2MEPlatform(String platformHomeDownload) throws MalformedURLException, IOException {
        if (new File(PLATFORM_HOME).exists()) {
            return new UEIEmulatorConfiguratorImpl(PLATFORM_HOME).getPlatform();
        } else {
            if (J2MEPlatformDownloader.downloadPlatform(new URL(PLATFORM_URL), new File(platformHomeDownload))) {
                return new UEIEmulatorConfiguratorImpl(platformHomeDownload).getPlatform();
            }
        }
        return null;
    }

    /**
     * Creates mock J2ME project properties.
     *
     * @param platform
     * @param platformHome
     * @return Customized J2ME project properties.
     */
    private static Map<String, String> addCustomMEProperties(J2MEPlatform platform, String platformHome) {
        J2MEPlatform.Device device = platform.getDevices()[0];
        String[] profiles = getDeviceProfiles(device);
        Map<String, String> props = new HashMap<>();
        props.put(J2MEProjectProperties.PROP_RUN_METHOD, "STANDARD"); //NOI18N
        props.put(J2MEProjectProperties.PROP_DEBUGGER_TIMEOUT, "30000"); //NOI18N
        props.put("platform.home", platformHome);
        props.put(J2MEProjectProperties.PROP_PLATFORM_CONFIGURATION, profiles[0]);
        props.put(J2MEProjectProperties.PROP_PLATFORM_PROFILE, profiles[1]);
        props.put(J2MEProjectProperties.PROP_PLATFORM_TYPE, ((J2MEPlatform) platform).getType());
        props.put(J2MEProjectProperties.PROP_PLATFORM_DEVICE, device.getName());
        props.put(J2MEProjectProperties.PROP_PLATFORM_APIS, profiles[2]);
        props.put(J2MEProjectProperties.PROP_PLATFORM_BOOTCLASSPATH, profiles[3]);
        return props;
    }

    /**
     * Fetches all available profiles for J2ME device.
     *
     * @param device
     * @return Array containing profiles divided into categories.
     */
    private static String[] getDeviceProfiles(J2MEPlatform.Device device) {
        String[] result = new String[4];
        StringBuilder sbOptional = new StringBuilder();
        StringBuilder sbBootCp = new StringBuilder();
        Profile profiles[] = device.getProfiles();
        for (Profile profile : profiles) {
            if (profile instanceof J2MEPlatform.J2MEProfile) {
                J2MEPlatform.J2MEProfile p = (J2MEPlatform.J2MEProfile) profile;
                switch (p.getType()) {
                    case J2MEPlatform.J2MEProfile.TYPE_CONFIGURATION:
                        result[0] = p.toString();
                        if (sbBootCp.length() > 0) {
                            sbBootCp.append(':');
                        }
                        sbBootCp.append(p.getClassPath());
                        break;
                    case J2MEPlatform.J2MEProfile.TYPE_PROFILE:
                        result[1] = p.toString();
                        if (sbBootCp.length() > 0) {
                            sbBootCp.append(':');
                        }
                        sbBootCp.append(p.getClassPath());
                        break;
                    case J2MEPlatform.J2MEProfile.TYPE_OPTIONAL:
                        if (sbOptional.length() > 0) {
                            sbOptional.append(',');
                        }
                        sbOptional.append(p.getName());
                        if (sbBootCp.length() > 0) {
                            sbBootCp.append(':');
                        }
                        sbBootCp.append(p.getClassPath());
                        break;
                }
            }
        }
        result[2] = sbOptional.toString();
        result[3] = sbBootCp.toString();
        return result;
    }

    /**
     * Helper class for downloading of J2ME Platform SDK.
     */
    public static class J2MEPlatformDownloader {

        public static boolean downloadPlatform(URL url, File targetDir) throws IOException {
            System.out.println("Downloading J2ME Platform from " + url.toString());
            targetDir.delete();
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            InputStream in = new BufferedInputStream(url.openStream());
            File zip = File.createTempFile("j2mePlatform", ".zip", targetDir);
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(zip))) {
                copyInputStream(in, out);
            }
            return unzipPlatform(zip, targetDir);
        }

        public static boolean unzipPlatform(File platformZip, File platformHome) throws IOException {
            if (!platformZip.exists()) {
                throw new IOException(platformZip.getAbsolutePath() + " does not exist");
            }
            if (!directoryExists(platformHome)) {
                throw new IOException("Could not create directory: " + platformHome);
            }
            try (ZipFile zipFile = new ZipFile(platformZip)) {
                for (Enumeration entries = zipFile.entries(); entries.hasMoreElements();) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    File file = new File(platformHome, File.separator + entry.getName());
                    if (!directoryExists(file.getParentFile())) {
                        throw new IOException("Could not create directory: " + file.getParentFile());
                    }
                    if (!entry.isDirectory()) {
                        copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(file)));
                    } else {
                        if (!directoryExists(file)) {
                            throw new IOException("Could not create directory: " + file);
                        }
                    }
                }
            }
            System.out.println("J2ME Platform has been sucessfully downloaded and unzipped.");
            return true;
        }

        public static void copyInputStream(InputStream in, OutputStream out) throws IOException {
            byte[] buffer = new byte[1024];
            int len = in.read(buffer);
            while (len >= 0) {
                out.write(buffer, 0, len);
                len = in.read(buffer);
            }
            in.close();
            out.close();
        }

        public static boolean directoryExists(File file) {
            return file.exists() || file.mkdirs();
        }
    }

}
