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

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.screen.display.ItemDisplayPresenter;
import org.netbeans.modules.vmd.midpnb.components.items.TableItemCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class TableItemDisplayPresenter extends ItemDisplayPresenter {
    
    private static final int BORDER_LINE_WIDTH = 1;
    private static final int CELL_INSETS = 2;
    private static final int DOUBLE_CELL_INSETS = 2 * CELL_INSETS;
    
    private static final Stroke BORDER_STROKE = new BasicStroke(1, BasicStroke.CAP_SQUARE,
            BasicStroke.JOIN_ROUND, 0f,  new float[] {3f ,3f}, 0f);
    
    private JPanel panel;
    private static JLabel label;
    private boolean hasModel;
    private String[] columnNames;
    private String[][] values;
    
    public TableItemDisplayPresenter() {
        label = new JLabel();
        
        panel = new JPanel() {
            public void paint(Graphics g) {
                super.paint(g);
                paintTable(g);
            }
        };
        panel.setOpaque(false);
        setContentComponent(panel);
    }
    
    private void paintTable(Graphics g) {
        Font headersFont = label.getFont();
        Font valuesFont = label.getFont();
        int cummulativeY = 0;
        
        if (!hasModel) {
            cummulativeY += getFontHeight(g, valuesFont);
            g.drawString("No model specified", CELL_INSETS, cummulativeY);
        } else {
            Graphics2D g2D = (Graphics2D)g;
            final Dimension size = panel.getSize();
            int x = 0;
            int y = 0;
            final int width = size.width;
            final int height = size.height;
            
            int headersY = 0;
            int valuesY = 0;
            
            int[] colWidths = getColWidths(g, values,columnNames, headersFont, valuesFont);
            
            if (columnNames != null) {
                g.setFont(headersFont);
                headersY = cummulativeY;
                cummulativeY += getFontHeight(g, headersFont);
                int cummulativeX = x + CELL_INSETS + BORDER_LINE_WIDTH;
                // draw headers ...
                for (int i=0; (i < columnNames.length) && (cummulativeX < width); i++) {
                    String name = columnNames[i];
                    if (name != null) {
                        g.drawString(name, cummulativeX, cummulativeY);
                    }
                    cummulativeX += colWidths[i];
                    
                }
                cummulativeY +=  DOUBLE_CELL_INSETS + BORDER_LINE_WIDTH;
            }
            
            if (values != null) {
                g.setFont(valuesFont);
                valuesY = cummulativeY;
                for (int i=0; (i < values.length) && (cummulativeY < height); i++) {
                    String[] row = values[i];
                    cummulativeY += getFontHeight(g,valuesFont);
                    int cummulativeX = x + CELL_INSETS + BORDER_LINE_WIDTH;
                    for (int j=0; (j < row.length) && (cummulativeX < width); j++) {
                        String cell = row[j];
                        if (cell != null) {
                            g.drawString(cell, cummulativeX, cummulativeY);
                        }
                        cummulativeX += colWidths[j];
                    }
                    cummulativeY += DOUBLE_CELL_INSETS + BORDER_LINE_WIDTH;
                }
            }
            
            // draw borders
            g2D.setStroke(BORDER_STROKE);
            g.drawRect(0,0,width-1,height-1);
            g.drawLine(0,cummulativeY,width,cummulativeY);
            if (columnNames != null) {
                cummulativeY += getFontHeight(g,headersFont) + DOUBLE_CELL_INSETS;
                g.drawLine(0,cummulativeY,width,cummulativeY);
                cummulativeY ++;
            }
            if (values != null) {
                // horizontal lines
                for (int i=0; (i < values.length) && (cummulativeY < height); i++) {
                    cummulativeY += getFontHeight(g,valuesFont) + DOUBLE_CELL_INSETS;
                    g.drawLine(0,cummulativeY,width,cummulativeY);
                    cummulativeY ++;
                }
                
                // vertical lines
                int cummulativeX = x;
                int rows = values[0].length;
                for (int i=0; (i < rows) && (cummulativeX < width); i++) {
                    g.drawLine(cummulativeX,headersY,cummulativeX,height-1);
                    cummulativeX += colWidths[i];
                }
            }
        }
    }
    
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        
        
        DesignComponent tableModelComponent = getComponent().readProperty(TableItemCD.PROP_MODEL).getComponent();
        hasModel = tableModelComponent != null;

        if (hasModel) {
            PropertyValue columnsProperty = tableModelComponent.readProperty(SimpleTableModelCD.PROP_COLUMN_NAMES);
            List<PropertyValue> list = columnsProperty.getArray();
            if (list != null) {
                columnNames = new java.lang.String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    columnNames[i] = MidpTypes.getString(list.get(i));
                }
            }
            
            PropertyValue valuesProperty = tableModelComponent.readProperty(SimpleTableModelCD.PROP_VALUES);
            list = valuesProperty.getArray();
            if (list != null) {
                values = new String[list.size()][];
                for (int i = 0; i < list.size(); i++) {
                    List<String> row = gatherStringValues(list.get(i).getArray());
                    values[i] = row.toArray(new String[row.size()]);
                }
            }
        }

        Dimension size = new Dimension(deviceInfo.getCurrentScreenSize());
        size.height = 60;
        size.width -= 10;
        panel.setMinimumSize(size);
        
        panel.repaint();
    }
    
    // TODO make parameter as generic type and move to ArraySupport class (gatherPrimitiveValues)
    private static List<String> gatherStringValues(List<PropertyValue> propertyValues) {
        List<String> list = new ArrayList<String>(propertyValues.size());
        for (PropertyValue pv : propertyValues) {
            list.add(MidpTypes.getString(pv));
        }
        return list;
    }
    
    private int getFontHeight(Graphics g, Font f) {
        assert (g != null) && (f != null);
        FontMetrics fm = g.getFontMetrics(f);
        return fm.getHeight();
    }
    
    private static int[] getColWidths(Graphics g, String[][] values, String[] headers, Font headersFont, Font valuesFont) {
        if (values == null) {
            return new int[0];
        } // else
        
        final int tableRows = values.length;
        final int tableCols = values[0].length;
        final int defaultCellWidth = g.getFontMetrics(valuesFont).charWidth('X') + DOUBLE_CELL_INSETS;
        final FontMetrics valuesFM = g.getFontMetrics(valuesFont);
        final FontMetrics headersFM = g.getFontMetrics(headersFont);
        
        final int[] colWidths = new int[tableCols];
        for (int i=0; i < tableCols; i++) {
            colWidths[i] = defaultCellWidth;
        }
        
        
        for (int i=0; i < tableCols; i++) {
            for (int j=0; j < tableRows; j++) {
                String value = values[j][i];
                if (value != null) {
                    int width = valuesFM.stringWidth(value) + DOUBLE_CELL_INSETS;
                    if (width > colWidths[i]) {
                        colWidths[i] = width;
                    }
                }
            }
        }
        // column headers (they might be bigger)
        if (headers != null) {
            for (int i=0; i < tableCols; i++) {
                String columnName = headers[i];
                if (columnName != null) {
                    int width = headersFM.stringWidth(columnName) + DOUBLE_CELL_INSETS;
                    if (width > colWidths[i]) {
                        colWidths[i] = width;
                    }
                }
            }
        }
        
        return colWidths;
    }
}
