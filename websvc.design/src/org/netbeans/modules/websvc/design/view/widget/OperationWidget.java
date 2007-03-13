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

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;

/**
 *
 * @author Ajit Bhate
 */
public class OperationWidget extends Widget{
    
    private static final Color FILL_COLOR = new Color(204,255,255);
    private static final Color BORDER_COLOR = new Color(153,204,255);
    private LabelWidget label;

    /**
     * Creates a new instance of OperationWidget
     * @param scene
     * @param operation
     */
    public OperationWidget(Scene scene, WsdlOperation operation) {
        super(scene);
        label = new LabelWidget(scene,operation.getName());
        addChild(label);
    }
    
    protected Rectangle calculateClientArea() {
        Rectangle labelBounds = label.getBounds();
        return new Rectangle(
                labelBounds.x - labelBounds.height, // x
                labelBounds.y - labelBounds.height/2,// y
                labelBounds.width + 2*labelBounds.height, //width
                2*labelBounds.height //height
                );
    }
    
    protected void paintWidget() {
        Rectangle bounds = getBounds();
        Polygon polygon = new Polygon();
        polygon.addPoint(bounds.x+bounds.height/2, bounds.y);
        polygon.addPoint(bounds.x+bounds.width-bounds.height/2, bounds.y);
        polygon.addPoint(bounds.x+bounds.width, bounds.y+bounds.height/2);
        polygon.addPoint(bounds.x+bounds.width-bounds.height/2, bounds.y+bounds.height);
        polygon.addPoint(bounds.x+bounds.height/2, bounds.y+bounds.height);
        polygon.addPoint(bounds.x, bounds.y+bounds.height/2);
        Graphics2D g = getGraphics();
        g.setColor(FILL_COLOR);
        g.fillPolygon(polygon);
        g.setColor(BORDER_COLOR);
        g.drawPolygon(polygon);
    }
    
}
