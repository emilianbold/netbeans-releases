/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.performance.test.utilities;

import org.netbeans.performance.test.utilities.BlacklistedClassLogger;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mrkam@netbeans.org
 */
public class BlacklistedClassLoggerTest {

    public BlacklistedClassLoggerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        BlacklistedClassLogger.resetViolations();
//        System.setProperty(
//                "org.netbeans.BlacklistedClassLogger.blacklist.filename", 
//                "C:\\Work\\Current\\Performance\\LoadClasses_01\\blacklist.txt");
    }

    @After
    public void tearDown() {
        if (BlacklistedClassLogger.noViolations()) {
            System.out.println("No violations");
        } else {
            System.out.println("There are violations:");
            BlacklistedClassLogger.listViolations(System.out);
        }
    }
    
    /**
     * Test of publish method, of class BlacklistedClassLogger.
     */
    @Test
    public void publish() {
        System.out.println("publish");
        BlacklistedClassLogger instance = new BlacklistedClassLogger();
        publish(instance);
        
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of flush method, of class BlacklistedClassLogger.
     */
    @Test
    public void flush() {
        System.out.println("flush");
        BlacklistedClassLogger instance = new BlacklistedClassLogger();
        instance.flush();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of close method, of class BlacklistedClassLogger.
     */
    @Test
    public void close() {
        System.out.println("close");
        BlacklistedClassLogger instance = new BlacklistedClassLogger();
        instance.close();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of noVialations method, of class BlacklistedClassLogger.
     */
    @Test
    public void noViolations_1() {
        System.out.println("noViolations_1");
        BlacklistedClassLogger instance = new BlacklistedClassLogger();
        boolean expResult = false;

        publish(instance);

        boolean result = instance.noViolations();
        assertEquals(expResult, result);
    }

    /**
     * Test of noVialations method, of class BlacklistedClassLogger.
     */
    @Test
    public void noViolations_2() {
        System.out.println("noViolations_2");
        BlacklistedClassLogger instance = new BlacklistedClassLogger();
        boolean expResult = true;
        boolean result = instance.noViolations();

//            publish(instance);

        assertEquals(expResult, result);
    }

    private void publish(BlacklistedClassLogger instance) {
        LogRecord record = null;

        record = new LogRecord(Level.ALL, "{0} initiated loading of {1}");
        record.setParameters(new String[]{"org.netbeans.BlacklistedClassLoggerTest", "org.netbeans.ProxyClassLoader"});
        instance.publish(record);

        record = new LogRecord(Level.ALL, "{0} initiated loading of {1}");
        record.setParameters(new String[]{"org.netbeans.BlacklistedClassLoggerTest", "org.netbeans.ProxyClassLoader"});
        instance.publish(record);

//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

}