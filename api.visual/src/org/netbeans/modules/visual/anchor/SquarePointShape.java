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

import org.netbeans.api.visual.anchor.PointShape;

import java.awt.*;

/**
 * Represents a square point shape.
 * @author David Kaspar
 */
public final class SquarePointShape implements PointShape {

    private int size;
    private boolean filled;

    /**
     * Creates a square shape.
     * @param size   the size
     * @param filled if true, then the shape is filled
     */
    public SquarePointShape (int size, boolean filled) {
        this.size = size;
        this.filled = filled;
    }

    public int getRadius () {
        return (int) Math.ceil (1.5f * size);
    }

    public void paint (Graphics2D graphics) {
        int size2 = size + size;
        Rectangle rect = new Rectangle (- size, - size, size2, size2);
        if (filled)
            graphics.fill (rect);
        else
            graphics.draw (rect);
    }

}
