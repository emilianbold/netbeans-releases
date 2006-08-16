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
package org.netbeans.modules.visual.border;

import java.awt.*;

/**
 *
 * @author alex_grk
 */
public final class FancyDashedBorder extends DashedBorder {

    private static int focusField=5;
    private static int rectSize=5;
    private static int halfRectSize=rectSize/2;
    private static int rect15Size=rectSize+halfRectSize;

    public FancyDashedBorder(Color color, int width, int height) {
        super (color, width, height);
    }
    
    public void paint(Graphics2D g, Rectangle bounds) {
        int x=bounds.x,y=bounds.y,width=bounds.width,height=bounds.height;
        //x=x+halfRectSize;y=y+halfRectSize;width=width-rectSize;height=height-rectSize;
        bounds.x=bounds.x+focusField/2;bounds.y=bounds.y+focusField/2;bounds.width=bounds.width-focusField;bounds.height=bounds.height-focusField;
        super.paint(g,bounds);
        g.drawRect(x,y,rectSize,rectSize);
        g.drawRect(x+width/2-halfRectSize,y,rectSize,rectSize);
        g.drawRect(x+width-rect15Size,y,rectSize,rectSize);
        g.drawRect(x+width-rect15Size,y-halfRectSize+height/2,rectSize,rectSize);
        g.drawRect(x+width-rect15Size,y-rect15Size+height,rectSize,rectSize);
        g.drawRect(x+width/2-halfRectSize,y-rect15Size+height,rectSize,rectSize);
        g.drawRect(x,y-rect15Size+height,rectSize,rectSize);
        g.drawRect(x,y-rectSize+height/2,rectSize,rectSize);
    }
    
}
