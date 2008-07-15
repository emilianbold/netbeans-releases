/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;

/**
 *
 * @author Sheryl Su
 */
public class CompartmentSeparatorWidget extends SeparatorWidget
{
    private Stroke stroke;
    private Orientation orientation;
    private int borderThickness;
    
    public CompartmentSeparatorWidget(Scene scene, Orientation orientation, Stroke stroke, int borderThickness)
    {
        super(scene, orientation);
        this.stroke = stroke;
        this.orientation = orientation;
        this.borderThickness = borderThickness;
        
        if (orientation == Orientation.HORIZONTAL)
            setBorder(BorderFactory.createEmptyBorder(borderThickness, 0, 0, 0));
        else
            setBorder(BorderFactory.createEmptyBorder(0, borderThickness, 0, 0));
        setCursor(Cursor.getPredefinedCursor(orientation == Orientation.HORIZONTAL? Cursor.S_RESIZE_CURSOR: Cursor.W_RESIZE_CURSOR));
    }
 

    protected void paintWidget()
    {
        Graphics2D gr = getGraphics();
        gr.setColor(getForeground());
        Rectangle bounds = getBounds();
        Stroke originalStroke = gr.getStroke();
        Insets insets = getBorder().getInsets();
        if (stroke != null)
        {
            gr.setStroke(stroke);
        }
        if (getOrientation() == Orientation.HORIZONTAL)
        {
            gr.drawLine(0, 0, bounds.width - insets.left - insets.right, 0);
        } else
        {
            gr.drawLine(0, 0, 0, bounds.height - insets.top - insets.bottom);
        }
        gr.setStroke(originalStroke);
    }
    
    @Override
    protected Rectangle calculateClientArea () 
    {
        if (orientation == Orientation.HORIZONTAL)
            return new Rectangle (0, 0, 0, getThickness() + getBorder().getInsets().bottom + getBorder().getInsets().top);
        else
            return new Rectangle (0, 0, getThickness() + getBorder().getInsets().left + getBorder().getInsets().right, 0);
    }
}
