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

package org.netbeans.modules.bugtracking.kenai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.QueryHandle;
import org.netbeans.modules.kenai.ui.spi.QueryResultHandle;
import org.openide.util.Exceptions;

/**
 *
 * @author tomas
 */
public class QueryAccessorTest extends NbTestCase {
    private Kenai kenai;

    public QueryAccessorTest(String arg0) {
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
        try {
            System.setProperty("kenai.com.url","https://testkenai.com");
            kenai = KenaiManager.getDefault().createKenai("testkenai", "https://testkenai.com");
            BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
            String username = br.readLine();
            String password = br.readLine();
            br.close();
            kenai.login(username, password.toCharArray());

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }        
    }

    public void testGetAllUnseenResult() throws MalformedURLException, CoreException, IOException {
        QueryAccessorImpl qa = new QueryAccessorImpl();
        KenaiProject project = kenai.getProject("koliba");

        QueryHandle h = qa.getAllIssuesQuery(new ProjectHandleImpl(project));
        assertNotNull(h);
        
        List<QueryResultHandle> results = qa.getQueryResults(h);
        assertNotNull(results);
        assertTrue(results.size() > 0);

        for (QueryResultHandle r : results) {
            if(r.getResultType() == QueryResultHandle.ResultType.ALL_CHANGES_RESULT) {
                Integer.parseInt(r.getText()); // just an integer value without text
                return;
            }
        }
        fail("no all changes result");
    }

//    public void testGetQueryResults() throws MalformedURLException, CoreException, IOException {
//        QueryAccessorImpl qa = new QueryAccessorImpl();
//        KenaiProject project = kenai.getProject("koliba");
//
//        List<QueryHandle> queries = qa.getQueries(new ProjectHandleImpl(project));
//        assertNotNull(queries);
//
//        assertTrue(queries.size() >= 2);
//
//        QueryHandle qh = getHandleByName(queries, "All Issues");
//        assertNotNull(qh);
//        List<QueryResultHandle> results = qa.getQueryResults(qh);
//        assertNotNull(results);
//        assertTrue(results.size() >= 1);
//        containsHandle(results, "total");
//
//        qh = getHandleByName(queries, "My Issues");
//        assertNotNull(qh);
//        results = qa.getQueryResults(qh);
//        assertNotNull(results);
//        assertTrue(results.size() >= 1);
//        containsHandle(results, "total");
//
//    }

    private QueryHandle getHandleByName(List<QueryHandle> queries, String name) {
        for (QueryHandle queryHandle : queries) {
            if(queryHandle.getDisplayName().equals(name)) {
                return queryHandle;
            }
        }
        return null;
    }

    private void containsHandle(List<QueryResultHandle> results, String name) {
        for (QueryResultHandle qh : results) {
            if(qh.getText().startsWith(name)) return;
        }
        fail("missing handle " + name);
    }

    private class ProjectHandleImpl extends ProjectHandle {

        private final KenaiProject kp;

        public ProjectHandleImpl(KenaiProject kp) {
            super(kp.getName());
            this.kp = kp;
        }

        @Override
        public String getDisplayName() {
            return kp.getDisplayName();
        }

        @Override
        public boolean isPrivate() {
            try {
                return kp.isPrivate();
            } catch (KenaiException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
        }

        @Override
        public KenaiProject getKenaiProject() {
            return kp;
        }

    }
}
