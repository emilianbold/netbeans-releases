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

/*
 * UEIEmulatorConfigurator.java -- synopsis.
 *
 *
 *
 *
 */
package org.netbeans.modules.mobility.cldcplatform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.io.NullOutputStream;
import org.openide.util.io.ReaderInputStream;
import org.openide.ErrorManager;

/**
 *
 * @author  Martin Ryzl, David Kaspar
 */
public final class UEIEmulatorConfiguratorImpl {
    
    /** Version command. */
    private static final String CMD_VERSION = "-version"; // NOI18N
    /** Query command. */
    private static final String CMD_QUERY = "-Xquery"; // NOI18N
    
    //if two versions of the same API is listed then higher version must go first
    private static final String[][] APIs = {
        {"CLDC",   "1.1", "java/lang/Object.class", "java/lang/Double.class", "java/io/InputStream.class", "java/util/Date.class", "javax/microedition/io/Connection.class"},
        {"CLDC",   "1.0", "java/lang/Object.class", "java/io/InputStream.class", "java/util/Date.class", "javax/microedition/io/Connection.class"},
        {"MIDP",   "2.0", "java/lang/IllegalStateException.class", "java/util/Timer.class", "javax/microedition/io/PushRegistry.class", "javax/microedition/lcdui/CustomItem.class", "javax/microedition/lcdui/game/GameCanvas.class", "javax/microedition/rms/RecordStore.class", "javax/microedition/midlet/MIDlet.class", "javax/microedition/pki/Certificate.class"},
        {"MIDP",   "1.0", "java/lang/IllegalStateException.class", "java/util/Timer.class", "javax/microedition/io/HttpConnection.class", "javax/microedition/lcdui/Display.class", "javax/microedition/rms/RecordStore.class", "javax/microedition/midlet/MIDlet.class"},
        {"JSR172", "1.0", "javax/microedition/xml/rpc/Element.class"},
        {"JSR82",  "1.0", "javax/bluetooth/DiscoveryAgent.class"},
        {"OBEX",   "1.0", "javax/obex/ClientSession.class"},
        {"JSR75",  "1.0", "javax/microedition/io/file/FileConnection.class", "javax/microedition/pim/PIM.class"},
        {"JSR177", "1.0", "javax/microedition/apdu/APDUConnection.class", "javax/microedition/jcrmi/JavaCardRMIConnection.class"},
        {"JSR179", "1.0", "javax/microedition/location/Location.class"},
        {"JSR184", "1.0", "javax/microedition/m3g/Object3D.class"},
        {"JSR211", "1.0", "javax/microedition/content/ContentHandler.class"},
        {"JSR238", "1.0", "javax/microedition/global/ResourceManager.class"},
        {"MMAPI",  "1.0", "javax/microedition/media/TimeBase.class", "javax/microedition/media/control/VolumeControl.class", "javax/microedition/media/protocol/ContentDescriptor.class"},
        {"WMA",    "2.0", "javax/wireless/messaging/MultipartMessage.class"},
        {"WMA",    "1.1", "javax/wireless/messaging/Message.class"},
        {"NOKIAUI","1.0", "com/nokia/mid/ui/FullCanvas.class"},
        {"IMP",    "1.0", "java/lang/IllegalStateException.class", "java/util/Timer.class", "javax/microedition/io/HttpConnection.class", "javax/microedition/rms/RecordStore.class", "javax/microedition/midlet/MIDlet.class"},
        {"IMP-NG", "1.0", "java/lang/IllegalStateException.class", "java/util/Timer.class", "javax/microedition/rms/RecordStore.class", "javax/microedition/midlet/MIDlet.class", "javax/microedition/pki/Certificate.class"},
        {"AJOF",   "1.0", "com/siemens/icm/ajof/WmMIDlet.class"},
    };
    
    private static File getFile(final File root, String path) {
        path = path.trim();
        File f = new File(path);
        if (!f.exists()) f = new File(root, path);
        return  FileUtil.normalizeFile(f);
    }
    
    private static String cutPath(final File root, final Object o) {
        final String rootPath = root.getPath();
        String path = o instanceof JarFile ? ((JarFile)o).getName() : ((File)o).getPath();
        if (path.length() > rootPath.length() && path.startsWith(rootPath)) path = J2MEPlatform.PLATFORM_STRING_PREFIX + path.substring(rootPath.length() + 1);
        return path.replace('\\', '/');
    }
    
    public static List<J2MEPlatform.J2MEProfile> analyzePath(final File froot, final String classpath, final String defaultClasspath) {
        final ArrayList<J2MEPlatform.J2MEProfile> result = new ArrayList<J2MEPlatform.J2MEProfile>();
        StringTokenizer st = new StringTokenizer(defaultClasspath, ","); //NOI18N
        //String path elements
        final HashSet<String> defaultCp = new HashSet<String>();
        while (st.hasMoreTokens()) defaultCp.add(cutPath(froot, getFile(froot, st.nextToken())));
        
        st = new StringTokenizer(classpath, ","); //NOI18N
        //File or JarFile -> String path element
        final HashSet<Object> files = new HashSet<Object>();
        try {
            //tokenize path, filter elements and fill "files" by Files of JarFiles
            final HashSet<File> ff = new HashSet<File>();
            while (st.hasMoreTokens()) try {
                final File f = getFile(froot, st.nextToken());
                String name;
                if (ff.add(f)) {
                    if (f.isDirectory()) files.add(f);
                    else if (f.isFile() && ((name = f.getName().toLowerCase()).endsWith(".zip") || name.endsWith(".jar"))) files.add(new JarFile(f));
                }
            } catch (IOException ioe) {}
            
            //String API name -> set of roots that are already assigned to another version of the same API
            final HashMap<String,Set<Object>> assignedRoots = new HashMap<String,Set<Object>>();
            //String API name + version -> set of Files or JarFiles
            final HashMap<String,Set<Object>> foundAPIs = new HashMap<String,Set<Object>>();
            //String API name + version -> set of dependencies
            final HashMap<String,String> dependencies = new HashMap<String,String>();
            //String API name -> String API display name
            final HashMap<String,String> dispNames = new HashMap<String,String>();
            boolean foundMIDP = false;
            //first get the information from manifests
            for ( final Object root : files ) {
                final Manifest m = getManifest(root);
                if (m != null) {
                    final String api = m.getMainAttributes().getValue("API"); //NOI18N
                    String version = m.getMainAttributes().getValue("API-Specification-Version"); //NOI18N
                    if (api != null) {
                        foundMIDP = foundMIDP || "MIDP".equals(api); //NOI18N
                        if (version == null) version = "1.0"; //NOI18N
                        final String key = api + '-' + version;
                        
                        String d = m.getMainAttributes().getValue("API-Dependencies"); // NOI18N
                        if (d != null) dependencies.put(key, d);
                        d = m.getMainAttributes().getValue("API-Name"); // NOI18N
                        if (d != null) dispNames.put(api, d);
                        
                        Set<Object> old = foundAPIs.get(key);
                        if (old != null) {
                            old = new HashSet<Object>(old);
                            old.add(root);
                            foundAPIs.put(key, old);
                        } else foundAPIs.put(key, Collections.singleton(root));
                        
                        Set<Object> s = assignedRoots.get(api);
                        if (s == null) {
                            s = new HashSet<Object>();
                            assignedRoots.put(api, s);
                        }
                        s.add(root);
                    }
                }
            }
            
            //iterate over all API descriptors
            for (int i=0; i<APIs.length; i++) {
                if (foundAPIs.containsKey(APIs[i][0] + '-' + APIs[i][1])) continue;
                Set<Object> apiRoots = null;
                Set<Object> ignoreRoots = assignedRoots.get(APIs[i][0]);
                if (ignoreRoots == null) {
                    ignoreRoots = new HashSet<Object>();
                    assignedRoots.put(APIs[i][0], ignoreRoots);
                }
                //trigger coverage of the whole API requirements
                boolean[] foundTrigger = new boolean[APIs[i].length - 2];
                for ( final Object root : files ) {
                    if (ignoreRoots.contains(root)) continue;
                    //all means all fragments are here, any means at least one fragment found
                    boolean all = true, any = false;
                    for (int j=2; j<APIs[i].length; j++) {
                        if (contains(root, APIs[i][j])) {
                            foundTrigger[j-2] = true;
                            any = true;
                        } else all = false;
                    }
                    if (all) {
                        //assigning the root and go to the next API
                        apiRoots = Collections.singleton(root);
                        break;
                    } else if (any) {
                        //the root contains a fragment
                        if (apiRoots == null) apiRoots = new HashSet<Object>();
                        apiRoots.add(root);
                    }
                }
                //this indicates that the set may not be complete
                if (apiRoots instanceof HashSet) {
                    for (int j=0; j<foundTrigger.length; j++) {
                        if (!foundTrigger[j]) {
                            apiRoots = null;
                            break;
                        }
                    }
                }
                if (apiRoots != null) {
                    foundMIDP = foundMIDP || "MIDP".equals(APIs[i][0]); //NOI18N
                    ignoreRoots.addAll(apiRoots);
                    foundAPIs.put(APIs[i][0] + '-' + APIs[i][1], apiRoots);
                }
            }
            
            //extract undetected roots, fill J2MEPlatform.Profiles
            final HashSet<Object> undetected = new HashSet<Object>(files);
            for ( final Map.Entry<String,Set<Object>> e : foundAPIs.entrySet() ) {
                String name = e.getKey();
                int i = name.lastIndexOf('-');
                String version = name.substring(i + 1);
                name = name.substring(0, i);
                if (!foundMIDP || !("IMP".equals(name) || "IMP-NG".equals(name))) { //NOI18N
                    String dispName = dispNames.get(name);
                    try {
                        dispName = NbBundle.getMessage(UEIEmulatorConfiguratorImpl.class, name);
                    } catch (Exception ex) {}
                    final String type = "CLDC".equals(name) ? J2MEPlatform.J2MEProfile.TYPE_CONFIGURATION : ("MIDP".equals(name) || "IMP".equals(name) || "IMP-NG".equals(name) ? J2MEPlatform.J2MEProfile.TYPE_PROFILE : J2MEPlatform.J2MEProfile.TYPE_OPTIONAL); //NOI18N
                    final StringBuffer cp = new StringBuffer();
                    boolean def = true;
                    for ( final Object obj : e.getValue() ) {
                        final String cpEl = cutPath(froot, obj);
                        if (cp.length() > 0) cp.append(':');
                        cp.append(cpEl);
                        if (!defaultCp.contains(cpEl)) def = false;
                    }
                    result.add(new J2MEPlatform.J2MEProfile(name, version, dispName, type, dependencies.get(e.getKey()), cp.toString(), def));
                    undetected.removeAll(e.getValue());
                }
            }
            
            for ( Object obj : undetected ) {
                final String jarName = cutPath(froot, obj);
                String dispName = jarName.startsWith(J2MEPlatform.PLATFORM_STRING_PREFIX) ? jarName.substring(J2MEPlatform.PLATFORM_STRING_PREFIX.length()) : jarName;
                final J2MEPlatform.J2MEProfile prof = new J2MEPlatform.J2MEProfile(dispName, "1.0", dispName, J2MEPlatform.J2MEProfile.TYPE_OPTIONAL, null, jarName, defaultCp.contains(jarName));
                result.add(prof);
            }
            return result;
        } finally {
              for ( final Object o : files ) {
                if (o instanceof JarFile) try {
                    ((JarFile)o).close();
                } catch (IOException ioe) {}
            }
        }
    }
    
    //root can be JarFile of File
    private static boolean contains(final Object root, final String file) {
        if (root instanceof JarFile) {
            return ((JarFile)root).getEntry(file) != null;
        }
        return new File((File)root, file).isFile();
    }
    
    private static Manifest getManifest(final Object root) {
        try {
            if (root instanceof JarFile) {
                return ((JarFile)root).getManifest();
            } 
            final File f = new File((File)root, "META-INF/manifest.mf"); //NOI18N
            if (f.isFile() && f.canRead()) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(f);
                    return new Manifest(fis);
                } catch (IOException e) {
                } finally {
                    if (fis != null) try { fis.close(); } catch (IOException e) {}
                }
            }
        } catch (IOException ioe) {}
        return null;
    }
    
    
    
    
    
    /** platform path. */
    private String platformPath;
    /** Process name. */
    private String process;
    
    // temporary
    private String platformName;
    private Properties properties;
    private Properties versionProperties;
    private String platformType;
    
    private Process emulatorProcess;
    private StringBuffer emulatorOutput;
    private StringBuffer emulatorError;
    
    /** Creates a new instance of UEIEmulatorConfigurator */
    public UEIEmulatorConfiguratorImpl(String platformPath) {
        this.platformPath = FileUtil.normalizeFile(new File(platformPath)).getPath();
        this.process = platformPath + File.separatorChar + "bin" + File.separatorChar + "emulator"; // NOI18N
        if (Utilities.isWindows())
            this.process += ".exe"; //NOI18N
    }
    
    /** Get platform path.
     * @return platform path
     */
    public String getPlatformPath() {
        return platformPath;
    }
    
    public J2MEPlatform getPlatform() {
        return getPlatform(new OutputStreamWriter(new NullOutputStream()));
    }
    
    
    public J2MEPlatform getPlatform(final Writer writer) {
        final PrintWriter pw = writer != null ? new PrintWriter(writer) : null;
        
        // process version
        if (pw != null) pw.println(NbBundle.getMessage(UEIEmulatorConfiguratorImpl.class, "LAB_GettingConfiguration")); // NOI18N
        if (!processVersion(pw)) {
            if (pw != null) pw.println(NbBundle.getMessage(UEIEmulatorConfiguratorImpl.class, "LAB_Failed")); // NOI18N
            if (pw != null) pw.flush();
            return null;
        }
        
        // process properties
        if (pw != null) pw.println(NbBundle.getMessage(UEIEmulatorConfiguratorImpl.class, "LAB_GettingProperties")); // NOI18N
        if (!processProperties(pw)) {
            if (pw != null) pw.println(NbBundle.getMessage(UEIEmulatorConfiguratorImpl.class, "LAB_Failed")); // NOI18N
            if (pw != null) pw.flush();
            return null;
        }
        if (pw != null) pw.println(NbBundle.getMessage(UEIEmulatorConfiguratorImpl.class, "LAB_GettingAllProfiles")); // NOI18N
        
        platformType = "UEI-1.0"; // NOI18N
        
        final ArrayList<J2MEPlatform.Device> devices = resolveDevices(pw);
        
        if (pw != null) pw.println(NbBundle.getMessage(UEIEmulatorConfiguratorImpl.class, "LAB_Success")); // NOI18N
        if (pw != null) pw.flush();
        
        final String srcpath = null;
        final String docpath = findDocsPath();
        
        final J2MEPlatform.Device[] devicesArray = devices.toArray(new J2MEPlatform.Device[devices.size()]);
        if (devicesArray.length <= 0) {
            if (pw != null) pw.println(NbBundle.getMessage(UEIEmulatorConfiguratorImpl.class, "LAB_Failed")); // NOI18N
            if (pw != null) pw.flush();
            return null;
        }
        return new J2MEPlatform(J2MEPlatform.computeUniqueName(platformName), platformPath, platformType, platformName, srcpath, docpath, null, null, null, devicesArray);
    }
    
    private ArrayList<J2MEPlatform.Device> resolveDevices(final PrintWriter pw) {
        final ArrayList<J2MEPlatform.Device> devices = new ArrayList<J2MEPlatform.Device>();
        
        final StringTokenizer devicesTokens = new StringTokenizer(properties.getProperty("device.list", ""), ","); // NOI18N
        final File plDir = new File(platformPath);
        final File libDir = new File(platformPath, "lib"); //NOI18N
        File[] libFiles = null;
        if (libDir.exists()  &&  libDir.isDirectory()) libFiles = libDir.listFiles();
        
        final File libextFile = new File(platformPath, "lib/ext"); //NOI18N
        File[] files = null;
        if (libextFile.exists()  &&  libextFile.isDirectory()) files = libextFile.listFiles();

        while (devicesTokens.hasMoreTokens()) {
            final String deviceName = devicesTokens.nextToken().trim();
            final String deviceDescription = properties.getProperty(deviceName + ".description"); // NOI18N
            final String securitydomains = properties.getProperty(deviceName + ".security.domains"); // NOI18N
            final String[] securitydomainsArray = PlatformConvertor.string2array(securitydomains);
            
            // boot class path hash for resolving default jars
            String bootClassPathString = properties.getProperty(deviceName + ".bootclasspath"); // NOI18N
            if (bootClassPathString == null) {
                StringBuffer sb = null;

                if (libFiles != null) {
                    for (int a = 0; a < libFiles.length; a ++) {
                        final File file = libFiles[a];
                        if (file.exists()  &&  file.isFile()) {
                            final String name = file.getName().toLowerCase();
                            if (name != null  &&  (name.endsWith(".jar")  ||  name.endsWith(".zip"))) { //NOI18N
                                if (sb == null)
                                    sb = new StringBuffer(file.getAbsolutePath().length() * libFiles.length);
                                else
                                    sb.append(','); //NOI18N
                                sb.append(file.getAbsolutePath());
                            }
                        }
                    }
                }
                
                if (sb != null)
                    bootClassPathString = sb.toString();
            }
            
            // find jars
            String jarsString = properties.getProperty(deviceName + ".apis"); // NOI18N
            if (jarsString != null) {
                platformType = "UEI-1.0.1"; // NOI18N
            } else {
                jarsString = bootClassPathString;
                if (files != null) {
                    StringBuffer sb = null;
                    for (int a = 0; a < files.length; a ++) {
                        final File file = files[a];
                        if (file.exists()  &&  file.isFile()) {
                            final String name = file.getName().toLowerCase();
                            if (name != null  &&  (name.endsWith(".jar")  ||  name.endsWith(".zip"))) { //NOI18N
                                if (sb == null)
                                    sb = new StringBuffer(file.getAbsolutePath().length() * files.length);
                                else
                                    sb.append(','); //NOI18N
                                sb.append(file.getAbsolutePath());
                            }
                        }
                    }
                    if (sb != null) {
                        if (jarsString == null  ||  "".equals(jarsString)) //NOI18N
                            jarsString = sb.toString();
                        else
                            jarsString = bootClassPathString + "," + sb.toString(); //NOI18N
                    }
                }
                if (jarsString == null) {
                    if (pw != null) pw.println(NbBundle.getMessage(UEIEmulatorConfiguratorImpl.class, "WARN_IgnoringDevice", deviceName)); // NOI18N
                    continue;
                }
            }
            
            final String configuration = properties.getProperty(deviceName + ".version.configuration"); //NOI18N
            //ignore CDC devicess
            if (configuration != null && configuration.startsWith("CDC")){ //NOI18N
                continue;
            }

            String profile = properties.getProperty(deviceName + ".version.profile"); //NOI18N
            if (profile == null) {
                // Profile detection modified because of #221063 and #228551 - Adding TC65 WTK as Java ME CLDC Platform
                final String profileValue = versionProperties.getProperty("Profile"); //NOI18N
                profile = (profileValue != null && profileValue.contains("IMP-NG")) ? "IMP-NG" //NOI18N
                        : (profileValue != null && profileValue.contains("MIDP-2.1")) ? "MIDP-2.1" //NOI18N
                        : (profileValue != null && profileValue.contains("MIDP-2.0")) ? "MIDP-2.0" : null; //NOI18N
            }
            String profileName = null;
            if(profile != null) {
                final int i = profile.lastIndexOf('-');
                final String version = (i + 1 == profile.length()) ? "" : profile.substring(i + 1);
                profileName = ("NG".equalsIgnoreCase(version) || (i == -1)) ? profile : profile.substring(0, i);
            }
            final List<J2MEPlatform.J2MEProfile> jars = analyzePath(plDir, jarsString, bootClassPathString);
            List<J2MEPlatform.J2MEProfile> official = new ArrayList<J2MEPlatform.J2MEProfile>();
            for(J2MEPlatform.J2MEProfile p : jars) {
                if(!p.getType().equalsIgnoreCase("profile") || 
                        ( (profileName != null) && (p.getName().startsWith(profileName)) )
                        ) {
                    official.add(p);
                }
            }

            final String screenWidth = properties.getProperty(deviceName + ".screen.width"); //NOI18N
            final String screenHeight = properties.getProperty(deviceName + ".screen.height"); //NOI18N
            final String screenBitDepth = properties.getProperty(deviceName + ".screen.bitDepth"); //NOI18N
            final String screenIsColor = properties.getProperty(deviceName + ".screen.isColor"); //NOI18N
            final String screenIsTouch = properties.getProperty(deviceName + ".screen.isTouch"); //NOI18N
            final J2MEPlatform.Screen screen = new J2MEPlatform.Screen(screenWidth, screenHeight, screenBitDepth, screenIsColor, screenIsTouch);
            
            final J2MEPlatform.J2MEProfile[] profilesArray = official.toArray(new J2MEPlatform.J2MEProfile[official.size()]);
            devices.add(new J2MEPlatform.Device(deviceName, deviceDescription, securitydomainsArray, profilesArray, screen));
        }
        
        return devices;
    }

    private void findDocAPIDirs(final ArrayList<File> outputList, final File directory, final int depth) {
        final File[] files = directory.listFiles();
        if (files != null) for (int a = 0; a < files.length; a ++) {
            final File file = files[a];
            if (file.isDirectory()) {
                if (file.getName().toLowerCase().indexOf("doc") >= 0) {
                    findDocRoots(outputList, file, 3);
                } else if (depth > 0){
                    findDocAPIDirs(outputList, file, depth - 1);
                }
            }
        }
    }
    
    private void findDocRoots(final ArrayList<File> outputList, final File directory, final int depth) {
        final File[] files = directory.listFiles();
        if (files != null) for (int a = 0; a < files.length; a ++) {
            final File file = files[a];
            if (file.isFile() && "index.html".equals(file.getName().toLowerCase())) { //NOI18N
                outputList.add(directory);
            } else if (file.isFile() && file.getName().endsWith(".zip") && file.getParentFile().getName().equalsIgnoreCase("api")) { //NOI18N
                outputList.add(file);
            } else if (depth > 0 && file.isDirectory()) {
                findDocRoots(outputList, file, depth - 1);
            }
        }
    }
    
    private String findDocsPath() {
        final ArrayList<File> docs = new ArrayList<File>();
        findDocAPIDirs(docs, new File(platformPath), 3);
        StringBuffer ret = null;
        for (int a = 0; a < docs.size(); a ++) {
            if (ret == null)
                ret = new StringBuffer(docs.get(a).getAbsolutePath().length() * docs.size());
            else
                ret.append(", "); //NOI18N
            ret.append(FileUtil.normalizeFile(docs.get(a)).getPath());
        }
        return ret == null ? null : ret.toString();
    }
    
    /** Get output of the process
     * @param arguments arguments
     * @throws IOException if something goes wrong
     */
    private void execute(final String argument, final PrintWriter pw) throws IOException {
        final File execFile = new File(process);
        final File binDir = (execFile.exists()) ? execFile.getParentFile() : null;
        String[] s = new String[2];
        s[0] = process;
        s[1] = argument;
        
        emulatorProcess = (binDir != null) ? Runtime.getRuntime().exec(s, null, binDir) : Runtime.getRuntime().exec(s);
        final InputStream inputStream = emulatorProcess.getInputStream();
        final InputStream errorStream = emulatorProcess.getErrorStream();
        
        emulatorOutput = new StringBuffer(1024);
        emulatorError = new StringBuffer(1024);
        final StreamCatcher out = new StreamCatcher(inputStream, emulatorOutput, pw);
        final StreamCatcher err = new StreamCatcher(errorStream, emulatorError, pw);
        out.start();
        err.start();
        
        try {
            emulatorProcess.waitFor();
            out.join(10000);
            err.join(10000);
        } catch (InterruptedException ex) {
            throw (IOException) org.openide.ErrorManager.getDefault().annotate(new IOException(), ex);
        } finally{
            emulatorProcess.destroy();
        }
        if (emulatorProcess.exitValue() != 0) {
            throw new IOException("exec, exitCode != 0"); // NOI18N
        }
    }
    
    /** Reads data from input stream and writes them into StringBuffer
     */
    private static class StreamCatcher extends Thread {
        InputStream is;
        StringBuffer sb;
        final private PrintWriter pw;
        
        StreamCatcher(InputStream is, StringBuffer sb, PrintWriter pw){
            this.is = is;
            this.sb = sb;
            this.pw = pw;
        }
        
        public void run(){
            try{
                final InputStreamReader r = new InputStreamReader(is);
                try {
                    final char[] buf = new char[256];
                    int len;
                    while ((len = r.read(buf)) >= 0)
                        sb.append(buf, 0, len);
                } finally {
                    r.close();
                }
            } catch(IOException ioEx){
                if (pw != null) ioEx.printStackTrace(pw);
            }
        }
        
        public StringBuffer getBuffer() {
            return sb;
        }
    }
    
    private boolean resolveVersion(final StringBuffer sb) throws IOException {
        platformName = null;
        
        final BufferedReader br = new BufferedReader(new StringReader(sb.toString()));
        platformName = br.readLine();
        br.close();
        return platformName != null;
    }
    
    /** Get version of the platform.
     * Loads temporary fields
     * @return true if load is successful
     */
    private boolean processVersion(final PrintWriter pw) {
        versionProperties = new Properties();
        try {
            execute(CMD_VERSION, pw);
            if (! resolveVersion(emulatorOutput))
                if (! resolveVersion(emulatorError))
                    return false;
            
            // Platform detection modified because of #221063 and #228551 - Adding TC65 WTK as Java ME CLDC Platform
            final String output = emulatorOutput.toString();
            if (output != null && !output.isEmpty()) {
                versionProperties.load(new ReaderInputStream(new StringReader(output)));
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            return false;
        }
//        if (configuration == null) return false;
//        if (profile == null) return false;
        return true;
    }
    
    /** Get properties of the platform.
     * @return true if the initialization has been succesfull
     */
    private boolean processProperties(final PrintWriter pw) {
        properties = new Properties();
        try {
            execute(CMD_QUERY, pw);
            String output = emulatorOutput.toString();
            if ("".equals(output)) //NOI18N
                output = emulatorError.toString();
            properties.load(new ReaderInputStream(new StringReader(output)));
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            return false;
        }
        return true;
    }
    /*
    private static class APIDescriptor {
        private String name;
        private String version;
        private boolean isFromFileName;
        
        public APIDescriptor(String name) {
            this.name = name;
            int i = name.lastIndexOf("-"); // NOI18N
            if (i >= 0) {
                this.version = name.substring(i + 1);
                this.name = name.substring(0, i);
            } else {
                this.version = "1.0"; // NOI18N
            }
            this.isFromFileName = false;
        }
        
        public APIDescriptor(String name, String version, boolean isFromFileName) {
            this.name = name;
            this.version = version;
            if (version == null)
                this.version = "1.0"; // NOI18N
            this.isFromFileName = isFromFileName;
        }
        
        public String getName() {
            return name;
        }
        
        public String getVersion() {
            return version;
        }
        
        public boolean isFromFileName() {
            return isFromFileName;
        }
        
    }
    */
    public static void main(final String args[]) throws Exception {
        final UEIEmulatorConfiguratorImpl c = new UEIEmulatorConfiguratorImpl("/opt/nokia/Devices/Series_60_MIDP_Concept_SDK_Beta_0_3_1_Nokia_edition"); // NOI18N
        c.getPlatform(new OutputStreamWriter(System.out));
    }
    
}
