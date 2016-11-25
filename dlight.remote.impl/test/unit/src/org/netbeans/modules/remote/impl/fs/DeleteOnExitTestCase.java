/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.remote.impl.fs;

import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 *
 * @author vkvashin
 */
public class DeleteOnExitTestCase extends RemoteFileTestBase {

    public DeleteOnExitTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testDeleteOnExit() throws Exception {
        String dir = null;
        try {
            dir = mkTempAndRefreshParent(true);
            RemoteFileObject dirFO = (RemoteFileObject) getFileObject(dir);
            
            String file1 = "file1.dat";
            String path1 = dir + '/' + file1;
            runScript("echo xxx > " + path1);
            FileObject fo1 = getFileObject(dirFO, file1);
            
            String file2 = "file2.dat";
            String path2 = dir + '/' + file2;
            runScript("echo xxx > " + path2);
            
            ProcessUtils.ExitStatus status = ProcessUtils.execute(execEnv, "ls", path1, path2);
            assertTrue("Error creating temp files", status.isOK());
            
            dirFO.getFileSystem().deleteOnExit(path1);
            dirFO.getFileSystem().deleteOnExit(path2);

            reconnect(true);
            sleep(200);
            assertExec("Files should be removed", false, 500, 40, "ls", path1, path2);
        } finally {
            removeRemoteDirIfNotNull(dir);
        }
    }

    @ForAllEnvironments
    public void testCreateTempFileDeleteOnExit() throws Exception {
        String dir = null;
        try {
            dir = mkTempAndRefreshParent(true);
            RemoteFileObject dirFO = (RemoteFileObject) getFileObject(dir);
            FileObject tmpFO = dirFO.getFileSystem().createTempFile(dirFO, "tmp", "tmp", true);            
            String path1 = tmpFO.getPath();            
            reconnect(true);
            assertExec("Files should be removed", false, 500, 40, "ls", path1);
        } finally {
            removeRemoteDirIfNotNull(dir);
        }
    }
    
    private void assertExec(String failureMessage, boolean expectSuccess, int timeout, int attempts, String cmd, String...args) {
        for (int i = 0; i < attempts; i++) {
            ProcessUtils.ExitStatus status = ProcessUtils.execute(execEnv, cmd, args);
            if (status.isOK() == expectSuccess) {
                return;
            }
            sleep(timeout);
        }        
        assertTrue(failureMessage, false);
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(DeleteOnExitTestCase.class);
    }
}
