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

package org.netbeans.spi.java.project.support.ui;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import javax.swing.Icon;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

public class PackageViewTest extends NbTestCase {
    
    static {
        System.setProperty("org.netbeans.spi.java.project.support.ui.packageView.TRUNCATE_PACKAGE_NAMES", "true");
    }
    
    public PackageViewTest( String name ) {
        super( name );
    }
    
    
    public void testFolders() throws Exception {
        
        // Prepare test data
        FileObject root = TestUtil.makeScratchDir( this );
        // System.out.println("root " + root.getFileSystem().getClass() );
        
        
	// Create children
        SourceGroup group = new SimpleSourceGroup( FileUtil.createFolder( root, "src" ) );
        Children ch = PackageView.createPackageView( group ).getChildren();
        
        
	FileUtil.createFolder( root, "src/a/b/c" );
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );
        
                     
                     
        FileUtil.createFolder( root, "src/e/f/g" );
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", },
                     new int[] { 0, 0 } );
        
        
        // Test package names
        FileUtil.createData( root, "src/e/f/g/Some.java" );                                     
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", },
                     new int[] { 0, 1 } );
                     
        // Add empty package
        FileUtil.createFolder( root, "src/x/y/z" );             
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.y.z" },
                     new int[] { 0, 1, 0 } );
                     
        // Add file to folder
        FileObject x_y_z_some = FileUtil.createData( root, "src/x/y/z/Some.java" );        
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.y.z" },
                     new int[] { 0, 1, 1 } );
                     
        // Remove file from folder
        x_y_z_some.delete();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.y.z" },
                     new int[] { 0, 1, 0 } );
                   
                     
        // Add file to super folder
        FileObject x_y_some = FileUtil.createData( root, "src/x/y/Some.java" );        
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.y", "x.y.z" },
                     new int[] { 0, 1, 1, 0 } );
                     
        // Remove file from superfolder
        x_y_some.delete();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.y.z" },
                     new int[] { 0, 1, 0 } );
        
        
        // Add subfolder    
        FileObject x_y_z_w = FileUtil.createFolder( root, "src/x/y/z/w" );
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.y.z.w" },
                     new int[] { 0, 1, 0 } );
                     
        // Remove subfolder
        x_y_z_w.delete();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.y.z" },
                     new int[] { 0, 1, 0 } );
                     
        // Remove super folder
        FileObject x_y = root.getFileObject( "src/x/y" );
        x_y.delete();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x" },
                     new int[] { 0, 1, 0 } );
        
        // Remove root folder
        FileUtil.createFolder( root, "src/x/v/w" );
                assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", "x.v.w" },
                     new int[] { 0, 1, 0 } );
        FileObject x = root.getFileObject( "src/x" );
        x.delete();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g" },
                     new int[] { 0, 1 } );
                     
        // Rename leaf folder
        FileObject e_f_g = root.getFileObject( "src/e/f/g" );
        FileLock lock = e_f_g.lock();
        e_f_g.rename( lock, "h", null );
        lock.releaseLock();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.h" },
                     new int[] { 0, 1 } );
        
                                          
        // Rename super folder
        FileUtil.createFolder( root, "src/e/f/g" );
        FileUtil.createFolder( root, "src/e/f/i" );
        FileObject e_f = root.getFileObject( "src/e/f" );
        lock = e_f.lock();
        e_f.rename( lock, "r", null );
        lock.releaseLock();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.r.g", "e.r.h", "e.r.i" },
                     new int[] { 0, 0, 1, 0 } );
                     
        // Rename rootfolder 
        FileObject e = root.getFileObject( "src/e/" );
        lock = e.lock();
        e.rename( lock, "t", null );
        lock.releaseLock();
        assertNodes( ch, 
                     new String[] { "a.b.c", "t.r.g", "t.r.h", "t.r.i" },
                     new int[] { 0, 0, 1, 0 } );
        
        // Test truncated package names
        FileUtil.createFolder(root, "src/org/foo/something/whatever");
        assertNodes( ch, 
                     new String[] { "a.b.c", "o.foo.som.whatever", "t.r.g", "t.r.h", "t.r.i" },
                     new int[] { 0, 0, 0, 1, 0 } );
                     
    }
    
    public void testDefaultPackage() throws Exception {
        
        // Prepare test data
        FileObject root = TestUtil.makeScratchDir( this );
        // System.out.println("root " + root.getFileSystem().getClass() );
        
        
	// Create children        
        SourceGroup group = new SimpleSourceGroup( FileUtil.createFolder( root, "src" ) );
        Children ch = PackageView.createPackageView( group ).getChildren();
        
        // Default package should be there
        assertNodes( ch, 
                     new String[] { "<default package>" },
                     new int[] { 0 } );
                     
        // Default package should disappear             
        FileObject a = FileUtil.createFolder( root, "src/a" );
        assertNodes( ch, 
                     new String[] { "a", },
                     new int[] { 0, } );
                     
        // Default package should appear again
        FileObject someJava = FileUtil.createData( root, "src/Some.java" );
        assertNodes( ch, 
                     new String[] { "<default package>", "a", },
                     new int[] { 1, 0, } );
                     
        // Disappear again             
        someJava.delete();
        assertNodes( ch, 
                     new String[] { "a", },
                     new int[] { 0, } );             
                     
        // And appear again
        a.delete();
        assertNodes( ch, 
                     new String[] { "<default package>" },
                     new int[] { 0 } );
        
    }
    
    public void testNodeDestroy() throws Exception {
        // Prepare test data
        FileObject root = TestUtil.makeScratchDir( this );
                
	FileObject srcRoot;
        FileObject toDelete;
        SourceGroup group;
        Node rootNode;
        Node n;
        
        // Empty parent
        srcRoot = FileUtil.createFolder( root, "ep" );
        toDelete = FileUtil.createFolder( srcRoot, "a/aa" );
        group = new SimpleSourceGroup( srcRoot );
        rootNode = PackageView.createPackageView( group );

        n = PackageView.findPath( rootNode, toDelete );
        n.destroy();        
        assertFileObjects( srcRoot, new String[0] );
        
        // Non-Empty parent
        srcRoot = FileUtil.createFolder( root, "nep" );
        toDelete = FileUtil.createFolder( srcRoot, "a/aa" );
        FileUtil.createData( srcRoot, "a/some.java" );
        group = new SimpleSourceGroup( srcRoot );
        rootNode = PackageView.createPackageView( group );

        n = PackageView.findPath( rootNode, toDelete );
        n.destroy();        
        assertFileObjects( srcRoot, new String[]{ "a" } );
        
               
        // Non empty siblings
        srcRoot = FileUtil.createFolder( root, "es" );
        FileObject a = FileUtil.createFolder( srcRoot, "a" );
        FileUtil.createFolder( a, "aa" );
        FileUtil.createData( srcRoot, "a/aa/some.java" );
        toDelete = FileUtil.createFolder( srcRoot, "a/b" );
        group = new SimpleSourceGroup( srcRoot );
        rootNode = PackageView.createPackageView( group );
        
        n = PackageView.findPath( rootNode, toDelete );
        n.destroy();        
        assertFileObjects( srcRoot, new String[]{ "a" } );
        assertFileObjects( a, new String[]{ "aa" } );
        
        // Empty siblings
        srcRoot = FileUtil.createFolder( root, "nes" );
        a = FileUtil.createFolder( srcRoot, "a" );
        FileUtil.createFolder( a, "aa" );
        toDelete = FileUtil.createFolder( srcRoot, "a/b" );
        group = new SimpleSourceGroup( srcRoot );
        rootNode = PackageView.createPackageView( group );
        
        n = PackageView.findPath( rootNode, toDelete );
        n.destroy();        
        assertFileObjects( srcRoot, new String[]{ "a" } );
        assertFileObjects( a, new String[]{ "aa" } );
        
                
    }
    
    
    public void testFindPath() throws Exception {
        
        // Prepare test data
        FileObject root = TestUtil.makeScratchDir( this );
        // System.out.println("root " + root.getFileSystem().getClass() );
        
        
	// Create children        
        SourceGroup group = new SimpleSourceGroup( FileUtil.createFolder( root, "src" ) );
        Node sourceRoot = PackageView.createPackageView( group );
        Children ch = sourceRoot.getChildren();
        
        FileObject a_b_c = FileUtil.createFolder( root, "src/a/b/c" );
        FileObject a_b = root.getFileObject( "src/a/b" );
        FileObject e_f_g = FileUtil.createFolder( root, "src/e/f/g" );
        
        FileObject dp_java = FileUtil.createData( root, "src/DP" );
        FileObject a_b_c_java = FileUtil.createData( root, "src/a/b/c/ABC" );
        FileObject a_b_java = FileUtil.createData( root, "src/a/b/AB" );
        FileObject e_f_g_java = FileUtil.createData( root, "src/e/f/g/EFG.someext" );
        
        // Try to find standard files
        Node n;
        n = PackageView.findPath( sourceRoot, a_b_c_java );
        assertNode( n, "ABC" );
        // Check also DataObject:
        n = PackageView.findPath(sourceRoot, DataObject.find(a_b_c_java));
        assertNode(n, "ABC");
                
        n = PackageView.findPath( sourceRoot, a_b_java );
        assertNode( n, "AB" );
        
        n = PackageView.findPath( sourceRoot, e_f_g_java );
        assertNode( n, "EFG.someext" );
        
        // Try to find folders
        n = PackageView.findPath( sourceRoot, a_b_c );
        assertNode( n, "a.b.c" );
        
        n = PackageView.findPath( sourceRoot, a_b );
        assertNode( n, "a.b" );
        
        n = PackageView.findPath( sourceRoot, e_f_g );
        assertNode( n, "e.f.g" );
        
        // Try file in default package
        n = PackageView.findPath( sourceRoot, dp_java );
        assertNode( n, "DP" );
        
        n = PackageView.findPath( sourceRoot, group.getRootFolder() );
        assertNode( n, "" );
                
        dp_java.delete(); // Dp will disapear should return root node
        n = PackageView.findPath( sourceRoot, group.getRootFolder() );
        assertNode( n, group.getName() );
        
    }
    
        
    public static void assertNodes( Children children, String[] nodeNames ) {
        assertNodes( children, nodeNames, null );
    }
    
    public static void assertNodes( Children children, String[] nodeNames, int[] childCount ) {
        Node[] nodes = children.getNodes();
        assertEquals( "Wrong number of nodes.", nodeNames.length, nodes.length );
        
        for( int i = 0; i < nodeNames.length; i++ ) {
            assertEquals( "Wrong node name on index " + i + ": " + nodes[i].getDisplayName(), nodeNames[i], nodes[i].getDisplayName() );
            if ( childCount != null ) {
                if ( childCount[i] == 0 ) {
                    assertEquals( "Node should be leaf", true, nodes[i].isLeaf() );
                }
                else {
                    assertEquals( "Node should not be leaf", false, nodes[i].isLeaf() );
                }
                
                assertEquals( "Wrong nuber of children. Node: " + nodeNames[i] +".", childCount[i], nodes[i].getChildren().getNodes( true ).length );
            }
        }
    }
    
    public static void assertNode( Node n, String name ) {
        
        if ( name != null ) {
            assertNotNull( "Node " + name +" not found", n  );
            assertEquals( "Wrong name", name, n.getName() );             
        }
        else {
            assertNull( "No node should be found", n );
        }
        
    }
    
    public static void assertFileObjects( FileObject folder, String[] names ) {
        
        assertTrue( "Has to be a folder ", folder.isFolder() );
        
        FileObject[] children = folder.getChildren();
        String[] chNames = new String[ children.length ];
        for( int i = 0; i < children.length; i++ ) {            
            chNames[i] = children[i].getNameExt();
        }
        
        Arrays.sort( names );
        Arrays.sort( chNames );
        
        assertTrue( "Arrays have to be equal ", Arrays.equals( names, chNames ) );
        
    }
    
    private static class SimpleSourceGroup implements SourceGroup {
        
        private FileObject root;
        
        public SimpleSourceGroup( FileObject root ) {
            this.root = root;
        }
        
        public FileObject getRootFolder() {
            return root;
        }
        
        public String getName() {
            return "TestGroup";
        }
        
        public String getDisplayName() {
            return getName();
        }
        
        public Icon getIcon(boolean opened) {
            return null;
        }

        public boolean contains(FileObject file) throws IllegalArgumentException {
            return FileUtil.isParentOf( root, file );
        }
    
        public void addPropertyChangeListener(PropertyChangeListener listener) {}

        public void removePropertyChangeListener(PropertyChangeListener listener) {}
        
    }
    
}
