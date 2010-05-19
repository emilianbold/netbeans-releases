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
package org.netbeans.modules.dlight.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.spi.impl.TableDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizers.api.AdvancedTableViewVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.AdvancedTableViewVisualizerConfigurationAccessor;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author mt154047
 */
final class AdvancedTableViewVisualizer extends JPanel implements
    Visualizer<AdvancedTableViewVisualizerConfiguration>, OnTimerTask, ComponentListener, ExplorerManager.Provider {

    private static final long MIN_REFRESH_MILLIS = 500;

    private TableDataProvider provider;
    private AdvancedTableViewVisualizerConfiguration configuration;
    private final List<DataRow> data = new ArrayList<DataRow>();
    private JButton refresh;
//    private AbstractTableModel tableModel;
//    private JTable table;
//    private TableSorter tableSorterModel = new TableSorter();
    private OnTimerRefreshVisualizerHandler timerHandler;
    private boolean isEmptyContent;
    private boolean isLoadingContent;
    private boolean isShown = true;
    private final OutlineView outlineView;
    private final String nodeColumnName;
    private final String nodeRowColumnID;
    private final ExplorerManager explorerManager;
    private Future<Boolean> task;
    private final Object queryLock = new Object();
    private final Object uiLock = new Object();
    private final String iconColumnID;
    private String resourceID;
    private final boolean dualPaneMode;
    private final DualPaneSupport<DataRow> dualPaneSupport;
    private Map<Integer, Boolean> ascColumnValues = new HashMap<Integer, Boolean>();
    private static final boolean isMacLaf = "Aqua".equals(UIManager.getLookAndFeel().getID()); // NOI18N
    private static final Color macBackground = UIManager.getColor("NbExplorerView.background"); // NOI18N

    AdvancedTableViewVisualizer(TableDataProvider provider, final AdvancedTableViewVisualizerConfiguration configuration) {
        super(new BorderLayout());
        // timerHandler = new OnTimerRefreshVisualizerHandler(this, 1, TimeUnit.SECONDS);
        this.provider = provider;
        this.configuration = configuration;
        this.explorerManager = new ExplorerManager();
        addComponentListener(this);
        AdvancedTableViewVisualizerConfigurationAccessor accessor = AdvancedTableViewVisualizerConfigurationAccessor.getDefault();
//        tableView = new TableView();
        nodeColumnName = accessor.getNodeColumnName(configuration);
        nodeRowColumnID = accessor.getRowNodeColumnName(configuration);
        outlineView = new OutlineView(configuration.getMetadata().getColumnByName(nodeColumnName).getColumnUName());
        outlineView.getOutline().setRootVisible(false);
        iconColumnID = accessor.getIconColumnID(configuration);
        if ( iconColumnID== null || configuration.getMetadata().getColumnByName(iconColumnID) == null){
            outlineView.getOutline().setDefaultRenderer(Object.class, new ExtendedTableCellRendererForNode());//do not display  icon
        }

        resourceID = iconColumnID == null ? null : accessor.getIconPath(configuration);
        List<String> hiddenColumns = accessor.getHiddenColumnNames(configuration);
        List<Property<?>> result = new ArrayList<Property<?>>();
        List<Column> columns = new ArrayList<Column>();
        List<String> columnProperties = new ArrayList<String>();
        for (String columnName : configuration.getMetadata().getColumnNames()) {
            if (!nodeColumnName.equals(columnName) && !nodeRowColumnID.equals(columnName) && !hiddenColumns.contains(columnName)) {
                final Column c = configuration.getMetadata().getColumnByName(columnName);
                columns.add(c);
                @SuppressWarnings("unchecked")
                Property<?> property = new PropertySupport(c.getColumnName(), c.getColumnClass(),
                    c.getColumnUName(), c.getColumnUName(), true, false) {
                    @Override
                    public Object getValue() throws IllegalAccessException, InvocationTargetException {
                        return null;
                    }
                    @Override
                    public void setValue(Object arg0) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    }
                };
                result.add(property);
                columnProperties.add(c.getColumnName());
                columnProperties.add(c.getColumnUName());
            }
        }
        outlineView.getOutline().setDefaultRenderer(Node.Property.class, new FunctionsListSheetCell.OutlineSheetCell(outlineView.getOutline(), columns));
        //outlineView.setProperties(result.toArray(new Property<?>[0]));
        outlineView.setPropertyColumns(columnProperties.toArray(new String[0]));
        outlineView.setPopupAllowed(false);
        outlineView.setDragSource(false);
        outlineView.setDropTarget(false);
        outlineView.setAllowedDragActions(DnDConstants.ACTION_NONE);
        outlineView.setAllowedDropActions(DnDConstants.ACTION_NONE);
        final Outline outline = outlineView.getOutline();
        outline.getTableHeader().setReorderingAllowed(false);
        outline.setRootVisible(false);
        //add Alt+Column Number for sorting
        int columnCount = columns.size() + 1;
        int firstKey = KeyEvent.VK_1;
        for (int i = 1; i <= columnCount; i++) {
            final int columnNumber = i - 1;
            KeyStroke columnKey = KeyStroke.getKeyStroke(firstKey++, KeyEvent.ALT_MASK, true);
            outlineView.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(columnKey, "ascSortFor" + i);//NOI18N
            outlineView.getActionMap().put("ascSortFor" + i, new AbstractAction() {// NOI18N

                public void actionPerformed(ActionEvent e) {
                    // ok, do the sorting
                    int column = columnNumber;
                    ETableColumnModel columnModel = null;
                    if (outline.getColumnModel() instanceof ETableColumnModel) {
                        columnModel = (ETableColumnModel) outline.getColumnModel();
                        columnModel.clearSortedColumns();
                    }
                    boolean asc = !ascColumnValues.containsKey(column) ? true : ascColumnValues.get(column);
                    outline.setColumnSorted(column, asc, 1);
                    ascColumnValues.put(column, !asc);
                    outline.getTableHeader().resizeAndRepaint();
                }
            });
//            KeyStroke columnDescKey = KeyStroke.getKeyStroke(firstKey++, KeyEvent.ALT_MASK + KeyEvent.SHIFT_MASK, true);
//            outlineView.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(columnDescKey, "descSortFor" + i);//NOI18N
//            outlineView.getActionMap().put("descSortFor" + i, new AbstractAction() {// NOI18N
//                public void actionPerformed(ActionEvent e) {
//                    // ok, do the sorting
//                    int column = columnNumber;
//                    ETableColumnModel columnModel = null;
//                    if (outline.getColumnModel() instanceof ETableColumnModel){
//                        columnModel = (ETableColumnModel)outline.getColumnModel();
//                        columnModel.clearSortedColumns();
//                    }
//                   boolean asc = !ascColumnValues.containsKey(column)  ?  false :  ascColumnValues.get(column);
//                    outline.setColumnSorted(column, !asc  , 1);
//                    ascColumnValues.put(column,! asc);
//                    outline.getTableHeader().resizeAndRepaint();
//                }
//            });
        }
        outline.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        outlineView.setProperties(result.toArray(new Property[0]));
        VisualizerTopComponentTopComponent.findInstance().addComponentListener(this);

        KeyStroke returnKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
        outlineView.getOutline().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(returnKey, "return"); // NOI18N
        outlineView.getOutline().getActionMap().put("return", new AbstractAction() {// NOI18N

            public void actionPerformed(ActionEvent e) {
                refresh.requestFocus(false);
            }
        });

        this.dualPaneMode = accessor.isDualPaneMode(configuration);
        if (dualPaneMode) {
            this.dualPaneSupport = DualPaneSupport.forExplorerManager(
                    this, this.explorerManager,
                    accessor.getDetailsRenderer(configuration),
                    new DualPaneSupport.DataAdapter<Node, DataRow>() {
                        public DataRow convert(Node obj) {
                            if (obj instanceof DataRowNode) {
                                return ((DataRowNode)obj).getDataRow();
                            } else {
                                return null;
                            }
                        }
                    });
        } else {
            this.dualPaneSupport = null;
        }
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public void requestFocus() {
        if (refresh != null) {
            refresh.requestFocus();
        } else {
            outlineView.requestFocus();
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        if (refresh != null) {
            return refresh.requestFocusInWindow();
        } else {
            return outlineView.requestFocusInWindow();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
//        addComponentListener(this);
//        VisualizerTopComponentTopComponent.findInstance().addComponentListener(this);
//        asyncFillModel(false);
//        if (timerHandler != null && timerHandler.isSessionRunning()) {
//            timerHandler.startTimer();
//            return;
//        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        synchronized (queryLock) {
            if (task != null) {
                task.cancel(true);
            }
        }
        if (timerHandler != null) {
            timerHandler.stopTimer();
        }
        removeComponentListener(this);
        VisualizerTopComponentTopComponent.findInstance().removeComponentListener(this);

    }

    private void setEmptyContent() {
        isEmptyContent = true;
        this.removeAll();
        JLabel label = new JLabel(timerHandler != null && timerHandler.isSessionAnalyzed() ? AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getEmptyAnalyzeMessage(configuration) : AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getEmptyRunningMessage(configuration), JLabel.CENTER);
        add(label, BorderLayout.CENTER);
        add(createToolbar(), BorderLayout.WEST);
        repaint();
        revalidate();
    }

    private void setLoadingContent() {
        isEmptyContent = false;
        isLoadingContent = true;
        this.removeAll();
        JLabel label = new JLabel(getMessage("Loading"), JLabel.CENTER); // NOI18N
        add(label, BorderLayout.CENTER);
        repaint();
        revalidate();
    }

    private void setContent(boolean isEmpty) {
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
        if (isEmptyContent && !isEmpty) {
            setNonEmptyContent();
            return;
        }
        if (isEmpty) {
            setEmptyContent();
            return;
        }

    }

    protected void updateList(final List<DataRow> list) {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        final boolean isEmptyConent = list == null || list.isEmpty();
        UIThread.invoke(new Runnable() {

            public void run() {
                synchronized (uiLock) {
                    setContent(isEmptyConent);
                    if (!isEmptyConent) {
                        if (!Children.MUTEX.isReadAccess()) {
                            Children.MUTEX.writeAccess(new Runnable() {

                                public void run() {
                                    explorerManager.setRootContext(new AbstractNode(new DataChildren(list)));
                                    setNonEmptyContent();
                                }
                            });
                        }
                    }
                }
            }
        });

    }

    private void setNonEmptyContent() {
        isEmptyContent = false;
        this.removeAll();

        add(createToolbar(), BorderLayout.WEST);
//        JComponent treeTableView =
//            Models.createView(compoundModel);
//        add(treeTableView, BorderLayout.CENTER);
//        treeModelImpl.fireTreeModelChanged();
//        tableView = new TableView();
        add(outlineView, BorderLayout.CENTER);


        repaint();
        validate();
        //    this.setFocusTraversalPolicyProvider(true);
        ArrayList<Component> order = new ArrayList<Component>();
        order.add(outlineView);
        order.add(refresh);
//        FocusTraversalPolicy newPolicy = new MyOwnFocusTraversalPolicy(this, order);
//        setFocusTraversalPolicy(newPolicy);
        refresh.requestFocus();        

    }

    private JToolBar createToolbar() {
        JToolBar buttonsToolbar = new JToolBar();
        if (isMacLaf) {
            buttonsToolbar.setBackground(macBackground);
        }
        buttonsToolbar.setFloatable(false);
        buttonsToolbar.setOrientation(1);
        buttonsToolbar.setRollover(true);

        // Refresh button...
        refresh = new JButton();
        refresh.setIcon(ImageLoader.loadIcon("refresh.png")); // NOI18N
        refresh.setToolTipText(getMessage("Refresh.Tooltip")); // NOI18N
        refresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refresh.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                asyncFillModel(false);
            }
        });

        buttonsToolbar.add(refresh);

        return buttonsToolbar;
    }

    public VisualizerContainer getDefaultContainer() {
        return VisualizerTopComponentTopComponent.findInstance();
    }

    public int onTimer() {
        if (!isShown || !isShowing()) {
            return 0;
        }
//        asyncFillModel();
        return 0;
    }

    public void refresh() {
        asyncFillModel(false);
    }


    private void asyncFillModel(boolean cancelIfNotDone) {
        synchronized (queryLock) {
            if (task != null && !task.isDone()) {
                if (cancelIfNotDone) {
                    task.cancel(true);
                } else {
                    return;
                }
            }

            UIThread.invoke(new Runnable() {
                public void run() {
                    setLoadingContent();
                }
            });

            task = DLightExecutorService.submit(new Callable<Boolean>() {

                public Boolean call() {
                    syncFillModel(true);
                    return Boolean.FALSE;
                }
            }, "AdvancedTableViewVisualizer Async data load for " + configuration.getID()); // NOI18N
        }
    }

    private void syncFillModel(boolean wait) {
        long startTime = System.currentTimeMillis();
        Future<List<DataRow>> queryDataTask = DLightExecutorService.submit(new Callable<List<DataRow>>() {

            public List<DataRow> call() throws Exception {
                return provider.queryData(configuration.getMetadata());
            }
        }, "AdvancedTableViewVisualizer Async data from provider  load for " + configuration.getID()); // NOI18N
        try {
            final List<DataRow> list = queryDataTask.get();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            if (wait && duration < MIN_REFRESH_MILLIS) {
                // ensure that request does not finish too fast -- IZ #172160
                Thread.sleep(MIN_REFRESH_MILLIS - duration);
            }

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
        } catch (ExecutionException ex) {
            Thread.currentThread().interrupt();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public AdvancedTableViewVisualizerConfiguration getVisualizerConfiguration() {
        return configuration;
    }

    public JComponent getComponent() {
        return dualPaneMode? dualPaneSupport : this;
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
//        if (isShown) {
//            //we should change explorerManager
//            onTimer();
//        }
    }

    public void componentHidden(ComponentEvent e) {
        isShown = false;
    }

    public void updateVisualizerConfiguration(AdvancedTableViewVisualizerConfiguration configuration) {
    }

    public class DataChildren extends Children.Keys<DataRow> {

        private final List<DataRow> list;

        public DataChildren(List<DataRow> list) {
            this.list = list;
        }

        @Override
        protected Node[] createNodes(DataRow key) {
            return new Node[]{new DataRowNode(key)};
        }

        @Override
        protected void addNotify() {
            setKeys(list);
        }
    }

    private class DataRowNode extends AbstractNode {

        private final DataRow dataRow;
        private PropertySet propertySet;

        DataRowNode(DataRow row) {
            super(Children.LEAF);
            dataRow = row;
            propertySet = new PropertySet() {

                @Override
                public Property<?>[] getProperties() {
                    List<Property<?>> result = new ArrayList<Property<?>>();
                    for (String columnName : dataRow.getColumnNames()) {
                        if (!columnName.equals(nodeColumnName) && !columnName.equals(nodeRowColumnID)) {
                            final Column c = configuration.getMetadata().getColumnByName(columnName);
                            @SuppressWarnings("unchecked")
                            Property<?> propery = new PropertySupport(columnName, c.getColumnClass(),
                                c.getColumnUName(), c.getColumnUName(), true, false) {
                                @Override
                                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                                    return dataRow.getData(c.getColumnName());
                                }
                                @Override
                                public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                                    //throw new UnsupportedOperationException("Not supported yet.");
                                }
                            };
                            result.add(propery);
                        }
                    }
                    return result.toArray(new Property[0]);


                }
            };
        }

        public DataRow getDataRow() {
            return dataRow;
        }

        @Override
        public Image getIcon(int type) {
            //if tge icon is turn
            if (iconColumnID == null){
                return super.getIcon(type);
            }
            return ImageUtilities.loadImage(resourceID + "/" + dataRow.getStringValue(iconColumnID) +".png"); // NOI18N
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }



        @Override
        public String getDisplayName() {
            return dataRow.getData(nodeColumnName) + "";
        }

        @Override
        public PropertySet[] getPropertySets() {
            return new PropertySet[]{propertySet};
        }
    }

    private class ExtendedTableCellRendererForNode extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (column != 0) {//we have
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }

            PropertyEditor editor = PropertyEditorManager.findEditor(configuration.getMetadata().getColumnByName(nodeColumnName).getColumnClass());
            if (editor != null && value != null && !(value + "").trim().equals("")) {//NOI18N
                editor.setValue(value);
                return super.getTableCellRendererComponent(table, editor.getAsText(), isSelected, hasFocus, row, column);
            }

            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(AdvancedTableViewVisualizer.class, key);
    }
}
