/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 *
 * @author anjeleevich
 */
public class TitledBorder implements Border {
    private JComponent title;
    
    public TitledBorder(JComponent title) {
        this.title = title;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, 
            int width, int height) 
    {
        Graphics2D g2 = ExUtils.prepareG2(g, true);
        
        Rectangle titleBounds  = title.getBounds();
        int w = c.getWidth();
        int h = c.getHeight();
        
        int y1 = titleBounds.y + titleBounds.height / 2;
        int y2 = h - 2;
        
        g2.setColor(ExTabbedPane.TAB_BORDER_COLOR);
        g2.drawRoundRect(1, y1, w - 3, y2 - y1, 12, 12);
        
        ExUtils.disposeG2(g2, true);
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(title.getPreferredSize().height + 3, 8, 8, 8);
    }

    public boolean isBorderOpaque() {
        return false;
    }
}
