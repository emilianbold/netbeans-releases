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
package org.netbeans.modules.dlight.indicators.impl;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.api.datafilter.DataFilterListener;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.DataUtil;
import org.netbeans.modules.dlight.indicators.DataRowToTimeSeries;
import org.netbeans.modules.dlight.indicators.TimeSeriesIndicatorConfiguration;
import org.netbeans.modules.dlight.indicators.graph.GraphPanel;
import org.netbeans.modules.dlight.indicators.graph.Legend;
import org.netbeans.modules.dlight.indicators.graph.RepairPanel;
import org.netbeans.modules.dlight.indicators.graph.TimeSeriesIndicatorConfigurationAccessor;
import org.netbeans.modules.dlight.indicators.graph.TimeSeriesPlot;
import org.netbeans.modules.dlight.extras.api.ViewportAware;
import org.netbeans.modules.dlight.extras.api.ViewportModel;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.indicators.DetailDescriptor;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import org.netbeans.modules.dlight.indicators.graph.TimeSeriesDataContainer;
import org.netbeans.modules.dlight.spi.indicator.IndicatorActionsProvider;
import org.netbeans.modules.dlight.spi.indicator.PersistentIndicator;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.util.UIUtilities;
import org.netbeans.modules.dlight.util.ui.DLightUIPrefs;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Indicator capable of drawing one or more time series.
 *
 * @author Alexey Vladykin
 */
public final class TimeSeriesIndicator
        extends PersistentIndicator<TimeSeriesIndicatorConfiguration>
        implements ViewportAware, DataFilterListener {

    private final static Logger log = DLightLogger.getLogger(TimeSeriesIndicator.class);
    private final DataRowToTimeSeries dataRowHandler;
    private final TimeSeriesDataContainer data;
    private GraphPanel<TimeSeriesPlot, Legend> panel;
    private TimeSeriesPlot graph;
    private Legend legend;
    private JButton button;
    private final int timeSeriesCount;
    private int tickCounter;
    private List<Action> popupActions;
    private volatile boolean isInitialized = false;
    private final TimeSeriesIndicatorConfiguration configuration;
    private final List<TimeSeriesDescriptor> timeSeriesList;
    private final List<DetailDescriptor> detailsList;
    private final DataTableMetadata timeSeriesTable;
    private final DataTableMetadata detailsTable;
    private volatile Map<String, String> detailsValues;
    private final UILock uiLock = new UILock();

    public TimeSeriesIndicator(TimeSeriesIndicatorConfiguration configuration) {
        super(configuration);
        this.configuration = configuration;
        TimeSeriesIndicatorConfigurationAccessor accessor = TimeSeriesIndicatorConfigurationAccessor.getDefault();
        this.dataRowHandler = accessor.getDataRowHandler(configuration);
        this.timeSeriesList = accessor.getTimeSeriesDescriptors(configuration);
        this.timeSeriesCount = timeSeriesList.size();
        this.data = new TimeSeriesDataContainer(accessor.getGranularity(configuration), accessor.getAggregation(configuration), timeSeriesCount, accessor.getLastNonNull(configuration));
        this.data.put(0, new float[timeSeriesCount]);
        this.timeSeriesTable = createTimeSeriesTableMetadata(accessor.getPersistencePrefix(configuration), timeSeriesList);
        this.detailsList = accessor.getDetailDescriptors(configuration);
        this.detailsTable = createDetailsTableMetadata(accessor.getPersistencePrefix(configuration), detailsList);
        this.detailsValues = Collections.emptyMap();
    }

    @Override
    protected void targetStarted() {
        legend.updateWithInfoProvided(getColumnsProvided());
    }



    private final void initUI() {
        synchronized (uiLock) {
            this.graph = createGraph(configuration, data);
            TimeSeriesIndicatorConfigurationAccessor accessor = TimeSeriesIndicatorConfigurationAccessor.getDefault();
            this.legend = new Legend(timeSeriesList, detailsList);
            this.button = getDefaultAction().isEnabled() ? new JButton(getDefaultAction()) : null;
            this.panel = new GraphPanel<TimeSeriesPlot, Legend>(accessor.getTitle(configuration), graph,
                    legend, graph.getHorizontalAxis(), graph.getVerticalAxis(), button);
            panel.setPopupActions(popupActions);
            isInitialized = true;
        }
    }

    private static TimeSeriesPlot createGraph(TimeSeriesIndicatorConfiguration configuration, TimeSeriesDataContainer data) {
        TimeSeriesIndicatorConfigurationAccessor accessor =
                TimeSeriesIndicatorConfigurationAccessor.getDefault();
        TimeSeriesPlot graph =
                new TimeSeriesPlot(accessor.getGraphScale(configuration),
                accessor.getLabelRenderer(configuration), accessor.getTimeSeriesDescriptors(configuration), data);
        graph.setBorder(BorderFactory.createLineBorder(DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_BORDER_COLOR)));
        Dimension graphSize = new Dimension(
                DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_GRAPH_WIDTH),
                DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_GRAPH_HEIGHT));
        graph.setMinimumSize(graphSize);
        graph.setPreferredSize(graphSize);
        Dimension valueAxisSize = new Dimension(
                DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_Y_AXIS_WIDTH),
                DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_Y_AXIS_HEIGHT));
        graph.getVerticalAxis().setMinimumSize(valueAxisSize);
        graph.getVerticalAxis().setPreferredSize(valueAxisSize);
        Dimension timeAxisSize = new Dimension(
                DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_X_AXIS_WIDTH),
                DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_X_AXIS_HEIGHT));
        graph.getHorizontalAxis().setMinimumSize(timeAxisSize);
        graph.getHorizontalAxis().setPreferredSize(timeAxisSize);
        return graph;
    }

    private static DataTableMetadata createTimeSeriesTableMetadata(String tablePrefix, List<TimeSeriesDescriptor> timeSeriesList) {
        DataTableMetadata table = null;
        if (!timeSeriesList.isEmpty()) {
            List<Column> timeSeriesColumns = new ArrayList<Column>(timeSeriesList.size());
            timeSeriesColumns.add(new Column("timestamp", Long.class)); // NOI18N
            for (TimeSeriesDescriptor timeSeries : timeSeriesList) {
                timeSeriesColumns.add(new Column(TimeSeriesDescriptorAccessor.getDefault().getName(timeSeries), Float.class));
            }
            table = new DataTableMetadata(tablePrefix + "_series", timeSeriesColumns, null); // NOI18N
        }
        return table;
    }

    private static DataTableMetadata createDetailsTableMetadata(String tablePrefix, List<DetailDescriptor> detailsList) {
        DataTableMetadata table = null;
        if (!detailsList.isEmpty()) {
            List<Column> detailsColumns = Arrays.asList(
                    new Column("name", String.class), new Column("value", String.class)); // NOI18N
            table = new DataTableMetadata(tablePrefix + "_details", detailsColumns, null); // NOI18N
        }
        return table;
    }

    public ViewportModel getViewportModel() {
        return graph.getViewportModel();
    }

    public void setViewportModel(ViewportModel viewportModel) {
        graph.setViewportModel(viewportModel);
    }

    @Override
    protected void repairNeeded(boolean needed) {
        if (needed) {
            final RepairPanel repairPanel = new RepairPanel(getRepairActionProvider().getValidationStatus());
            repairPanel.setPopupActions(popupActions);
            repairPanel.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    final Future<Boolean> repairResult = getRepairActionProvider().asyncRepair();
                    DLightExecutorService.submit(new Callable<Boolean>() {

                        public Boolean call() throws Exception {
                            UIThread.invoke(new Runnable() {

                                public void run() {
                                    repairPanel.setEnabled(false);
                                }
                            });
                            Boolean retValue = repairResult.get();
                            UIThread.invoke(new Runnable() {

                                public void run() {
                                    repairPanel.setEnabled(true);
                                }
                            });
                            return retValue;
                        }
                    }, "Click On Indicator task"); //NOI18N
                }
            });
            UIThread.invoke(new Runnable() {

                public void run() {
                    panel.setOverlay(repairPanel);
                }
            });
        } else {
            final JEditorPane label = UIUtilities.createJEditorPane(getRepairActionProvider().getMessage(getRepairActionProvider().getValidationStatus()),
                    false, DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_LEGEND_FONT_COLOR));
            UIThread.invoke(new Runnable() {

                public void run() {
                    panel.setOverlay(label);
                }
            });
        }
    }

    @Override
    protected void tick() {
        ++tickCounter;
        this.data.grow(tickCounter);
        this.detailsValues = dataRowHandler.getDetails();
        refresh();
    }

    @Override
    public void updated(List<DataRow> rows) {
        for (DataRow row : rows) {
            try {
                float[] plotData = dataRowHandler.getData(row);
                if (plotData != null) {
                    long realTimestamp = DataUtil.getTimestamp(row);
                    this.data.put(0 <= realTimestamp ? realTimestamp : 1000000000L * tickCounter, plotData);
                }
            } catch (Exception ex) {
                if (log.isLoggable(Level.WARNING)) {
                    log.log(Level.WARNING, "Exception while updating indicator", ex); // NOI18N
                }
            }
        }
    }

    @Override
    public void suggestRepaint() {
        refresh();
    }

    private void refresh() {
        for (Map.Entry<String, String> entry : detailsValues.entrySet()) {
            legend.updateDetail(entry.getKey(), entry.getValue());
        }
        graph.repaintAll();
    }

    @Override
    public void reset() {
    }

    @Override
    public JComponent getComponent() {
        synchronized (uiLock) {
            if (!isInitialized) {
                initUI();
            }
        }
        return panel;
    }

    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
        graph.dataFiltersChanged(newSet, isAdjusting);
    }

    @Override
    public void setIndicatorActionsProviderContext(Lookup context) {
        List<Action> actions = new ArrayList<Action>();
        for (IndicatorActionsProvider actionsProvider : Lookup.getDefault().lookupAll(IndicatorActionsProvider.class)) {
            actions.addAll(actionsProvider.getIndicatorActions(context)); // FIXUP: add DLightConfiguration, DLightTool, ... to lookup
        }
        if (panel != null) {
            panel.setPopupActions(actions);
        }
        this.popupActions = actions;
    }

    @Override
    public DataStorageType getDataStorageType() {
        return DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE);
    }

    @Override
    public List<DataTableMetadata> getDataTableMetadata() {
        if (timeSeriesTable != null && detailsTable != null) {
            return Collections.unmodifiableList(Arrays.asList(timeSeriesTable, detailsTable));
        } else if (timeSeriesTable != null) {
            return Collections.singletonList(timeSeriesTable);
        } else if (detailsTable != null) {
            return Collections.singletonList(detailsTable);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean loadState(DataStorage storage) {
        if (1 < data.size()) {
            throw new IllegalStateException("Indicator must contain no data when loading"); // NOI18N
        }

        SQLDataStorage sqlStorage = (SQLDataStorage) storage;

        if (timeSeriesTable != null) {
            try {
                ResultSet rs = sqlStorage.select(timeSeriesTable.getName(), timeSeriesTable.getColumns());
                try {
                    while (rs.next()) {
                        long timestamp = rs.getLong(1);
                        float[] dataArray = new float[timeSeriesCount];
                        for (int i = 0; i < timeSeriesList.size(); ++i) {
                            dataArray[i] = rs.getFloat(TimeSeriesDescriptorAccessor.getDefault().getName(timeSeriesList.get(i)));
                        }
                        data.put(timestamp, dataArray);
                    }
                } finally {
                    rs.close();
                }
            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
        }

        if (detailsTable != null) {
            try {
                Map<String, String> map = new HashMap<String, String>();
                ResultSet rs = sqlStorage.select(detailsTable.getName(), detailsTable.getColumns());
                try {
                    while (rs.next()) {
                        String key = rs.getString(1);
                        String value = rs.getString(2);
                        map.put(key, value);
                    }
                } finally {
                    rs.close();
                }
                detailsValues = map;
            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean saveState(DataStorage storage) {
        SQLDataStorage sqlStorage = (SQLDataStorage) storage;

        if (timeSeriesTable != null) {
            List<String> columnNames = timeSeriesTable.getColumnNames();
            List<DataRow> dataRows = new ArrayList<DataRow>();
            for (int i = 0; i < data.size(); ++i) {
                float[] dataArray = data.get(i);
                if (dataArray != null) {
                    List<Object> dataList = new ArrayList<Object>(1 + columnNames.size());
                    dataList.add(i * TimeSeriesIndicatorConfigurationAccessor.getDefault().getGranularity(configuration));
                    for (float x : dataArray) {
                        dataList.add(x);
                    }
                    dataRows.add(new DataRow(columnNames, dataList));
                }
            }
            sqlStorage.syncAddData(timeSeriesTable.getName(), dataRows);
        }

        if (detailsTable != null) {
            List<String> columnNames = detailsTable.getColumnNames();
            List<DataRow> dataRows = new ArrayList<DataRow>();
            for (Map.Entry<String, String> entry : detailsValues.entrySet()) {
                dataRows.add(new DataRow(columnNames, Arrays.asList(entry.getKey(), entry.getValue())));
            }
            sqlStorage.syncAddData(detailsTable.getName(), dataRows);
        }

        return true;
    }

    // for tests
    /*package*/ void dumpData(PrintStream out) {
        out.println("Time Series Data:"); // NOI18N
        for (int i = 0; i < data.size(); ++i) {
            out.printf("%d =>", i); // NOI18N
            for (float x : data.get(i)) {
                out.printf(" %.2f", x); // NOI18N
            }
            out.println();
        }

        out.println("Details:"); // NOI18N
        Set<String> details = new TreeSet<String>(detailsValues.keySet());
        for (String detail : details) {
            out.printf("%s => %s\n", detail, detailsValues.get(detail)); // NOI18N
        }
        out.flush();
    }

    private final static class UILock {
    }
}
