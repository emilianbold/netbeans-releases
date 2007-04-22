/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
package org.netbeans.modules.java.navigation;

import javax.swing.JPanel;
import javax.swing.RootPaneContainer;

import org.openide.windows.WindowManager;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;

/**
 * A simple singleton factory for a popup dialog for
 * hierarchy and members pop up windows.
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
final class ResizablePopup {
    private static Rectangle lastBounds;
    static
    {
        Dimension dimensions = Toolkit.getDefaultToolkit().getScreenSize();
        lastBounds = new Rectangle(((dimensions.width / 2) - 410), ((dimensions.height / 2) - 300), 820, 600);
    }

    private static final WindowListener windowListener = new WindowAdapter() {
        public void windowDeactivated(WindowEvent windowEvent) {
            cleanup(windowEvent.getWindow());
        }

        public void windowClosing(WindowEvent windowEvent) {
            cleanup(windowEvent.getWindow());
        }

        private void cleanup(Window window) {
            window.setVisible(false);
            if (window instanceof RootPaneContainer) {
                ((RootPaneContainer) window).setContentPane(new JPanel());
            }
            window.removeWindowListener(this);
            window.dispose();
        }
    };

    static JDialog getDialog() {
        JDialog dialog = new JDialog(WindowManager.getDefault().getMainWindow(), "", false) {
                    public void setVisible(boolean visible) {
                        boolean wasVisible = isVisible();
                        if (wasVisible && !visible) {
                            lastBounds = getBounds();
                        }
                        super.setVisible(visible);
                    }
                };
        //dialog.setUndecorated(true);
        dialog.setBounds(lastBounds);
        dialog.addWindowListener(windowListener);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        return dialog;
    }
}
