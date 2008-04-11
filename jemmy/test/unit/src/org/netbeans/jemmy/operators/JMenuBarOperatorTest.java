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

import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.plaf.MenuBarUI;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
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
        menu.add(new JMenuItem("JMenuItem1"));
        menu.add(new JMenuItem("JMenuItem11"));
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
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuBarOperator operator2 = new JMenuBarOperator(operator, new NameComponentChooser("JMenuBarOperatorTest"));
        assertNotNull(operator2);
    }

    /**
     * Test findJMenuBar method.
     */
    public void testFindJMenuBar() {
        frame.setVisible(true);

        JMenuBar menuBar1 = JMenuBarOperator.findJMenuBar(frame);
        assertNotNull(menuBar1);
        
        JMenuBar menuBar2 = JMenuBarOperator.findJMenuBar(new JDialog());
        assertNull(menuBar2);
    }

    /**
     * Test waitJMenuBar method.
     */
    public void testWaitJMenuBar() {
        frame.setVisible(true);

        JMenuBar menuBar1 = JMenuBarOperator.waitJMenuBar(frame);
        assertNotNull(menuBar1);
        
        JDialog dialog = new JDialog();
        dialog.setJMenuBar(new JMenuBar());
        dialog.setVisible(true);
        JMenuBar menuBar2 = JMenuBarOperator.waitJMenuBar(dialog);
        assertNotNull(menuBar2);
        dialog.setVisible(false);
        dialog.dispose();
    }

    /**
     * Test pushMenu method.
     */
    public void testPushMenu() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.pushMenu("JMenu1");
        operator1.pushMenu("JMenu1", "/", false, false);
        
        String[] names = new String[1];
        names[0] = "JMenu1";
        operator1.pushMenu(names);
        operator1.pushMenu(names, false, false);
        
        operator1.pushMenu("JMenu1", "/", new RegExComparator());
        operator1.pushMenu("JMenu1", new RegExComparator());
    }

    /**
     * Test pushMenuNoBlock method.
     */
    public void testPushMenuNoBlock() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.pushMenuNoBlock("JMenu1");
        operator1.pushMenuNoBlock("JMenu1", "/", false, false);
        
        String[] names = new String[1];
        names[0] = "JMenu1";
        operator1.pushMenuNoBlock(names);
        operator1.pushMenuNoBlock(names, false, false);

        operator1.pushMenuNoBlock("JMenu1", "/", new RegExComparator());
        operator1.pushMenuNoBlock("JMenu1", new RegExComparator());
    }

    /**
     * Test showMenuItems method.
     */
    public void testShowMenuItems() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        // operator1.showMenuItems("JMenuItem1");
        // operator1.showMenuItems("JMenuItem1", "/");

        String[] paths = new String[1];
        paths[0] = "JMenuItem1";
        // operator1.showMenuItems(paths);
        
        ComponentChooser[] choosers = new ComponentChooser[1];
        choosers[0] = new NameComponentChooser("JMenuItem1");
        // operator1.showMenuItems(choosers);
    }

    /**
     * Test showMenuItem method.
     */
    public void testShowMenuItem() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.showMenuItem("JMenu1");
        operator1.showMenuItem("JMenu1", "/");
        
        String[] paths = new String[1];
        paths[0] = "JMenu1";
        operator1.showMenuItem(paths);
        
        ComponentChooser[] choosers = new ComponentChooser[1];
        choosers[0] = new NameComponentChooser("JMenu1");
        operator1.showMenuItem(choosers);
    }

    /**
     * Test closeSubmenus method.
     */
    public void testCloseSubmenus() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.closeSubmenus();
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.getDump();
    }

    /**
     * Test add method.
     */
    public void testAdd() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.add(new JMenu("Test"));
    }

    /**
     * Test getComponentIndex method.
     */
    public void testGetComponentIndex() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.getComponentIndex(frame);
    }

    /**
     * Test getHelpMenu method.
     */
    public void testGetHelpMenu() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.setHelpMenu(new JMenu());
        operator1.getHelpMenu();
    }

    /**
     * Test getMargin method.
     */
    public void testGetMargin() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.setMargin(new Insets(0, 0, 0, 0));
        operator1.getMargin();
    }

    /**
     * Test getMenu method.
     */
    public void testGetMenu() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.getMenu(0);
    }

    /**
     * Test getMenuCount method.
     */
    public void testGetMenuCount() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.getMenuCount();
    }

    /**
     * Test getSelectionModel method.
     */
    public void testGetSelectionModel() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelectionModel(new DefaultSingleSelectionModel());
        operator1.getSelectionModel();
    }

    /**
     * Test getSubElements method.
     */
    public void testGetSubElements() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.getSubElements();
    }

    /**
     * Test getUI method.
     */
    public void testGetUI() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);

        operator1.setUI(new MenuBarUITest());
        operator1.getUI();
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
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.setBorderPainted(false);
        operator1.isBorderPainted();
    }

    /**
     * Test isSelected method.
     */
    public void testIsSelected() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.isSelected();
    }

    /**
     * Test menuSelectionChanged method.
     */
    public void testMenuSelectionChanged() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
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
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.processKeyEvent(new KeyEvent(frame, 0, 0, 0, 0), null, null);
    }

    /**
     * Test processMouseEvent method.
     */
    public void testProcessMouseEvent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.processMouseEvent(new MouseEvent(frame, 0, 0, 0, 0, 0, 0, false), null, null);
    }

    /**
     * Test setSelected method.
     */
    public void testSetSelected() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelected(new JPanel());
    }
    
    /**
     * Test issue #54793.
     */
    public void testIssue54793() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBarOperator operator1 = new JMenuBarOperator(operator);
        assertNotNull(operator1);
        
        JMenuOperator operator2 = new JMenuOperator(operator1, "JMenu1");
        assertNotNull(operator2);
        
        JMenuItemOperator operator3 = operator2.showMenuItem("JMenuItem11", new DefaultStringComparator(true, true));
        assertTrue(operator3.getText().equals("JMenuItem11"));
    }
}
