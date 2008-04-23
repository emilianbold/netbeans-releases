/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 *
 * The Original Software is the Jemmy library. The Initial Developer of the
 * Original Software is Alexandre Iline. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * ---------------------------------------------------------------------------
 *
 */
package org.netbeans.jemmy.operators;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.plaf.MenuItemUI;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.netbeans.jemmy.util.RegExComparator;

/**
 * A JUnit test for JMenuItemOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JMenuItemOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the menu bar.
     */
    private JMenuBar menuBar;
    
    /**
     * Stores the menu.
     */
    private JMenu menu;
    
    /**
     * Stores the menu item.
     */
    private JMenuItem menuItem;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JMenuItemOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        menuBar = new JMenuBar();
        menu = new JMenu("JMenuOperatorTest");
        menu.setName("JMenuOperatorTest");
        menuItem = new JMenuItem("JMenuItemOperatorTest");
        menuItem.setName("JMenuItemOperatorTest");
        menuBar.add(menu);
        menu.add(menuItem);
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
        TestSuite suite = new TestSuite(JMenuItemOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
    }

    /**
     * Test findJMenuItem method.
     */
    public void testFindJMenuItem() {
        frame.setVisible(true);

        JFrameOperator operator = new JFrameOperator();
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);

        operator1.pushMenu("JMenuOperatorTest", "|");
        
        JMenuItem menuItem1 = JMenuItemOperator.findJMenuItem(frame, new NameComponentChooser("JMenuItemOperatorTest"));
        assertNotNull(menuItem1);

        operator1.pushMenu("JMenuOperatorTest", "|");
        
        JMenuItem menuItem2 = JMenuItemOperator.findJMenuItem(frame, "JMenuItemOperatorTest", false, false);
        assertNotNull(menuItem2);
    }

    /**
     * Test waitJMenuItem method.
     */
    public void testWaitJMenuItem() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);

        operator1.pushMenuNoBlock("JMenuOperatorTest", "|");
        
        JMenuItem menuItem1 = JMenuItemOperator.waitJMenuItem(frame, new NameComponentChooser("JMenuItemOperatorTest"));
        assertNotNull(menuItem1);

        operator1.pushMenuNoBlock("JMenuOperatorTest", "|");
        
        JMenuItem menuItem2 = JMenuItemOperator.waitJMenuItem(frame, "JMenuItemOperatorTest", false, false);
        assertNotNull(menuItem2);
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        operator1.getDump();
    }

    /**
     * Test addMenuDragMouseListener method.
     */
    public void testAddMenuDragMouseListener() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        MenuDragMouseListener listener = new MenuDragMouseListenerTest();
        operator1.addMenuDragMouseListener(listener);
        operator1.removeMenuDragMouseListener(listener);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class MenuDragMouseListenerTest implements MenuDragMouseListener {
        public void menuDragMouseEntered(MenuDragMouseEvent e) {
        }

        public void menuDragMouseExited(MenuDragMouseEvent e) {
        }

        public void menuDragMouseDragged(MenuDragMouseEvent e) {
        }

        public void menuDragMouseReleased(MenuDragMouseEvent e) {
        }
    }

    /**
     * Test addMenuKeyListener method.
     */
    public void testAddMenuKeyListener() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        MenuKeyListenerTest listener = new MenuKeyListenerTest();
        operator1.addMenuKeyListener(listener);
        operator1.removeMenuKeyListener(listener);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class MenuKeyListenerTest implements MenuKeyListener {
        public void menuKeyTyped(MenuKeyEvent e) {
        }

        public void menuKeyPressed(MenuKeyEvent e) {
        }

        public void menuKeyReleased(MenuKeyEvent e) {
        }
    }

    /**
     * Test getAccelerator method.
     */
    public void testGetAccelerator() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        operator1.setAccelerator(KeyStroke.getKeyStroke('a'));
        operator1.getAccelerator();
    }

    /**
     * Test getComponent method.
     */
    public void testGetComponent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        operator1.getComponent();
    }

    /**
     * Test getSubElements method.
     */
    public void testGetSubElements() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        operator1.getSubElements();
    }

    /**
     * Test isArmed method.
     */
    public void testIsArmed() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        operator1.setArmed(true);
        operator1.isArmed();
    }

    /**
     * Test menuSelectionChanged method.
     */
    public void testMenuSelectionChanged() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        operator1.menuSelectionChanged(true);
    }

    /**
     * Test processKeyEvent method.
     */
    public void testProcessKeyEvent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        operator1.processKeyEvent(new KeyEvent(frame, 0, 0, 0, 0), null, null);
    }

    /**
     * Test processMenuDragMouseEvent method.
     */
    public void testProcessMenuDragMouseEvent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        operator1.processMenuDragMouseEvent(new MenuDragMouseEvent(frame, 0, 0, 0, 0, 0, 0, false, null, null));
    }

    /**
     * Test processMenuKeyEvent method.
     */
    public void testProcessMenuKeyEvent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        operator1.processMenuKeyEvent(new MenuKeyEvent(frame, 0, 0, 0, 0, 'a', null, null));
    }

    /**
     * Test processMouseEvent method.
     */
    public void testProcessMouseEvent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        operator1.processMouseEvent(new MouseEvent(frame, 0, 0, 0, 0, 0, 0, false), null, null);
    }

    /**
     * Test setUI method.
     */
    public void testSetUI() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        operator1.setUI(new MenuItemUITest());
        assertNotNull(operator1.getUI());
    }
    
    /**
     * Inner class needed for testing.
     */
    public class MenuItemUITest extends MenuItemUI {
    }

    /**
     * Test prepareToClick method.
     */
    public void testPrepareToClick() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        operator1.prepareToClick();
    }

    /**
     * Test getMenuItems method.
     */
    public void testGetMenuItems() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        operator1.getMenuItems(menu, operator.getEnvironmentOperator());
        operator1.getMenuItems((MenuElement) menu, operator.getEnvironmentOperator());
    }

    /**
     * Test createChoosers method.
     */
    public void testCreateChoosers() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuItemOperator operator1 = new JMenuItemOperator(operator);
        assertNotNull(operator1);
        
        String[] names = new String[1];
        names[0] = "Hello";
        operator1.createChoosers(names, new RegExComparator());
    }
}
