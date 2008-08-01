/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.localhistory.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.beans.PropertyVetoException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.localhistory.store.LocalHistoryStore;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.swing.outline.RenderDataProvider;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class LocalHistoryFileView implements VersioningListener {
           
    private FileTablePanel tablePanel;             
    private File[] files;

    private RequestProcessor rp = new RequestProcessor("LocalHistoryView", 1, true);    
    
    public LocalHistoryFileView() {                       
        tablePanel = new FileTablePanel();        
        LocalHistory.getInstance().getLocalHistoryStore().addVersioningListener(this); 
    }            

    public void refresh(File[] files) {
        refresh(files, -1);
    }
    
    public void refresh(File[] files, long toSelect) {
        this.files = files;                        
        refreshTablePanel(toSelect);                       
    }

    public void versioningEvent(VersioningEvent evt) {
        if(LocalHistoryStore.EVENT_HISTORY_CHANGED == evt.getId() ||
           LocalHistoryStore.EVENT_ENTRY_DELETED == evt.getId()     ) {
            storeChanged(evt);
        }
    }    
    
    public ExplorerManager getExplorerManager() {
        return tablePanel.getExplorerManager();
    }
    
    public StoreEntry[] getSelectedEntries() {
        Node[] nodes = tablePanel.getExplorerManager().getSelectedNodes();
        if(nodes != null && nodes.length > 0) {
            List<StoreEntry> entries = new ArrayList<StoreEntry>();            
            for(Node node : nodes) {                
                entries.add(node.getLookup().lookup(StoreEntry.class));    
            }
            return entries.toArray(new StoreEntry[entries.size()]);
        } 
        return new StoreEntry[0];        
    }
    
    public JPanel getPanel() {
        return tablePanel;
    }
    
    public void close() {
        LocalHistory.getInstance().getLocalHistoryStore().removeVersioningListener(this);
    }    
    
    private Node getNode(long ts) {
        if(ts == -1) return null;
        Node root = tablePanel.getExplorerManager().getRootContext();
        Node[] dayNodes = root.getChildren().getNodes();
        if(dayNodes != null && dayNodes.length > 0) {                        
            for(Node dayNode : dayNodes) {                
                Node[] entryNodes = dayNode.getChildren().getNodes();
                if(entryNodes != null && entryNodes.length > 0) {
                    for(Node entryNode : entryNodes) {
                        StoreEntry se = entryNode.getLookup().lookup(StoreEntry.class);
                        if(se != null && se.getTimestamp() == ts) {                            
                            return entryNode;
                        }                                       
                    }                    
                }
            }    
        }        
        return null;
    }
    
    private void storeChanged(VersioningEvent evt) {
        Object value = evt.getParams()[0];
        if(value != null && contains((File)value)) {
            refreshTablePanel(-1);   
        } 
    }

    private boolean contains(File file) {
        for(File f : files) {
            if(f.equals(file)) {
                return true;
            }
        }
        return false;
    }                
    
    private void refreshTablePanel(long toSelect) {                  
        RequestProcessor.Task refreshTask = rp.create(new RefreshTable(toSelect));                    
        refreshTask.schedule(100);
    }    
                     
    /**
     * Selects a node with the timestamp = toSelect, otherwise the selection stays.
     * If there wasn't a selection set yet then the first node will be selected.
     */ 
    private class RefreshTable implements Runnable {        
        private long toSelect;
        RefreshTable(long toSelect) {
            this.toSelect = toSelect;
        }
        public void run() {                        
            Node oldExploredContext = getExplorerManager().getExploredContext();
            Node root = LocalHistoryRootNode.createRootNode(files);
            
            Node[] oldSelection = getExplorerManager().getSelectedNodes();            
            tablePanel.getExplorerManager().setRootContext(root);

            if(root.getChildren().getNodesCount() > 0) {                
                if(toSelect > -1) {
                    Node node = getNode(toSelect);
                    if(node != null) {
                        oldSelection = new Node[] { node };
                    }                
                }             

                if (oldSelection != null && oldSelection.length > 0) {                        
                    Node[] newSelection = getEqualNodes(root, oldSelection);                        
                    if(newSelection.length > 0) {
                        setNodes(root, newSelection);
                    } else {
                        if(oldExploredContext != null) {
                            Node[] newExploredContext = getEqualNodes(root, new Node[] { oldExploredContext });                           
                            if(newExploredContext.length > 0) {
                                selectFirstNeighborNode(root, newExploredContext[0], oldSelection[0]);                                    
                            }                                
                        }                       
                    }
                } else {
                    selectFirstNode(root);
                }
            } else {
                setNodes(root, new Node[]{});
            }   
            tablePanel.revalidate();
            tablePanel.repaint();
        }
        
        private Node[] getEqualNodes(Node root, Node[] oldNodes) {    
            List<Node> ret = new ArrayList<Node>();
            for(Node on : oldNodes) {
                Node node = findEqualInChildren(root, on);
                if(node != null) {
                    ret.add(node);                                
                }                    
            }            
            return ret.toArray(new Node[ret.size()]);                            
        }                   
        
        private Node findEqualInChildren(Node node, Node toFind) {
            Node[] children = node.getChildren().getNodes();
            for(Node child : children) {
                if(toFind.getName().equals(child.getName())) {
                    return child;                
                }
                Node n = findEqualInChildren(child, toFind);
                if(n != null) {
                    return n;
                }                 
            }
            return null;
        } 

        private void selectFirstNode(final Node root) {        
            Node[] dateFolders = root.getChildren().getNodes();
            if (dateFolders != null && dateFolders.length > 0) {
                final Node[] nodes = dateFolders[0].getChildren().getNodes();
                if (nodes != null && nodes.length > 0) {                
                    setNodes(root, new Node[]{ nodes[0] });
                }
            }        
        }

        private void selectFirstNeighborNode(Node root, Node context, Node oldSelection) {            
            Node[] children = context.getChildren().getNodes();
            if(children.length > 0 && children[0] instanceof Comparable) {
                Node[] newSelection = new Node[] { children[0] } ;
                for(int i = 1; i < children.length; i++) {                                            
                    Comparable c = (Comparable) children[i];
                    if( c.compareTo(oldSelection) < 0 ) {
                       newSelection[0] = children[i]; 
                    }                                            
                }    
                setNodes(root, newSelection);
                tablePanel.getExplorerManager().setExploredContext(context);                                
            }        
        }   
        
        private void setNodes(final Node root, final Node[] nodes) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        tablePanel.getExplorerManager().setRootContext(root);
                        tablePanel.getExplorerManager().setSelectedNodes(nodes);
                    } catch (PropertyVetoException ex) {
                        // ignore
                    }
                }
            });                                             
        }            
    } 
    
    private class FileTablePanel extends JPanel implements ExplorerManager.Provider {

        private final BrowserTreeTableView treeView;    
        private final ExplorerManager manager;

        public FileTablePanel() { 
            manager = new ExplorerManager();

            setLayout(new GridBagLayout());

            treeView = new BrowserTreeTableView();             
            setLayout(new BorderLayout());
            add(treeView, BorderLayout.CENTER);
        }   

        public ExplorerManager getExplorerManager() {
            return manager;
        }

        private class BrowserTreeTableView extends OutlineView {    
            BrowserTreeTableView() {
                super( NbBundle.getMessage(LocalHistoryFileView.class, "LBL_LocalHistory_Column_Version")); //NOI18N
                setupColumns();

                getOutline().setRootVisible(false);                    
                setBorder(BorderFactory.createEtchedBorder());
        //        treeView.getAccessibleContext().setAccessibleDescription(browserAcsd);
        //        treeView.getAccessibleContext().setAccessibleName(browserAcsn);           
                getOutline().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);            
                setPopupAllowed(true);    
                setDragSource(false);
                setDropTarget(false);
                getOutline().setColumnHidingAllowed(false);

                getOutline().setRenderDataProvider( new NoLeafIconRenderDataProvider( getOutline().getRenderDataProvider() ) );
            }

            @Override
            public void addNotify() {
                super.addNotify();
                setDefaultColumnSizes();
            }

            private void setupColumns() {
                ResourceBundle loc = NbBundle.getBundle(FileTablePanel.class);            

                Node.Property [] columns = new Node.Property[1];
                columns = new Node.Property[1];
                columns[0] = new ColumnDescriptor<String>(
                                StoreEntryNode.PROPERTY_NAME_LABEL, 
                                String.class, 
                                loc.getString("LBL_LocalHistory_Column_Label"),            // NOI18N            
                                loc.getString("LBL_LocalHistory_Column_Label_Desc"));      // NOI18N            
                setProperties(columns);
            }    

            private void setDefaultColumnSizes() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        int width = getWidth();                    
                            getOutline().getColumnModel().getColumn(0).setPreferredWidth(width * 35 / 100);
                            getOutline().getColumnModel().getColumn(1).setPreferredWidth(width * 65 / 100);                        
                    }
                });
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
        }     
    }    
    
    private static class ColumnDescriptor<T> extends PropertySupport.ReadOnly<T> {        
        public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }
        public T getValue() throws IllegalAccessException, InvocationTargetException {
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
