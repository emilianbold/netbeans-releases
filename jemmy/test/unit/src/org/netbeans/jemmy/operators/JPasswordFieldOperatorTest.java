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

import javax.swing.JPasswordField;

import junit.framework.Test;

import junit.framework.TestCase;

import junit.framework.TestSuite;

import org.netbeans.jemmy.util.NameComponentChooser;



/**

 * A JUnit test for JPasswordFieldOperator.

 *

 * @author Manfred Riem (mriem@netbeans.org)

 * @version $Revision$

 */

public class JPasswordFieldOperatorTest extends TestCase {

    /**

     * Stores the frame we use for testing.

     */

    private JFrame frame;

    

    /**

     * Stores the password field we use for testing.

     */

    private JPasswordField passwordField;

    

    /**

     * Constructor.

     *

     * @param testName the name of the test.

     */

    public JPasswordFieldOperatorTest(String testName) {

        super(testName);

    }



    /**

     * Setup before testing.

     */

    protected void setUp() throws Exception {

        frame = new JFrame();

        passwordField = new JPasswordField("JPasswordFieldOperatorTest");

        passwordField.setName("JPasswordFieldOperatorTest");

        frame.getContentPane().add(passwordField);

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

        TestSuite suite = new TestSuite(JPasswordFieldOperatorTest.class);

        

        return suite;

    }



    /**

     * Test constructor.

     */

    public void testConstructor() {

        frame.setVisible(true);

        

        JFrameOperator operator1 = new JFrameOperator();

        assertNotNull(operator1);

        

        JPasswordFieldOperator operator3 = new JPasswordFieldOperator(operator1, new NameComponentChooser("JPasswordFieldOperatorTest"));

        assertNotNull(operator3);



        JPasswordFieldOperator operator4 = new JPasswordFieldOperator(operator1);

        assertNotNull(operator4);



        JPasswordFieldOperator operator5 = new JPasswordFieldOperator(operator1, "JPasswordFieldOperatorTest");

        assertNotNull(operator5);

    }

    

    /**

     * Test findJPasswordField method.

     */

    public void testFindJPasswordField() {

        frame.setVisible(true);

        

        JFrameOperator operator1 = new JFrameOperator();

        assertNotNull(operator1);

        

        JPasswordField passwordField1 = JPasswordFieldOperator.findJPasswordField(frame, new NameComponentChooser("JPasswordFieldOperatorTest"));

        assertNotNull(passwordField1);



        JPasswordField passwordField2 = JPasswordFieldOperator.findJPasswordField(frame, "JPasswordFieldOperatorTest", false, false);

        assertNotNull(passwordField2);

    }



    /**

     * Test waitJPasswordField method.

     */

    public void testWaitJPasswordField() {

        frame.setVisible(true);

        

        JFrameOperator operator1 = new JFrameOperator();

        assertNotNull(operator1);

        

        JPasswordField passwordField1 = JPasswordFieldOperator.waitJPasswordField(frame, new NameComponentChooser("JPasswordFieldOperatorTest"));

        assertNotNull(passwordField1);



        JPasswordField passwordField2 = JPasswordFieldOperator.waitJPasswordField(frame, "JPasswordFieldOperatorTest", false, false);

        assertNotNull(passwordField2);

    }



    /**

     * Test getDump method.

     */

    public void testGetDump() {

        frame.setVisible(true);

        

        JFrameOperator operator1 = new JFrameOperator();

        assertNotNull(operator1);

        

        JPasswordFieldOperator operator2 = new JPasswordFieldOperator(operator1, new NameComponentChooser("JPasswordFieldOperatorTest"));

        assertNotNull(operator2);

        

        operator2.setEchoChar('%');

        assertEquals("%", operator2.getDump().get(JPasswordFieldOperator.ECHO_CHAR_DPROP));

    }



    /**

     * Test echoCharIsSet method.

     */

    public void testEchoCharIsSet() {

        frame.setVisible(true);

        

        JFrameOperator operator1 = new JFrameOperator();

        assertNotNull(operator1);

        

        JPasswordFieldOperator operator3 = new JPasswordFieldOperator(operator1, new NameComponentChooser("JPasswordFieldOperatorTest"));

        assertNotNull(operator3);

        

        assertTrue(operator3.echoCharIsSet());

        assertTrue(passwordField.echoCharIsSet());

        

        operator3.setEchoChar('a');

        assertEquals('a', operator3.getEchoChar());

        assertEquals(operator3.getEchoChar(), passwordField.getEchoChar());

    }



    /**

     * Test getPassword method.

     */

    public void testGetPassword() {

        frame.setVisible(true);

        

        JFrameOperator operator1 = new JFrameOperator();

        assertNotNull(operator1);

        

        JPasswordFieldOperator operator3 = new JPasswordFieldOperator(operator1, new NameComponentChooser("JPasswordFieldOperatorTest"));

        assertNotNull(operator3);

        

        passwordField.setText("hallo");

        assertEquals(operator3.getPassword()[0], 'h');

        assertEquals(operator3.getPassword()[1], 'a');

        assertEquals(operator3.getPassword()[2], 'l');

        assertEquals(operator3.getPassword()[3], 'l');

        assertEquals(operator3.getPassword()[4], 'o');

    }

}

