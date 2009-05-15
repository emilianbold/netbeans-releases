/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.visualizers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionsListDataProvider;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.management.api.DLightSession.SessionState;
import org.netbeans.modules.dlight.management.api.SessionStateListener;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizers.api.ColumnsUIMapping;
import org.netbeans.modules.dlight.visualizers.api.FunctionsListViewVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.FunctionsListViewVisualizerConfigurationAccessor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mt154047
 */
public class FunctionsListViewVisualizer extends JPanel implements
        Visualizer<FunctionsListViewVisualizerConfiguration>, OnTimerTask, ComponentListener, ExplorerManager.Provider {

    private Future<Boolean> task;
    private final Object queryLock = new String(FunctionsListViewVisualizer.class + " query lock"); // NOI18N
    private final Object uiLock = new String(FunctionsListViewVisualizer.class + " UI lock"); // NOI18N
    private JToolBar buttonsToolbar;
    private JButton refresh;
    private boolean isEmptyContent;
    private boolean isLoadingContent;
    private boolean isShown = true;
    private OutlineView outlineView;
    private final ExplorerManager explorerManager;
    private final FunctionDatatableDescription functionDatatableDescription;
    private final FunctionsListDataProvider dataProvider;
    private final DataTableMetadata metadata;
    private final ColumnsUIMapping columnsUIMapping;
    private final List<Column> metrics;
    private final FunctionsListViewVisualizerConfiguration configuration;
    private final TableCellRenderer outlineNodePropertyDefault;
    private final VisualizersSupport visSupport;
    private FunctionCallChildren currentChildren;

    public FunctionsListViewVisualizer(FunctionsListDataProvider dataProvider, FunctionsListViewVisualizerConfiguration configuration) {
        visSupport = new VisualizersSupport(new VisualizerImplSessionStateListener());
        explorerManager = new ExplorerManager();
        this.configuration = configuration;
        this.functionDatatableDescription = FunctionsListViewVisualizerConfigurationAccessor.getDefault().getFunctionDatatableDescription(configuration);
        this.metrics = FunctionsListViewVisualizerConfigurationAccessor.getDefault().getMetricsList(configuration);
        columnsUIMapping = FunctionsListViewVisualizerConfigurationAccessor.getDefault().getColumnsUIMapping(configuration);
        this.dataProvider = dataProvider;
        this.metadata = configuration.getMetadata();
        setLoadingContent();
        addComponentListener(this);
        String nodeLabel = columnsUIMapping == null || columnsUIMapping.getDisplayedName(functionDatatableDescription.getNameColumn()) == null ? metadata.getColumnByName(functionDatatableDescription.getNameColumn()).getColumnUName() : columnsUIMapping.getDisplayedName(functionDatatableDescription.getNameColumn());
        outlineView = new OutlineView(nodeLabel);
        outlineView.getOutline().setRootVisible(false);
        outlineView.getOutline().setDefaultRenderer(Object.class, new ExtendedTableCellRendererForNode());
        outlineNodePropertyDefault = outlineView.getOutline().getDefaultRenderer(Node.Property.class);
        outlineView.getOutline().setDefaultRenderer(Node.Property.class, new FunctionsListSheetCell.OutlineSheetCell(outlineView.getOutline(), metrics));
        List<Property> result = new ArrayList<Property>();
        for (Column c : metrics) {
            String displayedName = columnsUIMapping == null || columnsUIMapping.getDisplayedName(c.getColumnName()) == null ? c.getColumnUName() : columnsUIMapping.getDisplayedName(c.getColumnName());
            String displayedTooltip = columnsUIMapping == null || columnsUIMapping.getTooltip(c.getColumnName()) == null ? c.getColumnLongUName() : columnsUIMapping.getTooltip(c.getColumnName());
            result.add(new PropertySupport(c.getColumnName(), c.getColumnClass(),
                    displayedName, displayedTooltip, true, false) {

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return null;
                }

                @Override
                public void setValue(Object arg0) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                }
            });

        }
        outlineView.setProperties(result.toArray(new Property[0]));
        VisualizerTopComponentTopComponent.findInstance().addComponentListener(this);

    }

    public FunctionsListViewVisualizerConfiguration getVisualizerConfiguration() {
        return configuration;
    }

    public JComponent getComponent() {
        return this;
    }

    public VisualizerContainer getDefaultContainer() {
        return VisualizerTopComponentTopComponent.findInstance();
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

            task = DLightExecutorService.submit(new Callable<Boolean>() {

                public Boolean call() {
                    syncFillModel();
                    return Boolean.TRUE;
                }
            }, "FunctionsListViewVisualizer Async data load for " + // NOI18N
                    configuration.getID() + " from main table " + // NOI18N
                    configuration.getMetadata().getName());
        }
    }

    private void syncFillModel() {
        List<FunctionCall> callsList =
                dataProvider.getFunctionsList(metadata, functionDatatableDescription, metrics);

        updateList(callsList);
    }

    private void updateList(final List<FunctionCall> list) {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        UIThread.invoke(new Runnable() {

            public void run() {
                synchronized (uiLock) {
                    final boolean isEmptyConent =
                            list == null || list.isEmpty();
                    setContent(isEmptyConent);
                    if (!isEmptyConent) {
                        currentChildren = new FunctionCallChildren(list);
                        explorerManager.setRootContext(new AbstractNode(currentChildren));
                        setNonEmptyContent();
                    }
                }
            }
        });
    }

    private void setEmptyContent() {
        isEmptyContent = true;
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(Box.createVerticalStrut(20));
        JLabel label = new JLabel(visSupport != null && visSupport.isSessionAnalyzed() ? FunctionsListViewVisualizerConfigurationAccessor.getDefault().getEmptyAnalyzeMessage(configuration) : FunctionsListViewVisualizerConfigurationAccessor.getDefault().getEmptyRunningMessage(configuration)); // NOI18N
        //JLabel label = new JLabel(NbBundle.getMessage(FunctionsListViewVisualizer.class, "NoDataAvailableYet"));//NOI18N
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        this.add(label);
        repaint();
        revalidate();
    }

    private void setLoadingContent() {
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
                asyncFillModel(false);
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

    public int onTimer() {
        //throw new UnsupportedOperationException("Not supported yet.");
        syncFillModel();
        return 0;
    }

    @Override
    public void addNotify() {
        super.addNotify();
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

        removeComponentListener(this);
        VisualizerTopComponentTopComponent.findInstance().removeComponentListener(this);
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
//            asyncFillModel(true);
//        }
    }

    public void componentHidden(ComponentEvent e) {
        isShown = false;
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public class FunctionCallChildren extends Children.Keys<FunctionCall> {

        private final List<FunctionCall> list;

        public FunctionCallChildren(List<FunctionCall> list) {
            this.list = list;
        }

        protected Node[] createNodes(FunctionCall key) {
            return new Node[]{new FunctionCallNode(key)};
        }

        @Override
        protected void addNotify() {
            setKeys(list);
        }
    }

    private class FunctionCallNode extends AbstractNode {

        private final FunctionCall functionCall;
        private PropertySet propertySet;
        private final Action[] actions;
        private final Action goToSourceAction;

        FunctionCallNode(FunctionCall row) {
            super(Children.LEAF);
            functionCall = row;
            goToSourceAction = new GoToSourceAction(this);
            actions = new Action[]{goToSourceAction};
            propertySet = new PropertySet() {

                @Override
                public Property<?>[] getProperties() {
                    List<Property> result = new ArrayList<Property>();
                    //create for metrics
                    for (final Column metric : metrics) {
                        result.add(new PropertySupport(metric.getColumnName(), metric.getColumnClass(),
                                metric.getColumnUName(), metric.getColumnLongUName(), true, false) {

                            @Override
                            public Object getValue() throws IllegalAccessException, InvocationTargetException {
                                return functionCall.getMetricValue(metric.getColumnName());
                            }

                            @Override
                            public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                            }
                        });
                    }
                    return result.toArray(new Property[0]);


                }
            };
        }

        public FunctionCall getFunctionCall() {
            return functionCall;
        }

        @Override
        public Image getIcon(int type) {
            return null;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return null;
        }

        @Override
        public Action getPreferredAction() {
            return goToSourceAction;
        }

        private void fire() {
            fireDisplayNameChange(getDisplayName() + "_", getDisplayName()); // NOI18N
        }

        @Override
        public Action[] getActions(boolean context) {
            return actions;
        //return super.getActions(context);
        }

        @Override
        public String getDisplayName() {
            return functionCall.getDisplayedName();//functionCall.getFunction().getName() + (functionCall.hasOffset() ? ("+0x" + functionCall.getOffset()) : "");//NOI18N
        }

        @Override
        public PropertySet[] getPropertySets() {
            return new PropertySet[]{propertySet};
        }
    }

    private class GoToSourceAction extends AbstractAction {

        private final FunctionCallNode functionCallNode;
        private final Future<SourceFileInfo> sourceFileInfoTask;
        private boolean isEnabled = true;
        private boolean gotTheInfo = false;

        public GoToSourceAction(FunctionCallNode funcCallNode) {
            super(NbBundle.getMessage(FunctionsListViewVisualizer.class, "GoToSourceActionName"));//NOI18N
            this.functionCallNode = funcCallNode;
            sourceFileInfoTask = DLightExecutorService.submit(new Callable<SourceFileInfo>() {

                public SourceFileInfo call() {
                    FunctionCall functionCall = functionCallNode.getFunctionCall();
                    return dataProvider.getSourceFileInfo(functionCall);
                }
            }, "SourceFileInfo getting info from Functions List View"); // NOI18N
            waitForSourceFileInfo();
//            try {
//                SourceFileInfo sourceFileInfo = sourceFileInfoTask.get();
//                isEnabled = sourceFileInfo != null && sourceFileInfo.isSourceKnown();
//            } catch (InterruptedException ex) {
//                isEnabled = false;
//            } catch (ExecutionException ex) {
//                isEnabled = false;
//            } finally {
//                synchronized (GoToSourceAction.this) {
//                    gotTheInfo = true;
//                }
//                setEnabled(isEnabled);
//                functionCallNode.fire();
//
//            }
        }

        private void waitForSourceFileInfo() {
            DLightExecutorService.submit(new Runnable() {

                public void run() {
                    try {
                        SourceFileInfo sourceFileInfo = sourceFileInfoTask.get();
                        isEnabled = sourceFileInfo != null && sourceFileInfo.isSourceKnown();
                    } catch (InterruptedException ex) {
                        isEnabled = false;
                    } catch (ExecutionException ex) {
                        isEnabled = false;
                    } finally {
                        synchronized (GoToSourceAction.this) {
                            gotTheInfo = true;
                        }
                        setEnabled(isEnabled);
                        functionCallNode.fire();

                    }

                }
            }, "Wait For the SourceFileInfo");//NOI18N
        }

        @Override
        public boolean isEnabled() {
            return isEnabled;
        }

        public void actionPerformed(ActionEvent e) {

            DLightExecutorService.submit(new Runnable() {

                public void run() {
                    SourceFileInfo sourceFileInfo = null;
                    try {
                        sourceFileInfo = sourceFileInfoTask.get();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    if (sourceFileInfo == null) {// TODO: what should I do here if there is no source file info
                        return;
                    }

                    SourceSupportProvider sourceSupportProvider = Lookup.getDefault().lookup(SourceSupportProvider.class);
                    sourceSupportProvider.showSource(sourceFileInfo);
                }
            }, "GoToSource from Functions List View"); // NOI18N

        }
    }

    private class ExtendedTableCellRendererForNode extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (column != 0) {//we have
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            //get function call
            PropertyEditor editor = PropertyEditorManager.findEditor(metadata.getColumnByName(functionDatatableDescription.getNameColumn()).getColumnClass());
            FunctionCallNode node = null;
            synchronized (FunctionsListViewVisualizer.this.uiLock) {
                if (currentChildren != null){
                    Node[] nodes  =  currentChildren.getNodes();
                    if (row >= 0 && row < nodes.length) {
                        node = (FunctionCallNode) nodes[row];
                    }
                }
            }

            //get node object
            if (editor != null && value != null && !(value + "").trim().equals("")) {//NOI18N
                editor.setValue(value);
                DefaultTableCellRenderer c = (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, editor.getAsText(), isSelected, hasFocus, row, column);
                c.setEnabled(node != null && node.getPreferredAction().isEnabled());
                return c;
            }

            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    private class VisualizerImplSessionStateListener implements SessionStateListener {

        public void sessionStateChanged(DLightSession session, SessionState oldState, SessionState newState) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
