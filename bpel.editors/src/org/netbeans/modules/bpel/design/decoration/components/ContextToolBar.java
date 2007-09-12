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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;
import javax.swing.border.Border;


public class ContextToolBar extends JPanel implements
        DecorationComponent {
    
    public ContextToolBar() {
        super(new GridLayout(1, 0, 2, 0));
        setBorder(new MyBorder());
        setBackground(null);
        setOpaque(false);
        
       super.toString();
    }
    private class MyBorder implements Border {
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
            
            float arcSize = Math.max(0, ARC_SIZE - STROKE_WIDTH);
            float strokeHalf = STROKE_WIDTH / 2;
            
            Shape shape = new RoundRectangle2D.Float(x + strokeHalf, y + strokeHalf,
                    w - STROKE_WIDTH, h - STROKE_WIDTH, arcSize, arcSize);
            
            if (FILL_PAINT != null) {
                g2.setPaint(FILL_PAINT);
                g2.fill(shape);
            }
            
            if (STROKE_PAINT != null) {
                g2.setPaint(STROKE_PAINT);
                g2.draw(shape);
            }
            
            g2.dispose();
        }
        
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 3, 2, 3);
        }
        
        public boolean isBorderOpaque() {
            return false;
        }
        
    }
    
    

    private static final float ARC_SIZE = 8;
    private static final float STROKE_WIDTH = 1;
    private static final Paint STROKE_PAINT = new Color(0xCCCCCC);
    private static final Paint FILL_PAINT = new Color(0xCCFFFFFF, true);
}
