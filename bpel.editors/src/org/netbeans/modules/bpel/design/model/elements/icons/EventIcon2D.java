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
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

/**
 *
 * @author anjeleevich
 */
public class EventIcon2D extends Icon2D {
    
    
    private EventIcon2D() {}

    
    public void paint(Graphics2D g2) {
        g2.setPaint(COLOR);
        g2.setStroke(STROKE);
        g2.draw(SHAPE_1);
        g2.draw(SHAPE_2);
        g2.fill(SHAPE_3);
    }
    
    
    public static final Icon2D INSTANCE = new EventIcon2D();
    
    
    private static final Shape SHAPE_1 = new Ellipse2D.Float(-12, -12, 24, 24);
    private static final Shape SHAPE_2 = new Ellipse2D.Float(-10, -10, 20, 20);
    
    private static final Shape SHAPE_3;
    
    static {
        float r1 = 8.5f;
        float r2 = 3.5f;
        
        float cos30 = (float) Math.cos(Math.toRadians(30));
        float sin30 = (float) Math.sin(Math.toRadians(30));
        
        GeneralPath gp;
        
        gp = new GeneralPath();
        gp.moveTo(0, -r1);
        gp.lineTo(r2 * cos30, -r2 * sin30);
        gp.lineTo(r1 * cos30, r1 * sin30);
        gp.lineTo(0, +r2);
        gp.lineTo(-r1 * cos30, r1 * sin30);
        gp.lineTo(-r2 * cos30, -r2 * sin30);
        gp.closePath();

        Area area = new Area(gp);
        
        gp.reset();
        gp.moveTo(0, r1);
        gp.lineTo(-r2 * cos30, r2 * sin30);
        gp.lineTo(-r1 * cos30, -r1 * sin30);
        gp.lineTo(0, -r2);
        gp.lineTo(r1 * cos30, -r1 * sin30);
        gp.lineTo(r2 * cos30, r2 * sin30);
        gp.closePath();
        
        area.add(new Area(gp));
        
        SHAPE_3 = new GeneralPath(area);
    }
}
