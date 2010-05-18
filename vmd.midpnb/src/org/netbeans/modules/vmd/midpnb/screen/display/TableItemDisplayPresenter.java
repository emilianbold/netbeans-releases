/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
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
    private static JLabel fontLabel = new JLabel();

    private JPanel tablePanel;
    private boolean hasModel;
    private boolean modelIsUserCode;
    private boolean drawBorders = true;
    private String title;
    private String[] columnNames;
    private String[][] values;

    private Font titleFont;
    private Font headersFont;
    private Font valuesFont;

    public TableItemDisplayPresenter() {
        tablePanel = new JPanel() {

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                paintTable(g);
            }
        };
        tablePanel.setOpaque(false);
        tablePanel.setPreferredSize(new Dimension(200, 40)); // TODO compute it from fontSize
        setContentComponent(tablePanel);

        titleFont = fontLabel.getFont();
        headersFont = fontLabel.getFont().deriveFont(Font.BOLD);
        valuesFont = fontLabel.getFont();
    }

    /**
     * prints ModelIsUserCode message 
     * @param g
     * @param currY currect Y coordiname
     * @return new Y coordinate for the next drawing after message is printed
     */
    private int printModelIsUserCode(Graphics g, int currY){
        int newY = printTitle(g, currY);

        newY += ScreenSupport.getFontHeight(g, getValuesFont());
        String msg = NbBundle.getMessage(TableItemDisplayPresenter.class, "DISP_table_is_user_code");
        g.drawString(msg, CELL_INSETS, newY); // NOI18N

        return newY;
    }
    
    /**
     * prints title message 
     * @param g
     * @param currY currect Y coordiname
     * @return new Y coordinate for the next drawing after message is printed
     */
    private int printTitle(Graphics g, int currY){
        int newY = currY;
        if (title != null){
            newY += ScreenSupport.getFontHeight(g, getTitleFont());
            g.setFont(getTitleFont());

            g.drawString(title, CELL_INSETS, newY);
        }
        return newY;
    }
    
    /**
     * prints NoModel message 
     * @param g
     * @param currY currect Y coordiname
     * @return new Y coordinate for the next drawing after message is printed
     */
    private int printNoModel(Graphics g, int currY){
        int newY = printTitle(g, currY);

        newY += ScreenSupport.getFontHeight(g, getValuesFont());
        String msg = NbBundle.getMessage(TableItemDisplayPresenter.class, "DISP_no_table_model_specified");
        g.drawString(msg, CELL_INSETS, newY); // NOI18N

        return newY;
    }
    
    /**
     * prints NoModel message 
     * @param g
     * @param currY currect Y coordiname
     * @return new Y coordinate for the next drawing after message is printed
     */
    private int printEmptyModel(Graphics g, int currY){
        int newY = currY + ScreenSupport.getFontHeight(g, getValuesFont());
        
        String msg = NbBundle.getMessage(TableItemDisplayPresenter.class, "DISP_no_table_model_specified");
        g.drawString(msg, CELL_INSETS, newY); // NOI18N

        return newY;
    }
    
    private Font getTitleFont(){
        return titleFont;
    }
    
    private Font getHeadersFont(){
        return headersFont;
    }
    
    private Font getValuesFont(){
        return valuesFont;
    }
    
    private void paintTable(Graphics g) {
        int cummulativeY = 0;

        if (modelIsUserCode) {
            cummulativeY = printModelIsUserCode(g, cummulativeY);
        } else if (!hasModel) {
            cummulativeY = printNoModel(g, cummulativeY);
        } else if (values == null || values.length < 1) {
            cummulativeY = printEmptyModel(g, cummulativeY);
        } else {
            Graphics2D g2D = (Graphics2D) g;
            Dimension oldSize = tablePanel.getSize();
            final int width = oldSize.width;
            final int height = oldSize.height;

            int firstRowY = 0;

            int[] colWidths = getColWidths(g, title, columnNames, values, 
                    getTitleFont(), getHeadersFont(), getValuesFont());

            if (title != null){
                g.setFont(getTitleFont());
                cummulativeY += ScreenSupport.getFontHeight(g, getTitleFont());
                int titleX = CELL_INSETS + BORDER_LINE_WIDTH;
                g.drawString(title, titleX, cummulativeY);
                cummulativeY += DOUBLE_CELL_INSETS + BORDER_LINE_WIDTH;
                firstRowY = cummulativeY;
            }
            if (columnNames != null) {
                g.setFont(getHeadersFont());
                firstRowY = cummulativeY;
                cummulativeY += ScreenSupport.getFontHeight(g, getHeadersFont());
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
                g.setFont(getValuesFont());
                for (int i = 0; (i < values.length) && (cummulativeY < height); i++) {
                    String[] row = values[i];
                    cummulativeY += ScreenSupport.getFontHeight(g, getValuesFont());
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
                    borderY += ScreenSupport.getFontHeight(g, getHeadersFont()) + DOUBLE_CELL_INSETS;
                    g.drawLine(0, borderY, width, borderY);
                    borderY++;
                }
                if (values != null && values.length > 0) {
                    // horizontal lines
                    for (int i = 0; (i < values.length) && (borderY < height); i++) {
                        borderY += ScreenSupport.getFontHeight(g, getValuesFont()) + DOUBLE_CELL_INSETS;
                        g.drawLine(0, borderY, width, borderY);
                        borderY++;
                    }

                    // vertical lines
                    int borderX = 0;
                    int rows = values[0].length;
                    for (int i = 0; (i < rows) && (borderX < width); i++) {
                        g.drawLine(borderX, firstRowY, borderX, height - 1);
                        borderX += colWidths[i];
                    }
                }
            }
        }
    }

    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);

        PropertyValue value = getComponent().readProperty(TableItemCD.PROP_TITLE);
        if (PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            title = NbBundle.getMessage(TableItemDisplayPresenter.class, "DISP_title_is_user_code");
        } else {
            title = MidpTypes.getString(value);
        }
        value = getComponent().readProperty(TableItemCD.PROP_MODEL);
        modelIsUserCode = PropertyValue.Kind.USERCODE.equals(value.getKind());
        if (!modelIsUserCode) {
            DesignComponent tableModelComponent = value.getComponent();
            hasModel = tableModelComponent != null;

            if (hasModel) {
                value = getComponent().readProperty(TableItemCD.PROP_BORDERS);
                if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
                    drawBorders = MidpTypes.getBoolean(value);
                }

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
        } else {
            hasModel = false;
        }

        setFonts();

        tablePanel.setPreferredSize(calculatePrefferedSize());
        tablePanel.repaint();
    }

    // TODO compute 14 from fontSize
    private Dimension calculatePrefferedSize() {
        final Dimension oldSize = tablePanel.getPreferredSize();
        int height = 0;
        if (title != null) {
            height += DOUBLE_CELL_INSETS + getTitleFont().getSize() + BORDER_LINE_WIDTH;
        }
        
        if ( isMessageNoTable()) {
            height += DOUBLE_CELL_INSETS + getValuesFont().getSize();
        } else {
            if (columnNames != null) {
                height += CELL_INSETS + getHeadersFont().getSize() + BORDER_LINE_WIDTH;
            }
            if (values != null) {
                height += (DOUBLE_CELL_INSETS + getValuesFont().getSize() 
                        + BORDER_LINE_WIDTH) * values.length;
            }
        }
        return new Dimension(oldSize.width, height);
    }
    
    private boolean isMessageNoTable(){
       return   modelIsUserCode                         // user code model message
                || !hasModel                            // no model message
                || values == null || values.length < 1; // empty model message
     }

    // TODO make parameter generic and move to ArraySupport class (gatherPrimitiveValues)
    private static List<String> gatherStringValues(List<PropertyValue> propertyValues) {
        List<String> list = new ArrayList<String>(propertyValues.size());
        for (PropertyValue pv : propertyValues) {
            list.add(MidpTypes.getString(pv));
        }
        return list;
    }

    private int[] getColWidths(Graphics g, String title, String[] headers, String[][] values, Font titleFont, Font headersFont, Font valuesFont) {
        if (values == null || values.length == 0) {
            return null;
        }

        final int tableCols = values[0].length;

        final int[] colWidths = new int[tableCols];
        for (int i = 0; i < tableCols; i++) {
            colWidths[i] = tablePanel.getSize().width / tableCols;
        }

        return colWidths;
    }

    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        List<ScreenPropertyDescriptor> descriptors = new ArrayList<ScreenPropertyDescriptor>(super.getPropertyDescriptors());
        PropertyValue value = getComponent().readProperty(TableItemCD.PROP_MODEL);
        DesignComponent tableModel = null;
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            tableModel = value.getComponent();
        }
        ScreenPropertyEditor tableModelDescriptor = null;
        if (tableModel == null) {
            tableModelDescriptor = new ResourcePropertyEditor(TableItemCD.PROP_MODEL, getComponent());
        } else {
            tableModelDescriptor = new ResourcePropertyEditor(SimpleTableModelCD.PROP_VALUES, tableModel);
        }
        descriptors.add(new ScreenPropertyDescriptor(getComponent(), tablePanel, tableModelDescriptor));
        return descriptors;
    }

    private void setFonts() {
        titleFont = getFont( TableItemCD.PROP_TITLE_FONT );
        valuesFont = getFont( TableItemCD.PROP_VALUES_FONT);
        headersFont = doGetFont( TableItemCD.PROP_HEADERS_FONT);
        if ( headersFont == null ) {
            headersFont = fontLabel.getFont().deriveFont(Font.BOLD);
        }
    }

    private Font getFont( String property ){
        Font font = doGetFont(property);
        if ( font == null ){
            return fontLabel.getFont();
        }
        else {
            return font;
        }
    }

    private Font doGetFont( String property ){
        DesignComponent fontComponent = getComponent().readProperty(
                property).getComponent();
        if ( fontComponent != null ){
            int kindCode = Integer.parseInt( fontComponent.readProperty(
                    FontCD.PROP_FONT_KIND).getPrimitiveValue().toString());
            int faceCode = Integer.parseInt( fontComponent.readProperty(
                    FontCD.PROP_FACE).getPrimitiveValue().toString());
            int styleCode = Integer.parseInt( fontComponent.readProperty(
                    FontCD.PROP_STYLE).getPrimitiveValue().toString());
            int sizeCode = Integer.parseInt( fontComponent.readProperty(
                    FontCD.PROP_SIZE).getPrimitiveValue().toString());
            return ScreenSupport.getFont( getComponent().getDocument(),
                    kindCode, faceCode, styleCode, sizeCode);
        }
        else {
            return null;
        }
    }
}
