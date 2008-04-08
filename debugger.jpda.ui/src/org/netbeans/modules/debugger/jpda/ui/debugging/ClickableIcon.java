/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.debugger.jpda.ui.debugging;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author Dan
 */
public class ClickableIcon extends JPanel implements MouseListener {

    private JLabel label = new JLabel();
    
    private ImageIcon normalIcon;
    private ImageIcon focusedIcon;
    
    ClickableIcon(ImageIcon normal, ImageIcon focused) {
        this.normalIcon = normal;
        this.focusedIcon = focused;
        label.setIcon(normal); // [TODO] compute the initial state
        add(label);
        addMouseListener(this);
    }
    
    ClickableIcon() {
        this(new ImageIcon(Utilities.loadImage(
            "org/netbeans/modules/debugger/jpda/resources/package.gif")),
            new ImageIcon(Utilities.loadImage(
            "org/netbeans/modules/debugger/jpda/resources/packageOpen.gif"))
        );
    }
    
//    @Override
//    protected void processMouseMotionEvent(MouseEvent e) {
//        super.processMouseMotionEvent(e);
//        final MouseEvent evt = e;
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                int id = evt.getID();
//                switch(id) {
//                    case MouseEvent.MOUSE_ENTERED:
//                        setIcon(focused);
//                    break;
//                    case MouseEvent.MOUSE_EXITED:
//                        setIcon(normal);
//                    break;
//                    case MouseEvent.MOUSE_MOVED:
//                        Rectangle rect = getBounds();
//                        int sx = evt.getX();
//                        int sy = evt.getY();
//                        boolean isInside = sx > 0 && sx < rect.getWidth() && sy > 0 && sy < rect.getHeight();
//                        setIcon(isInside ? focused : normal);
//                    break;
//                }
//            }
//        });
//    }

    
    // **************************************************************************
    // MouseListener
    // **************************************************************************
    
    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        label.setIcon(focusedIcon);
    }

    public void mouseExited(MouseEvent e) {
        label.setIcon(normalIcon);
    }

}
