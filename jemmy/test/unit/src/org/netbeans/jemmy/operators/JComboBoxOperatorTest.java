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



import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import java.awt.event.ItemEvent;

import java.awt.event.ItemListener;

import java.awt.event.KeyEvent;

import javax.swing.JComboBox;

import javax.swing.JFrame;

import javax.swing.plaf.basic.BasicComboBoxEditor;

import junit.framework.Test;

import junit.framework.TestCase;

import junit.framework.TestSuite;

import org.netbeans.jemmy.operators.Operator.StringComparator;

import org.netbeans.jemmy.util.NameComponentChooser;



/**

 * A JUnit test for JComboBoxOperator.

 *

 * @author Manfred Riem (mriem@netbeans.org)

 * @version $Revision$

 */

public class JComboBoxOperatorTest extends TestCase {

    /**

     * Stores the frame.

     */

    private JFrame frame;

    

    /**

     * Stores the combo box.

     */

    private JComboBox comboBox;

    

    /**

     * Constructor.

     *

     * @param testName the name of the test.

     */

    public JComboBoxOperatorTest(String testName) {

        super(testName);

    }



    /**

     * Setup before testing.

     */

    protected void setUp() throws Exception {

        frame = new JFrame();

        comboBox = new JComboBox();

        comboBox.setName("JComboBoxOperatorTest");

        comboBox.setEditable(true);

        comboBox.addItem("JComboBoxOperatorTest");

        frame.getContentPane().add(comboBox);

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

        TestSuite suite = new TestSuite(JComboBoxOperatorTest.class);

        

        return suite;

    }

    

    /**

     * Test constructor.

     */

    public void testConstructor() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);

        

        JComboBoxOperator operator2 = new JComboBoxOperator(operator, new NameComponentChooser("JComboBoxOperatorTest"));

        assertNotNull(operator2);

        

        JComboBoxOperator operator3 = new JComboBoxOperator(operator, "JComboBoxOperatorTest");

        assertNotNull(operator3);

    }



    /**

     * Test findJComboBox method.

     */

    public void testFindJComboBox() {

        frame.setVisible(true);

        

        JComboBox comboBox1 = JComboBoxOperator.findJComboBox(frame, "JComboBoxOperatorTest", false, false, 0);

        assertNotNull(comboBox1);



        JComboBox comboBox2 = JComboBoxOperator.findJComboBox(frame, new NameComponentChooser("JComboBoxOperatorTest"));

        assertNotNull(comboBox2);

    }



    /**

     * Test waitJComboBox method.

     */

    public void testWaitJComboBox() {

        frame.setVisible(true);

        

        JComboBox comboBox1 = JComboBoxOperator.waitJComboBox(frame, "JComboBoxOperatorTest", false, false, 0);

        assertNotNull(comboBox1);



        JComboBox comboBox2 = JComboBoxOperator.waitJComboBox(frame, new NameComponentChooser("JComboBoxOperatorTest"));

        assertNotNull(comboBox2);

    }



    /**

     * Test findJButton method.

     */

    public void testFindJButton() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);

        

        operator1.findJButton();

    }



    /**

     * Test findJTextField method.

     */

    public void testFindJTextField() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);

        

        operator1.findJTextField();

    }



    /**

     * Test getButton method.

     */

    public void testGetButton() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.getButton();

    }



    /**

     * Test getTextField method.

     */

    public void testGetTextField() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.getTextField();

    }



    /**

     * Test waitList method.

     */

    public void testWaitList() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        // operator1.waitList();

    }



    /**

     * Test pushComboButton method.

     */

    public void testPushComboButton() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.pushComboButton();

    }



    /**

     * Test findItemIndex method.

     */

    public void testFindItemIndex() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.findItemIndex("JComboBoxOperatorTest", new StringComparatorTest());

    }

    

    /**

     * Inner class used for testing.

     */

    public class StringComparatorTest implements StringComparator {

        public boolean equals(String caption, String match) {

            return true;

        }

    }



    /**

     * Test waitItem method.

     */

    public void testWaitItem() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.waitItem(0);

        operator1.waitItem("JComboBoxOperatorTest", new StringComparatorTest());

    }



    /**

     * Test selectItem method.

     */

    public void testSelectItem() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.selectItem(0);

        operator1.selectItem("JComboBoxOperatorTest");

        operator1.selectItem("JComboBoxOperatorTest", false, false);

    }



    /**

     * Test typeText method.

     */

    public void testTypeText() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.typeText("1");

    }



    /**

     * Test clearText method.

     */

    public void testClearText() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.clearText();

    }



    /**

     * Test enterText method.

     */

    public void testEnterText() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.enterText("1");

    }



    /**

     * Test waitItemSelected method.

     */

    public void testWaitItemSelected() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.selectItem(0);

        operator1.waitItemSelected(0);

        operator1.waitItemSelected("JComboBoxOperatorTest");

    }



    /**

     * Test getDump method.

     */

    public void testGetDump() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.getDump();

    }



    /**

     * Test actionPerformed method.

     */

    public void testActionPerformed() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.actionPerformed(null);

    }



    /**

     * Test addActionListener method.

     */

    public void testAddActionListener() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        ActionListenerTest listener = new ActionListenerTest();

        operator1.addActionListener(listener);

        operator1.removeActionListener(listener);

    }

    

    /**

     * Inner class needed for testing.

     */

    public class ActionListenerTest implements ActionListener {

        public void actionPerformed(ActionEvent e) {

        }

    }



    /**

     * Test addItem method.

     */

    public void testAddItem() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.addItem("1234");

    }



    /**

     * Test addItemListener method.

     */

    public void testAddItemListener() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        ItemListenerTest listener = new ItemListenerTest();

        operator1.addItemListener(listener);

        operator1.removeItemListener(listener);

    }

    

    /**

     * Inner class needed for testing.

     */

    public class ItemListenerTest implements ItemListener {

        public void itemStateChanged(ItemEvent e) {

        }

    }



    /**

     * Test configureEditor method.

     */

    public void testConfigureEditor() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.configureEditor(new BasicComboBoxEditor(), "");

    }



    /**

     * Test contentsChanged method.

     */

    public void testContentsChanged() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.contentsChanged(null);

    }



    /**

     * Test getActionCommand method.

     */

    public void testGetActionCommand() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.setActionCommand(operator1.getActionCommand());

    }



    /**

     * Test getEditor method.

     */

    public void testGetEditor() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.setEditor(operator1.getEditor());

    }



    /**

     * Test getItemAt method.

     */

    public void testGetItemAt() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.getItemAt(0);

    }



    /**

     * Test getItemCount method.

     */

    public void testGetItemCount() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.getItemCount();

    }



    /**

     * Test getKeySelectionManager method.

     */

    public void testGetKeySelectionManager() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.setKeySelectionManager(operator1.getKeySelectionManager());

    }



    /**

     * Test getMaximumRowCount method.

     */

    public void testGetMaximumRowCount() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.setMaximumRowCount(operator1.getMaximumRowCount());

    }



    /**

     * Test getModel method.

     */

    public void testGetModel() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.setModel(operator1.getModel());

    }



    /**

     * Test getRenderer method.

     */

    public void testGetRenderer() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.setRenderer(operator1.getRenderer());

    }



    /**

     * Test getSelectedIndex method.

     */

    public void testGetSelectedIndex() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.setSelectedIndex(operator1.getSelectedIndex());

    }



    /**

     * Test getSelectedItem method.

     */

    public void testGetSelectedItem() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.setSelectedItem(operator1.getSelectedItem());

    }



    /**

     * Test getSelectedObjects method.

     */

    public void testGetSelectedObjects() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.getSelectedObjects();

    }



    /**

     * Test getUI method.

     */

    public void testGetUI() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.setUI(operator1.getUI());

    }



    /**

     * Test hidePopup method.

     */

    public void testHidePopup() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.hidePopup();

    }



    /**

     * Test insertItemAt method.

     */

    public void testInsertItemAt() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.insertItemAt("1", 0);

    }



    /**

     * Test intervalAdded method.

     */

    public void testIntervalAdded() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.intervalAdded(null);

    }



    /**

     * Test intervalRemoved method.

     */

    public void testIntervalRemoved() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.intervalRemoved(null);

    }



    /**

     * Test isEditable method.

     */

    public void testIsEditable() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.setEditable(operator1.isEditable());

    }



    /**

     * Test isLightWeightPopupEnabled method.

     */

    public void testIsLightWeightPopupEnabled() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.setLightWeightPopupEnabled(operator1.isLightWeightPopupEnabled());

    }



    /**

     * Test isPopupVisible method.

     */

    public void testIsPopupVisible() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.setPopupVisible(operator1.isPopupVisible());

    }



    /**

     * Test processKeyEvent method.

     */

    public void testProcessKeyEvent() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.processKeyEvent(new KeyEvent(comboBox, 0, 0, 0, 0, 'a'));

    }



    /**

     * Test removeAllItems method.

     */

    public void testRemoveAllItems() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.removeAllItems();

    }



    /**

     * Test removeItem method.

     */

    public void testRemoveItem() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.removeItem("1");

    }



    /**

     * Test removeItemAt method.

     */

    public void testRemoveItemAt() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.removeItemAt(0);

    }



    /**

     * Test selectWithKeyChar method.

     */

    public void testSelectWithKeyChar() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.selectWithKeyChar('a');

    }



    /**

     * Test showPopup method.

     */

    public void testShowPopup() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JComboBoxOperator operator1 = new JComboBoxOperator(operator);

        assertNotNull(operator1);



        operator1.showPopup();

    }

}

