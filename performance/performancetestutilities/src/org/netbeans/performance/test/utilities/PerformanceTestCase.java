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
import java.util.ListIterator;

import org.netbeans.jellytools.JellyTestCase;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.WindowOperator;

import org.netbeans.junit.NbPerformanceTest;
import org.netbeans.modules.editor.options.BaseOptions;

import org.netbeans.performance.test.guitracker.ActionTracker;
import org.netbeans.performance.test.guitracker.ActionTracker.EventList;
import org.netbeans.performance.test.guitracker.LoggingRepaintManager;
import org.netbeans.performance.test.guitracker.LoggingEventQueue;


/**
 * Test case with implemented Performance Tests Validation support stuff.
 * This class provide methods for QA Performance measurement.
 * Implemented methods:
 * <pre>
 *          testMeasureTime()
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
     * <br><b>default</b> = UI_RESPONSE
     */
    public long expectedTime = UI_RESPONSE;
    
    /**
     * Maximum number of iterations to wait for last paint on component/container.
     * <br><b>default</b> = 10 iterations
     */
    public int MAX_ITERATION = 10;
    
    /**
     * Defines delay between checks if the component/container is painted.
     * <br><b>default</b> = 1000 ms
     */
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
    
    /** Number of the paint we'll measure
     * <br><b>default</b> = 0  and measured time will be last paint until WAIT_AFTER_OPEN after user event.*/
    public int MEASURE_PAINT_NUMBER = 0;
    
    /** Count of repeats */
    protected int repeat = Integer.getInteger("org.netbeans.performance.repeat", 1).intValue();
    
    /** Count of repeats for measure memory usage */
    protected int repeat_memory = Integer.getInteger("org.netbeans.performance.memory.repeat", -1).intValue();
    
    /** Performance data. */
    private static java.util.ArrayList data;
    
    /** Warmup finished flag. */
    private static boolean warmupFinished = false;

    /** tracker for UI activities */
    private static ActionTracker tr;
    
    private static LoggingRepaintManager rm;
    
    private static LoggingEventQueue leq;
    
    static {
        // XXX load our EQ and repaint manager
        tr = ActionTracker.getInstance();
        rm = new LoggingRepaintManager(tr);
        rm.setEnabled(true);
        leq = new LoggingEventQueue(tr);
        leq.setEnabled(true);
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
        //if scan dialog rises wait some time after scan finished
        if(org.netbeans.junit.ide.ProjectSupport.waitScanFinished())
            waitNoEvent(1000);
        
        checkWarmup ();
        
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
//        if(repeat_memory==-1)
            measureTime();
//        else
//            measureMemoryUsage();
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
        //        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        JemmyProperties.setCurrentTimeout("EventDispatcher.RobotAutoDelay", 1);
        log("----------------------- DISPATCHING MODEL = "+JemmyProperties.getCurrentDispatchingModel());
        
        EventTool et = new EventTool();
        
        tr.startNewEventList("test beggining"); // XXX add test name
        tr.add(tr.TRACK_APPLICATION_MESSAGE, "expectedTime "+expectedTime+", "+
                "repeat "+repeat+", "+
                "paint_number "+MEASURE_PAINT_NUMBER+", "+
                "after_prepare "+WAIT_AFTER_PREPARE+", "+
                "after_open "+WAIT_AFTER_OPEN+", "+
                "after_close "+WAIT_AFTER_CLOSE+", "+
                "paint "+WAIT_PAINT+", "+
                "iteration "+MAX_ITERATION);
        
        String performanceDataName = setPerformanceName();
        
        try {
            initialize();
            
            for(int i=1; i<=repeat && !exceptionDuringMeasurement; i++){
                try {
                    tr.startNewEventList("test iteration no."+i); // XXX add test name
                    tr.connectToAWT(true);
                    prepare();
                    rm.waitNoEvent(WAIT_AFTER_PREPARE);
                    
                    // Uncomment if you want to run with analyzer tool
                    // com.sun.forte.st.collector.CollectorAPI.resume ();
                    
                    tr.add(tr.TRACK_START, "before open");
                    testedComponentOperator = open();
                    
                    // this is to optimize delays
                    long wait_time = (wait_after_open_heuristic>WAIT_AFTER_OPEN)?WAIT_AFTER_OPEN:wait_after_open_heuristic;
                    tr.add(tr.TRACK_APPLICATION_MESSAGE, "wait_after_open_heuristic "+wait_time);
                    rm.waitNoEvent(wait_time);
                    
                    // PENDING need to check this
//                    if(testedComponentOperator != null) {
//                        java.awt.EventQueue.writeOutput("<wait_until_painted time=\""+System.currentTimeMillis()+"\">");
//                        Component comp = testedComponentOperator.getSource();
//                        waitUntilPainted(comp);
//                        java.awt.EventQueue.writeOutput("</wait_until_painted>");
//                    }
                    new org.netbeans.jemmy.QueueTool().waitEmpty();
                    
                    measuredTime[i] = getMeasuredTime();
                    tr.add(tr.TRACK_APPLICATION_MESSAGE, "measured time "+measuredTime[i]);
                    
                    wait_after_open_heuristic = (long)(measuredTime[i]*1.25);
                    
                    log("Measured Time ["+performanceDataName+" | "+i+"] = " +measuredTime[i]);
                    
                    if(measuredTime[i] > 0)
                        reportPerformance(performanceDataName, measuredTime[i], "ms", i, expectedTime);
                    else
                        fail("Measured value ["+measuredTime+"] is not > 0 !");
                    
//                    getScreenshotOfMeasuredIDEInTimeOfMeasurement(i);
                    
                }catch(Exception exc){ // catch for prepare(), open()
                    log("------- [ "+i+" ] ---------------- Exception rises while measuring performance :"+exc.getMessage());
                    exc.printStackTrace(getLog());
                    exceptionDuringMeasurement = true;
                    // throw new JemmyException("Exception arises during measurement:"+exc.getMessage());
                }finally{ // finally for prepare(), open()
                    try{
                        // Uncomment if you want to run with analyzer tool
                        // com.sun.forte.st.collector.CollectorAPI.pause ();
                        
                        tr.add(tr.TRACK_APPLICATION_MESSAGE, "before close");
                        close();
                        
                        closeAllModal();
                        rm.waitNoEvent(WAIT_AFTER_CLOSE);
                        
                    }catch(Exception e){ // catch for close()
                        log("------- [ "+i+" ] ---------------- Exception rises while closing tested component :"+e.getMessage());
                        e.printStackTrace(getLog());
//                        getScreenshot("measure");
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
        }catch (Exception e) { // catch for initialize(), shutdown(), closeAllDialogs()
            log("----------------------- Exception rises while shuting down / initializing:"+e.getMessage());
            e.printStackTrace(getLog());
//            getScreenshot("init_or_shutdown");
            // throw new JemmyException("Exception rises while shuting down :"+e.getMessage());
            exceptionDuringMeasurement = true;
        }finally{ // finally for initialize(), shutdown(), closeAllDialogs()
            // XXX export results?
        }
        
        dumpLog ();
        if(exceptionDuringMeasurement)
            throw new Error("Exception rises during measurement, look at appropriate log file for stack trace(s).");
        
        compare(measuredTime);
        
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
     * @param threshold
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
     * Sets an area filter to the JDK hack. Paints out of this area will be ignored.
     */
    protected void setAreaToFilter(int x, int y, int w, int h) {
        if(repeat_memory==-1)
            throw new UnsupportedOperationException("setAreaToFilter");
    }
    
    /**
     * Turn's off blinking of the caret in the Java editor.
     * A method generally useful for any UI Responsiveness tests which measure actions
     * in the Java editor. This method should be called from a test's initialize() method.
     */
    protected void setJavaEditorCaretFilteringOn() {
        // turn off blinking of the caret in the Java editor
        Class kitClass = org.netbeans.modules.editor.java.JavaKit.class;
        BaseOptions options = BaseOptions.getOptions(kitClass);
        options.setCaretBlinkRate(0);
    }
    
    /**
     * Turn's off blinking of the caret in the plain text editor.
     * A method generally useful for any UI Responsiveness tests which measure actions
     * in the plain text editor. This method should be called from a test's initialize() method.
     */
    protected void setPlainTextEditorCaretFilteringOn() {
        // turn off blinking of the caret in the plain text editor
        Class kitClass = org.netbeans.modules.editor.plain.PlainKit.class;
        BaseOptions options = BaseOptions.getOptions(kitClass);
        options.setCaretBlinkRate(0);
    }
    
    /**
     * Turn's off blinking of the caret in the XML editor.
     * A method generally useful for any UI Responsiveness tests which measure actions
     * in the XML editor. This method should be called from a test's initialize() method.
     */
    protected void setXMLEditorCaretFilteringOn() {
        // turn off blinking of the caret in the XML editor
        Class kitClass = org.netbeans.modules.xml.text.syntax.XMLKit.class;
        BaseOptions options = BaseOptions.getOptions(kitClass);
        options.setCaretBlinkRate(0);
    }
    
    /**
     * Turn's off blinking of the caret in the JSP editor.
     * A method generally useful for any UI Responsiveness tests which measure actions
     * in the JSP editor. This method should be called from a test's initialize() method.
     */
    protected void setJSPEditorCaretFilteringOn() {
        // turn off blinking of the caret in the JSP editor
        Class kitClass = org.netbeans.modules.web.core.syntax.JSPKit.class;
        BaseOptions options = BaseOptions.getOptions(kitClass);
        options.setCaretBlinkRate(0);
    }
    
    protected void setPaintFilteringForEditor () {
        fail ("Need to implement filtering for editor.");
    }
    
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
    
    /** Set name for performance data. Measured value is stored to database under this name.
     * @return performance data name
     */
/* It is not neccessary distinguish values by diferent names
 *
 public String setPerformanceName(int i){
        String performanceDataName = getPerformanceName();
 
        if(performanceDataName.equalsIgnoreCase("measureMemoryUsage"))
            performanceDataName = this.getClass().getName();
 
        if(i>0){
            return performanceDataName+"_"+(i+1);
        }
 
        return performanceDataName;
    }
 */
    
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
            
            if(measuredValues[i] > expectedTime)
                fail = true;
        }
        
        if(fail){
            captureScreen = false;
            fail("One of the measuredTime(s) ["+measuredValuesString+" ] > expectedTime["+expectedTime+"] - performance issue.");
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
                }
                catch (InterruptedException ie) {
                    // do nothing
                }
            }
            fail("checkWarmup - waiting for warmup completion failed");
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /**
     * Get measured time from overwritten JDK class EventQueue().</p>
     * <p>Measuring of time starts when the event queue is awakened by mouse or
     * key event and it can be finished when the queue is empty again and there was a
     * paint event. If there are more paints the difference between last paint and user
     * event is returned.</p>
     * @return measured time
     */
    public long getMeasuredTime(){
        // XX read measured data, find last START tuple and last (or expected) paint time
        // the difference is the result
        
        EventList lst = tr.getCurrentEvents();
        ListIterator/*ActionTracker.Tuple*/ it = lst.listIterator();
        long start = 0L;
        long end = 0L;
        while (it.hasNext()) {
            ActionTracker.Tuple t = (ActionTracker.Tuple)it.next();
            int code = t.getCode();
            if (code == ActionTracker.TRACK_START 
            || code == ActionTracker.TRACK_MOUSE_PRESS
            || code == ActionTracker.TRACK_KEY_PRESS) {
                start = t.getTimeMillis();
            }
            else if (code == ActionTracker.TRACK_PAINT
            || code == ActionTracker.TRACK_FRAME_SHOW
            || code == ActionTracker.TRACK_DIALOG_SHOW
            || code == ActionTracker.TRACK_COMPONENT_SHOW
                    ) {
                end = t.getTimeMillis();
            }
        }
        if (start > end || start == 0) {
            throw new IllegalStateException("measuring failed");
        }
        return (end - start);
    }
    
    /**
     * Get measured time from overwritten JDK class EventQueue().</p>
     * <p>Measuring of time starts when the event queue is awakened by mouse or
     * key event and it can be finished when the queue is empty again and there was a
     * paint event. If there are more paints the difference between last paint and user
     * event is returned.</p>
     * @return measured time
     */
    public void dumpLog(){
        tr.stopRecording();
        try {
            tr.exportAsXML(getLog("ActionTracker.xml"));
        }
        catch (Exception ex) {
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
            new EventTool().waitNoEvent(time);
        }else{
            // XXX need to reimplement
            rm.waitNoEvent(time);
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
    
//    /**
//     * Get screenshot - if testedComponentOperator=null - then grap whole screen Black&White,
//     * if isn't grap area with testedComponent (-100,-100, width+200, height+200)
//     * @param i order of measurement in one test case
//     */
//    protected void getScreenshotOfMeasuredIDEInTimeOfMeasurement(int i){
//        try {
//            if(testedComponentOperator==null){
//                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+java.io.File.separator+"screen_"+i+".png",PNGEncoder.BW_MODE);
//            }else{
//                java.awt.Point locationOnScreen = testedComponentOperator.getLocationOnScreen();
//                java.awt.Rectangle bounds = testedComponentOperator.getBounds();
//                java.awt.Rectangle bounds_new = new java.awt.Rectangle(locationOnScreen.x-100, locationOnScreen.y-100, bounds.width+200, bounds.height+200);
//                java.awt.Rectangle screen_size = new java.awt.Rectangle(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
//                
//                if(bounds_new.height > screen_size.height/2 || bounds_new.width > screen_size.width/2)
//                    PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+java.io.File.separator+"screen_"+i+".png",PNGEncoder.BW_MODE);
//                else
//                    PNGEncoder.captureScreen(bounds_new,getWorkDir().getAbsolutePath()+java.io.File.separator+"screen_"+i+".png",PNGEncoder.GREYSCALE_MODE);
//            }
//        } catch (Exception exc) {
//            log(" Exception rises during capturing screenshot of measurement ");
//            exc.printStackTrace(getLog());
//        }
//    }
    
    /** 
     * Get screenshot of whole screen if exception rised during initialize()
     * @param title title is part of the screenshot file name
     */
//    protected void getScreenshot(String title){
//        try{
//            PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+java.io.File.separator+"error_screenshot_" + title + ".png");
//        }catch(Exception exc){
//            log(" Exception rises during capturing screenshot ");
//            exc.printStackTrace(getLog());
//        }
//        
//    }
    
}
