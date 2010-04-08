/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author S. Aubrecht
 */
public class ContentSection extends JPanel implements Constants {

    private static final int PANEL_MAX_WIDTH = 800;
    private boolean maxSize;
    private boolean showSeparator;
    private final static Stroke SEPARATOR_STROKE = new BasicStroke(1,
            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[] {1.0f, 1.0f}, 0.0f);

    public ContentSection( String title, JComponent content, boolean showSeparator, boolean maxSize ) {
        this( content, showSeparator, maxSize, 0 );
        JLabel lblTitle = new JLabel( title );
        lblTitle.setFont( SECTION_HEADER_FONT );

        lblTitle.setBorder( BorderFactory.createEmptyBorder(0, 0, 20, 0) );
        lblTitle.setForeground( Utils.getColor( COLOR_SECTION_HEADER ) );
        add( lblTitle, new GridBagConstraints(0,0,1,1,0.0,0.0,
                GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(6,0,0,0),0,0) );
    }

    public ContentSection( JComponent titleComponent, JComponent content, boolean showSeparator, boolean maxSize ) {
        this( content, showSeparator, maxSize, 8 );
        if( null != titleComponent ) {
            add( titleComponent, new GridBagConstraints(0,0,1,1,1.0,0.0,
                    GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(0,0,15,0),0,0) );
        }
    }

    public ContentSection( JComponent content, boolean showSeparator, boolean maxSize ) {
        this( content, showSeparator, maxSize, 0 );
    }

    private ContentSection( JComponent content, boolean showSeparator, boolean maxSize, int leftInsets ) {
        super( new GridBagLayout() );
        setOpaque(false);
        this.maxSize = maxSize;
        this.showSeparator = showSeparator;
        add( content, new GridBagConstraints(0,1,2,1,1.0,1.0,
                GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,leftInsets,0,0),0,0) );
        
        setBorder( BorderFactory.createEmptyBorder(35,35,15,35) );
    }

    @Override
    protected void paintComponent(Graphics g) {
        if( showSeparator ) {
            int height = getHeight();
            Graphics2D g2d = (Graphics2D) g;
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(SEPARATOR_STROKE);
            g.setColor( Utils.getColor( COLOR_SECTION_SEPARATOR ) );
            g.drawLine( 0, 50, 0, height-35 );
            g2d.setStroke(oldStroke);
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
}
