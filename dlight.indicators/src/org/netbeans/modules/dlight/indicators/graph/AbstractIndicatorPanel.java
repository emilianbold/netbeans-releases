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

package org.netbeans.modules.dlight.indicators.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Convenient base class for indicator components.
 *
 * @author Alexey Vladykin
 */
public abstract class AbstractIndicatorPanel extends JPanel {

    protected static final Color TEXT_COLOR = new Color(49, 78, 114);
    protected static final Color BORDER_COLOR = new Color(114, 138, 132);

    protected AbstractIndicatorPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints c;

        JLabel label = new JLabel(getTitle());
        label.setFont(label.getFont().deriveFont(label.getFont().getStyle() | java.awt.Font.BOLD));
        label.setForeground(TEXT_COLOR);
        c = new java.awt.GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(12, 12, 0, 12);
        add(label, c);

        JComponent graph = createGraph();
        graph.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        graph.setMinimumSize(new Dimension(100, 80));
        graph.setPreferredSize(new Dimension(100, 80));

        c = new GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new java.awt.Insets(6, 12, 12, 0);
        add(graph, c);

        JComponent legendPanel = createLegend();
        legendPanel.setBackground(Color.WHITE);
        legendPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        legendPanel.setMinimumSize(new Dimension(100, 80));
        legendPanel.setPreferredSize(new Dimension(100, 80));

        c = new GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.insets = new java.awt.Insets(6, -1, 12, 12);
        add(legendPanel, c);
    }

    protected abstract String getTitle();

    protected abstract JComponent createGraph();

    protected abstract JComponent createLegend();

}
