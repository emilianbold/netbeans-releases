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

package org.netbeans.core.windows.view.ui;

import java.awt.event.ActionEvent;
import org.openide.awt.StatusDisplayer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;

/** The status line component of the main window.
*
* @author Jaroslav Tulach, Jesse Glick
*/
final class StatusLine extends JLabel implements ChangeListener, Runnable {
    private static int SURVIVING_TIME = Integer.getInteger("org.openide.awt.StatusDisplayer.DISPLAY_TIME", 5000);
    
    private StatusDisplayer d = StatusDisplayer.getDefault();
    
    Object clearing;
    
    private class Updater implements ActionListener {
        
        private Object token;

        private long startTime;
        
        private Timer controller;
        
        public Updater(Object token) {
            this.token = token;
        }
        
        public void schedule() {
            controller = new Timer(SURVIVING_TIME, this);
            controller.setDelay(100);
            controller.start();
        }
        
        public void actionPerformed(ActionEvent arg0) {
            if (clearing == token) {
                long t = System.currentTimeMillis();
                if (startTime != 0L) {
                    Color c = UIManager.getColor("Label.foreground");
                    if (c != null) {
                        int alpha = 256 * (int)(t - startTime) / 2000;
                        StatusLine.this.setForeground(
                                new Color(c.getRed(), c.getGreen(), c.getBlue(), 255 - Math.min(255, alpha)));
                    }
                }
                else {
                    startTime = t;
                }
                if (t > startTime + 2000) {
                    controller.stop();
                }
            }
            else {
                controller.stop();
            }
        }
    };
    
    /** Creates a new StatusLine */
    public StatusLine () {
    }
    
    public void addNotify() {
        super.addNotify();
        run();
        d.addChangeListener(this);
    }
    
    public void removeNotify() {
        super.removeNotify();
        d.removeChangeListener(this);
    }
    
    public void updateUI() {
        super.updateUI();
        Font f = UIManager.getFont ("controlFont"); //NOI18N
        if (f == null) {
            f = UIManager.getFont ("Tree.font"); //NOI18N
        }
        if (f != null) {
            setFont(f);
        }
    }

    public void stateChanged(ChangeEvent e) {
        if(SwingUtilities.isEventDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater (this);
        }
    }
    
    /** Called in event queue, should update the status text.
    */
    public void run () {
        String currentMsg = d.getStatusText ();
        setForeground(UIManager.getColor("Label.foreground"));
        setText (currentMsg);
        if (!"".equals(currentMsg) && SURVIVING_TIME != 0) {
            Object token = new Object();
            clearing = token;
            new Updater(token).schedule();
        }
    }
    
    /** #62967: Pref size so that status line is able to shrink as much as possible.
     */
    public Dimension getPreferredSize() {
        return new Dimension(100, 0);
    }
    
    /** #62967: Minimum size so that status line is able to shrink as much as possible.
     */
    public Dimension getMinimumSize() {
        return new Dimension(0, 0);
    }

}
