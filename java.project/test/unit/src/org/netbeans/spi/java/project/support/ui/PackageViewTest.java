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

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

public class PackageViewTest extends NbTestCase {
    
    static {
        System.setProperty("org.netbeans.spi.java.project.support.ui.packageView.TRUNCATE_PACKAGE_NAMES", "true");
    }
    
    public PackageViewTest( String name ) {
        super( name );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        // XXX causes problems: DataObjectNotFoundException when finding nodes
        // for folders which do not in fact exist on disk.
        // Maybe due to differences of behavior between LocalFileSystem and MasterFileSystem?
        /*
        TestUtil.setLookup(new Object[] {
            new VQImpl(),
        });
         */
        TestUtil.setLookup( Lookups.fixed( new Object[] { new VQImpl(), PackageViewTest.class.getClassLoader() } ) ); 
        clearWorkDir();
    }
    
    public void testFolders() throws Exception {
        
        // Prepare test data
        FileObject root = TestUtil.makeScratchDir( this );
        // System.out.println("root " + root.getFileSystem().getClass() );
        
        
        assertNull( "source folder should not exist yet", root.getFileObject( "src" ) );
        
        
	// Create children
        SourceGroup group = new SimpleSourceGroup( FileUtil.createFolder( root, "src" ) );
        Children ch = PackageView.createPackageView( group ).getChildren();
        
        
        // Create folder
	FileUtil.createFolder( root, "src/a/b/c" );
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, }, 
                     true ); // Needs to compute the nodes first
        
        // Testing files/folders in ignored folders
                     
        // Create ignored folder             
        FileUtil.createFolder( root, "src/KRTEK.folder" );
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );
        
        // Create file in ignored folder
        FileUtil.createData( root, "src/KRTEK.folder/nonignored.file" );
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );             
                     
        // Create folder in ignored folder             
        FileObject nonignoredFolder = FileUtil.createFolder( root, "src/KRTEK.folder/nonignored.folder" );
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );
                     
        // Create file in NONignored folder which is under ignored folder            
        FileObject nonignoredFile = FileUtil.createData( root, "src/KRTEK.folder/nonignored.folder/nonignored.file" );
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );
             
        // Rename the file             
        FileLock nfLock = nonignoredFile.lock();
        nonignoredFile.rename( nfLock, "othername.file", null );
        nfLock.releaseLock();     
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );

        // Delete the file and folder
        nonignoredFile.delete();             
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );
        nonignoredFolder.delete();
        assertNodes( ch, 
                     new String[] { "a.b.c", },
                     new int[] { 0, } );

                     
                     
        // Create some other folder
        FileUtil.createFolder( root, "src/e/f/g" );
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", },
                     new int[] { 0, 0 } );
        
        
        // Add some ignored files/folders             
        FileUtil.createFolder( root, "src/e/KRTEK" );
        FileUtil.createFolder( root, "src/e/f/KRTEK.folder" );
        FileUtil.createData( root, "src/e/f/KRTEK.file" );        
        FileUtil.createFolder( root, "src/e/f/g/KRTEK.folder" );
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", },
                     new int[] { 0, 0 } );
                                          
                    
        // Create file
        FileUtil.createData( root, "src/e/f/g/Some.java" );
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", },
                     new int[] { 0, 1 } );

        // Create ignored file
        FileUtil.createData( root, "src/e/f/g/KRTEK.file" );
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", },
                     new int[] { 0, 1 } );
                     
        // Create file in ignored folder
        FileUtil.createData( root, "src/e/f/g/KRTEK.folder/Tag" );
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g", },
                     new int[] { 0, 1 } );
                     
                                  
        // Add empty package and ignored package
        FileUtil.createFolder( root, "src/x/y/z/KRTEK" );        
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

        /*   
         * Sometime fails in Jarda's DataObject container test
         *          
        // Rename ignored file to unignored
        FileObject e_f_g_krtekFile = root.getFileObject( "src/e/f/g/KRTEK.file" );
        FileLock krtekLock = e_f_g_krtekFile.lock();
        e_f_g_krtekFile.rename( krtekLock, "ZIZALA.file", null );
        krtekLock.releaseLock();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g" },
                     new int[] { 0, 2 } );
        
                             
        // Rename unignored to ignored file
        e_f_g_krtekFile = root.getFileObject( "src/e/f/g/ZIZALA.file" );
        krtekLock = e_f_g_krtekFile.lock();
        e_f_g_krtekFile.rename( krtekLock, "KRTEK.file", null );
        krtekLock.releaseLock();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.g" },
                     new int[] { 0, 1 } );             
        */ 
                     
        // Rename leaf folder
        FileObject e_f_g = root.getFileObject( "src/e/f/g" );
        FileLock lock = e_f_g.lock();
        e_f_g.rename( lock, "h", null );
        lock.releaseLock();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.h" },
                     new int[] { 0, 1 } );
        
                     
        // Rename ignored folder to unignored folder
        FileObject e_f_h_krtekFolder = root.getFileObject( "src/e/f/h/KRTEK.folder" );
        lock = e_f_h_krtekFolder.lock();
        e_f_h_krtekFolder.rename( lock, "ZIZALA", null );
        lock.releaseLock();
        assertNodes( ch, 
                     new String[] { "a.b.c", "e.f.h", "e.f.h.ZIZALA" },
                     new int[] { 0, 1, 1 } );
                     
        // Rename unignored folder back to ignored folder
        e_f_h_krtekFolder = root.getFileObject( "src/e/f/h/ZIZALA" );
        lock = e_f_h_krtekFolder.lock();
        e_f_h_krtekFolder.rename( lock, "KRTEK.folder", null );
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
                     new int[] { 0 }, 
                     true ); // Needs to compute the nodes first
                     
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

        // Compute the nodes
        assertNodes( rootNode.getChildren(), 
                     new String[] { "a.aa", },
                     new int[] { 0, }, 
                     true ); // Needs to compute the nodes first
        
        
        n = PackageView.findPath( rootNode, toDelete );
        n.destroy();        
        assertFileObjects( srcRoot, new String[0] );
        
        // Non-Empty parent
        srcRoot = FileUtil.createFolder( root, "nep" );
        toDelete = FileUtil.createFolder( srcRoot, "a/aa" );
        FileUtil.createData( srcRoot, "a/some.java" );
        group = new SimpleSourceGroup( srcRoot );
        rootNode = PackageView.createPackageView( group );
        // Compute the nodes
        assertNodes( rootNode.getChildren(), 
                     new String[] { "a", "a.aa" },
                     new int[] { 1, 0 }, 
                     true ); // Needs to compute the nodes first
        
        
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
        // Compute the nodes
        assertNodes( rootNode.getChildren(), 
                     new String[] { "a.aa", "a.b" },
                     new int[] { 1, 0 }, 
                     true ); // Needs to compute the nodes first
        
        
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
        // Compute the nodes
        assertNodes( rootNode.getChildren(), 
                     new String[] { "a.aa", "a.b" },
                     new int[] { 0, 0 }, 
                     true ); // Needs to compute the nodes first
        
        n = PackageView.findPath( rootNode, toDelete );
        n.destroy();        
        assertFileObjects( srcRoot, new String[]{ "a" } );
        assertFileObjects( a, new String[]{ "aa" } );
        
        
        // Do not delete subfoders        
        srcRoot = FileUtil.createFolder( root, "dds" );
        a = FileUtil.createFolder( srcRoot, "a" );        
        FileUtil.createData( srcRoot, "a/some.java" );        
        FileObject aa = FileUtil.createFolder( a, "aa" );
        FileUtil.createData( srcRoot, "a/aa/some.java" );        
        toDelete = a;
        group = new SimpleSourceGroup( srcRoot );
        rootNode = PackageView.createPackageView( group );
        // Compute the nodes
        assertNodes( rootNode.getChildren(), 
                     new String[] { "a", "a.aa", },
                     new int[] { 1, 1 }, 
                     true ); // Needs to compute the nodes first
        
        n = PackageView.findPath( rootNode, toDelete );
        n.destroy();        
        assertFileObjects( srcRoot, new String[]{ "a" } );
        assertFileObjects( a, new String[]{ "aa" } );

        //Issue #49075
        srcRoot = FileUtil.createFolder(root, "issue49075");
        a = srcRoot.createFolder("a");
        FileObject b = FileUtil.createFolder( a, "b" );
        FileObject c = FileUtil.createFolder( b, "c" );
        group = new SimpleSourceGroup( srcRoot );
        rootNode = PackageView.createPackageView( group );
        assertNodes(rootNode.getChildren(), new String[] { "a.b.c" },true );
        File cFile = FileUtil.toFile(c);
        File bFile = FileUtil.toFile(b);
        cFile.delete();
        bFile.delete();
        a.getFileSystem().refresh(false);
        assertNodes(rootNode.getChildren(), new String[] { "a" },true );
    }
    
    
    public void testFindPath() throws Exception {
        
        // Prepare test data
        FileObject root = TestUtil.makeScratchDir( this );
        // System.out.println("root " + root.getFileSystem().getClass() );
        
        
	// Create children        
        SourceGroup group = new SimpleSourceGroup( FileUtil.createFolder( root, "src" ) );
        Node sourceRoot = PackageView.createPackageView( group );
        // Compute the nodes
        assertNodes( sourceRoot.getChildren(), 
                     new String[] { "<default package>" },
                     new int[] { 0 }, 
                     true ); // Needs to compute the nodes first
        
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
    
    public void testCopyPaste () throws Exception {
        //Setup 2 sourcegroups
        FileObject workDirFo = TestUtil.makeScratchDir( this );        
        FileObject root1 = workDirFo.createFolder("src1");
        FileObject tmp = root1.createFolder ("src1test1");
        root1.createFolder ("src1test2");
        createFile(tmp, "src1test1", "File1");
        createFile(tmp, "src1test1", "File2");
        FileObject root2 = workDirFo.createFolder("src2");
        SourceGroup group1 = new SimpleSourceGroup(root1);
        SourceGroup group2 = new SimpleSourceGroup(root2);
        Node rn1 = PackageView.createPackageView( group1 );        
        Node rn2 = PackageView.createPackageView( group2 );
        Node[] nodes = rn1.getChildren().getNodes(true);
        
        //Single package into same source root
        Transferable t = nodes[0].clipboardCopy();
        PasteType[] pts = rn1.getPasteTypes(t);
        assertEquals ("Single package into same source root",0, pts.length);        
        
        //Multiple packages into same source root
        t = new ExTransferable.Multi (new Transferable[] {nodes[0].clipboardCopy(),
                                                          nodes[1].clipboardCopy()});
        pts = rn1.getPasteTypes(t);
        assertEquals ("Multiple packages into same source root",0,pts.length);
        
        //Single file into package
        Node[] fileNodes = nodes[0].getChildren().getNodes(true);
        t = fileNodes[0].clipboardCopy();
        pts = nodes[1].getPasteTypes(t);
        assertEquals ("Single file into package",1, pts.length);        
        pts[0].paste();
        Node[] resultNodes = nodes[1].getChildren().getNodes(true);
        assertEquals ("Wrong paste result",1, resultNodes.length);        
        assertEquals ("Wrong paste result",fileNodes[0].getDisplayName(), resultNodes[0].getDisplayName());                
        ((DataObject)resultNodes[0].getCookie(DataObject.class)).delete();
        
        //Multiple files into package
        t = new ExTransferable.Multi (new Transferable[] {fileNodes[0].clipboardCopy(),
                                                          fileNodes[1].clipboardCopy()});
        pts = nodes[1].getPasteTypes(t);
        assertEquals ("Multiple files into package",1, pts.length);        
        pts[0].paste();
        //After change - requires optimalResults
        assertNodes (nodes[1].getChildren(), new String[] {
            fileNodes[0].getDisplayName(),
            fileNodes[1].getDisplayName(),
        }, true);
        resultNodes = nodes[1].getChildren().getNodes(true);
        for (int i=0; i< resultNodes.length; i++) {
            DataObject dobj = (DataObject) resultNodes[i].getCookie(DataObject.class);
            if (dobj != null)
                dobj.delete ();
        }
                
        //Single file into source root
        t = fileNodes[0].clipboardCopy();
        pts = rn1.getPasteTypes(t);
        assertEquals ("Single file into package",1, pts.length);        
        pts[0].paste();
        String defaultPackageName = ResourceBundle.getBundle("org/netbeans/modules/java/project/Bundle").getString("LBL_DefaultPackage");
        assertNodes(rn1.getChildren(), new String[] {
            defaultPackageName,
            "src1test1",
            "src1test2",
        }, true);
        resultNodes = rn1.getChildren().getNodes (true);
        for (int i=0; i< resultNodes.length; i++) {
            if (defaultPackageName.equals (resultNodes[i].getDisplayName())) {
                assertNodes (resultNodes[i].getChildren(), new String[] {
                    fileNodes[0].getDisplayName(),
                }, true);
                resultNodes = resultNodes[i].getChildren().getNodes(true);
                for (int j=0; j<resultNodes.length; j++) {
                    DataObject dobj = (DataObject) resultNodes[j].getCookie (DataObject.class);
                    if (dobj != null) {
                        dobj.delete ();
                    }
                }
                break;
            }
        }        
        //Multiple files into source root
        //Verify preconditions
        FileObject[] files = ((DataObject)rn1.getCookie(DataObject.class)).getPrimaryFile().getChildren();
        assertEquals("Invalid initial file count",2,files.length);

        t = new ExTransferable.Multi (new Transferable[] {fileNodes[0].clipboardCopy(),
                                                          fileNodes[1].clipboardCopy()});
        pts = rn1.getPasteTypes(t);
        assertEquals ("Multiple files into source root",1, pts.length);        
        pts[0].paste();
        //Verify that the files was added, the used PasteType is DataFolder's PasteType
        files = ((DataObject)rn1.getCookie(DataObject.class)).getPrimaryFile().getChildren();
        assertEquals("Invalid final file count",4,files.length);
        Set s = new HashSet ();
        s.add (((DataObject)fileNodes[0].getCookie(DataObject.class)).getPrimaryFile().getNameExt());
        s.add (((DataObject)fileNodes[1].getCookie(DataObject.class)).getPrimaryFile().getNameExt());
        for (int i=0; i<files.length; i++) {
            s.remove (files[i].getNameExt());
        }
        assertTrue("The following files were not created: "+s.toString(),s.size()==0);
        assertNodes(rn1.getChildren(), new String[] {
            defaultPackageName,
            "src1test1",
            "src1test2",
        }, true);
        resultNodes = rn1.getChildren().getNodes (true);
        for (int i=0; i< resultNodes.length; i++) {
            if (defaultPackageName.equals (resultNodes[i].getDisplayName())) {
                assertNodes (resultNodes[i].getChildren(), new String[] {
                    fileNodes[0].getDisplayName(),
                    fileNodes[1].getDisplayName()
                }, true);
                resultNodes = resultNodes[i].getChildren().getNodes(true);                
                for (int j=0; j<resultNodes.length; j++) {
                    DataObject dobj = (DataObject) resultNodes[j].getCookie (DataObject.class);
                    if (dobj != null) {
                        dobj.delete ();
                    }
                }
                break;
            }
        }
        
        //Single package into different source root
        t = nodes[0].clipboardCopy();
        pts = rn2.getPasteTypes(t);
        assertEquals ("Single package into different source root",1,pts.length);
        pts[0].paste ();
        assertNodes (rn2.getChildren(), new String[] {"src1test1"}, true);
        ((DataObject)rn2.getChildren().getNodes(true)[0].getCookie(DataObject.class)).delete();
        
        //Multiple packages into different source root
        t = new ExTransferable.Multi (new Transferable[] {nodes[0].clipboardCopy(),
                                                          nodes[1].clipboardCopy()});
        pts = rn2.getPasteTypes(t);
        assertEquals ("Multiple packages into different source root",1,pts.length);
        pts[0].paste ();
        assertNodes (rn2.getChildren(), new String[] {"src1test1","src1test2"}, true);
        resultNodes = rn2.getChildren().getNodes(true);
        for (int i=0; i< resultNodes.length; i++) {
            DataObject dobj = (DataObject) resultNodes[i].getCookie(DataObject.class);
            if (dobj != null)
                dobj.delete ();
        }
        
        //One more case (Issue #48246), Copy default pkg test
        FileObject defPkgFileRoot1 = createFile(root1, null, "TestDP1");
        nodes = rn1.getChildren().getNodes(true);
        FileObject defPkgFileRoot2 = createFile(root2, null, "TestDP2");
        
        Node defPkgNode = null;
        for (int i=0; i< nodes.length; i++) {
            if (nodes[i].getDisplayName().equals (defaultPackageName)) {
                defPkgNode = nodes[i];
                break;
            }
        }
        assertNotNull("Default package exists",defPkgNode);
        t = defPkgNode.clipboardCopy();
        pts = rn2.getPasteTypes(t);
        assertEquals ("Multiple packages into different source root",1,pts.length);
        pts[0].paste();
        assertNodes (rn2.getChildren(), new String[] {defaultPackageName}, true);
        defPkgFileRoot1.delete();
        resultNodes = rn2.getChildren().getNodes(true)[0].getChildren().getNodes(true);
        for (int i=0; i< resultNodes.length; i++) {
            DataObject dobj = (DataObject) resultNodes[i].getCookie(DataObject.class);
            if (dobj != null) {
                dobj.delete();
            }
        }
    }
    
    
    public void testRename() throws Exception {
        // Prepare test data
        FileObject root = TestUtil.makeScratchDir( this );
        // System.out.println("root " + root.getFileSystem().getClass() );
        
        
        assertNull( "source folder should not exist yet", root.getFileObject( "src" ) );
        
        
	// Create children
        SourceGroup group = new SimpleSourceGroup( FileUtil.createFolder( root, "src" ) );
        Children ch = PackageView.createPackageView( group ).getChildren();
        
        // Create folder
	FileUtil.createFolder( root, "src/a" );
        assertNodes( ch, 
                     new String[] { "a", },
                     new int[] { 0, },
                     true );
        
        Node n = ch.findChild( "a" );                     
        n.setName( "b" );        
        assertNodes( ch, 
                     new String[] { "b", },
                     new int[] { 0, } );
        
        FileUtil.createFolder( root, "src/b/c" );
        assertNodes( ch, 
                     new String[] { "b.c", },
                     new int[] { 0, } );
        
        n = ch.findChild( "b.c" );                     
        n.setName( "b.d" );        
        assertNodes( ch, 
                     new String[] { "b.d", },
                     new int[] { 0, } );
        
        n = ch.findChild( "b.d" );                     
        n.setName( "a.d" );
        assertNodes( ch, 
                     new String[] { "a.d", },
                     new int[] { 0, } );
        
        FileUtil.createFolder( root, "src/a/e" );
        assertNodes( ch, 
                     new String[] { "a.d", "a.e" },
                     new int[] { 0, 0 } );
        
        n = ch.findChild( "a.e" );                     
        n.setName( "a.f" );
        assertNodes( ch, 
                     new String[] { "a.d", "a.f" },
                     new int[] { 0, 0 } );
        
        
        n = ch.findChild( "a.d" );                     
        n.setName( "c.d" );
        assertNodes( ch, 
                     new String[] { "a.f", "c.d"},
                     new int[] { 0, 0 } );
        
        n = ch.findChild( "a.f" );                     
        n.setName( "c.f" );
        assertNodes( ch, 
                     new String[] { "c.d", "c.f" },
                     new int[] { 0, 0 } );
                     
        
        FileUtil.createFolder( root, "src/x/y/z" );
        assertNodes( ch, 
                     new String[] { "c.d", "c.f", "x.y.z" },
                     new int[] { 0, 0, 0 } );
        n = ch.findChild( "x.y.z" );                     
        n.setName( "x.y" );
        assertNodes( ch, 
                     new String[] { "c.d", "c.f", "x.y" },
                     new int[] { 0, 0, 0 } );                                          
        n = ch.findChild( "x.y" );                     
        n.setName( "p.me.tools" );
        assertNodes( ch, 
                     new String[] { "c.d", "c.f", "p.me.tools" },
                     new int[] { 0, 0, 0 } );                     
        n = ch.findChild( "p.me.tools" );
        n.setName( "p.metools" );
        assertNodes( ch, 
                     new String[] { "c.d", "c.f", "p.metools" },
                     new int[] { 0, 0, 0 } );                                          
        n = ch.findChild( "p.metools" );
        n.setName( "p.me.tools" );
        assertNodes( ch, 
                     new String[] { "c.d", "c.f", "p.me.tools" },
                     new int[] { 0, 0, 0 } );                     
        n = ch.findChild( "p.me.tools" );
        n.setName( "p.me.toolsx" );
        assertNodes( ch, 
                     new String[] { "c.d", "c.f", "p.me.toolsx" },
                     new int[] { 0, 0, 0 } );
        n = ch.findChild( "p.me.toolsx" );
        n.setName( "p.me.tools" );
        assertNodes( ch,
                     new String[] { "c.d", "c.f", "p.me.tools" },
                     new int[] { 0, 0, 0 } );
    }

    public static void assertNodes( Children children, String[] nodeNames, boolean optimalResult ) {
        assertNodes( children, nodeNames, null, optimalResult );
    }

    public static void assertNodes( Children children, String[] nodeNames ) {
        assertNodes( children, nodeNames, null, false );
    }

    public static void assertNodes (Children children, String[] nodeNames, int[] childCount) {
        assertNodes(children, nodeNames, childCount, false);
    }

    public static void assertNodes( Children children, String[] nodeNames, int[] childCount, boolean optimalResult ) {
        Node[] nodes = children.getNodes (optimalResult);
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
                
                
                DataObject.Container cont = (DataObject.Container)nodes[i].getCookie (DataObject.Container.class);
                if (cont != null) {
                    Node[] arr = nodes[i].getChildren ().getNodes ( true );
                    DataObject[] child = cont.getChildren ();
                    for (int k = 0, l = 0; k < arr.length; k++) {
                        if ( !VisibilityQuery.getDefault().isVisible( child[k].getPrimaryFile() ) ) {
                            continue;
                        }
                        DataObject myObj = (DataObject)arr[l].getCookie (DataObject.class);
                        assertNotNull ("Data object should be found for " + arr[k], myObj);
                        if (child.length <= k) {
                            fail ("But there is no object for node: " + arr[k]);
                        } else {
                            assertEquals ("child objects are the same", child[k], myObj);
                        }
                        l++;
                    }
                }
                
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
    
    private static FileObject createFile (FileObject parent, String pkg, String name) throws IOException {
        FileObject fo = parent.createData (name,"java");
        FileLock lock = fo.lock();
        try {
            PrintWriter out = new PrintWriter (new OutputStreamWriter (fo.getOutputStream(lock)));            
            try {
                if (pkg != null) {
                    out.println ("package "+pkg+";");
                }
                out.println("public class "+name+" {");            
                out.println("}");
            } finally {
                out.close ();
            }
        } finally {
            lock.releaseLock();
        }
        return fo;
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
    
    private static class VQImpl implements VisibilityQueryImplementation {
        
        public static String IGNORED = "KRTEK"; 
        
        public boolean isVisible(FileObject file) {
            return !file.getNameExt().startsWith( IGNORED );
        }

        public void addChangeListener(ChangeListener l) {}

        public void removeChangeListener(ChangeListener l) {}
        
    }
    
}
