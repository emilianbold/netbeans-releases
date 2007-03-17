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

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author jsandusky
 */
public class CasaBindingBadges {
    
    public enum Badge {
        IS_EDITABLE(RegionUtilities.IMAGE_EDIT_16_ICON),
        WS_POLICY(RegionUtilities.IMAGE_WS_POLICY_16_ICON);
        private Badge(Image image) {
            mImage = image;
        }
        private Image mImage;
        public Image getImage() {
            return mImage;
        }
    }
    
    private Widget mContainerWidget;
    private Map<Badge, ImageWidget> mBadgeWidgets = new HashMap<Badge, ImageWidget>();
    
    
    public CasaBindingBadges(Scene scene) {
        mContainerWidget = new Widget(scene);
        mContainerWidget.setOpaque(false);
        mContainerWidget.setLayout(
            LayoutFactory.createVerticalLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 1));
        
        Widget emptyWidget = new Widget(scene);
        emptyWidget.setPreferredBounds(new Rectangle(16, 0));
        mContainerWidget.addChild(emptyWidget);
        
        for (Badge badge : Badge.values()) {
            ImageWidget badgeWidget = new ImageWidget(scene);
            mBadgeWidgets.put(badge, badgeWidget);
            mContainerWidget.addChild(badgeWidget);
        }
    }
    
    public void setBadge(Badge badge, boolean isActive) {
        mBadgeWidgets.get(badge).setImage(isActive ? badge.getImage() : null);
    }
    
    public void setBadgePressed(Badge badge, boolean isPressed) {
        mBadgeWidgets.get(badge).setPaintAsDisabled(isPressed);
    }
    
    public Rectangle getBadgeBoundsForParent(Badge badge, Widget parentWidget) {
        Widget widget = mBadgeWidgets.get(badge);
        Point location = widget.getLocation();
        Widget iterParent = widget.getParentWidget();
        while (iterParent != null && iterParent != parentWidget) {
            location.x += iterParent.getLocation().x;
            location.y += iterParent.getLocation().y;
            iterParent = iterParent.getParentWidget();
        }
        return new Rectangle(location, widget.getBounds().getSize());
    }
    
    public Widget getContainerWidget() {
        return mContainerWidget;
    }
}
