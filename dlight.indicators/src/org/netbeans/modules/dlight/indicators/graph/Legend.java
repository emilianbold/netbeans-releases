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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.indicators.graph;

import org.netbeans.modules.dlight.indicators.DetailDescriptor;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.indicators.impl.TimeSeriesDescriptorAccessor;
import org.netbeans.modules.dlight.util.ui.DLightUIPrefs;

/**
 * Graph legend.
 *
 * @author Alexey Vladykin
 */
public class Legend extends JPanel {
    private final List<TimeSeriesDescriptor> graphs;
    private final List<DetailDescriptor> details;

    public Legend(List<TimeSeriesDescriptor> graphs, List<DetailDescriptor> details) {
        this.graphs = graphs;
        this.details = details;
        init(null);
    }

    private boolean hasInfoToPaint(TimeSeriesDescriptor graph, Collection<Column> providedColumns){
        TimeSeriesDescriptorAccessor accessor = TimeSeriesDescriptorAccessor.getDefault();
        if (providedColumns == null || providedColumns.isEmpty() ||
                accessor.getSourceColumns(graph) == null || accessor.getSourceColumns(graph) .isEmpty()){
            return true;
        }
        //if the  intersection is not empty - returns tru
        ArrayList<Column> list = new ArrayList<Column>(providedColumns);
        list.retainAll(accessor.getSourceColumns(graph));
        return !list.isEmpty();
    }

    private void init(Collection<Column> providedColumns){
        removeAll();
        setLayout(new GridBagLayout());

        Font font = DLightUIPrefs.getFont(DLightUIPrefs.INDICATOR_LEGEND_FONT);

        setBackground(DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_LEGEND_BGCOLOR));
        setBorder(BorderFactory.createLineBorder(DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_BORDER_COLOR)));
        Dimension size = new Dimension(
                DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_LEGEND_WIDTH),
                DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_LEGEND_WIDTH));
        setSize(size);
        setMinimumSize(size);
        setPreferredSize(size);
        setOpaque(true);
        GridBagConstraints c;
        TimeSeriesDescriptorAccessor accessor = TimeSeriesDescriptorAccessor.getDefault();
        for (TimeSeriesDescriptor graph : graphs) {
            //check if should paint the graph
            if (!hasInfoToPaint(graph, providedColumns)){
                    continue;
            }
            JLabel label = new JLabel(accessor.getDisplayName(graph), 
                    new ColorIcon(font.getSize(), accessor.getColor(graph)), SwingConstants.LEADING);
            label.setFont(font);
            label.setForeground(DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_LEGEND_FONT_COLOR));
            c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1.0;
            c.insets = new Insets(4, 4, 0, 4);
            add(label, c);
        }

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(Box.createVerticalStrut(4), c);

        if (details != null) {
            for (DetailDescriptor detail : details) {
                JLabel name = new JLabel(detail.getDisplayName());
                name.setFont(font);
                name.setForeground(DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_LEGEND_FONT_COLOR));
                c = new GridBagConstraints();
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.insets = new Insets(0, 4, 4, 4);
                add(name, c);
                JLabel value = new JLabel(detail.getDefaultValue());
                value.setName(detail.getName());
                value.setForeground(DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_LEGEND_FONT_COLOR));
                value.setFont(font.deriveFont(Font.BOLD));
                c = new GridBagConstraints();
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.weightx = 1.0;
                c.insets = new Insets(0, 0, 4, 4);
                add(value, c);
            }
        }
    }

    public void updateDetail(final String name, final String value) {
        if (SwingUtilities.isEventDispatchThread()) {
            updateDetailImpl(name, value);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateDetailImpl(name, value);
                }
            });
        }
    }

    public final void updateWithInfoProvided(Collection<Column> columns){
        init(columns);
        repaint();
    }

    private void updateDetailImpl(String name, String value) {
        for (int i = 0; i < getComponentCount(); ++i) {
            Component comp = getComponent(i);
            if (comp instanceof JLabel && name.equals(comp.getName())) {
                ((JLabel)comp).setText(value);
                repaint();
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (isEnabled()) {
            super.paintComponent(g);
        } else {
            g.setColor(DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_LEGEND_BGCOLOR));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        if (isEnabled()) {
            super.paintChildren(g);
        }
    }

    private static class ColorIcon implements Icon {

        private final int size;
        private final Color color;

        public ColorIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        public int getIconWidth() {
            return size;
        }

        public int getIconHeight() {
            return size;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, size - 1, size - 1);
            g.setColor(DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_BORDER_COLOR));
            g.drawRect(x, y, size - 1, size - 1);
        }
    }
}
