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

package org.netbeans.modules.bugtracking.ui.issue.cache;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.*;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache.IssueAccessor;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author tomas
 */
public class CacheTest extends NbTestCase {

    public CacheTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File wd = getWorkDir();
        System.setProperty("netbeans.user", wd.getAbsolutePath());
        emptyStorage();
        BugtrackingUtil.deleteRecursively(wd);
    }

    public void testInitialSeen2Modified2Seen() throws MalformedURLException, CoreException, IOException, InterruptedException {
        long tsBeforeRepo = System.currentTimeMillis();
        Thread.sleep(10);

        CTestRepository repo = new CTestRepository("test repo");
        IssueCache<CTestIssue, String> cache = repo.getLookup().lookup(IssueCache.class);
        // creating issue with creation     < repo reference time;
        //                     modification < repo reference time
        // => initial status SEEN
        CTestIssue issue = cache.setIssueData("1", "1#issue1#" + tsBeforeRepo + "#" + tsBeforeRepo + "#v11#v21#v31");
        assertNotNull(issue);
        int status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_SEEN, status);
        Map<String, String> attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v11", "v21", "v31");

        // setting changed data => MODIFIED, attrs stay the same
        long tsAfterRepo = System.currentTimeMillis();
        cache.setIssueData(issue, "1#issue1#" + tsBeforeRepo + "#" + tsAfterRepo + "#v12#v22#v32");
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_MODIFIED, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v11", "v21", "v31");

        // set SEEN TRUE => SEEN
        cache.setSeen(issue.getID(), true);
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_SEEN, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v12", "v22", "v32");
    }

    public void testInitialNew2RefreshChanged2Seen2Unseen() throws MalformedURLException, CoreException, IOException, InterruptedException {
        CTestRepository repo = new CTestRepository("test repo");
        IssueCache<CTestIssue, String> cache = repo.getLookup().lookup(IssueCache.class);
        Thread.sleep(10);
        long tsAfterRepo = System.currentTimeMillis();
        // creating issue with creation     > repo reference time;
        //                     modification > repo reference time
        // => initial status NEW
        CTestIssue issue = cache.setIssueData("1", "1#issue1#" + tsAfterRepo + "#" + tsAfterRepo + "#v11#v21#v31");
        assertNotNull(issue);
        int status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_NEW, status);
        Map<String, String> attr = cache.getSeenAttributes(issue.getID());
        assertNull(attr);

        // refresh
        Thread.sleep(10);
        cache.setIssueData(issue, "1#issue1#" + tsAfterRepo + "#" + System.currentTimeMillis()  + "#v12#v22#v32");
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_NEW, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNull(attr);

        // set SEEN TRUE => SEEN
        cache.setSeen(issue.getID(), true);
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_SEEN, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v12", "v22", "v32");

        // set SEEN FALSE => NEW
        cache.setSeen(issue.getID(), false);
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_NEW, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v12", "v22", "v32");
    }

    public void testInitialModified2Seen2Unseen() throws MalformedURLException, CoreException, IOException, InterruptedException {
        long tsBeforeRepo = System.currentTimeMillis();
        Thread.sleep(10);

        CTestRepository repo = new CTestRepository("test repo");
        IssueCache<CTestIssue, String> cache = repo.getLookup().lookup(IssueCache.class);
        Thread.sleep(10);
        long tsAfterRepo = System.currentTimeMillis();
        // creating issue with creation     < repo reference time;
        //                     modification > repo reference time
        // => initial status MODIFIED
        CTestIssue issue = cache.setIssueData("1", "1#issue1#" + tsBeforeRepo + "#" + tsAfterRepo + "#v11#v21#v31");
        assertNotNull(issue);
        int status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_MODIFIED, status);
        Map<String, String> attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);        
        assertAttributes(attr, "v11", "v21", "v31");

        // set SEEN TRUE => SEEN. attrs from the last setData
        cache.setSeen(issue.getID(), true);
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_SEEN, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v11", "v21", "v31");

        // set SEEN FALSE => MODIFIED, attrs stays the same
        Thread.sleep(10);
        cache.setSeen(issue.getID(), false);
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_MODIFIED, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v11", "v21", "v31");

        // set SEEN TRUE => SEEN, attsr stay the same
        cache.setSeen(issue.getID(), true);
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_SEEN, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v11", "v21", "v31");
    }

    public void testIssueModified2Seen2Restart2Unseen() throws MalformedURLException, CoreException, IOException, InterruptedException {
        long tsBeforeRepo = System.currentTimeMillis();
        Thread.sleep(10);

        CTestRepository repo = new CTestRepository("test repo");
        IssueCache<CTestIssue, String> cache = repo.getLookup().lookup(IssueCache.class);
        Thread.sleep(10);
        long tsAfterRepo = System.currentTimeMillis();

        // creating issue with creation     < repo reference time;
        //                     modification > repo reference time
        // => initial status MODIFIED
        String data = "1#issue1#" + tsBeforeRepo + "#" + tsAfterRepo + "#v11#v21#v31";
        CTestIssue issue = cache.setIssueData("1", data);
        assertNotNull(issue);
        int status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_MODIFIED, status);
        Map<String, String> attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v11", "v21", "v31");

        // set SEEN TRUE => SEEN. attrs from the last setData
        cache.setSeen(issue.getID(), true);
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_SEEN, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v11", "v21", "v31");

        // recreating the same repo emulates restart
        repo = new CTestRepository("test repo");
        cache = repo.getLookup().lookup(IssueCache.class);
        // setting the last set data emulates refresh with unchanged data
        // status is expected to be SEEN, and data the last set
        cache.setIssueData("1", data);
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_SEEN, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v11", "v21", "v31");

        // set SEEN FALSE => MODIFIED, attrs from the last modfiied
        Thread.sleep(10);
        cache.setSeen(issue.getID(), false);
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_MODIFIED, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v11", "v21", "v31");
    }

    public void testModified2RefreshChange2Seen2RefreshChange() throws MalformedURLException, CoreException, IOException, InterruptedException {
        modified2RefreshChange2Seen2RefreshChange(false);
    }

    public void testModified2RefreshChange2Seen2Restart2RefreshChange() throws MalformedURLException, CoreException, IOException, InterruptedException {
        modified2RefreshChange2Seen2RefreshChange(true);
    }
    public void modified2RefreshChange2Seen2RefreshChange(boolean restart) throws MalformedURLException, CoreException, IOException, InterruptedException {
        long tsBeforeRepo = System.currentTimeMillis();
        Thread.sleep(10);

        CTestRepository repo = new CTestRepository("test repo");
        IssueCache<CTestIssue, String> cache = repo.getLookup().lookup(IssueCache.class);
        Thread.sleep(10);
        long tsAfterRepo = System.currentTimeMillis();

        // creating issue with creation     < repo reference time;
        //                     modification > repo reference time
        // => initial status MODIFIED
        String data = "1#issue1#" + tsBeforeRepo + "#" + tsAfterRepo + "#v11#v21#v31";
        CTestIssue issue = cache.setIssueData("1", data);
        assertNotNull(issue);
        int status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_MODIFIED, status);
        Map<String, String> attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v11", "v21", "v31");

        // one more time setting changed data => MODIFIED, attrs stay the same
        tsAfterRepo = System.currentTimeMillis();
        cache.setIssueData(issue, "1#issue1#" + tsBeforeRepo + "#" + tsAfterRepo + "#v12#v22#v32");
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_MODIFIED, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v11", "v21", "v31");

        // set SEEN TRUE => SEEN, attrs from the last refresh
        cache.setSeen(issue.getID(), true);
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_SEEN, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v12", "v22", "v32");

        if(restart) {
            // recreating the same repo emulates restart
            repo = new CTestRepository("test repo");
            cache = repo.getLookup().lookup(IssueCache.class);
        }

        // setting changed data => MODIFIED, attrs stay the same
        tsAfterRepo = System.currentTimeMillis();
        cache.setIssueData("1", "1#issue1#" + tsBeforeRepo + "#" + tsAfterRepo + "#v13#v23#v33"); // reload
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_MODIFIED, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v12", "v22", "v32");
    }

    public void testInitialSeen2ModifiedNoChanges2Seen() throws MalformedURLException, CoreException, IOException, InterruptedException {
        long tsBeforeRepo = System.currentTimeMillis();
        Thread.sleep(10);

        CTestRepository repo = new CTestRepository("test repo");
        IssueCache<CTestIssue, String> cache = repo.getLookup().lookup(IssueCache.class);
        // creating issue with creation     < repo reference time;
        //                     modification < repo reference time
        // => initial status SEEN
        CTestIssue issue = cache.setIssueData("1", "1#issue1#" + tsBeforeRepo + "#" + tsBeforeRepo + "#v11#v21#v31");
        assertNotNull(issue);
        int status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_SEEN, status);
        Map<String, String> attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v11", "v21", "v31");

        // setting changed data => MODIFIED, unchanged attrs
        long tsAfterRepo = System.currentTimeMillis();
        cache.setIssueData(issue, "1#issue1#" + tsBeforeRepo + "#" + tsAfterRepo + "#v11#v21#v31");
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_MODIFIED, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v11", "v21", "v31");

        // set SEEN TRUE => SEEN
        cache.setSeen(issue.getID(), true);
        status = cache.getStatus(issue.getID());
        assertEquals(IssueCache.ISSUE_STATUS_SEEN, status);
        attr = cache.getSeenAttributes(issue.getID());
        assertNotNull(attr);
        assertAttributes(attr, "v11", "v21", "v31");
    }

    private void assertAttributes(Map<String, String> attr, String... values) {
        assertEquals(values.length, attr.size());
        for (int i = 0; i < values.length; i++) {
            assertEquals(values[i], attr.get("a"+(i+1)));
        }
    }

    private class CTestIssue extends TestIssue {
        private String[] dataArray;
        private Map<String, String> attrs = new HashMap<String, String>(3);
        private RepositoryImpl repository;
        public CTestIssue(RepositoryImpl repository, String data) {
            this.repository = repository;
            setData(data);
        }
        public String getDisplayName() {
            return dataArray[1];
        }
        public String getTooltip() {
            return dataArray[1];
        }
        public String getID() {
            return dataArray[0];
        }
        public boolean isNew() {
            return false;
        }
        public boolean isFinished() {
            return false;
        }
        public boolean refresh() {
            return true;
        }
        public Map<String, String> getAttributes() {
            HashMap<String, String> m = new HashMap<String, String>();
            m.put("a1", dataArray[4]);
            m.put("a2", dataArray[5]);
            m.put("a3", dataArray[6]);
            return m;
        }
        public String getSummary() {
            return "This is " + dataArray[1];
        }

        void setData(String data) {
            dataArray = data.split("#");
            assertEquals(7, dataArray.length);
        }

        public void addComment(String comment, boolean closeAsFixed) {throw new UnsupportedOperationException("Not supported yet.");}
        public void attachPatch(File file, String description) {throw new UnsupportedOperationException("Not supported yet.");}
        public BugtrackingController getController() {throw new UnsupportedOperationException("Not supported yet.");}
        public IssueNode getNode() {throw new UnsupportedOperationException("Not supported yet.");}
        public String[] getSubtasks() {throw new UnsupportedOperationException("Not supported yet.");}
        
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TestIssue createFor(String id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private class CTestRepository extends TestRepository {
        private final String name;
        private TestCache cache;
        private RepositoryInfo info;
        public CTestRepository(String name) {
            this.name = name;
            info = new RepositoryInfo(name, name, "http://" + name + ".org", name, name, null, null, null, null);
        }
        public Image getIcon() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public RepositoryInfo getInfo() {
            return info;
        }

        public Lookup getLookup() {
            if(cache == null) {
                cache = new TestCache(name, TestKit.getRepository(this));
            }
            return Lookups.singleton(cache);
        }

        public TestIssue[] getIssues(String[] id) {throw new UnsupportedOperationException("Not supported yet.");}
        public void remove() {throw new UnsupportedOperationException("Not supported yet.");}
        public RepositoryController getController() {throw new UnsupportedOperationException("Not supported yet.");}
        public TestQuery createQuery() {throw new UnsupportedOperationException("Not supported yet.");}
        public TestIssue createIssue() {throw new UnsupportedOperationException("Not supported yet.");}
        public Collection<TestQuery> getQueries() {throw new UnsupportedOperationException("Not supported yet.");}
        public Collection<TestIssue> simpleSearch(String criteria) {throw new UnsupportedOperationException("Not supported yet.");}

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) { }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) { }

        private class TestCache extends IssueCache<CTestIssue, String> {
            public TestCache(String nameSpace, RepositoryImpl repository) {
                super(
                    nameSpace, 
                    new IssueAccessor<CTestIssue, String>() {
                        @Override
                        public String getID(String issueData) {
                            String[] a = issueData.split("#");
                            return a[0];
                        }
                        @Override
                        public CTestIssue createIssue(String issueData) {
                            return new CTestIssue(TestKit.getRepository(CTestRepository.this), issueData);
                        }

                        @Override
                        public void setIssueData(CTestIssue issue, String issueData) {
                            ((CTestIssue)issue).setData(issueData);
                        }

                        @Override
                        public Map<String, String> getAttributes(CTestIssue issue) {
                            return ((CTestIssue)issue).getAttributes();
                        }
                        @Override
                        public String getRecentChanges(CTestIssue issue) {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }
                        @Override
                        public long getLastModified(CTestIssue issue) {
                            return Long.parseLong(((CTestIssue)issue).dataArray[3]);
                        }
                        @Override
                        public long getCreated(CTestIssue issue) {
                            return Long.parseLong(((CTestIssue)issue).dataArray[2]);
                        }
                    },
                    new CTestIssueProvider(), 
                    repository.getRepository());
            }
            
            protected void cleanup() {

            }
        }
    }

    public class CTestIssueProvider extends IssueProvider<CTestIssue> {

        @Override
        public String getDisplayName(CTestIssue data) {
            return data.getDisplayName();
        }

        @Override
        public String getTooltip(CTestIssue data) {
            return data.getTooltip();
        }

        @Override
        public String getID(CTestIssue data) {
            return data.getID();
        }

        @Override
        public String getSummary(CTestIssue data) {
            return data.getSummary();
        }

        @Override
        public boolean isNew(CTestIssue data) {
            return data.isNew();
        }
        
        @Override
        public boolean isFinished(CTestIssue data) {
            return data.isFinished();
        }

        @Override
        public boolean refresh(CTestIssue data) {
            return data.refresh();
        }

        @Override
        public void addComment(CTestIssue data, String comment, boolean closeAsFixed) {
            data.addComment(comment, closeAsFixed);
        }

        @Override
        public void attachPatch(CTestIssue data, File file, String description) {
            data.attachPatch(file, description);
        }

        @Override
        public BugtrackingController getController(CTestIssue data) {
            return data.getController();
        }

        @Override
        public void removePropertyChangeListener(CTestIssue data, PropertyChangeListener listener) {
            data.removePropertyChangeListener(listener);
        }

        @Override
        public void addPropertyChangeListener(CTestIssue data, PropertyChangeListener listener) {
            data.addPropertyChangeListener(listener);
        }

        public String[] getSubtasks(CTestIssue data) {throw new UnsupportedOperationException("Not supported yet.");}
    }

    private void emptyStorage() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        File f = getStorageRootFile();
        BugtrackingUtil.deleteRecursively(f);
        Field field = IssueStorage.class.getDeclaredField("storage");
        field.setAccessible(true);
        field.set(IssueStorage.getInstance(), f);
    }

    private File getStorageRootFile() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
        IssueStorage storage = IssueStorage.getInstance();
        Method m = storage.getClass().getDeclaredMethod("getStorageRootFile");
        m.setAccessible(true);
        return (File) m.invoke(storage, new Object[0]);
    }


}
