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
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class RefreshNonInstantiatedTestCase extends RemoteFileTestBase {

    public RefreshNonInstantiatedTestCase(String testName) {
        super(testName);
    }
    
    public RefreshNonInstantiatedTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    private void recurse(FileObject fo) {
        for (FileObject child : fo.getChildren()) {
            recurse(child);
        }
    }

    /**
     * Check that when refreshing a directory
     * no unnecessary children are instantiated
     */
    @ForAllEnvironments
    public void testRefreshNonInstantiated() throws Exception {
        String baseDir = null;
        try {
            RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, false);
            baseDir = mkTempAndRefreshParent(true);
            RemoteFileObject baseFO = getFileObject(baseDir);
            baseFO.refresh();
            recurse(baseFO);

            String[] struct = new String[] {
                "d real_dir_1",
                "d real_dir_1/subdir_1",
                "d real_dir_1/subdir_1/subsub_a",
                "d real_dir_1/subdir_1/subsub_a/subsubsub_x",
                "d real_dir_1/subdir_1/subsub_a/subsubsub_y",
                "d real_dir_1/subdir_1/subsub_a/subsubsub_z",
                "d real_dir_1/subdir_1/subsub_b",
                "d real_dir_1/subdir_2",
                "d real_dir_1/subdir_2/subsub_c",
                "d real_dir_1/subdir_2/subsub_d"
            };
            createDirStructure(execEnv, baseDir, struct);
            
            int dirSyncCount_1 = fs.getDirSyncCount();
            int cacheSize_1 = fs.getCachedFileObjectsCount();

            baseFO.refresh();

            int dirSyncCount_2 = fs.getDirSyncCount();
            int cacheSize_2 = fs.getCachedFileObjectsCount();

            System.err.printf("Cache size: %d -> %d   Dir sync count: %d -> %d\n", cacheSize_1, cacheSize_2, dirSyncCount_1, dirSyncCount_2);

            assertEquals("Wrong dir sync count:", 1, dirSyncCount_2 - dirSyncCount_1);
            assertEquals("Wrong cache size increment:", 1, cacheSize_2 - cacheSize_1);
        } finally {
            if (baseDir != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), "chmod", "-R", "700", baseDir);
                removeRemoteDirIfNotNull(baseDir);
            }
        }        
    }
    
    public static Test suite() {
        return RemoteApiTest.createSuite(RefreshNonInstantiatedTestCase.class);
    }
}
