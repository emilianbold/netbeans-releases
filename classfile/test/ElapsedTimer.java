/*
 * ElapsedTimer.java
 *
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2000-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

/**
 * A simple class for calculating and reporting elapsed system time.
 * Each timer has a start time and may have an end time.  If an 
 * elapsed time value is requested of a timer which doesn't have its
 * stop time set, the current system time is used.
 *
 * @author Tom Ball
 */
public class ElapsedTimer {
    // System times in milliseconds; see System.currentTimeMillis();
    private long startTime;
    private long stopTime;

    /**
     * Create a new timer.
     */
    public ElapsedTimer() {
	reset();
    }

    /**
     * Stop the current timer; that is, set its stopTime.
     */
    public void stop() {
	stopTime = System.currentTimeMillis();
    }

    /**
     * Reset the starting time to the current system time.
     */
    public final void reset() {
	startTime = System.currentTimeMillis();
    }

    public long getElapsedMilliseconds() {
	long st = (stopTime == 0) ? System.currentTimeMillis() : stopTime;
	return st - startTime;
    }

    public int getElapsedSeconds() {
	return (int)(getElapsedMilliseconds() / 1000L);
    }

    public String toString() {
	long ms = getElapsedMilliseconds();
	int sec = (int)(ms / 1000L);
	int frac = (int)(ms % 1000L);
	StringBuffer sb = new StringBuffer();
	sb.append(ms / 1000L);
	sb.append('.');
	sb.append(ms % 1000L);
	sb.append(" seconds");
	return sb.toString();
    }
}
