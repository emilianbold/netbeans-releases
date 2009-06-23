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

package org.openide.filesystems;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class XMLFileSystemTestHid extends TestBaseHid {
    /** Factory for all filesystems that want to use TCK in this class.
     */
    public static interface Factory {
        /** Creates a filesystem representing XML files at given URLs
         *
         * @param testName name of the test
         * @param layers URLs of layers to parse
         * @return new filesystem that represents those layers
         */
        public FileSystem createLayerSystem(String testName, URL[] layers) throws IOException;

        /** Assigns new URLs to existing filesystem.
         *
         * @param fs the filesystem produced by {@link #createLayerSystem(java.lang.String, java.net.URL[])}
         * @param layers new URLs that the filesystem shall switch to
         * @return true if successful, false if this operation is not supported
         */
        public boolean setXmlUrl(FileSystem fs, URL[] layers) throws IOException;
    }

    private String[] resources = new String[] {"a/b/c"};
    FileSystem xfs = null;

    public XMLFileSystemTestHid(String testName) {
        super(testName);
    }

    protected String[] getResources (String testName) {
        return resources;
    }


    public void testReset () throws Exception {        
        FileObject a = xfs.findResource("a");
        assertNotNull(a);
        

        FileChangeAdapter fcl = new FileChangeAdapter();
        a.addFileChangeListener(fcl);
        
        resources = new String[] {"a/b/c","a/b1/c"};

        if (!FileSystemFactoryHid.switchXMLSystem(xfs, this, createXMLLayer().toURL())) {
            // OK, unsupported
            return;
        }
        
        FileObject b1 = xfs.findResource("a/b1");
        assertNotNull(b1);                
        assertTrue(b1.isFolder());        
    }
    
    @Override
    protected void setUp() throws Exception {
        File f = createXMLLayer();
        xfs = FileSystemFactoryHid.createXMLSystem(getName(), this, f.toURL());
        this.testedFS = xfs;
        this.allTestedFS = new FileSystem[] { xfs };
        super.setUp();
    }

    private File createXMLLayer() throws IOException {
        String testName = getName();
        File f = TestUtilHid.createXMLLayer(testName, getResources(testName));
        return f;
    }

    public void testChangesAreFiredOnSetXMLUrlsIssue59160() throws Exception {
        File f = writeFile ("layer1.xml", 
"<filesystem>\n" +
    "<folder name='TestModule'>\n" +
        "<file name='sample.txt' >Ahoj</file>\n" +
    "</folder>\n" +
"</filesystem>\n"
        );
        
        File f2 = writeFile ("layer2.xml", 
"<filesystem>\n" +
    "<folder name='TestModule'>\n" +
        "<file name='sample.txt' >Hello!</file>\n" +
    "</folder>\n" +
"</filesystem>\n"
        );

        
        
        
        xfs = FileSystemFactoryHid.createXMLSystem(getName(), this, f.toURL());
        
        FileObject fo = xfs.findResource ("TestModule/sample.txt");
        assertEquals ("Four bytes there", 4, fo.getSize ());
        registerDefaultListener (fo);
        
        if (!FileSystemFactoryHid.switchXMLSystem(xfs, this, f2.toURL())) {
            // OK, unsupported
            return;
        }
        
        assertEquals ("Six bytes there", 6, fo.getSize ());
        
        fileChangedAssert ("Change in the content", 1);
    }

    public void testChangesAreFiredOnSetXMLUrlsWithURLsIssue59160() throws Exception {
        File u1 = writeFile("u1.txt", "Ahoj");
        File u2 = writeFile("u2.txt", "Hello!");
        
        File f = writeFile ("layer1.xml", 
"<filesystem>\n" +
    "<folder name='TestModule'>\n" +
        "<file name='sample.txt' url='u1.txt' />\n" +
    "</folder>\n" +
"</filesystem>\n"
        );
        
        File f2 = writeFile ("layer2.xml", 
"<filesystem>\n" +
    "<folder name='TestModule'>\n" +
        "<file name='sample.txt' url='u2.txt' />\n" +
    "</folder>\n" +
"</filesystem>\n"
        );

        
        
        
        xfs = FileSystemFactoryHid.createXMLSystem(getName(), this, f.toURL());
        
        FileObject fo = xfs.findResource ("TestModule/sample.txt");
        assertEquals ("Four bytes there", 4, fo.getSize ());
        registerDefaultListener (fo);
        
        if (!FileSystemFactoryHid.switchXMLSystem(xfs, this, f2.toURL())) {
            // OK, unsupported
            return;
        }
        
        assertEquals ("Six bytes there", 6, fo.getSize ());
        
        fileChangedAssert ("Change in the content", 1);
    }
    
    public void testChangesAreFiredOnSetXMLUrlsWithAttributesIssue21204() throws Exception {
        File f = writeFile ("layer1.xml", 
"<filesystem>\n" +
    "<folder name='TestModule'>\n" +
        "<file name='sample.txt' >\n" +
        "  <attr name='value' stringvalue='old' />\n" +
        "</file>\n" +
    "</folder>\n" +
"</filesystem>\n"
        );
        
        File f2 = writeFile ("layer2.xml", 
"<filesystem>\n" +
    "<folder name='TestModule'>\n" +
        "<file name='sample.txt' >\n" +
        "  <attr name='value' stringvalue='new' />\n" +
        "</file>\n" +
    "</folder>\n" +
"</filesystem>\n"
        );

        
        
        
        xfs = FileSystemFactoryHid.createXMLSystem(getName(), this, f.toURL());
        
        FileObject fo = xfs.findResource ("TestModule/sample.txt");
        assertEquals("Old value is in the attribute", "old", fo.getAttribute("value"));
        registerDefaultListener (fo);
        
        if (!FileSystemFactoryHid.switchXMLSystem(xfs, this, f2.toURL())) {
            // OK, unsupported
            return;
        }

        assertEquals("New value is in the attribute", "new", fo.getAttribute("value"));
        fileAttributeChangedAssert("Change in the content", 1);
    }
    
    public void testChangesAreFiredOnSetXMLUrlsEvenWhenRemoved() throws Exception {
        File u1 = writeFile("u1.txt", "Ahoj");
        
        File f = writeFile("layer1.xml",
                "<filesystem>\n" +
                "<folder name='TestModule'>\n" +
                "<file name='sample.txt' url='u1.txt' />\n" +
                "</folder>\n" +
                "</filesystem>\n"
                );
        
        File f2 = writeFile("layer2.xml",
                "<filesystem>\n" +
                "</filesystem>\n"
                );
        
        
        
        
        xfs = FileSystemFactoryHid.createXMLSystem(getName(), this, f.toURL());
        
        FileObject fo = xfs.findResource("TestModule/sample.txt");
        assertEquals("Four bytes there", 4, fo.getSize());
        registerDefaultListener(fo);
        
        if (!FileSystemFactoryHid.switchXMLSystem(xfs, this, f2.toURL())) {
            // OK, unsupported
            return;
        }
        
        assertFalse("Valid no more", fo.isValid());
        assertEquals("Empty now", 0, fo.getSize());
        
        fileDeletedAssert("Change in the content", 1);
    }
    
    public void testIssue62570() throws Exception {
        File f = writeFile("layer3.xml",
                "<filesystem>\n" +
                "<folder name='TestModule'>\n" +
                "<file name='sample.txt' >Ahoj</file>\n" +
                "<file name='sample2.txt' url='sample2.txt'/>\n" +
                "</folder>\n" +
                "</filesystem>\n"
                );
              
        File f2 = new File(f.getParentFile(), "sample2.txt");        
        if (!f2.exists()) {
            Thread.sleep(3000);
            assertTrue(f2.createNewFile());
        }
        xfs = FileSystemFactoryHid.createXMLSystem(getName(), this, f.toURL());
        FileObject fo = xfs.findResource ("TestModule/sample.txt");
        assertNotNull(fo);
        assertEquals(fo.lastModified().getTime(), f.lastModified());        
        
        FileObject fo2 = xfs.findResource ("TestModule/sample2.txt");
        assertNotNull(fo2);
        assertEquals(fo2.lastModified().getTime(), f2.lastModified());        
        assertFalse(fo2.lastModified().equals(fo.lastModified()));        
        
    }


    public void testNoInstanceCreatedWithNewValue() throws Exception {
        Count.cnt = 0;
        File f = writeFile("layer.xml",
                "<filesystem>\n" +
                "<folder name='TestModule'>\n" +
                "<file name='sample.txt' >" +
                "  <attr name='instanceCreate' newvalue='org.openide.filesystems.Count'/>" +
                "</file>\n" +
                "</folder>\n" +
                "</filesystem>\n"
                );

        xfs = FileSystemFactoryHid.createXMLSystem(getName(), this, f.toURL());
        FileObject fo = xfs.findResource ("TestModule/sample.txt");
        assertNotNull(fo);
        
        Object clazz = fo.getAttribute("class:instanceCreate");
        assertEquals("No instance of Count created", 0, Count.cnt);
        assertEquals("Yet right class guessed", Count.class, clazz);
        Object instance = fo.getAttribute("instanceCreate");
        assertEquals("One instance of Count created", 1, Count.cnt);
        assertNotNull("Returned", instance);
        assertEquals("Right class", Count.class, instance.getClass());
    }

    public void testGetAttributeDoesNotAccessAllAttributes() throws Exception {
        File f = writeFile("layer.xml",
                "<filesystem>\n" +
                "<folder name='TestModule'>\n" +
                "<file name='sample.txt' >" +
                "  <attr name='instanceCreate' methodvalue='org.openide.filesystems.Count.createFromAttribs'/>" +
                "</file>\n" +
                "</folder>\n" +
                "</filesystem>\n"
                );
        Count.cnt = 0;

        xfs = FileSystemFactoryHid.createXMLSystem(getName(), this, f.toURL());
        /** the following is a fake implementation of filesystem that
         * allows us to prevent calls to fileObject.getAttributes()
         */
        class AFS extends AbstractFileSystem
        implements AbstractFileSystem.List, AbstractFileSystem.Attr, AbstractFileSystem.Info {
            AFS() {
                this.attr = this;
                this.list = this;
                this.info = this;
            }

            @Override
            public String getDisplayName() {
                return "AFS";
            }

            @Override
            public boolean isReadOnly() {
                return true;
            }

            public String[] children(String f) {
                if (f.equals("")) {
                    return new String[] { "TestModule" };
                }
                if (f.equals("TestModule")) {
                    return new String[] { "sample.txt" };
                }
                return null;
            }

            public Object readAttribute(String name, String attrName) {
                if (name.equals("TestModule/sample.txt") && attrName.equals("add")) {
                    // this is the addition:
                    return 10;
                }
                return null;
            }

            public void writeAttribute(String name, String attrName, Object value) throws IOException {
                throw new UnsupportedOperationException();
            }

            public Enumeration<String> attributes(String name) {
                fail("This method shall not be called: " + name);
                return null;
            }

            public void renameAttributes(String oldName, String newName) {
                throw new UnsupportedOperationException();
            }

            public void deleteAttributes(String name) {
                throw new UnsupportedOperationException();
            }

            public Date lastModified(String name) {
                return new Date(1000L);
            }

            public boolean folder(String name) {
                return name.equals("TestModule");
            }

            public boolean readOnly(String name) {
                return true;
            }

            public String mimeType(String name) {
                return "content/unknown";
            }

            public long size(String name) {
                return 0;
            }

            public InputStream inputStream(String name) throws FileNotFoundException {
                throw new FileNotFoundException();
            }

            public OutputStream outputStream(String name) throws IOException {
                throw new IOException();
            }

            public void lock(String name) throws IOException {
                throw new IOException();
            }

            public void unlock(String name) {
            }

            public void markUnimportant(String name) {
            }
        }

        AFS afs = new AFS();
        MultiFileSystem mfs = new MultiFileSystem(new FileSystem[] { afs, xfs });

        FileObject fo = mfs.findResource ("TestModule/sample.txt");
        assertNotNull(fo);

        Count c = (Count)fo.getAttribute("instanceCreate");
        assertNotNull("Count found", c);
        assertEquals("Count is really 10", 10, Count.cnt);
    }

    public void testNoInstanceCreatedWithMethodValue1() throws Exception {
        Count.cnt = 0;
        File f = writeFile("layer.xml",
                "<filesystem>\n" +
                "<folder name='TestModule'>\n" +
                "<file name='sample.txt' >" +
                "  <attr name='instanceCreate' methodvalue='org.openide.filesystems.Count.create'/>" +
                "</file>\n" +
                "</folder>\n" +
                "</filesystem>\n"
                );

        xfs = FileSystemFactoryHid.createXMLSystem(getName(), this, f.toURL());
        FileObject fo = xfs.findResource ("TestModule/sample.txt");
        assertNotNull(fo);

        Object clazz = fo.getAttribute("class:instanceCreate");
        assertEquals("No instance of Count created", 0, Count.cnt);
        assertEquals("Yet right class guessed", Count.class, clazz);
        Object instance = fo.getAttribute("instanceCreate");
        assertEquals("One instance of Count created", 1, Count.cnt);
        assertNotNull("Returned", instance);
        assertEquals("Right class", Count.class, instance.getClass());
    }

    public void testNoInstanceCreatedWithMethodValue2() throws Exception {
        Count.cnt = 0;
        File f = writeFile("layer.xml",
                "<filesystem>\n" +
                "<folder name='TestModule'>\n" +
                "<file name='sample.txt' >" +
                "  <attr name='instanceCreate' methodvalue='org.openide.filesystems.Count.exec'/>" +
                "</file>\n" +
                "</folder>\n" +
                "</filesystem>\n"
                );

        xfs = FileSystemFactoryHid.createXMLSystem(getName(), this, f.toURL());
        FileObject fo = xfs.findResource ("TestModule/sample.txt");
        assertNotNull(fo);

        Object clazz = fo.getAttribute("class:instanceCreate");
        assertEquals("No instance of Count created", 0, Count.cnt);
        assertEquals("Only Runnable guessed as that is the return type of the method", Runnable.class, clazz);
        Object instance = fo.getAttribute("instanceCreate");
        assertEquals("One instance of Count created", 1, Count.cnt);
        assertNotNull("Returned", instance);
        assertEquals("Right class", Count.class, instance.getClass());
    }

    public void testMapsAreEqualWithoutCallsToAttributes() throws IOException {
        File f = writeFile("layer.xml",
            "<filesystem>\n" +
              "<folder name='TestModule'>\n" +
                "<file name='sample.txt' >" +
                "  <attr name='map' methodvalue='" + XMLFileSystemTestHid.class.getName() + ".map'/>" +
                "  <attr name='instanceCreate' methodvalue='" + XMLFileSystemTestHid.class.getName() + ".counter'/>" +
                "</file>\n" +
              "</folder>\n" +
            "</filesystem>\n"
        );

        xfs = FileSystemFactoryHid.createXMLSystem(getName(), this, f.toURL());
        FileObject fo = xfs.findResource ("TestModule/sample.txt");
        assertNotNull(fo);

        cnt = 0;

        Map m1 = (Map)fo.getAttribute("map");
        Map m2 = (Map)fo.getAttribute("map");

        if (m1 == m2) {
            fail("Surprise usually these two shall be different: " + m1);
        }
        assertTrue("But they have to be equal", m1.equals(m2));
        assertEquals("No calls to other attributes of the map", 0, cnt);
        assertEquals("Same hashcode", m1.hashCode(), m2.hashCode());
        assertEquals("Still no calls to other attributes of the map", 0, cnt);
    }

    static Map map(Map m) {
        return m;
    }
    static int cnt;
    static int counter() {
        return cnt++;
    }

    public void testClassBoolean() throws Exception {
        doPrimitiveTypeTest("boolvalue='true'", Boolean.class);
    }

    public void testClassByte() throws Exception {
        doPrimitiveTypeTest("bytevalue='1'", Byte.class);
    }

    public void testClassInt() throws Exception {
        doPrimitiveTypeTest("intvalue='1'", Integer.class);
    }

    public void testClassShort() throws Exception {
        doPrimitiveTypeTest("shortvalue='1'", Short.class);
    }

    public void testClassLong() throws Exception {
        doPrimitiveTypeTest("longvalue='1'", Long.class);
    }
    public void testClassDouble() throws Exception {
        doPrimitiveTypeTest("doublevalue='1.0'", Double.class);
    }
    public void testClassFloat() throws Exception {
        doPrimitiveTypeTest("floatvalue='1.0'", Float.class);
    }
    public void testClassString() throws Exception {
        doPrimitiveTypeTest("stringvalue='1'", String.class);
    }
    public void testClassURL() throws Exception {
        doPrimitiveTypeTest("urlvalue='http://www.netbeans.org'", URL.class);
    }

    private void doPrimitiveTypeTest(String value, Class<?> expClass) throws Exception {
        File f = writeFile("layer.xml",
                "<filesystem>\n" +
                "<folder name='TestModule'>\n" +
                "<file name='sample.txt' >" +
                "  <attr name='instanceCreate' " + value + "/>" +
                "</file>\n" +
                "</folder>\n" +
                "</filesystem>\n"
                );

        xfs = FileSystemFactoryHid.createXMLSystem(getName(), this, f.toURL());
        FileObject fo = xfs.findResource ("TestModule/sample.txt");
        assertNotNull(fo);

        Object clazz = fo.getAttribute("class:instanceCreate");
        assertEquals("Only Runnable guessed as that is the return type of the method", expClass, clazz);
        Object instance = fo.getAttribute("instanceCreate");
        assertNotNull("Returned", instance);
        assertEquals("Right class", expClass, instance.getClass());
    }
    
    
    private File writeFile(String name, String content) throws IOException {
        File f = new File (getWorkDir (), name);
        java.io.FileWriter w = new java.io.FileWriter (f);
        w.write(content);
        w.close();
        return f;
    }
    

    public void testAttribute08 () throws Exception {
      URL fsURLDef = XMLFileSystemTestHid.class.getResource("data/Attributes.xml");
      assertTrue ("Cannot create XML FS for testing purposes", fsURLDef != null);
      FileSystem fs = FileSystemFactoryHid.createXMLSystem(getName(), this, fsURLDef);
      FileObject fo = fs.findResource("testMethodValue");
      assertTrue ("Cannot acces  FileObject named testMethodValue", fo != null);

      String testName = "test1";
      Object obj = fo.getAttribute(testName);
      assertTrue ("methodValue failed", obj != null);
      assertEquals("methodValue doesn't keep order", obj, getObjectViaMethodValue1(fo, testName));

      testName = "test2";
      obj = fo.getAttribute(testName);
      assertTrue ("methodValue failed", obj != null);
      assertTrue ("methodValue doesn't keep order ",
      obj.equals(getObjectViaMethodValue2 (testName, fo)));

      testName = "test3";
      obj = fo.getAttribute(testName);
      assertTrue ("methodValue failed", obj != null);
      assertTrue ("methodValue doesn't keep order ",
      obj.equals(getObjectViaMethodValue3 (fo)));

      testName = "test4";
      obj = fo.getAttribute(testName);
      assertTrue ("methodValue failed", obj != null);
      assertTrue ("methodValue doesn't keep order ",
      obj.equals(getObjectViaMethodValue4 (testName)));

      testName = "test5";
      obj = fo.getAttribute(testName);
      assertTrue ("methodValue failed", obj != null);
      assertTrue ("methodValue doesn't keep order ",
      obj.equals(getObjectViaMethodValue5 ()));

      testName = "test6";
      obj = fo.getAttribute(testName);
      assertTrue ("methodValue failed", obj != null);
      assertEquals("even works for java.util.Map", "Ahoj1", obj);

      testName = "test7";
      obj = fo.getAttribute(testName);
      assertTrue ("methodValue failed", obj != null);
      assertEquals("works for map and string", "Ahoj1test7", obj);

      testName = "testLoc";
      obj = fo.getAttribute(testName);
      assertNotNull("Value returned", obj);
      assertEquals("works for bundle key", "Hello World!", obj);
    }
    public void testPeerAttribute() throws Exception {
      URL fsURLDef = XMLFileSystemTestHid.class.getResource("data/Attributes.xml");
      assertTrue ("Cannot create XML FS for testing purposes", fsURLDef != null);
      FileSystem fs = FileSystemFactoryHid.createXMLSystem(getName(), this, fsURLDef);
      root = fs.getRoot();
      FileObject fo = fs.findResource("peer/base");
      FileObject peer = fs.findResource("peer/snd");
      assertTrue ("Cannot acces  FileObject named testMethodValue", fo != null);

      Object obj = fo.getAttribute("testPeer");
      assertNotNull("methodValue failed", obj);
      assertEquals(Data.class, obj.getClass());
      Data data = (Data)obj;
      assertEquals("it is the top most fileobject", peer, data.peer);

    }

    
    public void testChangeOfAnAttributeInLayerIsFiredIfThereIsRealChange() throws Exception {
        XMLFileSystem fs = new XMLFileSystem();
        
        File f1 = changeOfAnAttributeInLayerIsFiredgenerateLayer("Folder", "java.awt.List");
        File f2 = changeOfAnAttributeInLayerIsFiredgenerateLayer("Folder", "java.awt.Button");
        File f3 = changeOfAnAttributeInLayerIsFiredgenerateLayer("NoChange", "nochange");

        fs.setXmlUrls (new URL[] { f1.toURL(), f3.toURL() } );
        
        FileObject file = fs.findResource("Folder/empty.xml");
        assertNotNull("File found in layer", file);
        
        FSListener l = new FSListener();
        file.addFileChangeListener(l);
        
        FileObject nochange = fs.findResource("NoChange/empty.xml");
        assertNotNull("File found in layer", nochange);
        FSListener no = new FSListener();
        nochange.addFileChangeListener(no);
        
        assertAttr("The first value is list", file, "value", "java.awt.List");
        assertAttr("Imutable value is nochange", nochange, "value", "nochange");
        
        fs.setXmlUrls (new URL[] { f2.toURL(), f3.toURL() } );
        String v2 = (String) file.getAttribute("value");
        assertEquals("The second value is button", "java.awt.Button", v2);
        
        assertEquals("One change: " + l.events, 1, l.events.size());
        
        if (!(l.events.get(0) instanceof FileAttributeEvent)) {
            fail("Wrong event: " + l.events);
        }
        
        assertAttr("Imutable value is still nochange", nochange, "value", "nochange");
        assertEquals("No change in this attribute: "  + no.events, 0, no.events.size());
    }    

    private static Image icon;

    /**
     * Called from layer, do not rename!
     */
    public static Object method(FileObject fo, String attr) {
        //System.err.println("CMTBH.m: fo=" + fo.getClass().getName() + "<" + fo.getPath() + ">; attr=" + attr + "; x=" + fo.getAttribute("x"));
        return String.valueOf(fo.getAttribute("x")) + "/" + attr;
    }

    public static Image icon() {
        assertNull("Called just once", icon);
        icon = new BufferedImage(133, 133, BufferedImage.TYPE_INT_ARGB);
        return icon;
    }

    public static Object map1(Map map) {
        return String.valueOf(map.get("x"));
    }
    public static Object map2(Map map, String attr) {
        return String.valueOf(map.get("x")) + "/" + attr;
    }
    public static Object mapImage(Map map) {
        return map.get("image");
    }
    public static Object mapDisplayName(Map map) {
        return map.get("displayName");
    }

    public void testVariousXMLAttributes() throws Exception {
        URL attribs = XMLFileSystemTestHid.class.getResource("test-layer-attribs.xml");
        xfs = FileSystemFactoryHid.createXMLSystem(getName(), this, attribs);

        clearWorkDir();

        FileSystem f = xfs;

        assertEquals("val/a", attr(xfs, "foo/bar", "a"));
        assertEquals("val", attr(xfs, "foo/bar", "map1"));
        assertEquals("val/map2", attr(xfs, "foo/bar", "map2"));
        assertEquals("Ahoj", attr(xfs, "foo/bar", "mapDisplayName"));
        Image read = (Image) attr(xfs, "foo/bar", "mapImage");
        assertNotNull("Image loaded", icon);
        assertEquals("Same image", icon, read);
    }
    public void testLayersAttribute() throws Exception {
        clearWorkDir();

        File f1 = new File(getWorkDir(), "layer1.xml");
        {
            FileWriter w = new FileWriter(f1);
            w.write(
                "<filesystem>" +
                "  <folder name='just1'>" +
                "    <file name='empty.xml'/>" +
                "  </folder>" +
                "  <folder name='both'>" +
                "    <file name='empty.xml'>" +
                "      <attr name='a' stringvalue='a'/>" +
                "    </file>" +
                "  </folder>" +
                "</filesystem>"
            );
            w.close();
        }
        File f2 = new File(getWorkDir(), "layer2.xml");
        {
            FileWriter w = new FileWriter(f2);
            w.write(
                "<filesystem>" +
                "  <folder name='just2'>" +
                "    <file name='empty.xml'/>" +
                "  </folder>" +
                "  <folder name='both'>" +
                "    <file name='empty.xml'>" +
                "      <attr name='b' stringvalue='b'/>" +
                "    </file>" +
                "  </folder>" +
                "</filesystem>"
            );
            w.close();
        }

        xfs = FileSystemFactoryHid.createXMLSystem(getName(), this, f1.toURL(), f2.toURL());


        FileObject just1 = xfs.findResource("just1/empty.xml");
        FileObject just2 = xfs.findResource("just2/empty.xml");
        FileObject both = xfs.findResource("both/empty.xml");

        String layersR = layers(xfs.getRoot());
        String layers1 = layers(just1);
        String layers2 = layers(just2);
        String layersB = layers(both);

        if (!layersR.contains(f1.toURI().toString())) {
            fail("Missing " + f1 + "\ninside: " + layersR);
        }
        if (!layersR.contains(f2.toURI().toString())) {
            fail("Missing " + f2 + "\ninside: " + layersR);
        }

        assertEquals(f1.toURL().toExternalForm(), layers1);
        assertEquals(f2.toURL().toExternalForm(), layers2);
        if (!layersB.contains(f1.toURI().toString())) {
            fail("Missing " + f1 + "\ninside: " + layersB);
        }
        if (!layersB.contains(f2.toURI().toString())) {
            fail("Missing " + f2 + "\ninside: " + layersB);
        }
    }

    private static String layers(FileObject fo) {
        Object obj = fo.getAttribute("layers");
        assertNotNull("layers attr found for " + fo, obj);
        assertTrue("attribute is URL[] for " + fo, obj instanceof URL[]);
        StringBuilder sb = new StringBuilder();
        for (URL u : ((URL[])obj)) {
            sb.append(u.toExternalForm());
        }
        return sb.toString();
    }

    private static Object attr(FileSystem f, String path, String a) throws IOException {
        FileObject fo = f.findResource(path);
        if (fo == null) return null;
        return fo.getAttribute(a);
    }
    
    private static void assertAttr(String msg, FileObject fo, String attr, String value) throws IOException {
        Object v = fo.getAttribute(attr);
        assertEquals(msg + "[" + fo + "]", value, v);
    }

    int cntl;
    private File changeOfAnAttributeInLayerIsFiredgenerateLayer(String folderName, String string) throws IOException {
        File f = new File(getWorkDir(), "layer" + (cntl++) + ".xml");
        FileWriter w = new FileWriter(f);
        w.write(
            "<filesystem>" +
            "<folder name='" + folderName + "'>" +
            "  <file name='empty.xml' >" +
            "    <attr name='value' stringvalue='" + string + "' />" +
            "  </file>" +
            "</folder>" +
            "</filesystem>"
        );
        w.close();
        return f;
    }
    
    private static class FSListener extends FileChangeAdapter {
        public List<FileEvent> events = new ArrayList<FileEvent>();
        public List<FileEvent> change = new ArrayList<FileEvent>();
        
        
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            events.add(fe);
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            events.add(fe);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            events.add(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            events.add(fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            events.add(fe);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            change.add(fe);
        }
        
    }
    private static String getObjectViaMethodValue1 (FileObject fo, String testName) {
        return fo.getPath()+testName;
    }

    private static String getObjectViaMethodValue1 (String testName, FileObject fo) {
        return testName+fo.getPath();
    }

    private static String getObjectViaMethodValue1 (FileObject fo) {
        return fo.getPath();
    }

    private static String getObjectViaMethodValue1 (String testName) {
        return testName;
    }

    private static String getObjectViaMethodValue1 () {
        return "";
    }
///
    private static String getObjectViaMethodValue2 (String testName, FileObject fo) {
        return testName+fo.getPath();
    }

    private static String getObjectViaMethodValue2 (FileObject fo) {
        return fo.getPath();
    }

    private static String getObjectViaMethodValue2 (String testName) {
        return testName;
    }

    private static String getObjectViaMethodValue2 () {
        return "";
    }
///
    private static String getObjectViaMethodValue3 (FileObject fo) {
        return fo.getPath();
    }

    private static String getObjectViaMethodValue3 (String testName) {
        return testName;
    }

    private static String getObjectViaMethodValue3 () {
        return "";
    }
///
    private static String getObjectViaMethodValue4 (String testName) {
        return testName;
    }

    private static String getObjectViaMethodValue4 () {
        return "";
    }
///
    private static String getObjectViaMethodValue5 () {
        return "";
    }

    private static Object getObjectViaMethodValue6 (Map attrs) {
        try {
            attrs.keySet().iterator().remove();
            return "UnsupportedOperationException";
        } catch (UnsupportedOperationException ex) {
            // ok
        }
        try {
            attrs.put("value1", "nothing");
            return "UnsupportedOperationException";
        } catch (UnsupportedOperationException ex) {
            // ok
        }
        try {
            attrs.remove("value1");
            return "UnsupportedOperationException";
        } catch (UnsupportedOperationException ex) {
            // ok
        }


        return attrs.get("value1");
    }
    private static Object getObjectViaMethodValue7 (Map<String,Object> attrs, String attrName) {
        assertEquals(9, attrs.keySet().size());
        try {
            attrs.entrySet().remove(null);
            return "UnsupportedOperationException";
        } catch (UnsupportedOperationException ex) {
            // ok
        }


        return attrs.get("value1") + attrName;
    }
    private static FileObject root;
    public static final class Data {
        Object peer;
        public Data() {
            FileObject fo = root.getFileObject("peer/snd");
            peer = fo.getAttribute("testPeer");
        }
    }
    private static Object getFO(FileObject fo) {
        return fo;
    }
    
}
