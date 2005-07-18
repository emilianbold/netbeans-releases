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
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.*;

import org.openide.filesystems.*;

/**
 *
 * @author  David Strupl
 * @version
 */
public class MemoryFileSystemTest extends FileSystemFactoryHid {
    /** Creates new JarFileSystemTest */
    public MemoryFileSystemTest(Test test) {
        super(test);
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RepositoryTestHid.class);
        suite.addTestSuite(FileSystemTestHid.class);
        suite.addTestSuite(FileObjectTestHid.class);
        suite.addTestSuite(MemoryFSTestHid.class);
        
        return new MemoryFileSystemTest(suite);
    }
    
    
    protected void destroyFileSystem(String testName) throws IOException {
    }
    
    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException{
        return new FileSystem[] {new MemoryFileSystem(resources)};
    }
}
