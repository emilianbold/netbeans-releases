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

import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.ContentSection;
import org.netbeans.modules.welcome.content.RecentProjectsPanel;

/**
 *
 * @author S. Aubrecht
 */
class MyNetBeansTab extends AbstractTab {
    
    private ContentSection recentProjectsSection;
    private ContentSection blogsSection;

    protected void buildContent() {
        JPanel main = new JPanel( new GridBagLayout() );
        main.setOpaque( false );
        add( main, BorderLayout.CENTER );
        
        JComponent c = new RecentProjectsPanel();
        recentProjectsSection = new ContentSection( BundleSupport.getLabel( "SectionRecentProjects" ), //NOI18N
                SwingConstants.NORTH_WEST, c, false );
        main.add( recentProjectsSection,
                new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.SOUTHEAST,
                GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        c = new ArticlesAndNews();
        main.add( new ContentSection( BundleSupport.getLabel( "SectionNewsAndTutorials" ), //NOI18N
                SwingConstants.NORTH_EAST, c, true ),
                new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.SOUTHWEST,
                GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        c = new DemoPanel();
        main.add( new ContentSection( BundleSupport.getLabel( "SectionDemo" ), //NOI18N
                SwingConstants.SOUTH_WEST, c, true ),
                new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.NORTHEAST,
                GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        c = new Blogs();
        blogsSection = new ContentSection( BundleSupport.getLabel( "SectionBlogs" ), //NOI18N
                SwingConstants.SOUTH_EAST, c, true );
        main.add( blogsSection,
                new GridBagConstraints(1,1,1,1,1.0,0.0,GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        main.add( new JLabel(),
                new GridBagConstraints(0,2,2,1,0.0,1.0,GridBagConstraints.CENTER,
                GridBagConstraints.VERTICAL,new Insets(0,0,0,0),0,0) );
        
        main.add( new BottomBar(),
                new GridBagConstraints(0,3,2,1,1.0,0.0,GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0) );
    }


    protected Point getTopStripOrigin() {
        Point p;
        if( null == recentProjectsSection ) {
            p = new Point(0,0);
        } else {
            Rectangle r = recentProjectsSection.getTitleBounds();
            p = r.getLocation();
            p.y += r.getHeight();
            p.x += r.getWidth()/2;
            p = SwingUtilities.convertPoint( recentProjectsSection, p, this );
        }
        return p;
    }

    protected Point getMiddleStripOrigin() {
        Point p;
        if( null == blogsSection ) {
            p = new Point(0,0);
        } else {
            Rectangle r = blogsSection.getTitleBounds();
            p = r.getLocation();
            p.y += r.getHeight();
            p.x += r.getWidth();
            p = SwingUtilities.convertPoint( blogsSection, p, this );
        }
        return p;
    }
}
