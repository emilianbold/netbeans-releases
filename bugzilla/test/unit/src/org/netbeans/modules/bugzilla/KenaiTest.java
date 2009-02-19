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

package org.netbeans.modules.bugzilla;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;

/**
 *
 * @author tomas
 */
public class KenaiTest extends NbTestCase implements TestConstants {

    public static final String REPO_PASSWD  = "YOUR_USERNAME";
    public static final String REPO_URL     = "https://kenai.com/bugzilla";
    public static final String REPO_USER    = "YOUR_PASSWORD";

    private TaskRepository repository;
    private BugzillaRepositoryConnector brc;
    private TaskRepositoryManager trm;

    public KenaiTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        repository = new TaskRepository("bugzilla", REPO_URL);
        AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(REPO_USER, REPO_PASSWD);
        repository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
        repository.setCredentials(AuthenticationType.HTTP, authenticationCredentials, false);

        trm = new TaskRepositoryManager();
        brc = new BugzillaRepositoryConnector();

        trm.addRepository(repository);
        trm.addRepositoryConnector(brc);

        WebUtil.init();

    }

    public void testValidate () throws Throwable {
        try {
            brc.getClientManager().getClient(repository, NULL_PROGRESS_MONITOR).validate(NULL_PROGRESS_MONITOR);
        } catch (Exception ex) {
            TestUtil.handleException(ex);
        }
    }

    public void testQuery() throws Throwable {
        try {

            String dateString = repository.getSynchronizationTimeStamp();
            if (dateString == null) {
                dateString = "";
            }

            String url = "/buglist.cgi?query_format=advanced&product=koliba";
            IRepositoryQuery query = new RepositoryQuery(repository.getConnectorKind(), "");
            query.setUrl(url);
            final List<TaskData> collectedData = new ArrayList<TaskData>();
            TaskDataCollector collector = new TaskDataCollector() {
                public void accept(TaskData taskData) {
                    collectedData.add(taskData);
                }
            };
            brc.performQuery(repository, query, collector, null, NULL_PROGRESS_MONITOR);
            assertTrue(collectedData.size() > 0);

        } catch (Exception e) {
            TestUtil.handleException(e);
        }
    }

}
