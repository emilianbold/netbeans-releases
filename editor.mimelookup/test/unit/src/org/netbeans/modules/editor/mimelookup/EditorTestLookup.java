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

package org.netbeans.modules.editor.mimelookup;


import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.Assert;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.Status;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/**
 * Inspired by org.netbeans.api.project.TestUtil and FolderLookupTest
 *
 * @author Martin Roskanin
 */
public class EditorTestLookup extends ProxyLookup {
    
    public static EditorTestLookup DEFAULT_LOOKUP = null;
    
    static {
        EditorTestLookup.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", EditorTestLookup.class.getName());
        Assert.assertEquals(EditorTestLookup.class, Lookup.getDefault().getClass());
    }
    
    public EditorTestLookup() {
        Assert.assertNull(DEFAULT_LOOKUP);
        DEFAULT_LOOKUP = this;
    }
    
    public static void setLookup(Object[] instances, ClassLoader cl) {
        DEFAULT_LOOKUP.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    
    public static void setLookup(String[] files, File workDir, Object[] instances, ClassLoader cl)
    throws IOException, PropertyVetoException {

        FileSystem system = createLocalFileSystem(workDir, files);
        
        Repository repository = new Repository(system);

        Object[] lookupContent = new Object[instances.length + 1];
        lookupContent[0] = repository;
        System.arraycopy(instances, 0, lookupContent, 1, instances.length);
        
        DEFAULT_LOOKUP.setLookup(lookupContent, cl);
    }

    private static synchronized void deleteFileImpl(File workDir, String path) throws IOException{
        FileObject fo = FileUtil.toFileObject(new File(workDir, path));
        if (fo == null) {
            fo = Repository.getDefault().getDefaultFileSystem().findResource(path); // NOI18N
            if (fo == null){
                return;
            }
        }
        FileObject parent = fo.getParent();
        fo.delete();        
        FileObject[] list = parent.getChildren();
        int len = (list == null ? 0 : list.length);

        while (parent!= null && len==0){
            parent.delete();            
            parent = parent.getParent();
            list = parent.getChildren();
            len = (list == null ? 0 : list.length);
        }
    }
    
    public static void deleteFile(final File workDir, final String path) throws IOException{
        // delete a file from a different thread
        RequestProcessor.getDefault().post(new Runnable(){
            public void run(){
                try {
                    deleteFileImpl(workDir, path);
                } catch (IOException ioe){
                    ioe.printStackTrace();
                }
            }
        });
    }

    private static synchronized void createFileImpl(File file) throws IOException{
        List parents = new ArrayList();
        String name = file.getName();
        File parent = file.getParentFile();
        while (!parent.exists()){
            parents.add(parent.getName());
            parent = parent.getParentFile();
        }
        
        FileObject dir = FileUtil.toFileObject(parent);
        
        for (int i = parents.size() - 1; i>=0; i--){
            String folderName = (String)parents.get(i);
            dir = dir.createFolder(folderName);
            dir.refresh();
        }
        
        FileObject fileObj =  dir.createData(name);
        fileObj.refresh();
    }
    
    public static void createFile(final File file) throws IOException{
        // create a file from a different thread
        RequestProcessor.getDefault().post(new Runnable(){
            public void run(){
                try {
                    createFileImpl(file);
                } catch (IOException ioe){
                    ioe.printStackTrace();
                }
            }
        });
    }
    
    private static FileSystem createLocalFileSystem(File mountPoint, String[] resources) throws IOException {
        mountPoint.mkdir();
        
        for (int i = 0; i < resources.length; i++) {
            createFile(mountPoint, resources[i]);
        }
        
        LocalFileSystem lfs = new StatusFileSystem();
        try {
        lfs.setRootDirectory(mountPoint);
        } catch (Exception ex) {}
        
        return lfs;
    }

    private static void createFile(File mountPoint, String path) throws IOException{
        mountPoint.mkdir();
        
        File f = new File (mountPoint, path);
        if (f.isDirectory() || path.endsWith("/")) {
            f.mkdirs();
        }
        else {
            f.getParentFile().mkdirs();
            try {
                f.createNewFile();
            } catch (IOException iex) {
                throw new IOException ("While creating " + path + " in " + mountPoint.getAbsolutePath() + ": " + iex.toString() + ": " + f.getAbsolutePath());
            }
        }
    }
    
    static class StatusFileSystem extends LocalFileSystem {
        Status status = new Status () {
            public String annotateName (String name, java.util.Set files) {
                return name;
            }

            public java.awt.Image annotateIcon (java.awt.Image icon, int iconType, java.util.Set files) {
                return icon;
            }
        };        
        
        public org.openide.filesystems.FileSystem.Status getStatus() {
            return status;
        }
        
    }
    
    
    
}
