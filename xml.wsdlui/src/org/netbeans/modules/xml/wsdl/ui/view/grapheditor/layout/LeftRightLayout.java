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
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author anjeleevich
 */
public class LeftRightLayout implements Layout {
        
    private int hgap;
    private int maxLeftWidth;
    
    public LeftRightLayout(int hgap) {
        this(hgap, 100000);
    }
    
    
    public LeftRightLayout(int hgap, int maxLeftWidth) {
        this.hgap = hgap;
        this.maxLeftWidth = maxLeftWidth;
    }

    
    public void layout(Widget widget) {
        Widget w1 = widget.getChildren().get(0);
        Widget w2 = widget.getChildren().get(1);

        Rectangle b1 = w1.getPreferredBounds();
        Rectangle b2 = w2.getPreferredBounds();
        
        b1.width = Math.min(b1.width, maxLeftWidth);

        int height = Math.max(b1.height, b2.height);
        int width = b1.width + hgap + b2.width;

        int x1 = -b1.x;
        int x2 = width - b2.width - b2.x;

        int y1 = (height - b1.height) / 2 - b1.y;
        int y2 = (height - b2.height) / 2 - b2.y;

        w1.resolveBounds(new Point(x1, y1), b1);
        w2.resolveBounds(new Point(x2, y2), b2);
    }

    
    public boolean requiresJustification(Widget widget) {
        return true;
    }
    

    public void justify(Widget widget) {
        Widget w1 = widget.getChildren().get(0);
        Widget w2 = widget.getChildren().get(1);

        Rectangle bounds = widget.getClientArea();

        Point l1 = w1.getLocation();
        Point l2 = w2.getLocation();

        Rectangle b1 = w1.getBounds();
        Rectangle b2 = w2.getBounds();

        int width = bounds.width;
        int height = bounds.height;

        int x1 = bounds.x - b1.x;
        int x2 = bounds.x + width - b2.width - b2.x;

        int y1 = bounds.y + (height - b1.height) / 2 - b1.y;
        int y2 = bounds.y + (height - b2.height) / 2 - b2.y;

        w1.resolveBounds(new Point(x1, y1), b1);
        w2.resolveBounds(new Point(x2, y2), b2);
    }
}    
