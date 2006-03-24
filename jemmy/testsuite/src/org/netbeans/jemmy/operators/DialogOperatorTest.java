/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version 1.0
 * (the "License"). You may not use this file except in compliance with the
 * License. A copy of the License is available at http://www.sun.com/.
 *
 * The Original Code is the Jemmy library. The Initial Developer of the
 * Original Code is Alexandre Iline. All Rights Reserved.
 *
 * ---------------------------------------------------------------------------
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 */
package org.netbeans.jemmy.operators;

import java.awt.Dialog;
import java.awt.Frame;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for DialogOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class DialogOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the dialog.
     */
    private Dialog dialog;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public DialogOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a major problem occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        dialog = new Dialog(frame, "DialogOperatorTest");
        dialog.setName("DialogOperatorTest");
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when a major problem occurs.
     */
    protected void tearDown() throws Exception {
        dialog.setVisible(false);
        frame.setVisible(false);
        
        dialog.dispose();
        frame.dispose();
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(DialogOperatorTest.class);
        
        return suite;
    }

    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        dialog.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        DialogOperator operator1 = new DialogOperator(operator);
        assertNotNull(operator1);
        
        DialogOperator operator2 = new DialogOperator();
        assertNotNull(operator2);
        
        DialogOperator operator3 = new DialogOperator(new NameComponentChooser("DialogOperatorTest"));
        assertNotNull(operator3);
        
        DialogOperator operator4 = new DialogOperator("DialogOperatorTest");
        assertNotNull(operator4);
        
        DialogOperator operator5 = new DialogOperator((WindowOperator) operator, new NameComponentChooser("DialogOperatorTest"));
        assertNotNull(operator5);
        
        DialogOperator operator6 = new DialogOperator((WindowOperator) operator, "DialogOperatorTest");
        assertNotNull(operator6);
    }
    
    /**
     * Test waitTitle method.
     */
    public void testWaitTitle() {
        frame.setVisible(true);
        dialog.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        DialogOperator operator1 = new DialogOperator(operator);
        assertNotNull(operator1);

        operator1.setTitle("BOOH");
        operator1.waitTitle("BOOH");
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        dialog.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        DialogOperator operator1 = new DialogOperator(operator);
        assertNotNull(operator1);
        
        operator1.getDump();
    }

    /**
     * Test getTitle method.
     */
    public void testGetTitle() {
        frame.setVisible(true);
        dialog.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        DialogOperator operator1 = new DialogOperator(operator);
        assertNotNull(operator1);

        operator1.getTitle();
    }

    /**
     * Test isModal method.
     */
    public void testIsModal() {
        frame.setVisible(true);
        dialog.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        DialogOperator operator1 = new DialogOperator(operator);
        assertNotNull(operator1);

        operator1.setModal(false);
        assertTrue(!operator1.isModal());
    }

    /**
     * Test isResizable method.
     */
    public void testIsResizable() {
        frame.setVisible(true);
        dialog.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        DialogOperator operator1 = new DialogOperator(operator);
        assertNotNull(operator1);

        operator1.setResizable(true);
        assertTrue(operator1.isResizable());
    }
}
