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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.util.Utilities;

/**
 *
 * @author Dan
 */
public class ClickableIcon extends JPanel implements MouseListener {

    private static final int STATE_NORMAL = 0;
    private static final int STATE_FOCUSED = 1;
    private static final int STATE_PRESSED = 2;
    
    private JLabel label = new JLabel();
    
    private ImageIcon normalIcon;
    private ImageIcon focusedIcon;
    private ImageIcon pressedIcon;
    
    private int state;
    
    ClickableIcon(ImageIcon normal, ImageIcon focused, ImageIcon pressed) {
        this.normalIcon = normal;
        this.focusedIcon = focused;
        this.pressedIcon = pressed;
        
        state = STATE_NORMAL;
        
        label.setIcon(normal); // [TODO] compute the initial state
        add(label);
        addMouseListener(this);
    }
    
    ClickableIcon() {
        this(new ImageIcon(Utilities.loadImage(
            "org/netbeans/modules/debugger/jpda/resources/package.gif")),
            new ImageIcon(Utilities.loadImage(
            "org/netbeans/modules/debugger/jpda/resources/packageOpen.gif")),
            new ImageIcon(Utilities.loadImage(
            "org/netbeans/modules/debugger/jpda/resources/ExprArguments.gif"))
        );
    }
    
    // **************************************************************************
    // MouseListener
    // **************************************************************************
    
    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        label.setIcon(pressedIcon);
        state = STATE_PRESSED;
    }

    public void mouseReleased(MouseEvent e) {
        if (state == STATE_PRESSED) {
            label.setIcon(focusedIcon);
            state = STATE_FOCUSED;
        }
    }

    public void mouseEntered(MouseEvent e) {
        if ((e.getModifiers() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
            label.setIcon(pressedIcon);
            state = STATE_PRESSED;
        } else {
            label.setIcon(focusedIcon);
            state = STATE_FOCUSED;
        }
    }

    public void mouseExited(MouseEvent e) {
        label.setIcon(normalIcon);
        state = STATE_NORMAL;
    }

}
