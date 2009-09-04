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
package org.netbeans.modules.cnd.remote.fs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import org.netbeans.modules.cnd.remote.support.*;
import java.io.InputStreamReader;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 * @author Sergey Grinev
 */
public class RemoteFileSystemTestCase extends RemoteTestBase {

    static {
        System.setProperty("cnd.remote.logger.level", "0");
        System.setProperty("nativeexecution.support.logger.level", "0");
    }

    private final FileSystem fs;
    private final FileObject rootFO;
    private final ExecutionEnvironment execEnv;
    
    public RemoteFileSystemTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
        setupUserDir();
        this.execEnv = execEnv;
        fs = RemoteFileSystemManager.getInstance().get(execEnv);
        assertNotNull("Null remote file system", fs);
        rootFO = fs.getRoot();
        assertNotNull("Null root file object", rootFO);
    }

    private void checkFileExistance(String absPath) throws Exception {
        FileObject fo = rootFO.getFileObject(absPath);
        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo);
        assertFalse("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isVirtual());
    }

    private CharSequence readFile(String absPath) throws Exception {
        FileObject fo = rootFO.getFileObject(absPath);
        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo);
        assertFalse("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isVirtual());
        InputStream is = fo.getInputStream();
        assertNotNull("Null input stream", is);
        BufferedReader rdr = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rdr.readLine()) != null) {
            sb.append(line);
        }
        return sb;
    }

    private String getFileName(ExecutionEnvironment execEnv, String absPath) {
        return execEnv.toString() + ':' + absPath;
    }

    @ForAllEnvironments
    public void testRemoteStdioH() throws Exception {
        String fileName = "/usr/include/stdio.h";
        checkFileExistance(fileName);
        CharSequence content = readFile(fileName);
        CharSequence text2search = "printf";
        assertTrue("Can not find \"" + text2search + "\" in " + getFileName(execEnv, fileName), 
                CharSequenceUtils.indexOf(content, text2search) >= 0);
    }

    @ForAllEnvironments
    public void testInexistance() throws Exception {
        String path = "/dev/qwe/asd/zxc";
        FileObject fo = rootFO.getFileObject(path);
        assertTrue("File " + getFileName(execEnv, path) + " does not exist, but is reported as existent",
                fo == null || fo.isVirtual());
    }

    
    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteFileSystemTestCase.class);
    }

    private void setupUserDir() {
        File dataDir = getDataDir();
        File dataDirParent = dataDir.getParentFile();
        File userDir = new File(dataDirParent, "userdir");
        userDir.mkdirs();
        System.setProperty("netbeans.user", userDir.getAbsolutePath());
    }
}
