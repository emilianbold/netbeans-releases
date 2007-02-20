/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.localhistory.ui.view;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.localhistory.store.LocalHistoryStore;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.TreeTableView;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class LocalHistoryFileView implements PropertyChangeListener {
           
    private FileTablePanel tablePanel;             
    private File[] files;
        
    private RequestProcessor.Task refreshTask;        
    private RefreshTable refreshTable;

    public LocalHistoryFileView(File[] files) {                       
        this(files, -1);
    }    
    
    public LocalHistoryFileView(File[] files, long toSelect) {                       
        tablePanel = new FileTablePanel();        
        LocalHistory.getInstance().getLocalHistoryStore().addPropertyChangeListener(this); // XXX remove listener
        this.files = files;                        
        refreshTablePanel(toSelect);                                                               
    }            
        
    public void propertyChange(PropertyChangeEvent evt) {
        if(LocalHistoryStore.PROPERTY_CHANGED.equals(evt.getPropertyName())) {
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
        LocalHistory.getInstance().getLocalHistoryStore().removePropertyChangeListener(this);
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
    
    private void storeChanged(PropertyChangeEvent evt) {
        Object newValue = evt.getNewValue();
        Object oldValue = evt.getOldValue();
        if( newValue != null && contains( (File) newValue ) || 
            oldValue != null && contains( (File) oldValue ) ) 
        {
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
        if(refreshTask == null) {
            refreshTable = new RefreshTable();
            RequestProcessor rp = new RequestProcessor();
            refreshTask = rp.create(refreshTable);            
        }
        refreshTable.setup(toSelect);
        refreshTask.schedule(100);
    }    
    
    private void selectNodes(final Node[] nodes) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    tablePanel.getExplorerManager().setSelectedNodes(nodes);
                } catch (PropertyVetoException ex) {
                    // ignore
                }                                     
            }
        });                                             
    }                     

    /**
     * Selects a node with the timestamp = toSelect, otherwise the selection stays.
     * If there wasn't a selection set yet then the first node will be selected.
     */ 
    private class RefreshTable implements Runnable {        
        private long toSelect;
        void setup(long toSelect) {
            this.toSelect = toSelect;
        }
        public void run() {                        
            Node oldExploredContext = getExplorerManager().getExploredContext();
            Node root = LocalHistoryRootNode.createRootNode(files);
            
            Node[] oldSelection = getExplorerManager().getSelectedNodes();
            tablePanel.getExplorerManager().setRootContext(root);
                        
            if(toSelect > -1) {
                Node node = getNode(toSelect);
                if(node != null) {
                    oldSelection = new Node[] { node };
                }                
            }             
            
            if (oldSelection != null && oldSelection.length > 0) {                        
                Node[] newSelection = getEqualNodes(root, oldSelection);                        
                if(newSelection.length > 0) {
                    selectNodes(newSelection);
                } else {
                    if(oldExploredContext != null) {
                        Node[] newExploredContext = getEqualNodes(root, new Node[] { oldExploredContext });                           
                        if(newExploredContext.length > 0) {
                            selectFirstNeighborNode(newExploredContext[0], oldSelection[0]);                                    
                        }                                
                    }                       
                }
            } else {
                selectFirstNode(root);
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
                    selectNodes(new Node[]{ nodes[0] });
                }
            }        
        }

        private void selectFirstNeighborNode(Node context, Node oldSelection) {
            tablePanel.getExplorerManager().setExploredContext(context);
            Node[] children = context.getChildren().getNodes();
            if(children.length > 0 && children[0] instanceof Comparable) {
                Node[] newSelection = new Node[] { children[0] } ;
                for(int i = 1; i < children.length; i++) {                                            
                    Comparable c = (Comparable) children[i];
                    if( c.compareTo(oldSelection) < 0 ) {
                       newSelection[0] = children[i]; 
                    }                                            
                }    
                selectNodes(newSelection);
            }        
        }   
    } 
    
    // XXX reuse in folder view
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

        private class BrowserTreeTableView extends TreeTableView {    
            BrowserTreeTableView() {
                setupColumns();

                tree.setShowsRootHandles(true);
                tree.setRootVisible(false);                    
                setDefaultActionAllowed (false);
                setBorder(BorderFactory.createEtchedBorder());
        //        treeView.getAccessibleContext().setAccessibleDescription(browserAcsd);
        //        treeView.getAccessibleContext().setAccessibleName(browserAcsn);           
                setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);            
                setPopupAllowed(true);    

                DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
                renderer.setLeafIcon(null);
                tree.setCellRenderer(renderer);

            }

            JTree getTree() {            
                return tree;
            } 

            JTable getTable() {            
                return treeTable;
            } 

            public void startEditingAtPath(TreePath path) {            
                tree.startEditingAtPath(path);            
            }         

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
                            treeTable.getColumnModel().getColumn(0).setPreferredWidth(width * 35 / 100);
                            treeTable.getColumnModel().getColumn(1).setPreferredWidth(width * 65 / 100);                        
                    }
                });
            }            
        }     

        int rowAtPoint(Point point) {
            return treeView.getTable().rowAtPoint(point);
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
}
