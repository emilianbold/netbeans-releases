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
package org.netbeans.modules.dlight.spi.indicator;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.spi.impl.IndicatorActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.dlight.api.execution.DLightTargetListener;
import org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.impl.IndicatorConfigurationAccessor;
import org.netbeans.modules.dlight.spi.impl.IndicatorAccessor;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;

/**
 * Indicator is a small, graphical, real-time monitor
 * which shows some piece of info.
 *
 * To provide own Indicator you should do the following:
 * <ul>
 *  <li> Create own org.netbeans.modules.dlight.indicator.api.IndicatorConfiguration
 *  <li> Extend Indicator with the specialization for your configuration
 *  <li> Create and register in Global Lookup factory to create
 *   your Indicator instance: {@link org.netbeans.modules.dlight.spi.indicator.IndicatorFactory}
 * </ul>
 *
 * @param <T> configuration indicator can be built on the base of
 */
public abstract class Indicator<T extends IndicatorConfiguration> implements DLightTargetListener, ChangeListener, IndicatorNotificationsListener {

    private static final int PADDING = 2;
    private final Object lock = new Object();
    private final IndicatorMetadata metadata;
    private final int position;
    private String toolName;
    private String actionDisplayName;
    private final List<IndicatorActionListener> listeners;
    private final TickerListener tickerListener;
    private IndicatorRepairActionProvider indicatorRepairActionProvider = null;
    private DLightTarget target;
    private boolean visible;

    static {
        IndicatorAccessor.setDefault(new IndicatorAccessorImpl());
    }
    private List<VisualizerConfiguration> visualizerConfigurations;

    protected final void notifyListeners(String  vcID){
        for (VisualizerConfiguration vc : visualizerConfigurations){
            if (vc.getID().equals(vcID)){
                notifyListeners(vc);
            }
        }
    }

    private void notifyListeners(VisualizerConfiguration vc){
        for (IndicatorActionListener l : listeners) {
            l.openVisualizerForIndicator(this, vc);
        }
    }
    protected final void notifyListeners() {
        for (IndicatorActionListener l : listeners) {
            l.mouseClickedOnIndicator(this);
        }
    }

    protected Indicator(T configuration) {
        listeners = Collections.synchronizedList(new ArrayList<IndicatorActionListener>());
        this.metadata = IndicatorConfigurationAccessor.getDefault().getIndicatorMetadata(configuration);
        this.visualizerConfigurations = IndicatorConfigurationAccessor.getDefault().getVisualizerConfigurations(configuration);
        this.position = IndicatorConfigurationAccessor.getDefault().getIndicatorPosition(configuration);
        this.actionDisplayName = IndicatorConfigurationAccessor.getDefault().getActionDisplayName(configuration);
        tickerListener = new TickerListener() {

            public void tick() {
                Indicator.this.tick();
            }
        };

        this.visible = configuration.isVisible();
    }

    //public abstract Action[]  getActions();
    public final Action getDefaultAction() {
        AbstractAction action = new AbstractAction(actionDisplayName) {

            public void actionPerformed(ActionEvent e) {
                notifyListeners();
            }
        };
        return action;
    }

    protected abstract void repairNeeded(boolean needed);

    private void setRepairActionProviderFor(IndicatorRepairActionProvider repairActionProvider) {
        this.indicatorRepairActionProvider = repairActionProvider;
        indicatorRepairActionProvider.addChangeListener(this);
        repairNeeded(true);
    }

    public final int getPosition() {
        return position;
    }

    protected final IndicatorRepairActionProvider getRepairActionProvider() {
        return indicatorRepairActionProvider;
    }

    public void stateChanged(ChangeEvent e) {
        if (indicatorRepairActionProvider == null || e.getSource() != indicatorRepairActionProvider) {
            return;
        }
        boolean needRepair = indicatorRepairActionProvider.needRepair() || !indicatorRepairActionProvider.getValidationStatus().isValid();
        if (!needRepair) {
            indicatorRepairActionProvider.removeChangeListener(this);
        }
        repairNeeded(needRepair);
    }

    public void targetStateChanged(DLightTargetChangeEvent event) {
        switch (event.state) {
            case RUNNING:
                targetStarted(event.target);
                return;
            case FAILED:
                targetFinished(event.target);
                return;
            case TERMINATED:
                targetFinished(event.target);
                return;
            case DONE:
                targetFinished(event.target);
                return;
            case STOPPED:
                targetFinished(event.target);
                return;
        }
    }

    private void targetStarted(DLightTarget target) {
        synchronized (lock) {
            this.target = target;
            IndicatorTickerService.getInstance().subsribe(tickerListener);
        }
    }

    private void targetFinished(DLightTarget target) {
        synchronized (lock) {
            IndicatorTickerService.getInstance().unsubscribe(tickerListener);
        }
    }

    public final DLightTarget getTarget() {
        synchronized (lock) {
            return target;
        }
    }

    protected abstract void tick();

    public final boolean isVisible() {
        return visible;
    }

    public final void setVisible(boolean visible) {
        this.visible = visible;
    }

    private void initMouseListener() {
        final JComponent component = getComponent();
        if (component == null) {
            return;
        }
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        component.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        component.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                if (component == null) {
                    return;
                }
                component.setBorder(BorderFactory.createEtchedBorder());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (component == null) {
                    return;
                }
                component.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                component.requestFocus();
                notifyListeners();
            }
        });
        final Color c = component.getBackground();
        final Color selectionColor = c == null ? (UIManager.getColor("Panel.background") == null ? c : UIManager.getColor("Panel.background").darker()) : c.darker();//UIManager.getColor("TextField.selectionBackground");
        component.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                if (component == null) {
                    return;
                }
                component.setBorder(BorderFactory.createEtchedBorder());
                component.setBackground(selectionColor);
                JRootPane rootPane = component.getRootPane();
                if (rootPane == null) {
                    return;
                }
                InputMap iMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");//NOI18N

                ActionMap aMap = rootPane.getActionMap();
                aMap.put("enter", new AbstractAction() {//NOI18N

                    public void actionPerformed(ActionEvent e) {
                        notifyListeners();
                    }
                });
            }

            public void focusLost(FocusEvent e) {
                if (component == null) {
                    return;
                }
                component.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
                component.setBackground(c);

            }
        });
        //and add input map
    }

    void setToolName(String toolName) {
        this.toolName = toolName;
    }

    final List<VisualizerConfiguration> getVisualizerConfigurations() {
        return visualizerConfigurations;
    }

    void addIndicatorActionListener(IndicatorActionListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    void removeIndicatorActionListener(IndicatorActionListener l) {
        listeners.remove(l);
    }

    /**
     * Returns indicator metadata
     * @return metada - list of columns
     */
    public IndicatorMetadata getMetadata() {
        return metadata;
    }

    /**
     * Returns list of columns
     * @return return columns of {@link #getMetadata() }
     */
    protected List<Column> getMetadataColumns() {
        return metadata.getColumns();
    }

    /**
     * Return column name for the column with index <code>idx</code>
     * @param idx index of column to get name of
     * @return column name, <code>null</code> if there is no column with the index <code>idx</code>
     */
    protected String getMetadataColumnName(int idx) {
        if (idx < 0 || idx >= metadata.getColumnsCount()) {
            return null;
        }
        Column col = metadata.getColumns().get(idx);
        return col.getColumnName();
    }

    /**
     * Returns component this indicator will paint data at
     * @return component this indicator will paint own data at
     */
    public abstract JComponent getComponent();

    // Dirty hack for TimeSeriesIndicatorManager
    // TODO: remove it
    public JComponent getAuxComponent() {
        return null;
    }

//  public final Indicator create(IndicatorConfiguration configuration);
//    return new
//  }
    private static class IndicatorAccessorImpl extends IndicatorAccessor {

        @Override
        public void setToolName(Indicator<?> ind, String toolName) {
            ind.setToolName(toolName);
        }

        @Override
        public List<Column> getMetadataColumns(Indicator<?> indicator) {
            return indicator.getMetadataColumns();
        }

        @Override
        public String getMetadataColumnName(Indicator<?> indicator, int idx) {
            return indicator.getMetadataColumnName(idx);
        }

        @Override
        public List<VisualizerConfiguration> getVisualizerConfigurations(Indicator<?> indicator) {
            return indicator.getVisualizerConfigurations();
        }

        @Override
        public void addIndicatorActionListener(Indicator<?> indicator, IndicatorActionListener l) {
            indicator.addIndicatorActionListener(l);
        }

        @Override
        public void removeIndicatorActionListener(Indicator<?> indicator, IndicatorActionListener l) {
            indicator.removeIndicatorActionListener(l);
        }

        @Override
        public String getToolName(Indicator<?> ind) {
            return ind.toolName;
        }

        @Override
        public void initMouseListener(Indicator<?> indicator) {
            indicator.initMouseListener();
        }

        @Override
        public void setRepairActionProviderFor(Indicator<?> indicator, IndicatorRepairActionProvider repairActionProvider) {
            indicator.setRepairActionProviderFor(repairActionProvider);
        }
    }
}
