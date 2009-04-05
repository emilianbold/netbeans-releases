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

import java.awt.event.ComponentEvent;
import javax.swing.JComponent;
import org.netbeans.modules.dlight.visualizers.api.TableVisualizerConfiguration;
import java.awt.BorderLayout;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.spi.impl.TableDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizers.api.impl.TableVisualizerConfigurationAccessor;

/**
 *
 * @author ak119685
 */
class TableVisualizer extends JPanel implements
        Visualizer<TableVisualizerConfiguration>, OnTimerTask, ComponentListener {

    private TableDataProvider provider;
    private volatile boolean isShown = true;
    private TableVisualizerConfiguration configuration;
    private final List<DataRow> data = new ArrayList<DataRow>();
    private JToolBar buttonsToolbar;
    private JButton refresh;
    private AbstractTableModel tableModel;
    private JTable table;
    private TableSorter tableSorterModel = new TableSorter();
    private OnTimerRefreshVisualizerHandler timerHandler;
    private boolean isEmptyContent;

    TableVisualizer(TableDataProvider provider, final TableVisualizerConfiguration configuration) {
        //timerHandler = new OnTimerRefreshVisualizerHandler(this, 1, TimeUnit.SECONDS);
        this.provider = provider;
        this.configuration = configuration;
        setEmptyContent();
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

    private void setEmptyContent() {
        isEmptyContent = true;
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        table = null;
        if (tableSorterModel != null) {
//            tableSorterModel.removeTableModelListener(this);
            tableSorterModel = null;
        }
        JLabel label = new JLabel(timerHandler != null && timerHandler.isSessionAnalyzed() ? TableVisualizerConfigurationAccessor.getDefault().getEmptyAnalyzeMessage(configuration) : TableVisualizerConfigurationAccessor.getDefault().getEmptyRunningMessage(configuration)); // NOI18N
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

    private void setNonEmptyContent() {
        isEmptyContent = false;
        this.removeAll();
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
        //mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        //table = new JTable(tableModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);
        buttonsToolbar = new JToolBar();
        refresh = new JButton();

        buttonsToolbar.setFloatable(false);
        buttonsToolbar.setOrientation(1);
        buttonsToolbar.setRollover(true);

        // Refresh button...
        refresh.setIcon(ImageLoader.loadIcon("refresh.png")); // NOI18N
//    refresh.setToolTipText(org.openide.util.NbBundle.getMessage(PerformanceMonitorViewTopComponent.class, "RefreshActionTooltip")); // NOI18N
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
        syncFillModel();
        return 0;
    }

    public void refresh() {
        asyncFillModel();
    }



    protected final void asyncFillModel() {
        DLightExecutorService.submit(new Runnable() {

            public void run() {
                syncFillModel();
            }
        }, "Async TableVisualizer model fill " + configuration.getID()); // NOI18N

    }

    private void syncFillModel() {
        List<DataRow> dataRow = provider.queryData(configuration.getMetadata());
        boolean isEmpty;
        synchronized (data) {
            data.clear();
            data.addAll(dataRow);
            isEmpty = data.isEmpty();
        //in case there is no data create fake model
        }
        final boolean isEmptyConent = isEmpty;
        UIThread.invoke(new Runnable() {

            public void run() {
                if (tableModel != null) {
                    tableModel.fireTableDataChanged();
                }
                setContent(isEmptyConent);
            }
        });
    }

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
        if (isShown) {
            asyncFillModel();
        }

    }

    public void componentHidden(ComponentEvent e) {
        isShown = false;
    }
}
