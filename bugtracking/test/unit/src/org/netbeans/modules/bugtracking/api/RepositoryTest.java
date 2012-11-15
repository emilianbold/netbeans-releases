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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.api;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.TestIssue;
import org.netbeans.modules.bugtracking.TestKit;
import org.netbeans.modules.bugtracking.TestQuery;
import org.netbeans.modules.bugtracking.TestRepository;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public class RepositoryTest extends NbTestCase {
    private static final String ID_REPO = "RepositoryTestRepo";
    private static final String ID_CONNECTOR = "RepositoryTestConector";

    public RepositoryTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }   
    
    @Override
    protected void setUp() throws Exception {    
    }

    @Override
    protected void tearDown() throws Exception {   
    }

    public void testQueryListChanged() {
        MyRepository myRepo = new MyRepository(new RepositoryInfo(ID_REPO, ID_CONNECTOR, "http://test", "test", "test"));
        Repository repo = TestKit.getRepository(myRepo).getRepository();
        
        final boolean[] received = new boolean[] {false};
        repo.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if(Repository.EVENT_QUERY_LIST_CHANGED.equals(pce.getPropertyName())) {
                    received[0] = true;
                }
            }
        });
        myRepo.fireQueryChangeEvent();
        assertTrue(received[0]);
    }

    private static class MyRepository extends TestRepository {
        private RepositoryInfo info;

        public MyRepository(RepositoryInfo info) {
            this.info = info;
        }

        public MyRepository(String id) {
            this(id, ID_CONNECTOR);
        }
        
        public MyRepository(String id, String cid) {
            this.info = new RepositoryInfo(id, cid, "http://test", null, null, null, null, null, null);
        }
        
        @Override
        public RepositoryInfo getInfo() {
            return info;
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public Image getIcon() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TestIssue[] getIssues(String[] id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public RepositoryController getController() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TestQuery createQuery() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TestIssue createIssue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<TestQuery> getQueries() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public Collection<TestIssue> simpleSearch(String criteria) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private PropertyChangeSupport support = new PropertyChangeSupport(this);
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) { 
            support.removePropertyChangeListener(listener);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) { 
            support.addPropertyChangeListener(listener);
        }
        
        void fireQueryChangeEvent() {
            support.firePropertyChange(new PropertyChangeEvent(this, Repository.EVENT_QUERY_LIST_CHANGED, null, null));
        }
    }
    
    @BugtrackingConnector.Registration (id=ID_CONNECTOR,displayName=ID_CONNECTOR,tooltip=ID_CONNECTOR)    
    public static class MyConnector extends BugtrackingConnector {
        public MyConnector() {
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public Repository createRepository(RepositoryInfo info) {
            return TestKit.getRepository(new MyRepository(info)).getRepository();
        }

        @Override
        public Repository createRepository() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }        
    
}
