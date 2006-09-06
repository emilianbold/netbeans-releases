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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
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
        setBackground( Utils.getColor(DEFAULT_BACKGROUND_COLOR) );
        setContent( buildContent( BundleSupport.getURL( key ) ) );
    }

    protected JComponent buildContent( String url ) {
        RSSFeed feed = new RSSFeed( url );
        feed.addPropertyChangeListener( RSSFeed.FEED_CONTENT_PROPERTY, this );
        return feed;
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

    protected void feedContentLoaded() {
        
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if( RSSFeed.FEED_CONTENT_PROPERTY.equals( evt.getPropertyName() ) ) {
            feedContentLoaded();
        } else {
            super.propertyChange( evt );
        }

    }
}
