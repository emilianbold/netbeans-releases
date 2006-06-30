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

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for AbstractButtonOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class AbstractButtonOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the button.
     */
    private JButton button;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public AbstractButtonOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a major error occurs.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        button = new JButton("AbstractButtonOperatorTest");
        button.setName("AbstractButtonOperatorTest");
        frame.getContentPane().add(button);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when a major error occurs.
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
        TestSuite suite = new TestSuite(AbstractButtonOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);
        
        AbstractButtonOperator operator2 = new AbstractButtonOperator(operator, new NameComponentChooser("AbstractButtonOperatorTest"));
        assertNotNull(operator2);
        
        AbstractButtonOperator operator3 = new AbstractButtonOperator(operator, "AbstractButtonOperatorTest");
        assertNotNull(operator3);
    }

    /**
     * Test findAbstractButton method.
     */
    public void testFindAbstractButton() {
        frame.setVisible(true);
        
        AbstractButton button1 = AbstractButtonOperator.findAbstractButton(frame, new NameComponentChooser("AbstractButtonOperatorTest"));
        assertNotNull(button1);
        
        AbstractButton button2 = AbstractButtonOperator.findAbstractButton(frame, "AbstractButtonOperatorTest", false, false);
        assertNotNull(button2);
    }

    /**
     * Test waitAbstractButton method.
     */
    public void testWaitAbstractButton() {
        frame.setVisible(true);
        
        AbstractButton button1 = AbstractButtonOperator.waitAbstractButton(frame, new NameComponentChooser("AbstractButtonOperatorTest"));
        assertNotNull(button1);
        
        AbstractButton button2 = AbstractButtonOperator.waitAbstractButton(frame, "AbstractButtonOperatorTest", false, false);
        assertNotNull(button2);
    }

    /**
     * Test push method.
     */
    public void testPush() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.push();
    }

    /**
     * Test pushNoBlock method.
     */
    public void testPushNoBlock() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.pushNoBlock();
    }

    /**
     * Test changeSelection method.
     */
    public void testChangeSelection() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setSelected(true);
        operator1.changeSelection(true);
    }

    /**
     * Test changeSelectionNoBlock method.
     */
    public void testChangeSelectionNoBlock() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setSelected(true);
        operator1.changeSelectionNoBlock(true);
    }

    /**
     * Test press method.
     */
    public void testPress() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.press();
    }

    /**
     * Test release method.
     */
    public void testRelease() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.release();
    }

    /**
     * Test waitSelected method.
     */
    public void testWaitSelected() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setSelected(true);
        operator1.waitSelected(true);
    }

    /**
     * Test waitText method.
     */
    public void testWaitText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.waitText("AbstractButtonOperatorTest");
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.getDump();
    }

    /**
     * Test addActionListener method.
     */
    public void testAddActionListener() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.addActionListener(null);
    }

    /**
     * Test addChangeListener method.
     */
    public void testAddChangeListener() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.addChangeListener(null);
    }

    /**
     * Test addItemListener method.
     */
    public void testAddItemListener() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.addItemListener(null);
    }

    /**
     * Test doClick method.
     */
    public void testDoClick() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.doClick();
        operator1.doClick(2);
    }

    /**
     * Test getActionCommand method.
     */
    public void testGetActionCommand() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setActionCommand(operator1.getActionCommand());
    }

    /**
     * Test getDisabledIcon method.
     */
    public void testGetDisabledIcon() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setDisabledIcon(operator1.getDisabledIcon());
    }

    /**
     * Test getDisabledSelectedIcon method.
     */
    public void testGetDisabledSelectedIcon() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setDisabledSelectedIcon(operator1.getDisabledSelectedIcon());
    }

    /**
     * Test getHorizontalAlignment method.
     */
    public void testGetHorizontalAlignment() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setHorizontalAlignment(operator1.getHorizontalAlignment());
    }

    /**
     * Test getHorizontalTextPosition method.
     */
    public void testGetHorizontalTextPosition() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setHorizontalTextPosition(operator1.getHorizontalTextPosition());
    }

    /**
     * Test getIcon method.
     */
    public void testGetIcon() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setIcon(operator1.getIcon());
    }

    /**
     * Test getMargin method.
     */
    public void testGetMargin() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setMargin(operator1.getMargin());
    }

    /**
     * Test getMnemonic method.
     */
    public void testGetMnemonic() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setMnemonic(operator1.getMnemonic());
        operator1.setMnemonic('a');
    }

    /**
     * Test getModel method.
     */
    public void testGetModel() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setModel(operator1.getModel());
    }

    /**
     * Test getPressedIcon method.
     */
    public void testGetPressedIcon() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setPressedIcon(operator1.getPressedIcon());
    }

    /**
     * Test getRolloverIcon method.
     */
    public void testGetRolloverIcon() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setRolloverIcon(operator1.getRolloverIcon());
    }

    /**
     * Test getRolloverSelectedIcon method.
     */
    public void testGetRolloverSelectedIcon() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setRolloverSelectedIcon(operator1.getRolloverSelectedIcon());
    }

    /**
     * Test getSelectedIcon method.
     */
    public void testGetSelectedIcon() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setSelectedIcon(operator1.getSelectedIcon());
    }

    /**
     * Test getSelectedObjects method.
     */
    public void testGetSelectedObjects() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.getSelectedObjects();
    }

    /**
     * Test getText method.
     */
    public void testGetText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setText(operator1.getText());
    }

    /**
     * Test getUI method.
     */
    public void testGetUI() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setUI(operator1.getUI());
    }

    /**
     * Test getVerticalAlignment method.
     */
    public void testGetVerticalAlignment() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setVerticalAlignment(operator1.getVerticalAlignment());
    }

    /**
     * Test getVerticalTextPosition method.
     */
    public void testGetVerticalTextPosition() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setVerticalTextPosition(operator1.getVerticalTextPosition());
    }

    /**
     * Test isBorderPainted method.
     */
    public void testIsBorderPainted() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setBorderPainted(operator1.isBorderPainted());
    }

    /**
     * Test isContentAreaFilled method.
     */
    public void testIsContentAreaFilled() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setContentAreaFilled(operator1.isContentAreaFilled());
    }

    /**
     * Test isFocusPainted method.
     */
    public void testIsFocusPainted() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setFocusPainted(operator1.isFocusPainted());
    }

    /**
     * Test isRolloverEnabled method.
     */
    public void testIsRolloverEnabled() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setRolloverEnabled(operator1.isRolloverEnabled());
    }

    /**
     * Test isSelected method.
     */
    public void testIsSelected() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setSelected(operator1.isSelected());
    }

    /**
     * Test removeActionListener method.
     */
    public void testRemoveActionListener() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.removeActionListener(null);
    }

    /**
     * Test removeChangeListener method.
     */
    public void testRemoveChangeListener() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.removeChangeListener(null);
    }

    /**
     * Test removeItemListener method.
     */
    public void testRemoveItemListener() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        AbstractButtonOperator operator1 = new AbstractButtonOperator(operator);
        assertNotNull(operator1);

        operator1.removeItemListener(null);
    }
}
