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

package org.netbeans.spi.java.project.support.ui;
import java.io.File;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Jan Becicka
 */
public class PackageRenameHandlerTest extends NbTestCase {
    
    static {
        System.setProperty ("org.openide.util.Lookup", Lkp.class.getName()); // NOI18N
    }
    
    private FileObject fo;
    private Node n;
    private PackageRenameHandlerImpl frh = new PackageRenameHandlerImpl();
    
    
    public PackageRenameHandlerTest (String testName) {
        super (testName);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File d = getWorkDir();
        assertTrue("root dir " + d + " exists", d.isDirectory());
        FileObject root = FileUtil.toFileObject(d);
        assertNotNull("file object for " + d + " exists", root);
        fo = FileUtil.createFolder (root, "test");// NOI18N

        SourceGroup group = GenericSources.group(null, root.createFolder("src"), "testGroup", "Test Group", null, null);
        Children ch = PackageView.createPackageView( group ).getChildren();
        
        // Create folder
	FileUtil.createFolder( root, "src/foo" );
        n = ch.findChild( "foo" );           
        
        assertNotNull(n);
    }
    
    public void testRenameHandlerNotCalled () throws Exception {
        ((Lkp) Lkp.getDefault()).register(new Object[]{});
        frh.called = false;
        
        n.setName("blabla");
        assertFalse(frh.called);
    }
    
    public void testRenameHandlerCalled () throws Exception {
        ((Lkp) Lkp.getDefault()).register(new Object[]{frh});
        frh.called = false;
        
        n.setName("foo");// NOI18N
        assertTrue(frh.called);
    }
    
    public static class Lkp extends ProxyLookup {
        public Lkp() {
            super(new Lookup[0]);
            register(new Object[0]);
        }
        public void register(Object[] instances) {
            setLookups(new Lookup[] {
                Lookups.fixed(instances),
                Lookups.metaInfServices(Lkp.class.getClassLoader()),
                Lookups.singleton(Lkp.class.getClassLoader()),
            });
        }
    }    
    
    private static final class PackageRenameHandlerImpl implements PackageRenameHandler {
        boolean called  = false;
        public void handleRename(Node n, String newName) throws IllegalArgumentException {
            called = true;
        }
    }
    
}
