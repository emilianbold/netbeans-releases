/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import java.io.IOException;
import java.util.Set;
import junit.textui.TestRunner;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import java.util.Enumeration;
import org.openide.nodes.Node;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.Repository;
import org.netbeans.junit.*;

/** Test things about node delegates.
 * @author Jesse Glick
 */
public class DataNodeTest extends NbTestCase {
    
    public DataNodeTest(String name) {
        super(name);
    }

    static {
        System.setProperty ("org.openide.util.Lookup", "org.openide.loaders.DataNodeTest$Lkp");
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(DataNodeTest.class));
        // Kill process since currently this pops up GUI windows or something:
        System.exit(0);
    }
    
    /*
    protected void setUp() throws Exception {
    }
    protected void tearDown() throws Exception {
    }
     */
    
    /** Test that for all examples to be found in the system file system,
     * the node delegate has the same object as a cookie from DataObject.class.
     * (It is fine to have a different object from a more specific cookie, as
     * may happen in the case of a data shadow.)
     * See jglick's message on nbdev as of 22 Jun 2001:
     * assertTrue(dataObject.getNodeDelegate().getCookie(DataObject.class)==dataObject)
     */
    public void testDataNodeHasObjectAsCookie() throws Exception {
        // First make sure some core is installed. This could be run inside or
        // outside a running IDE.
//        TopManager tm = TopManager.getDefault();
        // Now scan SFS for all DO's and check the assertion.
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        DataFolder top = DataFolder.findFolder(sfs.getRoot());
        Enumeration e = top.children(true);
        while (e.hasMoreElements()) {
            DataObject o = (DataObject)e.nextElement();
            Node n = o.getNodeDelegate();
            DataObject o2 = (DataObject)n.getCookie(DataObject.class);
            assertEquals("Correct cookie from node delegate", o, o2);
        }
    }

    
    public void testDataNodeGetHtmlNameDoesNotInitializeAllFiles () throws Exception {
        org.openide.filesystems.FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), new String[] {
            "F.java", "F.form"
        });
        
        FSWithStatus fs = new FSWithStatus ();
        fs.setRootDirectory(org.openide.filesystems.FileUtil.toFile(lfs.getRoot()));
        
        DataObject obj = DataObject.find (fs.findResource("F.java"));
        
        String n = obj.getNodeDelegate ().getHtmlDisplayName ();
        assertNotNull ("FSWithStatus called", fs.lastFiles);
     
        assertEquals ("Primary entry created", 1, TwoPartLoader.get ().primary);
        if (TwoPartLoader.get ().secondary != 0) {
            try {
                assertEquals ("Secondary entry not", 0, TwoPartLoader.get ().secondary);
            } catch (Error t1) {
                Throwable t2 = TwoPartLoader.get ().whoCreatedSecondary;
                if (t2 != null) {
                    t1.initCause (t2);
                }
                throw t1;
            }
        }
        assertEquals ("Size is two", 2, fs.lastFiles.size ());
        assertEquals ("Now the secondary entry had to be created", 1, TwoPartLoader.get ().secondary);
    }
    
    
    private static final class FSWithStatus extends org.openide.filesystems.LocalFileSystem 
    implements FileSystem.HtmlStatus {
        public Set lastFiles;
        
        
        public FileSystem.Status getStatus () {
            return this;
        }

        private void checkFirst (Set files) {
            lastFiles = files;
            assertNotNull ("There is first file", files.iterator ().next ());
        }
        
        public java.awt.Image annotateIcon(java.awt.Image icon, int iconType, java.util.Set files) {
            checkFirst (files);
            return icon;
        }

        public String annotateName(String name, java.util.Set files) {
            checkFirst (files);
            return name;
        }

        public String annotateNameHtml(String name, java.util.Set files) {
            checkFirst (files);
            return name;
        }
    } // end of FSWithStatus
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup  {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            
            ic.add (new Pool ());
        }
        
    } // end of Lkp
    
    private static final class Pool extends DataLoaderPool {
        protected Enumeration loaders () {
            return org.openide.util.Enumerations.singleton(TwoPartLoader.get ());
        }
    }
    
    public static final class TwoPartLoader extends MultiFileLoader {
        public int primary;
        public int secondary;
        public Throwable whoCreatedSecondary;
        
        public static TwoPartLoader get () {
            return (TwoPartLoader)TwoPartLoader.findObject (TwoPartLoader.class, true);
        }
        
        public TwoPartLoader() {
            super(TwoPartObject.class.getName ());
        }
        protected String displayName() {
            return "TwoPart";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            return org.openide.filesystems.FileUtil.findBrother(fo, "java");
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new TwoPartObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            primary++;
            return new FileEntry.Folder(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            secondary++;
            whoCreatedSecondary = new Throwable ("Secondary should not be created");
            return new FileEntry(obj, secondaryFile);
        }
    }
    public static final class TwoPartObject extends MultiDataObject {
        public TwoPartObject(TwoPartLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
        }
    }
    
}
