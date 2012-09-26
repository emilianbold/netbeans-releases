/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.utils;

import java.io.File;
import java.io.PrintStream;
import org.junit.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileChangeListener;

/**
 *
 * @author Vladimir Voskresensky
 */
public class SuspendableFileChangeListenerTest extends NativeExecutionBaseTestCase {

    public SuspendableFileChangeListenerTest(String name) {
        super(name, ExecutionEnvironmentFactory.getLocal());
    }

    @Test
    public void testNonSuspended1() throws Throwable {
        test1(false);
    }
    
    @Test
    public void testSuspended1() throws Throwable {
        test1(false);
    }

    private void test1(boolean suspend) throws Throwable {
        String[] dirStruct = new String[] {
            "d real_dir_1",
            "d real_dir_1/subdir_1",
            "d real_dir_1/subdir_1/subsub_a",
        };        
        String[] filesStruct1 = new String[]{
            "d real_dir_2",
        };
        String[] filesStruct2 = new String[]{
            "- real_dir_1/file_1",
            "- real_dir_1/file_2"
        };
        FileObject tempFO = mkTempFO("testNonSuspended", "tmp");
        final File tempFile = FileUtil.toFile(tempFO);
        try {
            createDirStructure(getTestExecutionEnvironment(), tempFO.getPath(), dirStruct);
            File workDir = getWorkDir();
            File testLog = new File(workDir, "test.dat");
            File referenceLog = new File(workDir, "reference.dat");   
            FileChangeListener golden = new DumpingFileChangeListener(getName(), "", new PrintStream(referenceLog), true);
            SuspendableFileChangeListener suspendableListener = new SuspendableFileChangeListener(new DumpingFileChangeListener(getName(), "", new PrintStream(testLog), true));
            if (suspend) {
                suspendableListener.suspendRemoves();
            }
            FileSystemProvider.addRecursiveListener(golden, tempFO.getFileSystem(), tempFO.getPath());
            FileSystemProvider.addRecursiveListener(suspendableListener, tempFO.getFileSystem(), tempFO.getPath());

            createDirStructure(getTestExecutionEnvironment(), tempFO.getPath(), filesStruct1, false);
            createDirStructure(getTestExecutionEnvironment(), tempFO.getPath(), filesStruct2, false);
            FileUtil.refreshFor(tempFile);
            if (suspend) {
                suspendableListener.resumeRemoves();
            }
            suspendableListener.flush();
            
            printFile(referenceLog, "referenceLog ", System.out);
            printFile(testLog, "testLog", System.out);
            File diff = new File(workDir, "diff.diff");
            try {
                assertFile("Wrapped and Golden events differ, see diff " + testLog.getAbsolutePath() + " " + referenceLog.getAbsolutePath(), testLog, referenceLog, diff);
            } catch (Throwable ex) {
                if (diff.exists()) {
                    printFile(diff, null, System.err);
                }
                throw ex;
            }     
        } finally {
            removeDirectory(tempFile);
//            tempFO.delete();
        }
    }
    
    protected FileObject mkTempFO(String prefix, String suffux) throws Exception {
        File tempFile = File.createTempFile(prefix, suffux);
        tempFile.delete();
        tempFile.mkdirs();
        FileObject parentFO = FileUtil.toFileObject(tempFile.getParentFile());
        parentFO.refresh();
        return FileUtil.toFileObject(tempFile);
    }

}
