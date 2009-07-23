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

package org.netbeans.modules.mobility.project.security;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.mobility.project.ui.security.ExportPanel;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author David Kaspar
 */
public class MEKeyTool {
    
    public static String getMEKeyToolPath(final Object target) {
        if (target == null)
            return null;

        if (target instanceof J2MEPlatform){
            return getMEKeyToolPath((J2MEPlatform)target);
        } else {
            return getMEKeyToolPath((J2MEPlatform.Device)target);
        }
    }

    public static String getMEKeyToolPath(final J2MEPlatform platform) {
        if (platform == null)
            return null;
        final FileObject toolFO = platform.findTool("mekeytool"); // NOI18N
        if (toolFO == null)
            return null;
        final File toolFile = FileUtil.toFile(toolFO);
        if (toolFile == null)
            return null;
        return toolFile.getAbsolutePath();
    }
    
    public static String getMEKeyToolPath(final J2MEPlatform.Device device) {
        final Collection<JavaPlatform> javaMEPlatforms = ExportPanel.getJavaMEPlatformsWithoutBdj();
        for (final JavaPlatform javaPlatform : javaMEPlatforms) {
            if (javaPlatform instanceof J2MEPlatform) {
                final J2MEPlatform j2MEPlatform = (J2MEPlatform) javaPlatform;
                J2MEPlatform.Device[] devices = j2MEPlatform.getDevices();
                for (J2MEPlatform.Device deviceInPlatform : devices) {
                    if (deviceInPlatform.equals(device)) {
                        final String result = getMEKeyToolPath(j2MEPlatform);
                        return result;
                    }
                }
            }
        }
        return null;
    }

    public static String keystoreForDevice(final J2MEPlatform.Device device) {
        final JavaPlatformManager platformManager = JavaPlatformManager.getDefault();
        for (final JavaPlatform platform : platformManager.getInstalledPlatforms()) {
            if (platform != null) {
                if (platform instanceof J2MEPlatform) {
                    J2MEPlatform.Device[] devices = ((J2MEPlatform) platform).getDevices();
                    for (J2MEPlatform.Device dev : devices) {
                        if (dev.equals(device))
                            return keystorePathForDevice(((J2MEPlatform) platform), device.getName());
                    }
                }
            }
        }
        return  null;
    }

    public static KeyDetail[] listKeys(final Object target) {
        if (target == null)
            return null;

        if (target instanceof J2MEPlatform){
            return listKeys((J2MEPlatform)target);
        } else {
            return listKeys((J2MEPlatform.Device)target);
        }
    }

    public static KeyDetail[] listKeys(final J2MEPlatform platform) {
        final String toolString = getMEKeyToolPath(platform);
        if (toolString == null)
            return null;
        try {
            final BufferedReader br = execute(new String[] { toolString, "-list" }); // NOI18N
            final ArrayList<KeyDetail> list = new ArrayList<KeyDetail>();
            KeyDetail key = null;
            for (; ;) {
                final String line = br.readLine();
                if (line == null)
                    break;
                if ("".equals(line)) // NOI18N
                    continue;
                if (line.startsWith("Key ")) { // NOI18N
                    if (key != null)
                        list.add(key);
                    try {
                        key = new KeyDetail(Integer.parseInt(line.substring("Key ".length()))); // NOI18N
                    } catch (NumberFormatException e) {
                        key = null;
                    }
                } else if (key != null)
                    key.addLine(line);
            }
            if (key != null)
                list.add(key);
            return list.toArray(new KeyDetail[list.size()]);
        } catch (IOException e) {
            return null;
        }
    }
    
    public static KeyDetail[] listKeys(final J2MEPlatform.Device device) {
        final String toolString = getMEKeyToolPath(device);
        String ksPath = keystoreForDevice(device);

        if (toolString == null)
            return null;
        try {
            final BufferedReader br = execute(new String[] { toolString, "-list", "-MEkeystore", ksPath}); // NOI18N
            final ArrayList<KeyDetail> list = new ArrayList<KeyDetail>();
            KeyDetail key = null;
            for (; ;) {
                final String line = br.readLine();
                if (line == null)
                    break;
                if ("".equals(line)) // NOI18N
                    continue;
                if (line.startsWith("Key ")) { // NOI18N
                    if (key != null)
                        list.add(key);
                    try {
                        key = new KeyDetail(Integer.parseInt(line.substring("Key ".length()))); // NOI18N
                    } catch (NumberFormatException e) {
                        key = null;
                    }
                } else if (key != null)
                    key.addLine(line);
            }
            if (key != null)
                list.add(key);
            return list.toArray(new KeyDetail[list.size()]);
        } catch (IOException e) {
            return null;
        }
    }

    private static String keystorePathForDevice(final J2MEPlatform platform, final String deviceName) {
        File systemPropsFile = new File(platform.getHomePath() + "/toolkit-lib/modules/bootstrap/conf/system.properties"); //NOI18N
        if (!systemPropsFile.exists()) {
            return null;
        }

        Properties systemProps = new Properties();
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(systemPropsFile));
            systemProps.load(bis);
        } catch (IOException iOException) {
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        File deviceWorkingDir = new File(System.getProperty("user.home") + File.separatorChar + systemProps.getProperty("toolkits.dir") + File.separatorChar + systemProps.getProperty("release.dir") + File.separatorChar + systemProps.getProperty("work.dir")); //NOI18N
        if (!deviceWorkingDir.exists()) {
            FileObject manager = platform.findTool("emulator"); //NOI18N
            if (manager == null) return null;
            try {
                execute(new String[]{FileUtil.toFile(manager).toString()});
            } catch (IOException ex) { } //no need to notify
            if (!deviceWorkingDir.exists()){
                return null;
            }
        }

        FileObject deviceFolderFO = FileUtil.toFileObject(FileUtil.normalizeFile(deviceWorkingDir));
        for (FileObject device : deviceFolderFO.getChildren()) {
            FileObject xmlProps = device.getFileObject("properties", "xml"); //NOI18N
            if (xmlProps == null) {
                return null;
            }

            final boolean[] inName = {false};
            final boolean[] found = {false};
            try {
                XMLReader reader = XMLUtil.createXMLReader();
                reader.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {

                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                        if ("void".equals(qName) && "name".equals(attributes.getValue("property"))) { //NOI18N
                            inName[0] = true;
                        }
                    }

                    @Override
                    public void endElement(String uri, String localName, String qName) throws SAXException {
                        if (inName[0] == true && "string".equals(qName)) { //NOI18N
                            inName[0] = false;
                        }
                    }

                    @Override
                    public void characters(char[] ch, int start, int length) throws SAXException {
                        if (inName[0] == true) {
                            if (deviceName.equals(new String(ch, start, length))) {
                                found[0] = true;
                            }
                        }
                    }
                });
                reader.parse(new InputSource(xmlProps.getInputStream()));
            } catch (SAXException sAXException) {
            } catch (IOException iOException) {
            }

            if (found[0]) {
                final FileObject appDbFO = device.getFileObject("appdb"); //NOI18N
                if (null == appDbFO) {
                    return null;
                }
                final FileObject ksFO = appDbFO.getFileObject("_main.ks"); //NOI18N
                if (null == ksFO) {
                    return null;
                }
                final String ksPath = ksFO.toString();
                return ksPath;
            }
        }

        return null;
    }

    public static class KeyDetail {
        
        final private int order;
        final private ArrayList<String> info;
        
        public KeyDetail(int order) {
            this.order = order;
            this.info = new ArrayList<String>();
        }
        
        public void addLine(final String line) {
            info.add(line);
        }
        
        public int getOrder() {
            return order;
        }
        
        public String[] getInfo() {
            return info.toArray(new String[info.size()]);
        }
        
        public String getOwner() {
            for (String s : info ) {
                if (s == null)
                    continue;
                s = s.trim();
                if (s.startsWith("Owner:")) // NOI18N
                    return s.substring("Owner:".length()).trim(); // NOI18N
            }
            return null;
        }
        
    }
    
    public static BufferedReader execute(final String[] arguments) throws IOException {
        File binDir = null;
        if (arguments != null  && arguments.length > 0) {
            final File execFile = new File(arguments[0]);
            binDir = (execFile.exists()) ? execFile.getParentFile() : null;
        }
        
        final int os = Utilities.getOperatingSystem();
        Thread currentThread = null;
        int currentPriority = 0;
        
        Process process = null;
        InputStream inputStream = null;
        InputStream errorStream = null;
        
        try {
            if (os == Utilities.OS_WIN98) {
                currentThread = Thread.currentThread();
                currentPriority = currentThread.getPriority();
                currentThread.setPriority(Thread.NORM_PRIORITY);
            }
            process = (binDir != null) ? Runtime.getRuntime().exec(arguments, null, binDir) : Runtime.getRuntime().exec(arguments);
            inputStream = process.getInputStream();
            errorStream = process.getErrorStream();
        } finally {
            if (os == Utilities.OS_WIN98) {
                currentThread.setPriority(currentPriority);
            }
        }
        
        final StringBuffer executeOutput = new StringBuffer(1024);
        final StringBuffer executeError = new StringBuffer(1024);
        final StreamCatcher out = new StreamCatcher(inputStream, executeOutput);
        final StreamCatcher err = new StreamCatcher(errorStream, executeError);
        out.start();
        err.start();
        
        try {
            process.waitFor();
            out.join(10000);
            err.join(10000);
        } catch (InterruptedException ex) {
            throw (IOException) org.openide.ErrorManager.getDefault().annotate(new IOException(), ex);
        } finally {
            process.destroy();
        }
        if (process.exitValue() != 0) {
            throw new IOException("exec, exitCode != 0"); // NOI18N
        }
        return new BufferedReader(new StringReader(executeOutput.toString()));
    }
    
    private static class StreamCatcher extends Thread {
        
        InputStream is;
        StringBuffer sb;
        
        StreamCatcher(InputStream is, StringBuffer sb) {
            this.is = is;
            this.sb = sb;
        }
        
        @Override
        public void run() {
            try {
                final InputStreamReader r = new InputStreamReader(is);
                try {
                    final char[] buf = new char[256];
                    int len;
                    while ((len = r.read(buf)) >= 0)
                        sb.append(buf, 0, len);
                } finally {
                    r.close();
                }
            } catch (IOException ioEx) {
                ioEx.printStackTrace();
            }
        }
    }
    
}
