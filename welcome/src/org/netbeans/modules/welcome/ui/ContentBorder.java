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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.welcome.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.border.Border;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht
 */
class ContentBorder implements Border {

    private final Image leftShadow;
    private final Image rightShadow;
    private final Image bottomShadow;
    private final Image bottomLeftCorner;
    private final Image bottomRightCorner;
    private final Color bottomGradientTop;
    private final Color bottomGradientBottom;

    private final static Insets insets = new Insets(0,4,23,6);

    public ContentBorder() {
        leftShadow = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/left_shadow.png");
        rightShadow = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/right_shadow.png");
        bottomShadow = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/bottom_shadow.png");
        bottomLeftCorner = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/bottom_left_corner.png");
        bottomRightCorner = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/bottom_right_corner.png");
        bottomGradientTop = new Color(223,233,242);
        bottomGradientBottom = new Color(208,223,235);
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.drawImage(bottomLeftCorner, 0, height-insets.bottom, c);
        g2d.drawImage(bottomRightCorner,
                width-bottomRightCorner.getWidth(c), height-insets.bottom, c);
        g2d.setPaint(new GradientPaint(0, height-insets.bottom, bottomGradientTop,
                0, height-bottomShadow.getHeight(c), bottomGradientBottom));
        g2d.fillRect(bottomLeftCorner.getWidth(c),
                height-insets.bottom, width-bottomLeftCorner.getWidth(c)-bottomRightCorner.getWidth(c), insets.bottom-bottomShadow.getHeight(c));
        g2d.drawImage(bottomShadow, 
                bottomLeftCorner.getWidth(c), height-bottomShadow.getHeight(c),
                width-bottomLeftCorner.getWidth(c)-bottomRightCorner.getWidth(c), bottomShadow.getHeight(c), c);

        g2d.drawImage(leftShadow, 
                0, 0,
                insets.left, height-insets.bottom, c);
        g2d.drawImage(rightShadow, 
                width-insets.right, 0,
                insets.right, height-insets.bottom, c);
    }

    public Insets getBorderInsets(Component c) {
        return (Insets) insets.clone();
    }

    public boolean isBorderOpaque() {
        return false;
    }
}
