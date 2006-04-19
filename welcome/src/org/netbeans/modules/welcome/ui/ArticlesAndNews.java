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

package org.netbeans.modules.welcome.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.autoupdate.Settings;
import org.netbeans.modules.welcome.WelcomeComponent;
import org.netbeans.modules.welcome.content.RSSFeedReaderPanel;
import org.netbeans.modules.welcome.content.WebLink;
import org.openide.windows.WindowManager;

/**
 *
 * @author S. Aubrecht
 */
public class ArticlesAndNews extends RSSFeedReaderPanel {

    public ArticlesAndNews() {
        super( "ArticlesAndNews" ); // NOI18N

        setBottomContent( buildBottomContent() );
    }

    protected JComponent buildContent(String url) {
        Settings autoUpdateSettings = ((Settings)Settings.findObject( Settings.class ));
        if( null != autoUpdateSettings ) {
            String ideId = autoUpdateSettings.getIdeIdentity();
            if( null != ideId && ideId.length() > 0 ) {
                url +=  "?unique=" + ideId; // NOI18N
            }
        }
        return super.buildContent( url );
    }

    protected JComponent buildBottomContent() {
        WebLink news = new WebLink( "AllNews" ); // NOI18N
        news.setFont( HEADER_FONT );
        news.setForeground( HEADER_TEXT_COLOR );
        WebLink articles = new WebLink( "AllArticles" ); // NOI18N
        articles.setFont( HEADER_FONT );
        articles.setForeground( HEADER_TEXT_COLOR );

        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque( false );

        panel.add( news, new GridBagConstraints(0,1,1,1,0.0,0.0,
                GridBagConstraints.SOUTHWEST,GridBagConstraints.HORIZONTAL,
                new Insets(5,5,0,5),0,0) );
        panel.add( new JLabel(), new GridBagConstraints(1,1,1,1,1.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,
                new Insets(0,0,0,0),0,0) );
        panel.add( articles, new GridBagConstraints(2,1,1,1,0.0,0.0,
                GridBagConstraints.SOUTHEAST,GridBagConstraints.HORIZONTAL,
                new Insets(5,5,0,5),0,0) );

        return panel;
    }

    private boolean firstTimeLoad = true;
    protected void feedContentLoaded() {
        if( firstTimeLoad ) {
            firstTimeLoad = false;
            WelcomeComponent wc = WelcomeComponent.findComp();
            if( null != wc && WindowManager.getDefault().getRegistry().getActivated() == wc )
                switchFocus();
        }
    }
}
