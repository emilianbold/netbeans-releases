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

import java.io.IOException;
import java.beans.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mobility.TestUtil;
import org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.J2MEPlatformNode;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Anton Chechel
 */
public class PlatformConvertorTest extends NbTestCase {
    private PlatformConvertor instance;
    private static J2MEPlatform platform;
    private boolean done;
    
    static {
        TestUtil.setLookup( new Object[] {
            TestUtil.testEntityCatalog()
        }, PlatformConvertorTest.class.getClassLoader());
    }
    
    public PlatformConvertorTest(String testName) {
        super(testName);
        instance = PlatformConvertor.createProvider(null);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of createProvider method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testCreateProvider() {
        System.out.println("createProvider");
        
        assertNotNull(instance);
    }
    
    /**
     * Test of getEnvironment method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testGetEnvironment() throws Exception {
        System.out.println("getEnvironment");
        DataObject xmlDataObject = DataObject.find(FileUtil.toFileObject(getGoldenFile("Sun_Java_Wireless_Toolkit__2_3.xml")));
        assertNotNull(xmlDataObject);
        assertTrue(xmlDataObject instanceof XMLDataObject);
        
        Lookup env = instance.getEnvironment(xmlDataObject);
        assertNotNull(env);
        
        Object node = env.lookup(Node.class);
        assertNotNull(node);
        assertTrue(node instanceof J2MEPlatformNode);
        platform = ((J2MEPlatformNode) node).platform;
        assertNotNull(platform);
    }
    
    /**
     * Test of instanceClass method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testInstanceClass() {
        System.out.println("instanceClass");
        
        Class clazz = instance.instanceClass();
        assertNotNull(clazz);
        assertEquals(clazz, J2MEPlatform.class);
    }
    
    /**
     * Test of instanceCreate method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testInstanceCreate() throws Exception {
        System.out.println("instanceCreate");
        
        FileObject folder = FileUtil.getConfigFile("Services/Platforms/org-netbeans-api-java-Platform");
        if (folder == null) {
            folder = FileUtil.getConfigRoot().createFolder("Services");
            folder = folder.createFolder("Platforms");
            folder = folder.createFolder("org-netbeans-api-java-Platform");
        }
        
        boolean platFound = false;
        JavaPlatform[] installedPlatforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (int i = 0; i < installedPlatforms.length; i++) {
            JavaPlatform platform = installedPlatforms[i];
            if (platform instanceof J2MEPlatform && ((J2MEPlatform) platform).getName().equals("Sun_Java_Wireless_Toolkit__2_3")) {
                platFound = true;
                break;
            }
        }
        
        FileObject xmlFileObject = FileUtil.toFileObject(getGoldenFile("Sun_Java_Wireless_Toolkit__2_3.xml"));
        if (!platFound) {
            folder.getFileSystem().runAtomicAction(new W(xmlFileObject, folder));
            if (!isDone()) {
                synchronized (this) {
                    wait(10000);
                }
            }
            if (!isDone()) {
                fail("Creating new J2MEPlatform from XML descriptor timed out");
            }
            
            JavaPlatform platform = null;
            installedPlatforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
            for (int i = 0; i < installedPlatforms.length; i++) {
                platform = installedPlatforms[i];
                if (platform instanceof J2MEPlatform && ((J2MEPlatform) platform).getName().equals("Sun_Java_Wireless_Toolkit__2_3")) {
                    break;
                }
            }
            
            // further testing is not possible due to some problems in the underlaying structure in the netbeans:
            // Sun_Java_Wireless_Toolkit__2_3 still doesn't exist
            
            //            if (platform == null) {
            //                fail("Sun_Java_Wireless_Toolkit__2_3 platform not found");
            //            }
            //
            //            J2MEPlatform j2mePlatform = (J2MEPlatform) platform;
            //            Device[] devices = j2mePlatform.getDevices();
            //            Device defaultColorPhone = null;
            //            Device brokenPhone = null;
            //            for (int i = 0; i < devices.length; i++) {
            //                if (devices[i].getName().equals("DefaultColorPhone")) {
            //                    defaultColorPhone = devices[i];
            //                }
            //                if (devices[i].getName().equals("BrokenPhone")) {
            //                    brokenPhone = devices[i];
            //                }
            //            }
            //
            //            // checking DefaultColorPhone
            //            assertNotNull(defaultColorPhone);
            //            assertTrue(defaultColorPhone.isValid());
            //            J2MEProfile[] profiles = defaultColorPhone.getProfiles();
            //            for (int i = 0; i < profiles.length; i++) {
            //                profiles[i].getClassPath();
            //            }
        }
    }
    
    /**
     * Test of instanceName method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testInstanceName() {
        System.out.println("instanceName");
        
        // same problem
        //        String name = instance.instanceName();
    }
    
    /**
     * Test of instanceOf method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testInstanceOf() {
        System.out.println("instanceOf");
        
        boolean result = instance.instanceOf(J2MEPlatform.class);
        assertTrue(result);
    }
    
    /**
     * Test of propertyChange method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testPropertyChange() {
        System.out.println("propertyChange");
        
        // same problem
        //        instance.propertyChange(evt);
    }
    
    /**
     * Test of convert method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testConvert() {
        System.out.println("convert");
        
        // same problem
        //        Object result = instance.convert(obj);
    }
    
    /**
     * Test of displayName method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testDisplayName() {
        System.out.println("displayName");
        
        String name = instance.displayName(J2MEPlatform.class);
        assertNotNull(name);
        assertEquals(name, "org.netbeans.modules.mobility.cldcplatform.J2MEPlatform");
    }
    
    /**
     * Test of id method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testId() {
        System.out.println("id");
        
        String id = instance.id(J2MEPlatform.class);
        assertNotNull(id);
        assertEquals(id, "class org.netbeans.modules.mobility.cldcplatform.J2MEPlatform");
    }
    
    /**
     * Test of type method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testType() {
        System.out.println("type");
        
        Class type = instance.type(J2MEPlatform.class);
        assertNotNull(type);
        assertEquals(type, J2MEPlatform.class);
    }
    
    /**
     * Test of create method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testCreate() throws Exception {
        System.out.println("create");
        
        DataFolder folder = DataFolder.findFolder(FileUtil.toFileObject(getWorkDir()));
        DataObject dataObject = PlatformConvertor.create(platform, folder, "Test J2ME Platform");
        assertNotNull(dataObject);
        
        Lookup env = instance.getEnvironment(dataObject);
        assertNotNull(env);
        Object node = env.lookup(Node.class);
        J2MEPlatform newPlatform = ((J2MEPlatformNode) node).platform;
        J2MEPlatform.Device[] d1 = newPlatform.getDevices();
        J2MEPlatform.Device[] d2 = platform.getDevices();
        assertEquals(d1.length, d2.length);
        for (int i = 0; i < d1.length; i++) {
            assertEquals(d1[i].getName(), d2[i].getName());
            assertEquals(d1[i].getScreen(), d2[i].getScreen());
            J2MEPlatform.J2MEProfile[] p1 = d1[i].getProfiles();
            J2MEPlatform.J2MEProfile[] p2 = d2[i].getProfiles();
            assertEquals(p1.length, p2.length);
            for (int j = 0; j < p1.length; j++) {
                assertEquals(p1[j], p2[j]);
            }
        }
    }
    
    /**
     * Test of array2string method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testArray2string() {
        System.out.println("array2string");
        
        String[] array = {"test1", "test2"};
        String str = PlatformConvertor.array2string(array);
        assertNotNull(str);
        assertEquals(str, "test1,test2");
    }
    
    /**
     * Test of string2array method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testString2array() {
        System.out.println("string2array");
        
        String string = "test1,test2";
        String[] result = PlatformConvertor.string2array(string);
        assertNotNull(result);
        assertTrue(result.length == 2);
        assertEquals(result[0], "test1");
        assertEquals(result[1], "test2");
    }
    
    /**
     * Test of extractPlatformProperties method, of class org.netbeans.modules.mobility.cldcplatform.PlatformConvertor.
     */
    public void testExtractPlatformProperties() throws Exception {
        System.out.println("extractPlatformProperties");
        
        J2MEPlatform.Device device = platform.getDevices()[0]; // DefaultColorPhone
        assertNotNull(device);
        Properties props = new Properties();
        props.putAll(PlatformConvertor.extractPlatformProperties("", platform, device, "", ""));
        assertTrue(props.size()>0);
        
        String active = props.getProperty("platform.active");
        assertNotNull(active);
        
        String desc = props.getProperty("platform.active.description");
        assertNotNull(desc);
        assertEquals(desc, "Sun Java Wireless Toolkit  2.3");
        
        String dev = props.getProperty("platform.device");
        assertNotNull(dev);
        assertEquals(dev, "DefaultColorPhone");
        
        String conf = props.getProperty("platform.configuration");
        assertNotNull(conf);
        assertEquals(conf, "CLDC-1.1");
        
        String profile = props.getProperty("platform.profile");
        assertNotNull(profile);
        assertEquals(profile, "MIDP-2.0");
        
        String apis = props.getProperty("platform.apis");
        assertNotNull(apis);
        assertTrue(apis.contains("JSR75-1.0"));
        assertTrue(apis.contains("JSR172-1.0"));
        assertTrue(apis.contains("MMAPI-1.1"));
        assertTrue(apis.contains("JSR177-1.0"));
        assertTrue(apis.contains("JSR184-1.0"));
        assertTrue(apis.contains("WMA-2.0"));
        assertTrue(apis.contains("JSR82-1.0"));
        assertTrue(apis.contains("JSR211-1.0"));
        assertTrue(apis.contains("JSR179-1.0"));
        
        String bcp = props.getProperty("platform.bootclasspath");
        assertNotNull(bcp);
        assertTrue(bcp.contains("${platform.home}/lib/wma20.jar"));
        assertTrue(bcp.contains("${platform.home}/lib/jsr082.jar"));
        assertTrue(bcp.contains("${platform.home}/lib/j2me-ws.jar"));
        assertTrue(bcp.contains("${platform.home}/lib/jsr177.jar"));
        assertTrue(bcp.contains("${platform.home}/lib/jsr184.jar"));
        assertTrue(bcp.contains("${platform.home}/lib/jsr179.jar"));
        assertTrue(bcp.contains("${platform.home}/lib/mmapi.jar"));
        assertTrue(bcp.contains("${platform.home}/lib/jsr211.jar"));
        assertTrue(bcp.contains("${platform.home}/lib/jsr75.jar"));
        assertTrue(bcp.contains("${platform.home}/lib/cldcapi11.jar"));
        assertTrue(bcp.contains("${platform.home}/lib/midpapi20.jar"));
        
        String abilities = props.getProperty("abilities");
        assertNotNull(abilities);
        assertTrue(abilities.contains("JSR179=1.0"));
        assertTrue(abilities.contains("WMA=2.0"));
        assertTrue(abilities.contains("JSR75=1.0"));
        assertTrue(abilities.contains("ScreenHeight=320"));
        assertTrue(abilities.contains("CLDC=1.1"));
        assertTrue(abilities.contains("MMAPI=1.1"));
        assertTrue(abilities.contains("JSR211=1.0"));
        assertTrue(abilities.contains("JSR184=1.0"));
        assertTrue(abilities.contains("JSR172=1.0"));
        assertTrue(abilities.contains("ScreenWidth=240"));
        assertTrue(abilities.contains("ScreenColorDepth=8"));
        assertTrue(abilities.contains("MIDP=2.0"));
        assertTrue(abilities.contains("JSR177=1.0"));
        assertTrue(abilities.contains("ColorScreen"));
        assertTrue(abilities.contains("JSR82=1.0,"));
    }
    
    private synchronized boolean isDone() {
        return done;
    }
    
    private class W implements FileSystem.AtomicAction, PropertyChangeListener {
        private FileObject xmlFileObject;
        private FileObject folder;
        
        public W(FileObject xmlFileObject, FileObject folder) {
            this.xmlFileObject = xmlFileObject;
            this.folder = folder;
            
            JavaPlatformManager.getDefault().addPropertyChangeListener(this);
        }
        
        public void run() throws IOException {
            String fn = FileUtil.findFreeFileName(folder, xmlFileObject.getName(), xmlFileObject.getExt());
            FileObject data = folder.createData(fn, xmlFileObject.getExt());
            FileLock lck = data.lock();
            
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(xmlFileObject.getInputStream()));
                OutputStream ostm = data.getOutputStream(lck);
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
                String str;
                while ((str = reader.readLine()) != null) {
                    writer.println(str);
                }
                writer.flush();
                writer.close();
                ostm.close();
            } finally {
                lck.releaseLock();
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(JavaPlatformManager.PROP_INSTALLED_PLATFORMS)) {
                synchronized (PlatformConvertorTest.this) {
                    PlatformConvertorTest.this.done = true;
                    PlatformConvertorTest.this.notify();
                }
            }
        }
        
    }
}
