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
import javax.swing.*;

import org.netbeans.modules.form.layoutsupport.*;

/**
 * @author Tomas Pavek
 */

public class JToolBarSupport extends AbstractLayoutSupport {

    public Class getSupportedClass() {
        return JToolBar.class;
    }

    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        if (!(container instanceof JToolBar))
            return -1;

        int orientation = ((JToolBar)container).getOrientation();
        
        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            Rectangle b = components[i].getBounds();
            if (orientation == SwingConstants.HORIZONTAL) {
                if (posInCont.x < b.x + b.width / 2)
                    return i;
            }
            else {
                if (posInCont.y < b.y + b.height / 2)
                    return i;
            }
        }

        return components.length;
    }

    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        if (!(container instanceof JToolBar))
            return false;

        int orientation = ((JToolBar)container).getOrientation();
        Component[] components = container.getComponents();
        Rectangle rect;

        if (components.length == 0) {
            Insets ins = container.getInsets();
            rect = orientation == SwingConstants.HORIZONTAL ?
                   new Rectangle(ins.left,
                                 ins.top + (container.getHeight() - ins.top
                                            - ins.bottom - 20) / 2,
                                 30, 20) :
                   new Rectangle(ins.left + (container.getWidth() - ins.left
                                 - ins.right - 30) / 2,
                                 ins.top,
                                 30, 20);
        }
        else if (newIndex < 0 || newIndex >= components.length) {
            Rectangle b = components[components.length - 1].getBounds();
            rect = orientation == SwingConstants.HORIZONTAL ?
                   new Rectangle(b.x + b.width - 10, b.y, 20, b.height) :
                   new Rectangle(b.x, b.y + b.height - 10, b.width, 20);
        }
        else {
            Rectangle b = components[newIndex].getBounds();
            rect = orientation == SwingConstants.HORIZONTAL ?
                   new Rectangle(b.x - 10, b.y, 20, b.height) :
                   new Rectangle(b.x, b.y - 10, b.width, 20);
        }

        g.drawRect(rect.x, rect.y, rect.width, rect.height);

        return true;
    }
}
