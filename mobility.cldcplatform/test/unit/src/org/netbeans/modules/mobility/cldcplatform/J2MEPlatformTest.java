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

package org.netbeans.modules.mobility.cldcplatform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.Profile;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mobility.cldcplatform.startup.PostInstallJ2meAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Anton Chechel
 */
public class J2MEPlatformTest extends NbTestCase {
    static private J2MEPlatform instance;
    static private String zipPath;
    static private String platHome;
    static private String osarch;
    static private Exception installed = null;    
    
    public J2MEPlatformTest(String testName) {
        super(testName);
        if (instance == null)
            createPlatform();
        String message=installed==null?"":installed.getMessage();        
        assertNull(message,installed);
        assertNotNull("Platform not installed correctly",instance);
        try {
            clearWorkDir();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    static private void createPlatform()  {
        String destPath = Manager.getWorkDirPath();
        String wtkStr = "wtk22";
        final   String rootStr="mobility";
        final String rootWTK=File.separator+"test"+File.separator+"emulators"+File.separator;
        osarch = System.getProperty("os.name", null);
        String ossuf = null;
        assertNotNull(osarch);
        if (osarch.toLowerCase().indexOf("windows") != -1) {
            ossuf = "22_win";
        } else if (osarch.toLowerCase().indexOf("linux") != -1) {
            ossuf = "22_linux";
        } else if (osarch.toLowerCase().indexOf("sunos") != -1) {
            wtkStr = "wtk21";
            ossuf = "21_sunos";
        } else {
            fail("Operating system architecture: " + osarch + " not supported");
        }
        
        String wtkPath=System.getProperty("wtk.base.dir");
        if (wtkPath == null)
        {
            String platPath;
            final String rootMobility=File.separator+rootStr;
            /* Get a path to wtk - dirty hack but I don't know any better way */
            String classPath=System.getProperty("java.class.path");
            int index=classPath.indexOf(rootMobility);
            if (index==-1) {
                /* Running as a part of validity test */
                String s2=Manager.getWorkDirPath();
                platPath=new File(s2).getParentFile().getParentFile().getParentFile().getParentFile().getParent()+rootMobility+rootWTK;
            } else {
                /* Running as a part of xtest test */
                int id1=classPath.lastIndexOf(File.pathSeparator,index)==-1?0:classPath.lastIndexOf(File.pathSeparator,index)+1;
                platPath=classPath.substring(id1,index+rootMobility.length())+rootWTK;
            }

            //File zipFile = getGoldenFile("wtk" + File.separator + "wtk" + ossuf + ".zip");
            zipPath = platPath+"wtk"+ossuf+".zip"; //zipFile.getAbsolutePath();
        }
        else
            zipPath=wtkPath;
        ZipFile zip = null;
        try {
            zip = new ZipFile(zipPath);
            Enumeration files = zip.entries();
            while (files.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) files.nextElement();
                if (entry.isDirectory())
                    new File(destPath, entry.getName()).mkdirs();
                else {
                    /* Extract only if not already present */
                    File test = new File(destPath + File.separator + entry.getName());
                    if (!(test.isFile() && test.length() == entry.getSize())) {
                        copy(zip.getInputStream(entry), entry.getName(), new File(destPath));
                    }
                }
            }
            
            //Modify scripts
            platHome=destPath+File.separator+"emulator"+File.separator+wtkStr;
            NbTestCase.assertTrue(new File(platHome).exists());
            PostInstallJ2meAction.installAction(platHome);
            
            if (osarch.indexOf("Windows") == -1) {//NOI18N
                java.lang.Runtime.getRuntime().exec("chmod -R +x " + destPath + File.separator + "emulator" + File.separator +
                        wtkStr + File.separator + "bin");
            }
        }    
        catch (IOException ex) {
            installed=ex;
            assertTrue("WTK zip file ("+zipPath+") not found or corrupted. Please add the correct zip file to run the tests",false);
        }
        finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                }
            }
        }
        
        FileObject root = FileUtil.getConfigRoot();
        if (root.getFileObject("Services") == null) {
            try
            {
                FileObject platformsFolder = root.createFolder("Services");
                platformsFolder = platformsFolder.createFolder("Platforms");
                platformsFolder = platformsFolder.createFolder("org-netbeans-api-java-Platform");
            }
            catch (IOException ex)
            {
                installed=ex;
            }
        }
        instance = new UEIEmulatorConfiguratorImpl(platHome).getPlatform();
    }
    
    static private void copy(InputStream input, String file, File target) throws IOException {
        if (input == null || file == null || file.length() == 0) {
            return;
        }
        File output = new File(target, file);
        output.getParentFile().mkdirs();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(output);
            copy(input, fos);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    static private void copy(InputStream is, OutputStream os) throws IOException {
        final byte[] buffer = new byte[4096];
        int len;
        
        for (;;) {
            len = is.read(buffer);
            if (len == -1) {
                return;
            }
            os.write(buffer, 0, len);
        }
    }
    
    protected void setUp() throws Exception {
        assertNotNull(instance);
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of getDevices method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetDevices() {
        System.out.println("getDevices");
        
        J2MEPlatform.Device[] devices = instance.getDevices();
        assertNotNull(devices);
        System.out.println("retrieved " + devices.length + " devices:");
        for (int i = 0; i < devices.length; i++) {
            System.out.println(devices[i]);
            assertNotNull(devices[i].getDescription());
            assertNotNull(devices[i].getName());
            assertNotNull(devices[i].getSecurityDomains());
            J2MEPlatform.J2MEProfile[] profiles = devices[i].getProfiles();
            for (int j = 0; j < profiles.length; j++) {
                assertNotNull(profiles[j].getClassPath());
                assertNotNull(profiles[j].getDependencies());
                assertNotNull(profiles[j].getDisplayName());
                assertNotNull(profiles[j].getDisplayNameWithVersion());
                assertNotNull(profiles[j].getType());
            }
            J2MEPlatform.Screen screen = devices[i].getScreen();
            assertNotNull(screen);
            Integer bitDepth = screen.getBitDepth();
            assertNotNull(bitDepth);
            assertTrue(bitDepth.intValue() > 0);
            Integer height = screen.getHeight();
            assertNotNull(height);
            assertTrue(height.intValue() > 0);
            Integer width = screen.getWidth();
            assertNotNull(width);
            assertTrue(width.intValue() > 0);
        }
    }
    
    /**
     * Test of setDevices method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testSetDevices() {
        System.out.println("setDevices");
        
        J2MEPlatform.Device[] devices = instance.getDevices();
        J2MEPlatform.J2MEProfile[] profiles = new J2MEPlatform.J2MEProfile[0];
        J2MEPlatform.Device d1 = new J2MEPlatform.Device("Device 1", "Description of Device 1", null, profiles, null);
        J2MEPlatform.Device d2 = new J2MEPlatform.Device("Device 2", "Description of Device 2", null, profiles, null);
        instance.setDevices(new J2MEPlatform.Device[] {d1, d2, devices[0]});
        
        devices = instance.getDevices();
        assertNotNull(devices);
        assertEquals(d1, devices[0]);
        assertEquals(d2, devices[1]);
    }
    
    /**
     * Test of resolveRelativePathToURL method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testResolveRelativePathToURL() {
        System.out.println("resolveRelativePathToURL");
        
        assertNull(instance.resolveRelativePathToURL(null));
        URL url = instance.resolveRelativePathToURL(zipPath);
        assertNotNull(url);
    }
    
    /**
     * Test of localfilepath2url method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testLocalfilepath2url() {
        System.out.println("localfilepath2url");
        
        URL url = instance.localfilepath2url(zipPath);
        assertNotNull(url);
    }
    
    /**
     * Test of resolveRelativePathToFileObject method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testResolveRelativePathToFileObject() {
        System.out.println("resolveRelativePathToFileObject");
        
        FileObject fo = instance.resolveRelativePathToFileObject(zipPath);
        assertNotNull(fo);
        
        String jarPath = getGoldenFile("MIDletSuite.jar").getAbsolutePath();
        fo = instance.resolveRelativePathToFileObject(jarPath);
        assertNotNull(fo);
        
        String jadPath = getGoldenFile("MIDletSuite.jad").getAbsolutePath();
        fo = instance.resolveRelativePathToFileObject(jadPath);
        assertNotNull(fo);
        
        String javaPath = getGoldenFile("Midlet.java").getAbsolutePath();
        fo = instance.resolveRelativePathToFileObject(javaPath);
        assertNotNull(fo);
        
        String classPath = getGoldenFile("Midlet.class").getAbsolutePath();
        fo = instance.resolveRelativePathToFileObject(classPath);
        assertNotNull(fo);
        
        String propPath = getGoldenFile("project.properties").getAbsolutePath();
        fo = instance.resolveRelativePathToFileObject(propPath);
        assertNotNull(fo);
    }
    
    /**
     * Test of getFilePath method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetFilePath() {
        System.out.println("getFilePath");
        
        String str = instance.getFilePath(FileUtil.toFileObject(FileUtil.normalizeFile(new File(zipPath))));
        assertNotNull(str);
        
        String jarPath = getGoldenFile("MIDletSuite.jar").getAbsolutePath();
        str = instance.getFilePath(FileUtil.toFileObject(new File(jarPath)));
        assertNotNull(str);
        
        String jadPath = getGoldenFile("MIDletSuite.jad").getAbsolutePath();
        str = instance.getFilePath(FileUtil.toFileObject(new File(jadPath)));
        assertNotNull(str);
        
        String javaPath = getGoldenFile("Midlet.java").getAbsolutePath();
        str = instance.getFilePath(FileUtil.toFileObject(new File(javaPath)));
        assertNotNull(str);
        
        String classPath = getGoldenFile("Midlet.class").getAbsolutePath();
        str = instance.getFilePath(FileUtil.toFileObject(new File(classPath)));
        assertNotNull(str);
        
        String propPath = getGoldenFile("project.properties").getAbsolutePath();
        str = instance.getFilePath(FileUtil.toFileObject(new File(propPath)));
        assertNotNull(str);
    }
    
    /**
     * Test of getAllLibrariesString method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetAllLibrariesString() {
        System.out.println("getAllLibrariesString");
        
        String str = instance.getAllLibrariesString();
        assertNotNull(str);
        // TODO how to check real LibrariesString?
    }
    
    /**
     * Test of getStandardLibraries method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetStandardLibraries() {
        System.out.println("getStandardLibraries");
        
        ClassPath classPath = instance.getStandardLibraries();
        assertNotNull(classPath);
        // TODO what should I test here?
    }
    
    /**
     * Test of getBootstrapLibraries method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetBootstrapLibraries() {
        System.out.println("getBootstrapLibraries");
        
        ClassPath classPath = instance.getBootstrapLibraries();
        assertNotNull(classPath);
        // TODO what should I test here?
    }
    
    /**
     * Test of getDisplayName method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetDisplayName() {
        System.out.println("getDisplayName");
        
        String name = instance.getDisplayName();
        assertNotNull(name);
    }
    
    /**
     * Test of setDisplayName method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testSetDisplayName() {
        System.out.println("setDisplayName");
        
        String displayName = "TEST123";
        instance.setDisplayName(displayName);
        assertEquals(instance.getDisplayName(), displayName);
    }
    
    /**
     * Test of setName method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testSetName() {
        System.out.println("setName");
        
        String name = "name123";
        instance.setName(name);
        assertEquals(instance.getName(), name);
    }
    
    /**
     * Test of toString method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testToString() {
        System.out.println("toString");
        
        assertEquals(instance.toString(), instance.getDisplayName());
    }
    
    /**
     * Test of getVendor method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetVendor() {
        System.out.println("getVendor");
        
        String vendor = instance.getVendor();
        assertNotNull(vendor);
    }
    
    /**
     * Test of getName method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetName() {
        System.out.println("getName");
        
        String name = instance.getName();
        assertNotNull(name);
    }
    
    /**
     * Test of getType method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetType() {
        System.out.println("getType");
        
        String type = instance.getType();
        assertNotNull(type);
    }
    
    /**
     * Test of getPreverifyCmd method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetPreverifyCmd() {
        System.out.println("getPreverifyCmd");
        
        String cmd = "preverify.exe";
        instance.setPreverifyCmd(cmd);
        assertEquals(cmd, instance.getPreverifyCmd());
    }
    
    /**
     * Test of getRunCmd method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetRunCmd() {
        System.out.println("getRunCmd");
        
        String cmd = "emulator.exe";
        instance.setRunCmd(cmd);
        assertEquals(cmd, instance.getRunCmd());
    }
    
    /**
     * Test of getDebugCmd method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetDebugCmd() {
        System.out.println("getDebugCmd");
        
        String cmd = "debug.exe";
        instance.setDebugCmd(cmd);
        assertEquals(cmd, instance.getDebugCmd());
    }
    
    /**
     * Test of getHomePath method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetHomePath() {
        System.out.println("getHomePath");
        
        String homePath = instance.getHomePath();
        assertNotNull(homePath);
    }
    
    /**
     * Test of getInstallFolders method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetInstallFolders() {
        System.out.println("getInstallFolders");
        
        Collection folders = instance.getInstallFolders();
        assertNotNull(folders);
        System.out.println("retrieved " + folders.size() + " folder(s):");
        for (Object folder : folders) {
            System.out.println(folder);
        }
    }
    
    /**
     * Test of getJavadocFolders method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetJavadocFolders() {
        System.out.println("getJavadocFolders");
        
        List folders = instance.getJavadocFolders();
        assertNotNull(folders);
        System.out.println("retrieved " + folders.size() + " folder(s):");
        for (Object folder : folders) {
            System.out.println(folder);
        }
    }
    
    /**
     * Test of setJavadocFolders method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testSetJavadocFolders() {
        System.out.println("setJavadocFolders");
        
        FileObject folder = FileUtil.toFileObject(FileUtil.normalizeFile(new File(zipPath))).getParent();
        List folders = new ArrayList(1);
        folders.add(folder);
        System.out.println("set folder: " + folder);
        instance.setJavadocFolders(folders);
        List l = instance.getJavadocFolders();
        assertNotNull(l);
        assertTrue(l.size() > 0);
        System.out.println("retrivied folders:");
        for (Object url : folders) {
            System.out.println(url);
        }
    }
    
    /**
     * Test of getJavadocPath method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetJavadocPath() {
        System.out.println("getJavadocPath");
        
        String jdp = instance.getJavadocPath();
        assertNotNull(jdp);
    }
    
    /**
     * Test of getSourceFolders method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetSourceFolders() {
        System.out.println("getSourceFolders");
        
        ClassPath classPath = instance.getSourceFolders();
        assertNotNull(classPath);
    }
    
    /**
     * Test of setSourceFolders method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testSetSourceFolders() {
        System.out.println("setSourceFolders");
        
        FileObject folder = FileUtil.toFileObject(FileUtil.normalizeFile(new File(zipPath))).getParent();
        List sources = new ArrayList(1);
        sources.add(folder);
        System.out.println("set folder: " + folder.getName());
        instance.setSourceFolders(sources);
        ClassPath retrivied = instance.getSourceFolders();
        assertNotNull(retrivied);
        System.out.println("retrivied folder: " + retrivied);
    }
    
    /**
     * Test of getSourcePath method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetSourcePath() {
        System.out.println("getSourcePath");
        
        String sourcePath = instance.getSourcePath();
        assertNotNull(sourcePath);
    }
    
    /**
     * Test of getProperties method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetProperties() {
        System.out.println("getProperties");
        
        Map map = instance.getProperties();
        assertNotNull(map);
        String name = (String) map.get("platform.ant.name");
        System.out.println("Property \"platform.ant.name\" is " + name);
        assertNotNull(name);
    }
    
    /**
     * Test of getSpecification method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetSpecification() {
        System.out.println("getSpecification");
        
        Specification specification = instance.getSpecification();
        assertNotNull(specification);
        assertNotNull(specification.getName());
        Profile[] profiles = specification.getProfiles();
        // BTW specification.getVersion() is null but it doesn's matter for us
        assertNotNull(profiles);
        assertTrue(profiles.length > 0);
        for (int i = 0; i < profiles.length; i++) {
            assertNotNull(profiles[i].getName());
            assertNotNull(profiles[i].getVersion());
        }
    }
    
    /**
     * Test of isValid method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testIsValid() {
        System.out.println("isValid");
        
        assertTrue(instance.isValid());
    }
    
    /**
     * Test of getSubFileObject method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testGetSubFileObject() {
        System.out.println("getSubFileObject");
        
        String name = "emulator";
        String binFolder = platHome + File.separator + "bin";
        String emulatorFileName = binFolder + File.separator + name;
        if (osarch.toLowerCase().indexOf("windows") != -1) {
            emulatorFileName += ".exe";
        }
        FileObject folder = FileUtil.toFileObject(FileUtil.normalizeFile(new File(binFolder)));
        FileObject emulator = FileUtil.toFileObject(FileUtil.normalizeFile(new File(emulatorFileName)));
        
        FileObject result = J2MEPlatform.getSubFileObject(folder, name);
        assertEquals(emulator, result);
    }
    
    /**
     * Test of findTool method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testFindTool() {
        System.out.println("findTool");
        
        String toolName = "emulator";
        FileObject folder = FileUtil.toFileObject(FileUtil.normalizeFile(new File(platHome)));
        Collection installFolders = new ArrayList(1);
        installFolders.add(folder);
        
        String emulatorFileName = platHome + File.separator + "bin" + File.separator + toolName;
        if (osarch.toLowerCase().indexOf("windows") != -1) {
            emulatorFileName += ".exe";
        }
        FileObject emulator = FileUtil.toFileObject(FileUtil.normalizeFile(new File(emulatorFileName)));
        FileObject result = J2MEPlatform.findTool(toolName, installFolders);
        assertEquals(emulator, result);
    }
    
    /**
     * Test of createPlatform method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testCreatePlatform() throws Exception {
        System.out.println("createPlatform");
        
        DataObject platform = J2MEPlatform.createPlatform(platHome);
        assertNotNull(platform);
    }
    
    /**
     * Test of computeUniqueName method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testComputeUniqueName() {
        System.out.println("computeUniqueName");
        
        String uniqueName = J2MEPlatform.computeUniqueName(instance.getName());
        assertNotNull(uniqueName);
    }
    
    /**
     * Test of setRunCmd method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testSetRunCmd() {
        System.out.println("setRunCmd");
        
        String runCmd = "emulator.exe";
        instance.setRunCmd(runCmd);
        assertEquals(runCmd, instance.getRunCmd());
    }
    
    /**
     * Test of setPreverifyCmd method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testSetPreverifyCmd() {
        System.out.println("setPreverifyCmd");
        
        String preverifyCmd = "preverify.exe";
        instance.setPreverifyCmd(preverifyCmd);
        assertEquals(preverifyCmd, instance.getPreverifyCmd());
    }
    
    /**
     * Test of setDebugCmd method, of class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform.
     */
    public void testSetDebugCmd() {
        System.out.println("setDebugCmd");
        
        String debugCmd = "debug.exe";
        instance.setDebugCmd(debugCmd);
        assertEquals(debugCmd, instance.getDebugCmd());
    }
}
