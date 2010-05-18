/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.uml.diagrams.anchors;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import org.netbeans.api.visual.anchor.AnchorShape;
import java.awt.geom.GeneralPath;

/**
 * @author sp153251
 */
public class ArrowWithCrossedCircleAnchorShape implements AnchorShape
{

    public static final Stroke STROKE = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private int arrowSize;
    private int circleSize;
    private double cutDistance;
    private GeneralPath generalPath;

    /**
     * Creates a triangular anchor shape.
     * @param arrowSize the size of triangle
     * @param circleSize diameter of circle
     * @param cutDistance the cut distance
     */
    public ArrowWithCrossedCircleAnchorShape(int arrowSize, int circleSize, double cutDistance)
    {
        this.arrowSize = arrowSize;
        this.cutDistance = cutDistance;
        this.circleSize=circleSize;
        float side = arrowSize;
        
        generalPath = new GeneralPath();
        generalPath.moveTo (side, -side/2);
        generalPath.lineTo (0, 0);
        generalPath.lineTo (side, side/2);
    }

    public boolean isLineOriented()
    {
        return true;
    }

    public int getRadius()
    {
        return (int) Math.ceil (1.5f * arrowSize);
    }

    public double getCutDistance()
    {
        return cutDistance;
    }

    public void paint(Graphics2D graphics, boolean source)
    {
        Stroke stroke = graphics.getStroke();
        Color color=graphics.getColor();
        graphics.setStroke(STROKE);
        graphics.draw(generalPath);
        graphics.setColor(Color.RED);
        graphics.setStroke(new BasicStroke(3.0f,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.drawOval(-circleSize/2, -circleSize/2, circleSize, circleSize);
        graphics.drawLine(circleSize/2, circleSize/2, -circleSize/2, -circleSize/2);
        graphics.setStroke(stroke);
        graphics.setColor(color);
    }
}
