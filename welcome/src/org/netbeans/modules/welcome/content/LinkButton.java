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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome.content;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import org.openide.util.Utilities;

/**
 *
 * @author S. Aubrecht
 */
public abstract class LinkButton extends JButton
        implements Constants, MouseListener, ActionListener, FocusListener {

    private boolean underline = false;

    final ImageIcon BULLET_ICON = new ImageIcon( Utilities.loadImage( BULLET_IMAGE ) );

    public LinkButton( String label, boolean showBullet ) {
        super( label );
        setForeground( Utils.getColor(LINK_COLOR) );
        setFont( BUTTON_FONT );
        setBorder( new EmptyBorder(1, 1, 1, 1) );
        setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
        setHorizontalAlignment( JLabel.LEFT );
        addMouseListener(this);
        setFocusable( true );
        if( showBullet )
            setIcon( BULLET_ICON );

        setMargin( new Insets(0, 0, 0, 0) );
        setBorderPainted( false );
        setFocusPainted( false );
        setRolloverEnabled( true );
        setContentAreaFilled( false );

        addActionListener( this );
        addFocusListener( this );
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        if( isEnabled() ) {
            underline = true;
            setForeground( Utils.getColor(LINK_IN_FOCUS_COLOR) );
            repaint();
            onMouseEntered( e );
        }
    }

    public void mouseExited(MouseEvent e) {
        if( isEnabled() ) {
            underline = false;
            setForeground( Utils.getColor(isVisited() ? VISITED_LINK_COLOR : LINK_COLOR) );
            repaint();
            onMouseExited( e );
        }
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Utils.prepareGraphics( g );
        super.paintComponent(g2);

        Dimension size = getSize();
        if( hasFocus() && isEnabled() ) {
            g2.setStroke( LINK_IN_FOCUS_STROKE );
            g2.setColor( Utils.getColor(LINK_IN_FOCUS_COLOR) );
            g2.drawRect( 0, 0, size.width - 1, size.height - 1 );
        }
    }
    
    public void focusLost(FocusEvent e) {
    }

    public void focusGained(FocusEvent e) {
        Rectangle rect = getBounds();
        rect.grow( 0, FONT_SIZE );
        scrollRectToVisible( rect );
    }

    protected void onMouseExited(MouseEvent e) {
    }

    protected void onMouseEntered(MouseEvent e) {
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if( underline && isEnabled() ) {
            Font f = getFont();
            FontMetrics fm = getFontMetrics(f);
            int iconWidth = 0;
            if( null != getIcon() ) {
                iconWidth = getIcon().getIconWidth()+getIconTextGap();
            }
            int x1 = iconWidth;
            int y1 = fm.getHeight();
            int x2 = fm.stringWidth(getText()) + iconWidth;
            if( getText().length() > 0 )
                g.drawLine(x1, y1, x2, y1);
        }
    }
    
    protected boolean isVisited() {
        return false;
    }
}
