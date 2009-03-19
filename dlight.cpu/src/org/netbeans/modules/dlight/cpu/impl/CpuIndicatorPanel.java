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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.dlight.indicators.graph.AbstractIndicatorPanel;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.indicators.graph.PercentageGraph;
import org.openide.util.NbBundle;

/**
 * @author Alexey Vladykin
 */
public class CpuIndicatorPanel extends AbstractIndicatorPanel {

    private static final Color COLOR_SYS = new Color(0xFF, 0xC7, 0x26);
    private static final Color COLOR_USR = new Color(0xB2, 0xBC, 0x00);

    private final CpuIndicator indicator;
    private PercentageGraph graph;
    private JPanel legend;
    private JLabel lblSysLabel;
    private JLabel lblSysValue;
    private JLabel lblUsrLabel;
    private JLabel lblUsrValue;

    /*package*/ CpuIndicatorPanel(CpuIndicator indicator) {
        this.indicator = indicator;
    }

    @Override
    protected String getTitle() {
        return NbBundle.getMessage(CpuIndicatorPanel.class, "indicator.title"); // NOI18N
    }

    @Override
    protected JComponent createGraph() {
        if (graph == null) {

            graph = new PercentageGraph(
                    new GraphDescriptor(COLOR_SYS, "System"),
                    new GraphDescriptor(COLOR_USR, "User"));
            graph.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
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
        }
        return graph;
    }

    @Override
    protected JComponent createLegend() {
        if (legend == null) {
            lblSysLabel = new JLabel(NbBundle.getMessage(getClass(), "label.sys"));
            lblSysValue = new JLabel();
            lblSysLabel.setForeground(TEXT_COLOR);
            lblSysValue.setForeground(TEXT_COLOR);

            lblUsrLabel = new JLabel(NbBundle.getMessage(getClass(), "label.usr"));
            lblUsrValue = new JLabel();
            lblUsrLabel.setForeground(TEXT_COLOR);
            lblUsrValue.setForeground(TEXT_COLOR);

            legend = new JPanel(new GridBagLayout());
            legend.setBackground(Color.WHITE);
            legend.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            legend.setMinimumSize(new Dimension(100, 80));
            legend.setPreferredSize(new Dimension(100, 80));

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(0, 6, 0, 0);
            c.anchor = GridBagConstraints.WEST;
            c.gridy = 0;
            c.gridx = 0;
            legend.add(lblSysLabel, c);
            c.gridx = 1;
            legend.add(lblSysValue, c);

            c.insets = new Insets(0, 6, 0, 0);
            c.anchor = GridBagConstraints.WEST;
            c.gridy = 1;

            c.gridx = 0;
            legend.add(lblUsrLabel, c);
            c.gridx = 1;
            legend.add(lblUsrValue, c);
        }
        return legend;
    }

    /*package*/ void addData(int sys, int usr) {
        graph.addData(sys, usr);
    }

    /*package*/ void setSysValue(int v) {
        lblSysValue.setText(formatValue(v));
    }

    /*package*/ void setUsrValue(int v) {
        lblUsrValue.setText(formatValue(v));
    }

    private String formatValue(int value) {
        return String.format("%02d%%", value);
    }
}
