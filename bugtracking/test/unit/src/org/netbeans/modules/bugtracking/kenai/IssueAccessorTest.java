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

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.LogHandler;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor.IssueHandle;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public class IssueAccessorTest extends NbTestCase {

    public IssueAccessorTest(String arg0) {
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
            Kenai kenai = Kenai.getDefault();
            BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
            String username = br.readLine();
            String password = br.readLine();
            br.close();
            kenai.login(username, password.toCharArray());

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        if(TestConnector.kolibaRepository == null) {
            TestConnector.kolibaRepository = new TestRepository("koliba");
            TestConnector.goldenProjectRepository = new TestRepository("golden-project-1");
        }
    }

    public void testGetRecentIssuesEmptyReturn() throws MalformedURLException, CoreException, IOException {
        KenaiIssueAccessor accessor = getIssueAccessor();
        IssueHandle[] issues = accessor.getRecentIssues();
        assertNotNull(issues);
        assertEquals(0, issues.length);
        issues = accessor.getRecentIssues(getKenaiProject("koliba"));
        assertNotNull(issues);
        assertEquals(0, issues.length);
    }

    public void testGetRecentIssues() throws MalformedURLException, CoreException, IOException {
        TestIssue issue1 = new TestIssue(TestConnector.kolibaRepository, "1");
        TestIssue issue2 = new TestIssue(TestConnector.kolibaRepository, "2");

        KenaiIssueAccessor accessor = getIssueAccessor();

        // add issue1, issue2
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, issue1);
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, issue2);

        // test for golden-project -> nothing is returned
        IssueHandle[] issues = accessor.getRecentIssues(getKenaiProject("golden-project-1"));
        assertNotNull(issues);
        assertEquals(0, issues.length);

        // getIssues for koliba -> issues 1..2 are returned
        issues = accessor.getRecentIssues(getKenaiProject("koliba"));
        assertNotNull(issues);
        assertIssueHandles(issues, new String[] {issue2.getID(), issue1.getID()});

        // getAll -> issues 1..2 are returned
        issues = accessor.getRecentIssues();
        assertNotNull(issues);
        assertIssueHandles(issues, new String[] {issue2.getID(), issue1.getID()});
    }

    public void testGetRecentIssuesTwoReposMoreThan5Each() throws MalformedURLException, CoreException, IOException, InterruptedException {
        KenaiIssueAccessor accessor = getIssueAccessor();

        TestIssue kolibaIssue1 = new TestIssue(TestConnector.kolibaRepository, "koliba1");
        TestIssue kolibaIssue2 = new TestIssue(TestConnector.kolibaRepository, "koliba2");
        TestIssue kolibaIssue3 = new TestIssue(TestConnector.kolibaRepository, "koliba3");
        TestIssue kolibaIssue4 = new TestIssue(TestConnector.kolibaRepository, "koliba4");
        TestIssue kolibaIssue5 = new TestIssue(TestConnector.kolibaRepository, "koliba5");
        TestIssue kolibaIssue6 = new TestIssue(TestConnector.kolibaRepository, "koliba6");
        TestIssue kolibaIssue7 = new TestIssue(TestConnector.kolibaRepository, "koliba7");
        TestIssue goldenProjectIssue1 = new TestIssue(TestConnector.kolibaRepository, "koliba1");
        TestIssue goldenProjectIssue2 = new TestIssue(TestConnector.goldenProjectRepository, "goldenProject2");
        TestIssue goldenProjectIssue3 = new TestIssue(TestConnector.goldenProjectRepository, "goldenProject3");
        TestIssue goldenProjectIssue4 = new TestIssue(TestConnector.goldenProjectRepository, "goldenProject4");
        TestIssue goldenProjectIssue5 = new TestIssue(TestConnector.goldenProjectRepository, "goldenProject5");
        TestIssue goldenProjectIssue6 = new TestIssue(TestConnector.goldenProjectRepository, "goldenProject6");
        TestIssue goldenProjectIssue7 = new TestIssue(TestConnector.goldenProjectRepository, "goldenProject7");

        // add koliba and golden-project issues 1, 2, 3, 4, 5, 6, 7,
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, kolibaIssue1);
        waitAbit();
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.goldenProjectRepository, goldenProjectIssue1);
        waitAbit();
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, kolibaIssue2);
        waitAbit();
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.goldenProjectRepository, goldenProjectIssue2);
        waitAbit();
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, kolibaIssue3);
        waitAbit();
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.goldenProjectRepository, goldenProjectIssue3);
        waitAbit();
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, kolibaIssue4);
        waitAbit();
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.goldenProjectRepository, goldenProjectIssue4);
        waitAbit();
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, kolibaIssue5);
        waitAbit();
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.goldenProjectRepository, goldenProjectIssue5);
        waitAbit();
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, kolibaIssue6);
        waitAbit();
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.goldenProjectRepository, goldenProjectIssue6);
        waitAbit();
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, kolibaIssue7);
        waitAbit();
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.goldenProjectRepository, goldenProjectIssue7);


        // getIssues for koliba -> repo1 issues 3..7 are returned
        IssueHandle[] issues = accessor.getRecentIssues(getKenaiProject("koliba"));
        assertNotNull(issues);
        assertIssueHandles(issues, new String[] {kolibaIssue7.getID(), kolibaIssue6.getID(), kolibaIssue5.getID(), kolibaIssue4.getID(), kolibaIssue3.getID()});

        // getIssues for goldenProject -> repo1 issues 3..7 are returned
        issues = accessor.getRecentIssues(getKenaiProject("golden-project-1"));
        assertNotNull(issues);
        assertIssueHandles(issues, new String[] {goldenProjectIssue7.getID(), goldenProjectIssue6.getID(), goldenProjectIssue5.getID(), goldenProjectIssue4.getID(), goldenProjectIssue3.getID()});

        // getAll -> repo1 issues 3..7 are returned and repo2 issues 3..7 are returned
        issues = accessor.getRecentIssues();
        assertNotNull(issues);
        assertIssueHandles(issues, new String[] {goldenProjectIssue7.getID(), kolibaIssue7.getID(), goldenProjectIssue6.getID(), kolibaIssue6.getID(), goldenProjectIssue5.getID()});
    }

    public void testDisplayNames() throws MalformedURLException, CoreException, IOException, InterruptedException {
        KenaiIssueAccessor accessor = getIssueAccessor();

        TestIssue kolibaIssue1 = new TestIssue(TestConnector.kolibaRepository, "This issue has a very long name so that that get shortened display name will return something shorter.");
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, kolibaIssue1);

        IssueHandle[] issues = accessor.getRecentIssues();
        assertNotNull(issues);
        assertEquals(kolibaIssue1.getDisplayName(), issues[0].getDisplayName());
        assertEquals(kolibaIssue1.getShortenedDisplayName(), issues[0].getShortDisplayName());
        assertNotSame(issues[0].getDisplayName(), issues[0].getShortDisplayName());
    }

    public void testIsOpened() throws MalformedURLException, CoreException, IOException, InterruptedException {
        KenaiIssueAccessor accessor = getIssueAccessor();

        TestIssue kolibaIssue1 = new TestIssue(TestConnector.kolibaRepository, "This issue has a very long name so that that get shortened display name will return something shorter.");
        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, kolibaIssue1);

        IssueHandle[] issues = accessor.getRecentIssues();
        assertNotNull(issues);
        assertFalse(issues[0].isOpened());
        assertFalse(issues[0].isShowing());

        kolibaIssue1.open();
        LogHandler ln = new LogHandler("IssueTopComponent Opened " + kolibaIssue1.getID(), LogHandler.Compare.ENDS_WITH);
        ln.waitUntilDone();

        assertTrue(issues[0].isOpened());
    }

    private void assertIssueHandles(IssueHandle[] issues, String[] ids) {
        assertEquals(ids.length, issues.length);
        for (int i = 0; i < ids.length; i++) {
            assertEquals(ids[i], issues[i].getID());                        
        }
    }

    private KenaiIssueAccessor getIssueAccessor() {
        return Lookup.getDefault().lookup(KenaiIssueAccessor.class);
    }

    private void waitAbit() throws InterruptedException {
        Thread.sleep(10);
    }

    private static KenaiProject getKenaiProject(String name) throws KenaiException {
        return Kenai.getDefault().getProject(name);
    }

    private static Repository getKenaiRepository(KenaiProject kp) throws KenaiException {
        return KenaiRepositories.getInstance().getRepository(kp);
    }

    private static class TestRepository extends Repository {
        private final Repository delegate;

        public TestRepository(String name) throws KenaiException {
            delegate = getKenaiRepository(getKenaiProject(name));
        }
        public String getDisplayName() {
            return delegate.getDisplayName();
        }
        public String getTooltip() {
            return delegate.getTooltip();
        }
        public String getID() {
            return delegate.getID();
        }
        public String getUrl() {
            return delegate.getUrl();
        }
        public Lookup getLookup() {
            return delegate.getLookup();
        }
        public Image getIcon() { throw new UnsupportedOperationException("Not supported yet."); }
        public Issue getIssue(String id) { throw new UnsupportedOperationException("Not supported yet."); }
        public void remove() { throw new UnsupportedOperationException("Not supported yet."); }
        public BugtrackingController getController() { throw new UnsupportedOperationException("Not supported yet.");}
        public Query createQuery() { throw new UnsupportedOperationException("Not supported yet.");}
        public Issue createIssue() {throw new UnsupportedOperationException("Not supported yet.");}
        public Query[] getQueries() {throw new UnsupportedOperationException("Not supported yet.");}
        public Issue[] simpleSearch(String criteria) {throw new UnsupportedOperationException("Not supported yet.");}

        @Override
        public Collection<RepositoryUser> getUsers() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private static class TestIssue extends Issue {
        private final String name;
        private final TestIssueController controller = new TestIssueController();
        public TestIssue(Repository repository, String name) {
            super(repository);
            this.name = name;
        }
        public String getDisplayName() {
            return name;
        }
        public String getTooltip() {
            return name;
        }
        public boolean isNew() {
            return false;
        }
        public String getSummary() {
            return "This is" + name;
        }
        public String getID() {
            return name;
        }
        public BugtrackingController getController() {
            return controller;
        }
        public boolean refresh() {throw new UnsupportedOperationException("Not supported yet.");}
        public void addComment(String comment, boolean closeAsFixed) {throw new UnsupportedOperationException("Not supported yet.");}
        public void attachPatch(File file, String description) {throw new UnsupportedOperationException("Not supported yet.");}
        public IssueNode getNode() {throw new UnsupportedOperationException("Not supported yet.");}
        public Map<String, String> getAttributes() {throw new UnsupportedOperationException("Not supported yet.");}
    }

    private static class TestIssueController extends BugtrackingController {
        private JPanel panel = new JPanel();
        public JComponent getComponent() {
            return panel;
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx(this.getClass());
        }
        public boolean isValid() {
            return true;
        }
        public void applyChanges() throws IOException { }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bugtracking.spi.BugtrackingConnector.class)
    public static class TestConnector extends BugtrackingConnector {
        static String ID = "KenaiCconector";
        static TestRepository kolibaRepository;
        static TestRepository goldenProjectRepository;

        public TestConnector() {
        }
        public String getDisplayName() {
            return ID;
        }
        public String getTooltip() {
            return ID;
        }
        public Repository createRepository() {
                throw new UnsupportedOperationException("Not supported yet.");
        }
        public Repository[] getRepositories() {
            return new Repository[] {kolibaRepository, goldenProjectRepository};
        }
        public Lookup getLookup() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }  

}
