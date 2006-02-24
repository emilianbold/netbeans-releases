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

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SingleSelectionModel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.PopupMenuUI;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A JUnit test for JPopupMenuOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JPopupMenuOperatorTest extends TestCase {
    /**
     * Stores the frame we use.
     */
    private JFrame frame;
    
    /**
     * Stores the popup menu.
     */
    private JPopupMenu popupMenu;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JPopupMenuOperatorTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        popupMenu = new JPopupMenu("0");
        popupMenu.setName("JPopupMenuOperatorTest");
        popupMenu.add(new JMenuItem("1"));
        popupMenu.add(new JMenuItem("12"));
        popupMenu.add(new JMenuItem("123"));
        frame.getContentPane().add(popupMenu);
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
        popupMenu.setVisible(false);
        popupMenu = null;
    }
    
    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(JPopupMenuOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        JPopupMenuOperator operator2 = new JPopupMenuOperator(popupMenu);
        assertNotNull(operator2);
        
        JFrameOperator operator3 = new JFrameOperator();
        assertNotNull(operator3);
    }
    
    /**
     * Test findJPopupMenu method.
     */
    public void testFindJPopupMenu() {
        frame.setVisible(true);
    }
    
    
    /**
     * Test waitJPopupMenu method.
     */
    public void testWaitJPopupMenu() {
        frame.setVisible(true);
    }
    
    /**
     * Test findJPopupWindow method.
     */
    public void testFindJPopupWindow() {
        frame.setVisible(true);
    }
    
    /**
     * Test waitJPopupWindow method.
     */
    public void testWaitJPopupWindow() {
        frame.setVisible(true);
    }
    
    /**
     * Test callPopup method.
     */
    public void testCallPopup() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
    }
    
    /**
     * Test pushMenu method.
     */
    public void testPushMenu() {
        frame.setVisible(true);
    }
    
    /**
     * Test pushMenuNoBlock method.
     */
    public void testPushMenuNoBlock() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        operator.pushMenuNoBlock("1");
    }
    
    /**
     * Test showMenuItems method.
     */
    public void testShowMenuItems() {
        frame.setVisible(true);
    }
    
    /**
     * Test showMenuItem method.
     */
    public void testShowMenuItem() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        operator.showMenuItem("1");
    }
    
    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        assertNotNull(operator.getDump());
    }
    
    /**
     * Test add method.
     */
    public void testAdd() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        operator.add(new JMenuItem("4"));
    }
    
    /**
     * Test addPopupMenuListener method.
     */
    public void testAddPopupMenuListener() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        PopupMenuListenerTest listener = new PopupMenuListenerTest();
        operator.addPopupMenuListener(listener);
        assertEquals(2, popupMenu.getPopupMenuListeners().length);
        
        operator.removePopupMenuListener(listener);
        assertEquals(1, popupMenu.getPopupMenuListeners().length);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class PopupMenuListenerTest implements PopupMenuListener {
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }
    
    /**
     * Test addSeparator method.
     */
    public void testAddSeparator() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        operator.addSeparator();
    }
    
    /**
     * Test getComponentIndex method.
     */
    public void testGetComponentIndex() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        operator.getComponentIndex(frame);
    }
    
    /**
     * Test getInvoker method.
     */
    public void testGetInvoker() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);

        operator.setInvoker(frame);
        assertNotNull(operator.getInvoker());

        /*
        operator.setInvoker(null);
        assertNull(operator.getInvoker());
         */
    }
    
    /**
     * Test getLabel method.
     */
    public void testGetLabel() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);

        operator.setLabel("12345");
        assertEquals("12345",  operator.getLabel());
    }
    
    /**
     * Test getMargin method.
     */
    public void testGetMargin() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        assertNotNull(operator.getMargin());
    }
    
    /**
     * Test getSelectionModel method.
     */
    public void testGetSelectionModel() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);

        SingleSelectionModel model = new DefaultSingleSelectionModel();
        operator.setSelectionModel(model);
        assertEquals(model, operator.getSelectionModel());
    }
    
    /**
     * Test getSubElements method.
     */
    public void testGetSubElements() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        operator.getSubElements();
    }
    
    /**
     * Test getUI method.
     */
    public void testGetUI() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);

        PopupMenuUITest ui = new PopupMenuUITest();
        operator.setUI(ui);
        assertEquals(ui, operator.getUI());
    }
    
    /**
     * Inner class needed for testing.
     */
    public class PopupMenuUITest extends PopupMenuUI {
    }
    
    /**
     * Test insert method.
     */
    public void testInsert() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        operator.insert(new JButton("Hello"), 0);
    }
    
    /**
     * Test isBorderPainted method.
     */
    public void testIsBorderPainted() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        operator.setBorderPainted(true);
        assertTrue(operator.isBorderPainted());

        operator.setBorderPainted(false);
        assertTrue(!operator.isBorderPainted());
    }
    
    /**
     * Test isLightWeightPopupEnabled method.
     */
    public void testIsLightWeightPopupEnabled() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);

        operator.setLightWeightPopupEnabled(true);
        assertTrue(operator.isLightWeightPopupEnabled());

        operator.setLightWeightPopupEnabled(false);
        assertTrue(!operator.isLightWeightPopupEnabled());
    }
    
    /**
     * Test menuSelectionChanged method.
     */
    public void testMenuSelectionChanged() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        operator.menuSelectionChanged(true);
    }
    
    /**
     * Test pack method.
     */
    public void testPack() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        operator.pack();
    }
    
    /**
     * Test processKeyEvent method.
     */
    public void testProcessKeyEvent() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        operator.processKeyEvent(new KeyEvent(frame, 0, 0, 0, 0), null, null);
    }
    
    /**
     * Test processMouseEvent method.
     */
    public void testProcessMouseEvent() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        operator.processMouseEvent(new MouseEvent(frame, 0, 0, 0, 0, 0, 0, true), null, null);
    }
    
    /**
     * Test setPopupSize method.
     */
    public void testSetPopupSize() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        operator.setPopupSize(100, 100);
        
        operator.setPopupSize(new Dimension(200, 200));
    }
    
    /**
     * Test setSelected method.
     */
    public void testSetSelected() {
        frame.setVisible(true);
        popupMenu.show(frame, 0, 0);
        
        JPopupMenuOperator operator = new JPopupMenuOperator();
        assertNotNull(operator);
        
        operator.setSelected(frame);
    }
}
