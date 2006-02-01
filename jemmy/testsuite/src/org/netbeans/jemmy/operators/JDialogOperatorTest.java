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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JDialogOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JDialogOperatorTest extends TestCase {
    /**
     * Stores the parent frame we use while testing.
     */
    private JFrame frame;
    
    /**
     * Stores the dialog we use while testing.
     */
    private JDialog dialog;
    
    /**
     * Constructor.
     *
     * @param testName
     */
    public JDialogOperatorTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup for testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        
        dialog = new JDialog(frame, "JDialogOperatorTest");
        dialog.setName("JDialogOperatorTest");
        dialog.pack();
        dialog.setLocationRelativeTo(null);
    }
    
    /**
     * Cleanup after testing.
     */
    protected void tearDown() throws Exception {
        dialog.setVisible(false);
        dialog.dispose();
        dialog = null;
        
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
        TestSuite suite = new TestSuite(JDialogOperatorTest.class);
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        dialog.setVisible(true);
        
        JDialogOperator operator1 = new JDialogOperator(new NameComponentChooser("JDialogOperatorTest"));
        assertNotNull(operator1);
        
        JFrameOperator operator2 = new JFrameOperator();
        assertNotNull(operator2);
        
        JDialogOperator operator3 = new JDialogOperator(operator2);
        assertNotNull(operator3);
        
        JFrameOperator operator4 = new JFrameOperator();
        assertNotNull(operator4);
        
        JDialogOperator operator5 = new JDialogOperator(operator4, new NameComponentChooser("JDialogOperatorTest"));
        assertNotNull(operator5);
        
        JDialogOperator operator6 = new JDialogOperator(operator4, "JDialogOperatorTest");
        assertNotNull(operator6);
    }
    
    /**
     * Test findJDialog.
     */
    public void testFindJDialog() {
        dialog.setVisible(true);
        
        JDialog dialog1 = JDialogOperator.findJDialog("JDialogOperatorTest", false, false);
        assertNotNull(dialog1);
        
        JDialog dialog2 = JDialogOperator.findJDialog(frame, "JDialogOperatorTest", false, false);
        assertNotNull(dialog2);
        
        JDialog dialog3 = JDialogOperator.findJDialog(new NameComponentChooser("JDialogOperatorTest"));
        assertNotNull(dialog3);
        
        JDialog dialog4 = JDialogOperator.findJDialog(frame, new NameComponentChooser("JDialogOperatorTest"));
        assertNotNull(dialog4);
    }
    
    /**
     * Test waitJDialog.
     *
     * @todo This test will fail if the system is overloaded. This is because
     *       the last 2 tests use threads. How do we solve this elegantly?
     */
    public void testWaitJDialog() {
        dialog.setVisible(true);
        
        JDialog dialog1 = JDialogOperator.waitJDialog("JDialogOperatorTest", false, false);
        assertNotNull(dialog1);
        
        JDialog dialog2 = JDialogOperator.waitJDialog(frame, "JDialogOperatorTest", false, false);
        assertNotNull(dialog2);
        
        JDialog dialog3 = JDialogOperator.waitJDialog(new NameComponentChooser("JDialogOperatorTest"));
        assertNotNull(dialog3);
        
        JDialog dialog4 = JDialogOperator.waitJDialog(frame, new NameComponentChooser("JDialogOperatorTest"));
        assertNotNull(dialog4);
        
        WaitJDialogRunnable1 runnable1 = new WaitJDialogRunnable1();
        Thread thread1 = new Thread(runnable1);
        thread1.start();
        JDialogOperator operator1 = new JDialogOperator();
        assertNotNull(operator1);
        thread1.interrupt();
        assertNull(runnable1.result);
        
        WaitJDialogRunnable2 runnable2 = new WaitJDialogRunnable2();
        Thread thread2 = new Thread(runnable2);
        thread2.start();
        JDialogOperator operator2 = new JDialogOperator();
        assertNotNull(operator2);
        thread2.interrupt();
        assertNull(runnable2.result);
    }
    
    /**
     * Helper class.
     */
    public class WaitJDialogRunnable1 implements Runnable {
        /**
         * Stores the result.
         */
        public JDialog result;
        
        /**
         * Run method.
         */
        public void run() {
            result = JDialogOperator.waitJDialog("YouWontEverFindMe", true, true);
        }
    }
    
    /**
     * Helper class.
     */
    public class WaitJDialogRunnable2 implements Runnable {
        /**
         * Stores the result.
         */
        public JDialog result;
        
        /**
         * Run method.
         */
        public void run() {
            result = JDialogOperator.waitJDialog(frame, new NameComponentChooser("YouWontEverFindMe"));
        }
    }
    
    /**
     * Test getJMenuBar.
     */
    public void testGetJMenuBar() {
        dialog.setVisible(true);
        
        JDialogOperator operator = new JDialogOperator("JDialogOperatorTest");
        operator.setJMenuBar(new JMenuBar());
        
        assertNotNull(dialog.getJMenuBar());
        assertNotNull(operator.getJMenuBar());
    }
    
    /**
     * Test getDefaultCloseOperation.
     */
    public void testGetDefaultCloseOperation() {
        dialog.setVisible(true);
        
        JDialogOperator operator = new JDialogOperator("JDialogOperatorTest");
        operator.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        assertEquals(JDialog.DO_NOTHING_ON_CLOSE, dialog.getDefaultCloseOperation());
        assertEquals(JDialog.DO_NOTHING_ON_CLOSE, operator.getDefaultCloseOperation());
    }
    
    /**
     * Test getContentPane.
     */
    public void testGetContentPane() {
        dialog.setVisible(true);
        
        JDialogOperator operator = new JDialogOperator("JDialogOperatorTest");
        operator.setContentPane(new JScrollPane());
        
        assertNotNull(dialog.getContentPane());
        assertNotNull(operator.getContentPane());
    }
    
    /**
     * Test getGlassPane.
     */
    public void testGetGlassPane() {
        dialog.setVisible(true);
        
        JDialogOperator operator = new JDialogOperator("JDialogOperatorTest");
        operator.setGlassPane(new JScrollPane());
        
        assertNotNull(dialog.getGlassPane());
        assertNotNull(operator.getGlassPane());
    }
    
    /**
     * Test getLayeredPane.
     */
    public void testGetLayeredPane() {
        dialog.setVisible(true);
        
        JDialogOperator operator = new JDialogOperator("JDialogOperatorTest");
        operator.setLayeredPane(new JLayeredPane());
        
        assertNotNull(dialog.getLayeredPane());
        assertNotNull(operator.getLayeredPane());
    }
    
    /**
     * Test getRootPane.
     */
    public void testGetRootPane() {
        dialog.setVisible(true);
        
        JDialogOperator operator = new JDialogOperator("JDialogOperatorTest");
        
        assertNotNull(dialog.getRootPane());
        assertNotNull(operator.getRootPane());
    }
    
    /**
     * Test getAccessibleContext.
     */
    public void testGetAccessibleContext() {
        dialog.setVisible(true);
        
        JDialogOperator operator = new JDialogOperator("JDialogOperatorTest");
        
        assertNotNull(dialog.getAccessibleContext());
        assertNotNull(operator.getAccessibleContext());
    }
    
    /**
     * Test getTopModalDialog.
     */
    public void testGetTopModalDialog() {
        GetTopModalDialogRunnable1 runnable1 = new GetTopModalDialogRunnable1();
        Thread thread = new Thread(runnable1);
        thread.start();
        
        JDialogOperator operator1 = new JDialogOperator();
        assertNotNull(operator1);
        
        JDialog dialog1 = (JDialog) JDialogOperator.getTopModalDialog();
        assertNotNull(dialog1);
        
        GetTopModalDialogRunnable2 runnable2 = new GetTopModalDialogRunnable2(dialog);
        Thread thread2 = new Thread(runnable2);
        thread2.start();
        
        JDialogOperator operator2 = new JDialogOperator("JDialogOperatorTest");
        assertNotNull(operator2);
        
        JDialog dialog2 = (JDialog) JDialogOperator.getTopModalDialog();
        assertNotNull(dialog2);
        
        runnable2.dialog.setVisible(false);
        runnable2.dialog.dispose();
        runnable2.dialog = null;
    }
    
    /**
     * Helper class.
     */
    public class GetTopModalDialogRunnable1 implements Runnable {
        /**
         * Run method.
         */
        public void run() {
            dialog.setModal(true);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }
    }
    
    /**
     * Helper class.
     */
    public class GetTopModalDialogRunnable2 implements Runnable {
        /**
         * Stores the dialog.
         */
        public JDialog dialog;
        
        /**
         * Constructor.
         */
        public GetTopModalDialogRunnable2(JDialog owner) {
            dialog = new JDialog(owner, "JDialogOperatorTest2", true);
        }
        
        /**
         * Run method.
         */
        public void run() {
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }
    }
    
    /**
     * Test testSetLocationRelativeTo.
     */
    public void testSetLocationRelativeTo() {
        dialog.setLocation(0, 0);
        dialog.pack();
        dialog.setVisible(true);
        
        int x = dialog.getX();
        int y = dialog.getY();
        
        JDialogOperator operator = new JDialogOperator("JDialogOperatorTest");
        operator.setLocationRelativeTo(null);
        
        assertTrue(x != dialog.getX());
        assertEquals(dialog.getX(), operator.getX());
        assertTrue(y != dialog.getY());
        assertEquals(dialog.getY(), operator.getY());
    }
}
