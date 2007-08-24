/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JComponent;
import org.netbeans.modules.welcome.content.Constants;
import org.openide.util.Utilities;

/**
 *
 * @author S. Aubrecht
 */
class TopBar extends JComponent implements Constants {

    private Image imgCenter;
    private Image imgLeft;
    private Image imgRight;
    
    public TopBar() {
        imgCenter = Utilities.loadImage(IMAGE_TOPBAR_CENTER, true);
        imgLeft = Utilities.loadImage(IMAGE_TOPBAR_LEFT, true);
        imgRight = Utilities.loadImage(IMAGE_TOPBAR_RIGHT, true);
        
        setPreferredSize( new Dimension( imgCenter.getWidth(null), imgCenter.getHeight(null)) );
    }

    @Override
    protected void paintBorder(Graphics g) {
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        
        int centerImageWidth = imgCenter.getWidth(null);
        int centerImageHeight = imgCenter.getHeight(null);
        
        int x = (width - centerImageWidth) / 2;
        int y = Math.min( (height - centerImageHeight) / 2, 0 );
        
        g.drawImage(imgCenter, x, y, null);
        if( x > 0 ) {
            for( int i=0; i<=x; i++ ) {
                g.drawImage(imgLeft, i, y, null);
                g.drawImage(imgRight, width-i-1, y, null);
            }
        }
    }
}
