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

import java.awt.event.ComponentEvent;
import javax.swing.JComponent;
import org.netbeans.modules.dlight.visualizers.api.TableVisualizerConfiguration;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.spi.impl.TableDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizers.api.impl.TableVisualizerConfigurationAccessor;
import org.netbeans.swing.etable.ETableColumnModel;
import org.openide.util.NbBundle;

/**
 *
 * @author ak119685
 */
final class TableVisualizer extends JPanel implements
        Visualizer<TableVisualizerConfiguration>, OnTimerTask, ComponentListener {

    private static final long MIN_REFRESH_MILLIS = 500;

    private final TableDataProvider provider;
    private final List<DataRow> data = new ArrayList<DataRow>();
    private final TableVisualizerConfiguration configuration;
    private final OnTimerRefreshVisualizerHandler timerHandler;
    private volatile boolean isShown = true;
    private JButton refresh;
    private JTable table;
    private AbstractTableModel tableModel;
    private TableSorter tableSorterModel = new TableSorter();
    private boolean isEmptyContent;
    private boolean isLoadingContent;
    private static final boolean isMacLaf = "Aqua".equals(UIManager.getLookAndFeel().getID()); // NOI18N
    private static final Color macBackground = UIManager.getColor("NbExplorerView.background"); // NOI18N
    private final Object queryLock = new Object();
    private final Object uiLock = new Object();
    private Future<Boolean> task;

    TableVisualizer(TableDataProvider provider, final TableVisualizerConfiguration configuration) {
        super(new BorderLayout());
        //timerHandler = new OnTimerRefreshVisualizerHandler(this, 1, TimeUnit.SECONDS);
        timerHandler = null;
        this.provider = provider;
        this.configuration = configuration;
        setEmptyContent();
        addComponentListener(this);
        VisualizerTopComponentTopComponent.findInstance().addComponentListener(this);
    }

    @Override
    public void addNotify() {
        super.addNotify();
//        addComponentListener(this);
//        VisualizerTopComponentTopComponent.findInstance().addComponentListener(this);
//        asyncFillModel();
//
//        if (timerHandler != null && timerHandler.isSessionRunning()) {
//            timerHandler.startTimer();
//            return;
//        }

// AK: Do we really need this for the second time?
//        if (timerHandler.isSessionAnalyzed() ||
//            timerHandler.isSessionPaused()) {
//            onTimer();
//        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (timerHandler != null) {
            timerHandler.stopTimer();
        }
        removeComponentListener(this);
        VisualizerTopComponentTopComponent.findInstance().removeComponentListener(this);

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



    private void setEmptyContent() {
        isEmptyContent = true;
        this.removeAll();
        table = null;
        if (tableSorterModel != null) {
//            tableSorterModel.removeTableModelListener(this);
            tableSorterModel = null;
        }
        JLabel label = new JLabel(timerHandler != null && timerHandler.isSessionAnalyzed() ?
            TableVisualizerConfigurationAccessor.getDefault().getEmptyAnalyzeMessage(configuration) :
            TableVisualizerConfigurationAccessor.getDefault().getEmptyRunningMessage(configuration), JLabel.CENTER);
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
                    data.clear();
                    data.addAll(list);
                    if (!isEmptyConent) {
                        if (tableModel != null) {
                            tableModel.fireTableDataChanged();
                        }
                        setNonEmptyContent();
                    }
                }
            }
        });

    }


    private void setNonEmptyContent() {
        isEmptyContent = false;
        this.removeAll();
        setLayout(new BorderLayout());
        tableModel = new AbstractTableModel() {

            public int getRowCount() {
                return data.size();
            }

            public int getColumnCount() {
                return configuration.getMetadata().getColumnsCount();
            }

            @Override
            public Class getColumnClass(int columnIndex) {
                return configuration.getMetadata().getColumns().get(columnIndex).getColumnClass();
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                Object x = data.get(rowIndex).getData().get(columnIndex);
                return x;
            }

            @Override
            public String getColumnName(int column) {
                return configuration.getMetadata().getColumns().get(column).getColumnUName();
            }
        };
        tableSorterModel = new TableSorter(tableModel);
        table = new JTable(tableSorterModel);
        tableSorterModel.addMouseListenerToHeaderInTable(table);
        int columnCount = configuration.getMetadata().getColumnsCount();
        int firstKey = KeyEvent.VK_1;
        for (int i = 1; i <= columnCount; i++) {
            final int columnNumber = i - 1;
            KeyStroke columnKey = KeyStroke.getKeyStroke(firstKey++, KeyEvent.ALT_MASK, true);
            table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(columnKey, "ascSortFor" + i);//NOI18N
            table.getActionMap().put("ascSortFor" + i, new AbstractAction() {// NOI18N

                public void actionPerformed(ActionEvent e) {
                    // ok, do the sorting
                    int column = columnNumber;
                    tableSorterModel.clickOnColumn(column);
                    table.getTableHeader().repaint();
                }
            });
        }
        //mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        //table = new JTable(tableModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);



        add(createToolbar(), BorderLayout.WEST);
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
        return 0;
    }

    public void refresh() {
        asyncFillModel(false);
    }



    protected final void asyncFillModel(boolean cancelIfNotDone) {
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
            }, "Async TableVisualizer Async data load for " + configuration.getID()); // NOI18N
        }


    }

    private void syncFillModel(boolean wait) {
        long startTime = System.currentTimeMillis();
        Future<List<DataRow>> queryDataTask = DLightExecutorService.submit(new Callable<List<DataRow>>() {

            public List<DataRow> call() throws Exception {
                return provider.queryData(configuration.getMetadata());
            }
        }, "TableVisualizer Async data from provider  load for " + configuration.getID()); // NOI18N
        try {
            final List<DataRow> list = queryDataTask.get();
            data.clear();
            data.addAll(list);
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

//    private void syncFillModel() {
//        List<DataRow> dataRow = provider.queryData(configuration.getMetadata());
//        boolean isEmpty;
//        synchronized (data) {
//            data.clear();
//            data.addAll(dataRow);
//            isEmpty = data.isEmpty();
//        //in case there is no data create fake model
//        }
//        final boolean isEmptyConent = isEmpty;
//        UIThread.invoke(new Runnable() {
//
//            public void run() {
//                if (tableModel != null) {
//                    tableModel.fireTableDataChanged();
//                }
//                setContent(isEmptyConent);
//            }
//        });
//    }

    public TableVisualizerConfiguration getVisualizerConfiguration() {
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
    }

    public void componentHidden(ComponentEvent e) {
        isShown = false;
    }

    public void updateVisualizerConfiguration(TableVisualizerConfiguration configuration) {
    }
    private static String getMessage(String key) {
        return NbBundle.getMessage(TableVisualizer.class, key);
    }
}
