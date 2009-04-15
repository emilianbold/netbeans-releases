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

package org.netbeans.modules.welcome.content;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht
 */
public class ContentSection extends JPanel implements Constants {

    private static final int PANEL_MAX_WIDTH = 800;
    private int location;
    private boolean maxSize;
    
    private JLabel lblTitle;
    
    public ContentSection( String title, String imgLabelKey, int location, JComponent content, boolean maxSize ) {
        super( new GridBagLayout() );
        setOpaque(false);
        this.location = location;
        this.maxSize = maxSize;

        if( title.length() > 0 ) {
            lblTitle = new JLabel( title );
            lblTitle.setFont( SECTION_HEADER_FONT );
        } else {
            Image img = ImageUtilities.loadImage(imgLabelKey);
            lblTitle = new JLabel( new ImageIcon(img) );
        }
        
        lblTitle.setBorder( BorderFactory.createEmptyBorder(0, 0, 8, 0) );
        lblTitle.setForeground( Utils.getColor( COLOR_SECTION_HEADER ) );
        add( lblTitle, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0) );
//        add( new JLabel(), new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0) );
        
        add( content, new GridBagConstraints(0,1,2,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        
        setBorder( BorderFactory.createEmptyBorder(8,12,6,12) );
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        g.setColor( Utils.getColor( COLOR_SECTION_HEADER ) );
        switch( location ) {
        case SwingConstants.NORTH_EAST:
            g.drawLine( 0, height-1, width-25, height-1 );
            g.drawLine( 0, 25, 0, height );
            break;
        case SwingConstants.NORTH_WEST:
            g.drawLine( 25, height, width, height );
            g.drawLine( 25, height-1, width, height-1 );
            break;
        case SwingConstants.SOUTH_EAST:
            g.drawLine( 0, 0, 0, height-25 );
            break;
        case SwingConstants.EAST:
            g.drawLine( 0, 25, 0, height );
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
