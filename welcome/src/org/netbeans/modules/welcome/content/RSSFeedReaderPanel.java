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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author S. Aubrecht
 */
public class RSSFeedReaderPanel extends JPanel implements PropertyChangeListener {

    private static final int FEED_PANEL_MIN_WIDTH = 200;
    private static final int FEED_PANEL_MAX_WIDTH = 600;

    /** Creates a new instance of AbstractFeedReaderPanel */
    public RSSFeedReaderPanel( String url ) {
        super( new BorderLayout() );
        setOpaque( false );
        add( buildContent( url, false ), BorderLayout.CENTER );
        setMaximumSize( new Dimension(400, Integer.MAX_VALUE) );
    }

    /** Creates a new instance of AbstractFeedReaderPanel */
    public RSSFeedReaderPanel( String key, boolean showProxyButton ) {
        super( new BorderLayout() );
        setOpaque( false );
        add( buildContent( BundleSupport.getURL( key ), showProxyButton ), BorderLayout.CENTER );
    }

    protected JComponent buildContent( String url, boolean showProxyButton ) {
        RSSFeed feed = new RSSFeed( url, showProxyButton );
        feed.addPropertyChangeListener( RSSFeed.FEED_CONTENT_PROPERTY, this );
        return feed;
    }
    
    protected void feedContentLoaded() {
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if( RSSFeed.FEED_CONTENT_PROPERTY.equals( evt.getPropertyName() ) ) {
            feedContentLoaded();
        }
    }
}
