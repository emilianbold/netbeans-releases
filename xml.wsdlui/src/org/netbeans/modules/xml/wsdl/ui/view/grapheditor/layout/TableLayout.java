/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author anjeleevich
 */
public class TableLayout implements Layout {
    
    private int columnCount;
    private int hgap;
    private int vgap;
    private int minColumnWidth;

    public TableLayout(int columnCount, int hgap, int vgap,
            int minColumnWidth) 
    {
        this.minColumnWidth = minColumnWidth;
        this.columnCount = columnCount;
        this.hgap = hgap;
        this.vgap = vgap;
    }
    
    
    public void layout(Widget widget) {
        List<Widget> children = widget.getChildren();

        int width = (minColumnWidth + hgap) * columnCount - hgap;;
        int y = 0;

        for (int i = 0; i < children.size(); i += columnCount) {
            
            int rowHeight = 0;
            
            for (int j = 0; j < columnCount; j++) {
                Widget w = children.get(i + j);
                Rectangle b = w.getPreferredBounds();
                rowHeight = Math.max(rowHeight, b.height);
            }
            
            int x = 0;
            
            for (int j = 0; j < columnCount; j++) {
                Widget w = children.get(i + j);
                Rectangle b = w.getPreferredBounds();
                b.height = rowHeight;
                b.width = minColumnWidth;
                w.resolveBounds(new Point(x - b.x, y - b.y), b);
                
                x += minColumnWidth + hgap;
            }
            
            y += rowHeight + vgap;
        }
    }

    public boolean requiresJustification(Widget widget) {
        return true;
    }

    public void justify(Widget widget) {
        Rectangle bounds = widget.getClientArea();

        int y0 = bounds.y;

        List<Widget> children = widget.getChildren();

        for (int i = 0; i < children.size(); i += columnCount) {
            int rowHeight = 0;

            for (int j = 0; j < columnCount; j++) {
                Widget w = children.get(i + j);
                Rectangle b = w.getBounds();
                rowHeight = Math.max(rowHeight, b.height);
            }
            
            int x0 = bounds.x;
            int width = bounds.width;
            
            for (int j = 0; j < columnCount; j++) {
                Widget w = children.get(i + j);
                Rectangle b = w.getBounds();
                
                int columnWidth = (width - hgap * (columnCount - 1 - j)) 
                        / (columnCount - j);
                
                b.width = columnWidth;
                b.height = rowHeight;
                
                w.resolveBounds(new Point(x0 - b.x, y0 - b.y), b);
                
                width -= columnWidth + hgap;
                x0 += columnWidth + hgap;
            }
            
            y0 += rowHeight + vgap;
        }
    }
}
