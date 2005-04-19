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

package org.netbeans.core.windows.view.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;
import junit.framework.TestCase;
import org.netbeans.swing.popupswitcher.SwitcherTableItem;


/**
 * Convenient IDE tester. Tests DocumentSwitcherTable. Just run and play with
 * Ctrl+Tab and Ctrl+Shift+Tab keys.
 *
 * @author mkrauskopf
 */
public class KeyboardPopupSwitcherTestHid extends TestCase
        implements KeyEventDispatcher {
    
    private JFrame frame;
    private SwitcherTableItem[] items = new SwitcherTableItem[100];
    
    public KeyboardPopupSwitcherTestHid(String testName) {
        super(testName);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("Cannot set L&F: " + ex);
        }
    }
    
    protected void setUp() {
        KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        keyboardFocusManager.addKeyEventDispatcher(this);
        frame = createFrame();
        frame.setVisible(true);
        
        items[0] = new SwitcherTableItem(new DummyActivable("Something.txt"), "Something.txt", new DummyIcon(Color.BLUE));
        items[1] = new SwitcherTableItem(new DummyActivable("Sometime.txt"), "Sometime.txt", new DummyIcon());
        items[2] = new SwitcherTableItem(new DummyActivable("Somewhere.txt"), "Somewhere.txt", new DummyIcon(Color.YELLOW));
        items[3] = new SwitcherTableItem(new DummyActivable("Something.txt"), "Something.txt", new DummyIcon(Color.BLUE));
        items[4] = new SwitcherTableItem(new DummyActivable("Sometime.txt"),
                "Very Very Very Long" +
                " name with a lot of words in its name bla bla bla bla bla bla" +
                " which sould be shortened and should ends with three dots [...]." +
                " Hmmmmm", new DummyIcon());
        items[5] = new SwitcherTableItem(new DummyActivable("Somewhere.txt"), "Somewhere.txt", new DummyIcon(Color.YELLOW));
        Arrays.fill(items, 6, 70, new SwitcherTableItem(new DummyActivable("s1.txt"), "s1.txt", new DummyIcon()));
        items[70] = new SwitcherTableItem(new DummyActivable("Somewhere.txt"), "null icon", null);
        Arrays.fill(items, 71, 90, new SwitcherTableItem(new DummyActivable("s1.txt"), "s1.txt", new DummyIcon()));
        items[90] = new SwitcherTableItem(new DummyActivable("Somewhere.txt"), null, new DummyIcon(Color.BLACK));
        Arrays.fill(items, 91, 100, new SwitcherTableItem(new DummyActivable("s1.txt"), "s1.txt", new DummyIcon(Color.GREEN)));
        
        // wait until a developer close the frame
        sleepForever();
        keyboardFocusManager.removeKeyEventDispatcher(this);
    }
    
    public void testFake() {
        // needed to "run" this class
    }
    
    private JFrame createFrame() {
        JFrame frame = new JFrame(getClass().getName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(600, 400));
        frame.setLocationRelativeTo(null);
        return frame;
    }
    
    public boolean dispatchKeyEvent(java.awt.event.KeyEvent e) {
        boolean isCtrl = e.getModifiers() == InputEvent.CTRL_MASK;
        boolean isCtrlShift = e.getModifiers() == (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
        boolean doPopup = (e.getKeyCode() == KeyEvent.VK_TAB) &&
                (isCtrl || isCtrlShift);
        if (doPopup && !KeyboardPopupSwitcher.isShown()) {
            // create popup with our SwitcherTable
            KeyboardPopupSwitcher.selectItem(items, KeyEvent.VK_CONTROL, e.getKeyCode());
            return true;
        }
        
        return false;
    }
    
    /**
     * Dummy icon meant for testing prupose.
     */
    private static class DummyIcon implements Icon {
        Color color;
        private DummyIcon(Color color) {
            this.color = color;
        }
        private DummyIcon() {
            this(Color.RED);
        }
        public int getIconWidth() {
            return 16;
        }
        public int getIconHeight() {
            return 16;
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            int left = ((JComponent) c).getInsets().left;
            int top = ((JComponent) c).getInsets().top;
            g.setColor(color);
            g.fillRect(left + 2, top + 2, 12, 12);
            g.setColor(Color.BLACK);
            g.fillRect(left + 4, top + 4, 8, 8);
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
        sleep(500);
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
            sleep();
        }
    }
}
