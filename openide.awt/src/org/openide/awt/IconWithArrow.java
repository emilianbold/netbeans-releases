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

package org.openide.awt;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import org.openide.util.Utilities;

/**
 * An icon that paints a small arrow to the right of the provided icon.
 * 
 * @author S. Aubrecht
 * @since 6.11
 */
class IconWithArrow implements Icon {
    
    public static final String ARROW_IMAGE_NAME = "org/openide/awt/resources/arrow.png"; //NOI18N
    
    private Icon orig;
    private Icon arrow = new ImageIcon( Utilities.loadImage( ARROW_IMAGE_NAME ) );
    private boolean paintRollOver;
    
    public static final int GAP = 6;
    
    /** Creates a new instance of IconWithArrow */
    public IconWithArrow(  Icon orig, boolean paintRollOver ) {
        this.orig = orig;
        this.paintRollOver = paintRollOver;
    }

    public void paintIcon( Component c, Graphics g, int x, int y ) {
        int height = getIconHeight();
        orig.paintIcon( c, g, x, y+(height-orig.getIconHeight())/2 );
        
        arrow.paintIcon( c, g, x+GAP+orig.getIconWidth(), y+(height-arrow.getIconHeight())/2 );
        
        if( paintRollOver ) {
            Color brighter = UIManager.getColor( "controlHighlight" ); //NOI18N
            Color darker = UIManager.getColor( "controlShadow" ); //NOI18N
            if( null == brighter || null == darker ) {
                brighter = c.getBackground().brighter();
                darker = c.getBackground().darker();
            }
            if( null != brighter && null != darker ) {
                g.setColor( brighter );
                g.drawLine( x+orig.getIconWidth()+1, y, 
                            x+orig.getIconWidth()+1, y+getIconHeight() );
                g.setColor( darker );
                g.drawLine( x+orig.getIconWidth()+2, y, 
                            x+orig.getIconWidth()+2, y+getIconHeight() );
            }
        }
    }

    public int getIconWidth() {
        return orig.getIconWidth() + GAP + arrow.getIconWidth();
    }

    public int getIconHeight() {
        return Math.max( orig.getIconHeight(), arrow.getIconHeight() );
    }

    public static int getArrowAreaWidth() {
        return GAP/2 + 5;
    }
}
