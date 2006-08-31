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

import org.netbeans.api.visual.border.BorderFactory;

import java.awt.image.ConvolveOp;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class ConvolveWidget extends Widget {

    private static final Color TRANSPARENT = new Color (0, 0, 0, 0);

    private ConvolveOp convolveOp;
    private BufferedImage image;
    private Graphics2D imageGraphics;

    public ConvolveWidget (Scene scene, ConvolveOp convolveOp) {
        super (scene);
        this.convolveOp = convolveOp;
        Kernel kernel = convolveOp.getKernel ();
        setBorder (BorderFactory.createEmptyBorder (kernel.getWidth (), kernel.getHeight ()));
    }

    public void clearCache () {
        if (imageGraphics != null)
            imageGraphics.dispose ();
        image = null;

    }

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
