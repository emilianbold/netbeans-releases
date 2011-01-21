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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.visualizers;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
import org.netbeans.modules.dlight.api.visualizer.TableBasedVisualizerConfiguration;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.util.ui.TextFilterPanel;
import org.netbeans.modules.dlight.visualizers.api.VisualizerToolbarComponentsProvider;
import org.netbeans.modules.dlight.visualizers.ui.TableViewNodeChildren;
import org.netbeans.modules.dlight.visualizers.util.TableViewDataFilter;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author ak119685
 */
public abstract class TableViewVisualizer<Config extends TableBasedVisualizerConfiguration, Data> extends JPanel implements
        Visualizer<Config>, ExplorerManager.Provider, Lookup.Provider, VisualizerToolbarComponentsProvider {

    private static final RequestProcessor RP = new RequestProcessor("TableViewVisualizer Data Refresh", 10); // NOI18N
    private static final boolean isMacLaf = "Aqua".equals(UIManager.getLookAndFeel().getID()); // NOI18N
    private static final Color macBackground = UIManager.getColor("NbExplorerView.background"); // NOI18N
    private final ExplorerManager manager = new ExplorerManager();
    private final JPanel busyPanel;
    private final JPanel dataPanel;
    private final JPanel contentPanel;
    private final Config configuration;
    private final InstanceContent content = new InstanceContent();
    private final Lookup lookup = new AbstractLookup(content);
    private final Task busyPanelDisplayTask;
    private final JButton refreshBtn;
    private final Task dataFetchTask;
    private final TextFilterPanel textFilterPanel = new TextFilterPanel();
    private final ChangeListener filterListener;
    private final AtomicReference<List<Data>> dataRef = new AtomicReference<List<Data>>();
    private final ComponentListener componentListener;
    private final AtomicBoolean isVisible = new AtomicBoolean();
    private TableViewNodeChildren<Data> children;
    private JPanel emptyPanel;

    protected TableViewVisualizer(final DataProvider provider, final Config configuration) {
        this.configuration = configuration;

        content.add(provider);
        
        setLayout(new BorderLayout());

        busyPanel = new TableViewBusyPanel();
        dataPanel = new JPanel(new BorderLayout());

        contentPanel = new JPanel(new CardLayout());
        contentPanel.add(busyPanel, "busy"); // NOI18N
        contentPanel.add(dataPanel, "data"); // NOI18N
        add(contentPanel, BorderLayout.CENTER);

        dataFetchTask = RP.create(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            dataRef.set(getUpdatedData());
                            updateView();
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    refreshBtn.setEnabled(true);
                                }
                            });
                        }
                    }
                });

        busyPanelDisplayTask = RP.create(new Runnable() {

            @Override
            public void run() {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            if (!dataFetchTask.isFinished()) {
                                ((CardLayout) contentPanel.getLayout()).show(contentPanel, "busy"); // NOI18N
                            }
                        }
                    });
                } catch (InterruptedException ex) {
                    Thread.interrupted();
                } catch (Exception ex) {
                }
            }
        });

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
        refreshBtn.setToolTipText(NbBundle.getMessage(TableViewVisualizer.class, "Refresh.Tooltip")); // NOI18N

        toolbar.add(refreshBtn);
        add(toolbar, BorderLayout.WEST);

        // Add reaction on filter

        filterListener = new ChangeListener() {

            private TableViewDataFilter<Data> currentFilter;

            @Override
            public synchronized void stateChanged(ChangeEvent e) {
                final String filter = ((TextFilterPanel) e.getSource()).getText();

                if (currentFilter != null) {
                    content.remove(currentFilter);
                }

                currentFilter = new TableViewDataFilter<Data>() {

                    @Override
                    public boolean matches(Data data) {
                        return matchesFilter(filter, data);
                    }
                };

                content.add(currentFilter);
                updateView();
            }
        };

        textFilterPanel.addChangeListener(filterListener);

        componentListener = new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                if (isVisible.compareAndSet(false, true)) {
                    registerListeners();
                    refresh();
                }
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                if (isVisible.compareAndSet(true, false)) {
                    unregisterListeners();
                }
            }
        };
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    private void registerListeners() {
        KeyStroke refreshKey = KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, true);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(refreshKey, "refresh"); // NOI18N
        getActionMap().put("refresh", refreshBtn.getAction()); // NOI18N
    }

    private void unregisterListeners() {
        KeyStroke refreshKey = KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, true);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(refreshKey);
        getActionMap().remove("refresh"); // NOI18N
        resetKeyboardActions();
    }

    public void init() {
        emptyPanel = new TableViewEmptyPanel(getEmptyRunningMessage(), getEmptyAnalyzeMessage());
        contentPanel.add(emptyPanel, "empty"); // NOI18N
        dataPanel.add(initTableView(), BorderLayout.CENTER);

        // Set root node
        children = initChildren();
        Node rootNode = new AbstractNode(children, lookup);
        manager.setRootContext(rootNode);
    }

    protected DataTableMetadata getMetadata() {
        return getVisualizerConfiguration().getMetadata();
    }

    private void updateView() {
        Node[] selectedNodes = getExplorerManager().getSelectedNodes();
        final String selectedNodeName;

        if (selectedNodes.length == 1) {
            Node selected = selectedNodes[0];
            selectedNodeName = selected.getDisplayName();
        } else {
            selectedNodeName = null;
        }

        final List<Data> newData = dataRef.get();

        if (newData.isEmpty()) {
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "empty"); // NOI18N
        } else {
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "data"); // NOI18N
        }

        children.setData(newData);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (selectedNodeName != null) {
                    for (Node node : getExplorerManager().getRootContext().getChildren().getNodes(true)) {
                        if (selectedNodeName.equals(node.getDisplayName())) {
                            try {
                                getExplorerManager().setSelectedNodes(new Node[]{node});
                            } catch (PropertyVetoException ex) {
                            }
                            break;
                        }
                    }
                }
            }
        });
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
        if (isVisible.get() && refreshBtn.isEnabled()) {
            refreshBtn.setEnabled(false);
            busyPanelDisplayTask.schedule(700);
            dataFetchTask.schedule(0);
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
        addComponentListener(componentListener);
        VisualizerContainer container = getDefaultContainer();
        if (container instanceof Component) {
            ((Component) container).addComponentListener(componentListener);
        }
        componentListener.componentShown(null);
        
        super.addNotify();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        
        componentListener.componentHidden(null);
        removeComponentListener(componentListener);
        VisualizerContainer container = getDefaultContainer();
        if (container instanceof Component) {
            ((Component) container).removeComponentListener(componentListener);
        }
    }

    @Override
    public Config getVisualizerConfiguration() {
        return configuration;
    }

    @Override
    public void updateVisualizerConfiguration(Config configuration) {
        // NOP
    }

    protected String getEmptyRunningMessage() {
        return NbBundle.getMessage(TableViewVisualizer.class, "TableViewVisualizer.defaultEmptyRunningMessage.text"); // NOI18N
    }

    protected String getEmptyAnalyzeMessage() {
        return NbBundle.getMessage(TableViewVisualizer.class, "TableViewVisualizer.defaultEmptyAnalyzeMessage.text"); // NOI18N
    }

    protected abstract TableViewNodeChildren<Data> initChildren();

    protected abstract Component initTableView();

    protected abstract List<Data> getUpdatedData();

    protected abstract boolean matchesFilter(String filter, Data data);
}
