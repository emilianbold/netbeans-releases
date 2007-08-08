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
package org.netbeans.modules.versioning.diff;

import org.netbeans.api.diff.Difference;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

/**
 * @author Maros Sandor
 */
class DiffActionTooltipWindow implements AWTEventListener {

    private static final int SCREEN_BORDER = 20;
    
    private JWindow actionsWindow;
    private JWindow contentWindow;

    private final DiffSidebar       master;    
    private final Difference        diff;

    public DiffActionTooltipWindow(DiffSidebar master, Difference diff) {
        this.master = master;
        this.diff = diff;
        Window w = SwingUtilities.windowForComponent(master.getTextComponent());
        actionsWindow = new JWindow(w);
        if (diff.getType() != Difference.ADD) {
            contentWindow = new JWindow(w);
        }
    }

    DiffSidebar getMaster() {
        return master;
    }

    public void show(Point location) {
        DiffTooltipActionsPanel tp = new DiffTooltipActionsPanel(this, diff);
        actionsWindow.add(tp);
        actionsWindow.pack();
        actionsWindow.setLocation(location);

        if (contentWindow != null) {
            DiffTooltipContentPanel cp = new DiffTooltipContentPanel(master.getTextComponent(), master.getMimeType(), diff);
            contentWindow.add(cp);
            contentWindow.pack();
            Dimension dim = contentWindow.getSize();
                        
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
            
            contentWindow.setSize(dim);

            contentWindow.setLocation(location.x, location.y + actionsWindow.getHeight() - 1);  // slight visual adjustment
            contentWindow.setVisible(true);
        }

        actionsWindow.setVisible(true);
        
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
    }

    public void eventDispatched(AWTEvent event) {
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            onClick(event);
/*
        } else if (event.getID() == KeyEvent.KEY_PRESSED) {
            if (((KeyEvent) event).getKeyCode() == KeyEvent.VK_ESCAPE) {
                shutdown();
            }
*/
        }
    }

    private void onClick(AWTEvent event) {
        Component component = (Component) event.getSource();
        Window w = SwingUtilities.windowForComponent(component);
        if (w != actionsWindow && (contentWindow == null || w != contentWindow)) shutdown();
    }

    void shutdown() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        actionsWindow.dispose();
        if (contentWindow != null) contentWindow.dispose();
    }
}
