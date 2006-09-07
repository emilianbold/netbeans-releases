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

package org.netbeans.modules.masterfs.filebasedfs;

import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.masterfs.MasterFileObjectTestHid;
import org.netbeans.modules.masterfs.MasterFileSystem;
import org.netbeans.modules.masterfs.MasterFileSystemTest;
import org.netbeans.modules.masterfs.MasterURLMapper;
import org.netbeans.modules.masterfs.providers.AutoMountProvider;
import org.netbeans.modules.masterfs.providers.FileSystemProvider;
import org.netbeans.modules.masterfs.providers.MountSupport;
import org.netbeans.modules.masterfs.providers.ProvidedExtensionsTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileObjectTestHid;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemFactoryHid;
import org.openide.filesystems.FileSystemTestHid;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileUtilTestHidden;
import org.openide.filesystems.URLMapperTestHidden;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author rmatous
 */
public class FileBasedFileSystemTest extends FileSystemFactoryHid {            
    public FileBasedFileSystemTest(Test test) {
        super(test);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {FileBasedURLMapper.class});
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(FileSystemTestHid.class);
        suite.addTestSuite(FileObjectTestHid.class);
        suite.addTestSuite(URLMapperTestHidden.class);
        suite.addTestSuite(FileUtilTestHidden.class);                
        return new FileBasedFileSystemTest(suite);
    }
        
    private File getWorkDir() {
        String workDirProperty = System.getProperty("workdir");//NOI18N
        workDirProperty = (workDirProperty != null) ? workDirProperty : System.getProperty("java.io.tmpdir");//NOI18N
        return new File(workDirProperty);
    }
            
    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {
        FileBasedFileSystem.reinitForTests();
        FileObject workFo = FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(workFo);
        for (int i = 0; i < resources.length; i++) {
            String res = resources[i];
            if (res.endsWith("/")) {
                assertNotNull(FileUtil.createFolder(workFo,res));
            } else {
                assertNotNull(FileUtil.createData(workFo,res));
            }
        }
        return new FileSystem[]{workFo.getFileSystem()};
    }
    
    protected void destroyFileSystem(String testName) throws IOException {}    

    protected String getResourcePrefix(String testName, String[] resources) {
        return getWorkDir().getAbsolutePath();
    }
}
