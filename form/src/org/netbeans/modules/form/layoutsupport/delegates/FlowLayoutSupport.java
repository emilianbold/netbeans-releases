/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import org.netbeans.modules.form.layoutsupport.*;

/**
 * @author Tran Duc Trung
 */

public class FlowLayoutSupport extends AbstractLayoutSupport
{
    public Class getSupportedClass() {
        return FlowLayout.class;
    }

    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        if (!(containerDelegate.getLayout() instanceof FlowLayout))
            return -1;

        int vgap = ((FlowLayout) containerDelegate.getLayout()).getVgap();
        Component[] components = containerDelegate.getComponents();
        int[] rowStarts = new int[components.length + 1];
        int[] rowTops = new int[components.length + 1];
        for (int i = 0; i < rowStarts.length; i++) {
            rowStarts[i] = -1;
        }

        // rowStarts keeps indices of the first components on each row
        // rowTops keeps y-position of each row
        
        int lastX = Integer.MAX_VALUE;
        int rowHeight = - vgap;
        int r = 0;
        
        for (int i = 0; i < components.length; i++) {
            Component comp = components[i];
            int posX = comp.getBounds().x;
            if (posX < lastX) {
                rowStarts[r] = i;
                rowTops[r] = rowHeight + vgap;
                if (r > 0)
                    rowTops[r] += rowTops[r-1];
                r++;
                rowHeight = 0;
            }
            rowHeight = Math.max(rowHeight, comp.getSize().height);
            lastX = posX;
        }
        if (r > 0) {
            rowTops[r] = rowTops[r-1] + rowHeight + vgap;
        }

        // find which row the pointer falls in
        
        r = 0;
        int i = 0;
        while (rowStarts[i] >= 0) {
            if (posInCont.y < rowTops[i]) {
                r = i - 1;
                break;
            }
            i++;
        }
        
        if (rowStarts[i] < 0) {
            if (posInCont.y >= rowTops[i]) {
                return components.length;
            }
            else {
                r = i - 1;
            }
        }

        int m = (r == 0) ? 0 : rowStarts[r];
        int n = rowStarts[r + 1];

        if (n > components.length || n < 0)
            n = components.length;

        for (i = m; i < n; i++) {
            Component comp = components[i];
            Rectangle bounds = comp.getBounds();
            int centerX = bounds.x + bounds.width / 2;
            if (posInCont.x < centerX)
                break;
        }

        return i < n ? i : n;
    }

    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        if (!(containerDelegate.getLayout() instanceof FlowLayout))
            return false;
        
        Component[] components = containerDelegate.getComponents();
        int alignment = ((FlowLayout) containerDelegate.getLayout()).getAlignment();
        int hgap = ((FlowLayout) containerDelegate.getLayout()).getHgap();

        int x = 0, y1 = 0, y2 = 0;
        
        if (newIndex <= 0) {
            if (components.length == 0) {
                if (alignment == FlowLayout.RIGHT) {
                    x = containerDelegate.getSize().width;
                }
                else if (alignment == FlowLayout.LEFT) {
                    x = 0;
                }
                else {
                    x = containerDelegate.getSize().width / 2 - 5;
                }
                y1 = 0;
                y2 = 20;
            }
            else {
                Rectangle b = components[0].getBounds();
                x = b.x;
                y1 = b.y;
                y2 = b.y + b.height;
            }
        }
        else if (newIndex >= components.length) {
            Rectangle b = components[components.length - 1].getBounds();
            x = b.x + b.width;
            y1 = b.y;
            y2 = b.y + b.height;
        }
        else {
            Rectangle b1 = components[newIndex].getBounds();
            x = b1.x;
            y1 = b1.y;
            y2 = b1.y + b1.height;
        }
        g.drawRect(x - 10 - hgap / 2, y1, 20, y2 - y1);
        return true;
    }
}
