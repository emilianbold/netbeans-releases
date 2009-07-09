/*
 * FaceletLocalFileSystem.java
 *
 * Created on December 2, 2006, 7:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.frameworks.facelets.loaders;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import junit.framework.TestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Petr Pisl
 */

public class FaceletLocalFileSystem extends TestCase{
    
    LocalFileSystem lfs;
    
    static {
        // set the lookup which will be returned by Lookup.getDefault()
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        ((Lkp) Lookup.getDefault()).setLookups(new Object[] {
            new FaceletsMimeResolver(),
            new Pool()
        });
    }
    
    public FaceletLocalFileSystem(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("data");
        File root = new File(url.toURI());
        assertTrue("Root folder doesn't exist.", root.exists());
        lfs = new LocalFileSystem();
        lfs.setRootDirectory(root);
        Repository.getDefault().addFileSystem(lfs);
    }

    protected void tearDown() throws Exception {
        Repository.getDefault().removeFileSystem(lfs);
    }
    
    protected FaceletDataObject findDataObject(String file) throws Exception{
        FileObject rFO =  lfs.findResource(file);
        return (FaceletDataObject)DataObject.find(rFO);
    }
    /**
     * DataLoaderPool which is registered in the default lookup and loads
     * PUDataLoader.
     */
    public static final class Pool extends DataLoaderPool {

        public Enumeration loaders() {
            return Enumerations.singleton(new FaceletDataLoader());
        }
    }
    
    /**
     * Our default lookup.
     */
    public static final class Lkp extends ProxyLookup {

        public Lkp() {
            setLookups(new Object[0]);
        }

        public void setLookups(Object[] instances) {
            ClassLoader l = FaceletDataObjectTest.class.getClassLoader();
            setLookups(new Lookup[] {
                Lookups.fixed(instances),
                Lookups.metaInfServices(l),
                Lookups.singleton(l)
            });
        }
    }
}
