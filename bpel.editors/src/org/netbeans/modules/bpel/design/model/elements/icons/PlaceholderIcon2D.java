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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import org.netbeans.modules.bpel.design.geometry.FStroke;

/**
 *
 * @author anjeleevich
 */
public class PlaceholderIcon2D extends Icon2D {
    

    private PlaceholderIcon2D() {}
    
    
    public void paint(Graphics2D g2) {
        g2.setPaint(COLOR_1_1);
        g2.setStroke(STROKE_1.createStroke(g2));
        g2.draw(SHAPE_1);
        
        g2.setStroke(STROKE_2.createStroke(g2));
        g2.draw(SHAPE_2);
    }
    
    
    public static final Icon2D INSTANCE = new PlaceholderIcon2D();
    
    
    private static final Shape SHAPE_1 = new Ellipse2D.Float(-10, -10, 20, 20);
    private static final Shape SHAPE_2 = new Ellipse2D.Float(-5, -5, 10, 10);

    private static final FStroke STROKE_1 = new FStroke(1, 3);
    private static final FStroke STROKE_2 = new FStroke(1);
    
    private static final Paint COLOR_1_1 = new Color(0xDCDCDC);
}
