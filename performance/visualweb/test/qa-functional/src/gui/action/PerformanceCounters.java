/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;

import java.util.LinkedList;
import org.netbeans.junit.NbPerformanceTest;
import org.netbeans.performance.test.guitracker.ActionTracker;
import org.netbeans.performance.test.utilities.PerformanceTestCase;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class PerformanceCounters {

    private static int COUNTER_START = -20;
    private static int COUNTER_STOP = - 30;
    private static int testAttempt = 0;
    private static String testName;
    private static ActionTracker tr;    
    private static LinkedList<String> countersList;
    private static PerformanceTestCase testCase;
    
    public static void initPerformanceCounters(PerformanceTestCase test) {
        testCase = test;
        testName = test.getName();
        testCase.log("Test name = "+test.getName());
        tr = ActionTracker.getInstance();
        countersList = new LinkedList<String>();
        testAttempt = 0;
    }
    
    public static void addPerformanceCounter(String counterName) {
        
        countersList.add(counterName);
        //long lastEventTime = tr.getCurrentEvents().getLast().getTimeMillis();
        long ctm = System.currentTimeMillis();
        //testCase.log("add: last event = "+lastEventTime+" system Time = "+ctm);
        tr.add(COUNTER_START, counterName+"[ "+getPass()+" ]");
    }
    
    public static void endPerformanceCounter(String counterName) {
        //long lastEventTime = tr.getCurrentEvents().getLast().getTimeMillis();
        long ctm = System.currentTimeMillis();
        //testCase.log("end: last event = "+lastEventTime+" system Time = "+ctm);        
        tr.add(COUNTER_STOP, counterName+"[ "+getPass()+" ]");
    }
    private static void clearPerformanceCounters() {
        countersList = new LinkedList<String>();
        
    }
    private static void nextPass() {
        testAttempt++;
    }
    private static int getPass() {
        return testAttempt;
    }
    public static void reportPerformanceCounters() {
        for (String counter : countersList) {
           reportPerformanceCounter(counter);           
        }
        nextPass();
    }    

    private static void reportPerformanceCounter(String counterName) {
        long counterResult = measurePerformanceCounter(counterName); 
        //testCase.log("Report for "+counterName+" = "+counterResult);
        testCase.reportPerformance(testName + " at step: "+counterName, counterResult, "ms", NbPerformanceTest.PerformanceData.NO_ORDER, NbPerformanceTest.PerformanceData.NO_THRESHOLD);
        
    }
    private static long measurePerformanceCounter(String name) {
        String attemptName = name+"[ "+getPass()+" ]";
        
        ActionTracker.Tuple start = tr.getCurrentEvents().getFirst();
        ActionTracker.Tuple end = tr.getCurrentEvents().getFirst(); 
        
        for (ActionTracker.Tuple tuple : tr.getCurrentEvents()) {
            int code = tuple.getCode();
            String counter = tuple.getName();
            
            if( code == COUNTER_START && counter.equals(attemptName)) {
                start = tuple;
            } else if(code == COUNTER_STOP && counter.equals(attemptName)) {
                end = tuple;
            }            
        }
        testCase.log("Start event code "+start.getCode()+" name "+start.getName()+" time "+start.getTimeMillis()+" diff "+start.getTimeDifference());
        testCase.log("End   event code "+end.getCode()+" name "+end.getName()+" time "+end.getTimeMillis()+" diff "+end.getTimeDifference());
        
        start.setMeasured(true);
        end.setMeasured(true);
        
        long result = end.getTimeMillis() - start.getTimeMillis();
        if (result < 0 || start.getTimeMillis() == 0) {
            throw new IllegalStateException("Measuring failed, because we start["+start.getTimeMillis()+"] > end["+end.getTimeMillis()+"] or start=0");
        }
        return result;        
    }
}
