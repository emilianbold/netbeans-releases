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

package org.netbeans.modules.debugger.jpda.ui.debugging;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JPanel;

/**
 *
 * @author Dan
 */
public class ZebraPanel extends JPanel {
    
    private int thickness;
    private int stripesCount;
    
    ZebraPanel(int thickness, int stripesCount) {
        this.thickness = thickness;
        this.stripesCount = stripesCount; // [TODO] not used yet
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        
        //super.paintComponent(g);
        
        int width = getWidth();
        int height = getHeight();
        
        if ((width <= 0) || (height <= 0)) {
            return;
        }

        Rectangle clipRect = g.getClipBounds();
        int clipX;
        int clipY;
        int clipW;
        int clipH;
        if (clipRect == null) {
            clipX = clipY = 0;
            clipW = width;
            clipH = height;
        }
        else {
            clipX = clipRect.x;
            clipY = clipRect.y;
            clipW = clipRect.width;
            clipH = clipRect.height;
        }

        if(clipW > width) {
            clipW = width;
        }
        if(clipH > height) {
            clipH = height;
        }

        Color origColor = g.getColor();
        int sy = (clipY / thickness) * thickness;
        boolean isWhite = (clipY / thickness) % 2 == 0;
        while (sy < clipY + clipH - 1) {
            int y1 = Math.max(sy, clipY);
            int y2 = Math.min(clipY + clipH, y1 + thickness) ;
            g.setColor(isWhite ? Color.WHITE : Color.LIGHT_GRAY);
            isWhite = !isWhite;
            g.fillRect(clipX, y1, clipW, y2 - y1);
            sy += thickness;
        }
        g.setColor(origColor);
    }
    
}
