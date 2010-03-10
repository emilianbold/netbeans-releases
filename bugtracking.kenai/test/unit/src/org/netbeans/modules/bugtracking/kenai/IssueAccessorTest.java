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
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiSupport;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiManager;
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
    private static Kenai kenai;

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
            kenai = KenaiManager.getDefault().createKenai("testkenai", "https://testkenai.com");
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

//    public void testGetRecentIssuesEmptyReturn() throws MalformedURLException, CoreException, IOException {
//        KenaiIssueAccessor accessor = getIssueAccessor();
//        IssueHandle[] issues = accessor.getRecentIssues();
//        assertNotNull(issues);
//        assertEquals(0, issues.length);
//        issues = accessor.getRecentIssues(getKenaiProject("koliba"));
//        assertNotNull(issues);
//        assertEquals(0, issues.length);
//    }

    public void testRecentIssuesOnOpen() throws MalformedURLException, CoreException, IOException, InterruptedException {
        TestIssue issue1 = new TestIssue(TestConnector.kolibaRepository, "1");
        TestIssue issue2 = new TestIssue(TestConnector.kolibaRepository, "2");
        TestIssue issue3 = new TestIssue(TestConnector.kolibaRepository, "3");

        KenaiIssueAccessor accessor = getIssueAccessor();

        // open issue1, issue2, issue3
        issue1.open(); waitAbit();
        issue2.open(); waitAbit();
        issue3.open(); waitAbit();
        issue2.open(); waitAbit();
        issue1.open();
        
//        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, issue1);
//        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, issue2);
//        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, issue3);
//        // add issue2 one more time
//        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, issue2);
//        // add issue1 one more time
//        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, issue1);
//        // the expected order should be 1,2,3

        // getIssues for koliba -> issues 1, 2, 3 are returned
        IssueHandle[] issues = accessor.getRecentIssues(getKenaiProject("koliba"));
        assertNotNull(issues);
        assertIssueHandles(issues, new String[] {issue1.getID(), issue2.getID(), issue3.getID()});
    }

//    public void testDisplayNames() throws MalformedURLException, CoreException, IOException, InterruptedException {
//        KenaiIssueAccessor accessor = getIssueAccessor();
//
//        TestIssue kolibaIssue1 = new TestIssue(TestConnector.kolibaRepository, "This issue has a very long name so that that get shortened display name will return something shorter.");
//        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, kolibaIssue1);
//
//        IssueHandle[] issues = accessor.getRecentIssues();
//        assertNotNull(issues);
//        assertEquals(kolibaIssue1.getDisplayName(), issues[0].getDisplayName());
//        assertEquals(kolibaIssue1.getShortenedDisplayName(), issues[0].getShortDisplayName());
//        assertNotSame(issues[0].getDisplayName(), issues[0].getShortDisplayName());
//    }
//
//    public void testIsOpened() throws MalformedURLException, CoreException, IOException, InterruptedException {
//        KenaiIssueAccessor accessor = getIssueAccessor();
//
//        TestIssue kolibaIssue1 = new TestIssue(TestConnector.kolibaRepository, "This issue has a very long name so that that get shortened display name will return something shorter.");
//        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, kolibaIssue1);
//
//        IssueHandle[] issues = accessor.getRecentIssues();
//        assertNotNull(issues);
//        assertFalse(issues[0].isOpened());
//        assertFalse(issues[0].isShowing());
//
//        kolibaIssue1.open();
//        LogHandler ln = new LogHandler("IssueTopComponent Opened " + kolibaIssue1.getID(), LogHandler.Compare.ENDS_WITH);
//        ln.waitUntilDone();
//
//        assertTrue(issues[0].isOpened());
//    }
    
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
        return kenai.getProject(name);
    }

    private static class TestRepository extends Repository {
        private final Repository delegate;

        public TestRepository(String name) throws IOException {
            KenaiProject kp = kenai.getProject(name);
            delegate = KenaiUtil.getRepository(kp.getWebLocation().toString(), kp.getName());
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
        public boolean refresh() {return true;}
        public Map<String, String> getAttributes() {return Collections.EMPTY_MAP;}
        public void addComment(String comment, boolean closeAsFixed) {throw new UnsupportedOperationException("Not supported yet.");}
        public void attachPatch(File file, String description) {throw new UnsupportedOperationException("Not supported yet.");}
        public IssueNode getNode() {throw new UnsupportedOperationException("Not supported yet.");}
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
            return Lookup.EMPTY;
        }

        @Override
        public String getID() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Image getIcon() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }  

}
