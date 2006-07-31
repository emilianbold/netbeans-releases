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

    private String label;
    private Alignment alignment = Alignment.LEFT;

    public LabelWidget (Scene scene) {
        this (scene, null);
    }
    
    public LabelWidget (Scene scene, String text) {
        super (scene);
        setOpaque (false);
//        setCursor (new Cursor (Cursor.TEXT_CURSOR));
        setLabel (text);
    }

    public String getLabel () {
        return label;
    }

    public void setLabel (String label) {
        if (GeomUtil.equals (this.label, label))
            return;
        this.label = label;
        revalidate ();
    }

    public Alignment getAlignment () {
        return alignment;
    }

    public void setAlignment (Alignment alignment) {
        this.alignment = alignment;
        repaint ();
    }

    protected Rectangle calculateClientArea () {
        if (label == null)
            return super.calculateClientArea ();
        Graphics2D gr = getGraphics ();
        FontMetrics fontMetrics = gr.getFontMetrics (getFont ());
        Rectangle2D stringBounds = fontMetrics.getStringBounds (label, gr);
        return GeomUtil.roundRectangle (stringBounds);
    }

    protected void paintWidget () {
        if (label == null)
            return;
        Graphics2D gr = getGraphics ();
        gr.setColor (getForeground ());
        gr.setFont (getFont ());

        int x = 0;
        if (alignment == Alignment.RIGHT) {
            int textWidth = gr.getFontMetrics ().stringWidth (label);
            x = getBounds ().width - textWidth;
        } else if (alignment == Alignment.CENTER) {
            int halfWidth = gr.getFontMetrics ().stringWidth (label) / 2;
            x = getBounds ().width / 2 - halfWidth;
        }

        gr.drawString (label, x, 0);
    }

}
