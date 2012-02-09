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

package org.netbeans.modules.bugtracking;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.*;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class RepositoryRegistryTest extends NbTestCase {
    

    public RepositoryRegistryTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }   
    
    @Override
    protected void setUp() throws Exception {    
        MockLookup.setLayersAndInstances();
    }

    @Override
    protected void tearDown() throws Exception {   
        RepositoryRegistry.getInstance().flushRepositories();
    }

    public void testEmpty() {
        RepositoryProvider[] repos = RepositoryRegistry.getInstance().getRepositories();
        assertEquals(0, repos.length);
        repos = RepositoryRegistry.getInstance().getRepositories("fake");
        assertEquals(0, repos.length);
    }
    
    public void testAddGetRemove() {
        MyRepository repo = new MyRepository("1");

        // add
        RepositoryRegistry.getInstance().addRepository(repo);
        RepositoryProvider[] repos = RepositoryRegistry.getInstance().getRepositories();
        assertEquals(1, repos.length);
        
        // remove
        RepositoryRegistry.getInstance().removeRepository(repo);
        repos = RepositoryRegistry.getInstance().getRepositories();
        assertEquals(0, repos.length);
    }
    
    public void testWrongConnector() {
        MyRepository repo = new MyRepository("1");

        // add
        RepositoryRegistry.getInstance().addRepository(repo);
        RepositoryProvider[] repos = RepositoryRegistry.getInstance().getRepositories();
        assertEquals(1, repos.length);
    
        repos = RepositoryRegistry.getInstance().getRepositories("fake");
        assertEquals(0, repos.length);
    }
    
    public void testDifferentConnectors() {
        MyRepository repo1c1 = new MyRepository("r1", "c1");
        MyRepository repo2c1 = new MyRepository("r2", "c1");
        MyRepository repo1c2 = new MyRepository("r1", "c2");
        MyRepository repo2c2 = new MyRepository("r2", "c2");

        // add
        RepositoryRegistry.getInstance().addRepository(repo1c1);
        RepositoryRegistry.getInstance().addRepository(repo2c1);
        RepositoryRegistry.getInstance().addRepository(repo1c2);
        RepositoryRegistry.getInstance().addRepository(repo2c2);
        RepositoryProvider[] repos = RepositoryRegistry.getInstance().getRepositories();
        assertEquals(4, repos.length);
        repos = RepositoryRegistry.getInstance().getRepositories("c1");
        assertEquals(2, repos.length);
        assertTrue(Arrays.asList(repos).contains(repo1c1));
        assertTrue(Arrays.asList(repos).contains(repo2c1));
        
        // remove
        RepositoryRegistry.getInstance().removeRepository(repo1c1);
        repos = RepositoryRegistry.getInstance().getRepositories("c1");
        assertEquals(1, repos.length);
        assertTrue(Arrays.asList(repos).contains(repo2c1));
        RepositoryRegistry.getInstance().removeRepository(repo2c1);
        repos = RepositoryRegistry.getInstance().getRepositories("c1");
        assertEquals(0, repos.length);
        
        repos = RepositoryRegistry.getInstance().getRepositories();
        assertEquals(2, repos.length);
        assertTrue(Arrays.asList(repos).contains(repo1c2));
        assertTrue(Arrays.asList(repos).contains(repo2c2));
    }

    public void testListener() {
        MyRepository repo1 = new MyRepository("1");
        MyRepository repo2 = new MyRepository("2");
        class L implements PropertyChangeListener {
            private Collection<RepositoryProvider> newRepos;
            private Collection<RepositoryProvider> oldRepos;
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                oldRepos = (Collection<RepositoryProvider>) evt.getOldValue();
                newRepos = (Collection<RepositoryProvider>) evt.getNewValue();
            }
        };
        L l = new L();
        
        RepositoryRegistry.getInstance().addPropertyChangeListener(l);
        
        // add 1.
        RepositoryRegistry.getInstance().addRepository(repo1);
        assertEquals(0, l.oldRepos.size());
        assertEquals(1, l.newRepos.size());
        assertTrue(l.newRepos.contains(repo1));
        
        // add 2.
        RepositoryRegistry.getInstance().addRepository(repo2);
        assertEquals(1, l.oldRepos.size());
        assertEquals(2, l.newRepos.size());
        assertTrue(l.oldRepos.contains(repo1));
        assertTrue(l.newRepos.contains(repo1));
        assertTrue(l.newRepos.contains(repo2));
        
        // remove 1.
        RepositoryRegistry.getInstance().removeRepository(repo1);
        assertEquals(2, l.oldRepos.size());
        assertEquals(1, l.newRepos.size());
        assertTrue(l.oldRepos.contains(repo1));
        assertTrue(l.oldRepos.contains(repo2));
        assertTrue(l.newRepos.contains(repo2));
        
        // remove 1.
        RepositoryRegistry.getInstance().removeRepository(repo2);
        assertEquals(1, l.oldRepos.size());
        assertEquals(0, l.newRepos.size());
        assertTrue(l.oldRepos.contains(repo2));
        
        // remove listner
        RepositoryRegistry.getInstance().removePropertyChangeListener(l);
        l.newRepos = null;
        l.oldRepos = null;
        RepositoryRegistry.getInstance().addRepository(repo2);
        assertNull(l.newRepos);
        assertNull(l.oldRepos);
    }
    
    public void testStoredRepository() {
        RepositoryInfo info = new RepositoryInfo("repoid", MyConnector.ID, "http://url", null, null, null, null, null, null);
        RepositoryRegistry.getInstance().putRepository(MyConnector.ID, new MyRepository(info));
        
        RepositoryProvider[] repos = RepositoryRegistry.getInstance().getRepositories(MyConnector.ID);
        assertEquals(1, repos.length);
        assertEquals("repoid", repos[0].getInfo().getId());
        assertEquals(MyConnector.ID, repos[0].getInfo().getConnectorId());
    }
    
    private static class MyRepository extends RepositoryProvider {
        private RepositoryInfo info;

        public MyRepository(RepositoryInfo info) {
            this.info = info;
        }

        public MyRepository(String id) {
            this(id, id);
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
        public IssueProvider getIssue(String id) {
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
        public QueryProvider createQuery() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public IssueProvider createIssue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public QueryProvider[] getQueries() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<RepositoryUser> getUsers() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public IssueProvider[] simpleSearch(String criteria) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    @BugtrackingConnector.Registration (
        id=MyConnector.ID,
        displayName="ManagerTestConector",
        tooltip="ManagerTestConector"
    )    
    public static class MyConnector extends BugtrackingConnector {
        static final String ID = "RepoRegistryTestConnector";
        public MyConnector() {
        }

        @Override
        public RepositoryProvider createRepository() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public RepositoryProvider createRepository(RepositoryInfo info) {
            return new MyRepository(info);
        }
    }    
}
