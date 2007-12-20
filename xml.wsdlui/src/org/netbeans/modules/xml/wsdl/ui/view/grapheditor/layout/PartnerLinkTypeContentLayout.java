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
package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;

public class PartnerLinkTypeContentLayout implements Layout {

    private int mGap = 40;
    
    
    //Half Width of Rectangle Widget + RectangleWidget thickness + Border thickness 
    //5                              +   2                       +         3
    private int mHorizontalSpacing = 10;
    
    public PartnerLinkTypeContentLayout(int gap) {
        mGap = gap;
    }
    
    public void justify(Widget widget) {
        Rectangle parentBounds = widget.getClientArea();
        int totalWidth = parentBounds.width;
        
        List<Widget> children = widget.getChildren();
        if (children.size() < 3) return;
        
        Widget firstRole = children.get(0);
        Widget secondRole = children.get(1);
        Widget operationLayer = children.get(2);
        
        Rectangle bounds1 = firstRole.getBounds();
        Rectangle bounds2 = secondRole.getBounds();
        Rectangle bounds3 = operationLayer.getBounds();
        
        int width = Math.max(bounds1.width, bounds2.width);
        
        bounds3.width = totalWidth - width + 2 * mHorizontalSpacing;
        Point point = secondRole.getLocation();
        point.x = totalWidth - bounds2.width;
        operationLayer.resolveBounds(operationLayer.getLocation(), bounds3);
        secondRole.resolveBounds(point, bounds2);
        
        
    }

    public void layout(Widget widget) {
        List<Widget> children = widget.getChildren();
        
        if (children.size() < 3) return;
        
        Widget firstRole = children.get(0);
        Widget secondRole = children.get(1);
        Widget operationLayer = children.get(2);
        
        
        Rectangle bounds1 = firstRole.getPreferredBounds();
        Rectangle bounds2 = secondRole.getPreferredBounds();
        Rectangle bounds3 = operationLayer.getPreferredBounds();
        
        
        int width = Math.max(bounds1.width, bounds2.width);
        int height = Math.max(bounds1.height, bounds2.height);
        
        int totalWidth = Math.max(2 * width + 50, bounds3.width + width - 2 * mHorizontalSpacing); //atleast 50 gap between two roles
        
        bounds3.width = Math.max(totalWidth - width + 2 * mHorizontalSpacing, bounds3.width);  
        
        int realHeight = bounds3.height + mGap;
        
        height = Math.max(height, realHeight);
        
        Rectangle roleBounds = new Rectangle(width, realHeight);
        firstRole.resolveBounds(new Point(), roleBounds);
        secondRole.resolveBounds(new Point(totalWidth - width, 0), roleBounds);
        operationLayer.resolveBounds(new Point(width / 2 - mHorizontalSpacing, mGap), bounds3);
    }

    public boolean requiresJustification(Widget widget) {
        return true;
    }

}
