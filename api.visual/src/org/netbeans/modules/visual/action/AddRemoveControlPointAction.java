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

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.widget.FreeConnectionWidget;

/**
 * @author Alex
 */
public class AddRemoveControlPointAction extends WidgetAction.Adapter {
    
    private double createSensitivity;
    private double deleteSensitivity;

    public AddRemoveControlPointAction(double createSensitivity, double deleteSensitivity) {
        this.createSensitivity = createSensitivity;
        this.deleteSensitivity = deleteSensitivity;
    }

    public State mouseClicked(Widget widget, WidgetMouseEvent event) {
        if(event.getButton()==MouseEvent.BUTTON1 && event.getClickCount()==2  &&  widget instanceof FreeConnectionWidget) {
            FreeConnectionWidget cWidget=(FreeConnectionWidget)widget;
            cWidget.setSensitivity(createSensitivity, deleteSensitivity);
            Point point=event.getPoint();
            cWidget.addRemoveControlPoint (point);
        }
        return State.REJECTED;
    }
    
}
