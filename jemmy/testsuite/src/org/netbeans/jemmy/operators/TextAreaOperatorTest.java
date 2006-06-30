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

import java.awt.Frame;
import java.awt.TextArea;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for TextAreaOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class TextAreaOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the text area.
     */
    private TextArea textArea;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public TextAreaOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a major error occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        textArea = new TextArea();
        textArea.setName("TextAreaOperatorTest");
        textArea.setText("TextAreaOperatorTest");
        frame.add(textArea);
        frame.setSize(400, 300);
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
        TestSuite suite = new TestSuite(TextAreaOperatorTest.class);
        
        return suite;
    }

    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextAreaOperator operator1 = new TextAreaOperator(operator);
        assertNotNull(operator1);
        
        TextAreaOperator operator2 = new TextAreaOperator(operator, "TextAreaOperatorTest");
        assertNotNull(operator2);
        
        TextAreaOperator operator3 = new TextAreaOperator(operator, new NameComponentChooser("TextAreaOperatorTest"));
        assertNotNull(operator3);
    }
    
    /**
     * Test findTextArea method.
     */
    public void testFindTextArea() {
        frame.setVisible(true);
        
        TextArea textArea1 = TextAreaOperator.findTextArea(frame, "TextAreaOperatorTest", false, false);
        assertNotNull(textArea1);
        
        TextArea textArea2 = TextAreaOperator.findTextArea(frame, new NameComponentChooser("TextAreaOperatorTest"));
        assertNotNull(textArea2);
    }

    /**
     * Test waitTextArea method.
     */
    public void testWaitTextArea() {
        frame.setVisible(true);
        
        TextArea textArea1 = TextAreaOperator.waitTextArea(frame, "TextAreaOperatorTest", false, false);
        assertNotNull(textArea1);
        
        TextArea textArea2 = TextAreaOperator.waitTextArea(frame, new NameComponentChooser("TextAreaOperatorTest"));
        assertNotNull(textArea2);
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextAreaOperator operator1 = new TextAreaOperator(operator);
        assertNotNull(operator1);

        operator1.getDump();
    }

    /**
     * Test getColumns method.
     */
    public void testGetColumns() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextAreaOperator operator1 = new TextAreaOperator(operator);
        assertNotNull(operator1);
        
        operator1.getColumns();
    }

    /**
     * Test getMinimumSize method.
     */
    public void testGetMinimumSize() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextAreaOperator operator1 = new TextAreaOperator(operator);
        assertNotNull(operator1);

        operator1.getMinimumSize(0, 0);
    }

    /**
     * Test getPreferredSize method.
     */
    public void testGetPreferredSize() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextAreaOperator operator1 = new TextAreaOperator(operator);
        assertNotNull(operator1);

        operator1.getPreferredSize(0, 0);
    }

    /**
     * Test getRows method.
     */
    public void testGetRows() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextAreaOperator operator1 = new TextAreaOperator(operator);
        assertNotNull(operator1);

        operator1.getRows();
    }

    /**
     * Test getScrollbarVisibility method.
     */
    public void testGetScrollbarVisibility() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextAreaOperator operator1 = new TextAreaOperator(operator);
        assertNotNull(operator1);

        operator1.getScrollbarVisibility();
    }

    /**
     * Test replaceRange method.
     */
    public void testReplaceRange() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextAreaOperator operator1 = new TextAreaOperator(operator);
        assertNotNull(operator1);

        operator1.replaceRange("Text", 0, 4);
    }

    /**
     * Test setColumns method.
     */
    public void testSetColumns() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextAreaOperator operator1 = new TextAreaOperator(operator);
        assertNotNull(operator1);

        operator1.setColumns(2);
    }

    /**
     * Test setRows method.
     */
    public void testSetRows() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextAreaOperator operator1 = new TextAreaOperator(operator);
        assertNotNull(operator1);

        operator1.setRows(2);
    }
}
