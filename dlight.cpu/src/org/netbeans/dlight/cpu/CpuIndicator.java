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

package org.netbeans.dlight.cpu;

import org.netbeans.modules.dlight.storage.api.DataRow;
import org.netbeans.modules.dlight.indicator.api.IndicatorMetadata;
import org.netbeans.modules.dlight.indicator.api.Indicator;
import org.netbeans.dlight.cpu.graph.PercentageGraph;
import org.netbeans.dlight.cpu.graph.PercentageGraphComponent;
import java.awt.Color;
import java.awt.Dimension;
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
import org.netbeans.modules.dlight.util.DLightLogger;

/**
 *
 * @author Vladimir Kvashin
 */
class CpuIndicator extends Indicator {

    private final PercentageGraphComponent graph;
    private final JPanel panel;
    private Collection<ActionListener> listeners;

    CpuIndicator(IndicatorMetadata metadata) {
        super(metadata);
        graph = new PercentageGraphComponent(
                new PercentageGraph.Descriptor(Color.RED, "System"),
                new PercentageGraph.Descriptor(Color.BLUE, "User"));
        graph.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        Dimension d = new Dimension(66, 32);
        graph.setPreferredSize(d);
        graph.setMaximumSize(d);
        graph.setMinimumSize(d);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalGlue());
        panel.add(graph);
        panel.add(Box.createHorizontalGlue());

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

    @Override
    public JComponent getComponent() {
        return panel;
    }

    public void reset() {
        //graph.reset();
    }

    public void updated( List<DataRow> data) {
        for (DataRow row : data) {
            if (DLightLogger.instance.isLoggable(Level.FINE)) { DLightLogger.instance.fine("UPDATE: " + row.getData().get(0) + " " + row.getData().get(1)); }
            Float usr = (Float) row.getData().get(0);
            Float sys = (Float) row.getData().get(1);
            graph.addData(sys.shortValue(), usr.shortValue());
        }
    }

    private void fireActionPerformed() {
        ActionEvent ae = new ActionEvent(this, 0, null);
        for (ActionListener al : getActionListeners()) {
            al.actionPerformed(ae);
        }
        notifyListeners();
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
}
