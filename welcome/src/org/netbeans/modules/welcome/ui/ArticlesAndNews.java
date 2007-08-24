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

package org.netbeans.modules.welcome.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.CombinationRSSFeed;
import org.netbeans.modules.welcome.content.RSSFeed;
import org.netbeans.modules.welcome.content.RSSFeedReaderPanel;
import org.netbeans.modules.welcome.content.WebLink;
import org.openide.util.NbPreferences;

/**
 *
 * @author S. Aubrecht
 */
class ArticlesAndNews extends RSSFeedReaderPanel {

    private RSSFeed feed;

    private static final int MAX_ARTICLES_COUNT = 4;

    public ArticlesAndNews() {
        super( "ArticlesAndNews", true ); // NOI18N

        add( buildBottomContent(), BorderLayout.SOUTH );
    }

    @Override
    protected JComponent buildContent(String url, boolean showProxyButton) {
        final Preferences p = NbPreferences.root().node("/org/netbeans/modules/autoupdate"); // NOI18N
        if( null != p ) {
            String ideId = p.get ("ideIdentity", null); // NOI18N
            if( null != ideId && ideId.length() > 0 ) {
                url +=  "?unique=" + ideId; // NOI18N
            }
        }
        feed = new ArticlesAndNewsRSSFeed( url, BundleSupport.getURL("News"), showProxyButton ); // NOI18N
        feed.addPropertyChangeListener( RSSFeed.FEED_CONTENT_PROPERTY, this );
        return feed;
    }
    
    protected JComponent buildBottomContent() {
        WebLink news = new WebLink( "AllNews", false ); // NOI18N
        BundleSupport.setAccessibilityProperties( news, "AllNews" ); //NOI18N
        
        WebLink articles = new WebLink( "AllArticles", false ); // NOI18N
        BundleSupport.setAccessibilityProperties( articles, "AllArticles" ); //NOI18N

        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque( false );

        panel.add( news, new GridBagConstraints(0,1,1,1,0.0,0.0,
                GridBagConstraints.SOUTHWEST,GridBagConstraints.HORIZONTAL,
                new Insets(5,5,0,5),0,0) );
        panel.add( new JLabel(), new GridBagConstraints(1,1,1,1,1.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,
                new Insets(5,5,0,5),0,0) );
        panel.add( articles, new GridBagConstraints(2,1,1,1,0.0,0.0,
                GridBagConstraints.SOUTHEAST,GridBagConstraints.HORIZONTAL,
                new Insets(5,5,0,5),0,0) );

        return panel;
    }

    private class ArticlesAndNewsRSSFeed extends CombinationRSSFeed {
        public ArticlesAndNewsRSSFeed( String url1, String url2, boolean showProxyButton ) {
            super( url1, url2, showProxyButton, MAX_ARTICLES_COUNT );
        }
    }
}
