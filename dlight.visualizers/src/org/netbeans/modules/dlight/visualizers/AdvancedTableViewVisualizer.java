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
package org.netbeans.modules.dlight.visualizers;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.spi.impl.TableDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizers.api.AdvancedTableViewVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.AdvancedTableViewVisualizerConfigurationAccessor;
import org.openide.explorer.view.NodeTableModel;
import org.openide.explorer.view.TableView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mt154047
 */
final class AdvancedTableViewVisualizer extends JPanel implements
    Visualizer<AdvancedTableViewVisualizerConfiguration>, OnTimerTask, ComponentListener {

    private TableDataProvider provider;
    private AdvancedTableViewVisualizerConfiguration configuration;
    private final List<DataRow> data = new ArrayList<DataRow>();
    private JToolBar buttonsToolbar;
    private JButton refresh;
//    private AbstractTableModel tableModel;
//    private JTable table;
//    private TableSorter tableSorterModel = new TableSorter();
    private OnTimerRefreshVisualizerHandler timerHandler;
    private boolean isEmptyContent;
    private boolean isShown = true;
    private NodeTableModel nodeTableModel;
    private TableView tableView;

    AdvancedTableViewVisualizer(TableDataProvider provider, final AdvancedTableViewVisualizerConfiguration configuration) {
       // timerHandler = new OnTimerRefreshVisualizerHandler(this, 1, TimeUnit.SECONDS);
        this.provider = provider;
        this.configuration = configuration;
        setEmptyContent();
        setModels();
        addComponentListener(this);
        tableView = new TableView();
        VisualizerTopComponentTopComponent.findInstance().addComponentListener(this);

    }

    @Override
    public void addNotify() {
        super.addNotify();
        addComponentListener(this);
        VisualizerTopComponentTopComponent.findInstance().addComponentListener(this);
        onTimer();
        if (timerHandler != null && timerHandler.isSessionRunning()) {
            timerHandler.startTimer();
            return;
        }


    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (timerHandler != null){
            timerHandler.stopTimer();
        }
        removeComponentListener(this);
        VisualizerTopComponentTopComponent.findInstance().removeComponentListener(this);

    }

    private void setEmptyContent() {
        isEmptyContent = true;
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(timerHandler != null && timerHandler.isSessionAnalyzed() ? AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getEmptyAnalyzeMessage(configuration) : AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getEmptyRunningMessage(configuration)); // NOI18N
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        this.add(label);
        repaint();
        revalidate();
    }

    private void setContent(boolean isEmpty) {
        if (isEmptyContent && isEmpty) {
            return;
        }
        if (isEmptyContent && !isEmpty) {
            setNonEmptyContent();
            return;
        }
        if (!isEmptyContent && isEmpty) {
            setEmptyContent();
            return;
        }

    }

    protected void updateList(List<DataRow> list) {
        VisualizerTopComponentTopComponent.getDefault().getExplorerManager().setRootContext(new AbstractNode(new DataChildren(list)));
        setNonEmptyContent();   
    //we have liste here, create node model on the base of it
//        //if there is no elements in the list
//        synchronized (TREE_ROOT) {
//            TREE_ROOT.removeAllChildren();
//        }
//        UIThread.invoke(new Runnable() {
//
//            public void run() {
//                treeModelImpl.fireTreeModelChanged();
//            }
//        });
//        if (list != null) {
//            synchronized (TREE_ROOT) {
//                for (DataRow value : list) {
//                    TREE_ROOT.add(new DefaultMutableTreeNode(value));
//                }
//
//            }
//        }
//        UIThread.invoke(new Runnable() {
//
//            public void run() {
//                treeModelImpl.fireTreeModelChanged();
//            }
//        });

    }

    private void setModels() {
        nodeTableModel = new NodeTableModel();
//        List<ColumnModel> columns = new ArrayList<ColumnModel>();
//        List<Column> tableColumns = configuration.getMetadata().getColumns();
//        for (final Column f : tableColumns) {
//            ColumnModel column = new ColumnModel() {
//
//                boolean isVisible = true;
//                boolean isSorted = false;
//                boolean isSortedDescending = false;
//                int currentOrderNumber = -1;
//
//                public String getID() {
//                    return f.getColumnName();
//                }
//
//                public String getDisplayName() {
//                    return f.getColumnUName();
//                }
//
//                public Class getType() {
////                    if (f.getColumnName().equals(AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getNodeColumnName(configuration))) {
////                        return null;
////                    }
//                    return f.getColumnClass();
//                }
//
//                @Override
//                public void setCurrentOrderNumber(int newOrderNumber) {
//                    this.currentOrderNumber = newOrderNumber;
//                }
//
//                @Override
//                public int getCurrentOrderNumber() {
//                    return currentOrderNumber;
//                }
//
//                @Override
//                public void setVisible(boolean arg0) {
//                    this.isVisible = arg0;
//                }
//
//                @Override
//                public boolean isVisible() {
//                    return isVisible;
//                }
//
//                @Override
//                public boolean isSortable() {
//                    return true;
//                }
//
//                @Override
//                public boolean isSorted() {
//                    return isSorted;
//                }
//
//                @Override
//                public void setSorted(boolean sorted) {
//                    this.isSorted = sorted;
//                }
//
//                @Override
//                public void setSortedDescending(boolean sortedDescending) {
//                    this.isSortedDescending = sortedDescending;
//                }
//
//                @Override
//                public boolean isSortedDescending() {
//                    return isSortedDescending;
//                }
//            };
//
//            columns.add(column);
//        }
//
//        List<Model> models = new ArrayList<Model>();
//        treeModelImpl =
//            new TreeModelImpl();
//
//        models.add(treeModelImpl);//tree model
//        tableModelImpl =
//            AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getTableModel(configuration) != null ? AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getTableModel(configuration) : new TableModelImpl();
//        models.add(tableModelImpl);
//        models.addAll(columns);
//        models.add(new NodeModelImpl());
//        if (AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getNodeActionProvider(configuration) != null) {
//            models.add(AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getNodeActionProvider(configuration));
//        }
//        compoundModel =
//            Models.createCompoundModel(models);
    }

    private void setNonEmptyContent() {
        isEmptyContent = false;
        this.removeAll();
        this.setLayout(new BorderLayout());
        buttonsToolbar = new JToolBar();
        refresh = new JButton();

        buttonsToolbar.setFloatable(false);
        buttonsToolbar.setOrientation(1);
        buttonsToolbar.setRollover(true);

        // Refresh button...
        refresh.setIcon(ImageLoader.loadIcon("refresh.png")); // NOI18N
        refresh.setFocusable(false);
        refresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refresh.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                load();
            }
        });

        buttonsToolbar.add(refresh);



        add(buttonsToolbar, BorderLayout.LINE_START);
//        JComponent treeTableView =
//            Models.createView(compoundModel);
//        add(treeTableView, BorderLayout.CENTER);
//        treeModelImpl.fireTreeModelChanged();
        tableView = new TableView();
        add(tableView, BorderLayout.CENTER);


        repaint();
        validate();

    }

    public VisualizerContainer getDefaultContainer() {
        return VisualizerTopComponentTopComponent.findInstance();
    }

    public int onTimer() {
        if (!isShown || !isShowing()) {
            return 0;
        }
        load();
        return 0;
    }

    private void load() {
        //Set node root fo parent explorer manager
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                final List<DataRow> list = provider.queryData(configuration.getMetadata());
                final boolean isEmptyConent = list == null || list.isEmpty();
                UIThread.invoke(new Runnable() {

                    public void run() {
                        setContent(isEmptyConent);
                        if (isEmptyConent) {
                            return;
                        }

                        updateList(list);
                    }
                });
            }
        });
    }

    public AdvancedTableViewVisualizerConfiguration getVisualizerConfiguration() {
        return configuration;
    }

    public JComponent getComponent() {
        return this;
    }

    public void timerStopped() {
        if (isEmptyContent) {
            //should set again to chahe Label message
            setEmptyContent();
        }
    }

    public void componentResized(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
        if (isShown) {
            return;
        }
        isShown = isShowing();
        if (isShown) {
            onTimer();
        }
    }

    public void componentHidden(ComponentEvent e) {
        isShown = false;
    }

//    protected class TreeModelImpl implements TreeModel, TreeExpansionModel {
//
//        private final Object listenersLock = new Object();
//        private Vector<ModelListener> listeners = new Vector<ModelListener>();
//
//        public Object getRoot() {
//            return ROOT;
//        }
//
//        public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
//            //throw new UnsupportedOperationException("Not supported yet.");
////      if (parent == ROOT) {
//            Object real_parent = parent;
//            if (parent == ROOT) {
//                real_parent = TREE_ROOT;
//            }
//            //return functionsList.getChildren(null).toArray();
////        return functionsCallTreeModel.get`
//            if (real_parent instanceof DefaultMutableTreeNode) {
//                List<Object> result = new ArrayList<Object>();
//                for (int i = from; i <= to; i++) {
//                    if (i >= 0 && i < treeModel.getChildCount(real_parent)) {
//                        result.add(treeModel.getChild(real_parent, i));
//                    }
//                }
//                return result.toArray();
//            }
//
//            throw new UnknownTypeException(parent);
//        }
//
//        void fireTreeModelChanged(DefaultMutableTreeNode node) {
//            synchronized (listenersLock) {
//                for (ModelListener l : listeners) {
//                    l.modelChanged(new ModelEvent.NodeChanged(AdvancedTableViewVisualizer.this, node));
//                }
//            }
//        }
//
//        void fireTreeModelChanged() {
//            synchronized (listenersLock) {
//                for (ModelListener l : listeners) {
//                    l.modelChanged(new ModelEvent.TreeChanged(AdvancedTableViewVisualizer.this));
//                    l.modelChanged(new ModelEvent.NodeChanged(AdvancedTableViewVisualizer.this, ROOT));
//                }
//            }
//
//
//        }
//
//        public boolean isLeaf(Object node) {
//            if (node == ROOT) {
//                return false;
//            }
//            return true;
//        }
//
//        public int getChildrenCount(Object node) throws UnknownTypeException {
//            Object real_node = node;
//            if (node == ROOT) {
//                real_node = TREE_ROOT;
//                return treeModel.getChildCount(real_node);
//            }
////
////            if (real_node instanceof DefaultMutableTreeNode) {
////                if (TreeTableVisualizerConfigurationAccessor.getDefault().isTableView(configuration)) {
////                    return 0;
////                }
////                return 1;
////            }
//            return 0;
//        }
//
//        public void addModelListener(ModelListener l) {
//            synchronized (listenersLock) {
//                listeners.add(l);
//            }
//        //throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void removeModelListener(ModelListener l) {
//            synchronized (listenersLock) {
//                listeners.remove(l);
//            }
//        }
//
//        public boolean isExpanded(Object node) throws UnknownTypeException {
//            //throw new UnsupportedOperationException("Not supported yet.");
//            return false;
//        }
//
//        public void nodeExpanded(Object node) {
//            if (node == ROOT) {
//                return;
//            }
//
//        }
//
//        public void nodeCollapsed(Object node) {
//            //System.out.println("nodeCollapsed invoked " + node);
//        }
//    }
//
//    class TableModelImpl implements TableModel {
//
//        private Vector<ModelListener> listeners = new Vector<ModelListener>();
//
//        public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
//            if (!(node instanceof DefaultMutableTreeNode)) {
//                throw new UnknownTypeException(node);
//            }
////      if ("iconID".equals(columnID)) {
////        return new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/dlight/resources/who_calls.png"));
////      }
//
//            DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) node;
//            Object nodeObject = theNode.getUserObject();
//
//            if (nodeObject instanceof DataRow) {
//                //return ((T)nodeObject).getMetricValue(getMetricByID(columnID));
//                return ((DataRow) nodeObject).getStringValue(columnID);
//            }
//
//            return "";
//        }
//
//        public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
//            //throw new UnsupportedOperationException("Not supported yet.");
//            return true;
//        }
//
//        public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
//            //throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void addModelListener(ModelListener l) {
//            //throw new UnsupportedOperationException("Not supported yet.");
//            listeners.add(l);
//        }
//
//        void fireTableValueChanged() {
//        }
//
//        public void removeModelListener(ModelListener l) {
//            listeners.remove(l);
//        //throw new UnsupportedOperationException("Not supported yet.");
//        }
//    }
//
//    class NodeModelImpl implements ExtendedNodeModel {
//
//        public String getDisplayName(Object node) {
//            if (node == TreeModel.ROOT) {
//                return configuration.getMetadata().getColumnByName(AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getNodeColumnName(configuration)).getColumnUName();
//            }
//            if (node instanceof DefaultMutableTreeNode) {
//                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
//                Object nodeObject = treeNode.getUserObject();
//                String result = "";
//                if (nodeObject instanceof DataRow) {
//                    DataRow dataRow = ((DataRow) nodeObject);
//                    String columnName = AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getNodeColumnName(configuration);
////
////                    if (configuration.getMetadata().getColumnByName(columnName).getColumnClass() == MangledNa)
////                    Object value = dataRow.getStringValue();
//                    result = dataRow.getStringValue(columnName);
//                } else {
//                    result = nodeObject.toString();
//                }
//                return result;
//            }
//            return "Unknown";
//        }
//
//        public String getIconBase(Object node) {
//            return null;
//        }
//
//        public String getShortDescription(Object node) {
//            if (node == TreeModel.ROOT) {
//                return configuration.getMetadata().getColumnByName(AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getNodeColumnName(configuration)).getColumnUName();
//            }
//            if (node instanceof DefaultMutableTreeNode) {
//                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
//                Object nodeObject = treeNode.getUserObject();
//                return (nodeObject instanceof DataRow) ? ((DataRow) nodeObject).getStringValue(AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getNodeColumnName(configuration))
//                    : nodeObject.toString();
//            }
//            return "Unknown";
//        }
//
//        public void addModelListener(ModelListener arg0) {
//            //    throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void removeModelListener(ModelListener arg0) {
//            //  throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean canRename(Object arg0) throws UnknownTypeException {
//            return false;
//        }
//
//        public boolean canCopy(Object arg0) throws UnknownTypeException {
//            return false;
//        }
//
//        public boolean canCut(Object arg0) throws UnknownTypeException {
//            return false;
//        }
//
//        public Transferable clipboardCopy(Object arg0) throws IOException, UnknownTypeException {
//            return null;
//        }
//
//        public Transferable clipboardCut(Object arg0) throws IOException, UnknownTypeException {
//            return null;
//        }
//
//        public PasteType[] getPasteTypes(Object arg0, Transferable arg1) throws UnknownTypeException {
//            return null;
//        }
//
//        public void setName(Object arg0, String arg1) throws UnknownTypeException {
//            //throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String getIconBaseWithExtension(Object arg0) throws UnknownTypeException {
////      return CsmImageName.FUNCTION_GLOBAL;
//            return null;
//        }
//    }

    public class DataChildren extends Children.Keys {

        private final List<DataRow> list;

        public DataChildren(List<DataRow> list) {
            this.list = list;
        }

        @Override
        protected Node[] createNodes(Object key) {
            DataRow row = (DataRow)key;
            return new Node[]{new DataRowNode(row)};
        }

        @Override
        protected void addNotify() {
            setKeys(list);
        }
    }

    private class DataRowNode extends AbstractNode {
        private final DataRow dataRow;
        private PropertySet propertySet;
        DataRowNode(DataRow row){
            super(Children.LEAF);
            dataRow = row;
            propertySet = new PropertySet() {
                @Override
                public Property<?>[] getProperties() {
                    List<Property> result = new ArrayList();
                    for (String columnName : dataRow.getColumnNames()){
                        final Column c =  configuration.getMetadata().getColumnByName(columnName);
                        result.add(new PropertySupport(columnName,  c.getColumnClass(),
                            c.getColumnUName(),c.getColumnUName(), true, false) {

                            @Override
                            public Object getValue() throws IllegalAccessException, InvocationTargetException {
                                return dataRow.getData(c.getColumnName());
                            }

                            @Override
                            public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                                //throw new UnsupportedOperationException("Not supported yet.");
                            }
                        });
                    }
                    return result.toArray(new Property[0]);


                }
            };
        }

        @Override
        public String getDisplayName() {
            return super.getDisplayName();
        }

        @Override
        public Object getValue(String attributeName) {
            return super.getValue(attributeName);
        }



        @Override
        public PropertySet[] getPropertySets() {
            return new PropertySet[]{propertySet};
        }

        
    }

    

}
