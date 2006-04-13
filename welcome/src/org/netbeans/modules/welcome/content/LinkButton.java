/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */
package org.netbeans.modules.welcome.content;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public class LinkButton extends JButton implements Constants, ActionListener  {

    private Action action;

    public LinkButton( Action a ) {
        this( a.getValue( Action.NAME ).toString() );
        this.action = a;
        Object icon = a.getValue( Action.SMALL_ICON );
        if( null != icon && icon instanceof Icon )
            setIcon( (Icon)icon );
        Object tooltip = a.getValue( Action.SHORT_DESCRIPTION );
        if( null != tooltip )
            setToolTipText( tooltip.toString() );
    }

    public LinkButton( String label ) {
        setText( label );
        setFocusable( true );
        setForeground( DEFAULT_TEXT_COLOR );
        setFont( REGULAR_FONT );
        setBorder( new EmptyBorder(1, 1, 1, 1) );
        setMargin( new Insets(0, 0, 0, 0) );
        setBorderPainted( false );
        setFocusPainted( false );
        setRolloverEnabled( true );
        setContentAreaFilled( false );
        setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
        setHorizontalAlignment( JButton.LEFT );
        
        addActionListener(this);
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = Utils.prepareGraphics( g );
        Dimension size = getSize();
        
        String text = getText();
        Font font = getFont();
        
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        
        Insets insets = getMargin();
        
        int x = 1;
        int y = size.height / 2 - (fm.getAscent() + fm.getDescent()) / 2
            + fm.getAscent();
        
        Icon icon = getIcon();
        if( null != icon ) {
            icon.paintIcon( this, g, 0, (size.height-icon.getIconHeight())/2);
            x += icon.getIconWidth() + getIconTextGap();
        }
        g2.setColor(getForeground());
        g2.drawString(text, x, y);
        
        if (getModel().isRollover()) {
            g2.drawLine(x, y + 1,
                size.width - 1, y + 1);
        }
        
        if( hasFocus() ) {
            g2.setStroke( LINK_IN_FOCUS_STROKE );
            g2.setColor( LINK_IN_FOCUS_COLOR );
            g2.drawRect( 0, 0, size.width - 1, size.height - 1 );
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        if( null != action ) {
            action.actionPerformed( e );
        }
    }

    private static final long serialVersionUID = 1L; 
}
