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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.Constants;
import org.netbeans.modules.welcome.content.Utils;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author S. Aubrecht
 */
class Stripe extends JPanel implements Constants {

    private Image pattern;
    private Image border;
    private boolean isUpperStripe;
    
    public Stripe( boolean isUpperStripe ) {
        this.isUpperStripe = isUpperStripe;
        pattern = ImageUtilities.loadImage( IMAGE_STRIPE_PATTERN );
        border = ImageUtilities.loadImage( isUpperStripe
                ? IMAGE_STRIPE_BORDER_UPPER
                : IMAGE_STRIPE_BORDER_LOWER );
        setPreferredSize(new Dimension(0,0));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        g.setColor( Utils.getColor(COLOR_TAB_SEL_BACKGROUND) );
        g.fillRect(0, 0, width, height);
        int patternWidth = pattern.getWidth(this);
        int patternHeight = pattern.getHeight(this);
        for( int i=0; i<=width; i+=patternWidth ) {
            for( int j=0; j<=height; j+=patternHeight ) {
                g.drawImage(pattern, i, j, this);
            }
        }
        g.fillRect( 0,0,5,height );
        g.fillRect( width-5,0,5,height );
        if( isUpperStripe )
            g.fillRect(0, 0, width, 5 );
        else
            g.fillRect(0, height-5, width, 5 );
        
        int borderWidth = border.getWidth(this);
        int borderHeight = border.getHeight(this);
        for( int i=0; i<=width; i+=borderWidth ) {
            g.drawImage(border, i, isUpperStripe ? height-borderHeight : 0, this);
        }
    }        
}
