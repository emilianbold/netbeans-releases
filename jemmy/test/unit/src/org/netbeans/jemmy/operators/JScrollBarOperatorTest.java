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



import java.awt.event.AdjustmentEvent;

import java.awt.event.AdjustmentListener;

import javax.swing.JFrame;

import javax.swing.JScrollBar;

import junit.framework.Test;

import junit.framework.TestCase;

import junit.framework.TestSuite;

import org.netbeans.jemmy.Waitable;

import org.netbeans.jemmy.util.NameComponentChooser;



/**

 * A JUnit test for JScrollBarOperator.

 *

 * @author Manfred Riem (mriem@netbeans.org)

 * @version $Revision$

 */

public class JScrollBarOperatorTest extends TestCase {

    /**

     * Stores the frame we use.

     */

    private JFrame frame;

    

    /**

     * Stores the scroll bar.

     */

    private JScrollBar scrollBar;

    

    /**

     * Constructor.

     *

     * @param testName the name of the test.

     */

    public JScrollBarOperatorTest(String testName) {

        super(testName);

    }



    /**

     * Setup before testing.

     */

    protected void setUp() throws Exception {

        frame = new JFrame();

        scrollBar = new JScrollBar();

        scrollBar.setName("JScrollBarOperatorTest");

        frame.getContentPane().add(scrollBar);

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

        TestSuite suite = new TestSuite(JScrollBarOperatorTest.class);

        

        return suite;

    }

    

    /**

     * Test constructor.

     */

    public void testConstructor() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        JScrollBarOperator operator2 = new JScrollBarOperator(operator, new NameComponentChooser("JScrollBarOperatorTest"));

        assertNotNull(operator2);

    }



    /**

     * Test findJScrollBar method.

     */

    public void testFindJScrollBar() {

        frame.setVisible(true);

        

        JScrollBar scrollBar1 = JScrollBarOperator.findJScrollBar(frame);

        assertNotNull(scrollBar1);



        JScrollBar scrollBar2 = JScrollBarOperator.findJScrollBar(frame, new NameComponentChooser("JScrollBarOperatorTest"));

        assertNotNull(scrollBar2);

    }



    /**

     * Test waitJScrollBar method.

     */

    public void testWaitJScrollBar() {

        frame.setVisible(true);

        

        JScrollBar scrollBar1 = JScrollBarOperator.waitJScrollBar(frame);

        assertNotNull(scrollBar1);



        JScrollBar scrollBar2 = JScrollBarOperator.waitJScrollBar(frame, new NameComponentChooser("JScrollBarOperatorTest"));

        assertNotNull(scrollBar2);

    }



    /**

     * Test scroll method.

     */

    public void testScroll() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.scroll(true);

    }



    /**

     * Test scrollTo method.

     */

    public void testScrollTo() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        WaitableTest waitable = new WaitableTest();

        operator1.scrollTo(waitable, null, true);

    }

    

    /**

     * Inner class needed for testing.

     */

    public class WaitableTest implements Waitable {

        public Object actionProduced(Object obj) {

            return this;

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

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.scrollToValue(1.0);

    }



    /**

     * Test scrollToMinimum method.

     */

    public void testScrollToMinimum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.scrollToMinimum();

    }



    /**

     * Test scrollToMaximum method.

     */

    public void testScrollToMaximum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.scrollToMaximum();

    }



    /**

     * Test getDecreaseButton method.

     */

    public void testGetDecreaseButton() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.getDecreaseButton();

    }



    /**

     * Test getIncreaseButton method.

     */

    public void testGetIncreaseButton() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.getIncreaseButton();

    }



    /**

     * Test getDump method.

     */

    public void testGetDump() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.getDump();

    }



    /**

     * Test addAdjustmentListener method.

     */

    public void testAddAdjustmentListener() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

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

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.setBlockIncrement(operator1.getBlockIncrement());

        operator1.getBlockIncrement(0);

    }



    /**

     * Test getMaximum method.

     */

    public void testGetMaximum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.setMaximum(operator1.getMaximum());

    }



    /**

     * Test getMinimum method.

     */

    public void testGetMinimum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.setMinimum(operator1.getMinimum());

    }



    /**

     * Test getModel method.

     */

    public void testGetModel() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.setModel(operator1.getModel());

    }



    /**

     * Test getOrientation method.

     */

    public void testGetOrientation() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.setOrientation(operator1.getOrientation());

    }



    /**

     * Test getUI method.

     */

    public void testGetUI() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.getUI();

    }



    /**

     * Test getUnitIncrement method.

     */

    public void testGetUnitIncrement() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.setUnitIncrement(operator1.getUnitIncrement());

    }



    /**

     * Test getValue method.

     */

    public void testGetValue() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.setValue(operator1.getValue());

    }



    /**

     * Test getValueIsAdjusting method.

     */

    public void testGetValueIsAdjusting() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.setValueIsAdjusting(operator1.getValueIsAdjusting());

    }



    /**

     * Test getVisibleAmount method.

     */

    public void testGetVisibleAmount() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.setVisibleAmount(operator1.getVisibleAmount());

    }



    /**

     * Test setValues method.

     */

    public void testSetValues() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JScrollBarOperator operator1 = new JScrollBarOperator(operator);

        assertNotNull(operator1);

        

        operator1.setValues(0, 0, 0, 0);

    }

}

