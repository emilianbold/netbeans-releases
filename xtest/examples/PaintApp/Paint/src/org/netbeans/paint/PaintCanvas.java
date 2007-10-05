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

package org.netbeans.paint;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * @author Timothy Boudreau
 */
public class PaintCanvas extends JComponent implements MouseListener, MouseMotionListener {
    
    private int diam = 10;
    private Paint paint = Color.BLUE;
    private BufferedImage backingImage;
    private Point last;
    
    public PaintCanvas() {
        addMouseListener(this);
        addMouseMotionListener(this);
        setBackground(Color.WHITE);
    }
    
    public void setBrush(int diam) {
        this.diam = diam;
    }
    
    public void setDiam(int val) {
        this.diam = val;
    }
    
    public int getDiam() {
        return diam;
    }
    
    public void setPaint(Paint c) {
        this.paint = c;
    }
    
    public Paint getPaint() {
        return paint;
    }
    
    public Color getColor() {
        if (paint instanceof Color) {
            return (Color) paint;
        } else {
            return Color.BLACK;
        }
    }
    
    public void clear() {
        backingImage = null;
        repaint();
    }
    
    public BufferedImage getImage() {
        int width = Math.min(getWidth(), 1600);
        int height = Math.min(getHeight(),1200);
        if (backingImage == null || backingImage.getWidth() != width || backingImage.getHeight() != height) {
            BufferedImage old = backingImage;
            backingImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics g = backingImage.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            if (old != null) {
                ((Graphics2D) backingImage.getGraphics()).drawRenderedImage(old,
                        AffineTransform.getTranslateInstance(0, 0));
            }
        }
        return backingImage;
    }
    
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawRenderedImage(getImage(), AffineTransform.getTranslateInstance(0,0));
    }
    
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        Graphics2D g = getImage().createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(getPaint());
        g.setStroke(new BasicStroke(diam, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (last == null) {
            last = p;
        }
        g.drawLine(last.x, last.y, p.x, p.y);
        repaint(Math.min(last.x, p.x) - diam / 2 - 1,
                Math.min(last.y, p.y) - diam / 2 - 1,
                Math.abs(last.x - p.x) + diam + 2,
                Math.abs(last.y - p.y) + diam + 2);
        last = p;
    }
    
    public void mousePressed(MouseEvent e) {
    }
    
    public void mouseReleased(MouseEvent e) {
    }
    
    public void mouseEntered(MouseEvent e) {
    }
    
    public void mouseExited(MouseEvent e) {
    }
    
    public void mouseDragged(MouseEvent e) {
        mouseClicked(e);
    }
    
    public void mouseMoved(MouseEvent e) {
        last = null;
    }
    
    JComponent createBrushSizeView() {
        return new BrushSizeView();
    }
    
    
    private class BrushSizeView extends JComponent {
        
        public boolean isOpaque() {
            return true;
        }
        
        public void paint(Graphics g) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRect(0,0,getWidth(),getHeight());
            Point p = new Point(getWidth() / 2, getHeight() / 2);
            int half = getDiam() / 2;
            int diam = getDiam();
            g.setColor(getColor());
            g.fillOval(p.x - half, p.y - half, diam, diam);
        }
        
        public Dimension getPreferredSize() {
            return new Dimension(32, 32);
        }
        
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
        
    }
    
}
