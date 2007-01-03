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
package org.netbeans.api.visual.widget;

import org.netbeans.modules.visual.util.GeomUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * A widget representing a text. The widget is not opaque and is checking clipping for by default.
 *
 * It allows to set 4 types of horizontal and vertical alignments (by default LEFT as horizontal and BASELINE as vertical).
 *
 * @author David Kaspar
 */
public class LabelWidget extends Widget {

    /**
     * The text alignment
     */
    public enum Alignment {
        LEFT, RIGHT, CENTER, BASELINE
    }

    /**
     * The text vertical alignment
     */
    public enum VerticalAlignment {
        TOP, BOTTOM, CENTER, BASELINE
    }

    private String label;
    private Alignment alignment = Alignment.LEFT;
    private VerticalAlignment verticalAlignment = VerticalAlignment.BASELINE;
    private boolean paintAsDisabled;

    /**
     * Creates a label widget.
     * @param scene the scene
     */
    public LabelWidget (Scene scene) {
        this (scene, null);
    }

    /**
     * Creates a label widget with a label.
     * @param scene the scene
     * @param label the label
     */
    public LabelWidget (Scene scene, String label) {
        super (scene);
        setOpaque (false);
//        setCursor (new Cursor (Cursor.TEXT_CURSOR));
        setLabel (label);
        setCheckClipping (true);
    }

    /**
     * Returns a label.
     * @return the label
     */
    public String getLabel () {
        return label;
    }

    /**
     * Sets a label.
     * @param label the label
     */
    public void setLabel (String label) {
        if (GeomUtil.equals (this.label, label))
            return;
        this.label = label;
        revalidate ();
    }

    /**
     * Returns a text horizontal alignment.
     * @return the text horizontal alignment
     */
    public Alignment getAlignment () {
        return alignment;
    }

    /**
     * Sets a text horizontal alignment.
     * @param alignment the text horizontal alignment
     */
    public void setAlignment (Alignment alignment) {
        this.alignment = alignment;
        repaint ();
    }

    /**
     * Gets a text vertical alignment.
     * @return the text vertical alignment
     */
    public VerticalAlignment getVerticalAlignment () {
        return verticalAlignment;
    }

    /**
     * Sets a text vertical alignment.
     * @param verticalAlignment the text vertical alignment
     */
    public void setVerticalAlignment (VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        repaint ();
    }

    /**
     * Returns whether the label is painted as disabled.
     * @return true, if the label is painted as disabled
     */
    public boolean isPaintAsDisabled () {
        return paintAsDisabled;
    }

    /**
     * Sets whether the label is painted as disabled.
     * @param paintAsDisabled if true, then the label is painted as disabled
     */
    public void setPaintAsDisabled (boolean paintAsDisabled) {
        boolean repaint = this.paintAsDisabled != paintAsDisabled;
        this.paintAsDisabled = paintAsDisabled;
        if (repaint)
            repaint ();
    }

    /**
     * Calculates a client area for the label.
     * @return the client area
     */
    protected Rectangle calculateClientArea () {
        if (label == null)
            return super.calculateClientArea ();
        Graphics2D gr = getGraphics ();
        FontMetrics fontMetrics = gr.getFontMetrics (getFont ());
        Rectangle2D stringBounds = fontMetrics.getStringBounds (label, gr);
        return GeomUtil.roundRectangle (stringBounds);
    }

    /**
     * Paints the label widget.
     */
    protected void paintWidget () {
        if (label == null)
            return;
        Graphics2D gr = getGraphics ();
        gr.setFont (getFont ());

        FontMetrics fontMetrics = gr.getFontMetrics ();
        Rectangle clientArea = getClientArea ();

        int x;
        switch (alignment) {
            case BASELINE:
                x = 0;
                break;
            case LEFT:
                x = clientArea.x;
                break;
            case CENTER:
                x = clientArea.x + (clientArea.width - fontMetrics.stringWidth (label)) / 2;
                break;
            case RIGHT:
                x = clientArea.x + clientArea.width - fontMetrics.stringWidth (label);
                break;
            default:
                return;
        }

        int y;
        switch (verticalAlignment) {
            case BASELINE:
                y = 0;
                break;
            case TOP:
                y = clientArea.y + fontMetrics.getAscent ();
                break;
            case CENTER:
                y = clientArea.y + (clientArea.height + fontMetrics.getAscent () - fontMetrics.getDescent ()) / 2;
                break;
            case BOTTOM:
                y = clientArea.y + fontMetrics.getAscent () + clientArea.height - fontMetrics.getDescent ();
                break;
            default:
                return;
        }

        Paint background = getBackground ();
        if (paintAsDisabled  &&  background instanceof Color) {
            Color color = ((Color) background);
            gr.setColor (color.brighter ());
            gr.drawString (label, x + 1, y + 1);
            gr.setColor (color.brighter ());
            gr.drawString (label, x, y);
        } else {
            gr.setColor (getForeground ());
            gr.drawString (label, x, y);
        }
    }

}
