/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.bpel.design.decoration.components;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 *
 * @author aa160298
 */
public abstract class Icon2D {


    protected double getDesignOriginX() { return 0; }
    protected double getDesignOriginY() { return 0; }

    protected abstract double getDesignWidth();
    protected abstract double getDesignHeight();


    public final void paint(Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g;
        
        Object oldAntialiasing = g2.getRenderingHint(
                RenderingHints.KEY_ANTIALIASING);
        Object oldStrokeControl = g2.getRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL);

        Stroke oldStroke = g2.getStroke();
        Paint oldPaint = g2.getPaint();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);

        double desWidth = getDesignWidth();
        double desHeight = getDesignHeight();
        
        double cx = x + 0.5 * w;
        double cy = y + 0.5 * h;
        
        double scale = Math.min((double) w / desWidth, (double) h / desHeight);
        double backScale = 1.0 / scale;
        
        double tx = -desWidth / 2 + getDesignOriginX();
        double ty = -desHeight / 2 + getDesignOriginY();
        
        g2.translate(cx, cy);
        g2.scale(scale, scale);
        g2.translate(tx, ty);
        
        paint(g2);

        g2.translate(-tx, -ty);
        g2.scale(backScale, backScale);
        g2.translate(-cx, -cy);
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                oldAntialiasing);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                oldStrokeControl);
        g2.setStroke(oldStroke);
        g2.setPaint(oldPaint);
    }
    
    
    protected abstract void paint(Graphics2D g2);
}
