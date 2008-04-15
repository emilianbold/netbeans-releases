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



import java.util.Hashtable;

import javax.swing.BoundedRangeModel;

import javax.swing.JFrame;

import javax.swing.JSlider;

import javax.swing.SwingConstants;

import javax.swing.event.ChangeEvent;

import javax.swing.event.ChangeListener;

import javax.swing.plaf.SliderUI;

import junit.framework.Test;

import junit.framework.TestCase;

import junit.framework.TestSuite;

import org.netbeans.jemmy.drivers.scrolling.ScrollAdjuster;

import org.netbeans.jemmy.util.NameComponentChooser;



/**

 * A JUnit test for JSliderOperator.

 *

 * @author Manfred Riem (mriem@netbeans.org)

 * @version $Revision$

 */

public class JSliderOperatorTest extends TestCase {

    /**

     * Stores the frame we use.

     */

    private JFrame frame;

    

    /**

     * Stores the slider.

     */

    private JSlider slider;

    

    /**

     * Constructor.

     *

     * @param testName the name of the test.

     */

    public JSliderOperatorTest(String testName) {

        super(testName);

    }

    

    /**

     * Setup for testing.

     */

    protected void setUp() throws Exception {

        frame = new JFrame();

        slider = new JSlider();

        slider.setName("JSliderOperatorTest");

        frame.getContentPane().add(slider);

        frame.pack();

        frame.setLocationRelativeTo(null);

    }

    

    /**

     * Cleanu after testing.

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

        TestSuite suite = new TestSuite(JSliderOperatorTest.class);

        

        return suite;

    }

    

    /**

     * Test constructor.

     */

    public void testConstructor() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        JSliderOperator operator3 = new JSliderOperator(operator, new NameComponentChooser("JSliderOperatorTest"));

        assertNotNull(operator3);

    }

    

    /**

     * Test findJSlider method.

     */

    public void testFindJSlider() {

        frame.setVisible(true);

        

        JSlider slider1 = JSliderOperator.findJSlider(frame);

        assertNotNull(slider1);

        

        JSlider slider2 = JSliderOperator.findJSlider(frame, new NameComponentChooser("JSliderOperatorTest"));

        assertNotNull(slider2);

    }

    

    /**

     * Test waitJSlider method.

     */

    public void testWaitJSlider() {

        frame.setVisible(true);

        

        JSlider slider1 = JSliderOperator.waitJSlider(frame);

        assertNotNull(slider1);

        

        JSlider slider2 = JSliderOperator.waitJSlider(frame, new NameComponentChooser("JSliderOperatorTest"));

        assertNotNull(slider2);

    }

    

    /**

     * Test setScrollModel method.

     */

    public void testSetScrollModel() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setScrollModel(JSliderOperator.CLICK_SCROLL_MODEL);

        assertEquals(JSliderOperator.CLICK_SCROLL_MODEL, operator2.getScrollModel());

    }

    

    /**

     * Test scrollTo method.

     */

    public void testScrollTo() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        ScrollAdjusterTest adjuster = new ScrollAdjusterTest();

        operator2.scrollTo(adjuster);

    }

    

    /**

     * Inner class used for testing.

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

     * Test scrollToValue method.

     */

    public void testScrollToValue() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.scrollToValue(10);

        assertEquals(10, operator2.getValue());

    }

    

    /**

     * Test scrollToMaximum method.

     */

    public void testScrollToMaximum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setMinimum(0);

        operator2.setMaximum(100);

        operator2.setValue(100);

        operator2.scrollToMaximum();

    }

    

    /**

     * Test scrollToMinimum method.

     */

    public void testScrollToMinimum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.scrollToMinimum();

    }

    

    /**

     * Test getDump method.

     */

    public void testGetDump() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        Hashtable hashtable = operator2.getDump();

        assertNotNull(hashtable);

        

        operator2.setOrientation(SwingConstants.HORIZONTAL);

        operator2.setInverted(false);

        hashtable = operator2.getDump();

        assertNotNull(hashtable);

    }

    

    /**

     * Test addChangeListener method.

     */

    public void testAddChangeListener() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        ChangeListenerTest listener = new ChangeListenerTest();

        operator2.addChangeListener(listener);

        assertEquals(1, slider.getChangeListeners().length);

        

        operator2.removeChangeListener(listener);

        assertEquals(0, slider.getChangeListeners().length);

    }

    

    /**

     * Inner class needed for testing.

     */

    public class ChangeListenerTest implements ChangeListener {

        public void stateChanged(ChangeEvent e) {

        }

    }

    

    /**

     * Test createStandardLabels method.

     */

    public void testCreateStandardLabels() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.createStandardLabels(10, 10);

        operator2.createStandardLabels(10);

    }

    

    /**

     * Test getExtent method.

     */

    public void testGetExtent() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setExtent(10);

        assertEquals(10, operator2.getExtent());

    }

    

    /**

     * Test getInverted method.

     */

    public void testGetInverted() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setInverted(true);

        assertTrue(operator2.getInverted());

        

        operator2.setInverted(false);

        assertTrue(!operator2.getInverted());

    }

    

    /**

     * Test getLabelTable method.

     */

    public void testGetLabelTable() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        Hashtable hashtable = new Hashtable();

        operator2.setLabelTable(hashtable);

        assertEquals(hashtable, operator2.getLabelTable());

    }

    

    /**

     * Test getMajorTickSpacing method.

     */

    public void testGetMajorTickSpacing() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setMajorTickSpacing(11);

        assertEquals(11, operator2.getMajorTickSpacing());

        assertEquals(11, slider.getMajorTickSpacing());

    }

    

    /**

     * Test getMaximum method.

     */

    public void testGetMaximum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setMaximum(111);

        assertEquals(111, operator2.getMaximum());

        assertEquals(111, slider.getMaximum());

    }

    

    /**

     * Test getMinimum method.

     */

    public void testGetMinimum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setMinimum(11);

        assertEquals(11, operator2.getMinimum());

        assertEquals(11, slider.getMinimum());

    }

    

    /**

     * Test getMinorTickSpacing method.

     */

    public void testGetMinorTickSpacing() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setMinorTickSpacing(7);

        assertEquals(7, operator2.getMinorTickSpacing());

        assertEquals(7, slider.getMinorTickSpacing());

    }

    

    /**

     * Test getModel method.

     */

    public void testGetModel() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        BoundedRangeModelTest model = new BoundedRangeModelTest();

        operator2.setModel(model);

        assertEquals(model, operator2.getModel());

    }

    

    /**

     * Inner class needed for testing.

     */

    public class BoundedRangeModelTest implements BoundedRangeModel {

        public int getMinimum() {

            return -1;

        }

        

        public void setMinimum(int newMinimum) {

        }

        

        public int getMaximum() {

            return -1;

        }

        

        public void setMaximum(int newMaximum) {

        }

        

        public int getValue() {

            return -1;

        }

        

        public void setValue(int newValue) {

        }

        

        public void setValueIsAdjusting(boolean b) {

        }

        

        public boolean getValueIsAdjusting() {

            return false;

        }

        

        public int getExtent() {

            return -1;

        }

        

        public void setExtent(int newExtent) {

        }

        

        public void setRangeProperties(int value, int extent, int min, int max, boolean adjusting) {

        }

        

        public void addChangeListener(ChangeListener x) {

        }

        

        public void removeChangeListener(ChangeListener x) {

        }

    }

    

    /**

     * Test getOrientation method.

     */

    public void testGetOrientation() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setOrientation(SwingConstants.VERTICAL);

        assertEquals(SwingConstants.VERTICAL, operator2.getOrientation());

    }

    

    /**

     * Test getPaintLabels method.

     */

    public void testGetPaintLabels() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setPaintLabels(true);

        assertTrue(operator2.getPaintLabels());

        

        operator2.setPaintLabels(false);

        assertTrue(!operator2.getPaintLabels());

    }

    

    /**

     * Test getPaintTicks method.

     */

    public void testGetPaintTicks() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setPaintTicks(true);

        assertTrue(operator2.getPaintTicks());

        

        operator2.setPaintTicks(false);

        assertTrue(!operator2.getPaintTicks());

    }

    

    /**

     * Test getPaintTrack method.

     */

    public void testGetPaintTrack() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setPaintTrack(true);

        assertTrue(operator2.getPaintTrack());

        

        operator2.setPaintTrack(false);

        assertTrue(!operator2.getPaintTrack());

    }

    

    /**

     * Test getSnapToTicks method.

     */

    public void testGetSnapToTicks() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setSnapToTicks(true);

        assertTrue(operator2.getSnapToTicks());

        

        operator2.setSnapToTicks(false);

        assertTrue(!operator2.getSnapToTicks());

    }

    

    /**

     * Test getUI method.

     */

    public void testGetUI() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        SliderUITest sliderUI = new SliderUITest();

        operator2.setUI(sliderUI);

        assertEquals(sliderUI, operator2.getUI());

    }

    

    /**

     * Inner class used for testing.

     */

    public class SliderUITest extends SliderUI {

    }

    

    /**

     * Test getValueIsAdjusting method.

     */

    public void testGetValueIsAdjusting() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setValueIsAdjusting(true);

        assertTrue(operator2.getValueIsAdjusting());

        

        operator2.setValueIsAdjusting(false);

        assertTrue(!operator2.getValueIsAdjusting());

    }

    

    /**

     * Test setMaximum method.

     */

    public void testSetMaximum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setMaximum(100);

        assertEquals(100, operator2.getMaximum());

    }

    

    /**

     * Test setMinimum method.

     */

    public void testSetMinimum() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JSliderOperator operator2 = new JSliderOperator(operator);

        assertNotNull(operator2);

        

        operator2.setMinimum(100);

        assertEquals(100, operator2.getMinimum());

    }

}

