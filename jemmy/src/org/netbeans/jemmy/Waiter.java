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
 * Contributor(s): Alexandre Iline,
 *                 Manfred Riem (mriem@netbeans.org).
 */
package org.netbeans.jemmy;

import java.awt.Component;

/**
 * 
 * Waits for something defined by Waitable interface to be happened.
 * 
 * <BR><BR>Timeouts used: <BR>
 * Waiter.TimeDelta - time delta to check actionProduced result.<BR>
 * Waiter.WaitingTime - maximal waiting time<BR>
 * Waiter.AfterWaitingTime - time to sleep after waiting has been finished.<BR>
 *
 * @see Timeouts
 * @see Waitable
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class Waiter implements Waitable, Timeoutable, Outputable{

    private final static long TIME_DELTA = 10;
    private final static long WAIT_TIME = 60000;
    private final static long AFTER_WAIT_TIME = 0;

    private Waitable waitable;
    private long startTime = 0;
    private long endTime = -1;
    private Object result;
    private Timeouts timeouts;
    private TestOut out;

    /**
     * Constructor.
     * @param	w Waitable object defining waiting criteria.
     */
    public Waiter(Waitable w) {
	super();
        // TODO: refactor code to get timeouts from another place, because 
        //       JemmyProperties initializes the Robot.
	// setTimeouts(JemmyProperties.getProperties().getTimeouts());
        // TODO: refactor code to use JDK logging.
	// setOutput(JemmyProperties.getProperties().getOutput());
        timeouts = new Timeouts();
        timeouts.setTimeout("Waiter.TimeDelta", TIME_DELTA);
        timeouts.setTimeout("Waiter.WaitingTime", WAIT_TIME);
        timeouts.setTimeout("Waiter.AfterWaitingTime", AFTER_WAIT_TIME);
	waitable = w;
    }

    /**
     * Can be used from subclass.
     */
    protected Waiter() {
	super();
        // TODO: refactor code to get timeouts from another place, because 
        //       JemmyProperties initializes the Robot.
	// setTimeouts(JemmyProperties.getProperties().getTimeouts());
        // TODO: refactor code to use JDK logging.
	// setOutput(JemmyProperties.getProperties().getOutput());
        timeouts = new Timeouts();
        timeouts.setTimeout("Waiter.TimeDelta", TIME_DELTA);
        timeouts.setTimeout("Waiter.WaitingTime", WAIT_TIME);
        timeouts.setTimeout("Waiter.AfterWaitingTime", AFTER_WAIT_TIME);
    }

    static {
	Timeouts.initDefault("Waiter.TimeDelta", TIME_DELTA);
	Timeouts.initDefault("Waiter.WaitingTime", WAIT_TIME);
	Timeouts.initDefault("Waiter.AfterWaitingTime", AFTER_WAIT_TIME);
    }

    /**
     * Defines current timeouts.
     * 
     * @param	timeouts A collection of timeout assignments.
     * @see	org.netbeans.jemmy.Timeoutable
     * @see	org.netbeans.jemmy.Timeouts
     * @see #getTimeouts
     */
    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
    }

    /**
     * Return current timeouts.
     * @return the collection of current timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     * @see #setTimeouts
     */
    public Timeouts getTimeouts() {
	return(timeouts);
    }

    /**
     * Defines print output streams or writers.
     * @param out Identify the streams or writers used for print output.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     * @see #getOutput
     */
    public void setOutput(TestOut out) {
	this.out = out;
    }

    /**
     * Returns print output streams or writers.
     * @return an object that contains references to objects for
     * printing to output and err streams.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     * @see #setOutput
     */
    public TestOut getOutput() {
	return(out);
    }

    /**
     * Waits for not null result of actionProduced method of Waitable implementation passed into constructor.
     * @param	waitableObject Object to be passed into actionProduced method.
     * @return non null result of action.
     * @throws	TimeoutExpiredException
     * @exception	InterruptedException
     */
    public Object waitAction(Object waitableObject)
	throws InterruptedException {
	startTime = System.currentTimeMillis();
	// out.printTrace(getWaitingStartedMessage());
	// out.printGolden(getGoldenWaitingStartedMessage());
	long timeDelta = timeouts.getTimeout("Waiter.TimeDelta");
	while((result = checkActionProduced(waitableObject)) == null) {
	    Thread.currentThread().sleep(timeDelta);
	    if(timeoutExpired()) {
		// out.printError(getTimeoutExpiredMessage(timeFromStart()));
		// out.printGolden(getGoldenTimeoutExpiredMessage());
		throw(new TimeoutExpiredException(getActualDescription()));
	    }
	}
	endTime = System.currentTimeMillis();
	// out.printTrace(getActionProducedMessage(endTime - startTime, result));
	//out.printGolden(getGoldenActionProducedMessage());
	Thread.currentThread().sleep(timeouts.getTimeout("Waiter.AfterWaitingTime"));
	return(result);
    }

    /**
     * @see	Waitable
     * @param	obj
     */
    public Object actionProduced(Object obj) {
	return(Boolean.TRUE);
    }

    /** 
     * @see Waitable
     */
    public String getDescription() {
	return("Unknown waiting");
    }

    /**
     * Returns message to be printed before waiting start.
     * @return a message.
     */
    protected String getWaitingStartedMessage() {
	return("Start to wait action \"" + getActualDescription() + "\"");
    }

    /**
     * Returns message to be printed when waiting timeout has been expired.
     * @param timeSpent time from waiting start (milliseconds)
     * @return a message.
     */
    protected String getTimeoutExpiredMessage(long timeSpent) {
	return("\"" + getActualDescription() + "\" action has not been produced in " +
	       (new Long(timeSpent)).toString() + " milliseconds");
    }

    /**
     * Returns message to be printed when waiting has been successfully finished.
     * @param timeSpent time from waiting start (milliseconds)
     * @param result result of Waitable.actionproduced method.
     * @return a message.
     */
    protected String getActionProducedMessage(long timeSpent, final Object result) {
        String resultToString;
        if(result instanceof Component) {
            // run toString in dispatch thread
            resultToString = (String)new QueueTool().invokeSmoothly(
                new QueueTool.QueueAction("result.toString()") {
                    public Object launch() {
                        return result.toString();
                    }
                }
            );
        } else {
            resultToString = result.toString();
        }
	return("\"" + getActualDescription() + "\" action has been produced in " +
	       (new Long(timeSpent)).toString() + " milliseconds with result " +
	       "\n    : " + resultToString);
    }

    /**
     * Returns message to be printed int golden output before waiting start.
     * @return a message.
     */
    protected String getGoldenWaitingStartedMessage() {
	return("Start to wait action \"" + getActualDescription() + "\"");
    }

    /**
     * Returns message to be printed int golden output when waiting timeout has been expired.
     * @return a message.
     */
    protected String getGoldenTimeoutExpiredMessage() {
	return("\"" + getActualDescription() + "\" action has not been produced");
    }

    /**
     * Returns message to be printed int golden output when waiting has been successfully finished.
     * @return a message.
     */
    protected String getGoldenActionProducedMessage() {
	return("\"" + getActualDescription() + "\" action has been produced");
    }

    /**
     * Returns time from waiting start.
     * @return Time spent for waiting already.
     */
    protected long timeFromStart() {
	return(System.currentTimeMillis() - startTime);
    }

    private Object checkActionProduced(Object obj) {
	if(waitable != null) {
	    return(waitable.actionProduced(obj));
	} else {
	    return(actionProduced(obj));
	}
    }

    private String getActualDescription() {
	if(waitable != null) {
	    return(waitable.getDescription());
	} else {
	    return(getDescription());
	}
    }

    private boolean timeoutExpired() {
	return(timeFromStart() > timeouts.getTimeout("Waiter.WaitingTime"));
    }

}
