/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.List;

/**
 * @author David Kaspar
 */
public final class SwitchCardProvider implements SelectProvider {

    private Widget cardLayoutWidget;

    public SwitchCardProvider (Widget cardLayoutWidget) {
        this.cardLayoutWidget = cardLayoutWidget;
    }

    public boolean isAimingAllowed (Widget widget, Point localLocation, boolean invertSelection) {
        return false;
    }

    public boolean isSelectionAllowed (Widget widget, Point localLocation, boolean invertSelection) {
        return true;
    }

    public void select (Widget widget, Point localLocation, boolean invertSelection) {
        Widget currentActiveCard = LayoutFactory.getActiveCard (cardLayoutWidget);

        List<Widget> children = cardLayoutWidget.getChildren ();
        int i = children.indexOf (currentActiveCard);
        i ++;
        if (i >= children.size ())
            i = 0;
        Widget newActiveCard = children.get (i);

        if (currentActiveCard == newActiveCard)
            return;

        LayoutFactory.setActiveCard (cardLayoutWidget, newActiveCard);
//        notifyCardSwitched (currentActiveCard, newActiveCard);
    }

}
