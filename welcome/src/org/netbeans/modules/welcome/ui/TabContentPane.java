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

package org.netbeans.modules.welcome.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht
 */
class TabContentPane extends JPanel {

    private final Image leftShadow;
    private final Image rightShadow;
    private final Image bottomShadow;
    private final Image bottomLeftCorner;
    private final Image bottomRightCorner;

    private final static Insets insets = new Insets(0,19,85,22);

    public TabContentPane() {
        super( new GridBagLayout() );
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0,4,23,6));
        setMinimumSize(new Dimension(41, 85));

        leftShadow = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/left_shadow.png");
        rightShadow = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/right_shadow.png");
        bottomShadow = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/bottom_shadow.png");
        bottomLeftCorner = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/bottom_left_corner.png");
        bottomRightCorner = ImageUtilities.loadImage("org/netbeans/modules/welcome/resources/bottom_right_corner.png");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();
        g2d.drawImage(bottomLeftCorner, 0, height-insets.bottom, this);
        g2d.drawImage(bottomRightCorner,
                width-insets.right, height-insets.bottom, this);
        g2d.drawImage(bottomShadow,
                insets.left, height-insets.bottom,
                width-insets.left-insets.right, insets.bottom, this);

        g2d.drawImage(leftShadow,
                0, 0,
                insets.left, height-insets.bottom, this);
        g2d.drawImage(rightShadow,
                width-insets.right, 0,
                insets.right, height-insets.bottom, this);

        g2d.setColor(Color.white);
        g2d.fillRect(insets.left, 0, width-insets.left-insets.right, height-insets.bottom);
    }
}
