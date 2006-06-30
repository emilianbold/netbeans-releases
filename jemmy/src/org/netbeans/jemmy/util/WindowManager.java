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

package org.netbeans.jemmy.util;

import java.awt.Component;
import java.awt.Dialog;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.WindowWaiter;

import org.netbeans.jemmy.operators.WindowOperator;

import java.awt.Window;

import java.util.Vector;

/**
 * Class allows to make periodical window jobs like error window closing.
 * @see WindowJob
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class WindowManager implements Timeoutable, Outputable {

    /**
     * Default value for WindowManager.TimeDelta timeout.
     */
    private static long TIME_DELTA = 1000;

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
     * @param job  a job to perform.
     */
    public static void addJob(WindowJob job) {
	manager.add(job);
    }

    /**
     * Removes job from list.
     * @param job  a job to remove.
     */
    public static void removeJob(WindowJob job) {
	manager.remove(job);
    }

    public static void performJob(WindowJob job) {
        while(manager.performJobOnce(job)) {
        }
    }

    static {
	Timeouts.initDefault("WindowManager.TimeDelta", TIME_DELTA);
	manager = new WindowManager();
    }

    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
    }

    public Timeouts getTimeouts() {
	return(timeouts);
    }

    public void setOutput(TestOut output) {
	this.output = output;
    }

    public TestOut getOutput() {
	return(output);
    }

    /**
     * Adds job to list.
     * @param job  a job to perform.
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
     * Removes job from list.
     * @param job  a job to remove.
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

    private boolean performJobOnce(WindowJob job) {
        Window win = WindowWaiter.getWindow(job);
        if(win != null) {
            job.launch(win);
            return(true);
        } else {
            return(false);
        }
    }

    public static class ModalDialogChoosingJob implements WindowJob {
        public boolean checkComponent(Component comp) {
            return(comp instanceof Dialog &&
                   ((Dialog)comp).isModal());
        }
        public Object launch(Object obj) {
            new WindowOperator((Window)obj).close();
            return(null);
        }
        public String getDescription() {
            return("A job of closing modal dialogs");
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
                manager.performJobOnce(job);
                manager.timeouts.sleep("WindowManager.TimeDelta");
	    }
	}
    }
		
}
