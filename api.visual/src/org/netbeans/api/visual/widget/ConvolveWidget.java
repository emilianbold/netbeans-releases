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

import org.netbeans.api.visual.border.BorderFactory;

import java.awt.image.ConvolveOp;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.awt.*;

/**
 * The widget which applies a convolve filter to a graphics rendered by the children.
 * <p>
 * Children are painted to an offscreen buffer which is later painted with a convolve filter applied to it.
 * <p>
 * Because of the offscreen buffer, be careful about the size of the widget. The buffer stays allocated
 * even after the painting and it is also expanding only (when required). You can clear the buffer using clearCache method.
 *
 * @author David Kaspar
 */
public class ConvolveWidget extends Widget {

    private static final Color TRANSPARENT = new Color (0, 0, 0, 0);

    private ConvolveOp convolveOp;
    private BufferedImage image;
    private Graphics2D imageGraphics;

    /**
     * Creates a convolve widget with a specified ColvolveOp.
     * @param scene the scene
     * @param convolveOp the convolve operation
     */
    public ConvolveWidget (Scene scene, ConvolveOp convolveOp) {
        super (scene);
        this.convolveOp = convolveOp;
        Kernel kernel = convolveOp.getKernel ();
        setBorder (BorderFactory.createEmptyBorder (kernel.getWidth (), kernel.getHeight ()));
    }

    /**
     * Returns a convolve operation.
     * @return the convolve operation
     */
    public ConvolveOp getConvolveOp () {
        return convolveOp;
    }

    /**
     * Sets a convolve operation.
     * @param convolveOp the convolve operation
     */
    public void setConvolveOp (ConvolveOp convolveOp) {
        this.convolveOp = convolveOp;
        repaint ();
    }

    /**
     * Clears an offscreen buffer.
     */
    public void clearCache () {
        if (imageGraphics != null)
            imageGraphics.dispose ();
        image = null;

    }

    /**
     * Paints the children into the offscreen buffer and then the buffer is rendered regularly using the convolve operation.
     */
    protected void paintChildren () {
        Rectangle bounds = getBounds ();
        if (image == null  ||  image.getWidth () < bounds.width  ||  image.getHeight () < bounds.height) {
            clearCache ();
            image = new BufferedImage (bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
            imageGraphics = image.createGraphics ();
        }

        Graphics2D previousGraphics = getScene ().getGraphics ();
        imageGraphics.translate (- bounds.x, - bounds.y);
        imageGraphics.setBackground (TRANSPARENT);
        imageGraphics.clearRect (bounds.x, bounds.y, bounds.width, bounds.height);

        getScene ().setGraphics (imageGraphics);
        super.paintChildren ();
        getScene ().setGraphics (previousGraphics);

        imageGraphics.translate (bounds.x, bounds.y);

        getGraphics ().drawImage (image, convolveOp, bounds.x, bounds.y);
    }

}
