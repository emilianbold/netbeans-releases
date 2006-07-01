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
package org.netbeans.swing.tabcontrol.plaf;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/** Basic chiclet demo, for use in write-your-own-ui-delegate demo at JavaOne 2004 */

public class ChicletDemo {
    public static void main (String[] ignored) {
        new TestFrame().setVisible(true);
    }


    private static class TestFrame extends JFrame implements WindowListener {
        private GenericGlowingChiclet thing = new GenericGlowingChiclet();

        public TestFrame() {
            addWindowListener(this);
            setBounds(20, 20, 200, 80);
        }


        public void paint(Graphics g) {
            super.paint(g);
            ColorUtil.setupAntialiasing(g);
//            thing.setArcs(20, 20, 20, 20);
            thing.setArcs(0.5f, 0.5f, 0.5f, 0.5f);
            thing.setNotch(true, false);
    Color[] rollover = new Color[]{
        new Color(222, 222, 227), new Color(220, 238, 255), new Color(190, 247, 255),
        new Color(205, 205, 205)};

//            thing.setState(thing.STATE_ACTIVE | thing.STATE_SELECTED | thing.STATE_);
        thing.setColors(rollover[0], rollover[1], rollover[2], rollover[3]);
            thing.setAllowVertical(true);
            thing.setBounds(25, 25, getWidth() - 120, getHeight() - 40);
            thing.draw((Graphics2D) g);
        }

        public void windowActivated(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
        }
    }}