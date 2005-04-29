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

package org.openide.filesystems;

import junit.framework.*;
import org.netbeans.junit.*;
//import org.openide.filesystems.hidden.*;

import java.io.*;
import java.beans.PropertyVetoException;

/**
 *
 * @author  rm111737
 * @version 
 */
public class MultiFileSystem3Test extends FileSystemFactoryHid {
    /** Creates new MultiFileSystemTest */
    public MultiFileSystem3Test(Test test) {
        super(test);        
    }

    public static void main(String args[]) throws  Exception{
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RepositoryTestHid.class);                        
        suite.addTestSuite(FileSystemTestHid.class);                        
        suite.addTestSuite(AttributesTestHidden.class);
        suite.addTestSuite(FileObjectTestHid.class);
        suite.addTestSuite(MultiFileObjectTestHid.class);
        /*failing tests*/        
        suite.addTestSuite(URLMapperTestHidden.class);        
        suite.addTestSuite(URLMapperTestInternalHidden.class);                        
        suite.addTestSuite(FileUtilTestHidden.class);                        
        
        return new MultiFileSystem3Test (suite);
    }
    

    /**
     * 
     * @param testName name of test 
     * @return  array of FileSystems that should be tested in test named: "testName" */
    protected FileSystem[] createFileSystem (String testName, String[] resources) throws IOException {        
            FileSystem lfs = TestUtilHid.createLocalFileSystem("mfs3"+testName, resources);
            FileSystem xfs = TestUtilHid.createXMLFileSystem(testName, resources);
            FileSystem mfs = new MultiFileSystem (new FileSystem[] {lfs,xfs});
            try {
                mfs.setSystemName("mfs3test");
            } catch (PropertyVetoException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }
        return new FileSystem[] {mfs,lfs,xfs};
    }
    
    protected void destroyFileSystem (String testName) throws IOException {}            
}
