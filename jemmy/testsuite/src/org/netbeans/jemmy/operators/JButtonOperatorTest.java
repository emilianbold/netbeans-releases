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

import java.awt.event.MouseEvent;

import java.awt.event.MouseListener;

import java.util.Hashtable;

import javax.swing.JButton;

import javax.swing.JFrame;

import junit.framework.Test;

import junit.framework.TestCase;

import junit.framework.TestSuite;

import org.netbeans.jemmy.util.NameComponentChooser;



/**

 * A JUnit test for JButtonOperator.

 *

 * @author Manfred Riem (mriem@netbeans.org)

 * @version $Revision$

 */

public class JButtonOperatorTest extends TestCase {

    /**

     * Stores the frame we use for testing.

     */

    private JFrame frame;

    

    /**

     * Stores the button we use for testing.

     */

    private JButton button;

    

    /**

     * Constructor.

     *

     * @param testName the name of the test.

     */

    public JButtonOperatorTest(String testName) {

        super(testName);

    }

    

    /**

     * Setup for testing.

     */

    protected void setUp() throws Exception {

        frame = new JFrame();

        button = new JButton("JButtonOperatorTest");

        button.setName("JButtonOperatorTest");

        frame.getContentPane().add(button);

        frame.pack();

        frame.setLocationRelativeTo(null);

    }

    

    /**

     * Cleanup for testing.

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

        TestSuite suite = new TestSuite(JButtonOperatorTest.class);

        

        return suite;

    }

    

    /**

     * Test constructor method.

     */

    public void testConstructor() {

        frame.setVisible(true);

        

        JFrameOperator operator1 = new JFrameOperator();

        JButtonOperator operator2 = new JButtonOperator(operator1);

        assertNotNull(operator2);

        

        JButtonOperator operator3 = new JButtonOperator(operator1, new NameComponentChooser("JButtonOperatorTest"));

        assertNotNull(operator3);



        JButtonOperator operator4 = new JButtonOperator(operator1, "JButtonOperatorTest");

        assertNotNull(operator4);

    }

    

    /**

     * Test findJButton method.

     */

    public void testFindJButton() {

        frame.setVisible(true);

        

        JButton button1 = JButtonOperator.findJButton(frame, new NameComponentChooser("JButtonOperatorTest"));

        assertNotNull(button1);

        

        JButton button2 = JButtonOperator.findJButton(frame, "JButtonOperatorTest", false, false);

        assertNotNull(button2);

    }

    

    /**

     * Test waitJButton method.

     */

    public void testWaitJButton() {

        frame.setVisible(true);

        

        JButton button1 = JButtonOperator.waitJButton(frame, new NameComponentChooser("JButtonOperatorTest"));

        assertNotNull(button1);

        

        JButton button2 = JButtonOperator.waitJButton(frame, "JButtonOperatorTest", false, false);

        assertNotNull(button2);

    }

    

    /**

     * Test getDump method.

     */

    public void testGetDump() {

        frame.setVisible(true);

        

        JFrameOperator operator1 = new JFrameOperator();

        assertNotNull(operator1);

        

        JButtonOperator operator2 = new JButtonOperator(operator1);

        assertNotNull(operator2);

        

        Hashtable hashtable = operator2.getDump();

        assertEquals("false", hashtable.get(JButtonOperator.IS_DEFAULT_DPROP));

        

        frame.getRootPane().setDefaultButton(button);

        hashtable = operator2.getDump();

        assertEquals("true", hashtable.get(JButtonOperator.IS_DEFAULT_DPROP));

        assertTrue(operator2.isDefaultButton());

    }

    

    /**

     * Test isDefaultCapable method.

     */

    public void testIsDefaultCapable() {

        frame.setVisible(true);

        

        JFrameOperator operator1 = new JFrameOperator();

        assertNotNull(operator1);

        

        JButtonOperator operator2 = new JButtonOperator(operator1);

        assertNotNull(operator2);

        

        operator2.setDefaultCapable(true);

        assertEquals(true, button.isDefaultCapable());

        assertEquals(operator2.isDefaultCapable(), button.isDefaultCapable());

        

        operator2.setDefaultCapable(false);

        assertEquals(false, button.isDefaultCapable());

        assertEquals(operator2.isDefaultCapable(), button.isDefaultCapable());

    }

    

    /**

     * Test prepareToClick method.

     *

     * @todo This will fail if I hide the button first. Should it make the

     *       button visible if it is not visible?

     */

    public void testPrepareToClick() {

        frame.setVisible(true);

        

        JFrameOperator operator1 = new JFrameOperator();

        assertNotNull(operator1);

        

        JButtonOperator operator2 = new JButtonOperator(operator1);

        assertNotNull(operator2);

        

        operator2.prepareToClick();

        

        JButtonOperator operator3 = new JButtonOperator(operator1);

        assertNotNull(operator3);

        assertTrue(operator3.isVisible());

    }

    /**
     * Test for issue #72187.
     */
    public void testIssue72187() {
        frame.setVisible(true);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        button.setVisible(false);
                    }
                });
            }
        });

        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);

        JButtonOperator operator2 = new JButtonOperator(operator1);
        assertNotNull(operator2);

        operator2.press();
        operator2.release();

        assertTrue(!operator2.isVisible());
        assertTrue(!button.isVisible());

        button.setVisible(true);

        JButtonOperator operator3 = new JButtonOperator(operator1);
        assertNotNull(operator3);

        operator3.doClick();

        assertTrue(!button.isVisible());

        button.setVisible(true);

        JButtonOperator operator4 = new JButtonOperator(operator1);

        assertNotNull(operator4);

        operator4.push();

        assertTrue(!operator4.isVisible());

        assertTrue(!button.isVisible());
    }
}

