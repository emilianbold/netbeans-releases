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

package org.netbeans.jemmy.util;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.WindowWaiter;

import java.awt.Window;

import java.util.Vector;

/**
 * Class allows to make periodical window jobs like error window closing.
 * @see WindowJob
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class WindowManager implements Timeoutable, Outputable {

    /**
     * Default value for WindowManager.TimeDelta timeout
     */
    public static long TIME_DELTA = 1000;
    private static WindowManager manager;

    private Vector jobs;
    private Timeouts timeouts;
    private TestOut output;

    private WindowManager() {
	super();
	setTimeouts(JemmyProperties.getCurrentTimeouts());
	setOutput(JemmyProperties.getCurrentOutput());
	jobs = new Vector();
    }

    /**
     * Adds job to list.
     */
    public static void addJob(WindowJob job) {
	manager.add(job);
    }

    /**
     * Removes job from list
     */
    public static void removeJob(WindowJob job) {
	manager.remove(job);
    }

    static {
	Timeouts.initDefault("WindowManager.TimeDelta", TIME_DELTA);
	manager = new WindowManager();
    }

    /**
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
    }

    /**
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public Timeouts getTimeouts() {
	return(timeouts);
    }

    /**
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     */
    public void setOutput(TestOut output) {
	this.output = output;
    }

    /**
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     */
    public TestOut getOutput() {
	return(output);
    }

    /**
     * Adds job to list.
     */
    public void add(WindowJob job) {
	output.printLine("Starting job \"" + 
			 job.getDescription() +
			 "\"");
	synchronized(jobs) {
	    JobThread thread = new JobThread(job);
	    jobs.add(thread);
	    thread.start();
	}
    }

    /**
     * Removes job from list
     */
    public void remove(WindowJob job) {
	output.printLine("Killing job \"" + 
			 job.getDescription() +
			 "\"");
	synchronized(jobs) {
	    for(int i = 0; i < jobs.size(); i++) {
		if(((JobThread)jobs.get(i)).job == job) {
		    ((JobThread)jobs.get(i)).needStop = true;
		    jobs.remove(i);
		}
	    }
	}
    }

    private static class JobThread extends Thread {
	WindowJob job;
	boolean needStop = false;
	public JobThread(WindowJob job) {
	    this.job = job;
	}
	private boolean getNS() {
	    synchronized(this) {
		return(needStop);
	    }
	}
	public void run() {
	    while(!getNS()) {
		Window win = WindowWaiter.getWindow(job);
		if(win != null) {
		    job.launch(win);
		}
		manager.timeouts.sleep("WindowManager.TimeDelta");
	    }
	}
    }
		
}
