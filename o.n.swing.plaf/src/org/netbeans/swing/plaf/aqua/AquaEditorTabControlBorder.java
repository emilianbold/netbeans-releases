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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
/*
 * AquaEditorTabControlBorder.java
 *
 * Created on March 14, 2004, 7:34 PM
 */

package org.netbeans.swing.plaf.aqua;

import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;


/** Border for the embedded control which displays tabs in the tab control.
 * This comprises the upper border of the control.
 *
 * @author  Tim Boudreau
 */
public class AquaEditorTabControlBorder implements Border {
    static int ARCSIZE = 16;

    /** Creates a new instance of AquaViewTabControlBorder */
    public AquaEditorTabControlBorder() {
    }
    
    public Insets getBorderInsets(Component component) {
        return new Insets (1,1,1,1);
    }

    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        UIUtils.configureRenderingHints(g);

        Color col = UIUtils.getMiddle(UIManager.getColor("controlShadow"), 
            UIManager.getColor("control"));

        g.setColor(col);

        Graphics2D g2d = (Graphics2D) g;
        int ytop = y + (h / 2) - 1;

        drawLines (g, x, y, ytop, w, h);
        x++;
        ytop++;
        w-=2;
        h-=1;
        g.setColor (UIUtils.getMiddle (col, UIManager.getColor("control"))); //NOI18N
        drawLines (g, x, y, ytop, w, h);
    }

    private void drawLines (Graphics g, int x, int y, int ytop, int w, int h) {
        g.drawArc (x, ytop, ARCSIZE, ARCSIZE, 90, 90);
        g.drawLine(x, ytop+(ARCSIZE/2), x, y+h);

        g.drawArc (x+w-(ARCSIZE+1), ytop, ARCSIZE, ARCSIZE, 90, -90);
        g.drawLine(x+w-1, ytop+(ARCSIZE/2), x+w-1, y+h);
    }
}
