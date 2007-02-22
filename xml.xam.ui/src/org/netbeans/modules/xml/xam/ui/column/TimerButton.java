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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.ui.column;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.Timer;

/**
 * A convenience button class which will continue re-firing its action
 * on a timer for as long as the button is depressed. Used for left-right
 * scroll buttons.
 */
public class TimerButton extends JButton implements ActionListener {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    private Timer timer;
    private Image disabledImage;
    private Image enabledImage;
    private int count;

    public TimerButton(Action a) {
        super(a);
    }

    private Timer getTimer() {
        if (timer == null) {
            timer = new Timer(400, this);
            timer.setRepeats(true);
        }
        return timer;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        count++;
        if (count > 5) {
            timer.setDelay(75);
        } else if (count > 2) {
            timer.setDelay(200);
        }
        performAction();
    }

    private void performAction() {
        if (!isEnabled()) {
            stopTimer();
            return;
        }
        getAction().actionPerformed(new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED, getActionCommand()));
    }

    private void startTimer() {
        performAction();
        Timer t = getTimer();
        if (t.isRunning()) {
            return;
        }
        repaint();
        t.setDelay(400);
        t.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
        repaint();
        count = 0;
    }

    protected void processMouseEvent(MouseEvent me) {
        if (isEnabled() && me.getID() == me.MOUSE_PRESSED) {
            startTimer();
        } else if (me.getID() == me.MOUSE_RELEASED) {
            stopTimer();
        } else {
            super.processMouseEvent(me);
        }
    }

    protected void processFocusEvent(FocusEvent fe) {
        super.processFocusEvent(fe);
        if (fe.getID() == fe.FOCUS_LOST) {
            stopTimer();
        }
    }
}
