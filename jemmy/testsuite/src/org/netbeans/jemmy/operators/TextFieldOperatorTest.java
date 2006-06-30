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
import java.awt.TextField;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for TextFieldOperatorTest.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class TextFieldOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the text field.
     */
    private TextField textField;
            
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public TextFieldOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a serious error occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        textField = new TextField("TextFieldOperatorTest");
        textField.setName("TextFieldOperatorTest");
        frame.add(textField);
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
        TestSuite suite = new TestSuite(TextFieldOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextFieldOperator operator1 = new TextFieldOperator(operator);
        assertNotNull(operator1);
        
        TextFieldOperator operator2 = new TextFieldOperator(operator, new NameComponentChooser("TextFieldOperatorTest"));
        assertNotNull(operator2);
        
        TextFieldOperator operator3 = new TextFieldOperator(operator, "TextFieldOperatorTest");
        assertNotNull(operator3);
    }

    /**
     * Test findTextField method.
     */
    public void testFindTextField() {
        frame.setVisible(true);
        
        TextField textField1 = TextFieldOperator.findTextField(frame, new NameComponentChooser("TextFieldOperatorTest"));
        assertNotNull(textField1);
        
        TextField textField2 = TextFieldOperator.findTextField(frame, "TextFieldOperatorTest", false, false);
        assertNotNull(textField2);
    }

    /**
     * Test waitTextField method.
     */
    public void testWaitTextField() {
        frame.setVisible(true);
        
        TextField textField1 = TextFieldOperator.waitTextField(frame, new NameComponentChooser("TextFieldOperatorTest"));
        assertNotNull(textField1);        
        
        TextField textField2 = TextFieldOperator.waitTextField(frame, "TextFieldOperatorTest", false, false);
        assertNotNull(textField2);
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextFieldOperator operator1 = new TextFieldOperator(operator);
        assertNotNull(operator1);

        operator1.getDump();
    }

    /**
     * Test addActionListener method.
     */
    public void testAddActionListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextFieldOperator operator1 = new TextFieldOperator(operator);
        assertNotNull(operator1);

        operator1.addActionListener(null);
        operator1.removeActionListener(null);
    }

    /**
     * Test echoCharIsSet method.
     */
    public void testEchoCharIsSet() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextFieldOperator operator1 = new TextFieldOperator(operator);
        assertNotNull(operator1);
        
        operator1.echoCharIsSet();
    }

    /**
     * Test getColumns method.
     */
    public void testGetColumns() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextFieldOperator operator1 = new TextFieldOperator(operator);
        assertNotNull(operator1);

        operator1.setColumns(operator1.getColumns());
    }

    /**
     * Test getEchoChar method.
     */
    public void testGetEchoChar() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextFieldOperator operator1 = new TextFieldOperator(operator);
        assertNotNull(operator1);
        
        operator1.getEchoChar();
    }

    /**
     * Test getMinimumSize method.
     */
    public void testGetMinimumSize() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextFieldOperator operator1 = new TextFieldOperator(operator);
        assertNotNull(operator1);

        operator1.getMinimumSize(0);
    }

    /**
     * Test getPreferredSize method.
     */
    public void testGetPreferredSize() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextFieldOperator operator1 = new TextFieldOperator(operator);
        assertNotNull(operator1);
        
        operator1.getPreferredSize(0);
    }
}
