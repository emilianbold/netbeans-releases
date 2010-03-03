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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.RemoteDevelopmentTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.remote.sync.ZipSyncFactory;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.test.If;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileUtil;
/**
 *
 * @author Vladimir Kvashin
 */
public class RfsGnuParameterizedRemoteBuildTestCase extends RemoteBuildTestBase {

    public static final String SECTION = "remote.rfs.build.parameterized";

    public RfsGnuParameterizedRemoteBuildTestCase(String testName) {
        super(testName);
    }

    public RfsGnuParameterizedRemoteBuildTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupHost("rfs");
    }

    private void doTest(String projectKey, String sync, String buildCommand, Level loggersLevel) throws Exception {
        setupHost(sync);
        setLoggersLevel(loggersLevel);
        setDefaultCompilerSet("GNU");
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String projectPath = rcFile.get( SECTION, projectKey);
        assertNotNull(projectPath);
        File projectDirFile = new File(projectPath);
        assertTrue(projectDirFile.exists());
        setupHost(sync);
        changeProjectHost(projectDirFile);
        FileObject projectDirFO = FileUtil.toFileObject(projectDirFile);
        MakeProject makeProject = (MakeProject) ProjectManager.getDefault().findProject(projectDirFO);
        long time = System.currentTimeMillis();
        addPropertyFromRcFile(SECTION, "cnd.remote.timestamps.clear");
        addPropertyFromRcFile(SECTION, "cnd.rfs.preload.sleep");
        addPropertyFromRcFile(SECTION, "cnd.rfs.preload.log");
        addPropertyFromRcFile(SECTION, "cnd.rfs.controller.log");
        addPropertyFromRcFile(SECTION, "cnd.rfs.controller.port");
        addPropertyFromRcFile(SECTION, "cnd.rfs.controller.host");
        buildProject(makeProject, buildCommand, 60*60*24*7, TimeUnit.SECONDS);
        time = System.currentTimeMillis() - time;
        System.err.printf("PROJECT=%s HOST=%s TRANSPORT=%s TIME=%d seconds\n", projectPath, getTestExecutionEnvironment(), sync, time/1000);
    }

    @If(section=SECTION, key = "test.build")
    @ForAllEnvironments(section = SECTION)
    public void testBuildRfsParameterized() throws Exception {
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String sync = rcFile.get(SECTION,"sync", ZipSyncFactory.ID);
        String buildCommand = rcFile.get(SECTION, "build-command", ActionProvider.COMMAND_BUILD);
        doTest("project", sync, buildCommand, Level.ALL);
    }

    @If(section=SECTION, key = "measure.plain.copy")
    @ForAllEnvironments(section = SECTION)
    @org.netbeans.api.annotations.common.SuppressWarnings("OBL")
    public void testPlainCopy() throws Exception {
        setLoggersLevel(Level.OFF);
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String timestampsPath = rcFile.get(SECTION,"timestamps");
        assertNotNull(timestampsPath);
        File timestampsFile = new File(timestampsPath);
        assertTrue(timestampsFile.exists());
        Properties props = new Properties();
        FileInputStream is = new FileInputStream(timestampsFile);
        try {
            props.load(is);
        } finally {
            is.close();
        }
        List<File> files = new ArrayList<File>();
        for (Object key : props.keySet()) {
            assertTrue(key instanceof String);
            String path = (String) key;
            File file = new File(path);
            assertTrue(file.exists());
            files.add(file);
        }
        long time = System.currentTimeMillis();
        ExecutionEnvironment env = getTestExecutionEnvironment();
        RemoteCommandSupport rcs = new RemoteCommandSupport(env, "mktemp");
        assertEquals(0, rcs.run());
        String tmpFile = rcs.getOutput();
        tmpFile = stripLf(tmpFile);
        for (File file : files) {
            Future<Integer> task = CommonTasksSupport.uploadFile(file.getAbsolutePath(), env, tmpFile, 0777, null);
            assertEquals(0, task.get().intValue());
        }
        time = System.currentTimeMillis() - time;
        System.out.printf("FILES PLAIN COPYING TOOK %d ms\n", time);
        // cleanup
        int rc = RemoteCommandSupport.run(env, "rm " + tmpFile);
        assertEquals(0, rc);
    }

    private void setLoggersLevel(Level level) {
        log.setLevel(level);
        org.netbeans.modules.nativeexecution.support.Logger.getInstance().setLevel(level);
    }

    private String stripLf(String text) {
        int pos = text.lastIndexOf('\n');
        if (pos >= 0 && pos == text.length() - 1) {
            return text.substring(0, pos);
        } else {
            return text;
        }
    }

    public static Test suite() {
        return new RemoteDevelopmentTestSuite(RfsGnuParameterizedRemoteBuildTestCase.class);
    }
}
