 /*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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