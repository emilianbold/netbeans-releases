/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.issuetable;

import java.awt.Color;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import javax.swing.JTable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.bugtracking.*;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer.TableCellStyle;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.issuetable.IssueNode.IssueProperty;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.bugtracking.cache.IssueCache;
import org.netbeans.modules.bugtracking.cache.IssueCache.IssueAccessor;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author tomas
 */
public class QueryTableCellRendererTest {

    public QueryTableCellRendererTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getCellStyle method, of class QueryTableCellRenderer.
     */
    @Test
    public void testGetCellStyle() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        JTable table = new JTable();
        RendererRepository rendererRepository = new RendererRepository();
        RendererQuery rendererQuery = new RendererQuery(rendererRepository);

        MessageFormat issueNewFormat       = getFormat("issueNewFormat");      // NOI18N
        MessageFormat issueObsoleteFormat  = getFormat("issueObsoleteFormat"); // NOI18N
        MessageFormat issueModifiedFormat  = getFormat("issueModifiedFormat"); // NOI18N

        Color newHighlightColor            = new Color(0x00b400);
        Color modifiedHighlightColor       = new Color(0x0000ff);
        Color obsoleteHighlightColor       = new Color(0x999999);
        
        RepositoryImpl repository = TestKit.getRepository(rendererRepository);
        QueryImpl query = TestKit.getQuery(repository, rendererQuery);
        
        IssueTable<RendererQuery> issueTable = new IssueTable(
                repository.getRepository(),
                rendererQuery, 
                new ColumnDescriptor[] {new ColumnDescriptor("dummy", String.class, "dummy", "dummy")});

        
        // issue seen, not selected
        RendererIssue rendererIssue = new RendererIssue(rendererRepository, "");
        IssueProperty property = new RendererNode(rendererIssue, "some value", rendererRepository, new ChangesProvider()).createProperty();
        rendererQuery.containsIssue = true;
        boolean selected = false;
        setEntryValues(rendererRepository, rendererIssue, IssueCache.Status.ISSUE_STATUS_SEEN, true);
        TableCellStyle defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        TableCellStyle result = QueryTableCellRenderer.getCellStyle(table, query.getQuery(), issueTable, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals("<html>some value</html>", result.getTooltip());

        // issue seen, selected
        rendererQuery.containsIssue = true;
        rendererIssue = new RendererIssue(rendererRepository, "");
        property = new RendererNode(rendererIssue, "some value", rendererRepository, new ChangesProvider()).createProperty();
        selected = true;
        setEntryValues(rendererRepository, rendererIssue, IssueCache.Status.ISSUE_STATUS_SEEN, true);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        result = QueryTableCellRenderer.getCellStyle(table, query.getQuery(), issueTable, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals("<html>some value</html>", result.getTooltip());

        // obsolete issue, not selected
        rendererQuery.containsIssue = false;
        rendererIssue = new RendererIssue(rendererRepository, "");
        property = new RendererNode(rendererIssue, "some value", rendererRepository, new ChangesProvider()).createProperty();
        selected = false;
        result = QueryTableCellRenderer.getCellStyle(table, query.getQuery(), issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(issueObsoleteFormat, result.getFormat());
        assertEquals("<html>some value<br><font color=\"#999999\"><s>Archived</s></font>- this task doesn't belong to the query anymore</html>", result.getTooltip());

        // obsolete issue, selected
        rendererQuery.containsIssue = false;
        selected = true;
        rendererIssue = new RendererIssue(rendererRepository, "");
        property = new RendererNode(rendererIssue, "some value", rendererRepository, new ChangesProvider()).createProperty();
        result = QueryTableCellRenderer.getCellStyle(table, query.getQuery(), issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(obsoleteHighlightColor, result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(defaultStyle.getFormat(), result.getFormat());
        assertEquals("<html>some value<br><font color=\"#999999\"><s>Archived</s></font>- this task doesn't belong to the query anymore</html>", result.getTooltip());

        // modified issue, not selected
        rendererQuery.containsIssue = true;
        selected = false;
        rendererIssue = new RendererIssue(rendererRepository, "changed");
        property = new RendererNode(rendererIssue, "some value", rendererRepository, new ChangesProvider()).createProperty();
        setEntryValues(rendererRepository, rendererIssue, IssueCache.Status.ISSUE_STATUS_MODIFIED, false);
        result = QueryTableCellRenderer.getCellStyle(table, query.getQuery(), issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(issueModifiedFormat, result.getFormat());
        assertEquals("<html>some value<br><font color=\"#0000FF\">Modified</font>- this task is modified - changed</html>", result.getTooltip());


        // modified issue, selected
        rendererQuery.containsIssue = true;
        selected = true;
        rendererIssue = new RendererIssue(rendererRepository, "changed");
        property = new RendererNode(rendererIssue, "some value", rendererRepository, new ChangesProvider()).createProperty();
        setEntryValues(rendererRepository, rendererIssue, IssueCache.Status.ISSUE_STATUS_MODIFIED, false);
        result = QueryTableCellRenderer.getCellStyle(table, query.getQuery(), issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(modifiedHighlightColor, result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals("<html>some value<br><font color=\"#0000FF\">Modified</font>- this task is modified - changed</html>", result.getTooltip());

        // new issue, not selected
        rendererQuery.containsIssue = true;
        selected = false;
        rendererIssue = new RendererIssue(rendererRepository, "");
        property = new RendererNode(rendererIssue, "some value", rendererRepository, new ChangesProvider()).createProperty();
        setEntryValues(rendererRepository, rendererIssue, IssueCache.Status.ISSUE_STATUS_NEW, false);
        result = QueryTableCellRenderer.getCellStyle(table, query.getQuery(), issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(issueNewFormat, result.getFormat());
        assertEquals("<html>some value<br><font color=\"#00b400\">New</font>- this task is new</html>", result.getTooltip());


        // new issue, selected
        rendererQuery.containsIssue = true;
        selected = true;
        rendererIssue = new RendererIssue(rendererRepository, "");
        property = new RendererNode(rendererIssue, "some value", rendererRepository, new ChangesProvider()).createProperty();
        setEntryValues(rendererRepository, rendererIssue, IssueCache.Status.ISSUE_STATUS_NEW, false);
        result = QueryTableCellRenderer.getCellStyle(table, query.getQuery(), issueTable, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, selected, 0);
        assertEquals(newHighlightColor, result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals("<html>some value<br><font color=\"#00b400\">New</font>- this task is new</html>", result.getTooltip());

    }


    /**
     * Test of getDefaultCellStyle method, of class QueryTableCellRenderer.
     */
    @Test
    public void testGetDefaultCellStyle() {
        JTable table = new JTable();
        RendererRepository rendererRepository = new RendererRepository();
        RendererIssue issue = new RendererIssue(rendererRepository, "");
        RendererQuery query = new RendererQuery(rendererRepository);
        IssueProperty property = new RendererNode(issue, "some value", rendererRepository, new ChangesProvider()).createProperty();

        IssueTable<RendererQuery> issueTable = new IssueTable(
                TestKit.getRepository(rendererRepository).getRepository(),
                query, 
                new ColumnDescriptor[] {new ColumnDescriptor("dummy", String.class, "dummy", "dummy")});
        
        TableCellStyle result = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, true, 0);
        assertEquals(table.getSelectionBackground(), result.getBackground()); // keep table selection colors
        assertEquals(Color.WHITE, result.getForeground());
        assertNull(result.getFormat());
        assertNull(result.getTooltip());

        result = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, false, 0);
        assertEquals(table.getForeground(), result.getForeground()); // keep table selection colors
        assertNull(result.getFormat());
        assertNull(result.getTooltip());
        Color unevenBackground = result.getBackground();

        result = QueryTableCellRenderer.getDefaultCellStyle(table, issueTable, property, false, 1);
        assertEquals(table.getForeground(), result.getForeground()); // keep table selection colors
        assertNull(result.getFormat());
        assertNull(result.getTooltip());
        Color evenBackground = result.getBackground();

        assertNotSame(evenBackground, unevenBackground);
        assertTrue(evenBackground.equals(Color.WHITE) || unevenBackground.equals(Color.WHITE));
        assertTrue(evenBackground.equals(new Color(0xf3f6fd)) || unevenBackground.equals(new Color(0xf3f6fd)));
    }

    private static MessageFormat getFormat(String key) {
        String format = NbBundle.getMessage(IssueTable.class, key);
        return new MessageFormat(format);
    }

    private class RendererQuery extends TestQuery {
        private boolean containsIssue;
        private RendererRepository repository;

        public RendererQuery(RendererRepository repository) {
            this.repository = repository;
        }

        @Override
        public boolean isSaved() {            
            return false;
        }

        @Override
        public String getDisplayName() {
            return "Renderer Query";
        }

        @Override
        public String getTooltip() {
            return "Renderer Query";
        }

        @Override
        public QueryController getController() {
            fail("implement me!!!");
            return null;
        }

        public TestRepository getRepository() {
            return repository;
        }

        public Collection<TestIssue> getIssues() {
            fail("implement me!!!");
            return null;
        }

        public boolean contains(String id) {
            return containsIssue;
        }

//        public int getIssueStatus(String id) {
//            return status;
//        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }        

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            
        }
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            
        }
        @Override
        public void refresh() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private class ChangesProvider implements IssueNode.ChangesProvider<TestIssue>  {
        @Override
        public String getRecentChanges(TestIssue issue) {
            return ((RendererIssue) issue).getRecentChanges();
        }
    }
    
    private class RendererNode<TestIssue> extends IssueNode {

        Object propertyValue;
        public RendererNode(RendererIssue issue, String value, RendererRepository rendererRepository, ChangesProvider changesProvider) {
            super(TestKit.getRepository(rendererRepository).getRepository(), issue, changesProvider);
            propertyValue = value;
        }
        RendererIssueProperty createProperty() {
            return new RendererIssueProperty(null, null, null, null, this);
        }
        @Override
        protected Property<?>[] getProperties() {
            return new Property[0];
        }
        class RendererIssueProperty extends IssueProperty {
            public RendererIssueProperty(String arg0, Class name, String type, String displayName, Object value) {
                super(arg0, name, type, displayName);
            }
            @Override
            public Object getValue() throws IllegalAccessException, InvocationTargetException {
                return propertyValue;
            }
        }
    }

    private static class RendererIssue extends TestIssue {
        private static int id = 0;
        private String recentChanges;
        private final RendererRepository repo;
        public RendererIssue(RendererRepository repo, String recentChanges) {
            id++;
            this.recentChanges = recentChanges;
            this.repo = repo;
        }

        @Override
        public String getDisplayName() {
            return "Renderer Issue";
        }

        @Override
        public String getTooltip() {
            return "Renderer Issue";
        }

        @Override
        public boolean isNew() {
            fail("implement me!!!");
            return false;
        }
        
        @Override
        public boolean isFinished() {
            fail("implement me!!!");
            return false;
        }

        @Override
        public boolean refresh() {
            fail("implement me!!!");
            return false;
        }

        @Override
        public void addComment(String comment, boolean closeAsFixed) {
            fail("implement me!!!");
        }

        @Override
        public void attachPatch(File file, String description) {
            fail("implement me!!!");
        }

        @Override
        public BugtrackingController getController() {
            fail("implement me!!!");
            return null;
        }

        public IssueNode getNode() {
            fail("implement me!!!");
            return null;
        }

        @Override
        public String getID() {
            return id + "";
        }

        @Override
        public String getSummary() {
            fail("implement me!!!");
            return null;
        }

        public String getRecentChanges() {
            return recentChanges;
        }

        public Map<String, String> getAttributes() {
            fail("implement me!!!");
            return null;
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            
        }
        
        @Override
        public String[] getSubtasks() {
            throw new UnsupportedOperationException("Not supported yet.");
        }        

        @Override
        public IssueStatusProvider.Status getStatus() {
            IssueCache.Status s = repo.cache.getStatus(getID());
            switch(s) {
                case ISSUE_STATUS_NEW:
                    return IssueStatusProvider.Status.NEW;
                case ISSUE_STATUS_MODIFIED:
                    return IssueStatusProvider.Status.MODIFIED;
                case ISSUE_STATUS_SEEN:
                    return IssueStatusProvider.Status.SEEN;
            }
            return null;
        }

        @Override
        public void setSeen(boolean seen) {
            try {
                repo.cache.setSeen(getID(), seen);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private class RendererRepository extends TestRepository {
        private RepositoryInfo info;
        private IssueCache<TestIssue> cache;
        private Lookup lookup;
        public RendererRepository() {
            info = new RepositoryInfo("testrepo", "testconnector", null, null, null, null, null, null, null);
            lookup = Lookups.singleton(getCache());
        }

        public IssueCache<TestIssue> getCache() {
            if(cache == null) {
                IssueAccessor<TestIssue> issueAccessor = new IssueCache.IssueAccessor<TestIssue>() {
                    @Override
                    public Map<String, String> getAttributes(TestIssue issue) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    @Override
                    public long getLastModified(TestIssue issue) {
                        return System.currentTimeMillis() - 10 * 60 * 1000;
                    }
                    @Override
                    public long getCreated(TestIssue issue) {
                        return System.currentTimeMillis() - 15 * 60 * 1000;
                    }
                };
                cache = new IssueCache<TestIssue>("test", issueAccessor);
            }
            return cache;
        }
        
        @Override
        public RepositoryInfo getInfo() {
            return info;
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
        public Lookup getLookup() {
            return lookup; 
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            
        }
    };

    private void setEntryValues(RendererRepository repository, RendererIssue rendererIssue, IssueCache.Status status, boolean seen) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        IssueCache cache = repository.getCache();
        try {
            cache.setIssueData(rendererIssue.getID(), rendererIssue); // ensure issue is cached
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        Method m = cache.getClass().getDeclaredMethod("setEntryValues", String.class, IssueCache.Status.class, boolean.class);
        m.setAccessible(true);
        m.invoke(cache, rendererIssue.getID(), status, seen);
    }
    
}
