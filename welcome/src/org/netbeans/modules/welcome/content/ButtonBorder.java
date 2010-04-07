/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.welcome.content;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.border.Border;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht
 */
public class ButtonBorder implements Border {

    private final Image imgTopLeft;
    private final Image imgBottomLeft;
    private final Image imgLeft;
    private final Image imgTopRight;
    private final Image imgBottomRight;
    private final Image imgRight;
    private final Image imgCenter;
    private final Image imgTop;
    private final Image imgBottom;

    private ButtonBorder( boolean mouseOver ) {
        if( mouseOver ) {
            imgTopLeft = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-sel-top-lef.png");
            imgTopRight = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-sel-top-right.png");
            imgBottomLeft = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-sel-bott-left.png");
            imgBottomRight = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-sel-bott-right.png");
            imgLeft = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-sel-side-left.png");
            imgRight = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-sel-side-right.png");
            imgTop = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-sel-top.png");
            imgBottom = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-sel-bottom.png");
            imgCenter = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-sel-center.png");
        } else {
            imgTopLeft = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-top-lef.png");
            imgTopRight = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-top-right.png");
            imgBottomLeft = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-bott-left.png");
            imgBottomRight = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-bott-right.png");
            imgLeft = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-side-left.png");
            imgRight = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-side-right.png");
            imgTop = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-top.png");
            imgBottom = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-bottom.png");
            imgCenter = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/butt-center.png");
        }
    }

    private static Border regularBorder;
    private static Border mouseoverBorder;

    public static Border createRegular() {
        if( null == regularBorder )
            regularBorder = new ButtonBorder(false);
        return regularBorder;
    }

    public static Border createMouseOver() {
        if( null == mouseoverBorder )
            mouseoverBorder = new ButtonBorder(true);
        return mouseoverBorder;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.drawImage(imgTopLeft, x, y, c);
        g.drawImage(imgBottomLeft, x, y+height-imgBottomLeft.getHeight(c), c);
        g.drawImage(imgLeft, x, y+imgTopLeft.getHeight(c), imgLeft.getWidth(c), height-imgTopLeft.getHeight(c)-imgBottomLeft.getHeight(c), c);

        g.drawImage(imgTopRight, x+width-imgTopRight.getWidth(c), y, c);
        g.drawImage(imgBottomRight, x+width-imgBottomRight.getWidth(c), y+height-imgBottomRight.getHeight(c), c);
        g.drawImage(imgRight, x+width-imgRight.getWidth(c), y+imgTopRight.getHeight(c), imgRight.getWidth(c), height-imgTopRight.getHeight(c)-imgBottomRight.getHeight(c), c);

        g.drawImage(imgTop, x+imgTopLeft.getWidth(c), y,
                x+width-imgTopLeft.getWidth(c)-imgTopRight.getWidth(c), imgTop.getHeight(c), c);
        g.drawImage(imgBottom, x+imgBottomLeft.getWidth(c), y+height-imgBottom.getHeight(c),
                x+width-imgBottomLeft.getWidth(c)-imgBottomRight.getWidth(c), imgBottom.getHeight(c), c);

        g.drawImage(imgCenter, x+imgTopLeft.getWidth(c), y+imgTopLeft.getHeight(c),
                x+width-imgTopLeft.getWidth(c)-imgTopRight.getWidth(c), y+height-imgTopLeft.getHeight(c)-imgBottomLeft.getHeight(c), c);

    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(11, 11, 11, 11);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

}
