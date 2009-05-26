/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyVetoException;
import java.awt.*;
import java.awt.event.*;
import org.openide.explorer.view.OutlineView;
import org.netbeans.swing.outline.RenderDataProvider;

/**
 * Treetable to show results of Search History action.
 * 
 * @author Maros Sandor
 */
class DiffTreeTable extends OutlineView implements MouseListener, MouseMotionListener {
    
    private RevisionsRootNode rootNode;
    private List results;

    public DiffTreeTable() {
        getOutline().setShowHorizontalLines(true);
        getOutline().setShowVerticalLines(false);
        getOutline().setRootVisible(false);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setupColumns();

        getOutline().setRenderDataProvider( new NoLeafIconRenderDataProvider( getOutline().getRenderDataProvider() ) );
        
        getOutline().addMouseListener(this);
        getOutline().addMouseMotionListener(this);
    }

    private SearchHistoryPanel.DispRevision getRevisionWithTagsAt(Point p) {
        SearchHistoryPanel.DispRevision drev = getRevisionWithPropertyAt(p, "tagsRevision");
        if (drev != null && drev.getBranches() != null && drev.getBranches().size() + drev.getTags().size() > 1) {
            return drev;
        }
        return null;
    }

    private SearchHistoryPanel.DispRevision getRevisionWithPropertyAt(Point p, String property) {
        int row = getOutline().rowAtPoint(p);
        int column = getOutline().columnAtPoint(p);
        if (row == -1 || column == -1) return null;
        Object o = getOutline().getValueAt(row, column);
        if (o instanceof Node.Property) {
            Node.Property tags = (Node.Property) o;
            return (SearchHistoryPanel.DispRevision) tags.getValue(property);
        }
        return null;
    }
    
    public void mouseClicked(MouseEvent e) {
        Point p = new Point(e.getPoint());
        SearchHistoryPanel.DispRevision drev = getRevisionWithTagsAt(p);
        if (drev != null) {
            Window w = SwingUtilities.windowForComponent(getOutline());
            SwingUtilities.convertPointToScreen(p, getOutline());
            p.x += 10;
            p.y += 10;
            SummaryView.showAllTags(w, p, drev);
        }
        p = new Point(e.getPoint());
        drev = getRevisionWithPropertyAt(p, "messageRevision"); // NOI18N
        if (drev != null) {
            Window w = SwingUtilities.windowForComponent(getOutline());
            SwingUtilities.convertPointToScreen(p, getOutline());
            if ((p.x -= 150) < 0) p.x = 10;
            p.y += getOutline().getRowHeight() * 3 / 2;
            showMessage(w, p, drev);
        }
    }

    private void showMessage(Window w, Point p, SearchHistoryPanel.DispRevision drev) {
        final JTextPane tp = new JTextPane();
        tp.setBackground(SummaryView.darker(UIManager.getColor("List.background"))); // NOI18N
        tp.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 0));
        tp.setEditable(false);

        Style headerStyle = tp.addStyle("headerStyle", null); // NOI18N
        StyleConstants.setBold(headerStyle, true);
            
        Document doc = tp.getDocument();
        try {
            doc.insertString(doc.getLength(), NbBundle.getMessage(DiffTreeTable.class, "CTL_MessageWindow_Title") + "\n\n", headerStyle);  // NOI18N
            doc.insertString(doc.getLength(), drev.getRevision().getMessage() + "\n", null); // NOI18N
        } catch (BadLocationException e) {
            Logger.getLogger(DiffTreeTable.class.getName()).log(Level.WARNING, "Internal error creating commit message popup", e); // NOI18N
        }
            
        Dimension dim = tp.getPreferredSize();
        tp.setPreferredSize(new Dimension(dim.width * 7 / 6, dim.height));
        final JScrollPane jsp = new JScrollPane(tp);
        jsp.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

        TooltipWindow ttw = new TooltipWindow(w, jsp);
        ttw.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                tp.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
            }
        });
        ttw.show(p);
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        if (getRevisionWithTagsAt(e.getPoint()) != null || getRevisionWithPropertyAt(e.getPoint(), "messageRevision") != null) { // NOI18N
            getOutline().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            getOutline().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private void setupColumns() {
        ResourceBundle loc = NbBundle.getBundle(DiffTreeTable.class);
        Node.Property [] columns;
        if (CvsModuleConfig.getDefault().getPreferences().getBoolean(CvsModuleConfig.PROP_SEARCHHISTORY_FETCHTAGS, true)) {
            columns = new Node.Property[5];
            columns[3] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_TAGS, List.class, loc.getString("LBL_DiffTree_Column_Tags"), loc.getString("LBL_DiffTree_Column_Tags_Desc"));
            columns[4] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_MESSAGE, String.class, loc.getString("LBL_DiffTree_Column_Message"), loc.getString("LBL_DiffTree_Column_Message_Desc"));
        } else {
            columns = new Node.Property[4];
            columns[3] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_MESSAGE, String.class, loc.getString("LBL_DiffTree_Column_Message"), loc.getString("LBL_DiffTree_Column_Message_Desc"));
        }
        columns[0] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_LOCATION, String.class, loc.getString("LBL_DiffTree_Column_Location"), loc.getString("LBL_DiffTree_Column_Location_Desc"));
        columns[1] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_DATE, String.class, loc.getString("LBL_DiffTree_Column_Time"), loc.getString("LBL_DiffTree_Column_Time_Desc"));
        columns[2] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_USERNAME, String.class, loc.getString("LBL_DiffTree_Column_Username"), loc.getString("LBL_DiffTree_Column_Username_Desc"));
        setProperties(columns);
    }
    
    private void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int width = getWidth();
                if (CvsModuleConfig.getDefault().getPreferences().getBoolean(CvsModuleConfig.PROP_SEARCHHISTORY_FETCHTAGS, true)) {
                    if (getOutline().getColumnModel().getColumnCount() != 6) return;
                    getOutline().getColumnModel().getColumn(0).setPreferredWidth(width * 15 / 100);
                    getOutline().getColumnModel().getColumn(1).setPreferredWidth(width * 15 / 100);
                    getOutline().getColumnModel().getColumn(2).setPreferredWidth(width * 10 / 100);
                    getOutline().getColumnModel().getColumn(3).setPreferredWidth(width * 10 / 100);
                    getOutline().getColumnModel().getColumn(4).setPreferredWidth(width * 10 / 100);
                    getOutline().getColumnModel().getColumn(5).setPreferredWidth(width * 40 / 100);
                } else {
                    if (getOutline().getColumnModel().getColumnCount() != 5) return;
                    getOutline().getColumnModel().getColumn(0).setPreferredWidth(width * 20 / 100);
                    getOutline().getColumnModel().getColumn(1).setPreferredWidth(width * 20 / 100);
                    getOutline().getColumnModel().getColumn(2).setPreferredWidth(width * 10 / 100);
                    getOutline().getColumnModel().getColumn(3).setPreferredWidth(width * 10 / 100);
                    getOutline().getColumnModel().getColumn(4).setPreferredWidth(width * 40 / 100);
                }
            }
        });
    }

    void setSelection(int idx) {
        getOutline().getSelectionModel().setValueIsAdjusting(false);
        getOutline().scrollRectToVisible(getOutline().getCellRect(idx, 1, true));
        getOutline().getSelectionModel().setSelectionInterval(idx, idx);
    }

    void setSelection(SearchHistoryPanel.ResultsContainer container) {
        RevisionNode node = (RevisionNode) getNode(rootNode, container);
        if (node == null) return;
        ExplorerManager em = ExplorerManager.find(this);
        try {
            em.setSelectedNodes(new Node [] { node });
        } catch (PropertyVetoException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    void setSelection(SearchHistoryPanel.DispRevision revision) {
        RevisionNode node = (RevisionNode) getNode(rootNode, revision);
        if (node == null) return;
        ExplorerManager em = ExplorerManager.find(this);
                try {
            em.setSelectedNodes(new Node [] { node });
        } catch (PropertyVetoException e) {
            ErrorManager.getDefault().notify(e);
                            }
                        }

    private Node getNode(Node node, Object obj) {
        Object object = node.getLookup().lookup(obj.getClass());
        if (obj.equals(object)) return node;
        Enumeration children = node.getChildren().nodes();
        while (children.hasMoreElements()) {
            Node child = (Node) children.nextElement();
            Node result = getNode(child, obj);
            if (result != null) return result;
        }
        return null;
    }

    public int [] getSelection() {
        return getOutline().getSelectedRows();
    }

    public int getRowCount() {
        return getOutline().getRowCount();
    }

    private static class ColumnDescriptor extends PropertySupport.ReadOnly {
        
        public ColumnDescriptor(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }

    public void addNotify() {
        super.addNotify();
        ExplorerManager em = ExplorerManager.find(this);
        em.setRootContext(rootNode);
        setDefaultColumnSizes();
    }

    public void setResults(List results) {
        this.results = results;
        rootNode = new RevisionsRootNode();
        ExplorerManager em = ExplorerManager.find(this);
        if (em != null) {
            em.setRootContext(rootNode);
        }
    }
    
    private class RevisionsRootNode extends AbstractNode {
    
        public RevisionsRootNode() {
            super(new RevisionsRootNodeChildren(), Lookups.singleton(results));
        }

        public String getName() {
            return "revision"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(DiffTreeTable.class, "LBL_DiffTree_Column_Name");  // NOI18N
        }

        public String getShortDescription() {
            return NbBundle.getMessage(DiffTreeTable.class, "LBL_DiffTree_Column_Name_Desc");  // NOI18N
        }
    }

    private class RevisionsRootNodeChildren extends Children.Keys {
    
        public RevisionsRootNodeChildren() {
        }

        protected void addNotify() {
            refreshKeys();
        }

        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
    
        private void refreshKeys() {
            setKeys(results);
        }
    
        protected Node[] createNodes(Object key) {
            RevisionNode node;
            if (key instanceof SearchHistoryPanel.ResultsContainer) {
                node = new RevisionNode((SearchHistoryPanel.ResultsContainer) key);
            } else { // key instanceof SearchHistoryPanel.DispRevision
                node = new RevisionNode(((SearchHistoryPanel.DispRevision) key));
            }
            return new Node[] { node };
        }
    }

    private class NoLeafIconRenderDataProvider implements RenderDataProvider {
        private RenderDataProvider delegate;
        public NoLeafIconRenderDataProvider( RenderDataProvider delegate ) {
            this.delegate = delegate;
}

        public String getDisplayName(Object o) {
            return delegate.getDisplayName(o);
        }

        public boolean isHtmlDisplayName(Object o) {
            return delegate.isHtmlDisplayName(o);
        }

        public Color getBackground(Object o) {
            return delegate.getBackground(o);
        }

        public Color getForeground(Object o) {
            return delegate.getForeground(o);
        }

        public String getTooltipText(Object o) {
            return delegate.getTooltipText(o);
        }

        public Icon getIcon(Object o) {
            if (getOutline().getOutlineModel().isLeaf(o)) {
                return NO_ICON;
            }
            return null;
        }

    }

    private static final Icon NO_ICON = new NoIcon();

    private static class NoIcon implements Icon {
        public void paintIcon(Component c, Graphics g, int x, int y) { }
        public int getIconWidth() {
            return 0;
        }
        public int getIconHeight() {
            return 0;
        }
    }

}
