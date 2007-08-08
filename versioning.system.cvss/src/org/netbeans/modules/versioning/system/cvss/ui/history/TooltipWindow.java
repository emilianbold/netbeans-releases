/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
