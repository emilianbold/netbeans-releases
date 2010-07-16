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
package org.netbeans.modules.nativeexecution;

import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import junit.framework.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class CopyTaskTest extends NativeExecutionBaseTestCase {

    public CopyTaskTest(String name) {
        super(name);
    }

    public CopyTaskTest(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(CopyTaskTest.class);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of uploadFile method, of class CopyTask.
     */
    public void __testCopyToLocal() throws Exception {
        System.out.println("copyTo"); // NOI18N
        File srcFile = createTempFile("src", null, false); // NOI18N
        writeFile(srcFile, "123\n456\n789"); // NOI18N
        String dstFileName = "/tmp/trg_x"; // NOI18N

        CharArrayWriter err = new CharArrayWriter();
        Future<Integer> fresult = CommonTasksSupport.uploadFile(
                srcFile.getAbsolutePath(),
                ExecutionEnvironmentFactory.getLocal(),
                dstFileName, 0777, err);

        if (fresult == null) {
            System.out.println("Error: " + err.toString()); // NOI18N
        }

        int result = -1;
        try {
            result = fresult.get();
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println("Done with status " + result); // NOI18N

        if (result != 0) {
            System.out.println("Error: " + err.toString()); // NOI18N
        }
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testCopyToRemote() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        assertNotNull(execEnv);
        File src = createTempFile("test-upload-1", null, false); // NOI18N
        writeFile(src, "qwe/nasd/nzxc"); // NOI18N
        String dst = "/tmp/" + /* execEnv.getUser() + "/" +  */ src.getName(); // NOI18N
        System.err.printf("testUploadFile: %s to %s:%s\n", src.getAbsolutePath(), execEnv.getDisplayName(), dst); // NOI18N
        Future<Integer> upload = CommonTasksSupport.uploadFile(src.getAbsolutePath(), execEnv, dst, 0755, new PrintWriter(System.err));
        int rc = upload.get();
        assertEquals("Error uploading " + src.getAbsolutePath() + " to " + execEnv + ":" + dst, 0, rc);
        assertTrue(HostInfoUtils.fileExists(execEnv, dst));
        Future<Integer> res = CommonTasksSupport.rmFile(execEnv, dst, null);
        assertEquals("Error removing " + execEnv + ":" + dst, 0, res.get().intValue());
    }
}
