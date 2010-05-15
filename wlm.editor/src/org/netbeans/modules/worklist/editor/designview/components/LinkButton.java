/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author anjeleevich
 */
public class LinkButton extends JButton {
    public LinkButton(Action action) {
        super(action);
        init();
    }
    
    public LinkButton(String text) {
        super(text);
        init();
    }
    
    private void init() {
        setForeground(Color.BLUE);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setBorder(new EmptyBorder(1, 3, 1, 3));
        setRolloverEnabled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Insets insets = getInsets();
        int w = getWidth();
        int x1 = insets.left;
        int x2 = w - insets.right - 1;;
        
        drawUnderline(g);
    }
    
    private void drawUnderline(Graphics g) {
        Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;
        int w = getWidth() - x - insets.right;
        int h = getHeight() - y - insets.bottom;
        
        FontMetrics fontMetrics = getFontMetrics(getFont());
        
        int lineY = y + (h - fontMetrics.getHeight()) / 2 
                + fontMetrics.getAscent() + 1;
        
        g.setColor(getForeground());
        g.drawLine(x, lineY, x + w - 1, lineY);
    }
}
