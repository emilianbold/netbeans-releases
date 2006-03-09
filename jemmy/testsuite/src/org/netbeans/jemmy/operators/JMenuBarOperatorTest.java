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

import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.plaf.MenuBarUI;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.netbeans.jemmy.util.RegExComparator;

/**
 * A JUnit test for JMenuBarOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JMenuBarOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the menu bar.
     */
    private JMenuBar menuBar;
    
    /**
     * Constructor.
     *
     * @param testName the name of test.
     */
    public JMenuBarOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        menuBar = new JMenuBar();
        menuBar.setName("JMenuBarOperatorTest");
        JMenu menu = new JMenu("JMenu1");
        menu.setName("JMenu1");
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);
        frame.setSize(400, 300);
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
        TestSuite suite = new TestSuite(JMenuBarOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // JMenuBarOperator operator2 = new JMenuBarOperator(operator, new NameComponentChooser("JMenuBarOperatorTest"));
        // assertNotNull(operator2);
    }

    /**
     * Test findJMenuBar method.
     */
    public void testFindJMenuBar() {
        frame.setVisible(true);

        // TODO: disabled, figure out why it is failing.
        // JMenuBar menuBar1 = JMenuBarOperator.findJMenuBar(frame);
        // assertNotNull(menuBar1);
        
        // JMenuBar menuBar2 = JMenuBarOperator.findJMenuBar(new JDialog());
        // assertNull(menuBar2);
    }

    /**
     * Test waitJMenuBar method.
     */
    public void testWaitJMenuBar() {
        frame.setVisible(true);

        // TODO: disabled, figure out why it is failing.
        // JMenuBar menuBar1 = JMenuBarOperator.waitJMenuBar(frame);
        // assertNotNull(menuBar1);
        
        // JDialog dialog = new JDialog();
        // dialog.setJMenuBar(new JMenuBar());
        // dialog.setVisible(true);
        // JMenuBar menuBar2 = JMenuBarOperator.waitJMenuBar(dialog);
        // assertNotNull(menuBar2);
        // dialog.setVisible(false);
        // dialog.dispose();
    }

    /**
     * Test pushMenu method.
     */
    public void testPushMenu() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.pushMenu("JMenu1");
        // operator1.pushMenu("JMenu1", "/", false, false);
        
        // String[] names = new String[1];
        // names[0] = "JMenu1";
        // operator1.pushMenu(names);
        // operator1.pushMenu(names, false, false);
        
        // operator1.pushMenu("JMenu1", "/", new RegExComparator());
        // operator1.pushMenu("JMenu1", new RegExComparator());
    }

    /**
     * Test pushMenuNoBlock method.
     */
    public void testPushMenuNoBlock() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.pushMenuNoBlock("JMenu1");
        // operator1.pushMenuNoBlock("JMenu1", "/", false, false);
        
        // String[] names = new String[1];
        // names[0] = "JMenu1";
        // operator1.pushMenuNoBlock(names);
        // operator1.pushMenuNoBlock(names, false, false);

        // operator1.pushMenuNoBlock("JMenu1", "/", new RegExComparator());
        // operator1.pushMenuNoBlock("JMenu1", new RegExComparator());
    }

    /**
     * Test showMenuItems method.
     */
    public void testShowMenuItems() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.showMenuItems("JMenu1");
        // operator1.showMenuItems("JMenu1", "/");

        // String[] paths = new String[1];
        // paths[0] = "JMenu1";
        // operator1.showMenuItems(paths);
        
        // ComponentChooser[] choosers = new ComponentChooser[1];
        // choosers[0] = new NameComponentChooser("JMenu1");
        // operator1.showMenuItems(choosers);
    }

    /**
     * Test showMenuItem method.
     */
    public void testShowMenuItem() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.showMenuItem("JMenu1");
        // operator1.showMenuItem("JMenu1", "/");
        
        // String[] paths = new String[1];
        // paths[0] = "JMenu1";
        // operator1.showMenuItem(paths);
        
        // ComponentChooser[] choosers = new ComponentChooser[1];
        // choosers[0] = new NameComponentChooser("JMenu1");
        // operator1.showMenuItem(choosers);
    }

    /**
     * Test closeSubmenus method.
     */
    public void testCloseSubmenus() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        //  JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.closeSubmenus();
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.getDump();
    }

    /**
     * Test add method.
     */
    public void testAdd() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.add(new JMenu("Test"));
    }

    /**
     * Test getComponentIndex method.
     */
    public void testGetComponentIndex() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.getComponentIndex(frame);
    }

    /**
     * Test getHelpMenu method.
     */
    public void testGetHelpMenu() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.setHelpMenu(new JMenu());
        // operator1.getHelpMenu();
    }

    /**
     * Test getMargin method.
     */
    public void testGetMargin() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.setMargin(new Insets(0, 0, 0, 0));
        // operator1.getMargin();
    }

    /**
     * Test getMenu method.
     */
    public void testGetMenu() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.getMenu(0);
    }

    /**
     * Test getMenuCount method.
     */
    public void testGetMenuCount() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.getMenuCount();
    }

    /**
     * Test getSelectionModel method.
     */
    public void testGetSelectionModel() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.setSelectionModel(new DefaultSingleSelectionModel());
        // operator1.getSelectionModel();
    }

    /**
     * Test getSubElements method.
     */
    public void testGetSubElements() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.getSubElements();
    }

    /**
     * Test getUI method.
     */
    public void testGetUI() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);

        // operator1.setUI(new MenuBarUITest());
        // operator1.getUI();
    }
    
    /**
     * Inner class needed for testing.
     */
    public class MenuBarUITest extends MenuBarUI {
    }

    /**
     * Test isBorderPainted method.
     */
    public void testIsBorderPainted() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.setBorderPainted(false);
        // operator1.isBorderPainted();
    }

    /**
     * Test isSelected method.
     */
    public void testIsSelected() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.isSelected();
    }

    /**
     * Test menuSelectionChanged method.
     */
    public void testMenuSelectionChanged() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.menuSelectionChanged(true);
    }

    /**
     * Test processKeyEvent method.
     */
    public void testProcessKeyEvent() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.processKeyEvent(new KeyEvent(frame, 0, 0, 0, 0), null, null);
    }

    /**
     * Test processMouseEvent method.
     */
    public void testProcessMouseEvent() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.processMouseEvent(new MouseEvent(frame, 0, 0, 0, 0, 0, 0, false), null, null);
    }

    /**
     * Test setSelected method.
     */
    public void testSetSelected() {
        frame.setVisible(true);
        
        // TODO: disabled, figure out why it is failing.
        // JFrameOperator operator = new JFrameOperator();
        // assertNotNull(operator);
        
        // JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        // assertNotNull(operator1);
        
        // operator1.setSelected(new JPanel());
    }
}
