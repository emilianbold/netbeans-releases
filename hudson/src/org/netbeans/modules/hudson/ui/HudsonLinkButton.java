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

package org.netbeans.modules.hudson.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import org.openide.util.ImageUtilities;

/**
 * Decorated button (as hyperlink)
 *
 * @author S. Aubrecht (original)
 * @author Michal Mocnak
 */
public class HudsonLinkButton extends JButton
        implements MouseListener, FocusListener {
    
    private boolean underline = false;
    
    public HudsonLinkButton(boolean showBullet ) {
        setForeground(new Color(Integer.decode("0x0E1B55").intValue()));
        setBorder( new EmptyBorder(1, 1, 1, 1) );
        setCursor( new Cursor(Cursor.HAND_CURSOR) );
        setHorizontalAlignment( JLabel.LEFT );
        addMouseListener(this);
        setFocusable( true );
        if( showBullet )
            setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/hudson/ui/resources/bullet.png", true));
        
        setMargin( new Insets(0, 0, 0, 0) );
        setBorderPainted( false );
        setFocusPainted( false );
        setRolloverEnabled( true );
        setContentAreaFilled( false );
        
        addFocusListener( this );
    }
    
    public void mouseClicked(MouseEvent e) {}
    
    public void mousePressed(MouseEvent e) {}
    
    public void mouseReleased(MouseEvent e) {}
    
    public void mouseEntered(MouseEvent e) {
        underline = true;
        setForeground(new Color(Integer.decode("0x164B7B")));
        repaint();
        onMouseEntered( e );
    }
    
    public void mouseExited(MouseEvent e) {
        underline = false;
        setForeground(new Color(Integer.decode("0x0E1B55")));
        repaint();
        onMouseExited( e );
    }
    
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = org.netbeans.modules.hudson.util.Utilities.prepareGraphics(g);
        
        super.paintComponent(g2);
        
        Dimension size = getSize();
        if( hasFocus() ) {
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0, new float[] {0, 2}, 0));
            g2.setColor(new Color(Integer.decode("0x000000")));
            g2.drawRect( 0, 0, size.width - 1, size.height - 1 );
        }
    }
    
    public void focusLost(FocusEvent e) {}
    
    public void focusGained(FocusEvent e) {
        Rectangle rect = getBounds();
        scrollRectToVisible( rect );
    }
    
    protected void onMouseExited(MouseEvent e) {}
    
    protected void onMouseEntered(MouseEvent e) {}
    
    public void paint(Graphics g) {
        if (!isEnabled())
            return;
        
        super.paint(g);
        if( underline ) {
            Font f = getFont();
            FontMetrics fm = getFontMetrics(f);
            int iconWidth = 0;
            if( null != getIcon() ) {
                iconWidth = getIcon().getIconWidth()+getIconTextGap();
            }
            int x1 = iconWidth;
            int y1 = fm.getHeight();
            int x2 = fm.stringWidth(getText()) + iconWidth;
            if( getText().length() > 0 )
                g.drawLine(x1, y1, x2, y1);
        }
    }
}