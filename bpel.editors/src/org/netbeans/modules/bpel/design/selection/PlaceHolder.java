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


package org.netbeans.modules.bpel.design.selection;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import org.netbeans.modules.bpel.design.geometry.FEllipse;
import org.netbeans.modules.bpel.design.geometry.FPath;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.geometry.FShape;
import org.netbeans.modules.bpel.design.geometry.FStroke;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;


public abstract class PlaceHolder {
    
    private Pattern ownerPattern;
    private Pattern draggedPattern;
    private boolean mouseHover;
    private FPath path;
    private FShape shape;
    
    public PlaceHolder(Pattern ownerPattern, Pattern draggedPattern,
            double cx, double cy) {
        this(ownerPattern, draggedPattern, cx, cy, null);
    }
    

    public PlaceHolder(Pattern ownerPattern, Pattern draggedPattern,
            FPoint center) {
        this(ownerPattern, draggedPattern, center.x, center.y, null);
    }
    
    
    public PlaceHolder(Pattern ownerPattern, Pattern draggedPattern, FPath path) {
        this(ownerPattern, draggedPattern, path.point(0.5f), path);
    }
    
    
    public PlaceHolder(Pattern ownerPattern, Pattern draggedPattern,
            FPoint center, FPath path) {
        this(ownerPattern, draggedPattern, center.x, center.y, path);
    }
    
    
    public PlaceHolder(Pattern ownerPattern, Pattern draggedPattern,
            double cx, double cy, FPath path) {
        this.ownerPattern = ownerPattern;
        this.draggedPattern = draggedPattern;
        this.shape = SHAPE.moveCenter(cx, cy);
        this.path = path;
    }
    
    
    public boolean contains(double px, double py) {
        return shape.contains(px, py);
    }
    
    
    public Pattern getDraggedPattern() {
        return draggedPattern;
    }
    
    
    public Pattern getOwnerPattern() {
        return ownerPattern;
    }
    
    
    public boolean isMouseHover() {
        return mouseHover;
    }
    
    
    public void dragEnter() {
        mouseHover = true;
    }
    
    public void dragExit() {
        mouseHover = false;
    }
    

    public abstract void drop();
    
    
    public void paint(Graphics2D g2) {
        g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        g2.setPaint(OUTER_FILL);
        g2.fill(shape);
        
        g2.setStroke(OUTER_STROKE.createStroke(g2));
        g2.setPaint(OUTER_STROKE_COLOR);
        g2.draw(shape);
        
        FPoint realCenter = shape.getNormalizedCenter(g2);
        
        g2.translate(realCenter.x, realCenter.y);
        
        g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        g2.setPaint((isMouseHover()) ? INNER_FILL_MOUSE_HOVER : INNER_FILL);
        g2.fill(INNER_SHAPE);
        
        g2.setPaint(INNER_STROKE_COLOR);
        g2.setStroke(INNER_STROKE.createStroke(g2));
        g2.draw(INNER_SHAPE);
        
        g2.translate(-realCenter.x, -realCenter.y);
    }
    
    public FShape getShape() {
        return shape;
    }
    
  
    private static final FShape INNER_SHAPE = new FEllipse(-5, -5, 10, 10);
    
    private static final FShape SHAPE = new FEllipse(20);
    
    private static final Color OUTER_FILL = new Color(0xFCFAF5);
    private static final Color OUTER_STROKE_COLOR = new Color(0xFF9900);
    private static final Color INNER_FILL = new Color(0xFFFFFF);
    private static final Color INNER_FILL_MOUSE_HOVER = new Color(0xFF9900);
    private static final Color INNER_STROKE_COLOR = new Color(0xB3B3B3);
    
    private static final FStroke OUTER_STROKE = new FStroke(1, 3);
    private static final FStroke INNER_STROKE = new FStroke(1);
}
