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



import java.util.ArrayList;

import java.util.Calendar;

import java.util.Date;

import javax.swing.JFrame;

import javax.swing.JSpinner;

import javax.swing.JTextField;

import javax.swing.SpinnerDateModel;

import javax.swing.SpinnerListModel;

import javax.swing.SpinnerModel;

import javax.swing.SpinnerNumberModel;

import javax.swing.SwingConstants;

import javax.swing.event.ChangeEvent;

import javax.swing.event.ChangeListener;

import javax.swing.plaf.SpinnerUI;

import junit.framework.Test;

import junit.framework.TestCase;

import junit.framework.TestSuite;

import org.netbeans.jemmy.JemmyException;

import org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster;

import org.netbeans.jemmy.operators.JSpinnerOperator.DateScrollAdjuster;

import org.netbeans.jemmy.operators.JSpinnerOperator.DateSpinnerOperator;

import org.netbeans.jemmy.operators.JSpinnerOperator.ExactScrollAdjuster;

import org.netbeans.jemmy.operators.JSpinnerOperator.ListScrollAdjuster;

import org.netbeans.jemmy.operators.JSpinnerOperator.ListSpinnerOperator;

import org.netbeans.jemmy.operators.JSpinnerOperator.NumberScrollAdjuster;

import org.netbeans.jemmy.operators.JSpinnerOperator.NumberSpinnerOperator;

import org.netbeans.jemmy.operators.JSpinnerOperator.SpinnerModelException;

import org.netbeans.jemmy.operators.JSpinnerOperator.ToStringScrollAdjuster;

import org.netbeans.jemmy.util.NameComponentChooser;



/**

 * A JUnit test for JSpinnerOperator.

 *

 * @author Manfred Riem (mriem@netbeans.org)

 * @version $Revision$

 */

public class JSpinnerOperatorTest extends TestCase {

    /**

     * Stores the frame we use.

     */

    private JFrame frame;

    

    /**

     * Stores the spinner we use.

     */

    private JSpinner spinner;

    

    /**

     * Constructor.

     *

     * @param testName the name of the test.

     */

    public JSpinnerOperatorTest(String testName) {

        super(testName);

    }

    

    /**

     * Setup before testing.

     */

    protected void setUp() throws Exception {

        frame = new JFrame();

        spinner = new JSpinner();

        spinner.setName("JSpinnerOperatorTest");

        frame.getContentPane().add(spinner);

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

        TestSuite suite = new TestSuite(JSpinnerOperatorTest.class);

        

        return suite;

    }

    

    /**

     * Test constructor.

     */

    public void testConstructor() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        JSpinnerOperator operator3 = new JSpinnerOperator(operator, new NameComponentChooser("JSpinnerOperatorTest"));

        assertNotNull(operator3);

        

        operator3.setValue(new Integer(1));

        JSpinnerOperator operator4 = new JSpinnerOperator(operator, "1");

        assertNotNull(operator4);

    }

    

    /**

     * Test findJSpinner method.

     */

    public void testFindJSpinner() {

        frame.setVisible(true);

        

        JSpinner spinner1 = JSpinnerOperator.findJSpinner(frame);

        assertNotNull(spinner1);

        

        JSpinner spinner2 = JSpinnerOperator.findJSpinner(frame, new NameComponentChooser("JSpinnerOperatorTest"));

        assertNotNull(spinner2);

    }

    

    /**

     * Test waitJSpinner method.

     */

    public void testWaitJSpinner() {

        frame.setVisible(true);

        

        JSpinner spinner1 = JSpinnerOperator.waitJSpinner(frame);

        assertNotNull(spinner1);

        

        JSpinner spinner2 = JSpinnerOperator.waitJSpinner(frame, new NameComponentChooser("JSpinnerOperatorTest"));

        assertNotNull(spinner2);

    }

    

    /**

     * Test checkModel method.

     */

    public void testCheckModel() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        try {

            JSpinnerOperator.checkModel(operator2, String.class);

            fail();

        } catch(SpinnerModelException exception) {

        }

    }

    

    /**

     * Test getNumberSpinner method.

     */

    public void testGetNumberSpinner() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        NumberSpinnerOperator operator3 = operator2.getNumberSpinner();

        assertNotNull(operator3);

        

        assertNotNull(operator3.getNumberModel());

        

        operator3.scrollToValue(new Integer(2));

        operator3.scrollToValue(4.0);

    }

    

    /**

     * Test getListSpinner method.

     */

    public void testGetListSpinner() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        operator2.setModel(new SpinnerListModel());

        

        ListSpinnerOperator operator3 = operator2.getListSpinner();

        assertNotNull(operator3);

        

        assertNotNull(operator3.getListModel());

        

        ArrayList list = new ArrayList();

        list.add("1");

        list.add("2");

        

        operator3.getListModel().setList(list);

        operator3.scrollToString("2");

        assertEquals("2", operator3.getValue());

        

        operator3.scrollToIndex(0);

        assertEquals("1", operator3.getValue());

        

        assertEquals(0, operator3.findItem("1"));



        try {

            operator3.scrollToString("-1");

            fail();

        }

        catch(JemmyException exception) {

        }

    }

    

    /**

     * Test getDateSpinner method.

     */

    public void testGetDateSpinner() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        operator2.setModel(new SpinnerDateModel());

        

        DateSpinnerOperator operator3 = operator2.getDateSpinner();

        assertNotNull(operator3);

        

        assertNotNull(operator3.getDateModel());

        operator3.scrollToDate(new Date());

    }

    

    /**

     * Test scrollTo method.

     */

    public void testScrollTo() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        operator2.scrollTo(new ScrollAdjusterTest());

    }

    

    /**

     * Inner class needed for testing.

     */

    public class ScrollAdjusterTest implements ScrollAdjuster {

        public int getScrollDirection() {

            return 0;

        }

        

        public int getScrollOrientation() {

            return -1;

        }

        

        public String getDescription() {

            return "";

        }

    }

    

    /**

     * Test scrollToMaximum method.

     */

    public void testScrollToMaximum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        try {

            SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 100, 1);

            operator2.setModel(model);

            operator2.scrollToMaximum();

        } catch(SpinnerModelException exception) {

        }

    }

    

    /**

     * Test scrollToMinimum method.

     */

    public void testScrollToMinimum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        try {

            SpinnerNumberModel model = new SpinnerNumberModel(100, 1, 100, 1);

            operator2.setModel(model);

            operator2.scrollToMinimum();

        } catch(SpinnerModelException exception) {

        }

    }

    

    /**

     * Test scrollToObject method.

     */

    public void testScrollToObject() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        try {

            NumberSpinnerOperator operator3 = operator2.getNumberSpinner();

            operator3.scrollToObject(new Integer(11), ScrollAdjuster.INCREASE_SCROLL_DIRECTION);

        } catch(SpinnerModelException exception) {

        }

    }

    

    /**

     * Test scrollToString method.

     */

    public void testScrollToString() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        try {

            NumberSpinnerOperator operator3 = operator2.getNumberSpinner();

            operator3.scrollToString("11", ScrollAdjuster.INCREASE_SCROLL_DIRECTION);

        } catch(SpinnerModelException exception) {

        }

    }

    

    /**

     * Test getIncreaseOperator method.

     */

    public void testGetIncreaseOperator() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        JButtonOperator operator3 = operator2.getIncreaseOperator();

        assertNotNull(operator3);

    }

    

    /**

     * Test getDecreaseOperator method.

     */

    public void testGetDecreaseOperator() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        JButtonOperator operator3 = operator2.getDecreaseOperator();

        assertNotNull(operator3);

    }

    

    /**

     * Test getMinimum method.

     */

    public void testGetMinimum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        assertNull(operator2.getMinimum());

        

        operator2.setModel(new SpinnerDateModel());

        assertNull(operator2.getMinimum());

        

        operator2.setModel(new SpinnerListModel());

        assertNotNull(operator2.getMinimum());

        

        operator2.setModel(new SpinnerModelTest());

        assertNull(operator2.getMinimum());

    }

    

    /**

     * Test getMaximum method.

     */

    public void testGetMaximum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        assertNull(operator2.getMaximum());

        

        operator2.setModel(new SpinnerDateModel());

        assertNull(operator2.getMaximum());

        

        operator2.setModel(new SpinnerListModel());

        assertNotNull(operator2.getMaximum());

        

        operator2.setModel(new SpinnerModelTest());

        assertNull(operator2.getMaximum());

    }

    

    /**

     * Inner class.

     */

    public class SpinnerModelTest implements SpinnerModel {

        public Object getValue() {

            return null;

        }

        

        public void setValue(Object value) {

        }

        

        public Object getNextValue() {

            return null;

        }

        

        public Object getPreviousValue() {

            return null;

        }

        

        public void addChangeListener(ChangeListener l) {

        }

        

        public void removeChangeListener(ChangeListener l) {

        }

    }

    

    /**

     * Test getDump method.

     */

    public void testGetDump() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        assertNotNull(operator2.getDump());

    }

    

    /**

     * Test getValue method.

     */

    public void testGetValue() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        operator2.setValue(new Integer(1));

        assertEquals("1", operator2.getValue().toString());

    }

    

    /**

     * Test getUI method.

     */

    public void testGetUI() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        SpinnerUITest spinnerUI = new SpinnerUITest();

        operator2.setUI(spinnerUI);

        assertEquals(spinnerUI, operator2.getUI());

    }

    

    /**

     * Inner class used for testing.

     */

    public class SpinnerUITest extends SpinnerUI {

    }

    

    /**

     * Test getNextValue method.

     */

    public void testGetNextValue() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        assertNotNull(operator2.getNextValue());

    }

    

    /**

     * Test addChangeListener method.

     */

    public void testAddChangeListener() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        ChangeListenerTest listener = new ChangeListenerTest();

        operator2.addChangeListener(listener);

        assertEquals(2, operator2.getChangeListeners().length);

        

        operator2.removeChangeListener(listener);

        assertEquals(1, operator2.getChangeListeners().length);

    }

    

    /**

     * Inner class we use for testing.

     */

    public class ChangeListenerTest implements ChangeListener {

        public void stateChanged(ChangeEvent e) {

        }

    }

    

    /**

     * Test getPreviousValue method.

     */

    public void testGetPreviousValue() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        assertNotNull(operator2.getPreviousValue());

    }

    

    /**

     * Test setEditor method.

     */

    public void testSetEditor() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        JTextField textField = new JTextField();

        operator2.setEditor(textField);

        assertEquals(textField, operator2.getEditor());

    }

    

    /**

     * Test commitEdit method.

     */

    public void testCommitEdit() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        operator2.commitEdit();

    }

    

    /**

     * Test DateScrollAdjuster.

     */

    public void testDateScrollAdjuster() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        operator2.setModel(new SpinnerDateModel());

        DateScrollAdjuster adjuster = new DateScrollAdjuster(operator2, new Date(System.currentTimeMillis() + 1000));

        assertNotNull(adjuster);



        assertEquals(SwingConstants.VERTICAL, adjuster.getScrollOrientation());

        assertNotNull(adjuster.getDescription());



        assertEquals(ScrollAdjuster.INCREASE_SCROLL_DIRECTION, adjuster.getScrollDirection());



        operator2.setModel(new SpinnerDateModel(new Date(), 

                new Date(System.currentTimeMillis() - 3600), 

                new Date(System.currentTimeMillis() + 3600), Calendar.MINUTE));

        

        adjuster = new DateScrollAdjuster(operator2, new Date(System.currentTimeMillis() - 2400));

        

        assertEquals(ScrollAdjuster.DECREASE_SCROLL_DIRECTION, adjuster.getScrollDirection());

        

        Date date = new Date();

        operator2.setModel(new SpinnerDateModel(date, 

                new Date(System.currentTimeMillis() - 3600), 

                new Date(System.currentTimeMillis() + 3600), Calendar.MINUTE));



        adjuster = new DateScrollAdjuster(operator2, date);

        

        assertEquals(ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION, adjuster.getScrollDirection());

    }



    /**

     * Test ListScrollAdjuster.

     */

    public void testListScrollAdjuster() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);

        

        operator2.setModel(new SpinnerListModel());

        ListScrollAdjuster adjuster = new ListScrollAdjuster(operator2, "1");

        assertNotNull(adjuster);



        assertEquals(SwingConstants.VERTICAL, adjuster.getScrollOrientation());

        assertNotNull(adjuster.getDescription());

        

        assertEquals(ScrollAdjuster.DECREASE_SCROLL_DIRECTION, adjuster.getScrollDirection());



        adjuster = new ListScrollAdjuster(operator2, 0);

        assertNotNull(adjuster);



        assertEquals(ScrollAdjuster.DO_NOT_TOUCH_SCROLL_DIRECTION, adjuster.getScrollDirection());



        adjuster = new ListScrollAdjuster(operator2, 1);

        assertNotNull(adjuster);



        assertEquals(ScrollAdjuster.INCREASE_SCROLL_DIRECTION, adjuster.getScrollDirection());

    }

    

    /**

     * Test ToStringScrollAdjuster.

     */

    public void testToStringScrollAdjuster() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);



        operator2.setModel(new SpinnerListModel());

        ToStringScrollAdjuster adjuster = new ToStringScrollAdjuster(operator2, "1", ScrollAdjuster.INCREASE_SCROLL_DIRECTION);

        assertNotNull(adjuster);

        

        assertNotNull(adjuster.getDescription());

        assertEquals(SwingConstants.VERTICAL, adjuster.getScrollOrientation());

    }



    /**

     * Test ExactScrollAdjuster.

     */

    public void testExactScrollAdjuster() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);



        operator2.setModel(new SpinnerListModel());

        ExactScrollAdjuster adjuster = new ExactScrollAdjuster(operator2, "1", ScrollAdjuster.INCREASE_SCROLL_DIRECTION);

        assertNotNull(adjuster);

        

        assertNotNull(adjuster.getDescription());

        assertEquals(SwingConstants.VERTICAL, adjuster.getScrollOrientation());

    }



    /**

     * Test NumberScrollAdjuster.

     */

    public void testNumberScrollAdjuster() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSpinnerOperator operator2 = new JSpinnerOperator(operator);

        assertNotNull(operator2);



        operator2.setModel(new SpinnerNumberModel());

        NumberScrollAdjuster adjuster = new NumberScrollAdjuster(operator2, new Integer(-1));

        assertNotNull(adjuster);

        

        assertNotNull(adjuster.getDescription());

        assertEquals(SwingConstants.VERTICAL, adjuster.getScrollOrientation());

        assertEquals(ScrollAdjuster.DECREASE_SCROLL_DIRECTION, adjuster.getScrollDirection());

    }

}

