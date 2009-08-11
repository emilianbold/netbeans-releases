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
import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
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
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 *
 * @author mt154047
 */
final class AdvancedTableViewVisualizer extends JPanel implements
    Visualizer<AdvancedTableViewVisualizerConfiguration>, OnTimerTask, ComponentListener, ExplorerManager.Provider {

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
    private OutlineView outlineView;
    private final String nodeColumnName;
    private final String nodeRowColumnID;
    private final ExplorerManager explorerManager;
    private Future task;
    private final Object queryLock = new Object();
    private final Object uiLock = new Object();

    AdvancedTableViewVisualizer(TableDataProvider provider, final AdvancedTableViewVisualizerConfiguration configuration) {
        // timerHandler = new OnTimerRefreshVisualizerHandler(this, 1, TimeUnit.SECONDS);
        this.provider = provider;
        this.configuration = configuration;
        this.explorerManager = new ExplorerManager();
        setLoadingContent();
        addComponentListener(this);
        AdvancedTableViewVisualizerConfigurationAccessor accessor = AdvancedTableViewVisualizerConfigurationAccessor.getDefault();
//        tableView = new TableView();
        nodeColumnName = accessor.getNodeColumnName(configuration);
        nodeRowColumnID = accessor.getRowNodeColumnName(configuration);
        outlineView = new OutlineView(configuration.getMetadata().getColumnByName(nodeColumnName).getColumnUName());
        outlineView.getOutline().setRootVisible(false);
        outlineView.getOutline().setDefaultRenderer(Object.class, new ExtendedTableCellRendererForNode());
        List<String> hiddenColumns = accessor.getHiddenColumnNames(configuration);
        List<Property> result = new ArrayList<Property>();
        for (String columnName : configuration.getMetadata().getColumnNames()) {
            if (!nodeColumnName.equals(columnName) && !nodeRowColumnID.equals(columnName) && !hiddenColumns.contains(columnName)) {
                final Column c = configuration.getMetadata().getColumnByName(columnName);
                result.add(new PropertySupport(c.getColumnName(), c.getColumnClass(),
                    c.getColumnUName(), c.getColumnUName(), true, false) {

                    @Override
                    public Object getValue() throws IllegalAccessException, InvocationTargetException {
                        return null;
                    }

                    @Override
                    public void setValue(Object arg0) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    }
                });
            }
        }
        outlineView.setProperties(result.toArray(new Property[0]));
        VisualizerTopComponentTopComponent.findInstance().addComponentListener(this);

    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        addComponentListener(this);
        VisualizerTopComponentTopComponent.findInstance().addComponentListener(this);
        asyncFillModel();
        if (timerHandler != null && timerHandler.isSessionRunning()) {
            timerHandler.startTimer();
            return;
        }


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
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(timerHandler != null && timerHandler.isSessionAnalyzed() ? AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getEmptyAnalyzeMessage(configuration) : AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getEmptyRunningMessage(configuration)); // NOI18N
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        this.add(label);
        repaint();
        revalidate();
    }

    private void setLoadingContent() {
        isEmptyContent = false;
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(NbBundle.getMessage(AdvancedTableViewVisualizer.class, "Loading")); // NOI18N
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
        synchronized (uiLock) {
            setNonEmptyContent();
            this.explorerManager.setRootContext(new AbstractNode(new DataChildren(list)));
        }
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
                asyncFillModel();
            }
        });

        buttonsToolbar.add(refresh);



        add(buttonsToolbar, BorderLayout.LINE_START);
//        JComponent treeTableView =
//            Models.createView(compoundModel);
//        add(treeTableView, BorderLayout.CENTER);
//        treeModelImpl.fireTreeModelChanged();
//        tableView = new TableView();
        add(outlineView, BorderLayout.CENTER);


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
        asyncFillModel();
        return 0;
    }

    public void refresh() {
        asyncFillModel();
    }


    private void asyncFillModel() {
        synchronized (queryLock) {
            if (task != null) {
                task.cancel(true);
            }
            task = DLightExecutorService.submit(new Callable<Boolean>() {

                public Boolean call() {
                    Future<List<DataRow>> queryDataTask = DLightExecutorService.submit(new Callable<List<DataRow>>() {

                        public List<DataRow> call() throws Exception {
                            return provider.queryData(configuration.getMetadata());
                        }
                    }, "AdvancedTableViewVisualizer Async data from provider  load for " + configuration.getID()); // NOI18N
                    try {
                        final List<DataRow> list = queryDataTask.get();
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
                        return Boolean.valueOf(true);
                    } catch (ExecutionException ex) {
                        Thread.currentThread().interrupt();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return Boolean.valueOf(false);
                }
            }, "AdvancedTableViewVisualizer Async data load for " + configuration.getID()); // NOI18N
        }


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
            //we should change explorerManager
            onTimer();
        }
    }

    public void componentHidden(ComponentEvent e) {
        isShown = false;
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
                    List<Property> result = new ArrayList<Property>();
                    for (String columnName : dataRow.getColumnNames()) {
                        if (!columnName.equals(nodeColumnName) && !columnName.equals(nodeRowColumnID)) {
                            final Column c = configuration.getMetadata().getColumnByName(columnName);
                            result.add(new PropertySupport(columnName, c.getColumnClass(),
                                c.getColumnUName(), c.getColumnUName(), true, false) {

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
                    }
                    return result.toArray(new Property[0]);


                }
            };
        }

        public DataRow getDataRow() {
            return dataRow;
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

}
