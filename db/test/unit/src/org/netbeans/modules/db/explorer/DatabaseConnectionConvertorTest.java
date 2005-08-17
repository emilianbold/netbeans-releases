/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import org.netbeans.modules.db.explorer.nodes.RootNode;
import org.netbeans.modules.db.test.DOMCompare;
import org.netbeans.modules.db.test.TestBase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
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
public class DatabaseConnectionConvertorTest extends TestBase {
    
    private static final String CONNECTION_FILE = "connection.xml";
    
    public DatabaseConnectionConvertorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        FileObject[] fos = getConnectionsFolder().getChildren();
        for (int i = 0; i < fos.length; i++) {
            fos[i].delete();
        }
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testReadXml() throws Exception {
        FileObject fo = createConnectionFile(CONNECTION_FILE, getConnectionsFolder());
        DataObject dobj = DataObject.find(fo);
        InstanceCookie ic = (InstanceCookie)dobj.getCookie(InstanceCookie.class);
        assertNotNull(ic);
        
        DatabaseConnection conn = (DatabaseConnection)ic.instanceCreate();
        assertEquals("org.foo.FooDriver", conn.getDriver());
        assertEquals("jdbc:foo:localhost", conn.getDatabase());
        assertEquals("schema", conn.getSchema());
        assertEquals("user", conn.getUser());
    }
    
    public void testWriteXml() throws Exception {
        DatabaseConnection conn = new DatabaseConnection("org.bar.BarDriver", "jdbc:bar:localhost", "schema", "user", null);
        DatabaseConnectionConvertor.create(conn);
        
        FileObject fo = getConnectionsFolder().getChildren()[0];
        
        ErrorHandlerImpl errHandler = new ErrorHandlerImpl();
        Document doc = null;
        InputStream input = fo.getInputStream();
        try {
            doc = XMLUtil.parse(new InputSource(input), true, true, errHandler, EntityCatalog.getDefault());
        } finally {
            input.close();
        }
        
        assertFalse("DatabaseConnectionConvertor generates invalid XML acc to the DTD!", errHandler.error);
        
        Document goldenDoc = null;
        input = getClass().getResourceAsStream("bar-connection.xml");
        try {
            goldenDoc = XMLUtil.parse(new InputSource(input), true, true, null, EntityCatalog.getDefault());
        } finally {
            input.close();
        }
        
        assertTrue(DOMCompare.compareDocuments(doc, goldenDoc));
    }
    
    /**
     * Tests that the instance retrieved from the DO created by DCC.create(DCI dbconn) is the same object as dbconn.
     */
    public void testSameInstanceAfterCreate() throws Exception {
        DatabaseConnection dbconn = new DatabaseConnection("org.bar.BarDriver", "jdbc:bar:localhost", "schema", "user", null);
        DataObject dobj = DatabaseConnectionConvertor.create(dbconn);
        assertSame(dbconn, ((InstanceCookie)dobj.getCookie(InstanceCookie.class)).instanceCreate());
    }
    
    public void testSaveOnPropertyChange() throws Exception {
        DatabaseConnection dbconn = new DatabaseConnection("a", "b", "c", "d", null);
        DatabaseConnectionConvertor.create(dbconn);
        
        dbconn.setDriver("org.bar.BarDriver");
        dbconn.setDatabase("jdbc:bar:localhost");
        dbconn.setSchema("schema");
        dbconn.setUser("user");
        
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) { }
        
        FileObject fo = getConnectionsFolder().getChildren()[0];
        
        ErrorHandlerImpl errHandler = new ErrorHandlerImpl();
        Document doc = null;
        InputStream input = fo.getInputStream();
        try {
            doc = XMLUtil.parse(new InputSource(input), true, true, errHandler, EntityCatalog.getDefault());
        } finally {
            input.close();
        }
        
        Document goldenDoc = null;
        input = getClass().getResourceAsStream("bar-connection.xml");
        try {
            goldenDoc = XMLUtil.parse(new InputSource(input), true, true, null, EntityCatalog.getDefault());
        } finally {
            input.close();
        }
        
        assertTrue(DOMCompare.compareDocuments(doc, goldenDoc));
    }
    
    public void testLookup() throws Exception {
        FileObject parent = getConnectionsFolder();
        createConnectionFile(CONNECTION_FILE, parent);
        FolderLookup lookup = new FolderLookup(DataFolder.findFolder(parent));
        Lookup.Result result = lookup.getLookup().lookup(new Lookup.Template(DatabaseConnection.class));
        Collection instances = result.allInstances();
        assertEquals(1, instances.size()); 
    }
    
    public void testImportOldConnections() throws Exception {
        DatabaseConnection conn = new DatabaseConnection("org.foo.FooDriver", "jdbc:foo:localhost", "schema", "user", null);
        RootNode.getOption().getConnections().add(conn);
        
        DatabaseConnectionConvertor.importOldConnections();
        
        FileObject root = getConnectionsFolder();
        Collection instances = new FolderLookup(DataFolder.findFolder(root)).getLookup().lookup(new Lookup.Template(DatabaseConnection.class)).allInstances();
        assertEquals(1, instances.size());
        
        DatabaseConnection importedConn = (DatabaseConnection)instances.iterator().next();
        assertEquals(conn.getDriver(), importedConn.getDriver());
        assertEquals(conn.getDatabase(), importedConn.getDatabase());
        assertEquals(conn.getSchema(), importedConn.getSchema());
        assertEquals(conn.getUser(), importedConn.getUser());
    }
    
    private FileObject getConnectionsFolder() {
        return Repository.getDefault().getDefaultFileSystem().findResource(DatabaseConnectionConvertor.CONNECTIONS_PATH);
    }
    
    private static FileObject createConnectionFile(String name, FileObject folder) throws Exception {
        FileObject fo = folder.createData(name);
        FileLock lock = fo.lock();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8");
            try {
                writer.write("<?xml version='1.0' encoding='UTF-8'?>");
                writer.write("<!DOCTYPE connection PUBLIC '-//NetBeans//DTD Database Connection 1.0//EN' 'http://www.netbeans.org/dtds/connection-1_0.dtd'>");
                writer.write("<connection>");
                writer.write("<driver-class value='org.foo.FooDriver'/>");
                writer.write("<connection-url value='jdbc:foo:localhost'/>");
                writer.write("<schema value='schema'/>");
                writer.write("<user value='user'/>");
                writer.write("</connection>");
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
