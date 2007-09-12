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


package org.netbeans.modules.bpel.design.geometry;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;


public final class FStroke {

    public final double width;
    public final double dash;
    public final double space;
    
    public final boolean isSolid;

    
    public FStroke(double width) {
        this(width, 0.0, 0.0);
    }
    
    
    public FStroke(double width, double dash) {
        this(width, dash, 0.0);
    }

    
    public FStroke(double width, double dash, double space) {
        this.width = (width < 0.0) ? -width : width;
        
        if (dash <= 0.0) {
            this.isSolid = true;
            this.dash = 0.0;
            this.space = 0.0;
        } else {
            this.isSolid = false;
            this.dash = dash;
            this.space = (space <= 0.0) ? (dash + width) : space;
        }
    }


    public final Stroke createSolidStroke(Graphics2D g2) {
        return createSolidStroke(g2.getTransform());
    }
    
    
    public final Stroke createStroke(Graphics2D g2) {
        return createStroke(g2.getTransform());
    }
    
    
    public final Stroke createSolidStroke(AffineTransform at) {
        double scale;
        
        if (at == null) { 
            scale = 1.0;
        } else {
            double a = at.getScaleX();
            double b = at.getShearY();
            scale = Math.sqrt(a * a + b * b);
            if (scale <= 0.0) scale = 1.0;
        }

        double k = scale * width;
        
        if (k < 1.0) {
            return new BasicStroke((float) (width / k), 
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        }
        
        return new BasicStroke((float) width, 
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    }
    
    
    public final Stroke createStroke(AffineTransform at) {
        double scale;
        
        if (at == null) { 
            scale = 1.0;
        } else {
            double a = at.getScaleX();
            double b = at.getShearY();
            scale = Math.sqrt(a * a + b * b);
            if (scale <= 0.0) scale = 1.0;
        }

        double k = scale * width;
        
        if (k < 1.0) {
            return (isSolid) 
                    ? new BasicStroke((float) (width / k), 
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)
                    : new BasicStroke((float) (width / k), 
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, 
                        new float[] { (float) (dash / k), (float) (space / k) }, 
                        0);
        }
        
        return (isSolid) 
                ? new BasicStroke((float) width, 
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)
                : new BasicStroke((float) width, 
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, 
                    new float[] { (float) dash, (float) space }, 0);
    }
}
