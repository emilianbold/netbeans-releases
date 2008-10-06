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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.mobility.svgcore.view.svg;

import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Pavel Benes
 * @author ads
 */
public abstract class SVGImagePanel extends JPanel {
    public static final int CROSS_SIZE = 10;

    private final JComponent imagePanel;
    
    public SVGImagePanel(JComponent imagePanel) {
        this.imagePanel = imagePanel;
        setLayout( new CenteredLayoutManager());
        add(imagePanel);
        setDoubleBuffered(true);
        canPaint = new AtomicBoolean( true );
        myErrorComponent = new JLabel( NbBundle.getMessage(SVGImagePanel.class, 
                "ERR_UnableRender")); 
        myErrorComponent.validate();
    }
    
    public void setTryPaint(){
        if ( !canPaint.getAndSet( true ) ) {
            System.gc();
            remove( myErrorComponent );
            setLayout( new CenteredLayoutManager() );
            add( imagePanel );
        }
    }
    
    protected void paintChildren(Graphics g) {
        if (canPaint.get()) {
            try {
                doPaintChildren(g);
            }
            catch (OutOfMemoryError e) {
                canPaint.set( false );
                remove( imagePanel );
                setLayout( new LabelLayout() );
                add(  myErrorComponent );
            }
        }
        else {
            super.paintChildren(g);
        }
    }

    private void doPaintChildren( Graphics g ) {
        super.paintChildren(g);
        int xOff = imagePanel.getX();
        int yOff = imagePanel.getY();

        g.setColor(Color.BLACK);
        int w = imagePanel.getWidth(),
            h = imagePanel.getHeight();

        drawCross( g, xOff - 1, yOff -1);
        drawCross( g, xOff - 1, yOff + h + 1);
        drawCross( g, xOff + w + 1, yOff -1);
        drawCross( g, xOff + w + 1, yOff + h + 1);

        Rectangle2D clip = new Rectangle2D.Float( xOff, yOff, w, h);
        Rectangle visible = getVisibleRect();
        if (visible != null) {
            clip = visible.createIntersection(clip);
        }
        g.setClip( clip);
        paintPanel(g, xOff, yOff);
    }   

    protected abstract void paintPanel(Graphics g, int x, int y);

    private static void drawCross(Graphics g, int x, int y) {
        g.drawLine( x - CROSS_SIZE, y, x + CROSS_SIZE, y);
        g.drawLine( x, y - CROSS_SIZE, x, y + CROSS_SIZE);                
    }     
    
    private class CenteredLayoutManager implements LayoutManager {
        
        public void addLayoutComponent(String name, Component comp) {
            assert imagePanel.equals(comp);
        }

        public Dimension preferredLayoutSize(Container parent) {
            return imagePanel.getSize();
        }

        public Dimension minimumLayoutSize(Container parent) {
            return imagePanel.getSize();
        }

        public void layoutContainer(Container parent) {
            imagePanel.setLocation( (parent.getWidth() - imagePanel.getWidth()) / 2,
                    (parent.getHeight() - imagePanel.getHeight()) / 2);
        }

        public void removeLayoutComponent(Component comp) {
            assert imagePanel.equals(comp);
        }            
    };
    
    private class LabelLayout extends FlowLayout {

        private static final long serialVersionUID = 842591224720135361L;

        public void layoutContainer(Container parent) {
            super.layoutContainer( parent );
            Point point = myErrorComponent.getLocation();
            myErrorComponent.setLocation( point.x ,
                    (int)(parent.getHeight() - 
                            myErrorComponent.getPreferredSize().getHeight()) / 2);
        }
    }
    
    
    private AtomicBoolean canPaint ;
    private JComponent myErrorComponent;
}
