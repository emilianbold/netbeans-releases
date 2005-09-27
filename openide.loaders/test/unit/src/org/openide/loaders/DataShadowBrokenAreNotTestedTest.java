/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/** Test that URL is not requested if there are no broken shadows.
 * @author Jaroslav Tulach
 */
public class DataShadowBrokenAreNotTestedTest extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.loaders.DataShadowBrokenAreNotTestedTest$Lkp");
		
		URLStreamHandlerFactory f = (URLStreamHandlerFactory)org.openide.util.Lookup.getDefault().lookup(URLStreamHandlerFactory.class);
		assertNotNull("Factory is in lookup", f);
		java.net.URL.setURLStreamHandlerFactory (f);
    }
    
    public DataShadowBrokenAreNotTestedTest (String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        
        FileSystem lfs = Repository.getDefault ().getDefaultFileSystem ();
        
        FileObject[] delete = lfs.getRoot().getChildren();
        for (int i = 0; i < delete.length; i++) {
            delete[i].delete();
        }
        
		UM.cnt = 0;
    }
	
	public void testNoURLMapperQueried() throws Exception {
        FileSystem lfs = Repository.getDefault ().getDefaultFileSystem ();
        FileObject fo = FileUtil.createData (lfs.getRoot (), getName () + "/folder/original.txt");
        assertNotNull(fo);
		
		assertEquals("No queries to UM yet", 0, UM.cnt);
        DataObject original = DataObject.find (fo);
		
		assertEquals("No queries to UM after creation of data object", 0, UM.cnt);
	}

	public void testQueriedWhenBrokenSharowsExists() throws Exception {
		
		//
		// Note: if anyone lowers the number of queries done here,
		// then go on, this test is here just to describe the current behaviour
		//
		
		
        FileSystem lfs = Repository.getDefault ().getDefaultFileSystem ();
        FileObject f1 = FileUtil.createData (lfs.getRoot (), getName () + "/folder/original.txt");
        assertNotNull(f1);
        FileObject f2 = FileUtil.createData (lfs.getRoot (), getName () + "/any/folder/original.txt");
        assertNotNull(f2);

		assertEquals("No queries to UM yet", 0, UM.cnt);
		DataObject original = DataObject.find (f1);
		assertEquals("No queries to UM still", 0, UM.cnt);
		DataShadow s = original.createShadow(original.getFolder());
		assertEquals("One query to create the shadow and one to create the instance", 2, UM.cnt);
		original.delete();
		assertEquals("One additional query to delete", 3, UM.cnt);
		DataObject brokenShadow = DataObject.find(s.getPrimaryFile());
		assertEquals("Creating one broken shadow", 5, UM.cnt);
		
        DataObject original2 = DataObject.find (f2);
		assertEquals("Additional query per very data object creation", 6, UM.cnt);
	}
	
	private static final class UM extends URLMapper {
		public static int cnt;
		
        public URL getURL(FileObject fo, int type) {
			cnt++;
			return null;
        }

        public FileObject[] getFileObjects(URL url) {
			cnt++;
			return null;
        }
	}

    public static final class Lkp extends org.openide.util.lookup.AbstractLookup 
	implements java.net.URLStreamHandlerFactory {
        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }
        
        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
			ic.add(new UM());
			ic.add(this);
        }
		public java.net.URLStreamHandler createURLStreamHandler(String protocol) {
			if (protocol.equals ("nbfs")) {
				return FileUtil.nbfsURLStreamHandler ();
			}
			return null;
		}
    }
	
}
