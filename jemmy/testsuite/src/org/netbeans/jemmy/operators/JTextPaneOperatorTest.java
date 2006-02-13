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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JTextPaneOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JTextPaneOperatorTest extends TestCase {
    /**
     * Stores the frame we use for testing.
     */
    private JFrame frame;
    
    /**
     * Stores the text pane.
     */
    private JTextPane textPane;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JTextPaneOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup for testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        textPane = new JTextPane();
        textPane.setName("JTextPaneOperatorTest");
        textPane.getStyledDocument().insertString(0, "JTextPaneOperatorTest", null);
        frame.getContentPane().add(textPane);
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
        TestSuite suite = new TestSuite(JTextPaneOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextPaneOperator operator1 = new JTextPaneOperator(operator);
        assertNotNull(operator1);
        
        JTextPaneOperator operator2 = new JTextPaneOperator(operator, new NameComponentChooser("JTextPaneOperatorTest"));
        assertNotNull(operator2);
        
        JTextPaneOperator operator3 = new JTextPaneOperator(operator, "JTextPaneOperatorTest");
        assertNotNull(operator3);
        
        JTextPaneOperator operator4 = new JTextPaneOperator(textPane);
        assertNotNull(operator4);
    }

    /**
     * Test findJTextPane method.
     */
    public void testFindJTextPane() {
        frame.setVisible(true);
        
        JTextPane textPane1 = JTextPaneOperator.findJTextPane(frame, "JTextPaneOperatorTest", false, false);
        assertNotNull(textPane1);
        
        JTextPane textPane2 = JTextPaneOperator.findJTextPane(frame, new NameComponentChooser("JTextPaneOperatorTest"));
        assertNotNull(textPane2);
    }

    /**
     * Test waitJTextPane method.
     */
    public void testWaitJTextPane() {
        frame.setVisible(true);
        
        JTextPane textPane1 = JTextPaneOperator.waitJTextPane(frame, "JTextPaneOperatorTest", false, false);
        assertNotNull(textPane1);
        
        JTextPane textPane2 = JTextPaneOperator.waitJTextPane(frame, new NameComponentChooser("JTextPaneOperatorTest"));
        assertNotNull(textPane2);
    }

    /**
     * Test addStyle method.
     */
    public void testAddStyle() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextPaneOperator operator1 = new JTextPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.addStyle("1234", null);
        assertNotNull(operator1.getStyle("1234"));
    }

    /**
     * Test getCharacterAttributes method.
     */
    public void testGetCharacterAttributes() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextPaneOperator operator1 = new JTextPaneOperator(operator);
        assertNotNull(operator1);
        
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        operator1.setCharacterAttributes(attributes, true);
        assertNotNull(operator1.getCharacterAttributes());
    }

    /**
     * Test getInputAttributes method.
     */
    public void testGetInputAttributes() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextPaneOperator operator1 = new JTextPaneOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getInputAttributes());
    }

    /**
     * Test getLogicalStyle method.
     */
    public void testGetLogicalStyle() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextPaneOperator operator1 = new JTextPaneOperator(operator);
        assertNotNull(operator1);
        
        Style style = null;
        operator1.setLogicalStyle(style);
        assertEquals(style, operator1.getLogicalStyle());
    }

    /**
     * Test getParagraphAttributes method.
     */
    public void testGetParagraphAttributes() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextPaneOperator operator1 = new JTextPaneOperator(operator);
        assertNotNull(operator1);
        
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        operator1.setParagraphAttributes(attributes, true);
        assertNotNull(operator1.getParagraphAttributes());
    }

    /**
     * Test getStyle method.
     */
    public void testGetStyle() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextPaneOperator operator1 = new JTextPaneOperator(operator);
        assertNotNull(operator1);
        
        assertNull(operator1.getStyle("BLABLA"));
    }

    /**
     * Test getStyledDocument method.
     */
    public void testGetStyledDocument() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextPaneOperator operator1 = new JTextPaneOperator(operator);
        assertNotNull(operator1);

        StyledDocument document = new DefaultStyledDocument();
        operator1.setStyledDocument(document);
        assertEquals(document, operator1.getStyledDocument());
    }

    /**
     * Test insertComponent method.
     */
    public void testInsertComponent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextPaneOperator operator1 = new JTextPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.insertComponent(new JButton());
    }

    /**
     * Test insertIcon method.
     */
    public void testInsertIcon() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextPaneOperator operator1 = new JTextPaneOperator(operator);
        assertNotNull(operator1);

        operator1.insertIcon(new ImageIcon());
    }

    /**
     * Test removeStyle method.
     */
    public void testRemoveStyle() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextPaneOperator operator1 = new JTextPaneOperator(operator);
        assertNotNull(operator1);

        operator1.removeStyle("BLABLA");
    }
}
