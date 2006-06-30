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

/**
 *
 * Runs actions with or without waiting.
 *
 * <BR><BR>Timeouts used: <BR>
 * ActionProducer.MaxActionTime - time action should be finished in. <BR>
 *
 * @see Action
 * @see Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class ActionProducer extends Thread
    implements Action, Waitable, Timeoutable{

    private final static long ACTION_TIMEOUT = 10000;

    private Action action;
    private boolean needWait = true;
    private Object parameter;
    private boolean finished;
    private Object result = null;
    private Timeouts timeouts;
    private Waiter waiter;
    private TestOut output;
    private Throwable exception;

    /**
     * Creates a producer for an action.
     * @param a Action implementation.
     */
    public ActionProducer(Action a) {
	super();
	waiter = new Waiter(this);
	action = a;
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
	finished = false;
        exception = null;
    }

    /**
     * Creates a producer for an action.
     * @param a Action implementation.
     * @param nw Defines if <code>produceAction</code> 
     * method should wait for the end of action.
     */
    public ActionProducer(Action a, boolean nw) {
	super();
	waiter = new Waiter(this);
	action = a;
	needWait = nw;
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
	finished = false;
        exception = null;
    }

    /**
     * Creates a producer.
     * <code>produceAction</code> must be overridden.
     */
    protected ActionProducer() {
	super();
	waiter = new Waiter(this);
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
	finished = false;
        exception = null;
    }

    /**
     * Creates a producer.
     * <code>produceAction</code> must be overridden.
     * @param nw Defines if <code>produceAction</code> 
     * method should wait for the end of action.
     */
    protected ActionProducer(boolean nw) {
	super();
	waiter = new Waiter(this);
	needWait = nw;
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
	finished = false;
        exception = null;
    }

    static {
	Timeouts.initDefault("ActionProducer.MaxActionTime", ACTION_TIMEOUT);
    }

    /**
     * Set all the time outs used by sleeps or waits used by the launched action.
     * @param ts An object containing timeout information.
     * @see org.netbeans.jemmy.Timeouts
     * @see org.netbeans.jemmy.Timeoutable
     * @see #getTimeouts
     */
    public void setTimeouts(Timeouts ts) {
	timeouts = ts;
    }

    /**
     * Get all the time outs used by sleeps or waits used by the launched action.
     * @return an object containing information about timeouts.
     * @see org.netbeans.jemmy.Timeouts
     * @see org.netbeans.jemmy.Timeoutable
     * @see #setTimeouts
     */
    public Timeouts getTimeouts() {
	return(timeouts);
    }

    /**
     * Identity of the streams or writers used for print output.
     * @param out  An object containing print output assignments for
     * output and error streams.
     * @see org.netbeans.jemmy.TestOut
     * @see org.netbeans.jemmy.Outputable
     */
    public void setOutput(TestOut out) {
	output = out;
	waiter.setOutput(output);
    }

    /**                            
     * Returns the exception value.
     * @return a Throwable object representing the exception value
     */    
    public Throwable getException() {
        return(exception);
    }

    /**
     * Defines action priority in terms of thread priority.
     * Increase (decrease) parameter value to Thread.MIN_PRIORITY(MAX_PRIORITY)
     * in case if it is less(more) then it.
     * 
     * @param	newPriority New thread priority.
     */
    public void setActionPriority(int newPriority) {
	int priority;
	if(newPriority < Thread.MIN_PRIORITY) {
	    priority = MIN_PRIORITY;
	} else if(newPriority > Thread.MAX_PRIORITY) {
	    priority = MAX_PRIORITY;
	} else {
	    priority = newPriority;
	}
	try {
	    setPriority(priority);
	} catch(IllegalArgumentException e) {
	} catch(SecurityException e) {
	}
    }

    /**
     * Get the result of a launched action.
     * @return a launched action's result.
     * without waiting in case if <code>getFinished()</code>
     * @see #getFinished()
     */
    public Object getResult() {
	return(result);
    }

    /** 
     * Check if a launched action has finished.
     * @return <code>true</code> if the launched action has completed,
     * either normally or with an exception;  <code>false</code> otherwise.
     */
    public boolean getFinished() {
	synchronized(this) {
	    return(finished);
	}
    }

    /**
     * Does nothing; the method should be overridden by inheritors.
     * @param obj An object used to modify execution.  This might be a
     * <code>java.lang.String[]</code> that lists a test's command line
     * arguments.
     * @return An object - result of the action.
     * @see org.netbeans.jemmy.Action
     */
    public Object launch(Object obj) {
	return(null);
    }

    /**
     * @return this <code>ActionProducer</code>'s description.
     * @see Action
     */
    public String getDescription() {
	if(action != null) {
	    return(action.getDescription());
	} else {
	    return("Unknown action");
	}
    }

    /**
     * Starts execution.
     * Uses ActionProducer.MaxActionTime timeout.
     * 
     * @param	obj Parameter to be passed into action's <code>launch(Object)</code> method.
     * This parameter might be a <code>java.lang.String[]</code> that lists a test's
     * command line arguments.
     * @return	<code>launch(Object)</code> result.
     * @throws	TimeoutExpiredException
     * @exception	InterruptedException
     */
    public Object produceAction(Object obj) throws InterruptedException{
	parameter =obj;
	synchronized(this) {
	    finished = false;
	}
	start();
	if(needWait) {
	    Timeouts times = timeouts.cloneThis();
	    times.setTimeout("Waiter.WaitingTime", 
			     timeouts.getTimeout("ActionProducer.MaxActionTime"));
	    waiter.setTimeouts(times);
	    try {
		waiter.waitAction(null);
	    } catch(TimeoutExpiredException e) {
		output.printError("Timeout for \"" + getDescription() + 
				  "\" action has been expired. Thread has been interrupted.");
		interrupt();
		throw(e);
	    }
	}
	return(result);
    }

    /**
     * Launch an action in a separate thread of execution.
     * When the action finishes, record that fact.  If the action finishes
     * normally, store it's result.  Use <code>getFinished()</code>
     * and <code>getResult</code> to answer questions about test
     * completion and return value, respectively.
     * @see #getFinished()
     * @see #getResult()
     * @see java.lang.Runnable
     */
    public final void run() {
	result = null;
	try {
	    result = launchAction(parameter);
	} catch(Throwable e) {
            exception = e;
	}
	synchronized(this) {
	    finished = true;
	}
    }

    /**
     * Inquire for a reference to the object returned by a launched action.
     * @param obj Not used.
     * @return the result returned when a launched action finishes
     * normally.
     * @see org.netbeans.jemmy.Waitable
     */
    public final Object actionProduced(Object obj) {
	synchronized(this) {
	    if(finished) {
		if(result == null) {
		    return(new Integer(0));
		} else {
		    return(result);
		}
	    } else {
		return(null);
	    }
	}
    }
    /**
     * Launch some action.
     * Pass the action parameters and get it's return value, too.
     * @param obj Parameter used to configure the execution of whatever
     * this <code>ActionProducer</code> puts into execution.
     * This parameter might be a <code>java.lang.String[]</code> that lists a
     * test's command line arguments.
     * @return the return value of the action.  This might be a
     * <code>java.lang.Integer</code> wrapped around a status code.
     */
    private Object launchAction(Object obj) {
	if(action != null) {
	    return(action.launch(obj));
	} else {
	    return(launch(obj));
	}
    }
}

