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

import java.awt.Frame;
import java.awt.Panel;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.netbeans.jemmy.util.RegExComparator;

/**
 * A JUnit test for Operator.
 *
 * @author Manfred Riem (mriem@manorrock.org)
 * @version $Revision$
 */
public class OperatorTest extends TestCase {
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
    public OperatorTest(String testName) {
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
        panel.setName("OperatorTest");
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when serious error occurs.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(true);
        frame.dispose();
        frame = null;
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(OperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
    }

    /**
     * Test isCaptionEqual method.
     */
    public void testIsCaptionEqual() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        operator1.isCaptionEqual("", "");
        operator1.isCaptionEqual("", "", false, false);
        operator1.isCaptionEqual("", "", new RegExComparator());
    }

    /**
     * Test getParentPath method.
     */
    public void testGetParentPath() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        operator1.getParentPath(new ComponentChooser[] { new NameComponentChooser("1") } );
    }

    /**
     * Test getCharsKeys method.
     */
    public void testGetCharsKeys() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        operator1.getCharsKeys("");
        operator1.getCharsKeys(new char[] { 'a', 'b'});
    }

    /**
     * Test getCharsModifiers method.
     */
    public void testGetCharsModifiers() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        operator1.getCharsModifiers("");
        operator1.getCharsModifiers(new char[] { 'b', 'b'} );
    }

    /**
     * Test printDump method.
     */
    public void testPrintDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        operator1.printDump();
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        operator1.getDump();
    }

    /**
     * Test unlockAndThrow method.
     */
    public void testUnlockAndThrow() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        try {
            operator1.unlockAndThrow(new Exception(""));
            fail();
        }
        catch(Exception exception) {
        }
    }
    
    /**
     * Test runMapping method.
     */
    public void testRunMapping() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
    }
}
