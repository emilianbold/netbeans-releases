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

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Point;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for ContainerOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class ContainerOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the panel.
     */
    private Panel panel;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public ContainerOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a serious error occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        panel = new Panel();
        panel.setName("ContainerOperatorTest");
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when a serious error occurs.
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
        TestSuite suite = new TestSuite(ContainerOperatorTest.class);
        
        return suite;
    }

    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        ContainerOperator operator = new ContainerOperator(frame);
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        ContainerOperator operator2 = new ContainerOperator(operator, new NameComponentChooser("ContainerOperatorTest"));
        assertNotNull(operator2);
    }
    
    /**
     * Test findContainer method.
     */
    public void testFindContainer() {
        frame.setVisible(true);
        
        Container container = ContainerOperator.findContainer(frame);
        assertNotNull(container);
        
        Container container1 = ContainerOperator.findContainer(frame, new NameComponentChooser("ContainerOperatorTest"));
        assertNotNull(container1);
    }

    /**
     * Test findContainerUnder method.
     */
    public void testFindContainerUnder() {
        frame.setVisible(true);
        
        Container container = ContainerOperator.findContainerUnder(frame);
        assertNull(container);
    }

    /**
     * Test waitContainer method.
     */
    public void testWaitContainer() {
        frame.setVisible(true);
        
        Container container = ContainerOperator.waitContainer(frame);
        assertNotNull(container);
        
        Container container1 = ContainerOperator.waitContainer(frame, new NameComponentChooser("ContainerOperatorTest"));
        assertNotNull(container1);
    }

    /**
     * Test findSubComponent method.
     */
    public void testFindSubComponent() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        Component component = operator.findSubComponent(new NameComponentChooser("ContainerOperatorTest"));
        assertNotNull(component);
    }

    /**
     * Test waitSubComponent method.
     */
    public void testWaitSubComponent() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        Component component = operator.waitSubComponent(new NameComponentChooser("ContainerOperatorTest"));
        assertNotNull(component);
    }

    /**
     * Test createSubOperator method.
     */
    public void testCreateSubOperator() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        Operator operator1 = operator.createSubOperator(new NameComponentChooser("ContainerOperatorTest"));
        assertNotNull(operator1);
    }

    /**
     * Test add method.
     */
    public void testAdd() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.add(new Panel());
        operator.add("South", new Panel());
        operator.add(new Panel(), null);
        operator.add(new Panel(), 0);
        operator.add(new Panel(), null, 0);
    }

    /**
     * Test addContainerListener method.
     */
    public void testAddContainerListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.addContainerListener(null);
    }

    /**
     * Test findComponentAt method.
     */
    public void testFindComponentAt() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.findComponentAt(100, 100);
        operator.findComponentAt(new Point(100, 100));
    }

    /**
     * Test getComponent method.
     */
    public void testGetComponent() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);

        operator.getComponent(0);
    }

    /**
     * Test getComponentCount method.
     */
    public void testGetComponentCount() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.getComponentCount();
    }

    /**
     * Test getComponents method.
     */
    public void testGetComponents() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.getComponents();
    }

    /**
     * Test getInsets method.
     */
    public void testGetInsets() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.getInsets();
    }

    /**
     * Test getLayout method.
     */
    public void testGetLayout() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.getLayout();
    }

    /**
     * Test isAncestorOf method.
     */
    public void testIsAncestorOf() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.isAncestorOf(null);
    }

    /**
     * Test paintComponents method.
     */
    public void testPaintComponents() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.paintComponents(operator.getGraphics());
    }

    /**
     * Test printComponents method.
     */
    public void testPrintComponents() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.printComponents(operator.getGraphics());
    }

    /**
     * Test remove method.
     */
    public void testRemove() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.remove(panel);
        operator.add(new Panel());
        operator.remove(0);
    }

    /**
     * Test removeAll method.
     */
    public void testRemoveAll() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.removeAll();
    }

    /**
     * Test removeContainerListener method.
     */
    public void testRemoveContainerListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.removeContainerListener(null);
    }

    /**
     * Test setLayout method.
     */
    public void testSetLayout() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.setLayout(operator.getLayout());
    }
}
