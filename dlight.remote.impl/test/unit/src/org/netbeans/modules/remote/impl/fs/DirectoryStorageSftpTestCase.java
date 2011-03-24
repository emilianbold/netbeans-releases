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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.util.concurrent.Future;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.test.RemoteApiTest;

/**
 *
 * @author Vladimir Kvashin
 */
public class DirectoryStorageSftpTestCase extends RemoteFileTestBase {

    private boolean oldLsViaSftp;
    
    public DirectoryStorageSftpTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        oldLsViaSftp = RemoteDirectory.getLsViaSftp();
        RemoteDirectory.testSetLsViaSftp(true);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        RemoteDirectory.testSetLsViaSftp(oldLsViaSftp);
    }
    
           
    @ForAllEnvironments
    public void testDirectoryStorageSftp() throws Exception {
        File file = File.createTempFile("directoryStorage", ".dat");
        try {
            ConnectionManager.getInstance().connectTo(execEnv);
            Future<StatInfo> res = FileInfoProvider.stat(getTestExecutionEnvironment(), "/usr/include");
            assertNotNull(res);
            StatInfo statInfo = res.get();
            
            DirectoryStorage ds1 = new DirectoryStorage(file);
            final String cacheName = "name.cache";
            DirEntry entry1 = new DirEntrySftp(statInfo, cacheName);
            ds1.testAddEntry(entry1);
            ds1.store();
            DirectoryStorage ds2 = new DirectoryStorage(file);
            ds2.load();
            DirEntry entry2 = ds2.getEntry(entry1.getName());
            assertNotNull("No entry restored for " + entry1.getName(), entry2);
            
            assertEquals("Name", entry1.getName(), entry2.getName());
            assertEquals("Cache", entry1.getCache(), entry2.getCache());
            assertEquals("Access", entry1.getAccessAsString(), entry2.getAccessAsString());
            assertEquals("Size", entry1.getSize(), entry2.getSize());
            assertTrue("Timestamps differ", entry1.isSameLastModified(entry2));
            assertEquals("Link", entry1.getLinkTarget(), entry2.getLinkTarget());
            
        } finally {
            file.delete();
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(DirectoryStorageSftpTestCase.class);
    }
    
}
