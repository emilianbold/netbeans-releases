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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import org.netbeans.modules.welcome.content.Constants;
import org.openide.util.Utilities;

/**
 * Base class for inner tabs in the Welcome Page
 * 
 * @author S. Aubrecht
 */
abstract class AbstractTab extends JPanel implements Scrollable, Constants {

    private boolean initialized = false;

    private Image imgBottomStripWest;
    private Image imgBottomStripCenter;
    private Image imgBottomStripEast;

    private Image imgTopStripWest;
    private Image imgTopStripCenter;

    private Image imgMiddleStripEast;
    private Image imgMiddleStripCenter;

    public AbstractTab() {
        super( new BorderLayout() );
        setOpaque( false );
        
        this.imgBottomStripCenter = Utilities.loadImage( IMAGE_STRIP_BOTTOM_CENTER );
        this.imgBottomStripWest = Utilities.loadImage( IMAGE_STRIP_BOTTOM_WEST );
        this.imgBottomStripEast = Utilities.loadImage( IMAGE_STRIP_BOTTOM_EAST );
        
        this.imgTopStripCenter = Utilities.loadImage( IMAGE_STRIP_TOP_CENTER );
        this.imgTopStripWest = Utilities.loadImage( IMAGE_STRIP_TOP_WEST );
        
        this.imgMiddleStripCenter = Utilities.loadImage( IMAGE_STRIP_MIDDLE_CENTER );
        this.imgMiddleStripEast = Utilities.loadImage( IMAGE_STRIP_MIDDLE_EAST );
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if( !initialized ) {
            buildContent();
            initialized = true;
        }
    }

    protected abstract void buildContent();
    
    protected abstract Point getTopStripOrigin();
    
    protected abstract Point getMiddleStripOrigin();
    
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if( null != getParent() && getParent().getHeight() > 0 && getParent().getHeight() > d.height )
            d.height = getParent().getHeight();
        return d;
    }
    
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Constants.FONT_SIZE;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 30*Constants.FONT_SIZE;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        
        //top strip
        Point origin = getTopStripOrigin();
        g.drawImage( imgTopStripWest, origin.x, origin.y, null );
        for( int i=origin.x+imgTopStripWest.getWidth(null); i<width; i++ ) {
            g.drawImage( imgTopStripCenter, i, origin.y, null );
        }
        
        //middle strip
        origin = getMiddleStripOrigin();
        g.drawImage( imgMiddleStripEast, origin.x-imgMiddleStripEast.getWidth(null), origin.y, null );
        for( int i=origin.x-imgMiddleStripEast.getWidth(null); i>=0; i-- ) {
            g.drawImage( imgMiddleStripCenter, i, origin.y, null );
        }
        
        //bottom strip
        int eastWidth = imgBottomStripEast.getWidth( null );
        int eastHeight = imgBottomStripEast.getHeight( null );
        int westWidth = imgBottomStripWest.getWidth( null );
        int westHeight = imgBottomStripWest.getHeight( null );
        g.drawImage( imgBottomStripEast, width-eastWidth-1, height-eastHeight-1, null    );
        int centerWidth = Math.max( 100, width-eastWidth-westWidth-150 );
        
        g.drawImage( imgBottomStripWest, width-eastWidth-1-centerWidth-westWidth, height-westHeight-1, null );
        for( int i=0; i<centerWidth; i++ ) {
            g.drawImage( imgBottomStripCenter, width-eastWidth-centerWidth-1+i, height-westHeight-1, null );
        }
        
    }
}
