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

import java.awt.AWTEvent;
import java.awt.Toolkit;

import java.awt.event.AWTEventListener;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.Vector;

/**
 *
 * Provides methods to check last dispatched events,
 * to wait for events of specific types, or to guarantee that events
 * of specific types are not dispatched during some time frame.
 * <BR><BR>
 * All possible listeners are added during this class initialization
 * in case if "jemmy.event_listening" system property is not equal to "no",
 * so, by default, all events are listened.
 *
 * Uses timeouts:<BR>
 * EventTool.WaitEventTimeout - time to wait for AWT events.<BR>
 * EventTool.WaitNoEventTimeout - when checking for the absence of incoming AWT
 *   events.<BR>
 * EventTool.EventCheckingDelta - time delta between checks for AWT events.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class EventTool implements Timeoutable, Outputable {

    private static final long WAIT_EVENT_TIMEOUT = 60000;
    private static final long WAIT_NO_EVENT_TIMEOUT = 180000;
    private static final long EVENT_CHECKING_DELTA = 10;

    private static ListenerSet listenerSet;
    private static long currentEventMask = 0;

    private TestOut output;
    private Timeouts timeouts;

    /**
     * Constructor.
     */
    public EventTool() {
	setOutput(JemmyProperties.getProperties().getOutput());
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
    }

    /**
     * Returns time of the last dispatched event under mask.
     * @param eventMask Events types to be searched. <code>AWTEvent.*_EVENT_MASK</code> fields combination.
     * @return time in milliseconds
     * @see #addListeners(long)
     */
    public static long getLastEventTime(long eventMask) {
	return(listenerSet.getLastEventTime(eventMask));
    }

    /**
     * Returns last dispatched event under mask.
     * @param eventMask Events types to be searched. <code>AWTEvent.*_EVENT_MASK</code> fields combination.
     * @return AWTEvent
     * @see #addListeners(long)
     */
    public static AWTEvent getLastEvent(long eventMask) {
	return(listenerSet.getLastEvent(eventMask));
    }

    /**
     * Returns time of the last dispatched event.
     * @return time in milliseconds
     * @see #addListeners(long)
     */
    public static long getLastEventTime() {
	return(getLastEventTime(listenerSet.getTheWholeMask()));
    }

    /**
     * Returns last dispatched event.
     * @return AWTEvent
     * @see #addListeners(long)
     */
    public static AWTEvent getLastEvent() {
	return(getLastEvent(listenerSet.getTheWholeMask()));
    }

    /**
     * Adds listeners to listen events under mask.
     * Invokes <code>removeListeners()</code> first, so any event history is lost.
     * @param eventMask Mask to listen events under. <code>AWTEvent.*_EVENT_MASK</code> fields combination.
     * @see #addListeners()
     * @see #removeListeners()
     */
    public static void addListeners(long eventMask) {
	removeListeners();
	listenerSet.addListeners(eventMask);
	currentEventMask = eventMask;
    }

    /**
     * Adds listeners to listen all types of events.
     * Invokes <code>removeListeners()</code> first, so any event history is lost.
     * This method is invoked during static section of this class.
     * @see #addListeners(long)
     * @see #removeListeners()
     * @see #getTheWholeEventMask()
     */
    public static void addListeners() {
	addListeners(listenerSet.getTheWholeMask());
    }

    /**
     * Removes all listeners.
     * @see #addListeners(long)
     * @see #addListeners()
     */
    public static void removeListeners() {
	listenerSet.removeListeners();
    }

    /**
     * Returns event mask last time used by <code>addListeners(long)</code> method.
     * In case if <code>addListeners()</code> method was used last, 
     * <code>getTheWholeEventMask() </code> result is returned.
     * @return a long representing the current event mask value
     * @see #getTheWholeEventMask()
     */
    public static long getCurrentEventMask() {
	return(currentEventMask);
    }

    /**
     * Returns a combination of all <code>AWTEvent.*_EVENT_MASK</code> fields..
     * @return a combination of all <code>AWTEvent.*_EVENT_MASK</code> fields.
     */
    public static long getTheWholeEventMask() {
	return(listenerSet.getTheWholeMask());
    }

    static {
	Timeouts.initDefault("EventTool.WaitEventTimeout", WAIT_EVENT_TIMEOUT);
	Timeouts.initDefault("EventTool.WaitNoEventTimeout", WAIT_NO_EVENT_TIMEOUT);
	Timeouts.initDefault("EventTool.EventCheckingDelta", EVENT_CHECKING_DELTA);
	listenerSet = new ListenerSet();
	if(System.getProperty("jemmy.event_listening") == null ||
	   !System.getProperty("jemmy.event_listening").equals("no")) {
	    listenerSet.addListeners();
	}
    }

    /**
     * Defines current timeouts.
     * 
     * @param	ts ?t? A collection of timeout assignments.
     * @see	org.netbeans.jemmy.Timeouts
     * @see	org.netbeans.jemmy.Timeoutable
     * @see #getTimeouts
     */
    public void setTimeouts(Timeouts ts) {
	timeouts = ts;
    }

    /**
     * Return current timeouts.
     * @return the collection of current timeout assignments.
     * @see org.netbeans.jemmy.Timeouts
     * @see org.netbeans.jemmy.Timeoutable
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
	output = out;
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
	return(output);
    }

    /**
     * Waits for the first event under mask.
     * Waits during <code>EventTool.WaitEventTimeout</code> milliseconds.
     * @param eventMask Mask to wait events under. 
     * <code>AWTEvent.*_EVENT_MASK</code> fields combination.
     * @return an AWTEvent object
     * @see #waitEvent()
     * @throws TimeoutExpiredException
     */
    public AWTEvent waitEvent(long eventMask) {
	return(waitEvent(eventMask, 
			 timeouts.getTimeout("EventTool.WaitEventTimeout"), 
			 output.createErrorOutput()));
    }

    /**
     * Waits for the first event.
     * Waits during <code>EventTool.WaitEventTimeout</code> milliseconds.
     * @return an AWTEvent object
     * @see #waitEvent(long)
     * @see #getTheWholeEventMask()
     * @throws TimeoutExpiredException
     */
    public AWTEvent waitEvent() {
	return(waitEvent(listenerSet.getTheWholeMask()));
    }

    /**
     * Check that no event under mask will be dispatched
     * during time specified.
     * @param eventMask Mask to wait events under. 
     * <code>AWTEvent.*_EVENT_MASK</code> fields combination.
     * @param waitTime Quiet time (millisecons).
     * @return true if no event ahs found.
     * @see #checkNoEvent(long)
     */
    public boolean checkNoEvent(long eventMask, long waitTime) {
	return(checkNoEvent(eventMask, waitTime, output));
    }

    /**
     * Check that no event will be dispatched during time specified.
     * @param waitTime Quiet time (millisecons).
     * @return true if no event ahs found.
     * @see #checkNoEvent(long, long)
     * @see #getTheWholeEventMask()
     */
    public boolean checkNoEvent(long waitTime) {
	return(checkNoEvent(listenerSet.getTheWholeMask(), waitTime));
    }

    /**
     * During <code>EventTool.WaitNoEventTimeout</code> time waits for
     * true result of checkNoEvent(long, long) method.
     * @param eventMask Mask to wait events under. 
     * <code>AWTEvent.*_EVENT_MASK</code> fields combination.
     * @param waitTime Quiet time (millisecons).
     * @see	#checkNoEvent(long, long)
     * @see	#waitNoEvent(long)
     * @throws	TimeoutExpiredException
     */
    public void waitNoEvent(long eventMask, long waitTime) {
	NoEventWaiter waiter = new NoEventWaiter(eventMask, waitTime);
	waiter.setTimeouts(timeouts.cloneThis());
	waiter.getTimeouts().
	    setTimeout("Waiter.WaitingTime", 
		       timeouts.getTimeout("EventTool.WaitNoEventTimeout"));
	waiter.getTimeouts().
	    setTimeout("Waiter.TimeDelta", 
		       timeouts.getTimeout("EventTool.EventCheckingDelta"));
	try {
	    waiter.waitAction(null);
	} catch(InterruptedException e) {
	    output.printStackTrace(e);
	}
    }

    /**
     * During <code>EventTool.WaitNoEventTimeout</code> time waits for
     * true result of <code>checkNoEvent(long)</code> method.
     * @param waitTime Quiet time (millisecons).
     * @see	#checkNoEvent(long)
     * @see	#waitNoEvent(long, long)
     * @throws	TimeoutExpiredException
     */
    public void waitNoEvent(long waitTime) {
	waitNoEvent(listenerSet.getTheWholeMask(), waitTime);
    }

    private AWTEvent waitEvent(long eventMask, long waitTime, TestOut waiterOutput) {
	EventWaiter waiter = new EventWaiter(eventMask);
	waiter.setTimeouts(timeouts.cloneThis());
	waiter.setOutput(waiterOutput);
	waiter.getTimeouts().
	    setTimeout("Waiter.WaitingTime",
		       waitTime);
	waiter.getTimeouts().
	    setTimeout("Waiter.TimeDelta",
		       timeouts.getTimeout("EventTool.EventCheckingDelta"));
	try {
	    return((AWTEvent)waiter.waitAction(null));
	} catch(InterruptedException e) {
	    output.printStackTrace(e);
	    return(null);
	}
    }

    private boolean checkNoEvent(long eventMask, long waitTime, TestOut waiterOutput) {
	try {
	    AWTEvent event = waitEvent(eventMask, waitTime, TestOut.getNullOutput());
	    waiterOutput.printLine("AWT event was produced during waiting: ");
            // used instead of event.toString() because it is not thread safe
            waiterOutput.printLine(event.getClass().getName());
	    return(false);
	} catch(TimeoutExpiredException e) {
	    return(true);
	}
    }

    private static class EventType implements AWTEventListener {
	long eventMask;
	long eventTime;
	private AWTEvent event;
	public EventType(long eventMask) {
	    this.eventMask = eventMask;
	    event = null;
	    eventTime = -1;
	}
	public void eventDispatched(AWTEvent event) {
	    this.event = event;
	    eventTime = System.currentTimeMillis();
	}
	public AWTEvent getEvent() {
	    return(event);
	}
	public long getTime() {
	    return(eventTime);
	}
	public long getEventMask() {
	    return(eventMask);
	}
    }

    private static class ListenerSet {
	private Vector eventTypes;
	private long theWholeMask;
	public ListenerSet() {
	    eventTypes = new Vector();
	    try {
		Class eventClass = Class.forName("java.awt.AWTEvent");
		Field[] fields = eventClass.getFields();
		theWholeMask = 0;
		long eventMask;
		for(int i = 0; i < fields.length; i++) {
		    if((fields[i].getModifiers() &
			(Modifier.PUBLIC | Modifier.STATIC)) != 0 &&
		       fields[i].getType().equals(Long.TYPE) &&
		       fields[i].getName().endsWith("_EVENT_MASK")) {
			eventMask = ((Long)fields[i].get(null)).longValue();
			eventTypes.add(new EventType(eventMask));
			theWholeMask = theWholeMask | eventMask;
		    }
		}
	    } catch(ClassNotFoundException e) {
		JemmyProperties.getCurrentOutput().printStackTrace(e);
	    } catch(IllegalAccessException e) {
		JemmyProperties.getCurrentOutput().printStackTrace(e);
	    }
	}
	public void addListeners(long eventMask) {
	    Toolkit dtk = Toolkit.getDefaultToolkit();
	    for(int i = 0; i < eventTypes.size(); i++) {
		EventType et = (EventType)eventTypes.get(i);
		if((et.getEventMask() & eventMask) != 0) {
		    dtk.addAWTEventListener(et, et.getEventMask());
		}
	    }
	}
	public void addListeners() {
	    addListeners(getTheWholeMask());
	}
	public void removeListeners() {
	    Toolkit dtk = Toolkit.getDefaultToolkit();
	    for(int i = 0; i < eventTypes.size(); i++) {
		dtk.removeAWTEventListener((EventType)eventTypes.get(i));
	    }
	}
	public long getTheWholeMask() {
	    return(theWholeMask);
	}
	public long getLastEventTime(long eventMask) {
	    EventType et = getLastEventType(eventMask);
	    return((et == null) ? -1 : et.getTime());
	}
	public AWTEvent getLastEvent(long eventMask) {
	    EventType et = getLastEventType(eventMask);
	    return((et == null) ? null : et.getEvent());
	}
	private EventType getLastEventType(long eventMask) {
	    long maxTime = -1;
	    EventType maxType = null;
	    for(int i = 0; i < eventTypes.size(); i++) {
		EventType et = (EventType)eventTypes.get(i);
		if((eventMask & et.getEventMask()) != 0 &&
		   et.getTime() > maxTime) {
		    maxType = et;
		    maxTime = maxType.getTime();
		}
	    }
	    return(maxType);
	}
    }

    private class  EventWaiter extends Waiter {
	long eventMask;
	long startTime;
	public EventWaiter(long eventMask) {
	    this.eventMask = eventMask;
	    startTime = getLastEventTime(eventMask);
	}
	public Object actionProduced(Object obj) {
	    EventType et = listenerSet.getLastEventType(eventMask);
	    if(et != null &&
	       et.getTime() > startTime) {
		return(et.getEvent());
	    } else {
		return(null);
	    }
	}
	public String getDescription() {
	    return("Last event under " + 
		   Long.toString(eventMask, 2) + " event mask");
	}
    }

    private class NoEventWaiter extends Waiter {
	long eventMask;
	long waitTime;
	public NoEventWaiter(long eventMask, long waitTime) {
	    this.eventMask = eventMask;
	    this.waitTime = waitTime;
	}
	public Object actionProduced(Object obj) {
	    return(checkNoEvent(eventMask, waitTime, TestOut.getNullOutput()) ?
		   "Reached!" :
		   null);
	}
	public String getDescription() {
	    return("No event under " + 
		   Long.toString(eventMask, 2) + 
		   " event mask during " +
		   Long.toString(waitTime) + 
		   " milliseconds");
	}
    }
}
