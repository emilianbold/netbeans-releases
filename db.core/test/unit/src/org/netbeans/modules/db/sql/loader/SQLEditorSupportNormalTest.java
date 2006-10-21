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

package org.netbeans.modules.db.sql.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import javax.swing.text.Document;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.windows.CloneableTopComponent;

/**
 * Tests the functionality of SQLEditorSupport when serving for a normal DataObject.
 *
 * @author Andrei Badea
 */
public class SQLEditorSupportNormalTest extends NbTestCase {
    
    private FileObject fileObject;
    private DataObject dataObject;
    private MySQLEditorSupport support;
    
    public SQLEditorSupportNormalTest(String testName) {
        super(testName);
    }
    
    public void setUp() throws Exception {
        FileObject folder = FileUtil.toFileObject(getWorkDir()).createFolder("folder");
        fileObject = folder.createData("SQLFile", "sql");
        
        MockServices.setServices(Pool.class);
        assertEquals(Pool.class, Lookup.getDefault().lookup(DataLoaderPool.class).getClass());
        
        dataObject = DataObject.find(fileObject);
        support = (MySQLEditorSupport)dataObject.getCookie(OpenCookie.class);
    }
    
    public void tearDown() throws Exception {
        fileObject.getParent().delete();
    }
    
    public boolean runInEQ() {
        return true;
    }
    
    public void testEditorNameIsNodeDisplayName() throws Exception {
        support.open();
        assertEquals(dataObject.getNodeDelegate().getDisplayName(), support.messageName());
        
        Document doc = support.openDocument();
        doc.insertString(0, "test", null);
        
        assertTrue(support.messageName().indexOf(dataObject.getNodeDelegate().getDisplayName()) >= 0);
        support.close();
    }
    
    public void testDataObjectIsModifiedWhenDocumentChanged() throws Exception {
        support.open();
        
        Document doc = support.openDocument();
        doc.insertString(0, "test", null);
        
        assertTrue(support.isModified());
        assertTrue(dataObject.isModified());
        assertNotNull(dataObject.getCookie(SaveCookie.class));
        // XXX should test canClose() here, but how, since canClose() displays a dialog box?
        
        support.close();
    }
    
    public void testDocumentIsNotSavedOnComponentDeactivatedOrWriteExternal() throws Exception {
        support.open();
        Document doc = support.openDocument();
        doc.insertString(0, "test", null);

        MySQLEditorSupport.MySQLCloneableEditor editor = (MySQLEditorSupport.MySQLCloneableEditor)support.getAllEditors().getComponents().nextElement();
        editor.callComponentDeactivated();
        assertFalse(support.saveDocumentCalled);

        doc.insertString(0, "test", null);
        editor.writeExternal(new ObjectOutputStream(new ByteArrayOutputStream()));
        assertFalse(support.saveDocumentCalled);
        
        support.close();
    }
    
    /**
     * DataLoaderPool which is registered in the default lookup and loads
     * MySQLDataLoader.
     */
    public static final class Pool extends DataLoaderPool {
        
        public Enumeration loaders() {
            return Enumerations.singleton(new MySQLDataLoader());
        }
    }
    
    /**
     * DataLoader for SQL files. Not using SQLDataLoader because we want
     * the loader to return our special MySQLDataObject's.
     */
    private static final class MySQLDataLoader extends UniFileLoader {
    
        public MySQLDataLoader() {
            super("org.netbeans.modules.db.sql.loader.MySQLDataLoader");
        }
    
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MySQLDataObject(primaryFile, this);
        }

        protected void initialize() {
            super.initialize();
            ExtensionList extensions = new ExtensionList();
            extensions.addExtension("sql");
            setExtensions(extensions);
        }
    }
    
    /**
     * SQLDataObject which has MySQLEditorSupport in its cookie set instead
     * of the cookie added by SQLDataObject.
     */
    private static final class MySQLDataObject extends SQLDataObject {
        
        public MySQLDataObject(FileObject primaryFile, UniFileLoader loader) throws DataObjectExistsException {
            super(primaryFile, loader);
            CookieSet cookies = getCookieSet();
            cookies.remove(cookies.getCookie(OpenCookie.class));
            cookies.add(new MySQLEditorSupport(this));
        }

        protected Node createNodeDelegate() {
            return new SQLNode(this);
        }
    }
    
    /**
     * SQLEditorSupport which allows finding out whether the saveDocument() method was called
     * and calling the componentDeactivated() method.
     */
    private static final class MySQLEditorSupport extends SQLEditorSupport {
        
        boolean saveDocumentCalled = false;
        
        public MySQLEditorSupport(SQLDataObject obj) {
            super(obj);
        }
        
        /**
         * In order to silently close a modified support.
         */
        public boolean canClose() {
            return true;
        }
        
        public void saveDocument() throws IOException {
            super.saveDocument();
            saveDocumentCalled = true;
        }
        
        public CloneableTopComponent.Ref getAllEditors() {
            return allEditors;
        }
        
        public CloneableEditor createCloneableEditor() {
            return new MySQLCloneableEditor(this);
        }
        
        private static final class MySQLCloneableEditor extends SQLCloneableEditor {
            
            public MySQLCloneableEditor(MySQLEditorSupport support) {
                super(support);
            }
            
            public void callComponentDeactivated() {
                componentDeactivated();
            }
        }
    }
}
