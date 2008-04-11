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

import java.awt.Button;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class ButtonOperatorTest extends TestCase {
    /**
     * Stores the frame we use for testing.
     */
    private Frame frame;
    
    /**
     * Stores the button we use for testing.
     */
    private Button button;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public ButtonOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        button = new Button("ButtonOperatorTest");
        button.setName("ButtonOperatorTest");
        
        frame.add(button);
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
     *
     * @return the test suite.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(ButtonOperatorTest.class);
        return suite;
    }
    
    /**
     * Test constructor method.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);
        
        ButtonOperator operator2 = new ButtonOperator(operator, new NameComponentChooser("ButtonOperatorTest"));
        assertNotNull(operator2);
        
        ButtonOperator operator3 = new ButtonOperator(operator, "ButtonOperatorTest");
        assertNotNull(operator3);
    }
    
    /**
     * Test findButton method.
     */
    public void testFindButton() {
        frame.setVisible(true);
        
        Button button1 = ButtonOperator.findButton(frame, "ButtonOperatorTest", false, false);
        assertNotNull(button1);

        Button button2 = ButtonOperator.findButton(frame, new NameComponentChooser("ButtonOperatorTest"));
        assertNotNull(button2);
    }
    
    /**
     * Test waitButton method.
     */
    public void testWaitButton() {
        frame.setVisible(true);
        
        Button button1 = ButtonOperator.waitButton(frame, "ButtonOperatorTest", false, false);
        assertNotNull(button1);

        Button button2 = ButtonOperator.waitButton(frame, new NameComponentChooser("ButtonOperatorTest"));
        assertNotNull(button2);
    }
    
    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);

        Hashtable hashtable1 = operator1.getDump();
        assertNotNull(hashtable1.get("Label"));
    }
    
    /**
     * Test getActionCommand method.
     */
    public void testGetActionCommand() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setActionCommand("TEST");
        assertEquals("TEST", operator1.getActionCommand());
    }

    /**
     * Test getLabel method.
     */
    public void testGetLabel() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setLabel("TEST");
        assertEquals("TEST", operator1.getLabel());
    }
    
    /**
     * Test addActionListener method.
     */
    public void testAddActionListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);

        operator1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            }
        });
        assertTrue(button.getActionListeners().length == 1);
        
        operator1.removeActionListener(button.getActionListeners()[0]);
        assertTrue(button.getActionListeners().length == 0);
    }
    
    /**
     * Test push method.
     */
    public void testPush() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);
        
        operator1.push();
    }
    
    /**
     * Test pushNoBlock method.
     */
    public void testPushNoBlock() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);

        operator1.pushNoBlock();
        operator1.push();
    }
    
    /**
     * Test press/release method.
     */
    public void testRelease() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);

        operator1.press();
        operator1.release();
    }
}
