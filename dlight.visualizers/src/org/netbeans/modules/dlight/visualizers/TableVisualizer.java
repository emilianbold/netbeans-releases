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

import javax.swing.JComponent;
import org.netbeans.modules.dlight.visualizers.api.TableVisualizerConfiguration;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.dlight.visualizers.api.impl.VisualizerConfigurationIDsProvider;
import org.netbeans.modules.dlight.dataprovider.spi.DataProvider;
import org.netbeans.modules.dlight.dataprovider.spi.support.TableDataProvider;
import org.netbeans.modules.dlight.visualizer.spi.Visualizer;
import org.netbeans.modules.dlight.storage.api.DataRow;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizer.spi.VisualizerContainer;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ak119685
 */
class TableVisualizer extends JPanel implements Visualizer<TableVisualizerConfiguration>, OnTimerTask {

    private TableDataProvider provider;
    private TableVisualizerConfiguration configuration;
    private final List<DataRow> data = new ArrayList<DataRow>();
    private JToolBar buttonsToolbar;
    private JButton refresh;
    private AbstractTableModel tableModel;
    private JTable table;
    private TableSorter tableSorterModel = new TableSorter();
    private OnTimerRefreshVisualizerHandler timerHandler;

    TableVisualizer(DataProvider provider, final TableVisualizerConfiguration configuration) {
        timerHandler = new OnTimerRefreshVisualizerHandler(this, 5);
        this.provider = (TableDataProvider) provider;
        this.configuration = configuration;

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
                load();
            }
        });

        buttonsToolbar.add(refresh);



        add(buttonsToolbar, BorderLayout.LINE_START);
        validate();

    }

    @Override
    public void addNotify() {
        super.addNotify();

        if (timerHandler.isSessionRunning()) {
            timerHandler.startTimer();
            return;
        }

        if (timerHandler.isSessionAnalyzed() ||
                timerHandler.isSessionPaused()) {
            onTimer();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        timerHandler.stopTimer();
    }

    public VisualizerContainer getDefaultContainer() {
        return VisualizerTopComponentTopComponent.findInstance();
    }

    public int onTimer() {
        load();
        return 0;
    }

    private void load() {


        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                List<DataRow> dataRow = provider.queryData(configuration.getMetadata());
                synchronized (data) {
                    data.clear();
                    data.addAll(dataRow);
                }
                UIThread.invoke(new Runnable() {

                    public void run() {
                        tableModel.fireTableDataChanged();
                    }
                });
            }
        });
    }

    public TableVisualizerConfiguration getVisualizerConfiguration() {
        return configuration;
    }

    public JComponent getComponent() {
        return this;
    }
}
