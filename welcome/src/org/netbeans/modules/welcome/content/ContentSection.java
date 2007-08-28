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

package org.netbeans.modules.welcome.content;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.openide.util.Utilities;

/**
 *
 * @author S. Aubrecht
 */
public class ContentSection extends JPanel implements Constants {

    private static final int PANEL_MAX_WIDTH = 800;
    private int location;
    private boolean maxSize;
    
    private Image verticalLine;
    private Image center;
    
    private JLabel lblTitle;
    
    public ContentSection( String title, int location, JComponent content, boolean maxSize ) {
        super( new GridBagLayout() );
        this.location = location;
        this.maxSize = maxSize;
        
        setOpaque( false );
        lblTitle = new JLabel( title );
        lblTitle.setFont( SECTION_HEADER_FONT );
        
        lblTitle.setHorizontalAlignment( JLabel.RIGHT );
        lblTitle.setBorder( BorderFactory.createEmptyBorder(0, 0, 8, 0) );
        lblTitle.setForeground( Utils.getColor( COLOR_SECTION_HEADER ) );
        add( lblTitle, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0) );
        
        add( content, new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        setBorder( BorderFactory.createEmptyBorder(8,12,6,12) );
        
        String centerImageName = null;
        String verticalImageName = null;
        switch( location ) {
        case SwingConstants.NORTH_EAST:
            centerImageName = "section_upper_right.png"; //NOI18N
            verticalImageName = "section_top.png"; //NOI18N
            break;
        case SwingConstants.NORTH_WEST:
            centerImageName = "section_upper_left.png"; //NOI18N
            verticalImageName = "section_top.png"; //NOI18N
            break;
        case SwingConstants.SOUTH_WEST:
            centerImageName = "section_bottom_left.png"; //NOI18N
            verticalImageName = "section_bottom.png"; //NOI18N
            break;
        case SwingConstants.SOUTH_EAST:
            centerImageName = "section_bottom_right.png"; //NOI18N
            verticalImageName = "section_bottom.png"; //NOI18N
            break;
        }
        center = Utilities.loadImage( "org/netbeans/modules/welcome/resources/" + centerImageName ); //NOI18N
        verticalLine = Utilities.loadImage( "org/netbeans/modules/welcome/resources/" + verticalImageName ); //NOI18N
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int centerWidth = center.getWidth(null);
        int centerHeight = center.getHeight(null);
        g.setColor( Utils.getColor( COLOR_SECTION_HEADER ) );
        switch( location ) {
        case SwingConstants.NORTH_EAST:
            g.drawImage( center, 1, height-centerHeight-1, null );
            g.drawLine( 0, height-1, width-25, height-1 );
            g.drawLine( 0, verticalLine.getHeight(null)+25, 0, height );
            g.drawImage( verticalLine, 0, 25, null );
            break;
        case SwingConstants.NORTH_WEST:
            g.drawImage( center, width-centerWidth, height-centerHeight-1, null );
            g.drawLine( 25, height-1, width, height-1 );
            break;
        case SwingConstants.SOUTH_WEST:
            g.drawImage( center, width-centerWidth, 0, null );
            break;
        case SwingConstants.SOUTH_EAST:
            g.drawImage( center, 1, 0, null );
            g.drawLine( 0, 0, 0, height-verticalLine.getHeight(null)-25 );
            g.drawImage( verticalLine, 0, height-verticalLine.getHeight(null)-25, null );
            break;
        }
    }

    @Override
    public void setSize(Dimension d) {
        if( maxSize && d.width > PANEL_MAX_WIDTH ) {
            d = new Dimension( d );
            d.width = PANEL_MAX_WIDTH;
        }
        super.setSize(d);
    }

    @Override
    public void setBounds(Rectangle r) {
        if( maxSize && r.width > PANEL_MAX_WIDTH ) {
            r = new Rectangle( r );
            r.width = PANEL_MAX_WIDTH;
        }
        super.setBounds(r);
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        if( maxSize && w > PANEL_MAX_WIDTH ) {
            w = PANEL_MAX_WIDTH;
        }
        super.setBounds(x,y,w,h);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if( maxSize && d.width > PANEL_MAX_WIDTH ) {
            d = new Dimension( d );
            d.width = PANEL_MAX_WIDTH;
        }
        return d;
    }
    
    public Rectangle getTitleBounds() {
        Rectangle res = lblTitle.getBounds();
        res.height -= lblTitle.getInsets().bottom + 7;
        return res;
    }
}
