/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import junit.framework.Test;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 * @author Vladimir Kvashin
 */
public class RemoteFileSystemTestCase extends RemoteFileTestBase {

    public RemoteFileSystemTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    // Disabled, see IZ 190453
    @RandomlyFails
    public void testRemoteStdioH() throws Exception {
        String absPath = "/usr/include/stdio.h";
        FileObject fo = rootFO.getFileObject(absPath);
        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo);
        assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isValid());
        String content = readFile(fo);
        String text2search = "printf";
        assertTrue("Can not find \"" + text2search + "\" in " + getFileName(execEnv, absPath),
                content.indexOf(text2search) >= 0);
    }
    
    @ForAllEnvironments
    public void testParents() throws Exception {
        String absPath = "/usr/include/sys/time.h";
        FileObject fo = rootFO.getFileObject(absPath);
        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo);
        FileObject p = getParentAssertNotNull(fo); // /usr/include/sys
        p = getParentAssertNotNull(p); // /usr/include
        p = getParentAssertNotNull(p); // /usr
        p = getParentAssertNotNull(p); // /
        assertTrue(p == rootFO);
    }

    private FileObject getParentAssertNotNull(FileObject fo) {
        FileObject parent = fo.getParent(); // /usr/include
        assertNotNull("Null parent for " + fo, parent);
        return parent;
    }

    @ForAllEnvironments
    public void testDifferentPaths() {
        class Pair {
            final String parent;
            final String relative;
            public Pair(String parent, String relative) {
                this.parent = parent;
                this.relative = relative;
            }
        }
        Pair[] pairs = new Pair[] {
            new Pair(null, "/usr/include/stdlib.h"),
            new Pair("/usr", "include/stdlib.h"),
            new Pair("/usr/include", "stdlib.h"),
            new Pair("/usr/lib", "libc" + sharedLibExt),
            new Pair("/usr", "lib/libc" + sharedLibExt),
            new Pair(null, "/usr/lib/libc" + sharedLibExt)
        };
        for (int i = 0; i < pairs.length; i++) {
            Pair pair = pairs[i];
            FileObject parentFO;
            if (pair.parent == null) {
                parentFO = rootFO;
            } else {
                parentFO = rootFO.getFileObject(pair.parent);
                assertNotNull("Null file object for " + pair.parent, parentFO);
            }
            FileObject childFO = parentFO.getFileObject(pair.relative);
            assertNotNull("Null file object for " + pair.relative + " from " + parentFO, childFO);
        }
    }

    @ForAllEnvironments
    public void testSingleFileObject() {
        String absPath = "/usr/include/stdio.h";
        FileObject fo1 = rootFO.getFileObject(absPath);
        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo1);
        assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo1.isValid());
        FileObject fo2 = rootFO.getFileObject(absPath);
        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo2);
        assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo2.isValid());
        assertTrue("Two instances of file objects for " + absPath, fo1 == fo2);
    }

//    @ForAllEnvironments
//    public void testSingleLocalFileObject() {
//        String absPath = "/usr/include/stdio.h";
//        FileObject fo1 = FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal()).findResource(absPath);
//        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo1);
//        assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo1.isValid());
//        FileObject fo2 = FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal()).findResource(absPath);
//        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo2);
//        assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo2.isValid());
//        assertTrue("Two instances of file objects for " + absPath, fo1 == fo2);
//    }

    @ForAllEnvironments
    // Disabled, see IZ 190453
    @RandomlyFails
    public void testMultipleRead() throws Exception {
        removeDirectory(fs.getCache());
        final String absPath = "/usr/include/errno.h";
        long firstTime = -1;
        for (int i = 0; i < 5; i++) {
            long time = System.currentTimeMillis();
            FileObject fo = rootFO.getFileObject(absPath);
            assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo);
            assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isValid());
            InputStream is = fo.getInputStream();
            assertNotNull("Got null input stream for " + getFileName(execEnv, absPath), is);
            is.close();
            time = System.currentTimeMillis() - time;
            System.err.printf("Pass %d; getting input stream for %s took %d ms\n", i, getFileName(execEnv, absPath), time);
            if (i == 0) {
                firstTime = time;
            } else if (time > 0) {
                assertTrue("Getting input stream for "+ getFileName(execEnv, absPath) + "(pass " + (i+1) + ")_ took too long (" +
                        time + ") ms (vs" + firstTime + " ms on 1-st pass", time < firstTime / 8);
            }
        }
    }

    @ForAllEnvironments
    public void testInexistance() throws Exception {
        String path = "/dev/qwe/asd/zxc";
        FileObject fo = rootFO.getFileObject(path);
        assertTrue("File " + getFileName(execEnv, path) + " does not exist, but is reported as existent",
                fo == null || !fo.isValid());
    }

    @ForAllEnvironments
    public void testWrite() throws Exception {
        String tempFile = null;
        try {
            FileObject fo;
            String stdio_h = "/usr/include/stdio.h";
            fo = rootFO.getFileObject(stdio_h);
            assertNotNull("null file object for " + stdio_h, fo);
            assertFalse("FileObject should NOT be writable: " + fo.getPath(), fo.canWrite());
            tempFile = mkTemp();
            fo = rootFO.getFileObject(tempFile);
            assertNotNull("Null file object for " + tempFile, fo);
            assertTrue("FileObject should be writable: " + fo.getPath(), fo.canWrite());
            String content = "a quick brown fox...";
            writeFile(fo, content);
            CharSequence readContent = readFile(fo);
            assertEquals("File content differ", content.toString(), readContent.toString());
            WritingQueue.getInstance(execEnv).waitFinished(null);
            readContent = ProcessUtils.execute(execEnv, "cat", tempFile).output;
            assertEquals("File content differ", content.toString(), readContent.toString());
        } finally {
            if (tempFile != null) {
                CommonTasksSupport.rmFile(execEnv, tempFile, new OutputStreamWriter(System.err));
            }
        }
    }

    @ForAllEnvironments
    public void testReservedWindowsNames() throws Exception {
        String tempDir = null;
        try {
            tempDir = mkTemp(true);
            FileObject tempDirFO = rootFO.getFileObject(tempDir);
            assertNotNull("Null file object for " + tempDir, tempDirFO);
            //assertTrue("FileObject should be writable: " + tempDirFO.getPath(), tempDirFO.canWrite());
            String lpt = "LPT1";
            String withColon = "file:with:colon";
            runScript("cd " + tempDir + "\n" +
                "echo \"123\" > " + lpt + "\n" +
                "echo \"123\" > " + withColon + "\n");
            FileObject lptFO = tempDirFO.getFileObject(lpt);
            assertNotNull("Null file object for " + lpt, lptFO);
            FileObject colonFO = tempDirFO.getFileObject(withColon);
            assertNotNull("Null file object for " + withColon, colonFO);
        } finally {
            if (tempDir != null) {
                CommonTasksSupport.rmFile(execEnv, tempDir, new OutputStreamWriter(System.err));
            }
        }
    }
    
    @ForAllEnvironments
    public void testDate() throws Exception {
        String path = mkTemp();
        Date localDate = new Date();
        FileObject fo = getFileObject(path);
        assertTrue("Invalid file object " + path, fo.isValid());
        Date lastMod = fo.lastModified();
        assertNotNull("getDate() returned null for " + fo, lastMod);
        System.out.println("local file creation date:  " + localDate);
        System.out.println("remote last modified date: " + lastMod);
        // time can differ, so I can't compare it; make sure it's not differ in many days :)
        assertTrue("Dates differ to much: " + localDate + " vs " + lastMod, Math.abs(localDate.getTime() - lastMod.getTime()) < 1000*60*60*24);        
        fo.delete();
        assertTrue("isValid should return false for " + fo, !fo.isValid());
        Date lastMod2 = fo.lastModified();
        System.out.println("remote date after deletion: " + lastMod2);
        assertNotNull("getDate() should never return null", lastMod2);
    }

    
    public static Test suite() {
        return RemoteApiTest.createSuite(RemoteFileSystemTestCase.class);
    }
}
