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
package org.netbeans.modules.dlight.spi.indicator;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.indicator.IndicatorActionDescriptor;
import org.netbeans.modules.dlight.spi.impl.IndicatorActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.openide.util.Lookup;

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
    private String toolID;
    private String toolDecsription;
    private final List<IndicatorActionListener> listeners;
    private final TickerListener tickerListener;
    private IndicatorRepairActionProvider indicatorRepairActionProvider = null;
    private DLightTarget target;
    private boolean visible;
    private boolean actionsEnabled;
    private final Action defaultAction;
    private final List<Action> actions;
    private final Collection<Column> columnsProvided = new ArrayList<Column>();
    private final AtomicReference<Object> oldRef = new AtomicReference<Object>();
    private List<VisualizerConfiguration> visualizerConfigurations;
    private final T configuration;

    static {
        IndicatorAccessor.setDefault(new IndicatorAccessorImpl());
    }

    protected final void notifyListeners(String vcID) {
        for (VisualizerConfiguration vc : visualizerConfigurations) {
            if (vc.getID().equals(vcID)) {
                notifyListeners(vc);
            }
        }
    }

    protected final String getDescription() {
        return toolDecsription;
    }

    /**
     * Deprecated API to get a tooltip of the default action associated with the 
     * indicator.
     * @see getDefaultAction()
     */
    @Deprecated
    protected final String getActionTooltip() {
        String actionTooltip = (String) getDefaultAction().getValue(Action.SHORT_DESCRIPTION);
        
        if (actionTooltip == null) {
            return null;
        }
        StringBuilder st = new StringBuilder();
        st.append("<html><body>"); // NOI18N
        st.append(actionTooltip.replaceAll("\n", "<br>")); // NOI18N
        st.append("</body></html>"); // NOI18N
        return st.toString();
    }

    private void notifyListeners(VisualizerConfiguration vc) {
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
        this.configuration = configuration;
        this.metadata = IndicatorConfigurationAccessor.getDefault().getIndicatorMetadata(configuration);
        this.visualizerConfigurations = IndicatorConfigurationAccessor.getDefault().getVisualizerConfigurations(configuration);
        this.position = IndicatorConfigurationAccessor.getDefault().getIndicatorPosition(configuration);
        tickerListener = new TickerListener() {

            @Override
            public void tick() {
                Indicator.this.tick();
            }
        };

        this.visible = configuration.isVisible();
        actions = initActions();
        defaultAction = initDefaultAction();
        
        // To support an old sceme...
        if (actions.isEmpty()) {
            actions.add(defaultAction);
        }
        
        setIndicatorActionsProviderContext(Lookup.EMPTY);
    }

    private Action initDefaultAction() {
        IndicatorConfigurationAccessor cfgAccess = IndicatorConfigurationAccessor.getDefault();
        IndicatorActionDescriptor actionDescr = cfgAccess.getDefaultActionDescriptor(configuration);
        
        if (actionDescr == null) {
            return new AbstractAction() {

                @Override
                public boolean isEnabled() {
                    return false;
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                }
            };
        }

        Action result = new AbstractAction(actionDescr.getDisplayName()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                notifyListeners();
            }
        };
        
        String tooltip = actionDescr.getTooltip();
        
        if (tooltip != null) {
            result.putValue(Action.SHORT_DESCRIPTION, tooltip);
        }
        
        return result;
    }

    
    /**
     * This method will be
     * @param context
     */
    public abstract void setIndicatorActionsProviderContext(Lookup context);

    //public abstract Action[]  getActions();
    public final Action getDefaultAction() {
        return defaultAction;
    }
    
    public final List<Action> getActions() {
        return actionsEnabled
                ? Collections.unmodifiableList(actions)
                : Collections.<Action>emptyList();
    }
    
    private List<Action> initActions() {
        IndicatorConfigurationAccessor cfgAccess = IndicatorConfigurationAccessor.getDefault();
        List<IndicatorActionDescriptor> descriptors = cfgAccess.getActionDescriptors(configuration);
        ArrayList<Action> result = new ArrayList<Action>();
        
        for (final IndicatorActionDescriptor d : descriptors) {
            AbstractAction a = new AbstractAction(d.getDisplayName()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    VisualizerConfiguration visCfg = d.getVisualizerConfiguration();
                    if (visCfg != null) {
                        notifyListeners(d.getVisualizerConfiguration());
                    } else {
                        notifyListeners();
                    }
                }
            };
            
            String tooltip = d.getTooltip();

            if (tooltip != null) {
                a.putValue(Action.SHORT_DESCRIPTION, tooltip);
            }
            
            Image image = d.getImage();
            
            if (image != null) {
                a.putValue(Action.LARGE_ICON_KEY, image);
            }
            
            result.add(a);
        }
        
        return result;
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

    @Override
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

    @Override
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
            targetStarted();
        }
    }

    protected void targetStarted() {
    }

    protected final Collection<Column> getColumnsProvided() {
        return Collections.unmodifiableCollection(columnsProvided);
    }

    private void targetFinished(DLightTarget target) {
        synchronized (lock) {
            columnsProvided.clear();
            IndicatorTickerService.getInstance().unsubscribe(tickerListener);
        }
    }

    public final DLightTarget getTarget() {
        synchronized (lock) {
            return target;
        }
    }

    protected abstract void tick();

    @Override
    public void suggestRepaint() {
    }

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
//        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        component.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        component.addMouseListener(new MouseAdapter() {

//            @Override
//            public void mouseEntered(MouseEvent e) {
//                if (component == null) {
//                    return;
//                }
//                component.setBorder(BorderFactory.createEtchedBorder());
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//                if (component == null) {
//                    return;
//                }
//                component.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
//            }
            @Override
            public void mouseClicked(MouseEvent e) {
                component.requestFocus();
                //do not notify anyone - click is not default action anymore
                //notifyListeners();
            }
        });
        final Color c = component.getBackground();
        final Color selectionColor = c == null ? UIManager.getColor("Panel.background") : c.darker(); // NOI18N

        component.addFocusListener(new FocusListener() {

            @Override
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
                iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter"); // NOI18N

                ActionMap aMap = rootPane.getActionMap();
                if (component.getActionMap().get("enter") != null) { // NOI18N
                    aMap.put("enter", component.getActionMap().get("enter")); // NOI18N
                } else {
                    //let to re-define in child
                    aMap.put("enter", new AbstractAction() { // NOI18N

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            notifyListeners();
                        }
                    });
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (component == null) {
                    return;
                }
                component.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
                component.setBackground(c);
                JRootPane rootPane = component.getRootPane();

                if (rootPane == null) {
                    return;
                }
                InputMap iMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), oldRef.get());
                ActionMap aMap = rootPane.getActionMap();
                aMap.remove("enter"); // NOI18N

            }
        });
        //and add input map
    }

    void setToolID(String toolID) {
        this.toolID = toolID;
    }

    private void setToolDescription(String toolDescription) {
        this.toolDecsription = toolDescription;
        if (toolDescription == null) {
            return;
        }
        final JComponent component = getComponent();
        if (component == null) {
            return;
        }
        StringBuilder st = new StringBuilder();
        st.append("<html><body>"); // NOI18N
        st.append(getDescription().replaceAll("\n", "<br>")); // NOI18N
        st.append("</body></html><"); // NOI18N
        component.setToolTipText(st.toString());
    }

    private void setDetailsEnabled(boolean detailsEnabled) {
        this.actionsEnabled = detailsEnabled;
    }

    final List<VisualizerConfiguration> getVisualizerConfigurations() {
        return Collections.unmodifiableList(visualizerConfigurations);
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

    final void columnProvided(Column c) {
        columnsProvided.add(c);
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

//  public final Indicator create(IndicatorConfiguration configuration);
//    return new
//  }
    private static class IndicatorAccessorImpl extends IndicatorAccessor {

        @Override
        public void setToolID(Indicator<?> ind, String toolName) {
            ind.setToolID(toolName);
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
        public String getToolID(Indicator<?> ind) {
            return ind.toolID;
        }

        @Override
        public void initMouseListener(Indicator<?> indicator) {
            indicator.initMouseListener();
        }

        @Override
        public void setRepairActionProviderFor(Indicator<?> indicator, IndicatorRepairActionProvider repairActionProvider) {
            indicator.setRepairActionProviderFor(repairActionProvider);
        }

        @Override
        public void setToolDescription(Indicator<?> ind, String toolDescription) {
            ind.setToolDescription(toolDescription);

        }

        @Override
        public void setDetailsEnabled(Indicator<?> ind, boolean isDetailsEnabled) {
            ind.setDetailsEnabled(isDetailsEnabled);
        }
    }
}
