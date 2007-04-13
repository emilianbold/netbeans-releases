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

package org.netbeans.modules.tasklist.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

/**
 *
 * @author S. Aubrecht
 */
class MenuToggleButton extends JToggleButton {
    
    private boolean mouseInArrowArea = false;
    
    /** Creates a new instance of MenuToggleButton */
    public MenuToggleButton( final Icon regIcon, Icon rollOverIcon, int arrowWidth ) {
        assert null != regIcon;
        assert null != rollOverIcon;
        final Icon lineIcon = new LineIcon( rollOverIcon, arrowWidth );
        setIcon( regIcon );
        setRolloverIcon( lineIcon );
        setRolloverSelectedIcon( lineIcon );
        setFocusable( false );
        
        addMouseMotionListener( new MouseMotionAdapter() {
            public void mouseMoved( MouseEvent e ) {
                mouseInArrowArea = isInArrowArea( e.getPoint() );
                setRolloverIcon( mouseInArrowArea ? regIcon : lineIcon );
                setRolloverSelectedIcon( mouseInArrowArea ? regIcon : lineIcon );
            }
        });
        
        addMouseListener( new MouseAdapter() {
            public void mousePressed( MouseEvent e ) {
                if( isInArrowArea( e.getPoint() ) ) {
                    JPopupMenu popup = getPopupMenu();
                    if( null != popup )
                        popup.show( MenuToggleButton.this, 0, getHeight() );
                }
            }

            public void mouseEntered( MouseEvent e ) {
                mouseInArrowArea = isInArrowArea( e.getPoint() );
                setRolloverIcon( mouseInArrowArea ? regIcon : lineIcon );
                setRolloverSelectedIcon( mouseInArrowArea ? regIcon : lineIcon );
            }

            public void mouseExited( MouseEvent e ) {
                mouseInArrowArea = false;
                setRolloverIcon( regIcon );
                setRolloverSelectedIcon( regIcon );
            }
        });
        
        setModel( new Model() );
    }

    protected JPopupMenu getPopupMenu() {
        return null;
    }
    
    private boolean isInArrowArea( Point p ) {
        return p.getLocation().x >= getWidth() - 3 - 2 - getInsets().right;
    }
    
    private static class LineIcon implements Icon {
        private Icon origIcon;
        private int arrowWidth;
        
        public LineIcon( Icon origIcon, int arrowWidth ) {
            this.origIcon = origIcon;
            this.arrowWidth = arrowWidth;
        }
    
        public void paintIcon( Component c, Graphics g, int x, int y ) {
            origIcon.paintIcon( c, g, x, y );
            
            g.setColor( UIManager.getColor( "controlHighlight" ) ); //NOI18N
            g.drawLine( x+origIcon.getIconWidth()-arrowWidth-2, y, 
                        x+origIcon.getIconWidth()-arrowWidth-2, y+getIconHeight() );
            g.setColor( UIManager.getColor( "controlShadow" ) ); //NOI18N
            g.drawLine( x+origIcon.getIconWidth()-arrowWidth-3, y, 
                        x+origIcon.getIconWidth()-arrowWidth-3, y+getIconHeight() );
        }

        public int getIconWidth() {
            return origIcon.getIconWidth();
        }

        public int getIconHeight() {
            return origIcon.getIconHeight();
        }
    }
    
    private class Model extends JToggleButton.ToggleButtonModel {
        public void setPressed(boolean b) {
            if( mouseInArrowArea )
                return;
            super.setPressed( b );
        }
    }
}