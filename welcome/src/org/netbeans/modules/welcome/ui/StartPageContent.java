/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.welcome.ui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.Constants;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht
 */
public class StartPageContent extends JPanel implements Constants {

    private final static Color COLOR_TOP = new Color(198, 211, 223);
    private final static Color COLOR_BOTTOM = new Color(235, 235, 235);

    private Image imgCenter;
    private Image imgLeft;
    private Image imgRight;


    public StartPageContent() {
        super( new GridBagLayout() );

        imgCenter = ImageUtilities.loadImage(IMAGE_TOPBAR_CENTER, true);
        imgLeft = ImageUtilities.loadImage(IMAGE_TOPBAR_LEFT, true);
        imgRight = ImageUtilities.loadImage(IMAGE_TOPBAR_RIGHT, true);

//        setPreferredSize( new Dimension( imgCenter.getWidth(null), imgCenter.getHeight(null)) );

        JComponent tabs = new TabbedPane( new LearnAndDiscoverTab(),
                new MyNetBeansTab(),
                new WhatsNewTab());
        tabs.setBorder(BorderFactory.createEmptyBorder(10,15,15,15));
        tabs.setOpaque(false);

        add( new TopBar(), new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(7,0,0,0), 0, 0) );
        
        add( tabs, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0) );

        add( new JLabel(), new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0) );
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();

        int centerImageWidth = imgCenter.getWidth(null);
        int centerImageHeight = imgCenter.getHeight(null);

        int x = (width - centerImageWidth) / 2;
        int y = 0;

        g.drawImage(imgCenter, x, y, null);
        if( x > 0 ) {
            for( int i=0; i<=x; i++ ) {
                g.drawImage(imgLeft, i, y, null);
                g.drawImage(imgRight, width-i-1, y, null);
            }
        }
        
        g2d.setPaint(new GradientPaint(0, centerImageHeight, COLOR_TOP, 0, height, COLOR_BOTTOM));
        g2d.fillRect(0, centerImageHeight, width, height);
    }
}
