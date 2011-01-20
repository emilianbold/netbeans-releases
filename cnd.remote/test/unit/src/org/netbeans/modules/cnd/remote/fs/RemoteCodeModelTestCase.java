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

package org.netbeans.modules.cnd.remote.fs;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.remote.test.RemoteBuildTestBase;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteCodeModelTestCase extends RemoteBuildTestBase {

    private boolean testReconnect = false;
    private boolean trace = Boolean.getBoolean("cnd.test.remote.code.model.trace");
    static {
        System.setProperty("apt.trace.resolver", "true");
    }

    public RemoteCodeModelTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        startupModel();
        System.setProperty("cnd.mode.unittest", "true");
        System.setProperty("org.netbeans.modules.cnd.apt.level","OFF"); // NOI18N
        Logger.getLogger("org.netbeans.modules.editor.settings.storage.Utils").setLevel(Level.SEVERE);
    }

    private void startupModel() {
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
        model.startup();
        ModelSupport.instance().startup();
        RepositoryUtils.cleanCashes();
    }

    private void trace(String pattern, Object... args) {
        if (trace) {
            System.err.printf(pattern, args);
        }
    }

    protected void checkIncludes(CsmFile csmFile, boolean recursive, Set<CsmFile> antiLoop) throws Exception {
        if (!antiLoop.contains(csmFile)) {
            antiLoop.add(csmFile);
            trace("Checking %s\n", csmFile.getAbsolutePath());
            for (CsmInclude incl : csmFile.getIncludes()) {
                CsmFile includedFile = incl.getIncludeFile();
                trace("\t%s -> %s\n", incl.getIncludeName(), includedFile);
                assertNotNull("Unresolved include: " + incl.getIncludeName() + " in " + csmFile.getAbsolutePath(), includedFile);
                if (recursive) {
                    checkIncludes(includedFile, true, antiLoop);
                }
            }
        }
    }

    protected void checkIncludes(CsmProject csmProject, boolean recursive) throws Exception {
        for (CsmFile csmFile : csmProject.getAllFiles()) {
            checkIncludes(csmFile, recursive, new HashSet<CsmFile>());
        }
    }

    @Override
    protected void clearRemoteSyncRoot() {
        super.clearRemoteSyncRoot();
        if (testReconnect) {
            ConnectionManager.getInstance().disconnect(getTestExecutionEnvironment());
        }
    }
    
    protected void processSample(Toolchain toolchain, String sampleName, String projectDirBase) throws Exception {
        final ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        MakeProject makeProject = prepareSampleProject(Sync.RFS, toolchain, sampleName, projectDirBase);
        if (testReconnect) {
            assertFalse("Host should be disconnected at this point", ConnectionManager.getInstance().isConnectedTo(execEnv));
        } else {
            assertTrue("Host should be connected at this point", ConnectionManager.getInstance().isConnectedTo(execEnv));
        }
        OpenProjects.getDefault().open(new Project[]{ makeProject }, false);
        changeProjectHost(makeProject, execEnv);
        CsmModel model = CsmModelAccessor.getModel();
        NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
        assertNotNull("Null NativeProject", np);
        ((ModelImpl) model).enableProject(np);
        CsmProject csmProject = model.getProject(makeProject);
        assertNotNull("Null CsmProject", csmProject);
        csmProject.waitParse();
        checkIncludes(csmProject, true);
        if (testReconnect) {
            ConnectionManager.getInstance().connectTo(execEnv);
            assertTrue("Can not reconnect to host", ConnectionManager.getInstance().isConnectedTo(execEnv));
        }
    }

    @ForAllEnvironments
    public void testArgumentsGNU() throws Exception {
        testReconnect = false;
        processSample(Toolchain.GNU, "Arguments", "Args_01");
    }

    @ForAllEnvironments
    public void testArgumentsSolStudio() throws Exception {
        testReconnect = false;
        processSample(Toolchain.SUN, "Arguments", "Args_02");
    }

    @ForAllEnvironments
    public void testQuoteGNU() throws Exception {
        testReconnect = false;
        processSample(Toolchain.GNU, "Quote", "Quote_01");
    }

    @ForAllEnvironments
    public void testQuoteSolStudio() throws Exception {
        testReconnect = false;
        processSample(Toolchain.SUN, "Quote", "Quote_02");
    }

//    @ForAllEnvironments
//    public void testArgumentsOffline() throws Exception {
//        testReconnect = true;
//        processSample(Toolchain.GNU, "Arguments", "Args_03");
//    }

    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteCodeModelTestCase.class);
    }
}
