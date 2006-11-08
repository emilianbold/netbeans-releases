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

package org.netbeans.modules.editor.settings.storage;


import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import junit.framework.Assert;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.Status;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
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
    
    public static void setLookup(Object[] instances, ClassLoader cl, FileObject servicesFolder, Class [] exclude) {
        Lookup metaInfServices = Lookups.metaInfServices(cl);
        if (exclude != null && exclude.length > 0) {
            metaInfServices = Lookups.exclude(metaInfServices, exclude);
        }
        
        DEFAULT_LOOKUP.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            metaInfServices,
            Lookups.singleton(cl),
        });
        
        if (servicesFolder != null) {
            // DataSystems need default repository, which is read from the default lookup.
            // That's why the lookup is set first without the services lookup and then again
            // here with the FolderLookup over the Services folder.
            Lookup services = new FolderLookup(DataFolder.findFolder(servicesFolder)).getLookup();
            if (exclude != null && exclude.length > 0) {
                services = Lookups.exclude(services, exclude);
            }
            
            DEFAULT_LOOKUP.setLookups(new Lookup[] {
                Lookups.fixed(instances),
                metaInfServices,
                Lookups.singleton(cl),
                services
            });
        }
    }
    
    public static void setLookup(String[] files, File workDir, Object[] instances, ClassLoader cl)
    throws IOException, PropertyVetoException {
        setLookup(files, workDir, instances, cl, null);
    }
    
    public static void setLookup(String[] files, File workDir, Object[] instances, ClassLoader cl, Class [] exclude)
    throws IOException, PropertyVetoException {
        FileSystem fs = createLocalFileSystem(workDir, files);
        setLookup(new FileSystem [] { fs }, instances, cl, exclude);
    }
    
    public static void setLookup(URL[] layers, File workDir, Object[] instances, ClassLoader cl)
    throws IOException, PropertyVetoException {
        setLookup(layers, workDir, instances, cl, null);
    }
    
    public static void setLookup(URL[] layers, File workDir, Object[] instances, ClassLoader cl, Class [] exclude)
    throws IOException, PropertyVetoException {
        FileSystem writeableFs = createLocalFileSystem(workDir, new String[0]);
        XMLFileSystem layersFs = new XMLFileSystem();
        layersFs.setXmlUrls(layers);
        
        setLookup(new FileSystem [] { writeableFs, layersFs }, instances, cl, exclude);
    }
    
    private static void setLookup(FileSystem [] fs, Object[] instances, ClassLoader cl, Class [] exclude)
    throws IOException, PropertyVetoException {

        // Remember the tests run in the same VM and repository is singleton.
        // Once it is created for the first time it will stick around forever.
        Repository repository = (Repository) Lookup.getDefault().lookup(Repository.class);
        if (repository == null) {
            repository = new Repository(new SystemFileSystem(fs));
        } else {
            ((SystemFileSystem) repository.getDefaultFileSystem()).setOrig(fs);
        }
        
        Object[] lookupContent = new Object[instances.length + 1];
        lookupContent[0] = repository;
        System.arraycopy(instances, 0, lookupContent, 1, instances.length);

        // Create the Services folder (if needed}
        FileObject services = repository.getDefaultFileSystem().findResource("Services");
        if (services == null) {
            services = repository.getDefaultFileSystem().getRoot().createFolder("Services");
        }
        
        DEFAULT_LOOKUP.setLookup(lookupContent, cl, services, exclude);
    }

    private static FileSystem createLocalFileSystem(File mountPoint, String[] resources) throws IOException {
        mountPoint.mkdir();
        
        for (int i = 0; i < resources.length; i++) {
            createFileOnPath(mountPoint, resources[i]);
        }
        
        LocalFileSystem lfs = new StatusFileSystem();
        try {
        lfs.setRootDirectory(mountPoint);
        } catch (Exception ex) {}
        
        return lfs;
    }

    private static void createFileOnPath(File mountPoint, String path) throws IOException{
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
    
    private static class StatusFileSystem extends LocalFileSystem {
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
    
    private static class SystemFileSystem extends MultiFileSystem {
        public SystemFileSystem(FileSystem [] orig) {
            super(orig);
        }
        
        public void setOrig(FileSystem [] orig) {
            setDelegates(orig);
        }
    }
}
