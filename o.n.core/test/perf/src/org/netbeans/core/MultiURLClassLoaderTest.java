/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.core;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.FileObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.loaders.DataObject;

import org.netbeans.performance.Benchmark;
import org.openide.filesystems.multifs.MultiXMLFSTest;
import org.openide.filesystems.multifs.MultiXMLFSTest.FSWrapper;
import org.openide.filesystems.ReadOnlyFSTest;
import org.openide.loaders.Utilities;

/**
 * Performance test for MultiURLClassLoader.
 */
public class MultiURLClassLoaderTest extends Benchmark {
    
    private static final Object[] ARGS = new Object[] { new Integer(3000) };
    
    private MultiURLClassLoader multicloader;
    private MultiFileSystem mfs;
    private FileObject[] fileObjects;
    private InstanceDataObject[] instanceObjects;
    private FSWrapper[] wrappers;
    
    /** Creates new Benchmark without arguments for given test method
     * @param name the name fo the testing method
     */    
    public MultiURLClassLoaderTest(String name) {
        super(name);
        setArgumentArray(ARGS);
    }

    /** Creates new Benchmark for given test method with given set of arguments
     * @param name the name fo the testing method
     * @param args the array of objects describing arguments to testing method
     */    
    public MultiURLClassLoaderTest(String name, Object[] args) {
        super(name, args);
    }
    
    // things to override by the implementation of a particular Benchmark

    /** This method is called before the actual test method to allow
     * the benchmark to prepare accordingly to informations available
     * through {@link #getIterationCount}, {@link #getArgument} and {@link #getName}.
     * This method can use assertions to signal failure of the test.
     * @throws Exception This method can throw any exception which is treated as a error in the testing code
     * or testing enviroment.
     */
    protected void setUp() throws Exception {
        int size = 100; //0; //((Integer) getArgument()).intValue();
        MultiXMLFSTest multifstest = new MultiXMLFSTest(getName());
        fileObjects = multifstest.setUpFileObjects(size);
        wrappers = multifstest.getFSWrappers();
        URLClassLoader[] parents = new URLClassLoader[wrappers.length];
        for (int i = 0; i < parents.length; i++) {
            parents[i] = wrappers[i].getClassLoader();
        }
        
        multicloader = new MultiURLClassLoader(new URL[] {}, parents);
        mfs = multifstest.getMultiFileSystem();
        
        instanceObjects = fileObjects2InstanceDataObjects(fileObjects);
        setClassLoader(instanceObjects, multicloader);
    }
    
    private static InstanceDataObject[] fileObjects2InstanceDataObjects(FileObject[] fos) throws Exception {
        ArrayList list = new ArrayList(fos.length);
        for (int i = 0; i < fos.length; i++) {
            DataObject res = DataObject.find(fos[i]);
            if (res instanceof InstanceDataObject) {
                list.add(res);
            }
        }
        
        return (InstanceDataObject[]) list.toArray(new InstanceDataObject[list.size()]);
    }
    
    private static void setClassLoader(InstanceDataObject[] idos, ClassLoader cl) throws Exception {
        for (int i = 0; i < idos.length; i++) {
            Utilities.setCustomClassLoader(idos[i], cl);
        }
    }

    /** This method is called after every finished test method.
     * It is intended to be used to free all the resources allocated
     * during {@link #setUp} or the test itself.
     * This method can use assertions to signal failure of the test.
     * @throws Exception This method can throw any exception which is treated as a error in the testing code
     * or testing enviroment.
     */
    protected void tearDown() throws Exception {
        for (int i = 0; i < wrappers.length; i++) {
            ReadOnlyFSTest.delete(wrappers[i].getFile());
        }
    }
    
    /** MultiURLClassLoader */
    public void testInstanceClasses() throws Exception {
        for (int i = 0; i < instanceObjects.length; i++) {
            String klass = instanceObjects[i].instanceName();
            instanceObjects[i].instanceClass();
        }
    }
    

    /*
    public static void main(String[] args) throws Exception {
        MultiURLClassLoaderTest mcltest = new MultiURLClassLoaderTest("test");
        mcltest.setUp();
        
        System.out.println("ORDINARY: " + mcltest.wrappers[1].getClassLoader().loadClass("org.openide.filesystems.data10.JavaSrc15"));
        System.out.println("Multi: " + mcltest.multicloader.loadClass("org.openide.filesystems.data10.JavaSrc15"));
        System.out.println("Multi2: " + mcltest.multicloader.loadClass("org.openide.filesystems.data90.JavaSrc99"));
    }
     */
}