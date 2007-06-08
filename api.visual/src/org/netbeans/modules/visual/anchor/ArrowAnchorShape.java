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
import java.awt.geom.GeneralPath;

/**
 * @author Antonio
 */
public class ArrowAnchorShape implements AnchorShape {

    private static final Stroke STROKE = new BasicStroke (1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private GeneralPath path;
    private int size;

    public ArrowAnchorShape (int degrees, int size) {
        this.size = size;
        path = new GeneralPath ();

        double radians = Math.PI * degrees / 180.0;
        double cos = Math.cos (radians / 2.0);
        double sin = -size * Math.sqrt (1 - cos * cos);
        cos *= size;

        path.moveTo (0.0f, 0.0f);
        path.lineTo ((float) cos, (float) -sin);
        path.moveTo (0.0f, 0.0f);
        path.lineTo ((float) cos, (float) sin);
    }

    public boolean isLineOriented () {
        return true;
    }

    public int getRadius () {
        return size + 1;
    }

    public double getCutDistance () {
        return 0;
    }

    public void paint (Graphics2D graphics, boolean source) {
        Stroke previousStroke = graphics.getStroke ();
        graphics.setStroke (STROKE);
        graphics.draw (path);
        graphics.setStroke (previousStroke);
    }

}
