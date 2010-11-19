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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.visualizers;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionsListDataProvider;
import org.netbeans.modules.dlight.core.stack.spi.AnnotatedSourceSupport;
import org.netbeans.modules.dlight.spi.SourceSupportProvider;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.visualizers.api.ColumnsUIMapping;
import org.netbeans.modules.dlight.visualizers.api.FunctionsListViewVisualizerConfiguration;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.util.ui.TextFilterPanel;
import org.netbeans.modules.dlight.visualizers.api.VisualizerToolbarComponentsProvider;
import org.netbeans.modules.dlight.visualizers.api.impl.FunctionsListViewVisualizerConfigurationAccessor;
import org.netbeans.modules.dlight.visualizers.ui.FunctionCallNodeChildren;
import org.netbeans.modules.dlight.visualizers.ui.FunctionsListViewTable;
import org.netbeans.modules.dlight.visualizers.util.FunctionCallFilter;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author mt154047
 */
public class FunctionsListViewVisualizer extends JPanel implements
        Visualizer<FunctionsListViewVisualizerConfiguration>, ExplorerManager.Provider, VisualizerToolbarComponentsProvider {

    private static final boolean isMacLaf = "Aqua".equals(UIManager.getLookAndFeel().getID()); // NOI18N
    private static final Color macBackground = UIManager.getColor("NbExplorerView.background"); // NOI18N
    private final ExplorerManager manager = new ExplorerManager();
    private final JPanel emptyPanel;
    private final JPanel dataPanel;
    private final JPanel contentPanel;
    private final InstanceContent content = new InstanceContent();
    private final Lookup lookup = new AbstractLookup(content);
    private final Task dataFetchTask;
    private final DataTableMetadata metadata;
    private final FunctionDatatableDescription functionDatatableDescription;
    private final FunctionsListViewVisualizerConfiguration configuration;
    private final FunctionCallNodeChildren functionChildren;
    private final List<Column> metrics;
    private final TextFilterPanel textFilterPanel = new TextFilterPanel();
    private final ChangeListener filterListener;
    private final JButton refreshBtn;
    private final AtomicReference<List<FunctionCallWithMetric>> dataRef = new AtomicReference<List<FunctionCallWithMetric>>();

    public FunctionsListViewVisualizer(final FunctionsListDataProvider dataProvider, final FunctionsListViewVisualizerConfiguration cfg) {
        setLayout(new BorderLayout());

        emptyPanel = new FunctionsListViewEmptyPanel(cfg);
        dataPanel = new JPanel(new BorderLayout());

        contentPanel = new JPanel(new CardLayout());
        contentPanel.add(emptyPanel, "empty"); // NOI18N
        contentPanel.add(dataPanel, "data"); // NOI18N
        add(contentPanel, BorderLayout.CENTER);

        this.configuration = cfg;
        this.metadata = cfg.getMetadata();

        FunctionsListViewVisualizerConfigurationAccessor cfgAccess =
                FunctionsListViewVisualizerConfigurationAccessor.getDefault();

        this.metrics = cfgAccess.getMetricsList(cfg);
        this.functionDatatableDescription = cfgAccess.getFunctionDatatableDescription(cfg);

        final GotoSourceActionProvider gotoSourceActionsProvider = new GotoSourceActionProvider(Lookup.getDefault().lookup(SourceSupportProvider.class), dataProvider);
        functionChildren = new FunctionCallNodeChildren(gotoSourceActionsProvider, metrics);

        dataFetchTask = RequestProcessor.getDefault().create(new DataFetchRunnable(dataProvider));

        // Create toolbar with refresh button

        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);

        if (isMacLaf) {
            toolbar.setBackground(macBackground);
        }

        toolbar.setRollover(true);
        toolbar.setFloatable(false);

        refreshBtn = new JButton(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });

        refreshBtn.setIcon(ImageLoader.loadIcon("refresh.png")); // NOI18N
        refreshBtn.setToolTipText(NbBundle.getMessage(FunctionsListViewVisualizer.class, "Refresh.Tooltip")); // NOI18N

        toolbar.add(refreshBtn);
        add(toolbar, BorderLayout.WEST);

        // Create table view
        final ColumnsUIMapping columnsUIMapping = cfgAccess.getColumnsUIMapping(cfg);

        String nodeLabel = columnsUIMapping == null
                || columnsUIMapping.getDisplayedName(functionDatatableDescription.getNameColumn()) == null
                ? metadata.getColumnByName(functionDatatableDescription.getNameColumn()).getColumnUName()
                : columnsUIMapping.getDisplayedName(functionDatatableDescription.getNameColumn());

        FunctionsListViewTable table = new FunctionsListViewTable(manager, nodeLabel, columnsUIMapping, metrics);
        dataPanel.add(table, BorderLayout.CENTER);

        // Add reaction on filter

        filterListener = new ChangeListener() {

            private FunctionCallFilter currentFilter;

            @Override
            public synchronized void stateChanged(ChangeEvent e) {
                final String filter = ((TextFilterPanel) e.getSource()).getText();

                if (currentFilter != null) {
                    content.remove(currentFilter);
                }

                currentFilter = new FunctionCallFilter() {

                    @Override
                    public boolean matches(FunctionCall function) {
                        return filter == null || filter.isEmpty() || function.getFunction().getName().contains(filter);
                    }
                };

                content.add(currentFilter);
                updateView();
            }
        };

        textFilterPanel.addChangeListener(filterListener);

        KeyStroke refreshKey = KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, true);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(refreshKey, "refresh"); // NOI18N
        getActionMap().put("refresh", new AbstractAction() { // NOI18N

            @Override
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });

        // Set root node
        Node rootNode = new AbstractNode(functionChildren, lookup);
        manager.setRootContext(rootNode);
    }

    @Override
    public FunctionsListViewVisualizerConfiguration getVisualizerConfiguration() {
        return configuration;
    }

    @Override
    public void updateVisualizerConfiguration(FunctionsListViewVisualizerConfiguration configuration) {
        // NOP
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public VisualizerContainer getDefaultContainer() {
        return VisualizerTopComponentTopComponent.findInstance();
    }

    @Override
    public synchronized void refresh() {
        if (refreshBtn.isEnabled()) {
            refreshBtn.setEnabled(false);
            dataFetchTask.schedule(0);
        }
    }

    private void updateView() {
        Node[] selectedNodes = getExplorerManager().getSelectedNodes();
        final List<FunctionCallWithMetric> newData = dataRef.get();

        if (newData.isEmpty()) {
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "empty"); // NOI18N
        } else {
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "data"); // NOI18N
        }

        functionChildren.setData(newData);

        try {
            getExplorerManager().setSelectedNodes(selectedNodes);
        } catch (PropertyVetoException ex) {
        }
    }

    @Override
    public final ExplorerManager getExplorerManager() {
        return manager;
    }

    @Override
    public List<Component> getToolbarComponents() {
        return Arrays.<Component>asList(textFilterPanel);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        refresh();
    }

    private class DataFetchRunnable implements Runnable {

        private final FunctionsListDataProvider dataProvider;

        public DataFetchRunnable(FunctionsListDataProvider dataProvider) {
            this.dataProvider = dataProvider;
        }

        @Override
        public void run() {
            try {
                List<FunctionCallWithMetric> newData = dataProvider.getFunctionsList(metadata, functionDatatableDescription, metrics);
                List<FunctionCallWithMetric> details = dataProvider.getDetailedFunctionsList(metadata, functionDatatableDescription, metrics);

                Collection<? extends AnnotatedSourceSupport> supports = Lookup.getDefault().lookupAll(AnnotatedSourceSupport.class);

                if (supports != null) {
                    for (AnnotatedSourceSupport sourceSupport : supports) {
                        sourceSupport.updateSource(dataProvider, metrics, newData, details);
                    }
                }

                // And refresh the explorer manager...
                dataRef.set(newData);

                updateView();
            } finally {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        refreshBtn.setEnabled(true);
                    }
                });
            }
        }
    }
}
