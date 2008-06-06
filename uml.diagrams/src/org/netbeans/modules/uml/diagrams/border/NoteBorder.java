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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.diagrams.border;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import org.netbeans.api.visual.border.Border;

/**
 *
 * @author sp153251
 */
public class NoteBorder implements Border {
    private Color color;

    public final static int TAB_WIDTH = 11;
    
    public NoteBorder()
    {
        this(Color.BLACK);
    }
    
    public NoteBorder(Color color)
    {
        this.color=color;
    }
    
    public Insets getInsets() {
        return new Insets(TAB_WIDTH, 2, 2, 2);
    }

    public void paint(Graphics2D gr, Rectangle bounds) {
        gr.setColor(color);
        
        int leftBorder = bounds.x;
        int top = bounds.y;
        int rightBorder = bounds.x + bounds.width - 1;
        int tabStartX = rightBorder - TAB_WIDTH;
        int bottom = bounds.y + bounds.height - 1;
        int tabBottom = bounds.y + TAB_WIDTH;
        
        gr.drawLine(leftBorder, top, tabStartX, top);
        gr.drawLine(tabStartX, top, rightBorder, tabBottom);
        gr.drawLine(rightBorder, tabBottom, tabStartX, tabBottom);
        gr.drawLine(tabStartX, tabBottom, tabStartX, top);
        gr.drawLine(rightBorder, tabBottom, rightBorder, bottom);
        gr.drawLine(rightBorder, bottom, leftBorder, bottom);
        gr.drawLine(leftBorder, bottom, leftBorder, top);
    }

    public boolean isOpaque() {
        return true;
    }

}
