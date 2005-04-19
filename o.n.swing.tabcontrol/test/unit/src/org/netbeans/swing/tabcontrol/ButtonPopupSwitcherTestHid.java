/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import junit.framework.TestCase;
import org.netbeans.swing.popupswitcher.SwitcherTableItem;

/**
 * Convenient IDE tester. Just run and push the button. Move the whole frame to
 * visually check the row per columns computation.
 *
 * @author mkrauskopf
 */
public class ButtonPopupSwitcherTestHid extends TestCase {
    
    private JFrame frame;
    
    private SwitcherTableItem[] items = new SwitcherTableItem[100];
    
    public ButtonPopupSwitcherTestHid(String testName) {
        super(testName);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("Cannot set L&F: " + ex);
        }
    }
    
    protected void setUp() {
        frame = createFrame();
        frame.setVisible(true);
        items[0] = new SwitcherTableItem(new DummyActivable("Something.txt"), "Something.txt", new DummyIcon(Color.BLUE));
        items[1] = new SwitcherTableItem(new DummyActivable("Sometime.txt"), "Sometime.txt", new DummyIcon());
        SwitcherTableItem selectedItem = new SwitcherTableItem(new DummyActivable("Somewhere.txt"), "Somewhere.txt", new DummyIcon(Color.YELLOW), true);
        items[2] = selectedItem;
        items[3] = new SwitcherTableItem(new DummyActivable("AbCd.txt"), "AbCd.txt", new DummyIcon(Color.BLUE));
        items[4] = new SwitcherTableItem(new DummyActivable("Sometime.txt"),
                "Very Very Very Long" +
                " name with a lot of words in its name bla bla bla bla bla bla" +
                " which sould be shortened and should ends with three dots [...]." +
                " Hmmmmm", new DummyIcon());
        items[5] = new SwitcherTableItem(new DummyActivable("Somewhere.txt"), "Somewhere.txt", new DummyIcon(Color.YELLOW));
        Arrays.fill(items, 6, 70, new SwitcherTableItem(new DummyActivable("s2.txt"), "s2.txt", new DummyIcon()));
        items[70] = new SwitcherTableItem(new DummyActivable("Somewhere.txt"), "null icon", null);
        Arrays.fill(items, 71, 90, new SwitcherTableItem(new DummyActivable("s5.txt"), "s5.txt", new DummyIcon()));
        items[90] = new SwitcherTableItem(new DummyActivable("Somewhere.txt"), null, new DummyIcon(Color.BLACK));
        Arrays.fill(items, 91, 100, new SwitcherTableItem(new DummyActivable("q1.txt"), "q1.txt", new DummyIcon(Color.GREEN)));
        Arrays.sort(items);
        // wait until a developer close the frame
        sleepForever();
    }
    
    public void testFake() {
        // needed to "run" this class
    }
    
    private JFrame createFrame() {
        JFrame frame = new JFrame(getClass().getName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new FlowLayout());
        JButton pBut = new JButton("Popup");
        pBut.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                pButAction(e);
            }
        });
        frame.getContentPane().add(pBut);
        frame.pack();
        frame.setLocationRelativeTo(null);
        return frame;
    }
    
    private void pButAction(MouseEvent e) {
        // create popup with our SwitcherTable
        JComponent c = (JComponent) e.getSource();
        Point p = new Point(c.getWidth(), c.getHeight());
        SwingUtilities.convertPointToScreen(p, c);
        if (!ButtonPopupSwitcher.isShown()) {
            ButtonPopupSwitcher.selectItem(c, items, p.x, p.y);
        }
    }
    
    private static class DummyIcon implements Icon {
        Color color;
        private DummyIcon(Color color) {
            this.color = color;
        }
        private DummyIcon() {
            this.color = Color.RED;
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            int left = ((JComponent) c).getInsets().left;
            int top = ((JComponent) c).getInsets().top;
            g.setColor(color);
            g.fillRect(left + 2, top + 2, 12, 12);
            g.setColor(Color.BLACK);
            g.fillRect(left + 4, top + 4, 8, 8);
        }
        
        public int getIconWidth() {
            return 16;
        }
        
        public int getIconHeight() {
            return 16;
        }
    }
    
    /**
     * Activable tester class.
     */
    private static class DummyActivable implements SwitcherTableItem.Activable {
        String dummyName;
        private DummyActivable(String name) {
            this.dummyName = name;
        }
        public void activate() {
            System.out.println("MK> Activating \"" + dummyName + "\"....");
        }
    }

    
    private void sleep() {
        sleep(12000);
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void sleepForever() {
        boolean dumb = true;
        while(dumb) {
            sleep(60000);
        }
    }
}
