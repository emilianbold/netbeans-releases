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

import java.awt.*;

/**
 * @author David Kaspar
 */
// TODO - alignment
public class ImageWidget extends Widget {

    private Image image;
    private int width, height;

    public ImageWidget (Scene scene) {
        super (scene);
    }
    
    public ImageWidget (Scene scene, Image image) {
        super (scene);
        setImage (image);
    }

    public Image getImage () {
        return image;
    }

    public void setImage (Image image) {
        if (this.image == image)
            return;
        int oldWidth = width;
        int oldHeight = height;

        this.image = image;
        width = image != null ? image.getWidth (null) : 0;
        height = image != null ? image.getHeight (null) : 0;

        if (oldWidth == width  &&  oldHeight == height)
            repaint ();
        else
            revalidate ();
    }

    protected Rectangle calculateClientArea () {
        if (image != null)
            return new Rectangle (0, 0, width, height);
        return super.calculateClientArea ();
    }

    protected void paintWidget () {
        if (image == null)
            return;
        Graphics2D gr = getGraphics ();
        if (image != null)
            gr.drawImage (image, 0, 0, null);
    }

}
