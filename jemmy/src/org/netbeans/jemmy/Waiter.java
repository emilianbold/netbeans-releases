/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy;

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
     */
    public Waiter(Waitable w) {
	super();
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
	waitable = w;
    }

    /**
     * Can be used from subclass.
     */
    protected Waiter() {
	super();
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
    }

    static {
	Timeouts.initDefault("Waiter.TimeDelta", TIME_DELTA);
	Timeouts.initDefault("Waiter.WaitingTime", WAIT_TIME);
	Timeouts.initDefault("Waiter.AfterWaitingTime", AFTER_WAIT_TIME);
    }

    /**
     * Defines current timeouts.
     * @param t A collection of timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
    }

    /**
     * Return current timeouts.
     * @return the collection of current timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public Timeouts getTimeouts() {
	return(timeouts);
    }

    /**
     * Defines print output streams or writers.
     * @param out Identify the streams or writers used for print output.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
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
     */
    public TestOut getOutput() {
	return(out);
    }

    /**
     * Waits for not null result of actionProduced method of Waitable implementation passed into constructor.
     * @param waitableObject Object to be passed into actionProduced method.
     * @throws TimeoutExpiredException
     */
    public Object waitAction(Object waitableObject)
	throws InterruptedException {
	startTime = System.currentTimeMillis();
	out.printTrace(getWaitingStartedMessage());
	out.printGolden(getGoldenWaitingStartedMessage());
	long timeDelta = timeouts.getTimeout("Waiter.TimeDelta");
	while((result = checkActionProduced(waitableObject)) == null) {
	    Thread.currentThread().sleep(timeDelta);
	    if(timeoutExpired()) {
		out.printError(getTimeoutExpiredMessage(timeFromStart()));
		out.printGolden(getGoldenTimeoutExpiredMessage());
		throw(new TimeoutExpiredException(getActualDescription()));
	    }
	}
	endTime = System.currentTimeMillis();
	out.printTrace(getActionProducedMessage(endTime - startTime, result));
	out.printGolden(getGoldenActionProducedMessage());
	Thread.currentThread().sleep(timeouts.getTimeout("Waiter.AfterWaitingTime"));
	return(result);
    }

    /** 
     * @see Waitable
     */
    public Object actionProduced(Object obj) {
	return(new Boolean(true));
    }

    /** 
     * @see Waitable
     */
    public String getDescription() {
	return("Unknown waiting");
    }

    /**
     * Returns message to be printed before waiting start.
     */
    protected String getWaitingStartedMessage() {
	return("Start to wait action \"" + getActualDescription() + "\"");
    }

    /**
     * Returns message to be printed when waiting timeout has been expired.
     * @param spendedTime time from waiting start (milliseconds)
     */
    protected String getTimeoutExpiredMessage(long spendedTime) {
	return("\"" + getActualDescription() + "\" action has not been produced in " +
	       (new Long(spendedTime)).toString() + " milliseconds");
    }

    /**
     * Returns message to be printed when waiting has been successfully finished.
     * @param spendedTime time from waiting start (milliseconds)
     * @param result result of Waitable.actionproduced method.
     */
    protected String getActionProducedMessage(long spendedTime, Object result) {
	return("\"" + getActualDescription() + "\" action has been produced in " +
	       (new Long(spendedTime)).toString() + " milliseconds with result " +
	       "\n    : " + result.toString());
    }

    /**
     * Returns message to be printed int golden output before waiting start.
     */
    protected String getGoldenWaitingStartedMessage() {
	return("Start to wait action \"" + getActualDescription() + "\"");
    }

    /**
     * Returns message to be printed int golden output when waiting timeout has been expired.
     */
    protected String getGoldenTimeoutExpiredMessage() {
	return("\"" + getActualDescription() + "\" action has not been produced");
    }

    /**
     * Returns message to be printed int golden output when waiting has been successfully finished.
     */
    protected String getGoldenActionProducedMessage() {
	return("\"" + getActualDescription() + "\" action has been produced");
    }

    /**
     * Returns time from waiting start.
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
