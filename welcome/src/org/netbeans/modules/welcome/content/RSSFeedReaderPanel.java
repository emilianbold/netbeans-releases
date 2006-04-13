/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome.content;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JComponent;

/**
 *
 * @author S. Aubrecht
 */
public abstract class RSSFeedReaderPanel extends ContentPanel {
    
    /** Creates a new instance of AbstractFeedReaderPanel */
    public RSSFeedReaderPanel( String key ) {
        super( BundleSupport.getLabel( key ) );
        setOpaque( true );
        setBackground( DEFAULT_BACKGROUND_COLOR );
        setContent( buildContent( BundleSupport.getURL( key ) ) );
    }

    protected JComponent buildContent( String url ) {
        return new RSSFeed( url );
    }

    public void setSize(Dimension d) {
        if( d.width < FEED_PANEL_MIN_WIDTH || d.width > FEED_PANEL_MAX_WIDTH ) {
            d = new Dimension( d );
            if( d.width < FEED_PANEL_MIN_WIDTH )
                d.width = FEED_PANEL_MIN_WIDTH;
            else
                d.width = FEED_PANEL_MAX_WIDTH;
        }
        super.setSize(d);
    }

    public void setBounds(Rectangle r) {
        if( r.width < FEED_PANEL_MIN_WIDTH || r.width > FEED_PANEL_MAX_WIDTH ) {
            r = new Rectangle( r );
            if( r.width < FEED_PANEL_MIN_WIDTH )
                r.width = FEED_PANEL_MIN_WIDTH;
            else
                r.width = FEED_PANEL_MAX_WIDTH;
        }
        super.setBounds(r);
    }

    public void setBounds(int x, int y, int width, int height) {
        if( width < FEED_PANEL_MIN_WIDTH )
            width = FEED_PANEL_MIN_WIDTH;
        else if( width > FEED_PANEL_MAX_WIDTH )
            width = FEED_PANEL_MAX_WIDTH;
        super.setBounds(x, y, width, height);
    }

    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if( d.width < FEED_PANEL_MIN_WIDTH || d.width > FEED_PANEL_MAX_WIDTH ) {
            d = new Dimension( d );
            if( d.width < FEED_PANEL_MIN_WIDTH )
                d.width = FEED_PANEL_MIN_WIDTH;
            else
                d.width = FEED_PANEL_MAX_WIDTH;
        }
        return d;
    }
}
