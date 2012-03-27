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
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 * @author Vladimir Kvashin
 */
public class RefreshTestCase_IZ_210125 extends RemoteFileTestBase {

    public RefreshTestCase_IZ_210125(String testName) {
        super(testName);
    }
    
    public RefreshTestCase_IZ_210125(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }


    @ForAllEnvironments
    public void test_iz_210125() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            RemoteFileObject baseFO = getFileObject(baseDir);
            FileObject dirFO1 = FileUtil.createFolder(baseFO, "subdir_1");
            FileObject fileFO1 = FileUtil.createData(dirFO1, "file_1");
            String dirPath1 = dirFO1.getPath();
            String filePath1 = fileFO1.getPath();
            RemoteFileSystemManager.getInstance().getFileSystem(execEnv).getRefreshManager().testWaitLastRefreshFinished();
            RemoteFileSystemManager.getInstance().resetFileSystem(execEnv);
            sleep(1000); // just in case
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", "rm -rf " + dirPath1);
            char[] passwd = PasswordManager.getInstance().getPassword(execEnv);
            ConnectionManager.getInstance().disconnect(execEnv);
            sleep(1000); // just in case
            PasswordManager.getInstance().storePassword(execEnv, passwd, false);
            fs = RemoteFileSystemManager.getInstance().getFileSystem(execEnv);
            assertNotNull("Null remote file system", fs);
            rootFO = fs.getRoot();
            ConnectionManager.getInstance().connectTo(execEnv);
            //sleep(1000); // just in case
            RemoteFileSystemManager.getInstance().getFileSystem(execEnv).getRefreshManager().testWaitLastRefreshFinished();
            baseFO = getFileObject(baseDir);
            dirFO1 = rootFO.getFileObject(dirPath1);
            fileFO1 = rootFO.getFileObject(filePath1);
            if (dirFO1 != null || fileFO1 != null) {
                sleep(1000);
                RemoteFileSystemManager.getInstance().getFileSystem(execEnv).getRefreshManager().testWaitLastRefreshFinished();
                dirFO1 = rootFO.getFileObject(dirPath1);
                fileFO1 = rootFO.getFileObject(filePath1);
                assertNull(dirFO1);
                assertNull(fileFO1);
            }
        } finally {
            if (baseDir != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), "chmod", "-R", "700", baseDir);
                removeRemoteDirIfNotNull(baseDir);
            }
        }        
    }
    
    public static Test suite() {
        return RemoteApiTest.createSuite(RefreshTestCase_IZ_210125.class);
    }
}
