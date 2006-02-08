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

import java.util.Hashtable;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.plaf.LabelUI;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JLabelOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JLabelOperatorTest extends TestCase {
    /**
     * Stores the frame we use for testing.
     */
    private JFrame frame;
    
    /**
     * Stores the label we use for testing.
     */
    private JLabel label;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JLabelOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup for testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        label = new JLabel("JLabelOperatorTest");
        label.setName("JLabelOperatorTest");
        frame.getContentPane().add(label);
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
        TestSuite suite = new TestSuite(JLabelOperatorTest.class);
        
        return suite;
    }

    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JLabelOperator operator2 = new JLabelOperator(operator1);
        assertNotNull(operator2);
        
        JLabelOperator operator3 = new JLabelOperator(operator1, new NameComponentChooser("JLabelOperatorTest"));
        assertNotNull(operator3);
    }
    
    /**
     * Test findJLabel method.
     */
    public void testFindJLabel() {
        frame.setVisible(true);

        JLabel label1 = JLabelOperator.findJLabel(frame, new NameComponentChooser("JLabelOperatorTest"));
        assertNotNull(label1);

        JLabel label2 = JLabelOperator.findJLabel(frame, "JLabelOperatorTest", false, false);
        assertNotNull(label2);
    }

    /**
     * Test waitJLabel method.
     */
    public void testWaitJLabel() {
        frame.setVisible(true);

        JLabel label1 = JLabelOperator.waitJLabel(frame, new NameComponentChooser("JLabelOperatorTest"));
        assertNotNull(label1);

        JLabel label2 = JLabelOperator.waitJLabel(frame, "JLabelOperatorTest", false, false);
        assertNotNull(label2);
    }

    /**
     * Test waitText method.
     */
    public void testWaitText() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JLabelOperator operator2 = new JLabelOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setText("JLabelOperatorTest-");
        assertEquals(label.getText(), "JLabelOperatorTest-");
        
        operator2.setText("JLabelOperatorTest");
        operator2.waitText("JLabelOperatorTest");
        assertNotNull(label.getText());
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JLabelOperator operator2 = new JLabelOperator(operator1);
        assertNotNull(operator2);
        
        Hashtable hashtable = operator2.getDump();
        assertEquals("JLabelOperatorTest", hashtable.get(JLabelOperator.TEXT_DPROP));
        
        operator2.setText(null);
        assertNull(label.getText());
        
        hashtable = operator2.getDump();
        assertEquals("null", hashtable.get(JLabelOperator.TEXT_DPROP));
    }

    /**
     * Test getDisabledIcon method.
     */
    public void testGetDisabledIcon() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JLabelOperator operator2 = new JLabelOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setDisabledIcon(null);
        assertNull(label.getDisabledIcon());
    }

    /**
     * Test of getDisplayedMnemonic method, of class org.netbeans.jemmy.operators.JLabelOperator.
     */
    public void testGetDisplayedMnemonic() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JLabelOperator operator2 = new JLabelOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setDisplayedMnemonic('A');
        assertEquals('A', operator2.getDisplayedMnemonic());
        assertEquals(label.getDisplayedMnemonic(), operator2.getDisplayedMnemonic());

        operator2.setDisplayedMnemonic((int) 'A');
        assertEquals('A', operator2.getDisplayedMnemonic());
        assertEquals(label.getDisplayedMnemonic(), operator2.getDisplayedMnemonic());
    }

    /**
     * Test getHorizontalAlignment method.
     */
    public void testGetHorizontalAlignment() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JLabelOperator operator2 = new JLabelOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setHorizontalAlignment(SwingConstants.TRAILING);
        assertEquals(SwingConstants.TRAILING, label.getHorizontalAlignment());
    }

    /**
     * Test getHorizontalTextPosition method.
     */
    public void testGetHorizontalTextPosition() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JLabelOperator operator2 = new JLabelOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setHorizontalTextPosition(SwingConstants.LEFT);
        assertEquals(SwingConstants.LEFT, label.getHorizontalTextPosition());
    }

    /**
     * Test getIcon method.
     */
    public void testGetIcon() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JLabelOperator operator2 = new JLabelOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setIcon(null);
        assertNull(label.getIcon());
    }

    /**
     * Test getIconTextGap method.
     */
    public void testGetIconTextGap() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JLabelOperator operator2 = new JLabelOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setIconTextGap(15);
        assertEquals(15, label.getIconTextGap());
    }

    /**
     * Test getLabelFor method.
     */
    public void testGetLabelFor() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JLabelOperator operator2 = new JLabelOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setLabelFor(frame);
        assertEquals(operator2.getLabelFor(), frame);
        assertEquals(operator2.getLabelFor(), label.getLabelFor());
    }

    /**
     * Test getUI method.
     */
    public void testGetUI() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JLabelOperator operator2 = new JLabelOperator(operator1);
        assertNotNull(operator2);
        
        JLabelOperatorTestUI ui = new JLabelOperatorTestUI();
        operator2.setUI(ui);
        assertEquals(operator2.getUI(), ui);
        assertEquals(label.getUI(), ui);
    }
    
    /**
     * Simple UI needed for testing.
     */
    public class JLabelOperatorTestUI extends LabelUI {
    }

    /**
     * Test getVerticalAlignment method.
     */
    public void testGetVerticalAlignment() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JLabelOperator operator2 = new JLabelOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setVerticalAlignment(SwingConstants.TOP);
        assertEquals(SwingConstants.TOP, label.getVerticalAlignment());
    }

    /**
     * Test getVerticalTextPosition method.
     */
    public void testGetVerticalTextPosition() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JLabelOperator operator2 = new JLabelOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setVerticalTextPosition(SwingConstants.TOP);
        assertEquals(SwingConstants.TOP, label.getVerticalTextPosition());
    }
}
