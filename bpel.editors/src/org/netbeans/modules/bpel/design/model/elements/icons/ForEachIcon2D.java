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


package org.netbeans.modules.bpel.design.model.elements.icons;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import org.netbeans.modules.bpel.design.geometry.Triangle;

/**
 *
 * @author anjeleevich
 */
public class ForEachIcon2D extends Icon2D {
    
    private ForEachIcon2D() {}
    
    
    public void paint(Graphics2D g2) {
        g2.setPaint(COLOR);
        g2.setStroke(STROKE);
        g2.draw(SHAPE_1);
        g2.fill(SHAPE_2);
    }
    

    public static final Icon2D INSTANCE = new ForEachIcon2D();
    
    
    private static final Shape SHAPE_1;
    private static final Shape SHAPE_2;
    
    
    static {
        final double r = 7;
        final double kt = 6;
        final double kn = 4;
        
        final double start = -45 + 90;
        final double delta = 270;
        
        SHAPE_1 = new Arc2D.Double(-r, -r, 2 * r, 2 * r, start, delta, 
                Arc2D.OPEN);
        
        double phi = Math.PI * start / 180;
        
        float x = (float) (r * Math.cos(phi));
        float y = (float) (-r * Math.sin(phi));
         
        float tx = (float) (-y / r);
        float ty = (float) (x / r);
        
        float nx = -ty;
        float ny = tx;
        
        tx *= kt;
        ty *= kt;
        nx *= kn;
        ny *= kn;
        
        SHAPE_2 = new Triangle(x + tx, y + ty, x - nx, y - ny, x + nx, y + ny);
    }     
}
