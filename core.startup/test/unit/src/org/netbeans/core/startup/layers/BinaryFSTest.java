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

package org.netbeans.core.startup.layers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.AttributesTestHidden;
import org.openide.filesystems.FileObjectTestHid;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemFactoryHid;
import org.openide.filesystems.FileSystemTestHid;
import org.openide.filesystems.TestUtilHid;
import org.openide.filesystems.XMLFileSystem;

/**
 *
 * @author Radek Matous
 */
public class BinaryFSTest extends FileSystemFactoryHid {  
    public BinaryFSTest(Test test) {
        super(test);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(FileSystemTestHid.class);
        suite.addTestSuite(FileObjectTestHid.class);
        suite.addTestSuite(AttributesTestHidden.class);
         
        return new BinaryFSTest(suite);
    }
    
    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {
        XMLFileSystem xfs = (XMLFileSystem)TestUtilHid.createXMLFileSystem(testName, resources);
        BinaryCacheManager bm = new BinaryCacheManager(getWorkDir());
        FileSystem fs = bm.store(Arrays.asList(new URL[] {xfs.getXmlUrl()}));
        return new FileSystem[] {fs};
    }

    protected void destroyFileSystem(String testName) throws IOException {
    }

    private File getWorkDir() {
        String workDirProperty = System.getProperty("workdir");//NOI18N
        workDirProperty = (workDirProperty != null) ? workDirProperty : System.getProperty("java.io.tmpdir");//NOI18N                 
        return new File(workDirProperty);
    }    
}
