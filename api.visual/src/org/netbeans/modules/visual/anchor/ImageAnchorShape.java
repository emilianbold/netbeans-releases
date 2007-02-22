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
package org.netbeans.modules.visual.anchor;

import org.netbeans.api.visual.anchor.AnchorShape;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class ImageAnchorShape implements AnchorShape {

    private Image image;
    private boolean lineOriented;
    private int radius;
    private int x, y;

    public ImageAnchorShape (Image image, boolean lineOriented) {
        this.lineOriented = lineOriented;
        assert image != null;
        this.image = image;
        x = image.getWidth (null);
        y = image.getHeight (null);
        radius = Math.max (x, y);
        x = - (x / 2);
        y = - (y / 2);
    }

    public boolean isLineOriented () {
        return lineOriented;
    }

    public int getRadius () {
        return radius;
    }

    public double getCutDistance () {
        return 0.0;
    }

    public void paint (Graphics2D graphics, boolean source) {
        graphics.drawImage (image, x, y, null);
    }

}
