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
 */
package org.netbeans.jemmy.operators;



import javax.swing.JFrame;

import javax.swing.JPanel;

import javax.swing.JSplitPane;

import javax.swing.JTextPane;

import junit.framework.Test;

import junit.framework.TestCase;

import junit.framework.TestSuite;

import org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster;

import org.netbeans.jemmy.util.NameComponentChooser;



/**

 * A JUnit test for JSplitPaneOperator.

 *

 * @author Manfred Riem (mriem@netbeans.org)

 * @version $Revision$

 */

public class JSplitPaneOperatorTest extends TestCase {

    /**

     * Stores the frame.

     */

    private JFrame frame;

    

    /**

     * Stores the split pane.

     */

    private JSplitPane splitPane;

    

    /**

     * Constructor.

     *

     * @param testName the name of the test.

     */

    public JSplitPaneOperatorTest(String testName) {

        super(testName);

    }



    /**

     * Setup before testing.

     */

    protected void setUp() throws Exception {

        frame = new JFrame();

        splitPane = new JSplitPane();

        splitPane.setName("JSplitPane");

        frame.getContentPane().add(splitPane);

        frame.setSize(400, 300);

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

        TestSuite suite = new TestSuite(JSplitPaneOperatorTest.class);

        

        return suite;

    }

    

    /**

     * Test constructor.

     */

    public void testConstructor() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        JSplitPaneOperator operator2 = new JSplitPaneOperator(operator, new NameComponentChooser("JSplitPane"));

        assertNotNull(operator2);

        

        JSplitPaneOperator operator3 = new JSplitPaneOperator(splitPane);

        assertNotNull(operator3);

    }



    /**

     * Test findJSplitPane method.

     */

    public void testFindJSplitPane() {

        frame.setVisible(true);

        

        JSplitPane pane1 = JSplitPaneOperator.findJSplitPane(frame);

        assertNotNull(pane1);

        

        JSplitPane pane2 = JSplitPaneOperator.findJSplitPane(frame, new NameComponentChooser("JSplitPane"));

        assertNotNull(pane2);

    }



    /**

     * Test findJSplitPaneUnder method.

     */

    public void testFindJSplitPaneUnder() {

        frame.setVisible(true);

        

        JSplitPane pane1 = JSplitPaneOperator.findJSplitPaneUnder(frame);

        assertNull(pane1);

        

        JSplitPane pane2 = JSplitPaneOperator.findJSplitPaneUnder(frame, new NameComponentChooser("JSplitPane"));

        assertNull(pane2);

    }



    /**

     * Test waitJSplitPane method.

     */

    public void testWaitJSplitPane() {

        frame.setVisible(true);

        

        JSplitPane pane1 = JSplitPaneOperator.waitJSplitPane(frame);

        assertNotNull(pane1);

        

        JSplitPane pane2 = JSplitPaneOperator.waitJSplitPane(frame, new NameComponentChooser("JSplitPane"));

        assertNotNull(pane2);

    }



    /**

     * Test findDivider method.

     */

    public void testFindDivider() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.findDivider();

    }



    /**

     * Test getDivider method.

     */

    public void testGetDivider() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.getDivider();

    }



    /**

     * Test scrollTo method.

     */

    public void testScrollTo() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.scrollTo(new ScrollAdjusterTest());

    }

    

    /**

     * Inner class.

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

     * Test moveDivider method.

     */

    public void testMoveDivider() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setOneTouchExpandable(true);

        operator1.moveDivider(250);

        operator1.moveDivider(1.0);

    }



    /**

     * Test moveToMinimum method.

     */

    public void testMoveToMinimum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setOneTouchExpandable(true);

        operator1.moveToMinimum();

    }



    /**

     * Test moveToMaximum method.

     */

    public void testMoveToMaximum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setOneTouchExpandable(true);

        operator1.moveToMaximum();

    }



    /**

     * Test expandRight method.

     */

    public void testExpandRight() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setOneTouchExpandable(true);

        operator1.expandRight();

    }



    /**

     * Test expandLeft method.

     */

    public void testExpandLeft() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setOneTouchExpandable(true);

        operator1.expandLeft();

    }



    /**

     * Test getDump method.

     */

    public void testGetDump() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.getDump();

    }



    /**

     * Test getBottomComponent method.

     */

    public void testGetBottomComponent() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setBottomComponent(new JPanel());

        operator1.getBottomComponent();

    }



    /**

     * Test getDividerLocation method.

     */

    public void testGetDividerLocation() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setDividerLocation(1.0);

        operator1.setDividerLocation(operator1.getDividerLocation());

    }



    /**

     * Test getDividerSize method.

     */

    public void testGetDividerSize() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setDividerSize(operator1.getDividerSize());

    }



    /**

     * Test getLastDividerLocation method.

     */

    public void testGetLastDividerLocation() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setLastDividerLocation(operator1.getLastDividerLocation());

    }



    /**

     * Test getLeftComponent method.

     */

    public void testGetLeftComponent() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setLeftComponent(operator1.getLeftComponent());

    }



    /**

     * Test getMaximumDividerLocation method.

     */

    public void testGetMaximumDividerLocation() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.getMaximumDividerLocation();

    }



    /**

     * Test getMinimumDividerLocation method.

     */

    public void testGetMinimumDividerLocation() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.getMinimumDividerLocation();

    }



    /**

     * Test getOrientation method.

     */

    public void testGetOrientation() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setOrientation(operator1.getOrientation());

    }



    /**

     * Test getRightComponent method.

     */

    public void testGetRightComponent() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setRightComponent(operator1.getRightComponent());

    }



    /**

     * Test getTopComponent method.

     */

    public void testGetTopComponent() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setTopComponent(operator1.getTopComponent());

    }



    /**

     * Test getUI method.

     */

    public void testGetUI() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setUI(operator1.getUI());

    }



    /**

     * Test isContinuousLayout method.

     */

    public void testIsContinuousLayout() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setContinuousLayout(operator1.isContinuousLayout());

    }



    /**

     * Test isOneTouchExpandable method.

     */

    public void testIsOneTouchExpandable() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.setOneTouchExpandable(operator1.isOneTouchExpandable());

    }



    /**

     * Test resetToPreferredSizes method.

     */

    public void testResetToPreferredSizes() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSplitPaneOperator operator1 = new JSplitPaneOperator(operator);

        assertNotNull(operator1);

        

        operator1.resetToPreferredSizes();

    }

}

