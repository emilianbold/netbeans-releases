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
import java.awt.geom.GeneralPath;

/**
 *
 * @author anjeleevich
 */
public class FaultBadgeIcon2D extends Icon2D {
    
    private FaultBadgeIcon2D() {}
    
    
    public void paint(Graphics2D g2) {
        g2.setPaint(COLOR);
        g2.fill(SHAPE);
    }
    
    
    public static final Icon2D INSTANCE = new FaultBadgeIcon2D();
    
    
    private static final Shape SHAPE;
    
    
    static {
        float w = 8f / 2;
        float h = 10f / 2;
        
        float x1 = -w;
        float x2 = -w * 0.67f;
        float x3 = -w * 0.33f;
        float x4 = w * 0.33f;
        float x5 = w * 0.67f;
        float x6 = w;
        
        float y1 = -h;
        float y2 = -h * 0.75f;
        float y3 = 0;
        float y4 = h * 0.75f;
        float y5 = h;
    
        GeneralPath gp = new GeneralPath();

        gp.moveTo(x1, y5);
        gp.lineTo(x2, y2);
        gp.lineTo(x4, y3);
        gp.lineTo(x6, y1);
        gp.lineTo(x5, y4);
        gp.lineTo(x3, y3);
        gp.closePath();
        
        SHAPE = gp;
    }    
}
