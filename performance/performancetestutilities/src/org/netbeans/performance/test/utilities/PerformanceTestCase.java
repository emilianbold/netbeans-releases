/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.performance.test.utilities;

import java.awt.Component;

import java.util.HashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.netbeans.jellytools.JellyTestCase;

import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.jemmy.util.PNGEncoder;

import org.netbeans.junit.NbPerformanceTest;

import org.netbeans.performance.test.guitracker.ActionTracker;
import org.netbeans.performance.test.guitracker.ActionTracker.EventList;
import org.netbeans.performance.test.guitracker.LoggingRepaintManager;
import org.netbeans.performance.test.guitracker.LoggingEventQueue;


/**
 * Test case with implemented Performance Tests Validation support stuff.
 * This class provide methods for QA Performance measurement.
 * Implemented methods:
 * <pre>
 * doMeasurement();
 * measureTime();
 * measureMemoryUsage();
 *</pre>
 *
 *
 * Number of repeatedly measured time can be set by system property
 * <b> org.netbeans.performance.repeat </b>. If property isn't set time is measured only once.
 *
 * @author  mmirilovic@netbeans.org, rkubacki@netbeans.org, anebuzelsky@netbeans.org
 */
public abstract class PerformanceTestCase extends JellyTestCase implements NbPerformanceTest{
    
    private static final boolean logMemory = Boolean.getBoolean("org.netbeans.performance.memory.usage.log");
    
    /**
     * Constant defining maximum time delay for "ui-response" of actions that needs to react
     * quickly to keep the user's flow to stay uninterrupted. This is set to 1000ms.
     */
    protected static final long WINDOW_OPEN = 1000;
    
    /**
     * Constant defining maximum time delay for "ui-response" of actions that needs to react
     * instantaneously. This is set to 100ms.
     */
    protected static final long UI_RESPONSE = 100;
    
    /**
     * Expected time in which the measured action should be completed.
     * Usualy should be set to WINDOW_OPEN or UI_RESPONSE.
     * <br><b>default</b> = UI_RESPONSE */
    public long expectedTime = UI_RESPONSE;
    
    /**
     * Maximum number of iterations to wait for last paint on component/container.
     * <br><b>default</b> = 10 iterations */
    public int MAX_ITERATION = 10;
    
    /**
     * Defines delay between checks if the component/container is painted.
     * <br><b>default</b> = 1000 ms */
    public int WAIT_PAINT = 1000;
    
    /** Wait No Event in the Event Queue after call method <code>open()</code>.
     * <br><b>default</b> = 1000 ms */
    public int WAIT_AFTER_OPEN = 1000;
    
    /** Wait No Event in the Event Queue after call method <code>prepare()</code>.
     * <br><b>default</b> = 1000 ms */
    public int WAIT_AFTER_PREPARE = 250;
    
    /** Wait No Event in the Event Queue after call method {@link close}.
     * <br><b>default</b> = 1000 ms */
    public int WAIT_AFTER_CLOSE = 250;
    
    /** Factor for wait_after_open_heuristic timeout, negative HEURISTIC_FACTOR
     * disables heuristic */
    public double HEURISTIC_FACTOR = 1.25;
    
    /** Count of repeats */
    protected static int repeat = Integer.getInteger("org.netbeans.performance.repeat", 1).intValue();
    
    /** Count of repeats for measure memory usage */
    protected static int repeat_memory = Integer.getInteger("org.netbeans.performance.memory.repeat", -1).intValue();
    
    /** Performance data. */
    private static java.util.ArrayList data;
    
    /** Warmup finished flag. */
    private static boolean warmupFinished = false;
    
    /** Measure from last MOUSE event, you can define your own , by default it's MOUSE_RELEASE */
    protected int track_mouse_event = ActionTracker.TRACK_MOUSE_RELEASE;
    
    /** tracker for UI activities */
    private static ActionTracker tr;
    
    private static LoggingRepaintManager rm;
    
    private static LoggingEventQueue leq;
    
    static {
        if(repeat_memory == -1) {
            // XXX load our EQ and repaint manager
            tr = ActionTracker.getInstance();
            rm = new LoggingRepaintManager(tr);
            rm.setEnabled(true);
            leq = new LoggingEventQueue(tr);
            leq.setEnabled(true);
        }
    }
    
    /** Tested component operator. */
    protected ComponentOperator testedComponentOperator;
    
    /** Name of test case should be changed. */
    protected HashMap renamedTestCaseName;
    
    /** Use order just for indentify first and next run, not specific run order */
    public boolean useTwoOrderTypes = true;
    
    /**
     * Creates a new instance of PerformanceTestCase
     * @param testName name of the test
     */
    public PerformanceTestCase(String testName) {
        super(testName);
        renamedTestCaseName = new HashMap();
    }
    
    /**
     * Creates a new instance of PerformanceTestCase
     * @param testName name of the test
     * @param performanceDataName name for measured performance data, measured values are stored to results under this name
     */
    public PerformanceTestCase(String testName, String performanceDataName) {
        this(testName);
        setTestCaseName(testName, performanceDataName);
    }
    
    /**
     * SetUp test cases: redirect log/ref, initialize performance data.
     */
    public void setUp() {
        
        checkScanFinished();
        checkWarmup();
        
        // XXX load our EQ and repaint manager
        /*
        tr = ActionTracker.getInstance();
        rm = new LoggingRepaintManager(tr);
        rm.setEnabled(true);
        leq = new LoggingEventQueue(tr);
        leq.setEnabled(true);
         */
        
        data = new java.util.ArrayList();
        
    }
    
    /**
     * Getter for LoggingRepaintManager.
     * @return LoggingRepaintManager
     */
    protected LoggingRepaintManager repaintManager() {
        return rm;
    }
    
    /**
     * TearDown test cases: call method <code>call()</code> and closing all modal dialogs.
     * @see close
     */
    public void tearDown() {
        // tr = null;
        //close();
        closeAllModal();
    }
    
    /**
     * Switch to measured methods.
     * Now all test can be used for measure UI responsiveness or look for memory leaks.
     */
    public void doMeasurement(){
        if(repeat_memory==-1)
            measureTime();
        else
            measureMemoryUsage();
    }
    
    /**
     * Test that measures time betwen generated AWT event and last paint event that
     * finishes painting of component/container.
     * It uses <code>ROBOT_MODEL_MASK</code> as an event dispatching model when user's
     * activity is simulated.</p>
     * <p>To initialize the test {@link prepare()} method is invoked at the begining
     * and processing is delayed until there is a period of time at least
     * <code>WAIT_AFTER_PREPARE</code>ms long.</p>
     * <p>The {@link open()} method is called then to perform the measured action,
     * tests waits for no event in <code>WAIT_AFTER_OPEN</code>ms and
     * until component/container is fully painted. Meaure time and report measured time.
     * <br>
     * <br>If during measurement exception arise - test fails and no value is reported as Performance Data.
     * <br>If measuredTime as longer than expectedTime test fails.</p>
     * <p>Each test should reset the state in {@link close()} method. Again there is a waiting
     * for quiet period of time after this call.</p>
     */
    public void measureTime() {
        boolean exceptionDuringMeasurement = false;
        
        long wait_after_open_heuristic = WAIT_AFTER_OPEN;
        
        long[] measuredTime = new long[repeat+1];
        
        // issue 56091 and applied workarround on the next line
        // JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.getCurrentDispatchingModel()|JemmyProperties.ROBOT_MODEL_MASK);
        JemmyProperties.setCurrentTimeout("EventDispatcher.RobotAutoDelay", 1);
        log("----------------------- DISPATCHING MODEL = "+JemmyProperties.getCurrentDispatchingModel());
        
        tr.startNewEventList("test beggining"); // XXX add test name
        tr.add(tr.TRACK_APPLICATION_MESSAGE, "expectedTime "+expectedTime+", "+
                "repeat "+repeat+", "+
                "after_prepare "+WAIT_AFTER_PREPARE+", "+
                "after_open "+WAIT_AFTER_OPEN+", "+
                "after_close "+WAIT_AFTER_CLOSE+", "+
                "paint "+WAIT_PAINT+", "+
                "iteration "+MAX_ITERATION);
        
        String performanceDataName = setPerformanceName();
        
        checkScanFinished(); // just to be sure, that during measurement we will not wait for scanning dialog
        
        try {
            initialize();
            
            for(int i=1; i<=repeat && !exceptionDuringMeasurement; i++){
                try {
                    tr.startNewEventList("test iteration no."+i); // XXX add test name
                    tr.connectToAWT(true);
                    prepare();
                    waitNoEvent(WAIT_AFTER_PREPARE);
                    
                    // Uncomment if you want to run with analyzer tool
                    // com.sun.forte.st.collector.CollectorAPI.resume ();
                    
                    // to be sure EventQueue is empty
                    new QueueTool().waitEmpty();
                    
                    logMemoryUsage();
                    
                    tr.add(tr.TRACK_START, "before open");
                    testedComponentOperator = open();
                    
                    // this is to optimize delays
                    long wait_time = (wait_after_open_heuristic>WAIT_AFTER_OPEN)?WAIT_AFTER_OPEN:wait_after_open_heuristic;
                    tr.add(tr.TRACK_APPLICATION_MESSAGE, "wait_after_open_heuristic "+wait_time);
                    waitNoEvent(wait_time);
                    
                    logMemoryUsage();
                    
                    // PENDING need to check this
//                    if(testedComponentOperator != null) {
//                        java.awt.EventQueue.writeOutput("<wait_until_painted time=\""+System.currentTimeMillis()+"\">");
//                        Component comp = testedComponentOperator.getSource();
//                        waitUntilPainted(comp);
//                        java.awt.EventQueue.writeOutput("</wait_until_painted>");
//                    }
                    new QueueTool().waitEmpty();
                    
                    measuredTime[i] = getMeasuredTime();
                    tr.add(tr.TRACK_APPLICATION_MESSAGE, "measured time "+measuredTime[i]);
                    // negative HEURISTIC_FACTOR disables heuristic
                    if (HEURISTIC_FACTOR > 0) {
                        wait_after_open_heuristic = (long) (measuredTime[i] * HEURISTIC_FACTOR);
                    }
                    
                    log("Measured Time ["+performanceDataName+" | "+i+"] = " +measuredTime[i]);
                    
                    // the measured time could be 0 (on Windows averything under 7-8 ms is logged as 0), but it shouldn't be under 0
                    if(measuredTime[i] < 0)
                        throw new Exception("Measured value ["+measuredTime[i]+"] < 0 !!!");
                    
                    reportPerformance(performanceDataName, measuredTime[i], "ms", i, expectedTime);
                    
                    getScreenshotOfMeasuredIDEInTimeOfMeasurement(i);
                    
                }catch(Exception exc){ // catch for prepare(), open()
                    log("------- [ "+i+" ] ---------------- Exception rises while measuring performance :"+exc.getMessage());
                    exc.printStackTrace(getLog());
                    getScreenshot("exception_during_open");
                    exceptionDuringMeasurement = true;
                    // throw new JemmyException("Exception arises during measurement:"+exc.getMessage());
                }finally{ // finally for prepare(), open()
                    try{
                        // Uncomment if you want to run with analyzer tool
                        // com.sun.forte.st.collector.CollectorAPI.pause ();
                        
                        tr.add(tr.TRACK_APPLICATION_MESSAGE, "before close");
                        close();
                        
                        closeAllModal();
                        waitNoEvent(WAIT_AFTER_CLOSE);
                        
                    }catch(Exception e){ // catch for close()
                        log("------- [ "+i+" ] ---------------- Exception rises while closing tested component :"+e.getMessage());
                        e.printStackTrace(getLog());
                        getScreenshot("exception_during_close");
                        exceptionDuringMeasurement = true;
                        //throw new JemmyException("Exception arises while closing tested component :"+e.getMessage());
                    }finally{ // finally for close()
                        tr.add(tr.TRACK_APPLICATION_MESSAGE, "iteration no."+i+" finished");
                        tr.connectToAWT(false);
                    }
                }
            }
            
            tr.startNewEventList("shutdown hooks");
            shutdown();
            closeAllDialogs();
            tr.add(tr.TRACK_APPLICATION_MESSAGE, "shutdown hooks finished");
            repaintManager().setRegionFilter(null);
        }catch (Exception e) { // catch for initialize(), shutdown(), closeAllDialogs()
            log("----------------------- Exception rises while shuting down / initializing:"+e.getMessage());
            e.printStackTrace(getLog());
            getScreenshot("exception_during_init_or_shutdown");
            // throw new JemmyException("Exception rises while shuting down :"+e.getMessage());
            exceptionDuringMeasurement = true;
        }finally{ // finally for initialize(), shutdown(), closeAllDialogs()
            // XXX export results?
        }
        
        dumpLog();
        if(exceptionDuringMeasurement)
            throw new Error("Exception rises during measurement, look at appropriate log file for stack trace(s).");
        
        compare(measuredTime);
        
    }
    
    
    /**
     * Test that measures memory consumption after each invocation of measured aciotn.
     * Tet finds the lowest value of measured memory consumption and compute all deltas against this value.
     * This method contains the same pattern as previously used method for measuring UI responsiveness
     * {@link measureTime()} . Memory consumption is computed as difference between
     * used and allocated memory (heap). Garbage Collection {@link runGC()} is called then to each measurement of action {@link open()}.
     * <br>
     * <br>If during measurement exception arise - test fails and no value is reported as Performance Data.
     * <p>Each test should reset the state in {@link close()} method. Again there is a waiting
     * for quiet period of time after this call.</p>
     */
    public void measureMemoryUsage() {
        
        boolean exceptionDuringMeasurement = false;
        long wait_after_open_heuristic = WAIT_AFTER_OPEN;
        
        long memoryUsageMinimum = 0;
        long[] memoryUsage = new long[repeat_memory+1];
        
        useTwoOrderTypes = false;
        
        // issue 56091 and applied workarround on the next line
        // JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.getCurrentDispatchingModel()|JemmyProperties.ROBOT_MODEL_MASK);
        JemmyProperties.setCurrentTimeout("EventDispatcher.RobotAutoDelay", 1);
        log("----------------------- DISPATCHING MODEL = "+JemmyProperties.getCurrentDispatchingModel());
        
        checkScanFinished(); // just to be sure, that during measurement we will not wait for scanning dialog
        
        runGC(5);
        
        initialize();
        
        for(int i=1; i<=repeat_memory && !exceptionDuringMeasurement; i++){
            try {
                prepare();
                
                waitNoEvent(WAIT_AFTER_PREPARE);
                
                // Uncomment if you want to run with analyzer tool
                // com.sun.forte.st.collector.CollectorAPI.resume ();
                
                // to be sure EventQueue is empty
                new QueueTool().waitEmpty();
                
                testedComponentOperator = open();
                
                long wait_time = (wait_after_open_heuristic>WAIT_AFTER_OPEN)?WAIT_AFTER_OPEN:wait_after_open_heuristic;
                waitNoEvent(wait_time);
                
                new QueueTool().waitEmpty();
                
            }catch(Exception exc){ // catch for prepare(), open()
                exc.printStackTrace(getLog());
                exceptionDuringMeasurement = true;
                getScreenshot("exception_during_open");
                // throw new JemmyException("Exception arises during measurement:"+exc.getMessage());
            }finally{
                try{
                    // Uncomment if you want to run with analyzer tool
                    // com.sun.forte.st.collector.CollectorAPI.pause ();
                    
                    close();
                    
                    closeAllModal();
                    waitNoEvent(WAIT_AFTER_CLOSE);
                    
                }catch(Exception e){
                    e.printStackTrace(getLog());
                    getScreenshot("measure");
                    exceptionDuringMeasurement = true;
                }finally{
                }
            }
            
            runGC(3);
            
            Runtime runtime = Runtime.getRuntime();
            memoryUsage[i] = runtime.totalMemory() - runtime.freeMemory();
            log("Used Memory ["+i+"] = " +memoryUsage[i]);
            
            if(memoryUsageMinimum == 0 || memoryUsageMinimum > memoryUsage[i])
                memoryUsageMinimum = memoryUsage[i];
            
        }
        
        // report deltas against minimum of measured values
        for(int i=1; i<=repeat_memory; i++){
            //String performanceDataName = setPerformanceName(i);
            String performanceDataName = setPerformanceName();
            log("Used Memory ["+performanceDataName+" | "+i+"] = " +memoryUsage[i]);
            
            reportPerformance(performanceDataName, memoryUsage[i] - memoryUsageMinimum, "bytes", i);
        }
        
        try {
            shutdown();
            closeAllDialogs();
        }catch (Exception e) {
            e.printStackTrace(getLog());
            getScreenshot("shutdown");
            exceptionDuringMeasurement = true;
        }finally{
        }
        
        if(exceptionDuringMeasurement)
            throw new Error("Exception rises during measurement, look at appropriate log file for stack trace(s).");
        
    }
    
    /**
     * Test that measures detailed memory usage.
     * (not reporting anything from here, the test has to call reportDetailMemoryUsage() in one of its methods!)
     * <br>
     * <br>If during measurement exception arise - test fails and no value is reported as Performance Data.
     * <p>Each test should reset the state in {@link close()} method.</p>
     */
    public void measureDetailMemoryUsage() {
        
        boolean exceptionDuringMeasurement = false;
        useTwoOrderTypes = false;
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.getCurrentDispatchingModel()|JemmyProperties.ROBOT_MODEL_MASK);
        JemmyProperties.setCurrentTimeout("EventDispatcher.RobotAutoDelay", 1);
        
        initialize();
        
        for (int i=1; i<=repeat_memory && !exceptionDuringMeasurement; i++) {
            try {

                prepare();
                
                testedComponentOperator = open();
                
            }catch(Exception exc){ // catch for prepare(), open()
                exc.printStackTrace(getLog());
                exceptionDuringMeasurement = true;
                getScreenshot("exception_during_open");
            }finally{
                try{
                    
                    close();
                    
                    closeAllModal();

                }catch(Exception e){
                    e.printStackTrace(getLog());
                    getScreenshot("measure");
                    exceptionDuringMeasurement = true;
                }finally{
                }
            }
        }
        
        // not reporting anything from here, the test has to call reportDetailMemoryUsage() in one of its methods!
        //reportDetailMemoryUsage();
        
        try {
            
            shutdown();
            
            closeAllDialogs();
            
        }catch (Exception e) {
            e.printStackTrace(getLog());
            getScreenshot("shutdown");
            exceptionDuringMeasurement = true;
        }finally{
        }
        
        if(exceptionDuringMeasurement)
            throw new Error("Exception rises during measurement, look at appropriate log file for stack trace(s).");
        
    }
    
    public void reportDetailMemoryUsage(int runOrder) {

        log ("----------------------------------------------");
        // what platform are we running on?
        String platformString = (System.getProperty("os.name","")+","+System.getProperty("os.arch","")).replace(' ','_');
        boolean onunix = platformString.equalsIgnoreCase("Linux,i386") || platformString.equalsIgnoreCase("SunOS,sparc");
        boolean onwindows = platformString.equalsIgnoreCase("Windows_NT,x86") || platformString.equalsIgnoreCase("Windows_2000,x86") ||
                platformString.equalsIgnoreCase("Windows_XP,x86") || platformString.equalsIgnoreCase("Windows_95,x86") ||
                platformString.equalsIgnoreCase("Windows_98,x86") || platformString.equalsIgnoreCase("Windows_Me,x86");

        // find IDE process's PID using JPS utility
        String pid = null;
        String jprocs = executeNativeCommand ("jps -m");
        String[] procstrings = jprocs.split("\n");
        for (int i=0; i<procstrings.length; i++) {
            if (procstrings[i].matches(".*test/work/sys/ide.*")) {
                log (procstrings[i]);
                log ("");
                pid = procstrings[i].split(" ")[0];
                break;
            }
        }
        
        // find the information using JSTAT
        if (pid!=null) {
            String commandoutput;
            
            log (commandoutput = executeNativeCommand ("jstat -class " + pid));
            reportPerformance ("Loaded",Long.valueOf(getOutputSubstring(commandoutput,1,0)).longValue(),"classes",runOrder);
            reportPerformance ("Unloaded",Long.valueOf(getOutputSubstring(commandoutput,1,2)).longValue(),"classes",runOrder);
            
            log (commandoutput = executeNativeCommand ("jstat -gc " + pid));
            reportPerformance ("S0U+S1U+EU",Float.valueOf(getOutputSubstring(commandoutput,1,2)).longValue()+Float.valueOf(getOutputSubstring(commandoutput,1,3)).longValue()+Float.valueOf(getOutputSubstring(commandoutput,1,5)).longValue(),"kB",runOrder);
            reportPerformance ("0U",Float.valueOf(getOutputSubstring(commandoutput,1,7)).longValue(),"kB",runOrder);
            reportPerformance ("PU",Float.valueOf(getOutputSubstring(commandoutput,1,9)).longValue(),"kB",runOrder);
            reportPerformance ("YGC",Float.valueOf(getOutputSubstring(commandoutput,1,10)).longValue(),"occurences",runOrder);
            reportPerformance ("YGCT",(long) (Float.valueOf(getOutputSubstring(commandoutput,1,11)).floatValue()*1000),"ms",runOrder);
            reportPerformance ("FGC",Float.valueOf(getOutputSubstring(commandoutput,1,12)).longValue(),"occurences",runOrder);
            reportPerformance ("FGCT",(long) (Float.valueOf(getOutputSubstring(commandoutput,1,13)).floatValue()*1000),"ms",runOrder);
            reportPerformance ("GCT",(long) (Float.valueOf(getOutputSubstring(commandoutput,1,14)).floatValue()*1000),"ms",runOrder);
            
            //log (commandoutput = executeNativeCommand ("bash -c \"footprint -s 1 -p " + pid + " 2>&1\""));
        }
        
        runGC(5);
        
        Runtime runtime = Runtime.getRuntime();
        long totalHeap = runtime.totalMemory();
        long heapUsage = totalHeap - runtime.freeMemory();
        reportPerformance ("Used Heap", heapUsage/1024, "kB", runOrder);
        reportPerformance ("Heap Capacity", totalHeap/1024, "kB", runOrder);
        log ("Heap = " + (long)(heapUsage/1024) + "/" + (long)(totalHeap/1024));
        log ("----------------------------------------------");
    }

    private String getOutputSubstring (String source, int line, int item) {
        String[] linestrings = source.split("\n");
        String[] lineitems = linestrings[line].split(" ");
        for (int i=0, j=0; i<lineitems.length; i++)
            if (lineitems[i].trim().length()>0) {
                if (item==j)
                    return lineitems[i];
                j++;
            }
        return "N/A";
    }
    
    private String executeNativeCommand (String commandLine){
            
        log("Execute command: ["+commandLine+"].");
        
        try {
            Process proc = Runtime.getRuntime().exec(commandLine);
            
            StringBuffer buffer = new StringBuffer();
            BufferedReader dataInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            
            while ((line = dataInput.readLine()) != null) {
                buffer.append(line);
                buffer.append('\n');
            }
            
            proc.waitFor();
            return buffer.toString();
            
        } catch (InterruptedException ie) {
            ie.printStackTrace(getLog());
            log("InterruptedException: "+ie.toString());
        } catch (IOException ioe){
            ioe.printStackTrace(getLog());
            log("None output from command, exception arise "+ioe.toString());
        }
        return null;
    }

    /**
     * Initialize callback that is called once before the repeated sequence of
     * testet operation is perfromed.
     * Default implementation is empty.
     */
    protected void initialize() {
    }
    
    /**
     * Prepare method is called before at the begining of each measurement
     * The system should be ready to perform measured action when work requested by
     * this method is completed.
     * Default implementation is empty.
     */
    public abstract void prepare();
    
    /**
     * This method should be overriden in subclasses to triger the measured action.
     * Only last action before UI changing must be specified here
     * (push button, select menuitem, expand tree, ...).
     * Whole method uses for dispatching ROBOT_MODEL_MASK in testing measurement.
     * Default implementation is empty.
     * @return tested component operator that will be later passed to close method
     */
    public abstract ComponentOperator open();
    
    /**
     * Close opened window, or invoked popup menu.
     * If tested component controled by testedCompponentOperator is Window it will
     * be closed, if it is component ESC key is pressed.
     */
    public void close(){
        if(testedComponentOperator!=null && testedComponentOperator.isShowing()){
            if (testedComponentOperator instanceof WindowOperator)
                ((WindowOperator)testedComponentOperator).close();
            else if(testedComponentOperator instanceof ComponentOperator){
                testedComponentOperator.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
                //testedComponentOperator.pressKey(java.awt.event.KeyEvent.VK_ESCAPE);
                //testedComponentOperator.releaseKey(java.awt.event.KeyEvent.VK_ESCAPE);
            }
        }
    }
    
    /**
     * Shutdown method resets the state of system when all test invocation are done.
     * Default implementation is empty.
     */
    protected void shutdown() {}
    
    /**
     * Method for storing and reporting measured performance value
     * @param name measured value name
     * @param value measured perofrmance value
     * @param unit unit name of measured value
     * @param runOrder order in which the data was measured (1st, 2nd, ...)
     * @param threshold the limit for an action, menu or dialog
     */
    public void reportPerformance(String name, long value, String unit, int runOrder, long threshold) {
        NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
        d.name = name==null? getName() : name;
        d.value = value;
        d.unit = unit;
        d.runOrder = (useTwoOrderTypes && runOrder>1)?2:runOrder;
        d.threshold = threshold;
        data.add(d);
    }
    
    /**
     * Method for storing and reporting measured performance value
     * @param name measured value name
     * @param value measured perofrmance value
     * @param unit unit name of measured value
     * @param runOrder order in which the data was measured (1st, 2nd, ...)
     */
    public void reportPerformance(String name, long value, String unit, int runOrder) {
        NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
        d.name = name==null? getName() : name;
        d.value = value;
        d.unit = unit;
        d.runOrder = (useTwoOrderTypes && runOrder>1)?2:runOrder;
        data.add(d);
    }
    
    /**
     * Turn's off blinking of the caret in the editor.
     * A method generally useful for any UI Responsiveness tests which measure actions
     * in the Java editor. This method should be called from a test's initialize() method.
     * @param kitClass class of the editor for which you want turn off caret blinking
     */
    protected void setEditorCaretFilteringOn(Class kitClass) {
        org.netbeans.modules.editor.options.BaseOptions options = org.netbeans.modules.editor.options.BaseOptions.getOptions(kitClass);
        options.setCaretBlinkRate(0);
    }
    
    /**
     * Turn's off blinking of the caret in the Java editor.
     * A method generally useful for any UI Responsiveness tests which measure actions
     * in the Java editor. This method should be called from a test's initialize() method.
     */
    protected void setJavaEditorCaretFilteringOn() {
        setEditorCaretFilteringOn(org.netbeans.modules.editor.java.JavaKit.class);
    }
    
    /**
     * Turn's off blinking of the caret in the plain text editor.
     * A method generally useful for any UI Responsiveness tests which measure actions
     * in the plain text editor. This method should be called from a test's initialize() method.
     */
    protected void setPlainTextEditorCaretFilteringOn() {
        setEditorCaretFilteringOn(org.netbeans.modules.editor.plain.PlainKit.class);
    }
    
    /**
     * Turn's off blinking of the caret in the XML editor.
     * A method generally useful for any UI Responsiveness tests which measure actions
     * in the XML editor. This method should be called from a test's initialize() method.
     */
    protected void setXMLEditorCaretFilteringOn() {
        setEditorCaretFilteringOn(org.netbeans.modules.xml.text.syntax.XMLKit.class);
    }
    
    /**
     * Turn's off blinking of the caret in the JSP editor.
     * A method generally useful for any UI Responsiveness tests which measure actions
     * in the JSP editor. This method should be called from a test's initialize() method.
     */
    protected void setJSPEditorCaretFilteringOn() {
        setEditorCaretFilteringOn(org.netbeans.modules.web.core.syntax.JSPKit.class);
    }
    
    /**
     * Log used memory size. It can help with evaluation what happend during measurement.
     * If the size of the memory before and after open differs :
     * <li>if increases- there could be memory leak</li>
     * <li>if decreases- there was an garbage collection during measurement - it prolongs the action time</li>
     */
    protected void logMemoryUsage(){
        // log memory usage after each test case
        if (logMemory) {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            tr.add(tr.TRACK_APPLICATION_MESSAGE, "usedMemory size:"+ (totalMemory-freeMemory) +" total:"+totalMemory+" free:"+freeMemory);
        }
    }
    
    /**
     * Run Garbage Collection 3 times * number defined as a parameter for this method
     * @param i number of repeat (GC runs i*3 times)
     */
    public void runGC(int i){
        for(int gc=0; gc < i; gc ++){
            try{
                System.runFinalization();
                System.gc();
                Thread.currentThread().sleep(500);
                System.gc();
                Thread.currentThread().sleep(500);
                System.gc();
                Thread.currentThread().sleep(500);
            }catch(Exception exc){}
        }
    }
    
    /**
     * Set name for performance data. Measured value is stored to database under this name.
     * @return performance data name
     */
    public String setPerformanceName(){
        String performanceDataName = getPerformanceName();
        
        if(performanceDataName.equalsIgnoreCase("measureTime"))
            performanceDataName = this.getClass().getName();
        
        return performanceDataName;
    }
    
    /**
     * Compare each measured value with expected value.
     *  Test fails if one of the measured value is bigger than expected one.
     * @param measuredValues array of measured values
     */
    public void compare(long[] measuredValues){
        boolean fail = false;
        String measuredValuesString = "";
        
        for(int i=1; i<measuredValues.length; i++){
            measuredValuesString = measuredValuesString + " " + measuredValues[i];
            
            if( (i>1  && measuredValues[i] > expectedTime) || 
                (i==1 && measuredValues.length==1 && measuredValues[i] > expectedTime) )
                // fail if it's subsequent usage and it's over expected time or it's first usage without any other usages and it's over expected time
                fail = true;
            else if(i==1 && measuredValues.length > 1 && measuredValues[i] > 2*expectedTime)
                // fail if it's first usage and it isn't the last one and it's over 2-times expected time
                fail = true;
        }
        
        if(fail){
            captureScreen = false;
            fail("One of the measuredTime(s) ["+measuredValuesString+" ] > expectedTime["+expectedTime+"] - performance issue (it's ok if the first usage is in boundary <0,2*expectedTime>) .");
        }
    }
    
    /**
     * Ensures that all warm up tasks are already executed so the tests may begin.
     */
    private void checkWarmup() {
        if (warmupFinished) {
            return;
        }
        try {
            Class cls = Class.forName("org.netbeans.core.WarmUpSupport"); // NOI18N
            java.lang.reflect.Field fld = cls.getDeclaredField("finished"); // NOI18N
            fld.setAccessible(true);
            // assume that warmup should not last more than 20sec
            for (int i=20; i>0; i--) {
                warmupFinished = fld.getBoolean(null);
                if (warmupFinished) {
                    return;
                }
                try {
                    log("checkWarmup - waiting");
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    // do nothing
                }
            }
            fail("checkWarmup - waiting for warmup completion failed");
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /**
     * If scan dialog rises wait some time after scan finished
     */
    public void checkScanFinished() {
        if(org.netbeans.junit.ide.ProjectSupport.waitScanFinished())
            waitNoEvent(1000);
    }
    
    
    /**
     * This method returns meaasured time, it goes through all logged data by
     * guitracker (LoggingEventQueue and LoggingRepaintManager).
     * The measured time is the difference between :
     * <ul>
     *     <li> last START or
     *     <li> last MOUSE_PRESS (if the measure_mouse_press property is true)
     *     <li> last MOUSE_RELEASE - by default (if the measure_mouse_press property is false)
     *     <li> last KEY_PRESS
     * </ul>
     * and
     * <ul>
     *     <li> last or expected paint
     *     <li> last FRAME_SHOW
     *     <li> last DIALOG_SHOW
     *     <li> last COMPONENT_SHOW
     * </ul>
     * @return measured time
     */
    public long getMeasuredTime(){
        EventList lst = tr.getCurrentEvents();
        ActionTracker.Tuple[] events = (ActionTracker.Tuple[]) lst.toArray(new ActionTracker.Tuple[0]);
        long start = 0L;
        long end = 0L;
        for (int i = 0; i < events.length; i++) {
            ActionTracker.Tuple t = events[i];
            int code = t.getCode();
            if (code == ActionTracker.TRACK_START
                    // it could be ActionTracker.TRACK_MOUSE_RELEASE (by default) or ActionTracker.TRACK_MOUSE_PRESS or ActionTracker.TRACK_MOUSE_MOVE
                    || code == track_mouse_event    
                    || code == ActionTracker.TRACK_KEY_PRESS) {
                start = t.getTimeMillis();
            } else if (code == ActionTracker.TRACK_PAINT
                    || code == ActionTracker.TRACK_FRAME_SHOW
                    || code == ActionTracker.TRACK_DIALOG_SHOW
                    || code == ActionTracker.TRACK_COMPONENT_SHOW
                    ) {
                end = t.getTimeMillis();
            }
        }
        if (start > end || start == 0) {
            throw new IllegalStateException("Measuring failed, because we start["+start+"] > end["+end+"] or start=0");
        }
        return (end - start);
    }
    
    /**
     * Data are logged to the file, it helps with evaluation of the failure
     * as well as it shows what exactly is meaured (user can find the start event
     * and stop paint/show) .
     */
    public void dumpLog(){
        tr.stopRecording();
        try {
            tr.exportAsXML(getLog("ActionTracker.xml"));
        } catch (Exception ex) {
            throw new Error("Exception while generating log", ex);
        }
        tr.forgetAllEvents();
        tr.startRecording();
    }
    
    /**
     * Waits for a period of time during which no event is processed by event queue.
     * @param time time to wait for after last event in EventQueue.
     */
    protected void waitNoEvent(long time) {
        if(repeat_memory!=-1){
            new QueueTool().waitEmpty(time);
        }else{
            // XXX need to reimplement
            rm.waitNoPaintEvent(time);
        }
    }
    
    
    /**
     * Getter for all measured performance data from current test
     * @return PerformanceData[] performance data
     */
    public NbPerformanceTest.PerformanceData[] getPerformanceData() {
        return (NbPerformanceTest.PerformanceData[])(data.toArray(new NbPerformanceTest.PerformanceData[0]));
    }
    
    /**
     * Setter for test case name. It is possible to set name of test case, it is useful if you have
     *  test suite where called test methods (with the same name) are from different classes, which is
     *  true if your tests extend PerformanceTestCase.
     * @param oldName old TestCase name
     * @param newName new TestCase name
     */
    public void setTestCaseName(String oldName, String newName){
        renamedTestCaseName.put(oldName,newName);
    }
    
    /**
     * Getter for test case name. It overwrites method getName() from superclass. It is necessary to diversify
     * method names if the test methods (with the same name) are runned from different classes, which is
     * done if your tests extend PerformanceTestCase.
     * @return testCaseName (all '|' are replaced by '#' if it was changed if not call super.getName() !
     */
    public String getName(){
        String originalTestCaseName = super.getName();
        
        if(renamedTestCaseName.containsKey(originalTestCaseName))
            return ((String)renamedTestCaseName.get(originalTestCaseName)).replace('|','-'); // workarround for problem on Win, there isn't possible cretae directories with '|'
        else
            return originalTestCaseName;
    }
    
    /**
     * Returns performance data name
     * @return performance data name if it was changed if not call super.getName() !
     */
    public String getPerformanceName(){
        String originalTestCaseName = super.getName();
        
        if(renamedTestCaseName.containsKey(originalTestCaseName))
            return (String)renamedTestCaseName.get(originalTestCaseName);
        else
            return originalTestCaseName;
    }
    
    /**
     * Closes all opened dialogs.
     */
    public static void closeAllDialogs() {
        javax.swing.JDialog dialog;
        org.netbeans.jemmy.ComponentChooser chooser = new org.netbeans.jemmy.ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return(comp instanceof javax.swing.JDialog && comp.isShowing());
            }
            public String getDescription() {
                return("Dialog");
            }
        };
        while((dialog = (javax.swing.JDialog)org.netbeans.jemmy.DialogWaiter.getDialog(chooser)) != null) {
            closeDialogs(findBottomDialog(dialog, chooser), chooser);
        }
    }
    
    /**
     * Find Bottom dialogs.
     * @param dialog find all dialogs of owner for this dialog
     * @param chooser chooser used for looking for dialogs
     * @return return bottm dialog
     */
    private static javax.swing.JDialog findBottomDialog(javax.swing.JDialog dialog, org.netbeans.jemmy.ComponentChooser chooser) {
        java.awt.Window owner = dialog.getOwner();
        if(chooser.checkComponent(owner)) {
            return(findBottomDialog((javax.swing.JDialog)owner, chooser));
        }
        return(dialog);
    }
    
    /**
     * Close dialogs
     * @param dialog find all dialogs of owner for this dialog
     * @param chooser chooser used for looking for dialogs
     */
    private static void closeDialogs(javax.swing.JDialog dialog, org.netbeans.jemmy.ComponentChooser chooser) {
        java.awt.Window[] ownees = dialog.getOwnedWindows();
        for(int i = 0; i < ownees.length; i++) {
            if(chooser.checkComponent(ownees[i])) {
                closeDialogs((javax.swing.JDialog)ownees[i], chooser);
            }
        }
        new org.netbeans.jemmy.operators.JDialogOperator(dialog).close();
    }
    
    /**
     * Get screenshot - if testedComponentOperator=null - then grap whole screen Black&White,
     * if isn't grap area with testedComponent (-100,-100, width+200, height+200)
     * @param i order of measurement in one test case
     */
    protected void getScreenshotOfMeasuredIDEInTimeOfMeasurement(int i){
        try {
            if(testedComponentOperator==null){
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+java.io.File.separator+"screen_"+i+".png",PNGEncoder.BW_MODE);
            }else{
                java.awt.Point locationOnScreen = testedComponentOperator.getLocationOnScreen();
                java.awt.Rectangle bounds = testedComponentOperator.getBounds();
                java.awt.Rectangle bounds_new = new java.awt.Rectangle(locationOnScreen.x-100, locationOnScreen.y-100, bounds.width+200, bounds.height+200);
                java.awt.Rectangle screen_size = new java.awt.Rectangle(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
                
                if(bounds_new.height > screen_size.height/2 || bounds_new.width > screen_size.width/2)
                    PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+java.io.File.separator+"screen_"+i+".png",PNGEncoder.BW_MODE);
                else
                    PNGEncoder.captureScreen(bounds_new,getWorkDir().getAbsolutePath()+java.io.File.separator+"screen_"+i+".png",PNGEncoder.GREYSCALE_MODE);
//System.err.println("XX "+rm.getRepaintedArea());
//                PNGEncoder.captureScreen(rm.getRepaintedArea(),getWorkDir().getAbsolutePath()+java.io.File.separator+"screen_"+i+".png",PNGEncoder.GREYSCALE_MODE);
            }
        } catch (Exception exc) {
            log(" Exception rises during capturing screenshot of measurement ");
            exc.printStackTrace(getLog());
        }
    }
    
    /**
     * Get screenshot of whole screen if exception rised during initialize()
     * @param title title is part of the screenshot file name
     */
    protected void getScreenshot(String title){
        try{
            PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+java.io.File.separator+"error_screenshot_" + title + ".png");
        }catch(Exception exc){
            log(" Exception rises during capturing screenshot ");
            exc.printStackTrace(getLog());
        }
        
    }
    
}
