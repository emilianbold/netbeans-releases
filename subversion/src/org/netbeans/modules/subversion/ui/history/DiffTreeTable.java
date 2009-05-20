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
package org.netbeans.modules.subversion.ui.history;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

import javax.swing.*;
import java.util.*;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.swing.outline.RenderDataProvider;
import org.openide.explorer.view.OutlineView;

/**
 * Treetable to show results of Search History action.
 * 
 * @author Maros Sandor
 */
class DiffTreeTable extends OutlineView {
    
    private RevisionsRootNode rootNode;
    private List results;
    private final SearchHistoryPanel master;

    public DiffTreeTable(SearchHistoryPanel master) {
        this.master = master;
        getOutline().setShowHorizontalLines(true);
        getOutline().setShowVerticalLines(false);
        getOutline().setRootVisible(false);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setupColumns();

        getOutline().setRenderDataProvider( new NoLeafIconRenderDataProvider( getOutline().getRenderDataProvider() ) );
    }
    
    private void setupColumns() {
        Node.Property [] columns = new Node.Property[3];
        ResourceBundle loc = NbBundle.getBundle(DiffTreeTable.class);
        columns[0] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_DATE, String.class, loc.getString("LBL_DiffTree_Column_Time"), loc.getString("LBL_DiffTree_Column_Time_Desc"));
        columns[1] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_USERNAME, String.class, loc.getString("LBL_DiffTree_Column_Username"), loc.getString("LBL_DiffTree_Column_Username_Desc"));
        columns[2] = new ColumnDescriptor(RevisionNode.COLUMN_NAME_MESSAGE, String.class, loc.getString("LBL_DiffTree_Column_Message"), loc.getString("LBL_DiffTree_Column_Message_Desc"));
        setProperties(columns);
    }
    
    private void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int width = getWidth();
                getOutline().getColumnModel().getColumn(0).setPreferredWidth(width * 25 / 100);
                getOutline().getColumnModel().getColumn(1).setPreferredWidth(width * 15 / 100);
                getOutline().getColumnModel().getColumn(2).setPreferredWidth(width * 10 / 100);
                getOutline().getColumnModel().getColumn(3).setPreferredWidth(width * 50 / 100);
            }
        });
    }

    void setSelection(int idx) {
        getOutline().getSelectionModel().setValueIsAdjusting(false);
        getOutline().scrollRectToVisible(getOutline().getCellRect(idx, 1, true));
        getOutline().getSelectionModel().setSelectionInterval(idx, idx);
    }

    void setSelection(RepositoryRevision container) {
        RevisionNode node = (RevisionNode) getNode(rootNode, container);
        if (node == null) return;
        ExplorerManager em = ExplorerManager.find(this);
        try {
            em.setSelectedNodes(new Node [] { node });
        } catch (PropertyVetoException e) {
            Subversion.LOG.log(Level.SEVERE, null, e);
        }
    }

    void setSelection(RepositoryRevision.Event revision) {
        RevisionNode node = (RevisionNode) getNode(rootNode, revision);
        if (node == null) return;
        ExplorerManager em = ExplorerManager.find(this);
        try {
            em.setSelectedNodes(new Node [] { node });
        } catch (PropertyVetoException e) {
            Subversion.LOG.log(Level.SEVERE, null, e);
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

    private static class ColumnDescriptor<T> extends PropertySupport.ReadOnly<T> {
        
        public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public T getValue() throws IllegalAccessException, InvocationTargetException {
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
            return NbBundle.getMessage(DiffTreeTable.class, "LBL_DiffTree_Column_Name"); // NOI18N
        }

        public String getShortDescription() {
            return NbBundle.getMessage(DiffTreeTable.class, "LBL_DiffTree_Column_Name_Desc"); // NOI18N
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
            if (key instanceof RepositoryRevision) {
                node = new RevisionNode((RepositoryRevision) key, master);
            } else { // key instanceof RepositoryRevision.Event
                node = new RevisionNode(((RepositoryRevision.Event) key), master);
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
            if( getOutline().getOutlineModel().isLeaf(o) )
                return NO_ICON;
            return null;
        }

    }
    private static final Icon NO_ICON = new NoIcon();
    private static class NoIcon implements Icon {

        public void paintIcon(Component c, Graphics g, int x, int y) {

        }

        public int getIconWidth() {
            return 0;
        }

        public int getIconHeight() {
            return 0;
        }
    }
}
