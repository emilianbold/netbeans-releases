
package org.netbeans.modules.bugtracking.issuetable;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
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
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer.TableCellStyle;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.issuetable.IssueNode.IssueProperty;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.openide.nodes.Node.Property;
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
    public void testGetCellStyle() {
        JTable table = new JTable();
        RendererQuery query = new RendererQuery();
        String propertyValue = "some value";
        RendererIssue issue = new RendererIssue();
        IssueProperty property = new RendererNode( issue, propertyValue).createProperty();

        MessageFormat issueNewFormat       = getFormat("issueNewFormat");      // NOI18N
        MessageFormat issueObsoleteFormat  = getFormat("issueObsoleteFormat"); // NOI18N
        MessageFormat issueModifiedFormat  = getFormat("issueModifiedFormat"); // NOI18N

        Color newHighlightColor            = new Color(0x00b400);
        Color modifiedHighlightColor       = new Color(0x0000ff);
        Color obsoleteHighlightColor       = new Color(0x999999);

        // issue seen, not selected
        query.containsIssue = true;
        issue.wasSeen = true;
        boolean selected = true;
        TableCellStyle defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, selected, 0);
        TableCellStyle result = QueryTableCellRenderer.getCellStyle(table, query, property, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals(propertyValue, result.getTooltip());

        // issue seen, selected
        query.containsIssue = true;
        issue.wasSeen = true;
        selected = true;
        result = QueryTableCellRenderer.getCellStyle(table, query, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals(propertyValue, result.getTooltip());

        // obsolete issue, not selected
        query.containsIssue = false;
        issue.wasSeen = false;
        selected = false;
        result = QueryTableCellRenderer.getCellStyle(table, query, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(issueObsoleteFormat, result.getFormat());
        assertEquals(propertyValue, result.getTooltip());

        // obsolete issue, selected
        query.containsIssue = false;
        selected = true;
        issue.wasSeen = false;
        result = QueryTableCellRenderer.getCellStyle(table, query, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, selected, 0);
        assertEquals(obsoleteHighlightColor, result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(defaultStyle.getFormat(), result.getFormat());
        assertEquals(propertyValue, result.getTooltip());

        // modified issue, not selected
        query.containsIssue = true;
        selected = false;
        issue.wasSeen = false;
        query.status = IssueCache.ISSUE_STATUS_MODIFIED;
        result = QueryTableCellRenderer.getCellStyle(table, query, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(issueModifiedFormat, result.getFormat());
        assertEquals(propertyValue, result.getTooltip());


        // modified issue, selected
        query.containsIssue = true;
        selected = true;
        issue.wasSeen = false;
        query.status = IssueCache.ISSUE_STATUS_MODIFIED;
        result = QueryTableCellRenderer.getCellStyle(table, query, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, selected, 0);
        assertEquals(modifiedHighlightColor, result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals(propertyValue, result.getTooltip());

        // new issue, not selected
        query.containsIssue = true;
        selected = false;
        issue.wasSeen = false;
        query.status = IssueCache.ISSUE_STATUS_NEW;
        result = QueryTableCellRenderer.getCellStyle(table, query, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, selected, 0);
        assertEquals(defaultStyle.getBackground(), result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(issueNewFormat, result.getFormat());
        assertEquals(propertyValue, result.getTooltip());


        // new issue, selected
        query.containsIssue = true;
        selected = true;
        issue.wasSeen = false;
        query.status = IssueCache.ISSUE_STATUS_NEW;
        result = QueryTableCellRenderer.getCellStyle(table, query, property, selected, 0);
        defaultStyle = QueryTableCellRenderer.getDefaultCellStyle(table, selected, 0);
        assertEquals(newHighlightColor, result.getBackground());
        assertEquals(defaultStyle.getForeground(), result.getForeground());
        assertEquals(null, result.getFormat());
        assertEquals(propertyValue, result.getTooltip());

    }


    /**
     * Test of getDefaultCellStyle method, of class QueryTableCellRenderer.
     */
    @Test
    public void testGetDefaultCellStyle() {
        JTable table = new JTable();
        
        TableCellStyle result = QueryTableCellRenderer.getDefaultCellStyle(table, true, 0);
        assertEquals(table.getSelectionBackground(), result.getBackground()); // keep table selection colors
        assertEquals(Color.WHITE, result.getForeground());
        assertNull(result.getFormat());
        assertNull(result.getTooltip());

        result = QueryTableCellRenderer.getDefaultCellStyle(table, false, 0);
        assertEquals(table.getForeground(), result.getForeground()); // keep table selection colors
        assertNull(result.getFormat());
        assertNull(result.getTooltip());
        Color unevenBackground = result.getBackground();

        result = QueryTableCellRenderer.getDefaultCellStyle(table, false, 1);
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

    private class RendererQuery extends Query {
        private boolean containsIssue;
        private int status;

        public RendererQuery() {
        }

        @Override
        public boolean isSaved() {
            fail("implement me!!!");
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
        public BugtrackingController getController() {
            fail("implement me!!!");
            return null;
        }

        @Override
        public Repository getRepository() {
            fail("implement me!!!");
            return null;
        }

        @Override
        public boolean refresh() {
            fail("implement me!!!");
            return false;
        }

        @Override
        public Issue[] getIssues(int includeStatus) {
            fail("implement me!!!");
            return null;
        }

        @Override
        public boolean contains(Issue issue) {
            return containsIssue;
        }

        @Override
        public int getIssueStatus(Issue issue) {
            return status;
        }
    }

    private class RendererNode extends IssueNode {

        Object propertyValue;
        public RendererNode(Issue issue, String value) {
            super(issue);
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

    private class RendererIssue extends Issue {
        boolean wasSeen = false;
        public RendererIssue() {
            super(new RendererRepository());
            ((RendererRepository)getRepository()).setIssue(this);
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

        @Override
        public IssueNode getNode() {
            fail("implement me!!!");
            return null;
        }

        @Override
        public String getID() {
            return "id";
        }

        @Override
        public String getSummary() {
            fail("implement me!!!");
            return null;
        }

        public String getRecentChanges() {
            fail("implement me!!!");
            return null;
        }

        @Override
        public Map<String, String> getAttributes() {
            fail("implement me!!!");
            return null;
        }
    }

    private class RendererRepository extends Repository {
        private RendererIssue issue;
        public RendererRepository() {
        }
        public void setIssue(RendererIssue issue) {
            this.issue = issue;
        }
        @Override
        public Image getIcon() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public String getTooltip() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public String getID() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public String getUrl() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public Issue getIssue(String id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public BugtrackingController getController() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public Query createQuery() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public Issue createIssue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public Query[] getQueries() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public Issue[] simpleSearch(String criteria) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        public Lookup getLookup() {
            return Lookups.singleton(new IssueCache("renderer", new IssueCache.IssueAccessor() {
                public Issue createIssue(Object issueData) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                public void setIssueData(Issue issue, Object issueData) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                public boolean wasSeen(String id) {
                    return issue.wasSeen;
                }
                public String getRecentChanges(Issue issue) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                public long getLastModified(Issue issue) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                public long getCreated(Issue issue) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }) {});
        }

        @Override
        public Collection<RepositoryUser> getUsers() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
}