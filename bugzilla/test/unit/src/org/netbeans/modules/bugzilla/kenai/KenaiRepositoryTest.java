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

package org.netbeans.modules.bugzilla.kenai;

import java.util.List;
import org.netbeans.modules.bugzilla.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.util.KenaiUtil;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.kenai.api.KenaiProject;

/**
 *
 * @author tomas
 */
public class KenaiRepositoryTest extends NbTestCase implements TestConstants {

    public static final String KENAI_REPO_URL = "https://testkenai.com/bugzilla";

    private TaskRepository taskRepository;
    private BugzillaRepositoryConnector brc;
    private TaskRepositoryManager trm;

    public KenaiRepositoryTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
        String username = br.readLine();
        String password = br.readLine();
        br.close();

        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        taskRepository = new TaskRepository("bugzilla", KENAI_REPO_URL);
        AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(username, password);
        taskRepository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);

        trm = new TaskRepositoryManager();
        brc = new BugzillaRepositoryConnector();

        trm.addRepository(taskRepository);
        trm.addRepositoryConnector(brc);

        WebUtil.init();

    }

    public void testIsKenai() throws Throwable {
        KenaiProject prj = KenaiProject.forRepository("https://testkenai.com/svn/golden-project-1~source-code-repository-svn ");
        assertNotNull(prj);

        KenaiSupportImpl support = new KenaiSupportImpl();
        BugzillaRepository repo = (BugzillaRepository) support.createRepository(prj);
        assertNotNull(repo);
        assertTrue(KenaiUtil.isKenai(repo));
    }

    public void testOneProductAfterConfigurationRefresh() throws Throwable {
        KenaiProject prj = KenaiProject.forRepository("https://testkenai.com/svn/golden-project-1~source-code-repository-svn ");
        assertNotNull(prj);

        KenaiSupportImpl support = new KenaiSupportImpl();
        BugzillaRepository repo = (BugzillaRepository) support.createRepository(prj);
        assertNotNull(repo);
        List<String> products = repo.getConfiguration().getProducts();
        assertEquals(1, products.size());
        assertTrue(KenaiUtil.isKenai(repo));
        
        repo.refreshConfiguration();
        products = repo.getConfiguration().getProducts();
        assertEquals(1, products.size());
        
    }

}
