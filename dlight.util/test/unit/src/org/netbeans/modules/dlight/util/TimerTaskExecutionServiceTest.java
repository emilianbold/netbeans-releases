/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class TimerTaskExecutionServiceTest {

    public TimerTaskExecutionServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of registerTimerTask method, of class TimerTaskExecutionService.
     */
    @Test
    public void testRegisterTimerTask() {
        System.out.println("registerTimerTask");
        int count = 1200;
        final List<Worker> workers = new ArrayList<Worker>();
        
        for (int i = 0; i < count; i++) {
            workers.add(new Worker(i));
        }

        final TimerTaskExecutionService service = TimerTaskExecutionService.getInstance();
        final Random r = new Random();

        Runnable runnable = new Runnable() {

            public void run() {
                for (Worker worker : workers) {
                    service.registerTimerTask(worker, r.nextInt(4) + 1);
                }
            }
        };

        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);

        t1.start();
        t2.start();
        try {
            t1.join();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        try {
            t2.join();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        for (Worker worker : workers) {
            service.unregisterTimerTask(worker);
        }

    }

    /**
     * Test of unregisterTimerTask method, of class TimerTaskExecutionService.
     */
//    @Test
    public void testUnregisterTimerTask() {
        System.out.println("unregisterTimerTask");
        Callable<Integer> task = null;
        TimerTaskExecutionService instance = null;
        instance.unregisterTimerTask(task);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    private static class Worker implements Callable<Integer> {
        private final int id;
        
        public Worker(int id) {
            this.id = id;
        }

        public Integer call() throws Exception {
            System.out.println("Hello from " + id);
            return id;
        }

    }

}