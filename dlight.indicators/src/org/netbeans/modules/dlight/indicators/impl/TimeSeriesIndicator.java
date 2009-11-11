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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.netbeans.modules.dlight.api.storage.DataUtil;
import org.netbeans.modules.dlight.indicators.DataRowToTimeSeries;
import org.netbeans.modules.dlight.indicators.TimeSeriesIndicatorConfiguration;
import org.netbeans.modules.dlight.indicators.graph.GraphPanel;
import org.netbeans.modules.dlight.indicators.graph.Legend;
import org.netbeans.modules.dlight.indicators.graph.RepairPanel;
import org.netbeans.modules.dlight.indicators.graph.TimeSeriesIndicatorConfigurationAccessor;
import org.netbeans.modules.dlight.indicators.graph.TimeSeriesPlot;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.extras.api.ViewportAware;
import org.netbeans.modules.dlight.extras.api.ViewportModel;
import org.netbeans.modules.dlight.indicators.graph.TimeSeriesDataContainer;
import org.netbeans.modules.dlight.spi.indicator.IndicatorActionsProvider;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.util.UIUtilities;
import org.netbeans.modules.dlight.util.ui.DLightUIPrefs;
import org.openide.util.Lookup;

/**
 * Indicator capable of drawing one or more time series.
 *
 * @author Alexey Vladykin
 */
public final class TimeSeriesIndicator
        extends Indicator<TimeSeriesIndicatorConfiguration>
        implements ViewportAware, DataFilterListener {

    private final static Logger log = DLightLogger.getLogger(TimeSeriesIndicator.class);
    private final DataRowToTimeSeries dataRowHandler;
    private final TimeSeriesDataContainer data;
    private GraphPanel<TimeSeriesPlot, Legend> panel;
    private TimeSeriesPlot graph;
    private Legend legend;
    private JButton button;
    private final int graphCount;
    private int tickCounter;
    private List<Action> popupActions;
    private volatile boolean isInitialized = false;
    private final TimeSeriesIndicatorConfiguration configuration;
    private final UILock uiLock = new UILock();

    public TimeSeriesIndicator(TimeSeriesIndicatorConfiguration configuration) {
        super(configuration);
        this.configuration = configuration;
        TimeSeriesIndicatorConfigurationAccessor accessor = TimeSeriesIndicatorConfigurationAccessor.getDefault();
        this.dataRowHandler = accessor.getDataRowHandler(configuration);
        this.graphCount = accessor.getTimeSeriesDescriptors(configuration).size();
        this.data = new TimeSeriesDataContainer(accessor.getGranularity(configuration), accessor.getAggregation(configuration), graphCount, accessor.getLastNonNull(configuration));
        this.data.put(0, new float[graphCount]);

    }

    @Override
    protected void targetStarted() {
        legend.updateWithInfoProvided(getColumnsProvided());
    }



    private final void initUI() {
        synchronized (uiLock) {
            this.graph = createGraph(configuration, data);
            TimeSeriesIndicatorConfigurationAccessor accessor = TimeSeriesIndicatorConfigurationAccessor.getDefault();
            this.legend = new Legend(accessor.getTimeSeriesDescriptors(configuration), accessor.getDetailDescriptors(configuration));
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
        for (Map.Entry<String, String> entry : dataRowHandler.getDetails().entrySet()) {
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

    private final static class UILock {
    }
}
