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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
