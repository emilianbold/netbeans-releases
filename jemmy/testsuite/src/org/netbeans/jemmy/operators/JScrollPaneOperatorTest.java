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

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.plaf.ScrollPaneUI;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JScrollPaneOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JScrollPaneOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the scroll pane.
     */
    private JScrollPane scrollPane;
    
    /**
     * Stores the text area.
     */
    private JTextArea textArea;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JScrollPaneOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup for testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        textArea = new JTextArea("JTextArea");
        textArea.setSize(1000, 1000);
        textArea.setMaximumSize(new Dimension(1000, 1000));
        textArea.setMinimumSize(new Dimension(1000, 1000));
        scrollPane = new JScrollPane(textArea);
        scrollPane.setName("JScrollPaneOperatorTest");
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frame.getContentPane().add(scrollPane);
        frame.setSize(200, 200);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(false);
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(JScrollPaneOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        JScrollPaneOperator operator2 = new JScrollPaneOperator(operator, new NameComponentChooser("JScrollPaneOperatorTest"));
        assertNotNull(operator1);
    }

    /**
     * Test findJScrollPane method.
     */
    public void testFindJScrollPane() {
        frame.setVisible(true);
        
        JScrollPane scrollPane1 = JScrollPaneOperator.findJScrollPane(frame);
        assertNotNull(scrollPane1);
        
        JScrollPane scrollPane2 = JScrollPaneOperator.findJScrollPane(frame, new NameComponentChooser("JScrollPaneOperatorTest"));
        assertNotNull(scrollPane2);
    }

    /**
     * Test findJScrollPaneUnder method.
     */
    public void testFindJScrollPaneUnder() {
        
    }

    /**
     * Test waitJScrollPane method.
     */
    public void testWaitJScrollPane() {
        frame.setVisible(true);
        
        JScrollPane scrollPane1 = JScrollPaneOperator.waitJScrollPane(frame);
        assertNotNull(scrollPane1);
        
        JScrollPane scrollPane2 = JScrollPaneOperator.waitJScrollPane(frame, new NameComponentChooser("JScrollPaneOperatorTest"));
        assertNotNull(scrollPane2);
    }

    /**
     * Test setValues method.
     */
    public void testSetValues() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setValues(1, 100);
    }

    /**
     * Test scrollToHorizontalValue method.
     */
    public void testScrollToHorizontalValue() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToHorizontalValue(0);
        operator1.scrollToHorizontalValue(0.0);
    }

    /**
     * Test scrollToVerticalValue method.
     */
    public void testScrollToVerticalValue() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToVerticalValue(0);
        operator1.scrollToVerticalValue(0.0);
    }

    /**
     * Test scrollToValues method.
     */
    public void testScrollToValues() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToValues(0, 0);
        operator1.scrollToValues(0.0, 0.0);
    }

    /**
     * Test scrollToTop method.
     */
    public void testScrollToTop() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToTop();
    }

    /**
     * Test scrollToBottom method.
     */
    public void testScrollToBottom() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToBottom();
    }

    /**
     * Test scrollToLeft method.
     */
    public void testScrollToLeft() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToLeft();
    }

    /**
     * Test scrollToRight method.
     */
    public void testScrollToRight() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToRight();
    }

    /**
     * Test scrollToComponentRectangle method.
     */
    public void testScrollToComponentRectangle() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToComponentRectangle(textArea, 0, 0, 10, 10);
    }

    /**
     * Test scrollToComponentPoint method.
     */
    public void testScrollToComponentPoint() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToComponentPoint(textArea, 10, 10);
    }

    /**
     * Test scrollToComponent method.
     */
    public void testScrollToComponent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToComponent(textArea);
    }

    /**
     * Test getHScrollBarOperator method.
     */
    public void testGetHScrollBarOperator() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getHScrollBarOperator());
    }

    /**
     * Test getVScrollBarOperator method.
     */
    public void testGetVScrollBarOperator() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getVScrollBarOperator());
    }

    /**
     * Test checkInside method.
     */
    public void testCheckInside() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.checkInside(textArea);
    }

    /**
     * Test createHorizontalScrollBar method.
     */
    public void testCreateHorizontalScrollBar() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.createHorizontalScrollBar();
    }

    /**
     * Test createVerticalScrollBar method.
     */
    public void testCreateVerticalScrollBar() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.createVerticalScrollBar();
    }

    /**
     * Test getColumnHeader method.
     */
    public void testGetColumnHeader() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setColumnHeader(null);
        assertNull(operator1.getColumnHeader());
    }

    /**
     * Test getCorner method.
     */
    public void testGetCorner() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setCorner(JScrollPane.LOWER_LEFT_CORNER, new JPanel());
        assertNotNull(operator1.getCorner(JScrollPane.LOWER_LEFT_CORNER));
    }

    /**
     * Test getHorizontalScrollBar method.
     */
    public void testGetHorizontalScrollBar() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setHorizontalScrollBar(new JScrollBar());
        operator1.getHorizontalScrollBar();
    }

    /**
     * Test getHorizontalScrollBarPolicy method.
     */
    public void testGetHorizontalScrollBarPolicy() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        operator1.getHorizontalScrollBarPolicy();
    }

    /**
     * Test getRowHeader method.
     */
    public void testGetRowHeader() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setRowHeader(null);
        operator1.getRowHeader();
    }

    /**
     * Test getUI method.
     */
    public void testGetUI() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        ScrollPaneUITest ui = new ScrollPaneUITest();
        operator1.setUI(ui);
        assertEquals(ui, operator1.getUI());
    }
    
    /**
     * Inner class needed for testing.
     */
    public class ScrollPaneUITest extends ScrollPaneUI {
    }

    /**
     * Test getVerticalScrollBar method.
     */
    public void testGetVerticalScrollBar() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setVerticalScrollBar(new JScrollBar());
        operator1.getVerticalScrollBar();
    }

    /**
     * Test getVerticalScrollBarPolicy method.
     */
    public void testGetVerticalScrollBarPolicy() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        operator1.getVerticalScrollBarPolicy();
    }

    /**
     * Test getViewport method.
     */
    public void testGetViewport() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setViewport(null);
        operator1.getViewport();
    }

    /**
     * Test getViewportBorder method.
     */
    public void testGetViewportBorder() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.setViewportBorder(null);
        operator1.getViewportBorder();
    }

    /**
     * Test getViewportBorderBounds method.
     */
    public void testGetViewportBorderBounds() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.getViewportBorderBounds();
    }

    /**
     * Test setColumnHeaderView method.
     */
    public void testSetColumnHeaderView() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setColumnHeaderView(null);
    }

    /**
     * Test setRowHeaderView method.
     */
    public void testSetRowHeaderView() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setRowHeaderView(null);
    }

    /**
     * Test setViewportView method.
     */
    public void testSetViewportView() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPaneOperator operator1 = new JScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setViewportView(null);
    }    
}
