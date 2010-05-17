/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.versioning.system.cvss.ui.history;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;

/**
 * Tooltip-like Window.
 * 
 * @author Maros Sandor
 */
class TooltipWindow extends JWindow implements AWTEventListener {

    private static final int SCREEN_BORDER = 20;
    
    private final JComponent content;

    public TooltipWindow(Window parent, JComponent content) {
        super(parent);
        this.content = content;
    }

    public void show(Point location) {
        this.add(content);
        this.pack();
        Dimension dim = this.getSize();
  
        Rectangle screenBounds = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice [] gds = ge.getScreenDevices();
        for (GraphicsDevice device : gds) {
            GraphicsConfiguration gc = device.getDefaultConfiguration();
            screenBounds = gc.getBounds();
            if (screenBounds.contains(location)) break;
        }
        
        if (location.y + dim.height + SCREEN_BORDER > screenBounds.y + screenBounds.height) {
            dim.height = (screenBounds.y + screenBounds.height) - (location.y + SCREEN_BORDER);
        }
        if (location.x + dim.width + SCREEN_BORDER > screenBounds.x + screenBounds.width) {
            dim.width = (screenBounds.x + screenBounds.width) - (location.x + SCREEN_BORDER);  
        }
        this.setSize(dim);
        this.setLocation(location.x, location.y - 1);  // slight visual adjustment
        this.setVisible(true); 
        
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }

    public void eventDispatched(AWTEvent event) {
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            onClick(event);
        } else if (event.getID() == KeyEvent.KEY_PRESSED) {
            if (((KeyEvent) event).getKeyCode() == KeyEvent.VK_ESCAPE) {
                shutdown();
            }
        }
    }

    private void onClick(AWTEvent event) {
        Component component = (Component) event.getSource();
        Window w = SwingUtilities.windowForComponent(component);
        if (w != this) shutdown();
    }

    void shutdown() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        dispose();
    }
}
