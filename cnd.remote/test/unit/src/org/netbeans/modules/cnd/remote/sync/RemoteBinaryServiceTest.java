/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.cnd.remote.sync;

import org.netbeans.modules.remote.api.RemoteBinaryService.RemoteBinaryID;
import org.netbeans.modules.cnd.remote.support.*;
import java.io.File;
import junit.framework.Test;
import org.netbeans.modules.remote.api.RemoteBinaryService;
import org.netbeans.modules.cnd.remote.RemoteDevelopmentTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 * @author Vladimir Kvashin
 */
public class RemoteBinaryServiceTest extends RemoteTestBase {

    public RemoteBinaryServiceTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    @org.netbeans.api.annotations.common.SuppressWarnings("RV")
    public void testBinaryService() throws Exception {

        ExecutionEnvironment execEnv = getTestExecutionEnvironment();

        // setup: create a temp file and copy /bin/ls into it
        RemoteCommandSupport rcs;
        rcs = new RemoteCommandSupport(execEnv, "mktemp");
        assertTrue("mktemp is not successful " + rcs.getErr() + rcs.getFailureReason(), rcs.run() == 0);
        String remotePath = rcs.getOutput();
        assertNotNull(remotePath);
        if (remotePath.endsWith("\n")) {
            remotePath = remotePath.substring(0, remotePath.length() - 1);
        }
        assertTrue(remotePath.length() > 0);
        rcs = new RemoteCommandSupport(execEnv, "cp /bin/ls " + remotePath);
        assertTrue("cp /bin/ls " + remotePath + "is not successful " + rcs.getErr() + rcs.getFailureReason(), rcs.run() == 0);

        String localPath;
        File localFile = null;
        RemoteBinaryServiceImpl.resetDownloadCount();
        int expectedDownloadCount = 1;
        for (int i = 0; i < 5; i++) {
            if (i == 3) {
                localFile.delete();
                expectedDownloadCount++;
            } else if (i == 4) {
                rcs = new RemoteCommandSupport(execEnv, "touch " + remotePath);
                assertTrue("touch " + remotePath + "is not successful " + rcs.getErr() + rcs.getFailureReason(), rcs.run() == 0);
                expectedDownloadCount++;
            }

            RemoteBinaryID remoteBinaryID = RemoteBinaryService.getRemoteBinary(execEnv, remotePath);
            assertNotNull(remoteBinaryID);

            Boolean result = RemoteBinaryService.getResult(remoteBinaryID).get();
            assertTrue(result);

            localPath = RemoteBinaryService.getFileName(remoteBinaryID);
            localFile = new File(localPath);
            assertTrue("file doesn't exists " + localFile, localFile.exists());
            assertTrue("file is empty " + localFile, localFile.length() > 0);
            assertEquals("Download Count differs", expectedDownloadCount, RemoteBinaryServiceImpl.getDownloadCount());
        }
    }
    
    public static Test suite() {
        return new RemoteDevelopmentTestSuite(RemoteBinaryServiceTest.class);
    }
}
