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

package org.netbeans.modules.compapp.javaee.sunresources.actions;

import java.awt.Point;

import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;

import org.netbeans.modules.compapp.javaee.sunresources.tool.graph.JAXBHandler;

/**
 * @author echou
 *
 */
public class WidgetMoveProvider implements MoveProvider {

    private JAXBHandler jaxbHandler;
    
    public WidgetMoveProvider(JAXBHandler jaxbHandler) {
        this.jaxbHandler = jaxbHandler;
    }
    
    public void movementStarted(Widget arg0) {
        // TODO Auto-generated method stub
        
    }

    public void movementFinished(Widget widget) {
        if (widget instanceof IconNodeWidget) {
            IconNodeWidget iconWidget = (IconNodeWidget) widget;
            if (jaxbHandler != null) {
                jaxbHandler.updateNode(iconWidget.getLabelWidget().getLabel(),
                        iconWidget.getLocation());
            }
        }
        widget.getScene().repaint();
    }

    public Point getOriginalLocation(Widget widget) {
        return widget.getLocation();
    }

    public void setNewLocation(Widget widget, Point location) {
        widget.setPreferredLocation (location);
    }

    
}
