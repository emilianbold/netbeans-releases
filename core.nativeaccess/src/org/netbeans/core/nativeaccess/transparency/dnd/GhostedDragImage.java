/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * Original file is from http://jna.dev.java.net/
 */
package org.netbeans.core.nativeaccess.transparency.dnd;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.netbeans.core.nativeaccess.transparency.WindowUtils;

/** Provide a ghosted drag image for use during drags where 
 * {@link DragSource#isDragImageSupported} returns false.<p>
 * Its location in screen coordinates may be changed via {@link #move}.<p>  
 * When the image is no longer needed, invoke {@link #dispose}, which
 * hides the graphic immediately, or {@link #returnToOrigin}, which 
 * moves the image to its original location and then disposes it.
 */
public class GhostedDragImage {

    private static final float DEFAULT_ALPHA = .5f;
    private Window dragImage;
    // Initial image position, relative to drag source
    private Point origin;

    /** Create a ghosted drag image, using the given icon.
     * @param icon image to be drawn
     * @param initialScreenLoc initial screen location of the image
     */
    public GhostedDragImage(Component dragSource, final Icon icon, Point initialScreenLoc, 
                            final Point cursorOffset) {
        Window parent = dragSource instanceof Window
            ? (Window)dragSource : SwingUtilities.getWindowAncestor(dragSource);
        // FIXME ensure gc is compatible (X11)
        GraphicsConfiguration gc = parent.getGraphicsConfiguration();
        dragImage = new Window(JOptionPane.getRootFrame(), gc) {
            public void paint(Graphics g) {
                icon.paintIcon(this, g, 0, 0);
            }
            public Dimension getPreferredSize() {
                return new Dimension(icon.getIconWidth(), icon.getIconHeight()); 
            }
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        dragImage.setFocusableWindowState(false);
        dragImage.setName("###overrideRedirect###");
        Icon dragIcon = new Icon() {
            public int getIconHeight() {
                return icon.getIconHeight();
            }
            public int getIconWidth() {
                return icon.getIconWidth();
            }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g = g.create();
                Area area = new Area(new Rectangle(x, y, getIconWidth(), getIconHeight()));
                // X11 needs more of a window due to differences in event processing
                area.subtract(new Area(new Rectangle(x + cursorOffset.x-1, y + cursorOffset.y-1, 3, 3)));
                g.setClip(area);
                icon.paintIcon(c, g, x, y);
                g.dispose();
            }
            
        };
        dragImage.pack();
        WindowUtils.setWindowMask(dragImage, dragIcon);
        WindowUtils.setWindowAlpha(dragImage, DEFAULT_ALPHA);
        move(initialScreenLoc);
        dragImage.setVisible(true);
    }

    /** Set the transparency of the ghosted image. */
    public void setAlpha(float alpha) {
        WindowUtils.setWindowAlpha(dragImage, alpha);
    }
    
    /** Make all ghosted images go away. */
    public void dispose() {
        dragImage.dispose();
        dragImage = null;
    }

    /** Move the ghosted image to the requested location. 
     * @param screenLocation Where to draw the image, in screen coordinates
     */
    public void move(Point screenLocation) {
        if (origin == null) {
            origin = screenLocation;
        }
        dragImage.setLocation(screenLocation.x, screenLocation.y);
    }
    
    private static final int SLIDE_INTERVAL = 1000/30;
    /** Animate the ghosted image returning to its origin. */
    public void returnToOrigin() {
        final Timer timer = new Timer(SLIDE_INTERVAL, null);
        timer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Point location = dragImage.getLocationOnScreen();
                Point dst = new Point(origin);
                int dx = (dst.x - location.x)/2;
                int dy = (dst.y - location.y)/2;
                if (dx != 0 || dy != 0) {
                    location.translate(dx, dy);
                    move(location);
                }
                else {
                    timer.stop();
                    dispose();
                }
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }
}