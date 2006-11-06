/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Enumerations;
import org.openide.util.lookup.Lookups;

/** Test things about shadows and broken shadows, etc.
 * @author Jaroslav Tulach
 */
public class DataShadowLookupTest extends NbTestCase
implements java.net.URLStreamHandlerFactory {
    /** original object */
    private DataObject original;
    /** folder to work with */
    private DataFolder folder;
    /** fs we work on */
    private FileSystem lfs;

    private Logger err;
    
    static {
        // to handle nbfs urls...
      //  java.net.URL.setURLStreamHandlerFactory (new DataShadowLookupTest(null));
        MockServices.setServices(new Class[] { Pool.class });
    }
    
    public DataShadowLookupTest (String name) {
        super(name);
    }

    protected Level logLevel() {
        return Level.INFO;
    }
    
    protected void setUp() throws Exception {
        
        lfs = Repository.getDefault ().getDefaultFileSystem ();
        
        FileObject[] delete = lfs.getRoot().getChildren();
        for (int i = 0; i < delete.length; i++) {
            delete[i].delete();
        }

        
        FileObject fo = FileUtil.createData (lfs.getRoot (), getName () + "/folder/original.string");
        assertNotNull(fo);
        original = DataObject.find (fo);
        assertFalse ("Just to be sure that this is not shadow", original instanceof DataShadow);
        assertEquals ("It is the right class", StringObject.class, original.getClass ());
        fo = FileUtil.createFolder (lfs.getRoot (), getName () + "/modify");
        assertNotNull(fo);
        assertTrue (fo.isFolder ());
        folder = DataFolder.findFolder (fo);
        
        Repository.getDefault ().addFileSystem (lfs);
        
        err = Logger.getLogger(getName());
    }
    
    public java.net.URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals ("nbfs")) {
            return FileUtil.nbfsURLStreamHandler ();
        }
        return null;
    }
    
    public void testStringIsInLookupOfDataShadow() throws Exception {
        DataShadow shade = original.createShadow(folder);

        {
            String s = (String)original.getNodeDelegate().getLookup().lookup(String.class);
            assertNotNull("String is in the original's lookup", s);
        }
        
        assertSame(shade.getOriginal(), original);
        String s = (String)shade.getNodeDelegate().getLookup().lookup(String.class);
        assertNotNull("String is in the lookup", s);
        assertEquals("It is the name of the original", original.getName(), s);
    }

    public static final class Pool extends DataLoaderPool {
        protected Enumeration loaders() {
            return Enumerations.singleton(StringLoader.findObject(StringLoader.class, true));
        }
        
    }
    
    private static final class StringLoader extends UniFileLoader {
        public StringLoader() {
            super("org.openide.loaders.StringObject");
            getExtensions().addExtension("string");
        }
        
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new StringObject(this, primaryFile);
        }
        
    } // end of StringLoader
    
    private static final class StringObject extends MultiDataObject {
        public StringObject(StringLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
        }

        protected Node createNodeDelegate() {
            return new DataNode(this, Children.LEAF, Lookups.singleton(getName()));
        }
    } // end of StringObject
    
    
}
