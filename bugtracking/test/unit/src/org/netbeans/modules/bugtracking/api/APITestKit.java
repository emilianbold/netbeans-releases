/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.api;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.BugtrackingFactory;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public class APITestKit {
    private static BugtrackingFactory<APITestRepository, APITestQuery, APITestIssue> factory = 
            new BugtrackingFactory<APITestRepository, APITestQuery, APITestIssue>();
    private static APITestRepository apiRepo;
    private static Repository repo;
    
    public static APITestRepository getAPIRepo() {
        if(apiRepo == null) {
            apiRepo = new APITestRepository(
                        new RepositoryInfo(
                            APITestRepository.ID, 
                            APITestConnector.ID_CONNECTOR, 
                            APITestRepository.URL, 
                            APITestRepository.DISPLAY_NAME, 
                            APITestRepository.TOOLTIP));
        }
        return apiRepo;
    }

    public static Repository getRepo() {
        if(repo == null) {
            repo = APITestKit.createRepository(getAPIRepo());
        }
        return repo;
    }
    
    public static Repository createRepository(APITestRepository repo) {
        return factory.createRepository(
                repo, 
                new APITestRepositoryProvider(), 
                new APITestQueryProvider(),
                new APITestIssueProvider());
    }
    
    public static class APITestQueryProvider extends QueryProvider<APITestQuery, APITestIssue> {

        @Override
        public String getDisplayName(APITestQuery q) {
            return q.getDisplayName();
        }

        @Override
        public String getTooltip(APITestQuery q) {
            return q.getTooltip();
        }

        @Override
        public QueryController getController(APITestQuery q) {
            return q.getController();
        }

        @Override
        public void remove(APITestQuery q) {
            q.remove();
        }

        @Override
        public boolean isSaved(APITestQuery q) {
            return q.isSaved();
        }

        @Override
        public Collection<APITestIssue> getIssues(APITestQuery q) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean contains(APITestQuery q, String id) {
            return q.contains(id);
        }

        @Override
        public void refresh(APITestQuery q) {
            q.refresh();
        }

        @Override
        public void removePropertyChangeListener(APITestQuery q, PropertyChangeListener listener) {
            q.removePropertyChangeListener(listener);
        }

        @Override
        public void addPropertyChangeListener(APITestQuery q, PropertyChangeListener listener) {
            q.addPropertyChangeListener(listener);
        }

    }

    public static class APITestRepositoryProvider extends RepositoryProvider<APITestRepository, APITestQuery, APITestIssue> {

        @Override
        public RepositoryInfo getInfo(APITestRepository r) {
            return r.getInfo();
        }

        @Override
        public Image getIcon(APITestRepository r) {
            return r.getIcon();
        }

        @Override
        public void remove(APITestRepository r) {
            r.remove();
        }

        @Override
        public RepositoryController getController(APITestRepository r) {
            return r.getController();
        }

        @Override
        public Lookup getLookup(APITestRepository r) {
            return r.getLookup();
        }

        @Override
        public void removePropertyChangeListener(APITestRepository r, PropertyChangeListener listener) {
            r.removePropertyChangeListener(listener);
        }

        @Override
        public void addPropertyChangeListener(APITestRepository r, PropertyChangeListener listener) {
            r.addPropertyChangeListener(listener);
        }

        @Override
        public APITestIssue[] getIssues(APITestRepository r, String... ids) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public APITestQuery createQuery(APITestRepository r) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public APITestIssue createIssue(APITestRepository r) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Collection<APITestQuery> getQueries(APITestRepository r) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Collection<APITestIssue> simpleSearch(APITestRepository r, String criteria) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public static class APITestIssueProvider extends IssueProvider<APITestIssue> {

        @Override
        public String[] getSubtasks(APITestIssue data) {
            return data.getSubtasks();
        }

        @Override
        public String getDisplayName(APITestIssue data) {
            return data.getDisplayName();
        }

        @Override
        public String getTooltip(APITestIssue data) {
            return data.getTooltip();
        }

        @Override
        public String getID(APITestIssue data) {
            return data.getID();
        }

        @Override
        public String getSummary(APITestIssue data) {
            return data.getSummary();
        }

        @Override
        public boolean isNew(APITestIssue data) {
            return data.isNew();
        }

        @Override
        public boolean isFinished(APITestIssue data) {
            return data.isFinished();
        }

        @Override
        public boolean refresh(APITestIssue data) {
            return data.refresh();
        }

        @Override
        public void addComment(APITestIssue data, String comment, boolean closeAsFixed) {
            data.addComment(comment, closeAsFixed);
        }

        @Override
        public void attachPatch(APITestIssue data, File file, String description) {
            data.attachPatch(file, description);
        }

        @Override
        public BugtrackingController getController(APITestIssue data) {
            return data.getController();
        }

        @Override
        public void removePropertyChangeListener(APITestIssue data, PropertyChangeListener listener) {
            data.removePropertyChangeListener(listener);
        }

        @Override
        public void addPropertyChangeListener(APITestIssue data, PropertyChangeListener listener) {
            data.addPropertyChangeListener(listener);
        }

    }
}
