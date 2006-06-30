/*
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
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy;

import java.io.PrintStream;
import java.io.PrintWriter;

import java.lang.reflect.InvocationTargetException;

/**
 *
 * Jemmy itself provides a way to create tests.
 * Test should implement org.netbeans.jemmy.Scenario interface.
 *
 * Test can be executed from command line:<BR>
 * <code>java [application options] [jemmy options] org.netbeans.jemmy.Test [full name of test class] [test args]</code><BR>
 * Test elso can be executed by one of the run(...) methods or by <BR>
 * <code>new Test([test class name]).startTest([test args]);</code><BR>
 *
 * <BR><BR>Timeouts used: <BR>
 * Test.WholeTestTimeout - time for the whole test<BR>
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class Test extends ActionProducer
    implements Timeoutable, Outputable, Scenario {

    private final static long WHOLE_TEST_TIMEOUT = 3600000;

    /**
     * Status returned by test if wrong parameter was passed.
     */
    public static int WRONG_PARAMETERS_STATUS = 101;

    /**
     * Status returned by test if exception appeared inside scenario.
     */
    public static int SCENARIO_EXCEPTION_STATUS = 102;

    /**
     * Positive test status.
     */
    public static int TEST_PASSED_STATUS = 0;

    /**
       Test timeouts.
     */
    protected Timeouts timeouts;

    /**
       Test output.
     */
    protected TestOut output;

    private Scenario scenario;
    private static int TEST_FAILED_STATUS = 1;

    /**
     * Constructor for tests requiring only a class instance.
     * Creates a subclass of <code>ActionProducer</code> and <code>java.lang.Thread</code>
     * that runs in a separate thread of execution and waits for execution to finish.
     * The current output stream assignments and timeouts are used.
     * @param testClassName Full test class name
     */
    public Test(String testClassName) {
	super(true);
	setOutput(JemmyProperties.getCurrentOutput());
	setTimeouts(JemmyProperties.getCurrentTimeouts());
	scenario = testForName(testClassName);
    }

    /**
     * Constructor for scenarios that require an instance and might require an argument.
     * Creates a subclass of <code>ActionProducer</code> and <code>java.lang.Thread</code>
     * that runs in a separate thread of execution and waits for execution to finish.
     * The current output stream assignments and timeouts are used.
     * @param scenario a test scenario
     * @see org.netbeans.jemmy.Scenario
     */
    public Test(Scenario scenario) {
	super(true);
	setOutput(JemmyProperties.getCurrentOutput());
	setTimeouts(JemmyProperties.getCurrentTimeouts());
	this.scenario = scenario;
    }

    /**
     * No argument constructor.
     * Used by subclasses of this <code>Test</code> class.
     * Creates a subclass of <code>ActionProducer</code> and <code>java.lang.Thread</code>
     * that runs in a separate thread of execution and waits for execution to finish.
     * The current output stream assignments and timeouts are used.
     */
    protected Test() {
	super(true);
	setOutput(JemmyProperties.getCurrentOutput());
	setTimeouts(JemmyProperties.getCurrentTimeouts());
    }

    /**
     * Throws TestCompletedException exception.
     * The exception thrown contains a pass/fail status and a short
     * status <code>java.lang.String</code>.
     * Can by invoked from test to abort test execution.
     * @param status If 0 - test passed, otherwise failed.
     * @throws TextCompletedException all of the time.
     */
    public static void closeDown(int status) {
	if(status == 0) {
	    throw(new TestCompletedException(status, "Test passed"));
	} else {
	    throw(new TestCompletedException(status, "Test failed with status " + 
					     Integer.toString(status)));
	}
    }

    /**
     * Executes a test.
     * @param argv First element should be a test class name,
     * all others - test args.
     * @return test status.
     */
    public static int run(String[] argv) {
	String[] args = argv;
	JemmyProperties.getProperties().init();
	if(argv.length < 1) {
	    JemmyProperties.getCurrentOutput().
		printErrLine("First element of String array should be test classname");
	    return(WRONG_PARAMETERS_STATUS);
	}
	JemmyProperties.getCurrentOutput().printLine("Executed test " + argv[0]);
	Test test = new Test(argv[0]);
	if(argv.length >= 1) {
	    args = shiftArray(args);
	}
	if(argv.length >= 2) {
	    JemmyProperties.getCurrentOutput().printLine("Work directory: " + argv[1]);
	    System.setProperty("user.dir", argv[1]);
	    args = shiftArray(args);
	}
	int status = TEST_FAILED_STATUS;
	if(test != null) {
	    status = test.startTest(args);
	}
	JemmyProperties.getCurrentOutput().flush();
	return(status);
    }

    /**
     * Executes a test.
     * @param argv First element should be a test class name,
     * all others - test args.
     * @param output Stream to put test output and errput into.
     * @return test status.
     */
    public static int run(String[] argv, PrintStream output) {
	JemmyProperties.setCurrentOutput(new TestOut(System.in, output, output));
	return(run(argv));
    }

    /**
     * Executes a test.
     * @param argv First element should be a test class name,
     * all others - test args.
     * @param output Stream to put test output into.
     * @param errput Stream to put test errput into.
     * @return test status.
     */
    public static int run(String[] argv, PrintStream output, PrintStream errput) {
	JemmyProperties.setCurrentOutput(new TestOut(System.in, output, errput));
	return(run(argv));
    }

    /**
     * Executes a test.
     * @param argv First element should be a test class name,
     * all others - test args.
     * @param output Writer to put test output and errput into.
     * @return test status.
     */
    public static int run(String[] argv, PrintWriter output) {
	JemmyProperties.setCurrentOutput(new TestOut(System.in, output, output));
	return(run(argv));
    }

    /**
     * Executes a test.
     * @param argv First element should be a test class name,
     * all others - test args.
     * @param output Writer to put test output into.
     * @param errput Writer to put test errput into.
     * @return test status.
     */
    public static int run(String[] argv, PrintWriter output, PrintWriter errput) {
	JemmyProperties.setCurrentOutput(new TestOut(System.in, output, errput));
	return(run(argv));
    }

    /**
     * Invoke this <code>Test</code>.
     * The call might be directly from the command line.
     * @param argv First element should be a test class name,
     * all others - test args.
     */
    public static void main(String[] argv) {
	System.exit(run(argv, System.out));
    }

    static {
	Timeouts.initDefault("Test.WholeTestTimeout", WHOLE_TEST_TIMEOUT);
    }

    /**
     * Creates an instance of a class named by the parameter.
     * @param testName Full test class name
     * @return an instance of the test <code>Scenario</code> to launch.
     * @see org.netbeans.jemmy.Scenario
     */
    public Scenario testForName(String testName) {
	try {
	    return((Scenario)(
			  Class.forName(testName).
			  getConstructor(new Class[0]).
			  newInstance(new Object[0])));
	} catch (ClassNotFoundException e) {
	    output.printErrLine("Class " + testName + " does not exist!");
	    output.printStackTrace(e);
	} catch (NoSuchMethodException e) {
	    output.printErrLine("Class " + testName + " has not constructor!");
	    output.printStackTrace(e);
	} catch (InvocationTargetException e) {
	    output.printErrLine("Exception inside " + testName + " constructor:");
	    output.printStackTrace(e.getTargetException());
	} catch (IllegalAccessException e) {
	    output.printErrLine("Cannot access to " + testName + " constructor!");
	    output.printStackTrace(e);
	} catch (InstantiationException e) {
	    output.printErrLine("Cannot instantiate " + testName + " class!");
	    output.printStackTrace(e);
	}
	return(null);
    }

    /**
     * Set the timeouts used by this <code>Test</code>.
     * @param	timeouts A collection of timeout assignments.
     * @see	org.netbeans.jemmy.Timeoutable
     * @see	org.netbeans.jemmy.Timeouts
     * @see #getTimeouts
     */
    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
	Timeouts times = timeouts.cloneThis();
	times.setTimeout("ActionProducer.MaxActionTime", 
			 timeouts.getTimeout("Test.WholeTestTimeout"));
	super.setTimeouts(times);
    }

    /**
     * Get the timeouts used by this <code>Test</code>.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     * @see #setTimeouts
     */
    public Timeouts getTimeouts() {
	return(timeouts);
    }

    /**
     * Set the streams or writers used for print output.
     * @param out An object used to identify both output and error
     * print streams.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     * @see #getOutput
     */
    public void setOutput(TestOut out) {
	output = out;
	super.setOutput(out);
    }

    /**
     * Get the streams or writers used for print output.
     * @return an object containing references to both output and error print
     * streams.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     * @see #setOutput
     */
    public TestOut getOutput() {
	return(output);
    }

    /**
     * Executes test.
     * @param param Object to be passed into this test's launch(Object) method.
     * @return test status.
     */
    public int startTest(Object param) {
	if(scenario != null) {
	    output.printLine("Test " + scenario.getClass().getName() + 
			     " has been started");
	} else {
	    output.printLine("Test " + getClass().getName() + 
			     " has been started");
	}
	try {
	    return(((Integer)produceAction(param)).intValue());
	} catch (InterruptedException e) {
	    output.printErrLine("Test was interrupted.");
	    output.printStackTrace(e);
	} catch (TimeoutExpiredException e) {
	    output.printErrLine("Test was not finished in " +
				Long.toString(timeouts.getTimeout("Test.WholeTestTimeout")) +
				" milliseconds");
	    output.printStackTrace(e);
	} catch (Exception e) {
	    output.printStackTrace(e);
	}
	return(1);
    }

    /**
     * Launch an action.
     * Pass arguments to and execute a test <code>Scenario</code>.
     * @param obj An argument object that controls test execution.
     * This might be a <code>java.lang.String[]</code> containing
     * command line arguments.
     * @see org.netbeans.jemmy.Action
     * @return an Integer containing test status.
     */
    public final Object launch(Object obj) {
	setTimeouts(timeouts);
	int status;
	try {
	    if(scenario != null) {
		closeDown(scenario.runIt(obj));
	    } else {
		closeDown(runIt(obj));
	    }
	} catch(TestCompletedException e) {
	    output.printStackTrace(e);
	    return(new Integer(e.getStatus()));
	} catch(Throwable e) {
	    output.printStackTrace(e);
	    return(new Integer(SCENARIO_EXCEPTION_STATUS));
	}
	return(new Integer(TEST_PASSED_STATUS));
    }

    /**
     * Supposed to be overridden to print a synopsys into test output.
     */
    public void printSynopsis() {
	output.printLine("Here should be a test synopsis.");
    }

    /**
     * @see org.netbeans.jemmy.Action
     */
    public final String getDescription() {
	return("Test " + scenario.getClass().getName() + " finished");
    }

    /**
     * Defines a way to execute this <code>Test</code>.
     * @param param An object passed to configure the test scenario
     * execution.  For example, this parameter might be a
     * <code>java.lang.String[]<code> object that lists the
     * command line arguments to the Java application corresponding
     * to a test.
     * @return an int that tells something about the execution.
     * For, example, a status code.
     * @see org.netbeans.jemmy.Scenario
     */
    public int runIt(Object param){
	return(0);
    }

    /**
     * Sleeps.
     * @param time The sleep time in milliseconds.
     */
    protected void doSleep(long time) {
	try {
	    Thread.currentThread().sleep(time);
	} catch(InterruptedException e) {
	}
    }

    private static String[] shiftArray(String[] orig) {
	String[] result = new String[orig.length - 1];
	for(int i = 0; i < result.length; i++) {
	    result[i] = orig[i+1];
	}
	return(result);
    }

}
