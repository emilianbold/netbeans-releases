/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version 1.0
 * (the "License"). You may not use this file except in compliance with the
 * License. A copy of the License is available at http://www.sun.com/.
 *
 * The Original Code is the Jemmy library. The Initial Developer of the
 * Original Code is Alexandre Iline. All Rights Reserved.
 *
 * ---------------------------------------------------------------------------
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 */
package org.netbeans.jemmy.operators;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.netbeans.jemmy.util.RegExComparator;

/**
 * A JUnit test for JMenuOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JMenuOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the menubar.
     */
    private JMenuBar menuBar;
    
    /**
     * Stores the menu.
     */
    private JMenu menu;
    
    /**
     * Stores the menu item.
     */
    private JMenuItem item1;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JMenuOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup for testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        menuBar = new JMenuBar();
        menu = new JMenu("JMenuOperatorTest");
        menu.setName("JMenuOperatorTest");
        item1 = new JMenuItem("Item1");
        item1.setName("Item1");
        menu.add(item1);
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(false);
        frame.dispose();
        frame = null;
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(JMenuOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);
        
        JMenuOperator operator3 = new JMenuOperator(operator1, new NameComponentChooser("JMenuOperatorTest"));
        assertNotNull(operator3);
        
        JMenuOperator operator4 = new JMenuOperator(operator1, "JMenuOperatorTest");
        assertNotNull(operator4);
        
        JMenuOperator operator5 = new JMenuOperator(menu);
        assertNotNull(operator5);
    }

    /**
     * Test findJMenu method.
     */
    public void testFindJMenu() {
        frame.setVisible(true);
        
        JMenu menu1 = JMenuOperator.findJMenu(frame, "JMenuOperatorTest", false, false);
        assertNotNull(menu1);
        
        JMenu menu2 = JMenuOperator.findJMenu(frame, new NameComponentChooser("JMenuOperatorTest"));
        assertNotNull(menu2);
    }

    /**
     * Test waitJMenu method.
     */
    public void testWaitJMenu() {
        frame.setVisible(true);
        
        JMenu menu1 = JMenuOperator.waitJMenu(frame, "JMenuOperatorTest", false, false);
        assertNotNull(menu1);
        
        JMenu menu2 = JMenuOperator.waitJMenu(frame, new NameComponentChooser("JMenuOperatorTest"));
        assertNotNull(menu2);
    }

    /**
     * Test pushMenu method
     */
    public void testPushMenu() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        menu.addMenuListener(new PushMenuListener());
        operator2.pushMenu("JMenuOperatorTest");
        operator2.pushMenu("JMenuOperatorTest", "/");
        operator2.pushMenu("JMenuOperatorTest", "/", false, false);
        
        String[] strings = new String[1];
        strings[0] = "JMenuOperatorTest";
        operator2.pushMenu(strings, false, false);
        
        operator2.pushMenu("JMenuOperatorTest", new RegExComparator());
    }

    /**
     * Test pushMenuNoBlock method
     */
    public void testPushMenuNoBlock() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        menu.addMenuListener(new PushMenuListener());
        operator2.pushMenuNoBlock("JMenuOperatorTest");
        operator2.pushMenuNoBlock("JMenuOperatorTest", "/");
        operator2.pushMenuNoBlock("JMenuOperatorTest", "/", false, false);

        String[] strings = new String[1];
        strings[0] = "JMenuOperatorTest";
        operator2.pushMenuNoBlock(strings, false, false);

        operator2.pushMenuNoBlock("JMenuOperatorTest", ",", new RegExComparator());
        operator2.pushMenuNoBlock("JMenuOperatorTest", new RegExComparator());
    }
    
    /**
     * Inner class needed for testing.
     */
    public class PushMenuListener implements MenuListener {
        public void menuSelected(MenuEvent e) {
        }

        public void menuDeselected(MenuEvent e) {
        }

        public void menuCanceled(MenuEvent e) {
        }
    }

    /**
     * Test showMenuItems method
     */
    public void testShowMenuItems() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        menu.addMenuListener(new PushMenuListener());
        // operator2.showMenuItems("JMenuOperatorTest");
        // operator2.showMenuItems("JMenuOperatorTest", "/");

        String[] strings = new String[1];
        strings[0] = "JMenuOperatorTest";
        
        // operator2.showMenuItems(strings);
    }

    /**
     * Test showMenuItem method
     */
    public void testShowMenuItem() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        menu.addMenuListener(new PushMenuListener());
        // operator2.showMenuItem("Item1");
        operator2.showMenuItem("Item1", "/");

        ComponentChooser[] choosers = new ComponentChooser[1];
        choosers[0] = new NameComponentChooser("Item1");
        operator2.showMenuItem(choosers);
        
        String[] strings = new String[1];
        strings[0] = "Item1";
        
        operator2.showMenuItem(strings);
    }

    /**
     * Test getDump method
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        operator2.getDump();
    }

    /**
     * Test add method
     */
    public void testAdd() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        operator2.add(new JMenuItem("JMenuOperatorTest1"));;
        operator2.add("JMenuOperatorTest2");
        operator2.add(new ActionTest());
    }

    /**
     * Test addMenuListener method
     */
    public void testAddMenuListener() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        MenuListenerTest listener = new MenuListenerTest();
        operator2.addMenuListener(listener);
        assertEquals(1, menu.getMenuListeners().length);
        
        operator2.removeMenuListener(listener);
        assertEquals(0, menu.getMenuListeners().length);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class MenuListenerTest implements MenuListener {
        public void menuSelected(MenuEvent e) {
        }

        public void menuDeselected(MenuEvent e) {
        }

        public void menuCanceled(MenuEvent e) {
        }
    }

    /**
     * Test addSeparator method
     */
    public void testAddSeparator() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);
        
        operator2.addSeparator();
    }

    /**
     * Test getDelay method
     */
    public void testGetDelay() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        assertEquals(200, operator2.getDelay());
        
        operator2.setDelay(400);
        assertEquals(400, operator2.getDelay());
    }

    /**
     * Test getItem method
     */
    public void testGetItem() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        assertNotNull(operator2.getItem(0));
    }

    /**
     * Test getItemCount method
     */
    public void testGetItemCount() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        assertEquals(1, operator2.getItemCount());
    }

    /**
     * Test getMenuComponent method
     */
    public void testGetMenuComponent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        assertNotNull(operator2.getMenuComponent(0));
    }

    /**
     * Test getMenuComponentCount method
     */
    public void testGetMenuComponentCount() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        assertEquals(1, operator2.getMenuComponentCount());
    }

    /**
     * Test getMenuComponents method
     */
    public void testGetMenuComponents() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        assertNotNull(operator2.getMenuComponents());
    }

    /**
     * Test getPopupMenu method
     */
    public void testGetPopupMenu() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        operator2.getPopupMenu();
    }

    /**
     * Test insert method
     */
    public void testInsert() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        operator2.insert(new JMenuItem("Test"), 0);
        operator2.insert("Testing", 0);
        operator2.insert(new ActionTest(), 0);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class ActionTest implements Action {
        public Object getValue(String key) {
            return null;
        }

        public void putValue(String key, Object value) {
        }

        public void setEnabled(boolean b) {
        }

        public boolean isEnabled() {
            return false;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public void actionPerformed(ActionEvent e) {
        }
    }

    /**
     * Test insertSeparator method
     */
    public void testInsertSeparator() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        operator2.insertSeparator(0);
    }

    /**
     * Test isMenuComponent method
     */
    public void testIsMenuComponent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        operator2.isMenuComponent(frame);
    }

    /**
     * Test isPopupMenuVisible method
     */
    public void testIsPopupMenuVisible() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        assertTrue(!operator2.isPopupMenuVisible());
        
        operator2.setPopupMenuVisible(true);
        assertTrue(operator2.isPopupMenuVisible());
    }

    /**
     * Test isTearOff method
     */
    public void testIsTearOff() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        try {
            operator2.isTearOff();
            fail();
        }
        catch(NullPointerException exception) {
        }
    }

    /**
     * Test isTopLevelMenu method
     */
    public void testIsTopLevelMenu() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        assertTrue(operator2.isTopLevelMenu());
    }

    /**
     * Test remove method
     */
    public void testRemove() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        operator2.remove(new JMenuItem("Test"));
    }

    /**
     * Test setMenuLocation method
     */
    public void testSetMenuLocation() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        operator2.setMenuLocation(0, 1);
    }

    /**
     * Test createDescription method
     */
    public void testCreateDescription() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        ComponentChooser[] choosers = new ComponentChooser[1];
        choosers[0] = new NameComponentChooser("Test");
        operator2.createDescription(choosers);
    }

    /**
     * Test converChoosers method
     */
    public void testConverChoosers() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1);
        assertNotNull(operator2);

        ComponentChooser[] choosers = new ComponentChooser[1];
        choosers[0] = new NameComponentChooser("Test");
        operator2.converChoosers(choosers);
    }
}
