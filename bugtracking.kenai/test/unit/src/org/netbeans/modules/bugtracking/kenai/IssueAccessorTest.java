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

package org.netbeans.modules.bugtracking.kenai;

import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.*;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.team.spi.TeamUtil;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor.IssueHandle;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

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
        MockLookup.setLayersAndInstances();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        try {
            System.setProperty("kenai.com.url","https://testjava.net");
            kenai = KenaiManager.getDefault().createKenai("testjava.net", "https://testjava.net");
            BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
            String username = br.readLine();
            String password = br.readLine();

            String proxy = br.readLine();
            String port = br.readLine();
        
            if(proxy != null) {
                System.setProperty("https.proxyHost", proxy);
                System.setProperty("https.proxyPort", port);
            }
            
            br.close();
            kenai.login(username, password.toCharArray(), false);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        if(IATestConnector.kolibaRepository == null) {
            IATestConnector.kolibaRepository = TestKit.getRepository(new IATestRepository("nb-jnet-test")).getRepository();
//            TestConnector.goldenProjectRepository = new TestRepository("golden-project-1");
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
        
        Issue issue1 = TestKit.getIssue(IATestConnector.kolibaRepository, new IATestIssue("1")).getIssue();
        Issue issue2 = TestKit.getIssue(IATestConnector.kolibaRepository, new IATestIssue("2")).getIssue();
        Issue issue3 = TestKit.getIssue(IATestConnector.kolibaRepository, new IATestIssue("3")).getIssue();

        KenaiIssueAccessor accessor = getIssueAccessor();

        LogHandler lh = new LogHandler("activated issue", LogHandler.Compare.STARTS_WITH);
        // open issue1, issue2, issue3
        issue1.open(); lh.waitUntilDone(); waitAbit();
        issue2.open(); lh.waitUntilDone(); waitAbit();
        issue3.open(); lh.waitUntilDone(); waitAbit();
        issue2.open(); lh.waitUntilDone(); waitAbit();
        issue1.open(); lh.waitUntilDone();
        
//        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, issue1);
//        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, issue2);
//        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, issue3);
//        // add issue2 one more time
//        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, issue2);
//        // add issue1 one more time
//        BugtrackingManager.getInstance().addRecentIssue(TestConnector.kolibaRepository, issue1);
//        // the expected order should be 1,2,3

        // getIssues for koliba -> issues 1, 2, 3 are returned
        IssueHandle[] issues = accessor.getRecentIssues(getKenaiProject("nb-jnet-test"));
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

    private static class IATestRepository extends TestRepository {
        private final RepositoryImpl delegate;

        public IATestRepository(String name) throws IOException {
            KenaiProject kp = kenai.getProject(name);
            delegate = APIAccessor.IMPL.getImpl(TeamUtil.getRepository(kp.getWebLocation().toString(), kp.getName()));
        }

        @Override
        public RepositoryInfo getInfo() {
            return delegate.getInfo();
        }
    }

    private static class IATestIssue extends TestIssue {
        private final String name;
        private final TestIssueController controller = new TestIssueController();

        public IATestIssue(String name) {
            this.name = name;
        }
        @Override
        public String getDisplayName() {
            return name;
        }
        @Override
        public String getTooltip() {
            return name;
        }
        @Override
        public boolean isNew() {
            return false;
        }
        @Override
        public boolean isFinished() {
            return false;
        }
        @Override
        public String getSummary() {
            return "This is" + name;
        }
        @Override
        public String getID() {
            return name;
        }
        @Override
        public IssueController getController() {
            return controller;
        }
        @Override
        public boolean refresh() {return true;}
        public Map<String, String> getAttributes() {return Collections.EMPTY_MAP;}
    }

    private static class TestIssueController implements IssueController {
        private final JPanel panel = new JPanel();
        @Override
        public JComponent getComponent() {
            return panel;
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(this.getClass());
        }
        @Override public void opened() { }
        @Override public void closed() { }

        @Override
        public boolean saveChanges() { 
            return true;
        }
        @Override
        public boolean discardUnsavedChanges() {
            return true;
        }
        @Override public void addPropertyChangeListener(PropertyChangeListener l) { }
        @Override public void removePropertyChangeListener(PropertyChangeListener l) { }
    }

    @BugtrackingConnector.Registration(
            displayName=IATestConnector.ID,
            tooltip=IATestConnector.ID,
            id=IATestConnector.ID
    )
    public static class IATestConnector implements BugtrackingConnector {
        public final static String ID = "KenaiCconector";
        static Repository kolibaRepository;
//        static TestRepository goldenProjectRepository;
        private static IATestConnector instance;

        public IATestConnector() {
        }
        public String getDisplayName() {
            return ID;
        }
        public String getTooltip() {
            return ID;
        }
        public Repository createRepository(RepositoryInfo info) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        public Repository createRepository() {
                throw new UnsupportedOperationException("Not supported yet.");
        }
        public Repository[] getRepositories() {
            return new Repository[] {kolibaRepository /*, goldenProjectRepository*/};
        }
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
        static IATestConnector getInstance() {
            if(instance == null) {
                DelegatingConnector[] conns = BugtrackingManager.getInstance().getConnectors();
                for (DelegatingConnector dc : conns) {
                    if(IATestConnector.ID.equals(dc.getID())) {
                        instance = (IATestConnector) dc.getDelegate();
                    }
                }
            }
            return instance;
        }
    }  

}
