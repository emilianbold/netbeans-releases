/*
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

package org.netbeans.performance.visualweb.actions;

import java.util.LinkedList;
import org.netbeans.junit.NbPerformanceTest;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

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
