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

import java.awt.geom.GeneralPath;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class TriangleAnchorShape implements AnchorShape {

    public static final Stroke STROKE = new BasicStroke (1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private int size;
    private boolean filled;
    private boolean hollow;
    private double cutDistance;

    private GeneralPath generalPath;

    /**
     * Creates a triangular anchor shape.
     * @param size the size of triangle
     * @param filled if true, then the triangle is filled
     * @param output if true, then it is output triangle
     * @param cutDistance the cut distance
     */
    public TriangleAnchorShape (int size, boolean filled, boolean output, boolean hollow, double cutDistance) {
        this.size = size;
        this.filled = filled;
        this.hollow = hollow;
        this.cutDistance = cutDistance;

        float side = size * 0.3f;
        generalPath = new GeneralPath ();
        if (output) {
            generalPath.moveTo (size, 0.0f);
            generalPath.lineTo (0.0f, -side);
            generalPath.lineTo (0.0f, +side);
            if (hollow)
                generalPath.lineTo (size, 0.0f);
        } else {
            generalPath.moveTo (0.0f, 0.0f);
            generalPath.lineTo (size, -side);
            generalPath.lineTo (size, +side);
            if (hollow)
                generalPath.lineTo (0.0f, 0.0f);
        }
    }

    public boolean isLineOriented () {
        return true;
    }

    public int getRadius () {
        return (int) Math.ceil (1.5f * size);
    }

    public double getCutDistance () {
        return cutDistance;
    }

    public void paint (Graphics2D graphics, boolean source) {
        if (filled)
            graphics.fill (generalPath);
        else {
            Stroke stroke = graphics.getStroke ();
            graphics.setStroke (STROKE);
            graphics.draw (generalPath);
            graphics.setStroke (stroke);
        }
    }

}
