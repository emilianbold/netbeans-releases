/*
 * ElapsedTimer.java
 *
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2000-2001 Sun
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
