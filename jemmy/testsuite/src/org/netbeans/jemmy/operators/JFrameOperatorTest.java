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

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JFrameOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JFrameOperatorTest extends TestCase {
    /**
     * Stores the frame we use for testing.
     */
    private JFrame frame;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JFrameOperatorTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame("JFrameOperatorTest");
        frame.setName("JFrameOperatorTest");
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
        TestSuite suite = new TestSuite(JFrameOperatorTest.class);
        
        return suite;
    }

    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JFrameOperator operator2 = new JFrameOperator("JFrameOperatorTest");
        assertNotNull(operator2);
        
        JFrameOperator operator3 = new JFrameOperator(new NameComponentChooser("JFrameOperatorTest"));
        assertNotNull(operator3);
    }
    
    /**
     * Test findJFrame method.
     */
    public void testFindJFrame() {
        frame.setVisible(true);
        
        JFrame frame1 = JFrameOperator.findJFrame(new NameComponentChooser("JFrameOperatorTest"));
        assertNotNull(frame1);
        
        JFrame frame2 = JFrameOperator.findJFrame("JFrameOperatorTest", false, false);
        assertNotNull(frame2);
    }

    /**
     * Test waitJFrame method.
     *
     * @todo This test will fail if the system is overloaded. This is because
     *       the last test uses a thread. How do we solve this elegantly?
     */
    public void testWaitJFrame() {
        frame.setVisible(true);
        
        JFrame frame1 = JFrameOperator.waitJFrame(new NameComponentChooser("JFrameOperatorTest"));
        assertNotNull(frame1);
        
        JFrame frame2 = JFrameOperator.waitJFrame("JFrameOperatorTest", false, false);
        assertNotNull(frame2);

        WaitJFrameRunnable1 runnable1 = new WaitJFrameRunnable1();
        Thread thread1 = new Thread(runnable1);
        thread1.start();
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        thread1.interrupt();
        assertNull(runnable1.result);
    }

    /**
     * Helper class.
     */
    public class WaitJFrameRunnable1 implements Runnable {
        /**
         * Stores the result.
         */
        public JFrame result;
        
        /**
         * Run method.
         */
        public void run() {
            result = JFrameOperator.waitJFrame("YouWontEverFindMe", true, true);
        }
    }
    
    /**
     * Test getAccessibleContext method.
     */
    public void testGetAccessibleContext() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        assertNotNull(operator.getAccessibleContext());
    }

    /**
     * Test getContentPane method.
     */
    public void testGetContentPane() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPane scrollPane = new JScrollPane();
        operator.setContentPane(scrollPane);
        assertEquals(frame.getContentPane(), scrollPane);
        assertNotNull(operator.getContentPane());
    }

    /**
     * Test getDefaultCloseOperation method.
     */
    public void testGetDefaultCloseOperation() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        operator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        assertEquals(frame.getDefaultCloseOperation(), JFrame.EXIT_ON_CLOSE);
        assertEquals(operator.getDefaultCloseOperation(), JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Test getGlassPane method.
     */
    public void testGetGlassPane() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JScrollPane scrollPane = new JScrollPane();
        operator.setGlassPane(scrollPane);
        assertEquals(frame.getGlassPane(), scrollPane);
        assertNotNull(operator.getGlassPane());
    }

    /**
     * Test getJMenuBar method.
     */
    public void testGetJMenuBar() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuBar menuBar = new JMenuBar();
        operator.setJMenuBar(menuBar);
        assertEquals(frame.getJMenuBar(), menuBar);
        assertEquals(operator.getJMenuBar(), frame.getJMenuBar());
    }

    /**
     * Test getLayeredPane method.
     */
    public void testGetLayeredPane() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JLayeredPane layeredPane = new JLayeredPane();
        operator.setLayeredPane(layeredPane);
        assertEquals(frame.getLayeredPane(), layeredPane);
        assertNotNull(operator.getLayeredPane());
    }

    /**
     * Test getRootPane method.
     */
    public void testGetRootPane() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        assertEquals(operator.getRootPane(), frame.getRootPane());
    }
}
