/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.pbuild;

import java.io.File;
import java.util.concurrent.TimeUnit;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.RemoteDevelopmentTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Vladimir Kvashin
 */
public class RemoteBuildMakefileTest extends RemoteBuildTestBase {

    public RemoteBuildMakefileTest(String testName) {
        super(testName);
    }

    public RemoteBuildMakefileTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);       
    }

    private void doTest(Sync sync, Toolchain toolchain) throws Exception {
        final ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        try {
            String projectName = "makefile_proj_1_nbproject";
            File origBase = getDataFile(projectName).getParentFile();
            File copiedBase = new File(new File(getWorkDir(), getTestHostName()), origBase.getName());
            copyDirectory(origBase, copiedBase);
            File projectDirFile = new File(copiedBase, projectName);
            changeProjectHost(projectDirFile, getTestExecutionEnvironment());
            setupHost(sync.ID);
            setSyncFactory(sync.ID);
            assertEquals("Wrong sybc factory:", sync.ID,
                    ServerList.get(execEnv).getSyncFactory().getID());

            setDefaultCompilerSet(toolchain.ID);
            assertEquals("Wrong tools collection", toolchain.ID,
                    CompilerSetManager.get(execEnv).getDefaultCompilerSet().getName());

            assertTrue(projectDirFile.exists());

            clearRemoteSyncRoot();
            FileObject projectDirFO = FileUtil.toFileObject(projectDirFile);
            MakeProject makeProject = (MakeProject) ProjectManager.getDefault().findProject(projectDirFO);
            assertNotNull("project is null", makeProject);
            buildProject(makeProject, ActionProvider.COMMAND_BUILD, getSampleBuildTimeout(), TimeUnit.SECONDS);
        } finally {
            ConnectionManager.getInstance().disconnect(execEnv);
        }
    }

    @ForAllEnvironments
    public void testBuildMakefileWithExt_rfs_gnu() throws Exception {
        doTest(Sync.RFS, Toolchain.GNU);
    }

    @ForAllEnvironments
    public void testBuildMakefileWithExt_scp_gnu() throws Exception {
        doTest(Sync.ZIP, Toolchain.GNU);
    }

    @ForAllEnvironments
    public void testBuildMakefileWithExt_ftp_gnu() throws Exception {
        doTest(Sync.FTP, Toolchain.GNU);
    }

    @ForAllEnvironments
    public void testBuildMakefileWithExt_rfs_sunstudio() throws Exception {
        doTest(Sync.RFS, Toolchain.SUN);
    }

    @ForAllEnvironments
    public void testBuildMakefileWithExt_scp_sunstudio() throws Exception {
        doTest(Sync.ZIP, Toolchain.SUN);
    }

    @ForAllEnvironments
    public void testBuildMakefileWithExt_ftp_sunstudio() throws Exception {
        doTest(Sync.FTP, Toolchain.SUN);
    }

    public static Test suite() {
        return new RemoteDevelopmentTestSuite(RemoteBuildMakefileTest.class);
    }
}
