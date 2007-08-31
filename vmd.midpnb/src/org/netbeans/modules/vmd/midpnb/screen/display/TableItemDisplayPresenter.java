/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.midpnb.screen.display;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyEditor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.screen.display.ItemDisplayPresenter;
import org.netbeans.modules.vmd.midp.screen.display.ScreenSupport;
import org.netbeans.modules.vmd.midp.screen.display.property.ResourcePropertyEditor;
import org.netbeans.modules.vmd.midpnb.components.items.TableItemCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class TableItemDisplayPresenter extends ItemDisplayPresenter {

    private static final int BORDER_LINE_WIDTH = 1;
    private static final int CELL_INSETS = 2;
    private static final int DOUBLE_CELL_INSETS = CELL_INSETS * 2;
    private static final Stroke BORDER_STROKE = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0f, new float[]{3f, 3f}, 0f);
    private JPanel panel;
    private static JLabel label;
    private boolean hasModel;
    private boolean drawBorders;
    private String[] columnNames;
    private String[][] values;

    public TableItemDisplayPresenter() {
        label = new JLabel();
        panel = new JPanel() {

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                paintTable(g);
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(200, 40)); // TODO compute it from fontSize
        setContentComponent(panel);
    }

    private void paintTable(Graphics g) {
        Font headersFont = label.getFont().deriveFont(Font.BOLD);
        Font valuesFont = label.getFont();
        int cummulativeY = 0;

        if (!hasModel) {
            cummulativeY += ScreenSupport.getFontHeight(g, valuesFont);
            g.drawString(NbBundle.getMessage(TableItemDisplayPresenter.class, "DISP_no_table_model_specified"), CELL_INSETS, cummulativeY); // NOI18N
        } else if (values == null || values.length < 1) {
            cummulativeY += ScreenSupport.getFontHeight(g, valuesFont);
            g.drawString(NbBundle.getMessage(TableItemDisplayPresenter.class, "DISP_empty_table_model"), CELL_INSETS, cummulativeY); // NOI18N
        } else {
            Graphics2D g2D = (Graphics2D) g;
            Dimension oldSize = panel.getSize();
            final int width = oldSize.width;
            final int height = oldSize.height;

            int headersY = 0;

            int[] colWidths = getColWidths(g, values, columnNames, headersFont, valuesFont);

            if (columnNames != null) {
                g.setFont(headersFont);
                headersY = cummulativeY;
                cummulativeY += ScreenSupport.getFontHeight(g, headersFont);
                int cummulativeX = CELL_INSETS + BORDER_LINE_WIDTH;
                // draw headers ...
                for (int i = 0; (i < columnNames.length) && (cummulativeX < width); i++) {
                    String name = columnNames[i];
                    if (name != null) {
                        g.drawString(name, cummulativeX, cummulativeY);
                    }
                    if (colWidths != null) {
                        cummulativeX += colWidths[i];
                    }
                }
                cummulativeY += DOUBLE_CELL_INSETS + BORDER_LINE_WIDTH;
            }

            if (values != null && values.length > 0) {
                g.setFont(valuesFont);
                for (int i = 0; (i < values.length) && (cummulativeY < height); i++) {
                    String[] row = values[i];
                    cummulativeY += ScreenSupport.getFontHeight(g, valuesFont);
                    int cummulativeX = CELL_INSETS + BORDER_LINE_WIDTH;
                    for (int j = 0; (j < row.length) && (cummulativeX < width); j++) {
                        String cell = row[j];
                        if (cell != null) {
                            g.drawString(cell, cummulativeX, cummulativeY);
                        }
                        if (colWidths != null) {
                            cummulativeX += colWidths[j];
                        }
                    }
                    cummulativeY += DOUBLE_CELL_INSETS + BORDER_LINE_WIDTH;
                }
            }

            // draw borders
            if (drawBorders) {
                g2D.setStroke(BORDER_STROKE);
                g.drawRect(0, 0, width - 1, height - 1);
                g.drawLine(0, cummulativeY, width, cummulativeY);
                int borderY = 0;
                if (columnNames != null) {
                    borderY += ScreenSupport.getFontHeight(g, headersFont) + DOUBLE_CELL_INSETS;
                    g.drawLine(0, borderY, width, borderY);
                    borderY++;
                }
                if (values != null && values.length > 0) {
                    // horizontal lines
                    for (int i = 0; (i < values.length) && (borderY < height); i++) {
                        borderY += ScreenSupport.getFontHeight(g, valuesFont) + DOUBLE_CELL_INSETS;
                        g.drawLine(0, borderY, width, borderY);
                        borderY++;
                    }

                    // vertical lines
                    int borderX = 0;
                    int rows = values[0].length;
                    for (int i = 0; (i < rows) && (borderX < width); i++) {
                        g.drawLine(borderX, headersY, borderX, height - 1);
                        borderX += colWidths[i];
                    }
                }
            }
        }
    }

    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        DesignComponent tableModelComponent = getComponent().readProperty(TableItemCD.PROP_MODEL).getComponent();
        hasModel = tableModelComponent != null;

        if (hasModel) {
            drawBorders = MidpTypes.getBoolean(getComponent().readProperty(TableItemCD.PROP_BORDERS));
            
            PropertyValue columnsProperty = tableModelComponent.readProperty(SimpleTableModelCD.PROP_COLUMN_NAMES);
            List<PropertyValue> list = columnsProperty.getArray();
            if (list != null) {
                columnNames = new java.lang.String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    columnNames[i] = MidpTypes.getString(list.get(i));
                }
            } else {
                columnNames = null;
            }

            PropertyValue valuesProperty = tableModelComponent.readProperty(SimpleTableModelCD.PROP_VALUES);
            list = valuesProperty.getArray();
            if (list != null) {
                values = new String[list.size()][];
                for (int i = 0; i < list.size(); i++) {
                    List<String> row = gatherStringValues(list.get(i).getArray());
                    values[i] = row.toArray(new String[row.size()]);
                }
            } else {
                values = null;
            }
        }

        panel.setPreferredSize(calculatePrefferedSize());
        panel.repaint();
    }

    // TODO compute 14 from fontSize
    private Dimension calculatePrefferedSize() {
        final Dimension oldSize = panel.getPreferredSize();
        if (!hasModel || values == null) {
            return oldSize;
        }

        int height = 0;
        if (columnNames != null) {
            height += CELL_INSETS + 14 + BORDER_LINE_WIDTH;
        }
        if (values != null) {
            height += (DOUBLE_CELL_INSETS + 14 + BORDER_LINE_WIDTH) * values.length;
        }
        return new Dimension(oldSize.width, height);
    }

    // TODO make parameter generic and move to ArraySupport class (gatherPrimitiveValues)
    private static List<String> gatherStringValues(List<PropertyValue> propertyValues) {
        List<String> list = new ArrayList<String>(propertyValues.size());
        for (PropertyValue pv : propertyValues) {
            list.add(MidpTypes.getString(pv));
        }
        return list;
    }

    private int[] getColWidths(Graphics g, String[][] values, String[] headers, Font headersFont, Font valuesFont) {
        if (values == null || values.length == 0) {
            return null;
        }

        final int tableCols = values[0].length;

        final int[] colWidths = new int[tableCols];
        for (int i = 0; i < tableCols; i++) {
            colWidths[i] = panel.getSize().width / tableCols;
        }

        return colWidths;
    }

    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        List<ScreenPropertyDescriptor> descriptors = new ArrayList<ScreenPropertyDescriptor>(super.getPropertyDescriptors());
        DesignComponent tableModel = getComponent().readProperty(TableItemCD.PROP_MODEL).getComponent();
        ScreenPropertyEditor tableModelDescriptor = null;
        if (tableModel == null) {
            tableModelDescriptor = new ResourcePropertyEditor(TableItemCD.PROP_MODEL, getComponent());
        } else {
            tableModelDescriptor = new ResourcePropertyEditor(SimpleTableModelCD.PROP_VALUES, tableModel);
        }
        descriptors.add(new ScreenPropertyDescriptor(getComponent(), panel, tableModelDescriptor));
        return descriptors;
    }
}