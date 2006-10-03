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
package org.netbeans.api.visual.anchor;

import org.netbeans.modules.visual.anchor.ImagePointShape;

import java.awt.*;

/**
 * The factory class of all built-in point shapes.
 * The instances of all built-in point shapes can be used multiple connection widgets.
 *
 * @author David Kaspar
 */
public class PointShapeFactory {

    private PointShapeFactory () {
    }

    /**
     * Creates an image point shape.
     * @param image the image
     * @return the point shape
     */
    public static PointShape createImagePointShape (Image image) {
        return new ImagePointShape (image);
    }

}
