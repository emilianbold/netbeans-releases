/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.welcome.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.ContentSection;

/**
 * 'Welcome' tab of the Start Page
 * 
 * @author S. Aubrecht
 */
class WelcomeTab extends AbstractTab {
    
    private ContentSection getStartedSection;
    private ContentSection learnMoreSection;
    private JComponent bottomBar;

    protected void buildContent() {
        JPanel main = new JPanel( new GridBagLayout() );
        main.setOpaque( false );
        add( main, BorderLayout.CENTER );
        
        JPanel upperSpace = new Stripe( true );
        upperSpace.setBackground( Color.red );
        main.add( upperSpace,
                new GridBagConstraints(0,0,2,1,1.0,1.0,GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        JComponent c = new GetStarted();
        getStartedSection = new ContentSection( BundleSupport.getLabel("SectionGetStarted"),  //NOI18N
                SwingConstants.NORTH_WEST, c, false );
        main.add( getStartedSection,
                new GridBagConstraints(0,1,1,1,1.0,0.0,GridBagConstraints.SOUTHEAST,
                GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        c = new Samples();
        main.add( new ContentSection( BundleSupport.getLabel("SectionSamples"), //NOI18N
                SwingConstants.NORTH_EAST, c, false ),
                new GridBagConstraints(1,1,1,1,1.0,0.0,GridBagConstraints.SOUTHWEST,
                GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        c = new PluginsPanel();
        main.add( new ContentSection( BundleSupport.getLabel("SectionPlugins"), //NOI18N
                SwingConstants.SOUTH_WEST, c, false ),
                new GridBagConstraints(0,2,1,1,1.0,0.0,GridBagConstraints.NORTHEAST,
                GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        c = new LearnMore();
        learnMoreSection = new ContentSection( BundleSupport.getLabel( "SectionLearnMore" ), //NOI18N
                SwingConstants.SOUTH_EAST, c, false );
        main.add( learnMoreSection,
                new GridBagConstraints(1,2,1,1,1.0,0.0,GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        bottomBar = new BottomBar();
        main.add( bottomBar,
                new GridBagConstraints(0,3,2,1,1.0,0.0,GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,new Insets(0,0,2,0),0,0) );
        
        JPanel bottomSpace = new Stripe( false );
        bottomSpace.setBackground( Color.red );
        main.add( bottomSpace,
                new GridBagConstraints(0,4,2,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
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

    protected Point getBottomStripOrigin() {
        Point p;
        if( null == bottomBar ) {
            p = new Point(0,0);
        } else {
            Rectangle r = bottomBar.getBounds();
            p = r.getLocation();
            p.y += r.getHeight();
            p.x += r.getWidth();
        }
        return p;
    }
}
