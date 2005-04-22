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

import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URI;

import junit.textui.TestRunner;
import org.openide.filesystems.FileSystem;
import java.util.Enumeration;
import java.io.File;

import org.openide.nodes.Node;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.Repository;
import org.netbeans.junit.*;
import org.openide.filesystems.*;

/** Test things about shadows and broken shadows, etc.
 * @author Jaroslav Tulach
 */
public class DataShadowTest extends NbTestCase 
implements java.net.URLStreamHandlerFactory {
    /** original object */
    private DataObject original;
    /** folder to work with */
    private DataFolder folder;
    /** fs we work on */
    private FileSystem lfs;
    
    static {
        // to handle nbfs urls...
        java.net.URL.setURLStreamHandlerFactory (new DataShadowTest (null));
    }
    
    public DataShadowTest (String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        
        lfs = Repository.getDefault ().getDefaultFileSystem ();
        
        FileObject[] delete = lfs.getRoot().getChildren();
        for (int i = 0; i < delete.length; i++) {
            delete[i].delete();
        }
        
        FileObject fo = FileUtil.createData (lfs.getRoot (), getName () + "/folder/original.txt");
        assertNotNull(fo);
        original = DataObject.find (fo);
        assertFalse ("Just to be sure that this is not shadow", original instanceof DataShadow);
        assertFalse ("And is some kind of subclass of DO", original.getClass () == DataObject.class);
        fo = FileUtil.createFolder (lfs.getRoot (), getName () + "/modify");
        assertNotNull(fo);
        assertTrue (fo.isFolder ());
        folder = DataFolder.findFolder (fo);
        
        Repository.getDefault ().addFileSystem (lfs);
    }
    
    protected void tearDown() throws Exception {
    }

    public java.net.URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals ("nbfs")) {
            return FileUtil.nbfsURLStreamHandler ();
        }
        return null;
    }
    
    public void testBrokenShadow55115 () throws Exception {
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        FileObject brokenShadow = FileUtil.createData(sfs.getRoot(),"brokenshadows/brokon.shadow");
        assertNotNull (brokenShadow);
        // intentionally not set attribute "originalFile" to let that shadow be broken 
        //brokenShadow.setAttribute("originalFile", null);
        BrokenDataShadow bds = (BrokenDataShadow)DataObject.find(brokenShadow);
        assertNotNull (bds);
        URL url = bds.getUrl();
        //this call proves #55115 - but just in case if there is reachable masterfs 
        // - probably unwanted here
        bds.refresh(); 
        
        //If masterfs isn't reachable - second test crucial for URL,File, FileObject conversions
        // not necessary to be able to convert - but at least no IllegalArgumentException is expected
        if ("file".equals(url.getProtocol())) {
            new File (URI.create(url.toExternalForm()));
        }
    }
    
    public void testCreateTheShadow () throws Exception {
        DataShadow shade = original.createShadow (folder);
        
        assertEquals ("Shadow's original is the one", original, shade.getOriginal ());
        
        Object cookie = shade.getCookie (DataObject.class);
        assertEquals ("The shadow is own data object", shade, cookie);
        
        cookie = shade.getCookie (original.getClass ());
        assertEquals ("But it also returns the original when requested", original, cookie);
        
        URL u = DataShadow.readURL(shade.getPrimaryFile());
        assertEquals("DataShadow's URL must point to the Original", original.getPrimaryFile().getURL(), u);
    }
    
    public void testDeleteInvalidatesCreateCreates () throws Exception {
        doDeleteInvalidatesCreateCreates (true);
    }
    
    /* This is not implemented and could cause problems when module is enabled
     * and there is a link to a file in its layer - this link could possibly
     * not be updated (until creation of another data object)
     */
    public void testDeleteInvalidatesCreateCreatesJustOnFileSystemLevel () throws Exception {
        doDeleteInvalidatesCreateCreates (false);
    }

    private void doDeleteInvalidatesCreateCreates (boolean createDataObjectOrNot) throws Exception {
        DataShadow shade = original.createShadow (folder);
        FileObject primary = shade.getPrimaryFile ();

        assertTrue ("Is valid now", shade.isValid ());
        original.delete ();
        
        assertFalse ("Shadow is not valid anymore", shade.isValid ());
        assertFalse ("Original is gone", original.isValid ());
        
        DataObject shade2 = DataObject.find (primary);
        assertEquals ("Represents broken shadow (a bit implemetnation detail, but useful test)", BrokenDataShadow.class, shade2.getClass ());
        assertFalse ("Is not data shadow", shade2 instanceof DataShadow);
        
        // recreates the original
        FileObject original2 = FileUtil.createData (lfs.getRoot (), original.getPrimaryFile ().getPath ());
        DataObject obj2;
        
        if (createDataObjectOrNot) {
            obj2 = DataObject.find (original2);
        }
        
        assertFalse ("Previous is not valid anymore", shade2.isValid ());
        
        DataObject shade3 = DataObject.find (primary);
        assertTrue ("it is a data shadow again", shade3 instanceof DataShadow);
        assertEquals ("Points to the same filename", original.getPrimaryFile ().getPath (), ((DataShadow)shade3).getOriginal ().getPrimaryFile ().getPath ());
        
        assertEquals ("But of course the original is newly created", DataObject.find (original2), ((DataShadow)shade3).getOriginal ());
        
        assertEquals ("However the old shadow is not updated as originals are never updated", original, shade.getOriginal ());
    }

    public void testDeleteInvalidatesCreateCreatesWhenChangeHappensInAtomicAction () throws Exception {
        DataShadow shade = original.createShadow (folder);
        FileObject primary = shade.getPrimaryFile ();

        assertTrue ("Is valid now", shade.isValid ());
        
        class DeleteCreate implements FileSystem.AtomicAction {
            public FileObject fo;
            
            public void run () throws java.io.IOException {
                FileSystem fs = original.getPrimaryFile ().getFileSystem ();
                String create = original.getPrimaryFile ().getPath ();
                original.getPrimaryFile ().delete ();
                
                fo = FileUtil.createData (fs.getRoot (), create);
            }
        }
        DeleteCreate deleteCreate = new DeleteCreate ();
        original.getPrimaryFile ().getFileSystem ().runAtomicAction (deleteCreate);
        
        assertTrue ("Shadow is valid (again)", shade.isValid ());
        assertFalse ("Original is gone", original.isValid ());
        DataObject orig = DataObject.find (deleteCreate.fo);
        if (orig == original) {
            fail ("new original shall be created");
        }
        assertTrue ("New orig is valid", orig.isValid ());
        
        // life would be nicer without this sleep, but somewhere inside
        // the DataShadow validation a request is send to RP with a delay
        // to not slow down regular apps. If you managed to kill next line,
        // you will have done the right job. Meanwhile it is here:
        Thread.sleep (2000);
        
        assertEquals ("Shadow's original is updated", orig, shade.getOriginal ());
    }
    
    public void testRenameUpdatesTheShadowIfItExists () throws Exception {
        DataShadow shade = original.createShadow (folder);
        FileObject primary = shade.getPrimaryFile ();
        
        original.rename ("newname.txt");
        
        WeakReference ref = new WeakReference (shade);
        shade = null;
        assertGC ("Shadow can disappear", ref);
        
        DataObject obj = DataObject.find (primary);
        assertEquals ("It is shadow", DataShadow.class, obj.getClass ());
        shade = (DataShadow)obj;
        
        assertEquals ("And points to original with updated name", original, shade.getOriginal ());
    }
    
    public void testRenameDoesNotUpdateTheShadowIfItDoesNotExist () throws Exception {
        //
        // Not sure if this is the desired behaviour, however it is the
        // one currently implemented
        //
        
        DataShadow shade = original.createShadow (folder);
        FileObject primary = shade.getPrimaryFile ();
        
        WeakReference ref = new WeakReference (shade);
        shade = null;
        assertGC ("Shadow can disappear", ref);
        
        original.rename ("newname");
        
        
        DataObject obj = DataObject.find (primary);
        assertEquals ("It is broken shadow", BrokenDataShadow.class, obj.getClass ());
    }
    
    public void testBrokenShadowNodeProperties() throws Exception {
        DataShadow shade = original.createShadow (folder);
        FileObject primary = shade.getPrimaryFile ();
        
        assertTrue ("Is valid now", shade.isValid ());
        original.delete ();

        DataObject obj = DataObject.find (primary);
        assertEquals ("Instance class must be BrokenDataShadow", BrokenDataShadow.class, obj.getClass ());
        
        Node node = obj.getNodeDelegate ();
        
        Node.Property link = findProperty (node, "BrokenLink");
        assertNotNull ("Link must be non null string", (String)link.getValue ());
        
        assertTrue ("Is writeable", link.canWrite ());
        // this will revalidate the link
        FileObject fo = FileUtil.createData (lfs.getRoot (), getName () + "/folder/orig.txt");
        link.setValue (fo.getURL().toExternalForm());
       
        assertFalse ("The change of link should turn the shadow to valid one and invalidate this broken shadow", obj.isValid ());
        
        DataObject newObj = DataObject.find (primary);
        assertEquals ("This is a shadow", DataShadow.class, newObj.getClass ());
        shade = (DataShadow)newObj;
        
        assertEquals ("Points to the new file", getName () + "/folder/orig.txt", shade.getOriginal ().getPrimaryFile ().getPath ());
    }
    
    private static Node.Property findProperty (Node n, String name) {
        Node.PropertySet[] arr = n.getPropertySets ();
        StringBuffer names = new StringBuffer ();
        
        String prefix = "";
        for (int i = 0; i < arr.length; i++) {
            Node.PropertySet set = arr[i];
            Node.Property[] properties = set.getProperties ();
            for (int j = 0; j < properties.length; j++) {
                Node.Property p = properties[j];
                if (name.equals (p.getName ())) {
                    return p;
                }
                names.append (prefix);
                names.append (p.getName ());
                prefix = ", ";
            }
        }
        
        fail ("Cannot find property \"" + name + "\" in node " + n + " it has only " + names + " propeties.");
        return null;
    }
}
