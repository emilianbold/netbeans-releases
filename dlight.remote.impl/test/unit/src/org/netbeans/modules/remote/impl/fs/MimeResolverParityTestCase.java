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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 * @author Vladimir Kvashin
 */
public class MimeResolverParityTestCase extends RemoteFileTestBase {

    private String longestDirName = "";
    private long longestDirTime = -1;
    
    public MimeResolverParityTestCase(String testName) throws IOException {
        super(testName, createLocalEnvAsRemote());
    }

    private static ExecutionEnvironment createLocalEnvAsRemote() throws IOException {
        String mspec = "local-as-remote";
        ExecutionEnvironment env = NativeExecutionTestSupport.getTestExecutionEnvironment(mspec);
        return env;
        
    }

    private void doTestMimeResolvers2(FileObject baseDirFO, PrintStream out, boolean recursive, Set<String> antiLoop) throws Exception {
        String canonicalPath = FileSystemProvider.getCanonicalPath(baseDirFO);
        if (antiLoop.contains(canonicalPath)) {
            return;
        } else {
            antiLoop.add(canonicalPath);
        }        
        FileObject[] children = baseDirFO.getChildren();
        Arrays.sort(children, new Comparator<FileObject>() {
            public int compare(FileObject o1, FileObject o2) {
                return o1.getNameExt().compareTo(o2.getNameExt());
            }
            
        });
        long dirTime = System.currentTimeMillis();
        long maxFileTime = 0;
        String maxFileName = "";
        for (FileObject child : children) {
            long fileTime = System.currentTimeMillis();
            String mimeType = child.getMIMEType();
            fileTime = System.currentTimeMillis() - fileTime;
            if (fileTime > maxFileTime) {
                maxFileTime = fileTime;
                maxFileName = child.getNameExt();
            }
            out.printf("%s %s\n", child.getPath(), mimeType);
        }
        dirTime = System.currentTimeMillis() - dirTime;
        System.err.printf("Getting MIME types for %s took %d ms, slowest file %s - %d ms\n", baseDirFO, dirTime, maxFileName, maxFileTime);
        if (dirTime > longestDirTime) {
            longestDirTime = dirTime;
            longestDirName = baseDirFO.getPath();
        }
        if (recursive) {
            for (FileObject child : children) {
                if (child.isFolder()) {
                    doTestMimeResolvers2(child, out, true, antiLoop);
                }
            }
        }
    }
    
    private void doTestMimeResolvers1(String baseDir, boolean recursive) throws Throwable {
        try {
            System.err.printf("========== Testing MIME types in %s started\n", baseDir);
            FileObject remoteBaseDirFO = getFileObject(baseDir);
            FileObject localBaseDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(new File(baseDir)));            
            assertNotNull(localBaseDirFO);
            File workDir = getWorkDir();
            File remoteLogFile = new File(workDir, "remote.dat");
            File localLogFile = new File(workDir, "local.dat");
            PrintStream remoteLog = new PrintStream(remoteLogFile);
            PrintStream localLog = new PrintStream(localLogFile);
            long time;
            
            time = System.currentTimeMillis();
            doTestMimeResolvers2(localBaseDirFO, localLog, recursive, new HashSet<String>());
            time = System.currentTimeMillis() - time;
            System.err.printf("========== Getting LOCAL MIME types for %s took %d ms, slowest dir %s - %d ms\n", baseDir, time, longestDirName, longestDirTime);
            
            time = System.currentTimeMillis();
            doTestMimeResolvers2(remoteBaseDirFO, remoteLog, recursive, new HashSet<String>());
            time = System.currentTimeMillis() - time;
            System.err.printf("========== Getting REMOTE MIME types for %s took %d ms, slowest dir %s - %d ms\n", baseDir, time, longestDirName, longestDirTime);
            
            localLog.close();
            remoteLog.close();
            printFile(localLogFile, "LOCAL ", System.out);
            printFile(remoteLogFile, "REMOTE", System.out);
            File diff = new File(workDir, "diff.diff");
            try {
                assertFile("Remote and local mime types differ, see diff " + remoteLogFile.getAbsolutePath() + " " + localLogFile.getAbsolutePath(), remoteLogFile, localLogFile, diff);
            } catch (Throwable ex) {
                if (diff.exists()) {
                    printFile(diff, null, System.err);
                }
                throw ex;
            }
        } finally {
            System.err.printf("========== Testing MIME types in %s done\n", baseDir);
        }
    }
    
    public void testMimeResolversUsr() throws Throwable {
        doTestMimeResolvers1("/usr", true);
    }

    public void testMimeResolversBin() throws Throwable {
        doTestMimeResolvers1("/bin", true);
    }
    
    public void testMimeResolversExportHome() throws Throwable {
        doTestMimeResolvers1("/export/home", true);
    }

    public void testMimeResolversHome() throws Throwable {
        doTestMimeResolvers1("/home", true);
    }
    
    public void testMimeResolversOpt() throws Throwable {
        doTestMimeResolvers1("/opt", true);
    }
    
    public void testMimeResolversSbin() throws Throwable {
        doTestMimeResolvers1("/sbin", true);
    }

    public static junit.framework.Test suite() {
        return RemoteApiTest.createSuite(MimeResolverParityTestCase.class);
    }
}
