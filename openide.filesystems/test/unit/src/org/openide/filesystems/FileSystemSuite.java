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

public class FileSystemSuite extends NbTestCase {

    public FileSystemSuite(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite("FileSystemTest");


/*
        suite.addTest(XMLFileSystemTest.suite());
        suite.addTest(LocalFileSystemTest.suite());
        suite.addTest(JarFileSystemTest.suite());                                
        //suite.addTest(MultiFileSystemTest.suite());                        
        suite.addTest(MultiFileSystem1Test.suite());                                
        //suite.addTest(MultiFileSystem2Test.suite());                                
        //suite.addTest(MultiFileSystem3Test.suite());                                
*/

        return suite;
    }
    
}
