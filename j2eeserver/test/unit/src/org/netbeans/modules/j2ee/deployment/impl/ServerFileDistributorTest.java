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

package org.netbeans.modules.j2ee.deployment.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.projects.DeploymentTargetImpl;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentChangeDescriptor;
import org.netbeans.tests.j2eeserver.plugin.jsr88.DepManager;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public class ServerFileDistributorTest extends ServerRegistryTestBase {

    private static final String URL = "fooservice:testInstance"; // NOI18N

    public ServerFileDistributorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ServerRegistry registry = ServerRegistry.getInstance();
        Map<String, String> props = new HashMap<String, String>();
        props.put(DepManager.MULTIPLE_TARGETS, "false");
        props.put(DepManager.WORK_DIR, getWorkDirPath());
        registry.addInstance(URL, "user", "password", "TestInstance", true, props); // NOI18N
    }

    @Override
    protected void tearDown() throws Exception {
        ServerRegistry registry = ServerRegistry.getInstance();
        registry.removeServerInstance(URL);
        super.tearDown();
    }

    public void testDistributeOnSaveSimple() throws IOException, ServerException {
        File f = getProjectAsFile(this, "deploytest1");
        Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        provider.setServerInstanceID(URL);


        ServerInstance instance = ServerRegistry.getInstance().getServerInstance(URL);
        DeploymentTargetImpl dtarget = new DeploymentTargetImpl(provider, null);
        TargetServer server = new TargetServer(dtarget);

        ProgressUI ui = new ProgressUI("test", true);
        TargetModule module = null;
        ui.start();
        try {
            module = server.deploy(ui, true)[0];
        } finally {
            ui.finish();
        }

        ServerFileDistributor dist = new ServerFileDistributor(instance, dtarget);

        File testFile = new File(f,
                "build/web/WEB-INF/classes/test/TestServlet.class".replace("/", File.separator));
        DeploymentChangeDescriptor desc = dist.distributeOnSave(module,
                dtarget.getModuleChangeReporter(), Collections.singleton(testFile));

        assertTrue(desc.classesChanged());
        assertTrue(new File(getWorkDir(),
                "testplugin/applications/web/WEB-INF/classes/test/TestServlet.class".replace("/", File.separator)).exists());
    }
}
