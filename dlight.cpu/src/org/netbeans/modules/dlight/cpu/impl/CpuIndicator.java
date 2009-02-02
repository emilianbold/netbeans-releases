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

package org.netbeans.modules.dlight.cpu.impl;

import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.indicators.graph.PercentageGraph;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.swing.*;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Kvashin
 */
class CpuIndicator extends Indicator<CpuIndicatorConfiguration> {

    private PercentageGraph graph;
    private JComponent panel;
    private JLabel lblSysLabel;
    private JLabel lblSysValue;
    private JLabel lblUsrLabel;
    private JLabel lblUsrValue;
    private Collection<ActionListener> listeners;

    CpuIndicator(CpuIndicatorConfiguration configuration) {
        super(configuration);
    }

    @Override
    public synchronized  JComponent getComponent() {
        if (panel == null) {

            Color colorSys = Color.RED;
            Color colorUsr = Color.BLUE;

            graph = new PercentageGraph(
                    new GraphDescriptor(colorSys, "System"),
                    new GraphDescriptor(colorUsr, "User"));
            graph.setBorder(BorderFactory.createLineBorder(Color.WHITE));
            Dimension d = new Dimension(66, 32);
            graph.setPreferredSize(d);
            graph.setMaximumSize(d);
            graph.setMinimumSize(d);

            lblSysLabel = new JLabel(NbBundle.getMessage(getClass(), "label.sys"));
            lblSysValue = new JLabel();
            lblSysLabel.setForeground(colorSys);
            lblSysValue.setForeground(colorSys);

            lblUsrLabel = new JLabel(NbBundle.getMessage(getClass(), "label.usr"));
            lblUsrValue = new JLabel();
            lblUsrLabel.setForeground(colorUsr);
            lblUsrValue.setForeground(colorUsr);

            panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = c.gridy = 0;
            c.gridheight = 2;
            panel.add(graph, c);

            c = new GridBagConstraints();
            c.insets = new Insets(0, 6, 0, 0);
            c.anchor = GridBagConstraints.WEST;
            c.gridy = 0;

            c.gridx = 1;
            panel.add(lblSysLabel, c);
            c.gridx = 2;
            panel.add(lblSysValue, c);

            c = new GridBagConstraints();
            c.insets = new Insets(0, 6, 0, 0);
            c.anchor = GridBagConstraints.WEST;
            c.gridy = 1;

            c.gridx = 1;
            panel.add(lblUsrLabel, c);
            c.gridx = 2;
            panel.add(lblUsrValue, c);

            MouseListener ml = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 1) {
                        fireActionPerformed();
                    }
                }
            };
            graph.addMouseListener(ml);
        }
        return panel;
    }

    public void reset() {
        //graph.reset();
    }

    @Override
    public void updated(List<DataRow> data) {
        for (DataRow row : data) {
            if (DLightLogger.instance.isLoggable(Level.FINE)) { DLightLogger.instance.fine("UPDATE: " + row.getData().get(0) + " " + row.getData().get(1)); }
            Float usr = (Float) row.getData().get(0);
            Float sys = (Float) row.getData().get(1);
            graph.addData(sys.shortValue(), usr.shortValue());
        }
        DataRow row = data.get(data.size()-1);
        Float usr = (Float) row.getData().get(0);
        Float sys = (Float) row.getData().get(1);
        lblSysValue.setText(formatValue(sys.intValue()));
        lblUsrValue.setText(formatValue(usr.intValue()));
    }

    private void fireActionPerformed() {
        ActionEvent ae = new ActionEvent(this, 0, null);
        for (ActionListener al : getActionListeners()) {
            al.actionPerformed(ae);
        }
    }

    Collection<ActionListener> getActionListeners() {
        synchronized (this) {
            return (listeners == null) ? Collections.<ActionListener>emptyList() : new ArrayList<ActionListener>(listeners);
        }
    }

    void addActionListener(ActionListener listener) {
        synchronized (this) {
            if (listeners == null) {
                listeners = new ArrayList<ActionListener>();
            }
            listeners.add(listener);
        }
    }

    void removeActionListener(ActionListener listener) {
        synchronized (this) {
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    private String formatValue(int value) {
        return String.format("%02d%%", value);
    }
}
