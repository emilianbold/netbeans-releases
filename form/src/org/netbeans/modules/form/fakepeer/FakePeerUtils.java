/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */
 
/* $Id$ */

package org.netbeans.modules.form.fakepeer;

import java.awt.*;

/**
 *
 * @author Tomas Pavek
 */

class FakePeerUtils
{
    public static void drawButton(Graphics g,int x,int y,int w,int h) {
        g.fillRect(x,y,w,h);

        // button has a raised border (Windows)
        g.setColor(SystemColor.controlHighlight);
        g.drawLine(x,y+h-2,x,y);
        g.drawLine(x,y,x+w-2,y);
        g.setColor(SystemColor.controlDkShadow);
        g.drawLine(x,y+h-1,x+w-1,y+h-1);
        g.drawLine(x+w-1,y+h-1,x+w-1,y);
        if (w >=4 && h >= 4) {
            g.setColor(SystemColor.controlLtHighlight);
            g.drawLine(x+1,y+h-3,x+1,y+1);
            g.drawLine(x+1,y+1,x+w-3,y+1);
            g.setColor(SystemColor.controlShadow);
            g.drawLine(x+1,y+h-2,x+w-2,y+h-2);
            g.drawLine(x+w-2,y+h-2,x+w-2,y+1);
        }
    }

    public static void drawArrowButton(Graphics g,int x,int y,int w,int h,int type) {
        g.setColor(SystemColor.control);
        drawButton(g,x,y,w,h);

        int minWH = w < h ? w : h,
            size; // size of the arrow - from 0 to 4
        if (minWH >= ABUT_SIZE) size = 4;
        else if (minWH >= 12) size = 3;
        else if (minWH >= 8) size = 2;
        else if (minWH >= 6) size = 1;
        else size = 0;

        g.setColor(SystemColor.controlText);

        // draw the arrow
        if (type == 1) { // left <
            int ax = x+w/2-size/2,
                ay = y+h/2-1;
            for (int i=0; i < size; i++)
                g.drawLine(ax+i,ay-i,ax+i,ay+i);
        } else if (type == 2) { // right >
            int ax = x+w/2+size/2,
                ay = y+h/2-1;
            for (int i=0; i < size; i++)
                g.drawLine(ax-i,ay-i,ax-i,ay+i);
        } else if (type == 3) { // upper ^
            int ax = x+w/2-1,
                ay = y+h/2-size/2;
            for (int i=0; i < size; i++)
                g.drawLine(ax-i,ay+i,ax+i,ay+i);
        } else if (type == 4) { // lower v
            int ax = x+w/2-1,
                ay = y+h/2+size/2;
            for (int i=0; i < size; i++)
                g.drawLine(ax-i,ay-i,ax+i,ay-i);
        }
    }

    public static void drawChoiceButton(Graphics g,int x,int y,int w,int h) {
        // Windows-like style - a button with an arrow
        drawArrowButton(g,x,y,w,h,4);
    }

    public static void drawScrollThumb(Graphics g,int x,int y,int w,int h) {
        // Windows-like style - thumb looks just like a button
        drawButton(g,x,y,w,h);
    }

    public static void drawLoweredBox(Graphics g,int x,int y,int w,int h) {
        g.fillRect(x,y,w,h);

        g.setColor(SystemColor.controlShadow);
        g.drawLine(x,y+h-2,x,y);
        g.drawLine(x,y,x+w-2,y);
        g.setColor(SystemColor.controlLtHighlight);
        g.drawLine(x,y+h-1,x+w-1,y+h-1);
        g.drawLine(x+w-1,y+h-1,x+w-1,y);
        if (w >=4 && h >= 4) {
            g.setColor(SystemColor.controlDkShadow);
            g.drawLine(x+1,y+h-3,x+1,y+1);
            g.drawLine(x+1,y+1,x+w-3,y+1);
            g.setColor(SystemColor.controlHighlight);
            g.drawLine(x+1,y+h-2,x+w-2,y+h-2);
            g.drawLine(x+w-2,y+h-2,x+w-2,y+1);
        }
    }

    private static int ABUT_SIZE = 16; // standard arrow button's width & height
}
