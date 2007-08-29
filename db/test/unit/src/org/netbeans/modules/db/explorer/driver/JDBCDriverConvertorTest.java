/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.driver;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Collection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.db.test.DOMCompare;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Andrei Badea
 */
public class JDBCDriverConvertorTest extends TestBase {
    
    public JDBCDriverConvertorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        Util.deleteDriverFiles();
    }
    
    public void testReadXml() throws Exception {
        // DTD version 1.0
        FileObject fo = createDriverFile10("org_foo_FooDriver_10.xml", Util.getDriversFolder());
        DataObject dobj = DataObject.find(fo);
        InstanceCookie ic = (InstanceCookie)dobj.getCookie(InstanceCookie.class);
        assertNotNull(ic);
        
        JDBCDriver driver = (JDBCDriver)ic.instanceCreate();
        assertEquals("foo_driver", driver.getName());
        assertEquals("org.foo.FooDriver", driver.getClassName());
        assertEquals(2, driver.getURLs().length);
        assertEquals(new URL("file:///foo1.jar"), driver.getURLs()[0]);
        assertEquals(new URL("file:///foo2.jar"), driver.getURLs()[1]);
        
        // DTD version 1.1
        fo = createDriverFile11("org_foo_FooDriver_11.xml", Util.getDriversFolder());
        dobj = DataObject.find(fo);
        ic = (InstanceCookie)dobj.getCookie(InstanceCookie.class);
        assertNotNull(ic);
        
        driver = (JDBCDriver)ic.instanceCreate();
        assertEquals("foo_driver", driver.getName());
        assertEquals("Foo Driver", driver.getDisplayName());
        assertEquals("org.foo.FooDriver", driver.getClassName());
        assertEquals(2, driver.getURLs().length);
        assertEquals(new URL("file:///foo1.jar"), driver.getURLs()[0]);
        assertEquals(new URL("file:///foo2.jar"), driver.getURLs()[1]);
    }
    
    public void testWriteXml() throws Exception {
        JDBCDriver driver = JDBCDriver.create("bar_driver", "Bar Driver", "org.bar.BarDriver", new URL[] { new URL("file:///bar1.jar"), new URL("file:///bar2.jar") });
        JDBCDriverConvertor.create(driver);
        
        FileObject fo = Util.getDriversFolder().getFileObject("org_bar_BarDriver.xml");
        
        ErrorHandlerImpl errHandler = new ErrorHandlerImpl();
        Document doc = null;
        InputStream input = fo.getInputStream();
        try {
            doc = XMLUtil.parse(new InputSource(input), true, true, errHandler, EntityCatalog.getDefault());
        } finally {
            input.close();
        }
        
        assertFalse("JDBCDriverConvertor generates invalid XML acc to the DTD!", errHandler.error);
        
        Document goldenDoc = null;
        input = getClass().getResourceAsStream("bar-driver.xml");
        try {
            goldenDoc = XMLUtil.parse(new InputSource(input), true, true, null, EntityCatalog.getDefault());
        } finally {
            input.close();
        }
        
        assertTrue(DOMCompare.compareDocuments(doc, goldenDoc));
    }
    
    /**
     * Tests that the instance retrieved from the DO created by JDBCDC.create(JDBCD driver) is the same object as driver.
     * Note that this test does not ensure that the DataObject-s returned from 
     * {@link JDBCDriverConvertor#create} cannot be GCd. They can be, which will
     * cause new JDBCDriver instances to be created. See issue 75204.
     */
    public void testSameInstanceAfterCreate() throws Exception {
        JDBCDriver driver1 = JDBCDriver.create("bar_driver", "Bar Driver", "org.bar.BarDriver", new URL[] { new URL("file:///bar1.jar"), new URL("file:///bar2.jar") });
        DataObject dobj1 = JDBCDriverConvertor.create(driver1);
        JDBCDriver driver2 = JDBCDriver.create("foo_driver", "Foo Driver", "org.foo.FooDriver", new URL[] { new URL("file:///foo1.jar"), new URL("file:///foo2.jar") });
        DataObject dobj2 = JDBCDriverConvertor.create(driver2);
        assertSame(driver1, ((InstanceCookie)dobj1.getCookie(InstanceCookie.class)).instanceCreate());
        assertSame(driver2, ((InstanceCookie)dobj2.getCookie(InstanceCookie.class)).instanceCreate());
    }
    
    public void testLookup() throws Exception {
        FileObject parent = Util.getDriversFolder();
        createDriverFile11("org_foo_FooDriver.xml", parent);
        Lookup.Result result = Lookups.forPath(parent.getPath()).lookup(new Lookup.Template(JDBCDriver.class));
        Collection instances = result.allInstances();
        assertEquals(1, instances.size()); 
    }
    
    public void testEncodeURL() throws Exception {
        assertEquals(new URL("file:///test%20file#fragment"), JDBCDriverConvertor.encodeURL(new URL("file:///test file#fragment")));
        assertEquals(new URL("file:///test%20file"), JDBCDriverConvertor.encodeURL(new URL("file:///test file")));
    }
    
    public void testImportOldDrivers() throws Exception {
        final String UNENCODED_URL = "file:///foo 1.jar";
        
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        FileObject oldRoot = sfs.findResource(JDBCDriverConvertor.OLD_DRIVERS_PATH);
        if (oldRoot == null) {
            oldRoot = FileUtil.createFolder(sfs.getRoot(), JDBCDriverConvertor.OLD_DRIVERS_PATH);
        }
        URL[] urls = new URL[] { new URL(UNENCODED_URL) };
        createDriverFile10("testdriver.xml", oldRoot, urls);
        
        JDBCDriverConvertor.importOldDrivers();
        
        assertEquals(0, oldRoot.getChildren().length);
        
        FileObject newRoot = Util.getDriversFolder();
        assertEquals(1, newRoot.getChildren().length);
        
        Lookup.Result result = Lookups.forPath(newRoot.getPath()).lookup(new Lookup.Template(JDBCDriver.class));
        Collection instances = result.allInstances();
        JDBCDriver drv = (JDBCDriver)instances.iterator().next();
        assertEquals(JDBCDriverConvertor.encodeURL(new URL(UNENCODED_URL)), drv.getURLs()[0]);
        // assert the imported driver has a display name set
        assertEquals(drv.getName(), drv.getDisplayName());
    }
    
    private static FileObject createDriverFile10(String fileName, FileObject folder) throws Exception {
        URL[] urls = new URL[] {
            new URL("file:///foo1.jar"),
            new URL("file:///foo2.jar"),
        };
        return createDriverFile10(fileName, folder, urls);
    }
    
    private static FileObject createDriverFile10(String fileName, FileObject folder, URL[] urls) throws Exception {
        return createDriverFile(10, fileName, folder, urls);
    }
    
    private static FileObject createDriverFile11(String fileName, FileObject folder) throws Exception {
        URL[] urls = new URL[] {
            new URL("file:///foo1.jar"),
            new URL("file:///foo2.jar"),
        };
        return createDriverFile(11, fileName, folder, urls);
    }
    
    private static FileObject createDriverFile(int version, String fileName, FileObject folder, URL[] urls) throws Exception {
        String publicIdVer = version == 10 ? "1.0" : "1.1";
        String systemIdVer = version == 10 ? "1_0" : "1_1";
        
        FileObject fo = folder.createData(fileName);
        FileLock lock = fo.lock();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8");
            try {
                writer.write("<?xml version='1.0' encoding='UTF-8'?>");
                writer.write("<!DOCTYPE driver PUBLIC '-//NetBeans//DTD JDBC Driver " + publicIdVer + "//EN' 'http://www.netbeans.org/dtds/jdbc-driver-" + systemIdVer + ".dtd'>");
                writer.write("<driver>");
                writer.write("<name value='foo_driver'/>");
                if (version == 11) {
                    writer.write("<display-name value='Foo Driver'/>");
                }
                writer.write("<class value='org.foo.FooDriver'/>");
                writer.write("<urls>");
                for (int i = 0; i < urls.length; i++) {
                    writer.write("<url value='" + urls[i].toExternalForm() + "'/>");
                }
                writer.write("</urls>");
                writer.write("</driver>");
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }           
        return fo;
    }
    
    private static final class ErrorHandlerImpl implements ErrorHandler {
        
        public boolean error = false;
        
        public void warning(SAXParseException exception) throws SAXException {
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            error = true;
        }

        public void error(SAXParseException exception) throws SAXException {
            error = true;
        }
    }
}
