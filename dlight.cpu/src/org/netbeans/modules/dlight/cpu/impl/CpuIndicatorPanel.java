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
package org.netbeans.modules.dlight.cpu.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.dlight.indicators.graph.GraphPanel;
import org.netbeans.modules.dlight.indicators.graph.GraphColors;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.indicators.graph.PercentageGraph;
import org.openide.util.NbBundle;

/**
 * @author Alexey Vladykin
 */
public class CpuIndicatorPanel extends GraphPanel<PercentageGraph, CpuIndicatorPanel.LegendPanel> {

    private static final Color COLOR_SYS = GraphColors.COLOR_3;
    private static final Color COLOR_USR = GraphColors.COLOR_4;

    /*package*/ CpuIndicatorPanel(CpuIndicator indicator) {
        super(getTitle(), createGraph(indicator), createLegend(), null, null);
    }

    private static String getTitle() {
        return NbBundle.getMessage(CpuIndicatorPanel.class, "indicator.title"); // NOI18N
    }

    private static PercentageGraph createGraph(final CpuIndicator indicator) {
        PercentageGraph graph = new PercentageGraph(
                new GraphDescriptor(COLOR_SYS, "System"),
                new GraphDescriptor(COLOR_USR, "User"));
        graph.setBorder(BorderFactory.createLineBorder(GraphColors.BORDER_COLOR));
        graph.setMinimumSize(new Dimension(66, 32));
        graph.setPreferredSize(new Dimension(150, 80));

        MouseListener ml = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    indicator.fireActionPerformed();
                }
            }
        };
        graph.addMouseListener(ml);
        return graph;
    }

    private static LegendPanel createLegend() {
        return new LegendPanel();
    }

    /*package*/ void addData(int sys, int usr) {
        getGraph().addData(sys, usr);
    }

    /*package*/ void setSysValue(int v) {
        getLegend().setSysValue(formatValue(v));
    }

    /*package*/ void setUsrValue(int v) {
        getLegend().setUsrValue(formatValue(v));
    }

    private String formatValue(int value) {
        return String.format("%02d%%", value);
    }

    protected static final class LegendPanel extends JPanel {

        private final JLabel lblSysValue;
        private final JLabel lblUsrValue;

        private LegendPanel() {
            super(new GridBagLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(GraphColors.BORDER_COLOR));
            setMinimumSize(new Dimension(100, 80));
            setPreferredSize(new Dimension(100, 80));

            JLabel lblSysLabel = new JLabel(NbBundle.getMessage(CpuIndicatorPanel.class, "label.sys"));
            lblSysValue = new JLabel();
            lblSysLabel.setForeground(GraphColors.TEXT_COLOR);
            lblSysValue.setForeground(GraphColors.TEXT_COLOR);

            JLabel lblUsrLabel = new JLabel(NbBundle.getMessage(CpuIndicatorPanel.class, "label.usr"));
            lblUsrValue = new JLabel();
            lblUsrLabel.setForeground(GraphColors.TEXT_COLOR);
            lblUsrValue.setForeground(GraphColors.TEXT_COLOR);

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(0, 6, 0, 0);
            c.anchor = GridBagConstraints.WEST;
            c.gridy = 0;
            c.gridx = 0;
            add(lblSysLabel, c);
            c.gridx = 1;
            add(lblSysValue, c);

            c.insets = new Insets(0, 6, 0, 0);
            c.anchor = GridBagConstraints.WEST;
            c.gridy = 1;

            c.gridx = 0;
            add(lblUsrLabel, c);
            c.gridx = 1;
            add(lblUsrValue, c);
        }

        public void setSysValue(String value) {
            lblSysValue.setText(value);
        }

        public void setUsrValue(String value) {
            lblUsrValue.setText(value);
        }
    }
}
