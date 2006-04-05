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

import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextComponent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for TextComponentOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class TextComponentOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the text component.
     */
    private TextComponent textComponent;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public TextComponentOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a serious error occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        textComponent = new TextArea();
        textComponent.setName("TextComponentOperatorTest");
        textComponent.setText("TextComponentOperatorTest");
        frame.add(textComponent);
        frame.setSize(400, 300);
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
        TestSuite suite = new TestSuite(TextComponentOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);
        
        TextComponentOperator operator2 = new TextComponentOperator(operator, new NameComponentChooser("TextComponentOperatorTest"));
        assertNotNull(operator2);
        
        TextComponentOperator operator3 = new TextComponentOperator(operator, "TextComponentOperatorTest");
        assertNotNull(operator3);
    }

    /**
     * Test findTextComponent method.
     */
    public void testFindTextComponent() {
        frame.setVisible(true);
        
        TextComponent component1 = TextComponentOperator.findTextComponent(frame, "TextComponentOperatorTest", false, false);
        assertNotNull(component1);
        
        TextComponent component2 = TextComponentOperator.findTextComponent(frame, new NameComponentChooser("TextComponentOperatorTest"));
        assertNotNull(component2);
    }

    /**
     * Test waitTextComponent method.
     */
    public void testWaitTextComponent() {
        frame.setVisible(true);
        
        TextComponent component1 = TextComponentOperator.waitTextComponent(frame, "TextComponentOperatorTest", false, false);
        assertNotNull(component1);

        TextComponent component2 = TextComponentOperator.waitTextComponent(frame, new NameComponentChooser("TextComponentOperatorTest"));
        assertNotNull(component2);
    }

    /**
     * Test changeCaretPosition method.
     */
    public void testChangeCaretPosition() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.changeCaretPosition(1);
    }

    /**
     * Test selectText method.
     */
    public void testSelectText() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.selectText(0, 10);
    }

    /**
     * Test getPositionByText method.
     */
    public void testGetPositionByText() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getPositionByText("Text");
    }

    /**
     * Test clearText method.
     */
    public void testClearText() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.clearText();
    }

    /**
     * Test typeText method.
     */
    public void testTypeText() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.typeText("BOOOOOOOH !");
    }

    /**
     * Test enterText method.
     */
    public void testEnterText() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.enterText("BOOOOOOOH !");
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getDump();
    }

    /**
     * Test addTextListener method.
     */
    public void testAddTextListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        TextListenerTest listener = new TextListenerTest();
        operator1.addTextListener(listener);
        operator1.removeTextListener(listener);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class TextListenerTest implements TextListener {
        public void textValueChanged(TextEvent e) {
        }
    }

    /**
     * Test getCaretPosition method.
     */
    public void testGetCaretPosition() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getCaretPosition();
    }

    /**
     * Test getSelectedText method.
     */
    public void testGetSelectedText() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getSelectedText();
    }

    /**
     * Test getSelectionEnd method.
     */
    public void testGetSelectionEnd() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getSelectionEnd();
    }

    /**
     * Test getSelectionStart method.
     */
    public void testGetSelectionStart() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getSelectionStart();
    }

    /**
     * Test getText method.
     */
    public void testGetText() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getText();
    }

    /**
     * Test isEditable method.
     */
    public void testIsEditable() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.isEditable();
    }

    /**
     * Test select method.
     */
    public void testSelect() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.select(0, 10);
    }

    /**
     * Test selectAll method.
     */
    public void testSelectAll() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.selectAll();
    }

    /**
     * Test setCaretPosition method.
     */
    public void testSetCaretPosition() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setCaretPosition(0);
    }

    /**
     * Test setEditable method.
     */
    public void testSetEditable() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setEditable(true);
    }

    /**
     * Test setSelectionEnd method.
     */
    public void testSetSelectionEnd() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setSelectionEnd(1);
    }

    /**
     * Test setSelectionStart method.
     */
    public void testSetSelectionStart() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setSelectionStart(0);
    }

    /**
     * Test setText method.
     */
    public void testSetText() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setText("1");
    }

    /**
     * Test getTextDriver method.
     */
    public void testGetTextDriver() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        TextComponentOperator operator1 = new TextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.getTextDriver();
    }
}
