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
 * Represents one timeout.
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class Timeout extends Object {
    private String name;
    private long value;
    private long startTime;

    /**
     * Constructor.
     * @param name Timeout name.
     * @param value Timeout value in milliseconds.
     */
    public Timeout(String name, long value) {
	this.name = name;
	this.value = value;
    }

    /**
     * Returns timeout name.
     * @return timeout name.
     */
    public String getName() {
	return(name);
    }

    /**
     * Returns timeout value.
     * @return timeout value.
     */
    public long getValue() {
	return(value);
    }

    /**
     * Sleeps for timeout value.
     */
    public void sleep() {
	if(getValue()>0) {
	    try {
		Thread.currentThread().sleep(getValue());
	    } catch(InterruptedException e) {
		throw(new JemmyException("Sleep " +
					 getName() + 
					 " was interrupted!",
					 e));
	    }
	}
    }

    /**
     * Starts timeout measuring.
     */
    public void start() {
	startTime = System.currentTimeMillis();
    }

    /**
     * Checks if timeout has been expired after start() invocation.
     * @return true if timeout has been expired.
     */
    public boolean expired() {
	return(System.currentTimeMillis() - startTime > getValue());
    }

    /**
     * Throws a TimeoutExpiredException exception if timeout has been expired.
     * @throws TimeoutExpiredException if timeout has been expired after start() invocation.
     */
    public void check() {
	if(expired()) {
	    throw(new TimeoutExpiredException(getName() +
					      " timeout expired!"));
	}
    }
}
