/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]".
 *
 * The Original Software is the Jemmy library. The Initial Developer of the
 * Original Software is Alexandre Iline. All Rights Reserved.
 *
 * ---------------------------------------------------------------------------
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 */
package org.netbeans.jemmy.operators;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;


/**
 * A JUnit test for JCheckBoxOperatorTest.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JCheckBoxOperatorTest extends TestCase {
    /**
     * Stores the frame we use for testing.
     */
    private JFrame frame;
    
    /**
     * Stores the checkBox we use for testing.
     */
    private JCheckBox checkBox;
    
    /**
     * Constructor.
     * 
     * @param testName the name of the test.
     */
    public JCheckBoxOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup for testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        checkBox = new JCheckBox("JCheckBoxOperatorTest");
        checkBox.setName("JCheckBoxOperatorTest");
        frame.getContentPane().add(checkBox);
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
        TestSuite suite = new TestSuite(JCheckBoxOperatorTest.class);
        
        return suite;
    }

    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JCheckBoxOperator operator2 = new JCheckBoxOperator(operator1);
        assertNotNull(operator2);
        
        JCheckBoxOperator operator3 = new JCheckBoxOperator(operator1, new NameComponentChooser("JCheckBoxOperatorTest"));
        assertNotNull(operator3);

        JCheckBoxOperator operator4 = new JCheckBoxOperator(operator1, "JCheckBoxOperatorTest");
        assertNotNull(operator4);
    }
        
    /**
     * Test findJCheckBox method.
     */
    public void testFindJCheckBox() {
        frame.setVisible(true);

        JCheckBox checkBox1 = JCheckBoxOperator.findJCheckBox(frame, new NameComponentChooser("JCheckBoxOperatorTest"));
        assertNotNull(checkBox1);

        JCheckBox checkBox2 = JCheckBoxOperator.findJCheckBox(frame, "JCheckBoxOperatorTest", false, false);
        assertNotNull(checkBox2);
    }

    /**
     * Test waitJCheckBox method.
     */
    public void testWaitJCheckBox() {
        frame.setVisible(true);

        JCheckBox checkBox1 = JCheckBoxOperator.waitJCheckBox(frame, new NameComponentChooser("JCheckBoxOperatorTest"));
        assertNotNull(checkBox1);

        JCheckBox checkBox2 = JCheckBoxOperator.waitJCheckBox(frame, "JCheckBoxOperatorTest", false, false);
        assertNotNull(checkBox2);
    }
}
