/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public class ServerRegistryTest extends NbTestCase {

    public ServerRegistryTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);

        FileObject servers = Repository.getDefault().getDefaultFileSystem().getRoot()
                .getFileObject(ServerRegistry.SERVERS_PATH);
        FileObject testProvider = FileUtil.createData(servers, "TestProvider1.instance"); // NOI18N

        testProvider.setAttribute("instanceOf", ServerInstanceProvider.class.getName()); // NOI18N
        testProvider.setAttribute("instanceCreate", new TestInstanceProvider()); // NOI18N
    }

    public void testEmptyProvider() throws IOException {
        assertEquals(1, ServerRegistry.getInstance().getProviders().size());

        ServerInstanceProvider provider = ServerRegistry.getInstance().getProviders().iterator().next();
        assertTrue(provider instanceof TestInstanceProvider);
        ((TestInstanceProvider) provider).clean();
        assertTrue(provider.getInstances().isEmpty());
    }

    @SuppressWarnings("unchecked")
    public void testInstanceProvider() throws IOException {
        assertEquals(1, ServerRegistry.getInstance().getProviders().size());

        ServerInstanceProvider provider = ServerRegistry.getInstance().getProviders().iterator().next();
        assertTrue(provider instanceof TestInstanceProvider);
        TestInstanceProvider testProvider = (TestInstanceProvider) provider;
        testProvider.clean();

        TestInstance instance1 = TestInstance.createInstance(testProvider);
        TestInstance instance2 = TestInstance.createInstance(testProvider);

        List<ServerInstance> step1 = new ArrayList<ServerInstance>();
        Collections.addAll(step1, instance1.getApiInstance());
        List<ServerInstance> step2 = new ArrayList<ServerInstance>();
        Collections.addAll(step2, instance1.getApiInstance(), instance2.getApiInstance());

        InstanceListener listener = new InstanceListener(step1, step2,
                step1, Collections.<ServerInstance>emptyList());
        ServerRegistry.getInstance().addChangeListener(listener);

        testProvider.addInstance(instance1.getApiInstance());
        testProvider.addInstance(instance2.getApiInstance());
        testProvider.removeInstance(instance2.getApiInstance());
        testProvider.removeInstance(instance1.getApiInstance());
    }

    private static class InstanceListener implements ChangeListener {

        private final List<List<ServerInstance>> steps = new ArrayList<List<ServerInstance>>();

        private int stepIndex;

        public InstanceListener(List<ServerInstance>... steps) {
            Collections.addAll(this.steps, steps);
        }

        public void stateChanged(ChangeEvent e) {
            final ServerRegistry registry = (ServerRegistry) e.getSource();

            List<ServerInstance> current = new ArrayList<ServerInstance>();
            for (ServerInstanceProvider provider : registry.getProviders()) {
                current.addAll(provider.getInstances());
            }

            List<ServerInstance> expected = steps.get(stepIndex++);
            assertEquals(expected.size(), current.size());

            for (ServerInstance instance : expected) {
                current.remove(instance);
            }

            assertTrue(current.isEmpty());
        }

    }

}
