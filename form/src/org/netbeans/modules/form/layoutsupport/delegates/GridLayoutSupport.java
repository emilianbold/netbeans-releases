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
import java.util.*;
import org.netbeans.modules.form.layoutsupport.*;

/**
 * @author Tran Duc Trung
 */

public class GridLayoutSupport extends AbstractLayoutSupport
{
    public Class getSupportedClass() {
        return GridLayout.class;
    }

    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        if (!(containerDelegate.getLayout() instanceof GridLayout))
            return -1;

        Component[] components = containerDelegate.getComponents();
        GridLayout layout = (GridLayout) containerDelegate.getLayout();
        int nrows = layout.getRows();
        int ncols = layout.getColumns();

        if (nrows <= 0 && ncols <= 0 || components.length == 0)
            return components.length;
        
        if (nrows != 0) {
            ncols = (components.length + nrows - 1) / nrows;
        }
        else {
            nrows = (components.length + ncols - 1) / ncols;
        }

        Dimension sz = containerDelegate.getSize();
        Insets insets = containerDelegate.getInsets();
        sz.width -= insets.left + insets.right;
        sz.height -= insets.top + insets.bottom;

        int colwidth = sz.width / ncols;
        if (colwidth <= 0)
            return components.length;
        int col = (posInCont.x + colwidth / 2) / colwidth;
        
        int rowheight = sz.height / nrows;
        if (rowheight <= 0)
            return components.length;
        int row = posInCont.y / rowheight;

        int newIndex = row * ncols + col;
        return newIndex >= components.length ? components.length : newIndex;
    }

    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        if (!(containerDelegate.getLayout() instanceof GridLayout))
            return false;

        Component[] components = containerDelegate.getComponents();
        GridLayout layout = (GridLayout) containerDelegate.getLayout();
        int dx = 12 + layout.getHgap() / 2;
        int x = 0, w = 24, y = 0, h = 0;
        
        if (newIndex <= 0) {
            if (components.length > 0) {
                Rectangle b = components[0].getBounds();
                x = b.x - dx ;
                y = b.y;
                h = b.height;
            }
            else {
                Insets ins = containerDelegate.getInsets();
                x = ins.left + 1;
                w = containerDelegate.getWidth() - ins.right - ins.left - 2;
                y = ins.top + 1;
                h = containerDelegate.getHeight() - ins.bottom - ins.top - 2;
            }
        }
        else if (newIndex >= components.length) {
            Rectangle b = components[components.length-1].getBounds();
            x = b.x + b.width - dx;
            y = b.y;
            h = b.height;
        }
        else {
            Rectangle b = components[newIndex].getBounds();
            x = b.x - dx;
            y = b.y;
            h = b.height;
        }

        g.drawRect(x, y, w, h);
        return true;
    }
}
