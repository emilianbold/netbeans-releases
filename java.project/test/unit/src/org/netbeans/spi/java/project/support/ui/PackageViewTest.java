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

import junit.framework.*;
import org.netbeans.junit.NbTestCase;

import org.netbeans.api.project.TestUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.nodes.Children;
import org.openide.nodes.Node;


/**
 * Factory for package views.
 * @see org.netbeans.spi.project.ui.LogicalViewProvider
 * @author Jesse Glick
 */
public class PackageViewTest extends NbTestCase {
    
    public PackageViewTest( String name ) {
        super( name );
    }
    
    
    public void testFolders() throws Exception {
        
        // Prepare test data
        FileObject root = TestUtil.makeScratchDir( this );
        // System.out.println("root " + root.getFileSystem().getClass() );
        
        
	// Create children
        FileUtil.createFolder( root, "src" );
        Children ch = PackageView.createPackageView( root.getFileObject( "src" ) );
        
        
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
                     
    }
    
        
    public static void assertNodes( Children children, String[] nodeNames ) {
        assertNodes( children, nodeNames, null );
    }
    
    public static void assertNodes( Children children, String[] nodeNames, int[] childCount ) {
        Node[] nodes = children.getNodes();
        assertEquals( "Wrong number of nodes.", nodeNames.length, nodes.length );
        
        for( int i = 0; i < nodeNames.length; i++ ) {
            assertEquals( "Wrong node name on index " + i +".", nodeNames[i], nodes[i].getDisplayName() );
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
    
}
