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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.ContentSection;
import org.netbeans.modules.welcome.content.Utils;

/**
 * 'Welcome' tab of the Start Page
 * 
 * @author S. Aubrecht
 */
class WelcomeTab extends AbstractTab {
    
    private ContentSection getStartedSection;
    private ContentSection learnMoreSection;

    protected void buildContent() {
        JLabel lbl = new JLabel( BundleSupport.getLabel("WelcomeToNetBeans") ); //NOI18N
        lbl.setFont( WELCOME_LABEL_FONT );
        lbl.setForeground( Utils.getColor( COLOR_WELCOME_LABEL ) );
        lbl.setHorizontalAlignment( JLabel.CENTER );
        lbl.setBorder( BorderFactory.createEmptyBorder(15, 0, 0, 0));
        add( lbl, BorderLayout.NORTH );
        
        JPanel main = new JPanel( new GridBagLayout() );
        main.setOpaque( false );
        add( main, BorderLayout.CENTER );
        
        JComponent c = new GetStarted();
        getStartedSection = new ContentSection( BundleSupport.getLabel("SectionGetStarted"),  //NOI18N
                SwingConstants.NORTH_WEST, c, false );
        main.add( getStartedSection,
                new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.SOUTHEAST,
                GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        c = new Samples();
        main.add( new ContentSection( BundleSupport.getLabel("SectionSamples"), //NOI18N
                SwingConstants.NORTH_EAST, c, false ),
                new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.SOUTHWEST,
                GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        c = new PluginsPanel();
        main.add( new ContentSection( BundleSupport.getLabel("SectionPlugins"), //NOI18N
                SwingConstants.SOUTH_WEST, c, false ),
                new GridBagConstraints(0,1,1,1,1.0,0.0,GridBagConstraints.NORTHEAST,
                GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        c = new LearnMore();
        learnMoreSection = new ContentSection( BundleSupport.getLabel( "SectionLearnMore" ), //NOI18N
                SwingConstants.SOUTH_EAST, c, false );
        main.add( learnMoreSection,
                new GridBagConstraints(1,1,1,1,1.0,0.0,GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        main.add( new JLabel(),
                new GridBagConstraints(0,2,2,1,0.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.VERTICAL,new Insets(0,0,0,0),0,0) );
        
        main.add( new BottomBar(false),
                new GridBagConstraints(0,3,2,1,1.0,0.0,GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0) );
    }

    protected Point getTopStripOrigin() {
        Point p;
        if( null == getStartedSection ) {
            p = new Point(0,0);
        } else {
            Rectangle r = getStartedSection.getTitleBounds();
            p = r.getLocation();
            p.y += r.getHeight();
            p = SwingUtilities.convertPoint( getStartedSection, p, this );
        }
        return p;
    }

    protected Point getMiddleStripOrigin() {
        Point p;
        if( null == learnMoreSection ) {
            p = new Point(0,0);
        } else {
            Rectangle r = learnMoreSection.getTitleBounds();
            p = r.getLocation();
            p.y += r.getHeight();
            p.x += r.getWidth();
            p = SwingUtilities.convertPoint( learnMoreSection, p, this );
        }
        return p;
    }
}
