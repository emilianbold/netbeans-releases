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

import java.awt.Choice;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for ChoiceOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class ChoiceOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the choice.
     */
    private Choice choice;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public ChoiceOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a major problem occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        choice = new Choice();
        choice.add("ChoiceOperatorTest");
        choice.setName("ChoiceOperatorTest");
        frame.add(choice);
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
        TestSuite suite = new TestSuite(ChoiceOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        ChoiceOperator operator2 = new ChoiceOperator(operator, new NameComponentChooser("ChoiceOperatorTest"));
        assertNotNull(operator2);
        
        ChoiceOperator operator3 = new ChoiceOperator(operator, "ChoiceOperatorTest");
        assertNotNull(operator3);
    }

    /**
     * Test findChoice method.
     */
    public void testFindChoice() {
        frame.setVisible(true);
        
        Choice choice1 = ChoiceOperator.findChoice(frame, "ChoiceOperatorTest", false, false);
        assertNotNull(choice1);
        
        Choice choice2 = ChoiceOperator.findChoice(frame, new NameComponentChooser("ChoiceOperatorTest"));
        assertNotNull(choice2);
    }

    /**
     * Test waitChoice method.
     */
    public void testWaitChoice() {
        frame.setVisible(true);
        
        Choice choice1 = ChoiceOperator.waitChoice(frame, "ChoiceOperatorTest", false, false);
        assertNotNull(choice1);
        
        Choice choice2 = ChoiceOperator.waitChoice(frame, new NameComponentChooser("ChoiceOperatorTest"));
        assertNotNull(choice2);
    }

    /**
     * Test findItemIndex method.
     */
    public void testFindItemIndex() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);
        
        operator1.findItemIndex("ChoiceOperatorTest");
    }

    /**
     * Test selectItem method.
     */
    public void testSelectItem() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        operator1.selectItem("ChoiceOperatorTest");
    }

    /**
     * Test waitItemSelected method.
     */
    public void testWaitItemSelected() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        operator1.selectItem(0);
        operator1.waitItemSelected(0);
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        operator1.getDump();
    }

    /**
     * Test add method.
     */
    public void testAdd() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        operator1.add("ChoiceOperatorTest2");
    }

    /**
     * Test addItemListener method.
     */
    public void testAddItemListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
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
     * Test addNotify method.
     */
    public void testAddNotify() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        operator1.addNotify();
    }

    /**
     * Test getItem method.
     */
    public void testGetItem() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        operator1.getItem(0);
    }

    /**
     * Test getItemCount method.
     */
    public void testGetItemCount() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        operator1.getItemCount();
    }

    /**
     * Test getSelectedIndex method.
     */
    public void testGetSelectedIndex() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        operator1.getSelectedIndex();
    }

    /**
     * Test getSelectedItem method.
     */
    public void testGetSelectedItem() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        operator1.getSelectedItem();
    }

    /**
     * Test insert method.
     */
    public void testInsert() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        operator1.insert("ChoiceOperatorTest2", 1);
    }

    /**
     * Test remove method.
     */
    public void testRemove() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        operator1.remove("ChoiceOperatorTest");
        operator1.add("ChoiceOperatorTest");
        operator1.remove(0);
    }

    /**
     * Test removeAll method.
     */
    public void testRemoveAll() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        operator1.removeAll();
    }

    /**
     * Test select method.
     */
    public void testSelect() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        operator1.select(0);
    }

    /**
     * Test setState method.
     */
    public void testSetState() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ChoiceOperator operator1 = new ChoiceOperator(operator);
        assertNotNull(operator1);

        operator1.setState("ChoiceOperatorTest");
    }
}
