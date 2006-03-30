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
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for Scrollbar.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class ScrollbarOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the scrollbar.
     */
    private Scrollbar scrollbar;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public ScrollbarOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a major problem occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        scrollbar = new Scrollbar();
        scrollbar.setName("ScrollbarOperatorTest");
        frame.add(scrollbar);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when a major problem occurs.
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
        TestSuite suite = new TestSuite(ScrollbarOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollbarOperator operator1 = new ScrollbarOperator(operator);
        assertNotNull(operator1);
        
        ScrollbarOperator operator2 = new ScrollbarOperator(operator, new NameComponentChooser("ScrollbarOperatorTest"));
        assertNotNull(operator2);
    }

    /**
     * Test findScrollbar method.
     */
    public void testFindScrollbar() {
        frame.setVisible(true);
        
        Scrollbar scrollbar1 = ScrollbarOperator.findScrollbar(frame);
        assertNotNull(scrollbar1);
        
        Scrollbar scrollbar2 = ScrollbarOperator.findScrollbar(frame, new NameComponentChooser("ScrollbarOperatorTest"));
        assertNotNull(scrollbar2);
    }

    /**
     * Test waitScrollbar method.
     */
    public void testWaitScrollbar() {
        frame.setVisible(true);
        
        Scrollbar scrollbar1 = ScrollbarOperator.waitScrollbar(frame);
        assertNotNull(scrollbar1);
        
        Scrollbar scrollbar2 = ScrollbarOperator.waitScrollbar(frame, new NameComponentChooser("ScrollbarOperatorTest"));
        assertNotNull(scrollbar2);
    }

    /**
     * Test scrollTo method.
     */
    public void testScrollTo() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollbarOperator operator1 = new ScrollbarOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollTo(new ScrollAdjusterTest());
        operator1.scrollTo(new WaitableTest(), "", false);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class ScrollAdjusterTest implements ScrollAdjuster {
        public int getScrollDirection() {
            return 0;
        }

        public int getScrollOrientation() {
            return 0;
        }

        public String getDescription() {
            return "";
        }
    }
    
    /**
     * Inner class needed for testing.
     */
    public class WaitableTest implements Waitable {
        public Object actionProduced(Object obj) {
            return "";
        }

        public String getDescription() {
            return "";
        }
    }

    /**
     * Test scrollToValue method.
     */
    public void testScrollToValue() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollbarOperator operator1 = new ScrollbarOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToValue(1.0);
        operator1.scrollToValue(1);
    }

    /**
     * Test scrollToMinimum method.
     */
    public void testScrollToMinimum() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollbarOperator operator1 = new ScrollbarOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToMinimum();
    }

    /**
     * Test scrollToMaximum method.
     */
    public void testScrollToMaximum() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollbarOperator operator1 = new ScrollbarOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToMaximum();
    }

    /**
     * Test addAdjustmentListener method.
     */
    public void testAddAdjustmentListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollbarOperator operator1 = new ScrollbarOperator(operator);
        assertNotNull(operator1);
        
        AdjustmentListenerTest listener = new AdjustmentListenerTest();
        operator1.addAdjustmentListener(listener);
        operator1.removeAdjustmentListener(listener);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class AdjustmentListenerTest implements AdjustmentListener {
        public void adjustmentValueChanged(AdjustmentEvent e) {
        }
    }

    /**
     * Test getBlockIncrement method.
     */
    public void testGetBlockIncrement() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollbarOperator operator1 = new ScrollbarOperator(operator);
        assertNotNull(operator1);
        
        operator1.setBlockIncrement(operator1.getBlockIncrement());
    }

    /**
     * Test getMaximum method.
     */
    public void testGetMaximum() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollbarOperator operator1 = new ScrollbarOperator(operator);
        assertNotNull(operator1);

        operator1.setMaximum(operator1.getMaximum());
    }

    /**
     * Test getMinimum method.
     */
    public void testGetMinimum() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollbarOperator operator1 = new ScrollbarOperator(operator);
        assertNotNull(operator1);

        operator1.setMinimum(operator1.getMinimum());
    }

    /**
     * Test getOrientation method.
     */
    public void testGetOrientation() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollbarOperator operator1 = new ScrollbarOperator(operator);
        assertNotNull(operator1);

        operator1.setOrientation(operator1.getOrientation());
    }

    /**
     * Test getUnitIncrement method.
     */
    public void testGetUnitIncrement() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollbarOperator operator1 = new ScrollbarOperator(operator);
        assertNotNull(operator1);

        operator1.setUnitIncrement(operator1.getUnitIncrement());
    }

    /**
     * Test getValue method.
     */
    public void testGetValue() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollbarOperator operator1 = new ScrollbarOperator(operator);
        assertNotNull(operator1);

        operator1.setValue(operator1.getValue());
    }

    /**
     * Test getVisibleAmount method.
     */
    public void testGetVisibleAmount() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollbarOperator operator1 = new ScrollbarOperator(operator);
        assertNotNull(operator1);

        operator1.setVisibleAmount(operator1.getVisibleAmount());
    }
    
    /**
     * Test setValues method.
     */
    public void testSetValues() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollbarOperator operator1 = new ScrollbarOperator(operator);
        assertNotNull(operator1);

        operator1.setValues(0, 1, 0, 1);
    }
}
