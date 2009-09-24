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
package org.netbeans.modules.dlight.extras.api.support;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.dlight.api.datafilter.DataFilterManager;
import org.netbeans.modules.dlight.extras.api.ViewportAware;
import org.netbeans.modules.dlight.util.Range;
import org.netbeans.modules.dlight.extras.api.ViewportModel;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.util.ui.DLightUIPrefs;
import org.openide.util.NbBundle;

/**
 * @author Alexey Vladykin
 */
public final class IndicatorsContainer extends JPanel
        implements AdjustmentListener, ChangeListener {

    private static final long EXTENT = 20000L; // 20 seconds

    private final ViewportBar viewportBar;
    private final JScrollBar scrollBar;
    private final ViewportModel viewportModel;
    private boolean isAdjusting;

    public IndicatorsContainer(DataFilterManager filterManager, List<Indicator<?>> indicators) {
        viewportModel = new DefaultViewportModel();
        viewportModel.setLimits(new Range<Long>(0L, 0L));
        viewportModel.setViewport(new Range<Long>(0L, EXTENT));
        viewportModel.addChangeListener(this);

        JComponent indicatorsComponent = createIndicatorsComponent(indicators, viewportModel);

        int knobSize = UIManager.getInt("ScrollBar.width"); // NOI18N
        int padding = DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_PADDING);
        int leftMargin = padding + DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_Y_AXIS_WIDTH);
        int rightMargin = padding + DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_LEGEND_WIDTH);
        viewportBar = new ViewportBar(viewportModel, filterManager, leftMargin, rightMargin);

        scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        adjust();
        scrollBar.addAdjustmentListener(this);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        add(indicatorsComponent);
        add(viewportBar);

        Box scrollBox = Box.createHorizontalBox();
        scrollBox.setMaximumSize(scrollBar.getMaximumSize());
        scrollBox.setPreferredSize(scrollBar.getPreferredSize());
        scrollBox.add(Box.createHorizontalStrut(leftMargin - knobSize));
        scrollBox.add(scrollBar);
        scrollBox.add(Box.createHorizontalStrut(rightMargin - knobSize));
        add(scrollBox);
    }

    private static JComponent createIndicatorsComponent(List<Indicator<?>> indicators, ViewportModel viewportModel) {
        JComponent indicatorsComponent = null;
        if (indicators != null && !indicators.isEmpty()) {
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
            JSplitPane prevSplit = null;
            Vector<JComponent> indicatorPanels = new Vector<JComponent>(indicators.size());
            indicatorPanels.setSize(indicators.size());
            // We will resize only components without MaximumSize.
            // Implemented for Parallel Adviser indicator.
            int freeSizeComponentsNumber = 0;
            for (int i = 0; i < indicators.size(); ++i) {
                JComponent component = indicators.get(i).getComponent();
                if(!component.isMaximumSizeSet()) {
                    freeSizeComponentsNumber++;
                }
            }
            for (int i = 0; i < indicators.size(); ++i) {
                Indicator<?> indicator = indicators.get(i);
                JComponent component = indicators.get(i).getComponent();
                if (indicator instanceof ViewportAware) {
                    ((ViewportAware) indicator).setViewportModel(viewportModel);
                }
                indicatorPanels.set(i, component);
                if (i + 1 < indicators.size()) {
                    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                    splitPane.setBorder(BorderFactory.createEmptyBorder());
                    splitPane.setContinuousLayout(true);
                    splitPane.setDividerSize(5);
                    if(!component.isMaximumSizeSet()) {
                        splitPane.setResizeWeight(1.0 / (freeSizeComponentsNumber - i));
                    }
                    splitPane.setTopComponent(component);
                    component = splitPane;
                }
                if (prevSplit == null) {
                    scrollPane.setViewportView(component);
                } else {
                    prevSplit.setBottomComponent(component);
                }
                if (component instanceof JSplitPane) {
                    prevSplit = (JSplitPane) component;
                }
            }
            indicatorsComponent = scrollPane;
        } else {
            JLabel emptyLabel = new JLabel(getMessage("IndicatorsContainer.EmptyContent")); // NOI18N
            emptyLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            indicatorsComponent = emptyLabel;
        }
        indicatorsComponent.setBorder(new ThreeSidesBorder());
        return indicatorsComponent;
    }

    private void adjust() {
        Range<Long> limits = viewportModel.getLimits();
        Range<Long> viewport = viewportModel.getViewport();
        limits = limits.extend(viewport);
        isAdjusting = true;
        scrollBar.setMinimum((int) TimeUnit.MILLISECONDS.toSeconds(limits.getStart()));
        scrollBar.setMaximum((int) TimeUnit.MILLISECONDS.toSeconds(limits.getEnd()));
        scrollBar.setValue((int) TimeUnit.MILLISECONDS.toSeconds(viewport.getStart()));
        scrollBar.setVisibleAmount((int) TimeUnit.MILLISECONDS.toSeconds(viewport.getEnd() - viewport.getStart()));
        isAdjusting = false;
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == viewportModel) {
            UIThread.invoke(new Runnable() {
                public void run() {
                    adjust();
                }
            });
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (!isAdjusting) {
            Range<Long> viewport = viewportModel.getViewport();
            long newViewportStart = TimeUnit.SECONDS.toMillis(e.getValue());
            viewportModel.setViewport(new Range<Long>(newViewportStart, newViewportStart + viewport.getEnd() - viewport.getStart()));
        }
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(IndicatorsContainer.class, key);
    }

    private static class ThreeSidesBorder implements Border {

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_BORDER_COLOR));
            g.fillRect(0, 0, width, 2); // top
            g.fillRect(0, 0, 2, height); // left
            g.fillRect(width - 1, 0, 2, height); // right
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 0, 2);
        }

        public boolean isBorderOpaque() {
            return true;
        }
    }
}
