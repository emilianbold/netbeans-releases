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
package org.netbeans.modules.remotefs.versioning.spi;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.versioning.VCSFilesystemTestFactory;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public class RemoteVCSTCKTest extends VCSFilesystemTestFactory {

    private ExecutionEnvironment execEnv = null;
    private String tmpDir;

    public RemoteVCSTCKTest(Test test) {
        super(test);
        //VCSFileProxy.createFileProxy(new File("")); // init APIAccessor
    }

    @Override
    protected FileObject createFileObject(String path) throws IOException {
        FileObject fo = FileSystemProvider.getFileObject(execEnv, path);
        if (fo != null && fo.isValid()) {
            return fo;
        }
        String dir = PathUtilities.getDirName(path);
        FileObject dirFO = FileSystemProvider.getFileObject(execEnv, dir);
        fo = dirFO.createData(PathUtilities.getBaseName(path));
        return fo;
    }

    @Override
    protected void setReadOnly(FileObject fo) throws IOException {
        File f = FileUtil.toFile(fo);
        assertNotNull(f);
        f.setReadOnly();
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        //suite.addTestSuite(VCSOwnerTestCase.class);
        //suite.addTestSuite(VCSInterceptorTestCase.class);
        //suite.addTestSuite(VCSAnnotationProviderTestCase.class);
        return new RemoteVCSTCKTest(suite);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("nativeexecution.mode.unittest", "true");
        
        String userdir = System.getProperty("netbeans.user");
        if (userdir == null) {
            System.setProperty("netbeans.user", System.getProperty("nbjunit.workdir"));
        }
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String mspec = null;
        for(String section : rcFile.getSections()) {
            if (section.equals("remote.platforms")) {
                Collection<String> keys = rcFile.getKeys(section);
                for(String key : keys) {
                    if (key.equals("intel-S2")) {
                        String get = rcFile.get(section, key, null);
                        if (get == null) {
                            mspec = key;
                        }
                    }
                }
            }
        }
        
        execEnv = NativeExecutionTestSupport.getTestExecutionEnvironment(mspec);
        ConnectionManager.getInstance().connectTo(execEnv);
        //RemoteFileSystemManager.getInstance().resetFileSystem(execEnv);
        //RemoteFileSystem rfs = RemoteFileSystemManager.getInstance().getFileSystem(execEnv);
        tmpDir = mkTemp(execEnv, true);
        String tmpDirParent = PathUtilities.getDirName(tmpDir);
        FileObject tmpDirParentFO = FileSystemProvider.getFileObject(execEnv, tmpDirParent); 
        if (tmpDirParentFO == null) {
            throw new IOException("Null file object for " + tmpDirParent);
        }
        tmpDirParentFO.refresh();
        FileObject fileObject = FileSystemProvider.getFileObject(execEnv, tmpDir);
        if (tmpDirParentFO == null) {
            throw new IOException("Null file object for " + tmpDir);
        }
        VCSFileProxy.createFileProxy(fileObject); // init APIAccessor
    }
    
    private String mkTemp(ExecutionEnvironment execEnv, boolean directory) throws Exception {        
        String[] mkTempArgs;
        if (HostInfoUtils.getHostInfo(execEnv).getOSFamily() == HostInfo.OSFamily.MACOSX) {
            mkTempArgs = directory ? new String[] { "-t", "/tmp", "-d" } : new String[] { "-t", "/tmp" };
        } else {
            mkTempArgs = directory ? new String[] { "-d" } : new String[0];
        }        
        ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "mktemp", mkTempArgs);
        assertEquals("mktemp failed: " + res.error, 0, res.exitCode);
        return res.output;
    }    

    @Override
    protected void tearDown() throws Exception {     
        if(!workDirPath.isEmpty()) {
            File f = new File(workDirPath);
            deleteRecursively(f);
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }    
}
