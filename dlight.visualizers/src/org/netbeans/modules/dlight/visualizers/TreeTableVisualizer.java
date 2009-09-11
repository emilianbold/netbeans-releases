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
import java.awt.EventQueue;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.impl.TreeTableNode;
import org.netbeans.modules.dlight.spi.impl.TreeTableDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizers.api.ColumnsUIMapping;
import org.netbeans.modules.dlight.visualizers.api.TreeTableVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.TreeTableVisualizerConfigurationAccessor;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author mt154047
 */
class TreeTableVisualizer<T extends TreeTableNode> extends JPanel implements
        Visualizer<TreeTableVisualizerConfiguration>, OnTimerTask, ComponentListener {

    //public static final String IS_CALLS = "TopTenFunctionsIsCalls"; // NOI18N
    private final Object queryLock = new String(TreeTableVisualizer.class + " query lock"); // NOI18N
    private boolean isShown = true;
    private JToolBar buttonsToolbar;
    private JButton refresh;
    private final TreeTableVisualizerConfiguration configuration;
    private final DefaultTreeModel treeModel;
    protected final DefaultMutableTreeNode TREE_ROOT = new DefaultMutableTreeNode("ROOT");////NOI18N
    private JPanel mainPanel = null;
    private TreeModelImpl treeModelImpl;
    private TableModelImpl tableModelImpl;
    private Models.CompoundModel compoundModel;
    protected JComponent treeTableView;
    private final TreeTableDataProvider<T> dataProvider;
    private OnTimerRefreshVisualizerHandler timerHandler;
    protected boolean isEmptyContent;
    protected boolean isLoadingContent;
    private Future<Boolean> task;
    private Future<List<T>> syncFillDataTask;
    private final Object syncFillInLock = new Object();
    private final ColumnsUIMapping columnsUIMapping;

    TreeTableVisualizer(TreeTableVisualizerConfiguration configuration,
            TreeTableDataProvider<T> dataProvider) {
        //timerHandler = new OnTimerRefreshVisualizerHandler(this, 1, TimeUnit.SECONDS);
        this.configuration = configuration;
        this.dataProvider = dataProvider;
        this.columnsUIMapping = TreeTableVisualizerConfigurationAccessor.getDefault().getColumnsUIMapping(configuration);
        treeModel = new DefaultTreeModel(TREE_ROOT);
        setLoadingContent();

    }

    protected void setLoadingContent() {
        isEmptyContent = false;
        isLoadingContent = true;
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(NbBundle.getMessage(AdvancedTableViewVisualizer.class, "Loading")); // NOI18N
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        this.add(label);
        repaint();
        revalidate();
    }

    protected void setEmptyContent() {
        isEmptyContent = true;
        this.removeAll();
        if (mainPanel == null) {
            mainPanel = new JPanel();
        } else {
            mainPanel.removeAll();
        }
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(NbBundle.getMessage(TreeTableVisualizer.class, "NoDataAvailableYet"));//NOI18N
        //(timerHandler.isSessionAnalyzed() ?
        //TableVisualizerConfigurationAccessor.getDefault().getEmptyAnalyzeMessage(configuration) : TableVisualizerConfigurationAccessor.getDefault().getEmptyRunningMessage(configuration)); // NOI18N
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        mainPanel.add(label);
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
        repaint();
        revalidate();

    }

    protected void setContent(final boolean isEmpty) {
        UIThread.invoke(new Runnable() {

            public void run() {
                if (isLoadingContent && isEmpty) {
                    isLoadingContent = false;
                    setEmptyContent();
                    return;
                }
                if (isLoadingContent && !isEmpty) {
                    isLoadingContent = false;
                    setNonEmptyContent();
                    return;
                }
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
        });

    }

    protected void setNonEmptyContent() {
        initComponents();
        updateButtons();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        addComponentListener(this);
        VisualizerTopComponentTopComponent.findInstance().addComponentListener(this);

//        asyncFillModel(configuration.getMetadata().getColumns());

        if (timerHandler != null && timerHandler.isSessionRunning()) {
            timerHandler.startTimer();
            return;

        }

//        if (timerHandler.isSessionAnalyzed() ||
//            timerHandler.isSessionPaused()) {
//            onTimer();
//        }

    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        synchronized (queryLock) {
            if (task != null) {
                task.cancel(false);
                task = null;
            }
        }
        synchronized (syncFillInLock) {
            if (syncFillDataTask != null) {
                syncFillDataTask.cancel(true);
                syncFillDataTask = null;
            }
        }
        if (timerHandler != null) {
            timerHandler.stopTimer();
        }
        removeComponentListener(this);
        VisualizerTopComponentTopComponent.findInstance().removeComponentListener(this);
    }

    protected void setNodes(List<DefaultMutableTreeNode> nodes) {
        synchronized (TREE_ROOT) {
            TREE_ROOT.removeAllChildren();
            fireTreeModelChanged();

            if (nodes != null) {
                for (DefaultMutableTreeNode node : nodes) {
                    TREE_ROOT.add(node);
                }
                fireTreeModelChanged();
            }
        }
    }

    /**
     * Fire treeModelChanged event in AWT Thread
     */
    protected void fireTreeModelChanged() {
        fireTreeModelChanged(null);
    }

    /**
     * Fire treeModelChanged event in AWT Thread
     */
    protected void fireTreeModelChanged(final DefaultMutableTreeNode node) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (node == null) {
                    treeModelImpl.fireTreeModelChanged();
                } else {
                    treeModelImpl.fireTreeModelChanged(node);
                }
            }
        });
    }

    protected void updateButtons() {
    }

    protected JToolBar getButtonsTolbar() {
        return buttonsToolbar;
    }

    protected void initComponents() {
        this.removeAll();
        setLayout(new BorderLayout());
        buttonsToolbar = new JToolBar();
        refresh = new JButton();

        buttonsToolbar.setFloatable(false);
        buttonsToolbar.setOrientation(JToolBar.VERTICAL);
        buttonsToolbar.setRollover(true);

        // Refresh button...
        refresh.setIcon(ImageLoader.loadIcon("refresh.png")); // NOI18N
//    refresh.setToolTipText(org.openide.util.NbBundle.getMessage(PerformanceMonitorViewTopComponent.class, "RefreshActionTooltip")); // NOI18N
        refresh.setFocusable(false);
        refresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refresh.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                asyncFillModel(configuration.getMetadata().getColumns(), false);
            }
        });

        buttonsToolbar.add(refresh);

        add(buttonsToolbar, BorderLayout.LINE_START);
        mainPanel = new JPanel();
        mainPanel.removeAll();
        add(mainPanel, BorderLayout.CENTER);

        TreeTableVisualizerConfigurationAccessor configInfo =
                TreeTableVisualizerConfigurationAccessor.getDefault();


        List<ColumnModel> columns = new ArrayList<ColumnModel>();
        Column[] tableColumns = configInfo.getTableColumns(configuration);

        for (final Column column : tableColumns) {
            columns.add(new TreeTableVisualizerColumnModel(column, columnsUIMapping));
        }

        Column treeColumn = configInfo.getTreeColumn(configuration);
        columns.add(new TreeTableVisualizerColumnModel(treeColumn, columnsUIMapping) {

            @Override
            public Class getType() {
                return null;
            }
        });

        treeModelImpl = new TreeModelImpl();
        tableModelImpl = new TableModelImpl();
        NodeActionsProvider nodesActionProvider =
                configInfo.getNodesActionProvider(configuration);

        List<Model> models = new ArrayList<Model>();
        models.add(treeModelImpl);
        models.add(tableModelImpl);
        models.addAll(columns);
        models.add(new NodeModelImpl());
        if (nodesActionProvider != null) {
            models.add(nodesActionProvider);
        }

        compoundModel = Models.createCompoundModel(models);
        treeTableView = Models.createView(compoundModel);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(treeTableView, BorderLayout.CENTER);

        treeModelImpl.fireTreeModelChanged();
        //we should find JTable and set new Renderer
        //tableModelImpl.fireTableValueChanged();
        mainPanel.repaint();
        repaint();
        revalidate();
    }

    protected final void asyncFillModel(final List<Column> columns, boolean cancelIfNotDone) {
        synchronized (queryLock) {
            if (task != null && !task.isDone()) {
                if (cancelIfNotDone) {
                    task.cancel(true);
                } else {
                    return;
                }
            }

            task = DLightExecutorService.submit(new Callable<Boolean>() {

                public Boolean call() {
                    syncFillModel(columns);
                    return Boolean.TRUE;
                }
            }, "Async TreeTableVisualizer model fill " + configuration.getID()); // NOI18N
        }

    }

    protected void syncFillModel(final List<Column> columns) {
        List<T> list = dataProvider.getTableView(columns, null, Integer.MAX_VALUE);
        updateList(list);
    }

    protected void updateList(List<T> list) {
        if (list == null) {
            return;
        }

        List<DefaultMutableTreeNode> nodes =
                new ArrayList<DefaultMutableTreeNode>(list.size());

        for (T value : list) {
            nodes.add(new DefaultMutableTreeNode(value));
        }

        setNodes(nodes);
    }

    protected void loadTree(final DefaultMutableTreeNode rootNode,
            final List<T> path) {
        //we should show Loading Node
        //this.functionsCallTreeModel.get
        Runnable r = new Runnable() {

            public void run() {
                final List<T> result = dataProvider.getChildren(path, configuration.getMetadata().getColumns(), null);
                UIThread.invoke(new Runnable() {

                    public void run() {
                        updateTree(rootNode, result);
                    }
                });
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            DLightExecutorService.submit(r,
                    "Loading data for TreeTableVisualizer " + configuration.getID()); // NOI18N
        } else {
            r.run();
        }

    }

    protected void updateTree(final DefaultMutableTreeNode rootNode,
            List<T> result) {
        //add them all as a children to rootNode
        rootNode.removeAllChildren();
        if (result != null) {
            for (T value : result) {
                rootNode.add(new DefaultMutableTreeNode(value));
            }

        }

        fireTreeModelChanged();
    }

    protected boolean isShown() {
        return isShown;
    }

    public int onTimer() {
        if (!isShown() || !isShowing()) {
            return 0;
        }

        syncFillModel(configuration.getMetadata().getColumns());
        return 0;
    }

    public VisualizerContainer getDefaultContainer() {
        return VisualizerTopComponentTopComponent.findInstance();
    }

    public TreeTableVisualizerConfiguration getVisualizerConfiguration() {
        return configuration;
    }

    public JComponent getComponent() {
        return this;
    }

    public void timerStopped() {
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
        // Fill model only in case we are really became visible ...
//        if (isShown) {
//            // We are in the AWT thread. Need to update model out of it.
//            asyncFillModel(configuration.getMetadata().getColumns(), true);
//        }

    }

    public void refresh() {
        asyncFillModel(configuration.getMetadata().getColumns(), false);
    }

    public void componentHidden(ComponentEvent e) {
        isShown = false;
    }

    protected class TreeModelImpl implements TreeModel, TreeExpansionModel {

        private final Object listenersLock = new Object();
        private Vector<ModelListener> listeners = new Vector<ModelListener>();

        public Object getRoot() {
            return ROOT;
        }

        public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
            //throw new UnsupportedOperationException("Not supported yet.");
//      if (parent == ROOT) {
            Object real_parent = parent;
            if (parent == ROOT) {
                real_parent = TREE_ROOT;
            }
            //return functionsList.getChildren(null).toArray();
//        return functionsCallTreeModel.get`
            if (real_parent instanceof DefaultMutableTreeNode) {
                List<Object> result = new ArrayList<Object>();
                for (int i = from; i <= to; i++) {
                    if (i >= 0 && i < treeModel.getChildCount(real_parent)) {
                        result.add(treeModel.getChild(real_parent, i));
                    }
                }
                return result.toArray();
            }

            throw new UnknownTypeException(parent);
        }

        void fireTreeModelChanged(DefaultMutableTreeNode node) {
            synchronized (listenersLock) {
                for (ModelListener l : listeners) {
                    l.modelChanged(new ModelEvent.NodeChanged(TreeTableVisualizer.this, node));
                }
            }
        }

        void fireTreeModelChanged() {
            synchronized (listenersLock) {
                for (ModelListener l : listeners) {
                    l.modelChanged(new ModelEvent.TreeChanged(TreeTableVisualizer.this));
                    l.modelChanged(new ModelEvent.NodeChanged(TreeTableVisualizer.this, ROOT));
                }
            }


        }

        public boolean isLeaf(Object node) {
            if (node == ROOT) {
                return false;
            }
            if (TreeTableVisualizerConfigurationAccessor.getDefault().isTableView(configuration)) {
                return true;
            }
            return timerHandler != null && timerHandler.isSessionRunning();
        }

        public int getChildrenCount(Object node) throws UnknownTypeException {
            Object real_node = node;
            if (node == ROOT) {
                real_node = TREE_ROOT;
                return treeModel.getChildCount(real_node);
            }

            if (real_node instanceof DefaultMutableTreeNode) {
                if (TreeTableVisualizerConfigurationAccessor.getDefault().isTableView(configuration)) {
                    return 0;
                }
                return 1;
            }
            return 0;
        }

        public void addModelListener(ModelListener l) {
            synchronized (listenersLock) {
                listeners.add(l);
            }
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeModelListener(ModelListener l) {
            synchronized (listenersLock) {
                listeners.remove(l);
            }
        }

        public boolean isExpanded(Object node) throws UnknownTypeException {
            //throw new UnsupportedOperationException("Not supported yet.");
            return false;
        }

        public void nodeExpanded(Object node) {
            if (node == ROOT) {
                return;
            }

            if (!(node instanceof DefaultMutableTreeNode)) {
                return;
            }

            DefaultMutableTreeNode tNode = (DefaultMutableTreeNode) node;
            List<T> result = Arrays.asList((T) tNode.getUserObject());
            loadTree(tNode, result);
        }

        public void nodeCollapsed(Object node) {
            //System.out.println("nodeCollapsed invoked " + node);
        }
    }

    class TableModelImpl implements TableModel {

        private Vector<ModelListener> listeners = new Vector<ModelListener>();

        public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
            if (!(node instanceof DefaultMutableTreeNode)) {
                throw new UnknownTypeException(node);
            }
//      if ("iconID".equals(columnID)) {
//        return new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/dlight/resources/who_calls.png"));
//      }

            DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) node;
            Object nodeObject = theNode.getUserObject();

            if (nodeObject instanceof TreeTableNode) {
                //return ((T)nodeObject).getMetricValue(getMetricByID(columnID));
                return ((TreeTableNode) nodeObject).getValue(columnID);
            }

            return "";
        }

        public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
            //throw new UnsupportedOperationException("Not supported yet.");
            return true;
        }

        public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addModelListener(ModelListener l) {
            //throw new UnsupportedOperationException("Not supported yet.");
            listeners.add(l);
        }

        void fireTableValueChanged() {
        }

        public void removeModelListener(ModelListener l) {
            listeners.remove(l);
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    protected String getIcon(T node) {
        return null;
    }

    class NodeModelImpl implements ExtendedNodeModel {

        private final Object nodesMapLock = new Object();
        private final Object listenersLock = new Object();
        private Vector<ModelListener> listeners = new Vector<ModelListener>();
        private Map<String, String> nodes = new HashMap<String, String>();

        public String getDisplayName(Object node) {
            if (node == TreeModel.ROOT) {
                String treeColumnDisplayedName = TreeTableVisualizerConfigurationAccessor.getDefault().getTreeColumn(configuration).getColumnUName();
                return treeColumnDisplayedName;
            }
            final Object finalNodeObject = node;
            if (node instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                final Object nodeObject = treeNode.getUserObject();
                //we should check type here
                String result = "";
                if (nodeObject instanceof TreeTableNode) {
                    final Object value = ((TreeTableNode) nodeObject).getValue();
                    synchronized (nodesMapLock) {
                        if (nodes.containsKey(value + "")) {
                            return nodes.get(value + "");
                        }
                    }
                    if (TreeTableVisualizerConfigurationAccessor.getDefault().getTreeColumn(configuration).getColumnClass() == String.class) {
                        return value + "";
                    }

                    DLightExecutorService.submit(new Runnable() {

                        public void run() {
                            PropertyEditor editor = PropertyEditorManager.findEditor(TreeTableVisualizerConfigurationAccessor.getDefault().getTreeColumn(configuration).getColumnClass());
                            if (editor != null) {
                                editor.setValue(value);
                                synchronized (nodesMapLock) {
                                    nodes.put(value + "", editor.getAsText());
                                }
                                fireNodeModelChanged(finalNodeObject);
                            }
                        }
                    }, "insight getDisplayName.. "); // NOI18N
                    return "...";//NOI18N

                } else {
                    result = nodeObject.toString();
                }
                return result;
            }
            return "Unknown";//NOI18N
        }

        @SuppressWarnings("unchecked")
        public String getIconBase(Object node) {
            if (node == TreeModel.ROOT) {
                return null;
            }
            final Object finalNodeObject = node;
            if (node instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                final Object nodeObject = treeNode.getUserObject();
                //we should check type here
                if (nodeObject instanceof TreeTableNode) {
                    return getIcon((T) nodeObject);
                }
            }
            return null;
        }

        public String getShortDescription(Object node) {
            if (node == TreeModel.ROOT) {
                return TreeTableVisualizerConfigurationAccessor.getDefault().getTreeColumn(configuration).getColumnUName();
            }
            if (node instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                return ((TreeTableNode) treeNode.getUserObject()).getValue() + "";
            }

            return "Unknown";//NOI18N
        }

        void fireNodeModelChanged(final Object node) {

            if (EventQueue.isDispatchThread()) {
                synchronized (listenersLock) {
                    for (ModelListener l : listeners) {
                        l.modelChanged(new ModelEvent.NodeChanged(NodeModelImpl.this, node));
                    }
                }
            } else {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        synchronized (listenersLock) {
                            for (ModelListener l : listeners) {
                                l.modelChanged(new ModelEvent.NodeChanged(NodeModelImpl.this, node));
                            }
                        }

                    }
                });
            }



        }

        public void addModelListener(ModelListener l) {
            //    throw new UnsupportedOperationException("Not supported yet.");
            synchronized (listenersLock) {
                if (listeners.contains(l)) {
                    return;
                }
                listeners.add(l);
            }
        }

        public void removeModelListener(ModelListener l) {
            synchronized (listenersLock) {
                listeners.remove(l);
            }
        }

        public boolean canRename(Object arg0) throws UnknownTypeException {
            return false;
        }

        public boolean canCopy(Object arg0) throws UnknownTypeException {
            return false;
        }

        public boolean canCut(Object arg0) throws UnknownTypeException {
            return false;
        }

        public Transferable clipboardCopy(Object arg0) throws IOException, UnknownTypeException {
            return null;
        }

        public Transferable clipboardCut(Object arg0) throws IOException, UnknownTypeException {
            return null;
        }

        public PasteType[] getPasteTypes(Object arg0, Transferable arg1) throws UnknownTypeException {
            return null;
        }

        public void setName(Object node, String name) throws UnknownTypeException {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getIconBaseWithExtension(Object arg0) throws UnknownTypeException {
//      return CsmImageName.FUNCTION_GLOBAL;
            return null;
        }
    }
}

