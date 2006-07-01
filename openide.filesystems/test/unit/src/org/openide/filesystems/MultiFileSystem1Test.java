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

package org.openide.filesystems;

import junit.framework.*;
import org.netbeans.junit.*;
//import org.openide.filesystems.hidden.*;

import java.io.*;
import java.beans.PropertyVetoException;

/**
 *
 * @author  rmatous
 * @version
 */
public class MultiFileSystem1Test extends FileSystemFactoryHid {

    /** Creates new MultiFileSystemTest */
    public MultiFileSystem1Test(Test test) {
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

        return new MultiFileSystem1Test (suite);
    }
    

    /**
     * 
     * @param testName name of test 
     * @return  array of FileSystems that should be tested in test named: "testName" */
    protected FileSystem[] createFileSystem (String testName, String[] resources) throws IOException {
            FileSystem lfs = TestUtilHid.createLocalFileSystem("mfs1"+testName, resources);
            FileSystem xfs = TestUtilHid.createXMLFileSystem(testName, new String[] {});
            FileSystem mfs = new MultiFileSystem (new FileSystem[] {lfs,xfs});
            try {
                mfs.setSystemName("mfs1test");
            } catch (PropertyVetoException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }
        return new FileSystem[] {mfs,lfs,xfs};
    }
    
    protected void destroyFileSystem (String testName) throws IOException {}            
}
