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

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.StringReader;
import java.io.StringWriter;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.operators.JTextComponentOperator.NoSuchTextException;
import org.netbeans.jemmy.operators.JTextComponentOperator.TextChooser;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JTextComponent.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JTextComponentOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the text component.
     */
    private JTextComponent textComponent;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JTextComponentOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        textComponent = new JTextField("JTextComponentOperatorTest");
        textComponent.setName("JTextComponentOperatorTest");
        frame.getContentPane().add(textComponent);
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
        TestSuite suite = new TestSuite(JTextComponentOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        JTextComponentOperator operator2 = new JTextComponentOperator(operator, "JTextComponentOperatorTest");
        assertNotNull(operator2);
        
        JTextComponentOperator operator3 = new JTextComponentOperator(operator, new NameComponentChooser("JTextComponentOperatorTest"));
        assertNotNull(operator3);
    }

    /**
     * Test findJTextComponent method.
     */
    public void testFindJTextComponent() {
        frame.setVisible(true);
        
        JTextComponent textComponent = JTextComponentOperator.findJTextComponent(frame, "JTextComponentOperatorTest", false, false);
        assertNotNull(textComponent);

        JTextComponent textComponent2 = JTextComponentOperator.findJTextComponent(frame, new NameComponentChooser("JTextComponentOperatorTest"));
        assertNotNull(textComponent2);
    }

    /**
     * Test waitJTextComponent method.
     */
    public void testWaitJTextComponent() {
        frame.setVisible(true);
        
        JTextComponent textComponent = JTextComponentOperator.waitJTextComponent(frame, "JTextComponentOperatorTest", false, false);
        assertNotNull(textComponent);

        JTextComponent textComponent2 = JTextComponentOperator.waitJTextComponent(frame, new NameComponentChooser("JTextComponentOperatorTest"));
        assertNotNull(textComponent2);
    }

    /**
     * Test getPositionByText method.
     */
    public void testGetPositionByText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getPositionByText("Text");
        operator1.getPositionByText("Text", new TextChooserTest());
    }
    
    /**
     * Inner class for testing.
     */
    public class TextChooserTest implements TextChooser {
        public boolean checkPosition(Document document, int offset) {
            return false;
        }

        public String getDescription() {
            return "";
        }
    }

    /**
     * Test enterText method.
     */
    public void testEnterText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.enterText("Hallo");
    }

    /**
     * Test changeCaretPosition method.
     */
    public void testChangeCaretPosition() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setText("test");
        operator1.changeCaretPosition(0);
        operator1.changeCaretPosition("test", false);
        operator1.changeCaretPosition("test", 0, false);
        
        try {
            operator1.changeCaretPosition("blabla", 0, false);
            fail();
        }
        catch(NoSuchTextException exception) {
        }
    }

    /**
     * Test typeText method.
     */
    public void testTypeText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.typeText("Boooooh!");
    }

    /**
     * Test selectText method.
     */
    public void testSelectText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setText("Hallo");
        operator1.selectText("Hallo");
    }

    /**
     * Test clearText method.
     */
    public void testClearText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.clearText();
    }

    /**
     * Test scrollToPosition method.
     */
    public void testScrollToPosition() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToPosition(0);
    }

    /**
     * Test getDisplayedText method.
     */
    public void testGetDisplayedText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getDisplayedText();
    }

    /**
     * Test waitText method.
     */
    public void testWaitText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setText("Hallo");
        operator1.waitText("Hallo");
    }

    /**
     * Test waitCaretPosition method.
     */
    public void testWaitCaretPosition() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setCaretPosition(0);
        operator1.waitCaretPosition(0);
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getDump();
    }

    /**
     * Test addCaretListener method.
     */
    public void testAddCaretListener() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        CaretListenerTest listener = new CaretListenerTest();
        operator1.addCaretListener(listener);
        operator1.removeCaretListener(listener);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class CaretListenerTest implements CaretListener {
        public void caretUpdate(CaretEvent e) {
        }
    }

    /**
     * Test copy method.
     */
    public void testCopy() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.copy();
    }

    /**
     * Test cut method.
     */
    public void testCut() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.cut();
    }

    /**
     * Test getActions method.
     */
    public void testGetActions() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getActions();
    }

    /**
     * Test getCaret method.
     */
    public void testGetCaret() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setCaret(new DefaultCaret());
        operator1.getCaret();
    }

    /**
     * Test getCaretColor method.
     */
    public void testGetCaretColor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setCaretColor(Color.black);
        operator1.getCaretColor();
    }

    /**
     * Test getCaretPosition method.
     */
    public void testGetCaretPosition() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setCaretPosition(0);
        operator1.getCaretPosition();

    }

    /**
     * Test getDisabledTextColor method.
     */
    public void testGetDisabledTextColor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setDisabledTextColor(Color.black);
        operator1.getDisabledTextColor();
    }

    /**
     * Test getDocument method.
     */
    public void testGetDocument() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setDocument(new DefaultStyledDocument());
        operator1.getDocument();
    }

    /**
     * Test getFocusAccelerator method.
     */
    public void testGetFocusAccelerator() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setFocusAccelerator('a');
        operator1.getFocusAccelerator();
    }

    /**
     * Test getHighlighter method.
     */
    public void testGetHighlighter() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setHighlighter(new DefaultHighlighter());
        operator1.getHighlighter();
    }

    /**
     * Test getKeymap method.
     */
    public void testGetKeymap() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setKeymap(operator1.getKeymap());
    }

    /**
     * Test getMargin method.
     */
    public void testGetMargin() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);

        operator1.setMargin(new Insets(0, 0, 0, 0));
        operator1.getMargin();
    }

    /**
     * Test getPreferredScrollableViewportSize method.
     */
    public void testGetPreferredScrollableViewportSize() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getPreferredScrollableViewportSize();
    }

    /**
     * Test getScrollableBlockIncrement method.
     */
    public void testGetScrollableBlockIncrement() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getScrollableBlockIncrement(new Rectangle(0,0), 0, 0);
    }

    /**
     * Test getScrollableTracksViewportHeight method.
     */
    public void testGetScrollableTracksViewportHeight() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getScrollableTracksViewportHeight();
    }

    /**
     * Test getScrollableTracksViewportWidth method.
     */
    public void testGetScrollableTracksViewportWidth() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getScrollableTracksViewportWidth();
    }

    /**
     * Test getScrollableUnitIncrement method.
     */
    public void testGetScrollableUnitIncrement() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getScrollableUnitIncrement(new Rectangle(0,0), 0, 0);
    }

    /**
     * Test getSelectedText method.
     */
    public void testGetSelectedText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.getSelectedText();
    }

    /**
     * Test getSelectedTextColor method.
     */
    public void testGetSelectedTextColor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelectedTextColor(Color.black);
        operator1.getSelectedTextColor();
    }

    /**
     * Test getSelectionColor method.
     */
    public void testGetSelectionColor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelectionColor(Color.black);
        operator1.getSelectionColor();
    }

    /**
     * Test getSelectionEnd method.
     */
    public void testGetSelectionEnd() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelectionEnd(0);
        operator1.getSelectionEnd();
    }

    /**
     * Test getSelectionStart method.
     */
    public void testGetSelectionStart() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelectionStart(0);
        operator1.getSelectionStart();
    }

    /**
     * Test getText method.
     */
    public void testGetText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setText("12345");
        operator1.getText();
        operator1.getText(0, 0);
    }

    /**
     * Test getUI method.
     */
    public void testGetUI() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setUI(operator1.getUI());
    }

    /**
     * Test isEditable method.
     */
    public void testIsEditable() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.setEditable(true);
        operator1.isEditable();
    }

    /**
     * Test modelToView method.
     */
    public void testModelToView() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.modelToView(0);
    }

    /**
     * Test moveCaretPosition method.
     */
    public void testMoveCaretPosition() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.moveCaretPosition(0);
    }

    /**
     * Test paste method.
     */
    public void testPaste() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.paste();
    }

    /**
     * Test read method.
     */
    public void testRead() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.read(new StringReader("String"), "String");
    }

    /**
     * Test replaceSelection method.
     */
    public void testReplaceSelection() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.replaceSelection("Hallo");
    }

    /**
     * Test select method.
     */
    public void testSelect() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.select(0, 0);
    }

    /**
     * Test selectAll method.
     */
    public void testSelectAll() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.selectAll();
    }

    /**
     * Test viewToModel method.
     */
    public void testViewToModel() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.viewToModel(new Point(0, 0));
    }

    /**
     * Test write method.
     */
    public void testWrite() {
        frame.setVisible(true); 
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextComponentOperator operator1 = new JTextComponentOperator(operator);
        assertNotNull(operator1);
        
        operator1.write(new StringWriter());
    }
}
