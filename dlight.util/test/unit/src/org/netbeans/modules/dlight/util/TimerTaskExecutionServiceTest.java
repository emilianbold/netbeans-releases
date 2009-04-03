/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
        System.out.println("registerTimerTask");//NOI18N
        int count = 1200;
        final List<Worker> workers = new ArrayList<Worker>();
        final List<Future> tasks = Collections.synchronizedList(new ArrayList<Future>());
        
        for (int i = 0; i < count; i++) {
            workers.add(new Worker(i));
        }

        final Random r = new Random();

        Runnable runnable = new Runnable() {

            public void run() {
                for (Worker worker : workers) {
                    tasks.add(DLightExecutorService.scheduleAtFixedRate(worker, r.nextInt(4) + 1, TimeUnit.SECONDS, "testRegisterTimerTask"));//NOI18N
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
        
        for (Future task : tasks) {
            task.cancel(true);
        }

    }

    /**
     * Test of unregisterTimerTask method, of class TimerTaskExecutionService.
     */
//    @Test
    public void testUnregisterTimerTask() {
        System.out.println("unregisterTimerTask");//NOI18N
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");//NOI18N
    }

    private static class Worker implements Runnable {
        private final int id;
        
        public Worker(int id) {
            this.id = id;
        }

        public void run() {
            System.out.println("Hello from " + id);//NOI18N
        }

    }

}