/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 *
 * The Original Software is the Jemmy library. The Initial Developer of the
 * Original Software is Alexandre Iline. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * ---------------------------------------------------------------------------
 *
 */
package org.netbeans.jemmy.operators;

import java.awt.Frame;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for ScrollPaneOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class ScrollPaneOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the scroll pane.
     */
    private ScrollPane scrollPane;
    
    /**
     * Stores the panel inside the scroll pane.
     */
    private Panel panel;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public ScrollPaneOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a serious problem occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        panel = new Panel();
        panel.setSize(400, 300);
        scrollPane = new ScrollPane();
        scrollPane.add(panel);
        scrollPane.setName("ScrollPaneOperatorTest");
        frame.add(scrollPane);
        frame.setSize(200, 100);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when a serious problem occurs.
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
        TestSuite suite = new TestSuite(ScrollPaneOperatorTest.class);
        
        return suite;
    }

    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        ScrollPaneOperator operator2 = new ScrollPaneOperator(operator, new NameComponentChooser("ScrollPaneOperatorTest"));
        assertNotNull(operator2);
    }
    
    /**
     * Test findScrollPane method.
     */
    public void testFindScrollPane() {
        frame.setVisible(true);
        
        ScrollPane scrollPane1 = ScrollPaneOperator.findScrollPane(frame);
        assertNotNull(scrollPane1);
        
        ScrollPane scrollPane2 = ScrollPaneOperator.findScrollPane(frame, new NameComponentChooser("ScrollPaneOperatorTest"));
        assertNotNull(scrollPane2);
    }

    /**
     * Test findScrollPaneUnder method.
     */
    public void testFindScrollPaneUnder() {
        frame.setVisible(true);
        
        ScrollPane scrollPane1 = ScrollPaneOperator.findScrollPaneUnder(frame);
        assertNull(scrollPane1);
    }

    /**
     * Test waitScrollPane method.
     */
    public void testWaitScrollPane() {
        frame.setVisible(true);
        
        ScrollPane scrollPane1 = ScrollPaneOperator.waitScrollPane(frame);
        assertNotNull(scrollPane1);

        ScrollPane scrollPane2 = ScrollPaneOperator.waitScrollPane(frame, new NameComponentChooser("ScrollPaneOperatorTest"));
        assertNotNull(scrollPane2);
    }

    /**
     * Test setValues method.
     */
    public void testSetValues() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setValues(0, 0);
    }

    /**
     * Test scrollTo method.
     */
    public void testScrollTo() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollTo(new ScrollAdjusterTest());
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
     * Test scrollToHorizontalValue method.
     */
    public void testScrollToHorizontalValue() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToHorizontalValue(0);
        operator1.scrollToHorizontalValue(0.0);
    }

    /**
     * Test scrollToVerticalValue method.
     */
    public void testScrollToVerticalValue() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToVerticalValue(0);
        operator1.scrollToVerticalValue(0.0);
    }

    /**
     * Test scrollToValues method.
     */
    public void testScrollToValues() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.scrollToValues(0, 0);
        operator1.scrollToValues(0.0, 0.0);
    }

    /**
     * Test scrollToTop method.
     */
    public void testScrollToTop() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.scrollToTop();
    }

    /**
     * Test scrollToBottom method.
     */
    public void testScrollToBottom() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToBottom();
    }

    /**
     * Test scrollToLeft method.
     */
    public void testScrollToLeft() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.scrollToLeft();
    }

    /**
     * Test scrollToRight method.
     */
    public void testScrollToRight() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.scrollToRight();
    }

    /**
     * Test scrollToComponentRectangle method.
     */
    public void testScrollToComponentRectangle() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.scrollToComponentRectangle(panel, 1, 1, 10, 10);
    }

    /**
     * Test scrollToComponentPoint method.
     */
    public void testScrollToComponentPoint() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.scrollToComponentPoint(panel, 7, 5);
    }

    /**
     * Test scrollToComponent method.
     */
    public void testScrollToComponent() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        // operator1.scrollToComponent(panel);
    }

    /**
     * Test checkInside method.
     */
    public void testCheckInside() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.checkInside(panel);
    }

    /**
     * Test isScrollbarVisible method.
     */
    public void testIsScrollbarVisible() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.isScrollbarVisible(Scrollbar.HORIZONTAL);
        operator1.isScrollbarVisible(Scrollbar.VERTICAL);
    }

    /**
     * Test getHAdjustable method.
     */
    public void testGetHAdjustable() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.getHAdjustable();
    }

    /**
     * Test getHScrollbarHeight method.
     */
    public void testGetHScrollbarHeight() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.getHScrollbarHeight();
    }

    /**
     * Test getScrollPosition method.
     */
    public void testGetScrollPosition() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.setScrollPosition(operator1.getScrollPosition());
        operator1.setScrollPosition(operator1.getScrollPosition().x, operator1.getScrollPosition().y);
    }

    /**
     * Test getScrollbarDisplayPolicy method.
     */
    public void testGetScrollbarDisplayPolicy() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.getScrollbarDisplayPolicy();
    }

    /**
     * Test getVAdjustable method.
     */
    public void testGetVAdjustable() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.getVAdjustable();
    }

    /**
     * Test getVScrollbarWidth method.
     */
    public void testGetVScrollbarWidth() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.getVScrollbarWidth();
    }

    /**
     * Test getViewportSize method.
     */
    public void testGetViewportSize() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);

        operator1.getViewportSize();
    }

    /**
     * Test paramString method.
     */
    public void testParamString() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ScrollPaneOperator operator1 = new ScrollPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.paramString();
    }
}
