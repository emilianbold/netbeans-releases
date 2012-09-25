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
package org.netbeans.modules.ods.client.mock;

import org.netbeans.modules.ods.client.mock.ODSMockClientFactory;
import org.netbeans.modules.ods.client.mock.ODSMockClient;
import com.tasktop.c2c.server.profile.domain.project.Project;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.libs.ods.jersey.ODSJerseyClient;
import org.netbeans.modules.ods.client.api.ODSFactory;
import org.netbeans.modules.ods.client.api.ODSClient;

/**
 *
 * @author jpeska
 */
public class CloudClientMockTest extends NbTestCase {
    private String url;

    public CloudClientMockTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        url = new File(getDataDir(), "ods-json").getAbsolutePath();
        super.setUp();
    }
    
    public void testMockClient() throws Exception {
        assertTrue(getClient() instanceof ODSMockClient);
    }
    
    public void testGetMyProjects() throws Exception {
        List<Project> projects = getClient().getMyProjects();
        assertNotNull(projects);
        assertEquals(2, projects.size());
    }
    
    public void testGetProjectById () throws Exception {
        Project project = getClient().getProjectById("anagram-game");
        assertNotNull(project);
        assertEquals("anagram-game", project.getIdentifier());
    }
    
    private ODSClient getClient() {
        System.setProperty(ODSMockClientFactory.ID, "true");
        ODSClient client = ODSFactory.getInstance().createClient(url, null);
        assertEquals(ODSMockClient.class, client.getClass());
        return client;
    }
    
    
}
