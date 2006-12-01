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

import org.netbeans.api.visual.action.ResizeControlPointResolver;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.util.GeomUtil;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class ResizeCornersControlPointResolver implements ResizeControlPointResolver {

    public ResizeProvider.ControlPoint resolveControlPoint (Widget widget, Point point) {
        Rectangle bounds = widget.getBounds ();
        Insets insets = widget.getBorder ().getInsets ();
        Point center = GeomUtil.center (bounds);
        Dimension centerDimension = new Dimension (Math.max (insets.left, insets.right), Math.max (insets.top, insets.bottom));
        if (point.y >= bounds.y + bounds.height - insets.bottom && point.y < bounds.y + bounds.height) {

            if (point.x >= bounds.x + bounds.width - insets.right && point.x < bounds.x + bounds.width)
                return ResizeProvider.ControlPoint.BOTTOM_RIGHT;
            else if (point.x >= bounds.x && point.x < bounds.x + insets.left)
                return ResizeProvider.ControlPoint.BOTTOM_LEFT;
            else
            if (point.x >= center.x - centerDimension.height / 2 && point.x < center.x + centerDimension.height - centerDimension.height / 2)
                return ResizeProvider.ControlPoint.BOTTOM_CENTER;

        } else if (point.y >= bounds.y && point.y < bounds.y + insets.top) {

            if (point.x >= bounds.x + bounds.width - insets.right && point.x < bounds.x + bounds.width)
                return ResizeProvider.ControlPoint.TOP_RIGHT;
            else if (point.x >= bounds.x && point.x < bounds.x + insets.left)
                return ResizeProvider.ControlPoint.TOP_LEFT;
            else
            if (point.x >= center.x - centerDimension.height / 2 && point.x < center.x + centerDimension.height - centerDimension.height / 2)
                return ResizeProvider.ControlPoint.TOP_CENTER;

        } else
        if (point.y >= center.y - centerDimension.width / 2 && point.y < center.y + centerDimension.width - centerDimension.width / 2)
        {

            if (point.x >= bounds.x + bounds.width - insets.right && point.x < bounds.x + bounds.width)
                return ResizeProvider.ControlPoint.CENTER_RIGHT;
            else if (point.x >= bounds.x && point.x < bounds.x + insets.left)
                return ResizeProvider.ControlPoint.CENTER_LEFT;

        }
        // TODO - resolve CENTER points
        return null;
    }

}
