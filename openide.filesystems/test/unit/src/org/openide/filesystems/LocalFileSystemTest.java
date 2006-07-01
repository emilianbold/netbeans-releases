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
import java.io.*;
import org.netbeans.junit.*;

/**
 *
 * @author  rm111737
 * @version
 */
public class LocalFileSystemTest extends FileSystemFactoryHid {

    /** Creates new LocalFileSystemTest */
    public LocalFileSystemTest (Test test) {
        super(test);
    }


    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }



    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();

        suite.addTestSuite(AttributesTestHidden.class);
        suite.addTestSuite(RepositoryTestHid.class);                
        suite.addTestSuite(FileSystemTestHid.class);                         
        suite.addTestSuite(FileObjectTestHid.class);        
        suite.addTestSuite(LocalFileSystemTestHid.class);
        /*failing tests*/        
        suite.addTestSuite(URLMapperTestHidden.class);        
        suite.addTestSuite(URLMapperTestInternalHidden.class);                        
        suite.addTestSuite(FileUtilTestHidden.class);                        
        
        return new LocalFileSystemTest(suite);
    }

    protected void destroyFileSystem (String testName) throws IOException {}
    
    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {
        return new FileSystem[] {TestUtilHid.createLocalFileSystem(testName, resources)};
    }
}
