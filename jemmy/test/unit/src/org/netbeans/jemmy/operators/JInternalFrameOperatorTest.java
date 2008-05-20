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
 */
package org.netbeans.jemmy.operators;



import java.awt.Rectangle;

import java.beans.PropertyVetoException;

import java.util.Hashtable;

import javax.swing.ImageIcon;

import javax.swing.JDesktopPane;

import javax.swing.JFrame;

import javax.swing.JInternalFrame;

import javax.swing.JInternalFrame.JDesktopIcon;

import javax.swing.JLayeredPane;

import javax.swing.JMenuBar;

import javax.swing.JPanel;

import javax.swing.event.InternalFrameEvent;

import javax.swing.event.InternalFrameListener;

import javax.swing.plaf.InternalFrameUI;

import junit.framework.Test;

import junit.framework.TestCase;

import junit.framework.TestSuite;

import org.netbeans.jemmy.operators.JInternalFrameOperator.JDesktopIconOperator;

import org.netbeans.jemmy.operators.JInternalFrameOperator.WrongInternalFrameStateException;

import org.netbeans.jemmy.util.NameComponentChooser;



/**

 * A JUnit test for JInternalFrameOperator.

 *

 * @author Manfred Riem (mriem@netbeans.org)

 * @version $Revision$

 */

public class JInternalFrameOperatorTest extends TestCase {

    /**

     * Stores the frame.

     */

    private JFrame frame;

    

    /**

     * Stores the desktop

     */

    private JDesktopPane desktop;

    

    /**

     * Stores the internal frame.

     */

    private JInternalFrame internalFrame;

    

    /**

     * Constructor.

     *

     * @param testName the name of the test.

     */

    public JInternalFrameOperatorTest(String testName) {

        super(testName);

    }



    /**

     * Setup for testing.

     */

    protected void setUp() throws Exception {

        frame = new JFrame();

        desktop = new JDesktopPane();

        frame.setContentPane(desktop);

        internalFrame = new JInternalFrame("JInternalFrameOperatorTest", true, true, true, true);

        internalFrame.setName("JInternalFrameOperatorTest");

        internalFrame.setSize(100, 100);

        internalFrame.setVisible(true);

        desktop.add(internalFrame);

        frame.setSize(200,200);

        frame.setLocationRelativeTo(null);

    }



    /**

     * Cleanup for testing.

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

        TestSuite suite = new TestSuite(JInternalFrameOperatorTest.class);

        

        return suite;

    }

    

    /**

     * Test constructor.

     */

    public void testConstructor() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);



        JInternalFrameOperator operator3 = new JInternalFrameOperator(operator, new NameComponentChooser("JInternalFrameOperatorTest"));

        assertNotNull(operator3);



        JInternalFrameOperator operator4 = new JInternalFrameOperator(operator, "JInternalFrameOperatorTest");

        assertNotNull(operator4);

    }



    /**

     * Test findJInternalFrame method.

     */

    public void testFindJInternalFrame() {

        frame.setVisible(true);

        

        JInternalFrame internalFrame1 = JInternalFrameOperator.findJInternalFrame(frame, new NameComponentChooser("JInternalFrameOperatorTest"));

        assertNotNull(internalFrame1);



        JInternalFrame internalFrame2 = JInternalFrameOperator.findJInternalFrame(frame, "JInternalFrameOperatorTest", false, false);

        assertNotNull(internalFrame2);

        

        try {

            internalFrame.setIcon(true);

        }

        catch(PropertyVetoException exception) {

        }

        

        JInternalFrame internalFrame3 = JInternalFrameOperator.findJInternalFrame(frame, new NameComponentChooser("JInternalFrameOperatorTest"));

        assertNull(internalFrame3);

        

        JDesktopIcon desktopIcon = new JDesktopIcon(internalFrame);

        internalFrame.setDesktopIcon(desktopIcon);

        JInternalFrame internalFrame4 = JInternalFrameOperator.findJInternalFrame(frame, new NameComponentChooser("JInternalFrameOperatorTest"));

        assertNull(internalFrame4);

    }



    /**

     * Test findJInternalFrameUnder method.

     */

    public void testFindJInternalFrameUnder() {

    }



    /**

     * Test waitJInternalFrame method.

     */

    public void testWaitJInternalFrame() {

        frame.setVisible(true);

        

        JInternalFrame internalFrame1 = JInternalFrameOperator.waitJInternalFrame(frame, new NameComponentChooser("JInternalFrameOperatorTest"));

        assertNotNull(internalFrame1);



        JInternalFrame internalFrame2 = JInternalFrameOperator.waitJInternalFrame(frame, "JInternalFrameOperatorTest", false, false);

        assertNotNull(internalFrame2);

    }



    /**

     * Test iconify method.

     */

    public void testIconify() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        assertTrue(operator2.isIconifiable());

        

        operator2.iconify();

        assertTrue(operator2.isIcon());

        assertTrue(internalFrame.isIcon());

        

        operator2.deiconify();

        assertTrue(!operator2.isIcon());

        assertTrue(!internalFrame.isIcon());

        

        operator2.iconify();

        

        try {

            operator2.iconify();

        }

        catch(WrongInternalFrameStateException exception) {

        }

        

        assertTrue(operator2.isIcon());

        assertTrue(internalFrame.isIcon());



        operator2.deiconify();

        

        try {

            operator2.deiconify();

        }

        catch(WrongInternalFrameStateException exception) {

        }

        

        assertTrue(!operator2.isIcon());

        assertTrue(!internalFrame.isIcon());

    }



    /**

     * Test maximize method.

     */

    public void testMaximize() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);



        operator2.maximize();

        assertTrue(operator2.isMaximum());

        assertTrue(internalFrame.isMaximum());

        

        operator2.demaximize();

        assertTrue(!operator2.isMaximum());

        assertTrue(!internalFrame.isMaximum());

    }



    /**

     * Test move method.

     */

    public void testMove() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.move(100, 100);

        assertEquals(100, operator2.getX());

        assertEquals(100, operator2.getY());

    }



    /**

     * Test resize method.

     */

    public void testResize() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.resize(127,129);

        assertEquals(127, internalFrame.getWidth());

        assertEquals(129, internalFrame.getHeight());

    }



    /**

     * Test activate method.

     */

    public void testActivate() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.activate();

        assertEquals(internalFrame.isSelected(), operator2.isSelected());

    }



    /**

     * Test close method.

     */

    public void testClose() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.close();

        assertEquals(true, operator2.isClosed());

        assertEquals(false, internalFrame.isVisible());

    }



    /**

     * Test scrollToRectangle method.

     */

    public void testScrollToRectangle() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.scrollToRectangle(0, 0, 100, 100);

        operator2.scrollToRectangle(new Rectangle(0, 0, 100, 100));

    }



    /**

     * Test scrollToFrame method.

     */

    public void testScrollToFrame() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.scrollToFrame();

    }



    /**

     * Test getMinimizeButton method.

     */

    public void testGetMinimizeButton() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        JButtonOperator minimizeButtonOperator = operator2.getMinimizeButton();

        assertNotNull(minimizeButtonOperator);

    }



    /**

     * Test getMaximizeButton method.

     */

    public void testGetMaximizeButton() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        JButtonOperator maximizeButtonOperator = operator2.getMaximizeButton();

        assertNotNull(maximizeButtonOperator);

    }



    /**

     * Test getCloseButton method.

     */

    public void testGetCloseButton() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        JButtonOperator closeButtonOperator = operator2.getCloseButton();

        assertNotNull(closeButtonOperator);

    }



    /**

     * Test getTitleOperator method.

     */

    public void testGetTitleOperator() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        ContainerOperator titleOperator = operator2.getTitleOperator();

        assertNotNull(titleOperator);

    }



    /**

     * Test getIconOperator method.

     */

    public void testGetIconOperator() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        JDesktopIconOperator iconOperator = operator2.getIconOperator();

        assertNotNull(iconOperator);

    }



    /**

     * Test waitIcon method.

     */

    public void testWaitIcon() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.waitIcon(false);

        assertTrue(!internalFrame.isIcon());

        

        operator2.iconify();

        operator2.waitIcon(true);

        assertTrue(internalFrame.isIcon());

    }



    /**

     * Test waitMaximum method.

     */

    public void testWaitMaximum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.waitMaximum(false);

        assertTrue(!operator2.isMaximum());

        

        operator2.maximize();

        operator2.waitMaximum(true);

        assertTrue(operator2.isMaximum());

    }



    /**

     * Test getDump method.

     */

    public void testGetDump() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        Hashtable hashtable = operator2.getDump();

        

        operator2.iconify();

        hashtable = operator2.getDump();

        assertEquals("true", hashtable.get(JInternalFrameOperator.IS_RESIZABLE_DPROP));



        operator2.deiconify();

        operator2.maximize();

        hashtable = operator2.getDump();

        assertEquals("false", hashtable.get(JInternalFrameOperator.IS_RESIZABLE_DPROP));

        

        operator2.close();

        hashtable = operator2.getDump();

        assertEquals("false", hashtable.get(JInternalFrameOperator.IS_RESIZABLE_DPROP));

    }



    /**

     * Test addInternalFrameListener method.

     */

    public void testAddInternalFrameListener() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        InternalFrameListenerTest listener = new InternalFrameListenerTest();

        operator2.addInternalFrameListener(listener);

        assertEquals(2, internalFrame.getInternalFrameListeners().length);

        

        operator2.removeInternalFrameListener(listener);

        assertEquals(1, internalFrame.getInternalFrameListeners().length);

    }

    

    /**

     * Inner class used for testing.

     */

    public class InternalFrameListenerTest implements InternalFrameListener {

        public void internalFrameOpened(InternalFrameEvent e) {

        }



        public void internalFrameClosing(InternalFrameEvent e) {

        }



        public void internalFrameClosed(InternalFrameEvent e) {

        }



        public void internalFrameIconified(InternalFrameEvent e) {

        }



        public void internalFrameDeiconified(InternalFrameEvent e) {

        }



        public void internalFrameActivated(InternalFrameEvent e) {

        }



        public void internalFrameDeactivated(InternalFrameEvent e) {

        }

    }



    /**

     * Test dispose method.

     */

    public void testDispose() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.dispose();

    }



    /**

     * Test getContentPane method.

     */

    public void testGetContentPane() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);



        JPanel contentPane = new JPanel();

        operator2.setContentPane(contentPane);

        assertEquals(contentPane, operator2.getContentPane());

    }



    /**

     * Test getDefaultCloseOperation method.

     */

    public void testGetDefaultCloseOperation() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

        assertEquals(JInternalFrame.DISPOSE_ON_CLOSE, operator2.getDefaultCloseOperation());

        assertEquals(JInternalFrame.DISPOSE_ON_CLOSE, internalFrame.getDefaultCloseOperation());

    }



    /**

     * Test getDesktopIcon method.

     */

    public void testGetDesktopIcon() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        JDesktopIcon icon = new JDesktopIcon(internalFrame);

        operator2.setDesktopIcon(icon);

        assertEquals(icon, operator2.getDesktopIcon());

    }



    /**

     * Test getDesktopPane method.

     */

    public void testGetDesktopPane() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        assertNotNull(operator2.getDesktopPane());

    }



    /**

     * Test getFrameIcon method.

     */

    public void testGetFrameIcon() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        ImageIcon icon = new ImageIcon();

        operator2.setFrameIcon(icon);

        assertEquals(icon, operator2.getFrameIcon());

    }



    /**

     * Test getGlassPane method.

     */

    public void testGetGlassPane() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        JPanel glassPane = new JPanel();

        operator2.setGlassPane(glassPane);

        assertEquals(glassPane, operator2.getGlassPane());

    }



    /**

     * Test getJMenuBar method.

     */

    public void testGetJMenuBar() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        JMenuBar menuBar = new JMenuBar();

        operator2.setJMenuBar(menuBar);

        assertEquals(menuBar, operator2.getJMenuBar());

        assertEquals(menuBar, internalFrame.getJMenuBar());

    }



    /**

     * Test getLayer method.

     */

    public void testGetLayer() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        assertEquals(operator2.getLayer(), internalFrame.getLayer());

    }



    /**

     * Test getLayeredPane method.

     */

    public void testGetLayeredPane() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        JLayeredPane layeredPane = new JLayeredPane();

        operator2.setLayeredPane(layeredPane);

        assertEquals(layeredPane, operator2.getLayeredPane());

    }



    /**

     * Test getTitle method.

     */

    public void testGetTitle() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.setTitle("TITLE");

        assertEquals("TITLE", operator2.getTitle());

    }



    /**

     * Test getUI method.

     */

    public void testGetUI() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        InternalFrameUITest ui = new InternalFrameUITest();

        operator2.setUI(ui);

        assertEquals(ui, operator2.getUI());

    }

    

    /**

     * Inner class used for testing.

     */

    public class InternalFrameUITest extends InternalFrameUI {

    }



    /**

     * Test getWarningString method.

     */

    public void testGetWarningString() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        assertNull(operator2.getWarningString());

    }



    /**

     * Test isClosable method.

     */

    public void testIsClosable() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        assertTrue(operator2.isClosable());



        operator2.setClosable(false);

        assertTrue(!operator2.isClosable());

    }



    /**

     * Test of isSelected method, of class org.netbeans.jemmy.operators.JInternalFrameOperator.

     */

    public void testIsSelected() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.setSelected(true);

        assertTrue(internalFrame.isSelected());



        operator2.setSelected(false);

        assertTrue(!internalFrame.isSelected());

    }



    /**

     * Test moveToBack method.

     */

    public void testMoveToBack() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.moveToBack();

    }



    /**

     * Test moveToFront method.

     */

    public void testMoveToFront() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.moveToFront();

    }



    /**

     * Test pack method.

     */

    public void testPack() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.pack();

    }



    /**

     * Test setClosable method.

     */

    public void testSetClosable() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.setClosable(true);

        assertTrue(operator2.isClosable());



        operator2.setClosable(false);

        assertTrue(!operator2.isClosable());

    }



    /**

     * Test setClosed method.

     */

    public void testSetClosed() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.setClosed(true);

        assertTrue(operator2.isClosed());

    }



    /**

     * Test setLayer method.

     */

    public void testSetLayer() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.setLayer(new Integer(1));

        assertEquals(1, operator2.getLayer());

    }



    /**

     * Test toBack method.

     */

    public void testToBack() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.toBack();

    }



    /**

     * Test toFront method.

     */

    public void testToFront() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.toFront();

    }



    /**

     * Test isResizeable method.

     */

    public void testIsResizable() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        

        operator2.setResizable(true);

        assertTrue(operator2.isResizable());

        

        operator2.setResizable(false);

        assertTrue(!operator2.isResizable());

    }



    /**

     * Test isMaximizable method.

     */

    public void tesIsMaximizable() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);



        operator2.setMaximizable(false);

        assertTrue(!operator2.isMaximizable());

        

        operator2.setMaximizable(true);

        assertTrue(operator2.isMaximizable());

    }



    /**

     * Test isIconifiable method.

     */

    public void testIsIconifiable() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        assertTrue(operator2.isIconifiable());



        operator2.setIconifiable(false);

        assertTrue(!operator2.isIconifiable());

    }



    /**

     * Test setIcon method.

     */

    public void testSetIcon() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        assertTrue(operator2.isIconifiable());



        operator2.setIcon(true);

        assertTrue(operator2.isIcon());

    }



    /**

     * Test setMaximum method.

     */

    public void testSetMaximum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);

        assertTrue(operator2.isMaximizable());



        operator2.setMaximum(true);

        assertTrue(operator2.isMaximum());

    }



    /**

     * Test setMaximizable method.

     */

    public void testSetMaximizable() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JInternalFrameOperator operator2 = new JInternalFrameOperator(operator);

        assertNotNull(operator2);



        operator2.setMaximizable(true);

        assertTrue(operator2.isMaximizable());

    }

}

