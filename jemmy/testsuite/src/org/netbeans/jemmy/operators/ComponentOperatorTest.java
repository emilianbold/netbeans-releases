/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
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
 * "Portions Copyrighted [year] [name of copyright owner]".
 *
 * The Original Software is the Jemmy library. The Initial Developer of the
 * Original Software is Alexandre Iline. All Rights Reserved.
 *
 * ---------------------------------------------------------------------------
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 */
package org.netbeans.jemmy.operators;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for ComponentOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class ComponentOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the panel.
     */
    private Panel panel;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public ComponentOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a serious error occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        frame.setName("FrameOperatorTest");
        panel = new Panel();
        panel.setName("ComponentOperatorTest");
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when a serious error occurs.
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
        TestSuite suite = new TestSuite(ComponentOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);
        
        ComponentOperator operator2 = new ComponentOperator(operator, new NameComponentChooser("ComponentOperatorTest"));
        assertNotNull(operator2);
    }

    /**
     * Test findComponent method.
     */
    public void testFindComponent() {
        frame.setVisible(true);
        
        Component component = ComponentOperator.findComponent(frame, new NameComponentChooser("ComponentOperatorTest"));
        assertNotNull(component);
    }

    /**
     * Test waitComponent method.
     */
    public void testWaitComponent() {
        frame.setVisible(true);
        
        Component component = ComponentOperator.waitComponent(frame, new NameComponentChooser("ComponentOperatorTest"));
        assertNotNull(component);
    }

    /**
     * Test getSource method.
     */
    public void testGetSource() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getSource();
    }

    /**
     * Test getEventDispatcher method.
     */
    public void testGetEventDispatcher() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getEventDispatcher();
    }

    /**
     * Test clickMouse method.
     */
    public void testClickMouse() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.clickMouse();
    }

    /**
     * Test pressMouse method.
     */
    public void testPressMouse() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.pressMouse();
    }

    /**
     * Test releaseMouse method.
     */
    public void testReleaseMouse() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.releaseMouse();
    }

    /**
     * Test moveMouse method.
     */
    public void testMoveMouse() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.moveMouse(100, 100);
    }

    /**
     * Test dragMouse method.
     */
    public void testDragMouse() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.dragMouse(100, 100);
    }

    /**
     * Test dragNDrop method.
     */
    public void testDragNDrop() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.dragNDrop(100, 100, 200, 200);
        operator1.dragNDrop(100, 100, 100, 100, 1);
        operator1.dragNDrop(100, 100, 100, 100, 1, 0);
    }

    /**
     * Test clickForPopup method.
     */
    public void testClickForPopup() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.clickForPopup();
        operator1.clickForPopup(100, 100);
    }

    /**
     * Test enterMouse method.
     */
    public void testEnterMouse() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.enterMouse();
    }

    /**
     * Test exitMouse method.
     */
    public void testExitMouse() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.exitMouse();
    }

    /**
     * Test pressKey method.
     */
    public void testPressKey() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.pressKey(KeyEvent.VK_0);
    }

    /**
     * Test releaseKey method.
     */
    public void testReleaseKey() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.releaseKey(KeyEvent.VK_0);
    }

    /**
     * Test pushKey method.
     */
    public void testPushKey() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.pushKey(KeyEvent.VK_0);
    }

    /**
     * Test typeKey method.
     */
    public void testTypeKey() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.typeKey('a');
    }

    /**
     * Test activateWindow method.
     */
    public void testActivateWindow() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.activateWindow();
    }

    /**
     * Test makeComponentVisible method.
     */
    public void testMakeComponentVisible() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.makeComponentVisible();
    }

    /**
     * Test getFocus method.
     */
    public void testGetFocus() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getFocus();
    }

    /**
     * Test getCenterX method.
     */
    public void testGetCenterX() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getCenterX();
    }

    /**
     * Test getCenterY method.
     */
    public void testGetCenterY() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getCenterY();
    }

    /**
     * Test getCenterXForClick method.
     */
    public void testGetCenterXForClick() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getCenterXForClick();
    }

    /**
     * Test getCenterYForClick method.
     */
    public void testGetCenterYForClick() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getCenterYForClick();
    }

    /**
     * Test waitComponentEnabled method.
     */
    public void testWaitComponentEnabled() throws Exception {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setEnabled(true);
        operator1.waitComponentEnabled();
    }

    /**
     * Test wtComponentEnabled method.
     */
    public void testWtComponentEnabled() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setEnabled(true);
        operator1.wtComponentEnabled();
    }

    /**
     * Test getContainers method.
     */
    public void testGetContainers() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getContainers();
    }

    /**
     * Test getContainer method.
     */
    public void testGetContainer() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        Container container = operator1.getContainer(new NameComponentChooser("FrameOperatorTest"));
        assertNotNull(container);
    }

    /**
     * Test getWindow method.
     */
    public void testGetWindow() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getWindow();
    }

    /**
     * Test waitHasFocus method.
     */
    public void testWaitHasFocus() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getFocus();
        operator1.waitHasFocus();
    }

    /**
     * Test waitComponentVisible method.
     */
    public void testWaitComponentVisible() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setVisible(true);
        operator1.waitComponentVisible(true);
    }

    /**
     * Test waitComponentShowing method.
     */
    public void testWaitComponentShowing() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setVisible(true);
        operator1.waitComponentShowing(true);
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getDump();
    }

    /**
     * Test add method.
     */
    public void testAdd() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.add(new PopupMenu());
    }

    /**
     * Test addComponentListener method.
     */
    public void testAddComponentListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.addComponentListener(null);
    }

    /**
     * Test addFocusListener method.
     */
    public void testAddFocusListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.addFocusListener(null);
    }

    /**
     * Test addInputMethodListener method.
     */
    public void testAddInputMethodListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.addInputMethodListener(null);
    }

    /**
     * Test addKeyListener method.
     */
    public void testAddKeyListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.addKeyListener(null);
    }

    /**
     * Test addMouseListener method.
     */
    public void testAddMouseListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.addMouseListener(null);
    }

    /**
     * Test addMouseMotionListener method.
     */
    public void testAddMouseMotionListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.addMouseMotionListener(null);
    }

    /**
     * Test addNotify method.
     */
    public void testAddNotify() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.addNotify();
    }

    /**
     * Test addPropertyChangeListener method.
     */
    public void testAddPropertyChangeListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.addPropertyChangeListener(null);
        operator1.addPropertyChangeListener(null, null);
    }

    /**
     * Test checkImage method.
     */
    public void testCheckImage() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.checkImage(null, null);
        operator1.checkImage(null, 100, 100, null);
    }

    /**
     * Test contains method.
     */
    public void testContains() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.contains(100, 100);
        operator1.contains(new Point(100, 100));
    }

    /**
     * Test createImage method.
     */
    public void testCreateImage() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.createImage(null);
        operator1.createImage(100, 100);
    }

    /**
     * Test dispatchEvent method.
     */
    public void testDispatchEvent() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.dispatchEvent(new ActionEvent(frame, 1, "BOOH"));
    }

    /**
     * Test doLayout method.
     */
    public void testDoLayout() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.doLayout();
    }

    /**
     * Test enableInputMethods method.
     */
    public void testEnableInputMethods() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.enableInputMethods(true);
    }

    /**
     * Test getAlignmentX method.
     */
    public void testGetAlignmentX() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getAlignmentX();
    }

    /**
     * Test getAlignmentY method.
     */
    public void testGetAlignmentY() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getAlignmentY();
    }

    /**
     * Test getBackground method.
     */
    public void testGetBackground() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setBackground(operator1.getBackground());
    }

    /**
     * Test getBounds method.
     */
    public void testGetBounds() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setBounds(operator1.getBounds());
        operator1.setBounds(100, 100, 200, 200);
        operator1.getBounds(new Rectangle(100, 100));
        
    }

    /**
     * Test getColorModel method.
     */
    public void testGetColorModel() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getColorModel();
    }

    /**
     * Test getComponentAt method.
     */
    public void testGetComponentAt() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getComponentAt(100, 100);
        operator1.getComponentAt(new Point(100, 100));
    }

    /**
     * Test getComponentOrientation method.
     */
    public void testGetComponentOrientation() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setComponentOrientation(operator1.getComponentOrientation());
    }

    /**
     * Test getCursor method.
     */
    public void testGetCursor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setCursor(operator1.getCursor());
    }

    /**
     * Test getDropTarget method.
     */
    public void testGetDropTarget() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setDropTarget(operator1.getDropTarget());
    }

    /**
     * Test getFont method.
     */
    public void testGetFont() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setFont(operator1.getFont());
    }

    /**
     * Test getFontMetrics method.
     */
    public void testGetFontMetrics() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getFontMetrics(new Font("Times New Roman", Font.BOLD, 12));
    }

    /**
     * Test getForeground method.
     */
    public void testGetForeground() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setForeground(operator1.getForeground());
    }

    /**
     * Test getGraphics method.
     */
    public void testGetGraphics() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getGraphics();
    }

    /**
     * Test getHeight method.
     */
    public void testGetHeight() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getHeight();
    }

    /**
     * Test getInputContext method.
     */
    public void testGetInputContext() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getInputContext();
    }

    /**
     * Test getInputMethodRequests method.
     */
    public void testGetInputMethodRequests() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getInputMethodRequests();
    }

    /**
     * Test getLocale method.
     */
    public void testGetLocale() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setLocale(operator1.getLocale());
    }

    /**
     * Test getLocation method.
     */
    public void testGetLocation() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setLocation(operator1.getLocation());
        operator1.getLocation(new Point(100, 100));
    }

    /**
     * Test getLocationOnScreen method.
     */
    public void testGetLocationOnScreen() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator.getLocationOnScreen();
    }

    /**
     * Test getMaximumSize method.
     */
    public void testGetMaximumSize() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getMaximumSize();
    }

    /**
     * Test getMinimumSize method.
     */
    public void testGetMinimumSize() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getMinimumSize();
    }

    /**
     * Test getName method.
     */
    public void testGetName() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setName(operator1.getName());
    }

    /**
     * Test getParent method.
     */
    public void testGetParent() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getParent();
    }

    /**
     * Test getPreferredSize method.
     */
    public void testGetPreferredSize() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getPreferredSize();
    }

    /**
     * Test getSize method.
     */
    public void testGetSize() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setSize(operator1.getSize());
        operator1.getSize(new Dimension(100, 100));
    }

    /**
     * Test getToolkit method.
     */
    public void testGetToolkit() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getToolkit();
    }

    /**
     * Test getTreeLock method.
     */
    public void testGetTreeLock() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getTreeLock();
    }

    /**
     * Test getWidth method.
     */
    public void testGetWidth() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getWidth();
    }

    /**
     * Test getX method.
     */
    public void testGetX() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getX();
    }

    /**
     * Test getY method.
     */
    public void testGetY() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getY();
    }

    /**
     * Test hasFocus method.
     */
    public void testHasFocus() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.hasFocus();
    }

    /**
     * Test imageUpdate method.
     */
    public void testImageUpdate() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.imageUpdate(null, 100, 100, 100, 100, 100);
    }

    /**
     * Test invalidate method.
     */
    public void testInvalidate() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.invalidate();
    }

    /**
     * Test isDisplayable method.
     */
    public void testIsDisplayable() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator.isDisplayable();
    }

    /**
     * Test isDoubleBuffered method.
     */
    public void testIsDoubleBuffered() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.isDoubleBuffered();
    }

    /**
     * Test isEnabled method.
     */
    public void testIsEnabled() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator.setEnabled(operator1.isEnabled());
    }

    /**
     * Test isFocusTraversable method.
     */
    public void testIsFocusTraversable() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.isFocusTraversable();
    }

    /**
     * Test isLightweight method.
     */
    public void testIsLightweight() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.isLightweight();
    }

    /**
     * Test isOpaque method.
     */
    public void testIsOpaque() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.isOpaque();
    }

    /**
     * Test isShowing method.
     */
    public void testIsShowing() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.isShowing();
    }

    /**
     * Test isValid method.
     */
    public void testIsValid() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.isValid();
    }

    /**
     * Test isVisible method.
     */
    public void testIsVisible() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setVisible(operator1.isVisible());
    }

    /**
     * Test list method.
     */
    public void testList() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.list();
        operator1.list(new PrintWriter(new StringWriter()));
        operator1.list(new PrintWriter(new StringWriter()), 0);
        operator1.list(new PrintStream(new ByteArrayOutputStream()));
        operator1.list(new PrintStream(new ByteArrayOutputStream()), 0);
    }

    /**
     * Test paint method.
     */
    public void testPaint() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.paint(operator1.getGraphics());
    }

    /**
     * Test paintAll method.
     */
    public void testPaintAll() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.paintAll(operator1.getGraphics());
    }

    /**
     * Test prepareImage method.
     */
    public void testPrepareImage() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.prepareImage(null, null);
        operator1.prepareImage(null, 100, 100, null);
    }

    /**
     * Test print method.
     */
    public void testPrint() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.print(operator1.getGraphics());
    }

    /**
     * Test printAll method.
     */
    public void testPrintAll() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.printAll(operator1.getGraphics());
    }

    /**
     * Test remove method.
     */
    public void testRemove() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.remove(null);
    }

    /**
     * Test removeComponentListener method.
     */
    public void testRemoveComponentListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.removeComponentListener(null);
    }

    /**
     * Test removeFocusListener method.
     */
    public void testRemoveFocusListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.removeFocusListener(null);
    }

    /**
     * Test removeInputMethodListener method.
     */
    public void testRemoveInputMethodListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.removeInputMethodListener(null);
    }

    /**
     * Test removeKeyListener method.
     */
    public void testRemoveKeyListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.removeKeyListener(null);
    }

    /**
     * Test removeMouseListener method.
     */
    public void testRemoveMouseListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.removeMouseListener(null);
    }

    /**
     * Test removeMouseMotionListener method.
     */
    public void testRemoveMouseMotionListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.removeMouseMotionListener(null);
    }

    /**
     * Test removeNotify method.
     */
    public void testRemoveNotify() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.removeNotify();
    }

    /**
     * Test removePropertyChangeListener method.
     */
    public void testRemovePropertyChangeListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.removePropertyChangeListener(null);
        operator1.removePropertyChangeListener(null, null);
    }

    /**
     * Test repaint method.
     */
    public void testRepaint() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.repaint();
        operator1.repaint(1L);
        operator1.repaint(100, 100, 100, 100);
        operator1.repaint(1L, 100, 100, 100, 100);
    }

    /**
     * Test requestFocus method.
     */
    public void testRequestFocus() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.requestFocus();
    }

    /**
     * Test transferFocus method.
     */
    public void testTransferFocus() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.transferFocus();
    }

    /**
     * Test update method.
     */
    public void testUpdate() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.update(operator1.getGraphics());
    }

    /**
     * Test validate method.
     */
    public void testValidate() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ComponentOperator operator1 = new ComponentOperator(operator);
        assertNotNull(operator1);

        operator1.validate();
    }
}
