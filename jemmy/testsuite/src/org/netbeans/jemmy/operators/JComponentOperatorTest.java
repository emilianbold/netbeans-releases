/*
 * $Id$
 *
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Jemmy library. The Initial Developer of the
 * Original Code is Alexandre Iline. All Rights Reserved.
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 */
package org.netbeans.jemmy.operators;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JComponentOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JComponentOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the component.
     */
    private JComponent component;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JComponentOperatorTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        component = new JPanel();
        component.setName("JComponentOperatorTest");
        component.setToolTipText("JComponentOperatorTest");
        frame.getContentPane().add(component);
        frame.setName("JFrameOperatorTest");
        frame.setSize(300, 200);
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
        TestSuite suite = new TestSuite(JComponentOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        JComponentOperator operator2 = new JComponentOperator(operator, new NameComponentChooser("JComponentOperatorTest"));
        assertNotNull(operator2);
    }
    
    /**
     * Test findJComponent method.
     */
    public void testFindJComponent() {
        frame.setVisible(true);
        
        JComponent component1 = JComponentOperator.findJComponent(frame, new NameComponentChooser("JComponentOperatorTest"));
        assertNotNull(component1);
        
        JComponent component2 = JComponentOperator.findJComponent(frame, "JComponentOperatorTest", false, false);
        assertNotNull(component2);
    }
    
    /**
     * Test waitJComponent method.
     */
    public void testWaitJComponent() {
        frame.setVisible(true);
        
        JComponent component1 = JComponentOperator.waitJComponent(frame, new NameComponentChooser("JComponentOperatorTest"));
        assertNotNull(component1);
        
        JComponent component2 = JComponentOperator.waitJComponent(frame, "JComponentOperatorTest", false, false);
        assertNotNull(component2);
    }
    
    /**
     * Test getCenterXForClick method.
     */
    public void testGetCenterXForClick() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getCenterXForClick();
    }
    
    /**
     * Test getCenterYForClick method.
     */
    public void testGetCenterYForClick() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getCenterYForClick();
    }
    
    /**
     * Test showToolTip method.
     */
    public void testShowToolTip() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        // operator1.showToolTip().setVisible(true);
    }
    
    /**
     * Test waitToolTip method.
     */
    public void testWaitToolTip() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        // operator1.waitToolTip().setVisible(true);
    }
    
    /**
     * Test getWindowContainerOperator method.
     */
    public void testGetWindowContainerOperator() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getWindowContainerOperator();
    }
    
    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getDump();
    }
    
    /**
     * Test addAncestorListener method.
     */
    public void testAddAncestorListener() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        AncestorListenerTest listener = new AncestorListenerTest();
        operator1.addAncestorListener(listener);
        operator1.removeAncestorListener(listener);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class AncestorListenerTest implements AncestorListener {
        public void ancestorAdded(AncestorEvent event) {
        }

        public void ancestorRemoved(AncestorEvent event) {
        }

        public void ancestorMoved(AncestorEvent event) {
        }
    }
    
    /**
     * Test addVetoableChangeListener method.
     */
    public void testAddVetoableChangeListener() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        VetoableChangeListenerTest listener = new VetoableChangeListenerTest();
        operator1.addVetoableChangeListener(listener);
        operator1.removeVetoableChangeListener(listener);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class VetoableChangeListenerTest implements VetoableChangeListener {
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        }
    }
    
    /**
     * Test computeVisibleRect method.
     */
    public void testComputeVisibleRect() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.computeVisibleRect(new Rectangle(0, 0, 100, 100));
    }
    
    /**
     * Test createToolTip method.
     */
    public void testCreateToolTip() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.createToolTip();
    }
    
    /**
     * Test firePropertyChange method.
     */
    public void testFirePropertyChange() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.firePropertyChange("1", false, false);
        operator1.firePropertyChange("1", (byte) 'a', (byte) 'b');
        operator1.firePropertyChange("1", 'a', 'b');
        operator1.firePropertyChange("1", 0.0, 0.0);
        operator1.firePropertyChange("1", 0.0f, 0.0f);
        operator1.firePropertyChange("1", 1, 1);
        operator1.firePropertyChange("1", 1L, 1L);
        operator1.firePropertyChange("1", (short) 1, (short) 1);
    }
    
    /**
     * Test getAccessibleContext method.
     */
    public void testGetAccessibleContext() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getAccessibleContext();        
    }
    
    /**
     * Test getActionForKeyStroke method.
     */
    public void testGetActionForKeyStroke() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getActionForKeyStroke(KeyStroke.getKeyStroke('a'));
    }
    
    /**
     * Test getAutoscrolls method.
     */
    public void testGetAutoscrolls() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setAutoscrolls(true);
        assertTrue(operator1.getAutoscrolls());
        
        operator1.setAutoscrolls(false);
        assertTrue(!operator1.getAutoscrolls());
    }
    
    /**
     * Test getBorder method.
     */
    public void testGetBorder() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setBorder(null);
        assertNull(operator1.getBorder());
    }
    
    /**
     * Test getClientProperty method.
     */
    public void testGetClientProperty() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getClientProperty("1");
    }
    
    /**
     * Test getConditionForKeyStroke method.
     */
    public void testGetConditionForKeyStroke() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getConditionForKeyStroke(KeyStroke.getKeyStroke('a'));
    }
    
    /**
     * Test getDebugGraphicsOptions method.
     */
    public void testGetDebugGraphicsOptions() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setDebugGraphicsOptions(0);
        assertEquals(0, operator1.getDebugGraphicsOptions());
    }
    
    /**
     * Test getInsets method.
     */
    public void testGetInsets() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getInsets(new Insets(0, 0, 1, 1));
        operator1.getInsets();
    }
    
    /**
     * Test getNextFocusableComponent method.
     */
    public void testGetNextFocusableComponent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getNextFocusableComponent();
    }
    
    /**
     * Test getRegisteredKeyStrokes method.
     */
    public void testGetRegisteredKeyStrokes() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getRegisteredKeyStrokes();
    }
    
    /**
     * Test getRootPane method.
     */
    public void testGetRootPane() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getRootPane();
    }
    
    /**
     * Test getToolTipLocation method.
     */
    public void testGetToolTipLocation() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getToolTipLocation(new MouseEvent(frame, 0, 0, 0, 0, 0, 0, false));
    }
    
    /**
     * Test getToolTipText method.
     */
    public void testGetToolTipText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getToolTipText();
        operator1.getToolTipText(new MouseEvent(frame, 0, 0, 0, 0, 0, 0, false));
    }
    
    /**
     * Test getTopLevelAncestor method.
     */
    public void testGetTopLevelAncestor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getTopLevelAncestor();
    }
    
    /**
     * Test getUIClassID method.
     */
    public void testGetUIClassID() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getUIClassID();
    }
    
    /**
     * Test getVisibleRect method.
     */
    public void testGetVisibleRect() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getVisibleRect();
    }
    
    /**
     * Test grabFocus method.
     */
    public void testGrabFocus() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.grabFocus();
    }
    
    /**
     * Test isFocusCycleRoot method.
     */
    public void testIsFocusCycleRoot() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.isFocusCycleRoot();
    }
    
    /**
     * Test isManagingFocus method.
     */
    public void testIsManagingFocus() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.isManagingFocus();
    }
    
    /**
     * Test isOptimizedDrawingEnabled method.
     */
    public void testIsOptimizedDrawingEnabled() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.isOptimizedDrawingEnabled();
    }
    
    /**
     * Test isPaintingTile method.
     */
    public void testIsPaintingTile() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.isPaintingTile();
    }
    
    /**
     * Test isRequestFocusEnabled method.
     */
    public void testIsRequestFocusEnabled() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.isRequestFocusEnabled();
    }
    
    /**
     * Test isValidateRoot method.
     */
    public void testIsValidateRoot() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.isValidateRoot();
    }
    
    /**
     * Test paintImmediately method.
     */
    public void testPaintImmediately() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);

        operator1.paintImmediately(0, 0, 1, 1);
        operator1.paintImmediately(new Rectangle(0, 0, 1, 1));
    }
    
    /**
     * Test putClientProperty method.
     */
    public void testPutClientProperty() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.putClientProperty("1", "2");
    }
    
    /**
     * Test registerKeyboardAction method.
     */
    public void testRegisterKeyboardAction() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.registerKeyboardAction(new ActionListenerTest(), KeyStroke.getKeyStroke('a'), 1);
        operator1.registerKeyboardAction(new ActionListenerTest(), "1", KeyStroke.getKeyStroke('a'), 1);
    }
    
    /**
     * Inner class.
     */
    public class ActionListenerTest implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        }
    }
     
    
    /**
     * Test removeAncestorListener method.
     */
    public void testRemoveAncestorListener() {
        
    }
    
    /**
     * Test removeVetoableChangeListener method.
     */
    public void testRemoveVetoableChangeListener() {
        
    }
    
    /**
     * Test repaint method.
     */
    public void testRepaint() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.repaint();
        operator1.repaint(new Rectangle(0, 0, 1, 1));
    }
    
    /**
     * Test requestDefaultFocus method.
     */
    public void testRequestDefaultFocus() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.requestDefaultFocus();
    }
    
    /**
     * Test resetKeyboardActions method.
     */
    public void testResetKeyboardActions() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.resetKeyboardActions();
    }
    
    /**
     * Test revalidate method.
     */
    public void testRevalidate() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.revalidate();
    }
    
    /**
     * Test scrollRectToVisible method.
     */
    public void testScrollRectToVisible() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
    }
    
    /**
     * Test setAlignmentX method.
     */
    public void testSetAlignmentX() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setAlignmentX(1.0f);
    }
    
    /**
     * Test setAlignmentY method.
     */
    public void testSetAlignmentY() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setAlignmentY(1.0f);
    }
    
    /**
     * Test setDoubleBuffered method.
     */
    public void testSetDoubleBuffered() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setDoubleBuffered(true);
    }
    
    /**
     * Test setMaximumSize method.
     */
    public void testSetMaximumSize() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setMaximumSize(new Dimension(100, 100));
    }
    
    /**
     * Test setMinimumSize method.
     */
    public void testSetMinimumSize() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setMinimumSize(new Dimension(10, 10));
    }
    
    /**
     * Test setNextFocusableComponent method.
     */
    public void testSetNextFocusableComponent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setNextFocusableComponent(new JPanel());
    }
    
    /**
     * Test setOpaque method.
     */
    public void testSetOpaque() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setOpaque(false);
    }
    
    /**
     * Test setPreferredSize method.
     */
    public void testSetPreferredSize() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setPreferredSize(new Dimension(100, 100));
    }
    
    /**
     * Test setRequestFocusEnabled method.
     */
    public void testSetRequestFocusEnabled() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setRequestFocusEnabled(false);
    }
    
    /**
     * Test setToolTipText method.
     */
    public void testSetToolTipText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setToolTipText("1234");
    }
    
    /**
     * Test unregisterKeyboardAction method.
     */
    public void testUnregisterKeyboardAction() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.unregisterKeyboardAction(KeyStroke.getKeyStroke('a'));
    }
    
    /**
     * Test updateUI method.
     */
    public void testUpdateUI() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JComponentOperator operator1 = new JComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.updateUI();
    }
}
