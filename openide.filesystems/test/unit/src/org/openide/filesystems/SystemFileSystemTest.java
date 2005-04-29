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
import org.openide.*;


import java.io.*;

/**
 *
 * @author  rmatous
 * @version 
 */
public class SystemFileSystemTest extends FileSystemFactoryHid {
    
    /** Creates new MultiFileSystemTest */
    public SystemFileSystemTest(Test test) {
        super(test);        
    }

    public static void main(String args[]) throws  Exception{
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(MfoOnSFSTestHid.class);
        
        /*failing tests*/        
/*
        suite.addTestSuite(URLMapperTestHidden.class);        
        suite.addTestSuite(URLMapperTestInternalHidden.class);                        
*/
        
        return new SystemFileSystemTest (suite);
    }
    

    /**
     * 
     * @param testName name of test 
     * @return  array of FileSystems that should be tested in test named: "testName" */
    protected FileSystem[] createFileSystem (String testName, String[] resources) throws IOException {        
            return new FileSystem[] {};
    }
    
    protected void destroyFileSystem (String testName) throws IOException {} 
}
