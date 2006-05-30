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
package org.netbeans.api.visual.widget;

import org.netbeans.api.visual.util.GeomUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * A widget representing a text.
 *
 * @author David Kaspar
 */
public class LabelWidget extends Widget {

    public enum Alignment {
        LEFT, RIGHT, CENTER
    }

    private String text;
    private Alignment aligment = Alignment.LEFT;

    public LabelWidget (Scene scene) {
        this (scene, null);
    }
    
    public LabelWidget (Scene scene, String text) {
        super (scene);
        setOpaque (false);
        setCursor (new Cursor (Cursor.TEXT_CURSOR));
        setText (text);
    }

    public String getText () {
        return text;
    }

    public void setText (String text) {
        this.text = text;
        revalidate ();
    }

    public Alignment getAligment () {
        return aligment;
    }

    public void setAligment (Alignment aligment) {
        this.aligment = aligment;
        revalidate ();
    }

    protected Rectangle calculateClientArea () {
        if (text == null)
            return super.calculateClientArea ();
        Graphics2D gr = getGraphics ();
        FontMetrics fontMetrics = gr.getFontMetrics (getFont ());
        Rectangle2D stringBounds = fontMetrics.getStringBounds (text, gr);
        return GeomUtil.roundRectangle (stringBounds);
    }

    protected void paintWidget () {
        if (text == null)
            return;
        Graphics2D gr = getGraphics ();
        gr.setColor (getForeground ());
        gr.setFont (getFont ());

        int x = 0;
        if (aligment == Alignment.RIGHT) {
            int textWidth = gr.getFontMetrics ().stringWidth (text);
            x = getBounds ().width - textWidth;
        } else if (aligment == Alignment.CENTER) {
            int halfWidth = gr.getFontMetrics ().stringWidth (text) / 2;
            x = getBounds ().width / 2 - halfWidth;
        }

        gr.drawString (text, x, 0);
    }

}
