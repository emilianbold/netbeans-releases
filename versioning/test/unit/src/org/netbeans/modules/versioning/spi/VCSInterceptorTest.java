/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.spi;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.util.Lookup;
import org.netbeans.modules.versioning.spi.testvcs.TestVCS;
import org.netbeans.modules.versioning.spi.testvcs.TestVCSInterceptor;

/**
 * Versioning SPI unit tests of VCSInterceptor.
 * 
 * @author Maros Sandor
 */
public class VCSInterceptorTest extends TestCase {
    
    private File dataRootDir;
    private TestVCSInterceptor inteceptor;

    public VCSInterceptorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dataRootDir = new File(System.getProperty("data.root.dir"));
        Lookup.getDefault().lookupAll(VersioningSystem.class);
        inteceptor = (TestVCSInterceptor) TestVCS.getInstance().getVCSInterceptor();
        File f = new File(dataRootDir, "workdir/root-test-versioned/deleteme.txt");
        FileObject fo = FileUtil.toFileObject(f);
        if (fo != null) {
            fo.delete();
        }
        inteceptor.clearTestData();
    }

    public void testChangedFile() throws IOException {
        File f = new File(dataRootDir, "workdir/root-test-versioned");
        FileObject fo = FileUtil.toFileObject(f);
        fo = fo.createData("deleteme.txt");
        File file = FileUtil.toFile(fo);
        OutputStream os = fo.getOutputStream();
        os.close();
        assertTrue(inteceptor.getBeforeCreateFiles().contains(file));
        assertTrue(inteceptor.getDoCreateFiles().contains(file));
        assertTrue(inteceptor.getCreatedFiles().contains(file));
        assertTrue(inteceptor.getBeforeChangeFiles().contains(file));
        assertTrue(inteceptor.getAfterChangeFiles().contains(file));
    }
    
    public void testFileProtectedAndNotDeleted() throws IOException {
        File f = new File(dataRootDir, "workdir/root-test-versioned");
        FileObject fo = FileUtil.toFileObject(f);
        fo = fo.createData("deleteme.txt-do-not-delete");
        File file = FileUtil.toFile(fo);
        fo.delete();
        assertTrue(file.isFile());
        assertTrue(inteceptor.getBeforeCreateFiles().contains(file));
        assertTrue(inteceptor.getDoCreateFiles().contains(file));
        assertTrue(inteceptor.getCreatedFiles().contains(file));
        assertTrue(inteceptor.getBeforeDeleteFiles().contains(file));
        assertTrue(inteceptor.getDoDeleteFiles().contains(file));
        assertTrue(inteceptor.getDeletedFiles().contains(file));
    }

    public void testFileCreatedLockedRenamedDeleted() throws IOException {
        File f = new File(dataRootDir, "workdir/root-test-versioned");
        FileObject fo = FileUtil.toFileObject(f);
        fo = fo.createData("deleteme.txt");
        File file = FileUtil.toFile(fo);
        FileLock lock = fo.lock();
        fo.rename(lock, "deleteme", "now");
        lock.releaseLock();
        File file2 = FileUtil.toFile(fo);
        fo.delete();
        assertTrue(inteceptor.getBeforeCreateFiles().contains(file));
        assertTrue(inteceptor.getDoCreateFiles().contains(file));
        assertTrue(inteceptor.getCreatedFiles().contains(file));
        assertTrue(inteceptor.getBeforeEditFiles().contains(file));
        assertTrue(inteceptor.getBeforeMoveFiles().contains(file));
        assertTrue(inteceptor.getAfterMoveFiles().contains(file));
        assertTrue(inteceptor.getBeforeDeleteFiles().contains(file2));
        assertTrue(inteceptor.getDoDeleteFiles().contains(file2));
        assertTrue(inteceptor.getDeletedFiles().contains(file2));
    }
}
