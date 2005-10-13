/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.driver;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.db.test.DOMCompare;
import org.netbeans.modules.db.test.TestBase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
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
    
    private static final String DRIVER_FILE = "org_foo_FooDriver.xml";
    
    public JDBCDriverConvertorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        FileObject[] fos = getDriversFolder().getChildren();
        for (int i = 0; i < fos.length; i++) {
            fos[i].delete();
        }
    }
    
    public void testReadXml() throws Exception {
        FileObject fo = createDriverFile(DRIVER_FILE, getDriversFolder());
        DataObject dobj = DataObject.find(fo);
        InstanceCookie ic = (InstanceCookie)dobj.getCookie(InstanceCookie.class);
        assertNotNull(ic);
        
        JDBCDriver driver = (JDBCDriver)ic.instanceCreate();
        assertEquals("Foo Driver", driver.getName());
        assertEquals("org.foo.FooDriver", driver.getClassName());
        assertEquals(2, driver.getURLs().length);
        assertEquals(new URL("file:///foo1.jar"), driver.getURLs()[0]);
        assertEquals(new URL("file:///foo2.jar"), driver.getURLs()[1]);
    }
    
    public void testWriteXml() throws Exception {
        JDBCDriver driver = JDBCDriver.create("Bar Driver", "org.bar.BarDriver", new URL[] { new URL("file:///bar1.jar"), new URL("file:///bar2.jar") });
        JDBCDriverConvertor.create(driver);
        
        FileObject fo = getDriversFolder().getFileObject("org_bar_BarDriver.xml");
        
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
    
    public void testLookup() throws Exception {
        FileObject parent = getDriversFolder();
        createDriverFile(DRIVER_FILE, parent);
        FolderLookup lookup = new FolderLookup(DataFolder.findFolder(parent));
        Lookup.Result result = lookup.getLookup().lookup(new Lookup.Template(JDBCDriver.class));
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
        createDriverFile("testdriver.xml", oldRoot, urls);
        
        JDBCDriverConvertor.importOldDrivers();
        
        assertEquals(0, oldRoot.getChildren().length);
        
        FileObject newRoot = getDriversFolder();
        assertEquals(1, newRoot.getChildren().length);
        
        FolderLookup lookup = new FolderLookup(DataFolder.findFolder(newRoot));
        Lookup.Result result = lookup.getLookup().lookup(new Lookup.Template(JDBCDriver.class));
        Collection instances = result.allInstances();
        JDBCDriver drv = (JDBCDriver)instances.iterator().next();
        System.out.println(JDBCDriverConvertor.encodeURL(new URL(UNENCODED_URL)));
        assertEquals(JDBCDriverConvertor.encodeURL(new URL(UNENCODED_URL)), drv.getURLs()[0]);
    }
    
    private FileObject getDriversFolder() {
        return Repository.getDefault().getDefaultFileSystem().findResource(JDBCDriverConvertor.DRIVERS_PATH);
    }
    
    private static FileObject createDriverFile(String name, FileObject folder) throws Exception {
        URL[] urls = new URL[] {
            new URL("file:///foo1.jar"),
            new URL("file:///foo2.jar"),
        };
        return createDriverFile(name, folder, urls);
    }
    
    private static FileObject createDriverFile(String name, FileObject folder, URL[] urls) throws Exception {
        FileObject fo = folder.createData(name);
        FileLock lock = fo.lock();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8");
            try {
                writer.write("<?xml version='1.0' encoding='UTF-8'?>");
                writer.write("<!DOCTYPE driver PUBLIC '-//NetBeans//DTD JDBC Driver 1.0//EN' 'http://www.netbeans.org/dtds/jdbc-driver-1_0.dtd'>");
                writer.write("<driver>");
                writer.write("<name value='Foo Driver'/>");
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
